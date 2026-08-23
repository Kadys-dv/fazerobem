package br.com.ajudamutua.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentProviderGateway {
    private final PaymentProvider provider;
    private final PaymentDestinationService destinations;
    private final int maxAttempts;
    private final Duration timeout;
    private final long backoffMs;

    @Autowired
    public PaymentProviderGateway(PaymentProvider provider,
                                  PaymentDestinationService destinations,
                                  @Value("${app.payment-provider.max-attempts:3}") int maxAttempts,
                                  @Value("${app.payment-provider.timeout-ms:2000}") long timeoutMs,
                                  @Value("${app.payment-provider.backoff-ms:100}") long backoffMs) {
        this.provider = provider;
        this.destinations = destinations;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.timeout = Duration.ofMillis(Math.max(100, timeoutMs));
        this.backoffMs = Math.max(0, backoffMs);
    }

    PaymentProviderGateway(PaymentProvider provider, int maxAttempts, long timeoutMs, long backoffMs) {
        this.provider = provider;
        this.destinations = null;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.timeout = Duration.ofMillis(Math.max(100, timeoutMs));
        this.backoffMs = Math.max(0, backoffMs);
    }

    public PaymentProvider.Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey) {
        return retry(() -> provider.initiate(paymentId, amount, idempotencyKey));
    }

    public PaymentProvider.Initiation initiate(UUID paymentId,
                                               UUID memberId,
                                               BigDecimal amount,
                                               String idempotencyKey) {
        if (!(provider instanceof DestinationAwarePaymentProvider destinationAware)) {
            return initiate(paymentId, amount, idempotencyKey);
        }
        if (destinations == null) {
            throw new IllegalStateException("Serviço de destino de pagamento indisponível");
        }
        try (ResolvedPaymentDestination destination = destinations.resolveActive(memberId)) {
            return retry(() -> destinationAware.initiateWithDestination(
                    paymentId, amount, idempotencyKey, destination));
        }
    }

    public PaymentProvider.StatusResult queryStatus(String providerReference) {
        return retry(() -> provider.queryStatus(providerReference));
    }

    private <T> T retry(java.util.concurrent.Callable<T> operation) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return CompletableFuture.supplyAsync(() -> {
                            try {
                                return operation.call();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .join();
            } catch (RuntimeException ex) {
                last = ex;
                if (attempt < maxAttempts && backoffMs > 0) {
                    try {
                        Thread.sleep(backoffMs * attempt);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Provider retry interrompido", interrupted);
                    }
                }
            }
        }
        throw new IllegalStateException("Provider indisponível após retries", last);
    }
}
