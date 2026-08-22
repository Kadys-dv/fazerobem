package br.com.ajudamutua.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SandboxPaymentProvider implements PaymentProvider {
    private final Map<String, Initiation> byIdempotencyKey = new ConcurrentHashMap<>();
    private final Map<String, ExternalStatus> statuses = new ConcurrentHashMap<>();

    @Override
    public Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey) {
        return byIdempotencyKey.computeIfAbsent(idempotencyKey, key -> {
            String reference = "sandbox-" + paymentId;
            String requestId = "req-" + UUID.randomUUID();
            statuses.put(reference, ExternalStatus.PROCESSING);
            return new Initiation(reference, requestId);
        });
    }

    @Override
    public StatusResult queryStatus(String providerReference) {
        ExternalStatus status = statuses.getOrDefault(providerReference, ExternalStatus.UNKNOWN);
        return new StatusResult(status, "status-" + providerReference, "sandbox");
    }

    void simulateStatus(String providerReference, ExternalStatus status) {
        statuses.put(providerReference, status);
    }
}
