package br.com.ajudamutua.payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
    Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey);

    StatusResult queryStatus(String providerReference);

    default String providerCode() {
        return "SANDBOX";
    }

    record Initiation(String providerReference, String providerRequestId) {}

    record StatusResult(ExternalStatus status, String providerRequestId, String detail) {}

    enum ExternalStatus {
        PROCESSING,
        SETTLED,
        FAILED,
        UNKNOWN
    }
}
