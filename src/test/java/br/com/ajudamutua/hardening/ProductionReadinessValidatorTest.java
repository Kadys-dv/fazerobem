package br.com.ajudamutua.hardening;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProductionReadinessValidatorTest {

    @Test
    void rejectsIncompleteProductionConfiguration() {
        var env = new MockEnvironment();
        var error = assertThrows(IllegalStateException.class, () -> ProductionReadinessValidator.validate(env));
        assertTrue(error.getMessage().contains("Missing required production property"));
    }

    @Test
    void rejectsLocalhostAndInsecureWebauthnConfiguration() {
        var env = validEnvironment()
                .withProperty("app.production.public-base-url", "http://localhost:8080")
                .withProperty("app.webauthn.rp-id", "localhost")
                .withProperty("app.webauthn.allowed-origins", "http://localhost:8080");
        assertThrows(IllegalStateException.class, () -> ProductionReadinessValidator.validate(env));
    }

    @Test
    void rejectsLocalKmsInProduction() {
        var env = validEnvironment().withProperty("app.kms.provider", "local");
        var error = assertThrows(IllegalStateException.class, () -> ProductionReadinessValidator.validate(env));
        assertTrue(error.getMessage().contains("KMS_PROVIDER=local"));
    }

    @Test
    void acceptsCoherentAwsKmsHttpsConfiguration() {
        assertDoesNotThrow(() -> ProductionReadinessValidator.validate(validEnvironment()));
    }

    private MockEnvironment validEnvironment() {
        return new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://db.internal:5432/fazerobem")
                .withProperty("spring.datasource.username", "fazerobem")
                .withProperty("spring.datasource.password", "synthetic-db-secret")
                .withProperty("spring.data.redis.host", "redis.internal")
                .withProperty("spring.data.redis.port", "6379")
                .withProperty("spring.data.redis.password", "synthetic-redis-secret")
                .withProperty("server.servlet.session.cookie.secure", "true")
                .withProperty("app.crypto.key-id", "prod-key-v1")
                .withProperty("app.sandbox.webhook-secret", "synthetic-sandbox-secret")
                .withProperty("app.security.mfa-required-for-privileged", "true")
                .withProperty("app.production.tls-required", "true")
                .withProperty("app.production.public-base-url", "https://app.fazerobem.example")
                .withProperty("app.webauthn.rp-name", "Fazer o Bem")
                .withProperty("app.webauthn.rp-id", "fazerobem.example")
                .withProperty("app.webauthn.allowed-origins", "https://app.fazerobem.example")
                .withProperty("app.kms.provider", "aws")
                .withProperty("app.kms.aws-key-id", "arn:aws:kms:us-east-1:111122223333:key/example");
    }
}
