package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDestinationAdminControllerTest {
    @Test
    void requestToStringNeverExposesRawPixKey() {
        var request = new PaymentDestinationAdminController.SavePixDestinationRequest();
        request.setKeyType(PaymentDestination.PixKeyType.EMAIL);
        request.setPixKey("sandbox-secret-pix@example.invalid");

        String rendered = request.toString();
        assertTrue(rendered.contains("[REDACTED]"));
        assertFalse(rendered.contains("sandbox-secret-pix@example.invalid"));
    }
}
