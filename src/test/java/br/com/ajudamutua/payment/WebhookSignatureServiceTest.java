package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSignatureServiceTest {
    private static final String SECRET = "1234567890abcdef1234567890abcdef";
    private static final String BODY = "{\"providerReference\":\"sandbox-x\",\"status\":\"SETTLED\"}";

    @Test
    void validSignatureIsAccepted() {
        var service = new WebhookSignatureService(SECRET);
        String timestamp = Instant.now().toString();
        String signature = service.sign(timestamp + "." + BODY);

        assertTrue(service.verify(timestamp, BODY, signature));
    }

    @Test
    void invalidSignatureIsRejected() {
        var service = new WebhookSignatureService(SECRET);
        String timestamp = Instant.now().toString();

        assertFalse(service.verify(timestamp, BODY, "00"));
        assertFalse(service.verify(timestamp, BODY, ""));
    }

    @Test
    void expiredWebhookIsRejectedEvenWithCorrectSignature() {
        var service = new WebhookSignatureService(SECRET);
        String timestamp = Instant.now().minusSeconds(6 * 60).toString();
        String signature = service.sign(timestamp + "." + BODY);

        assertFalse(service.verify(timestamp, BODY, signature));
    }

    @Test
    void futureWebhookOutsideClockSkewIsRejectedEvenWithCorrectSignature() {
        var service = new WebhookSignatureService(SECRET);
        String timestamp = Instant.now().plusSeconds(6 * 60).toString();
        String signature = service.sign(timestamp + "." + BODY);

        assertFalse(service.verify(timestamp, BODY, signature));
    }

    @Test
    void tamperedBodyIsRejected() {
        var service = new WebhookSignatureService(SECRET);
        String timestamp = Instant.now().toString();
        String signature = service.sign(timestamp + "." + BODY);
        String tampered = "{\"providerReference\":\"sandbox-x\",\"status\":\"FAILED\"}";

        assertFalse(service.verify(timestamp, tampered, signature));
    }

    @Test
    void shortOrMissingSecretFailsClosed() {
        String timestamp = Instant.now().toString();

        assertFalse(new WebhookSignatureService("").verify(timestamp, BODY, "00"));
        assertFalse(new WebhookSignatureService("short").verify(timestamp, BODY, "00"));
    }
}
