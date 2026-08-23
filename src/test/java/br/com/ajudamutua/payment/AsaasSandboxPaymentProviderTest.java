package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsaasSandboxPaymentProviderTest {

    @Test
    void rejectsAnyNonSandboxBaseUrl() {
        assertThrows(IllegalStateException.class, () ->
                new AsaasSandboxPaymentProvider("https://api.asaas.com/v3", "$aact_hmlg_test", "test"));
    }

    @Test
    void rejectsMissingSandboxApiKey() {
        assertThrows(IllegalStateException.class, () ->
                new AsaasSandboxPaymentProvider(AsaasSandboxPaymentProvider.SANDBOX_BASE_URL, "", "test"));
    }

    @Test
    void rejectsProductionKeyPrefix() {
        assertThrows(IllegalStateException.class, () ->
                new AsaasSandboxPaymentProvider(
                        AsaasSandboxPaymentProvider.SANDBOX_BASE_URL, "$aact_prod_test", "test"));
    }

    @Test
    void mapsTransferStatusesConservatively() {
        assertEquals(PaymentProvider.ExternalStatus.PROCESSING,
                AsaasSandboxPaymentProvider.mapStatus("PENDING"));
        assertEquals(PaymentProvider.ExternalStatus.PROCESSING,
                AsaasSandboxPaymentProvider.mapStatus("BANK_PROCESSING"));
        assertEquals(PaymentProvider.ExternalStatus.SETTLED,
                AsaasSandboxPaymentProvider.mapStatus("DONE"));
        assertEquals(PaymentProvider.ExternalStatus.FAILED,
                AsaasSandboxPaymentProvider.mapStatus("CANCELLED"));
        assertEquals(PaymentProvider.ExternalStatus.FAILED,
                AsaasSandboxPaymentProvider.mapStatus("FAILED"));
        assertEquals(PaymentProvider.ExternalStatus.UNKNOWN,
                AsaasSandboxPaymentProvider.mapStatus("NEW_STATUS_FROM_PROVIDER"));
    }
}
