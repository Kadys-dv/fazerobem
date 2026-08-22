package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SandboxPaymentProviderContractTest {

    @Test
    void sameIdempotencyKeyReturnsSameProviderReference() {
        SandboxPaymentProvider provider = new SandboxPaymentProvider();
        UUID paymentId = UUID.randomUUID();

        var first = provider.initiate(paymentId, new BigDecimal("10.00"), "idem-1");
        var second = provider.initiate(UUID.randomUUID(), new BigDecimal("10.00"), "idem-1");

        assertEquals(first.providerReference(), second.providerReference());
        assertEquals(first.providerRequestId(), second.providerRequestId());
    }

    @Test
    void statusCanBeQueriedAndEvolvesExternally() {
        SandboxPaymentProvider provider = new SandboxPaymentProvider();
        var initiation = provider.initiate(UUID.randomUUID(), new BigDecimal("25.00"), "idem-2");

        assertEquals(PaymentProvider.ExternalStatus.PROCESSING,
                provider.queryStatus(initiation.providerReference()).status());

        provider.simulateStatus(initiation.providerReference(), PaymentProvider.ExternalStatus.SETTLED);
        assertEquals(PaymentProvider.ExternalStatus.SETTLED,
                provider.queryStatus(initiation.providerReference()).status());
    }

    @Test
    void gatewayRetriesTransientFailureWithSameIdempotencyKey() {
        AtomicInteger calls = new AtomicInteger();
        PaymentProvider flaky = new PaymentProvider() {
            @Override
            public Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey) {
                if (calls.incrementAndGet() < 3) throw new IllegalStateException("temporary");
                return new Initiation("provider-ref", idempotencyKey);
            }

            @Override
            public StatusResult queryStatus(String providerReference) {
                return new StatusResult(ExternalStatus.PROCESSING, "status-1", "ok");
            }
        };

        PaymentProviderGateway gateway = new PaymentProviderGateway(flaky, 3, 1000, 0);
        var result = gateway.initiate(UUID.randomUUID(), new BigDecimal("5.00"), "stable-key");

        assertEquals("provider-ref", result.providerReference());
        assertEquals("stable-key", result.providerRequestId());
        assertEquals(3, calls.get());
    }
}
