package br.com.ajudamutua.hardening;

import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.observability.ReconciliationMetrics;
import br.com.ajudamutua.payment.PaymentProvider;
import br.com.ajudamutua.payment.PaymentProviderGateway;
import br.com.ajudamutua.payment.PaymentSettlementService;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AuditService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationJobObservabilityTest {
    @Mock PaymentAttemptRepository payments;
    @Mock AuditService audit;
    @Mock PaymentProviderGateway provider;
    @Mock PaymentSettlementService settlement;

    private SimpleMeterRegistry registry;
    private ReconciliationMetrics metrics;
    private ReconciliationJob job;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new ReconciliationMetrics(registry);
        job = new ReconciliationJob(payments, audit, provider, settlement, metrics, Duration.ofMinutes(10));
    }

    @Test
    void providerUnavailableLeavesPaymentForReconciliationAndEmitsMetric() {
        PaymentAttempt payment = processingAttempt("sandbox-unavailable");
        when(payments.findByStatusInAndUpdatedAtBefore(any(), any())).thenReturn(List.of(payment));
        when(provider.queryStatus("sandbox-unavailable")).thenThrow(new RuntimeException("timeout"));

        job.reconcile();

        assertEquals(PaymentStatus.RECONCILIATION_REQUIRED, payment.getStatus());
        assertEquals(1.0, registry.get("fazerobem.reconciliation.scanned.total").counter().count());
        assertEquals(1.0, registry.get("fazerobem.reconciliation.provider_unavailable.total").counter().count());
        verify(settlement, never()).settle(any(), anyString(), anyString());
        verify(audit).append(eq(null), eq("PAYMENT_RECONCILIATION_PROVIDER_UNAVAILABLE"),
                eq("PaymentAttempt"), eq(payment.getId()), anyString());
    }

    @Test
    void externallySettledPaymentDelegatesToSingleSettlementPathAndEmitsMetric() {
        PaymentAttempt payment = processingAttempt("sandbox-settled");
        when(payments.findByStatusInAndUpdatedAtBefore(any(), any())).thenReturn(List.of(payment));
        when(provider.queryStatus("sandbox-settled")).thenReturn(new PaymentProvider.StatusResult(
                PaymentProvider.ExternalStatus.SETTLED, "sandbox-settled", "request-1"));

        job.reconcile();

        verify(settlement).settle(payment, "sandbox-settled", "RECONCILIATION");
        assertEquals(1.0, registry.get("fazerobem.reconciliation.settled.total").counter().count());
    }

    @Test
    void unknownExternalStateProducesDivergenceWithoutSettlement() {
        PaymentAttempt payment = processingAttempt("sandbox-unknown");
        when(payments.findByStatusInAndUpdatedAtBefore(any(), any())).thenReturn(List.of(payment));
        when(provider.queryStatus("sandbox-unknown")).thenReturn(new PaymentProvider.StatusResult(
                PaymentProvider.ExternalStatus.UNKNOWN, "sandbox-unknown", "request-2"));

        job.reconcile();

        assertEquals(PaymentStatus.RECONCILIATION_REQUIRED, payment.getStatus());
        assertEquals(1.0, registry.get("fazerobem.reconciliation.divergence.total").counter().count());
        verify(settlement, never()).settle(any(), anyString(), anyString());
    }

    private PaymentAttempt processingAttempt(String providerReference) {
        PaymentAttempt payment = new PaymentAttempt(UUID.randomUUID(), UUID.randomUUID(), "key-" + UUID.randomUUID(),
                "SANDBOX", PaymentStatus.READY, new BigDecimal("10.00"), UUID.randomUUID(), Instant.now());
        payment.processing(providerReference);
        return payment;
    }
}
