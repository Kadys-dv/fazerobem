package br.com.ajudamutua.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FreshDatabaseApplicationStartupTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ajuda_mutua_startup")
            .withUsername("ajuda_startup")
            .withPassword("startup-test-only");

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
        registry.add("app.sandbox.webhook-secret", () -> "startup-test-webhook-secret");
        registry.add("app.kms.provider", () -> "local");
        registry.add("management.tracing.enabled", () -> "false");
    }

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void contextStartsAfterFlywayMigratesFreshDatabase() {
        Integer migrations = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true", Integer.class);
        Integer recoveryTable = jdbc.queryForObject(
                "select count(*) from information_schema.tables where table_schema='public' and table_name='account_recovery_approvals'",
                Integer.class);
        Integer paymentVersionColumn = jdbc.queryForObject(
                "select count(*) from information_schema.columns where table_schema='public' and table_name='payment_attempts' and column_name='version'",
                Integer.class);

        assertEquals(9, migrations);
        assertEquals(1, recoveryTable);
        assertEquals(1, paymentVersionColumn);
    }
}
