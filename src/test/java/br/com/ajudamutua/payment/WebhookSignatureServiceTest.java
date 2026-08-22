package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookSignatureServiceTest {
    private static final String TEST_SIGNING_MATERIAL = "unit test webhook signing material";
    private static final String BODY = "{\"providerReference\":\"sandbox-x\",\"status\":\"SETTLED\"}";

    @Test
    void validSignatureIsAccepted() {
        var service = new WebhookSignatureService(TEST_SIGNING_MATERIAL);
        String timestamp = Instant.now().toString();
        String signature = service.sign(timestamp + "." + BODY);

        assertTrue(service.verify(timestamp, BODY, signature));
    }

    @Test
    void invalidOrMissingSignatureIsRejected() {
        var service = new WebhookSignatureService(TEST_SIGNING_MATERIAL);
        String timestamp = Instant.now().toString();

        assertFalse(service.verify(timestamp, BODY, "00"));
        assertFalse(service.verify(timestamp, BODY, ""));
        assertFalse(service.verify(timestamp, BODY, null));
    }

    @Test
    void expiredWebhookIsRejectedEvenWithCorrectSignature() {
        var service = new WebhookSignatureService(TEST_SIGNING_MATERIAL);
        String timestamp = Instant.now().minusSeconds(301).toString();
        String signature = service.sign(timestamp + "." + BODY);

        assertFalse(service.verify(timestamp, BODY, signature));
    }

    @Test
    void futureWebhookOutsideClockSkewIsRejectedEvenWithCorrectSignature() {
        var service = new WebhookSignatureService(TEST_SIGNING_MATERIAL);
        String timestamp = Instant.now().plusSeconds(301).toString();
        String signature = service.sign(timestamp + "." + BODY);

        assertFalse(service.verify(timestamp, BODY, signature));
    }

    @Test
    void tamperedBodyIsRejected() {
        var service = new WebhookSignatureService(TEST_SIGNING_MATERIAL);
        String timestamp = Instant.now().toString();
        String signature = service.sign(timestamp + "." + BODY);
        String tampered = "{\"providerReference\":\"sandbox-x\",\"status\":\"FAILED\"}";

        assertFalse(service.verify(timestamp, tampered, signature));
    }

    @Test
    void missingOrShortSigningMaterialFailsClosed() {
        String timestamp = Instant.now().toString();

        assertFalse(new WebhookSignatureService("").verify(timestamp, BODY, "00"));
        assertFalse(new WebhookSignatureService("short").verify(timestamp, BODY, "00"));
    }
}
