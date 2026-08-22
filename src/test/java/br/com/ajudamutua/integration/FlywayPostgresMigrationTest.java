package br.com.ajudamutua.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class FlywayPostgresMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ajuda_mutua_test")
            .withUsername("ajuda_test")
            .withPassword("test-only-password");

    @Test
    void appliesAllMigrationsAndCreatesCriticalTables() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        var result = flyway.migrate();
        assertTrue(result.migrationsExecuted >= 7, "Expected migrations V1 through V7 to be applied");

        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {

            assertTableExists(statement, "members");
            assertTableExists(statement, "aid_requests");
            assertTableExists(statement, "ledger_entries");
            assertTableExists(statement, "audit_events");
            assertTableExists(statement, "payment_attempts");
            assertTableExists(statement, "account_recovery_requests");

            try (ResultSet rs = statement.executeQuery(
                    "select count(*) from flyway_schema_history where success = true")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 7, "Flyway history must contain all successful migrations");
            }
        }
    }

    private static void assertTableExists(Statement statement, String table) throws Exception {
        try (ResultSet rs = statement.executeQuery(
                "select count(*) from information_schema.tables " +
                        "where table_schema = 'public' and table_name = '" + table + "'")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "Missing expected table: " + table);
        }
    }
}
