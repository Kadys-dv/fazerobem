package br.com.ajudamutua.observability;

import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationalMetricsTest {

    @Test
    void exportsOperationalPaymentAndOutboxGauges() {
        PaymentAttemptRepository payments = mock(PaymentAttemptRepository.class);
        OutboxEventRepository outbox = mock(OutboxEventRepository.class);

        when(payments.countByStatusAndUpdatedAtBefore(eq(PaymentStatus.PROCESSING), any(Instant.class)))
                .thenReturn(2L, 1L);
        when(payments.countByStatus(PaymentStatus.RECONCILIATION_REQUIRED)).thenReturn(3L);
        when(payments.countByStatusAndUpdatedAtBefore(eq(PaymentStatus.RECONCILIATION_REQUIRED), any(Instant.class)))
                .thenReturn(1L);
        when(payments.countByStatus(PaymentStatus.FAILED)).thenReturn(4L);
        when(outbox.countByPublishedAtIsNull()).thenReturn(5L);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        new OperationalMetrics(payments, outbox, Duration.ofMinutes(10), Duration.ofMinutes(30)).bindTo(registry);

        assertEquals(2.0, registry.get("fazerobem.payment.processing.stuck").gauge().value());
        assertEquals(1.0, registry.get("fazerobem.payment.processing.critical").gauge().value());
        assertEquals(3.0, registry.get("fazerobem.payment.reconciliation.required").gauge().value());
        assertEquals(1.0, registry.get("fazerobem.payment.reconciliation.aged").gauge().value());
        assertEquals(4.0, registry.get("fazerobem.payment.failed.current").gauge().value());
        assertEquals(5.0, registry.get("fazerobem.outbox.pending").gauge().value());
    }
}
