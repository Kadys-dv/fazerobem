package br.com.ajudamutua.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FinancialConcurrencyIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ajuda_mutua_concurrency")
            .withUsername("ajuda_concurrency")
            .withPassword("concurrency-test-only");

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
        registry.add("app.sandbox.webhook-secret", () -> "concurrency-test-webhook-secret");
        registry.add("app.kms.provider", () -> "local");
        registry.add("management.tracing.enabled", () -> "false");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

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
                memberId, "Concurrency Member", "member-" + memberId + "@test.local", "ACTIVE", now, "VERIFIED");
        jdbc.update("insert into app_users(id,email,password_hash,role,enabled,created_at) values (?,?,?,?,?,?)",
                adminId, "admin-" + adminId + "@test.local", "test", "ADMIN", true, now);
        jdbc.update("insert into aid_requests(id,member_id,amount,reason,status,created_at,category,emergency,version) values (?,?,?,?,?,?,?,?,?)",
                aidId, memberId, new BigDecimal("10.00"), "concurrency test", "APPROVED", now, "HEALTH", true, 0L);
    }

    @Test
    void aidRowLockSerializesTwoTransactions() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        CountDownLatch firstHasLock = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = pool.submit(() -> tx.executeWithoutResult(status -> {
                jdbc.queryForObject("select id from aid_requests where id = ? for update", UUID.class, aidId);
                firstHasLock.countDown();
                await(releaseFirst);
            }));

            assertTrue(firstHasLock.await(5, TimeUnit.SECONDS), "Primeira transação não adquiriu o lock");

            CountDownLatch secondStarted = new CountDownLatch(1);
            Future<?> second = pool.submit(() -> tx.executeWithoutResult(status -> {
                secondStarted.countDown();
                jdbc.queryForObject("select id from aid_requests where id = ? for update", UUID.class, aidId);
            }));

            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            Thread.sleep(250);
            assertFalse(second.isDone(), "A segunda transação não deveria atravessar o lock enquanto a primeira está aberta");

            releaseFirst.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void databaseRejectsTwoActiveAttemptsForSameAid() {
        insertAttempt(UUID.randomUUID(), "key-a-" + aidId, "READY");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertAttempt(UUID.randomUUID(), "key-b-" + aidId, "PROCESSING"));

        Integer count = jdbc.queryForObject(
                "select count(*) from payment_attempts where aid_request_id = ?", Integer.class, aidId);
        assertEquals(1, count);
    }

    private void insertAttempt(UUID paymentId, String key, String status) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbc.update("insert into payment_attempts(id,aid_request_id,idempotency_key,provider,status,amount,initiated_by_user_id,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,?)",
                paymentId, aidId, key, "SANDBOX", status, new BigDecimal("10.00"), adminId, now, now, 0L);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout aguardando liberação do lock de teste");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
