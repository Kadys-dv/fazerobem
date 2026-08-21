package br.com.ajudamutua.integration;
import org.junit.jupiter.api.Test; import org.testcontainers.containers.GenericContainer; import org.testcontainers.containers.PostgreSQLContainer; import org.testcontainers.junit.jupiter.*; import org.testcontainers.utility.DockerImageName; import static org.junit.jupiter.api.Assertions.*;
@Testcontainers class PostgresRedisContainerSmokeTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
 @Container static GenericContainer<?> redis=new GenericContainer<>(DockerImageName.parse("redis:8-alpine")).withExposedPorts(6379);
 @Test void containersAreReachable(){assertTrue(postgres.isRunning());assertTrue(redis.isRunning());assertNotNull(postgres.getJdbcUrl());assertTrue(redis.getMappedPort(6379)>0);}
}
