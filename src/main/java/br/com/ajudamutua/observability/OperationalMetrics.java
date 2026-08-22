package br.com.ajudamutua.observability;

import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class OperationalMetrics implements MeterBinder {
    private final PaymentAttemptRepository payments;
    private final OutboxEventRepository outbox;
    private final Duration stuckAfter;

    public OperationalMetrics(PaymentAttemptRepository payments,
                              OutboxEventRepository outbox,
                              @Value("${app.observability.payment-stuck-after:PT10M}") Duration stuckAfter) {
        this.payments = payments;
        this.outbox = outbox;
        this.stuckAfter = stuckAfter;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("fazerobem.payment.processing.stuck", payments,
                        repository -> repository.countByStatusAndUpdatedAtBefore(
                                PaymentStatus.PROCESSING, Instant.now().minus(stuckAfter)))
                .description("Payment attempts still PROCESSING beyond the operational threshold")
                .register(registry);

        Gauge.builder("fazerobem.payment.reconciliation.required", payments,
                        repository -> repository.countByStatus(PaymentStatus.RECONCILIATION_REQUIRED))
                .description("Payment attempts requiring manual reconciliation")
                .register(registry);

        Gauge.builder("fazerobem.payment.failed.current", payments,
                        repository -> repository.countByStatus(PaymentStatus.FAILED))
                .description("Payment attempts currently in FAILED state")
                .register(registry);

        Gauge.builder("fazerobem.outbox.pending", outbox, OutboxEventRepository::countByPublishedAtIsNull)
                .description("Unpublished outbox events waiting for delivery")
                .register(registry);
    }
}
