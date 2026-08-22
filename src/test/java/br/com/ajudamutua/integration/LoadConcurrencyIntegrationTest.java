package br.com.ajudamutua.integration;

import br.com.ajudamutua.model.LedgerType;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LoadConcurrencyIntegrationTest {

    private static final int THREADS = 12;
    private static final int CHAIN_OPERATIONS = 80;
    private static final int REDIS_OPERATIONS = 600;
    private static final int PAYMENT_RACERS = 20;
    private static final String GENESIS = "0".repeat(64);

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ajuda_mutua_load")
            .withUsername("ajuda_load")
            .withPassword("load-test-only");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("app.crypto.master-key-base64", () -> Base64.getEncoder().encodeToString(new byte[32]));
        registry.add("app.sandbox.webhook-secret", () -> "load-test-webhook-secret");
        registry.add("app.kms.provider", () -> "local");
        registry.add("management.tracing.enabled", () -> "false");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired LedgerService ledger;
    @Autowired AuditService audit;
    @Autowired StringRedisTemplate redisTemplate;

    private UUID memberId;
    private UUID aidId;
    private UUID adminId;

    @BeforeEach
    void seed() {
        memberId = UUID.randomUUID();
        aidId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbc.update("insert into members(id,name,email,status,joined_at,kyc_status) values (?,?,?,?,?,?)",
                memberId, "Load Member", "member-" + memberId + "@test.local", "ACTIVE", now, "VERIFIED");
        jdbc.update("insert into app_users(id,email,password_hash,role,enabled,created_at) values (?,?,?,?,?,?)",
                adminId, "admin-" + adminId + "@test.local", "test", "ADMIN", true, now);
        jdbc.update("insert into aid_requests(id,member_id,amount,reason,status,created_at,category,emergency,version) values (?,?,?,?,?,?,?,?,?)",
                aidId, memberId, new BigDecimal("10.00"), "load test", "APPROVED", now, "HEALTH", true, 0L);
    }

    @Test
    void concurrentFinancialIntegrityGate() throws Exception {
        Metric ledgerMetric = runConcurrent("ledger", CHAIN_OPERATIONS, () -> {
            ledger.append(LedgerType.ADJUSTMENT, BigDecimal.ZERO, memberId, aidId, "load-ledger-" + UUID.randomUUID());
            return null;
        });
        assertChainGraph("ledger_entries", "entry_hash", "previous_hash", CHAIN_OPERATIONS);

        Metric auditMetric = runConcurrent("audit", CHAIN_OPERATIONS, () -> {
            audit.append(adminId, "LOAD_TEST", "AidRequest", aidId, "{\"run\":\"" + UUID.randomUUID() + "\"}");
            return null;
        });
        assertChainGraph("audit_events", "event_hash", "previous_hash", CHAIN_OPERATIONS);

        String redisKey = "phase8:load:" + UUID.randomUUID();
        Metric redisMetric = runConcurrent("redis", REDIS_OPERATIONS, () -> {
            redisTemplate.opsForValue().increment(redisKey);
            return null;
        });
        assertEquals(REDIS_OPERATIONS, Integer.parseInt(redisTemplate.opsForValue().get(redisKey)));

        AtomicInteger inserted = new AtomicInteger();
        Metric paymentMetric = runConcurrent("payment-race", PAYMENT_RACERS, () -> {
            try {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                jdbc.update("insert into payment_attempts(id,aid_request_id,idempotency_key,provider,status,amount,initiated_by_user_id,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,?)",
                        UUID.randomUUID(), aidId, "load-key-" + UUID.randomUUID(), "SANDBOX", "READY",
                        new BigDecimal("10.00"), adminId, now, now, 0L);
                inserted.incrementAndGet();
            } catch (RuntimeException ignored) {
                // Esperado para perdedores da corrida: a constraint ativa protege exactly-once.
            }
            return null;
        });
        assertEquals(1, inserted.get(), "Somente uma tentativa financeira ativa deve sobreviver à corrida");
        Integer active = jdbc.queryForObject("select count(*) from payment_attempts where aid_request_id=? and status in ('READY','PROCESSING','SETTLED','RECONCILIATION_REQUIRED')", Integer.class, aidId);
        assertEquals(1, active);

        writeReport(List.of(ledgerMetric, auditMetric, redisMetric, paymentMetric));
    }

    private Metric runConcurrent(String name, int operations, Callable<Void> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger errors = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < operations; i++) {
                futures.add(pool.submit(() -> {
                    start.await(10, TimeUnit.SECONDS);
                    long begin = System.nanoTime();
                    try {
                        task.call();
                    } catch (Exception e) {
                        errors.incrementAndGet();
                        throw e;
                    } finally {
                        latencies.add(System.nanoTime() - begin);
                    }
                    return null;
                }));
            }
            long wallStart = System.nanoTime();
            start.countDown();
            for (Future<?> future : futures) future.get(45, TimeUnit.SECONDS);
            long elapsed = System.nanoTime() - wallStart;
            return Metric.of(name, operations, elapsed, latencies, errors.get());
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    private void assertChainGraph(String table, String hashColumn, String previousColumn, int minimumEntries) {
        List<ChainRow> rows = jdbc.query("select " + hashColumn + "," + previousColumn + " from " + table,
                (rs, rowNum) -> new ChainRow(rs.getString(1), rs.getString(2)));
        assertTrue(rows.size() >= minimumEntries);
        var hashes = rows.stream().map(ChainRow::hash).collect(java.util.stream.Collectors.toSet());
        long genesisLinks = rows.stream().filter(r -> GENESIS.equals(r.previousHash())).count();
        assertEquals(1L, genesisLinks, "A cadeia deve ter exatamente um elo genesis em " + table);
        for (ChainRow row : rows) {
            if (!GENESIS.equals(row.previousHash())) {
                assertTrue(hashes.contains(row.previousHash()), "previous_hash órfão em " + table);
            }
        }
        Integer forks = jdbc.queryForObject("select count(*) from (select " + previousColumn + " from " + table + " where " + previousColumn + " <> ? group by " + previousColumn + " having count(*) > 1) x", Integer.class, GENESIS);
        assertEquals(0, forks, "A cadeia não pode ter forks em " + table);
    }

    private void writeReport(List<Metric> metrics) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("# Phase 8 Load & Concurrency Report");
        lines.add("");
        lines.add("| Scenario | Ops | Throughput ops/s | p95 ms | p99 ms | Errors |");
        lines.add("|---|---:|---:|---:|---:|---:|");
        metrics.forEach(metric -> lines.add(metric.row()));
        lines.add("");
        lines.add("Validated invariants: ledger chain intact; audit chain intact; Redis increments exact; exactly one active payment attempt after concurrent race.");
        Path output = Path.of("target", "load-concurrency-report.md");
        Files.createDirectories(output.getParent());
        Files.write(output, lines);
        System.out.println(String.join(System.lineSeparator(), lines));
    }

    private record ChainRow(String hash, String previousHash) {}

    private record Metric(String name, int operations, double throughput, double p95Ms, double p99Ms, int errors) {
        static Metric of(String name, int operations, long elapsedNanos, List<Long> latencyNanos, int errors) {
            List<Long> sorted = new ArrayList<>(latencyNanos);
            Collections.sort(sorted);
            double seconds = elapsedNanos / 1_000_000_000.0;
            return new Metric(name, operations, operations / seconds,
                    percentile(sorted, 0.95) / 1_000_000.0,
                    percentile(sorted, 0.99) / 1_000_000.0,
                    errors);
        }
        private static long percentile(List<Long> values, double p) {
            int index = Math.max(0, (int) Math.ceil(values.size() * p) - 1);
            return values.get(index);
        }
        String row() {
            return String.format("| %s | %d | %.2f | %.2f | %.2f | %d |", name, operations, throughput, p95Ms, p99Ms, errors);
        }
    }
}
