package br.com.ajudamutua.integration;
import org.junit.jupiter.api.Test; import org.testcontainers.junit.jupiter.Testcontainers; import org.testcontainers.containers.PostgreSQLContainer; import static org.junit.jupiter.api.Assertions.*;
@Testcontainers(disabledWithoutDocker=true) class PostgresContainerSmokeTest { @Test void postgresStarts(){try(var pg=new PostgreSQLContainer<>("postgres:16-alpine")){pg.start();assertTrue(pg.isRunning());}} }
