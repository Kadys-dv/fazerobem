package br.com.ajudamutua.payment;

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
    private final int maxAttempts;
    private final Duration timeout;
    private final long backoffMs;

    public PaymentProviderGateway(PaymentProvider provider,
                                  @Value("${app.payment-provider.max-attempts:3}") int maxAttempts,
                                  @Value("${app.payment-provider.timeout-ms:2000}") long timeoutMs,
                                  @Value("${app.payment-provider.backoff-ms:100}") long backoffMs) {
        this.provider = provider;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.timeout = Duration.ofMillis(Math.max(100, timeoutMs));
        this.backoffMs = Math.max(0, backoffMs);
    }

    public PaymentProvider.Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey) {
        return retry(() -> provider.initiate(paymentId, amount, idempotencyKey));
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
