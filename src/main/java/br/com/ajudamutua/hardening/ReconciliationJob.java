package br.com.ajudamutua.hardening;

import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.observability.ReconciliationMetrics;
import br.com.ajudamutua.payment.PaymentProvider;
import br.com.ajudamutua.payment.PaymentProviderGateway;
import br.com.ajudamutua.payment.PaymentSettlementService;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class ReconciliationJob {
    private final PaymentAttemptRepository payments;
    private final AuditService audit;
    private final PaymentProviderGateway provider;
    private final PaymentSettlementService settlement;
    private final ReconciliationMetrics metrics;
    private final Duration scanAfter;

    public ReconciliationJob(PaymentAttemptRepository payments,
                             AuditService audit,
                             PaymentProviderGateway provider,
                             PaymentSettlementService settlement,
                             ReconciliationMetrics metrics,
                             @Value("${app.reconciliation.scan-after:PT10M}") Duration scanAfter) {
        this.payments = payments;
        this.audit = audit;
        this.provider = provider;
        this.settlement = settlement;
        this.metrics = metrics;
        this.scanAfter = scanAfter;
    }

    @Scheduled(fixedDelayString = "${app.reconciliation.fixed-delay-ms:60000}")
    @Transactional
    public void reconcile() {
        Instant cutoff = Instant.now().minus(scanAfter);
        for (var payment : payments.findByStatusInAndUpdatedAtBefore(
                List.of(PaymentStatus.PROCESSING, PaymentStatus.RECONCILIATION_REQUIRED), cutoff)) {
            metrics.scanned();

            if (payment.getProviderReference() == null || payment.getProviderReference().isBlank()) {
                payment.requireReconciliation();
                metrics.divergence();
                audit.append(null, "PAYMENT_RECONCILIATION_REQUIRED", "PaymentAttempt", payment.getId(),
                        "{\"reason\":\"missing_provider_reference\"}");
                continue;
            }

            PaymentProvider.StatusResult external;
            long started = System.nanoTime();
            try {
                external = provider.queryStatus(payment.getProviderReference());
            } catch (RuntimeException ex) {
                metrics.recordProviderQuery(System.nanoTime() - started);
                metrics.providerUnavailable();
                payment.requireReconciliation();
                audit.append(null, "PAYMENT_RECONCILIATION_PROVIDER_UNAVAILABLE", "PaymentAttempt", payment.getId(),
                        "{\"providerReference\":\"" + payment.getProviderReference() + "\"}");
                continue;
            }
            metrics.recordProviderQuery(System.nanoTime() - started);

            switch (external.status()) {
                case SETTLED -> {
                    settlement.settle(payment, payment.getProviderReference(), "RECONCILIATION");
                    metrics.settled();
                }
                case FAILED -> {
                    payment.fail("Provider confirmed failure during reconciliation");
                    metrics.failed();
                }
                case PROCESSING -> {
                    if (payment.getStatus() != PaymentStatus.RECONCILIATION_REQUIRED) {
                        payment.requireReconciliation();
                    }
                    metrics.pending();
                    audit.append(null, "PAYMENT_RECONCILIATION_PENDING", "PaymentAttempt", payment.getId(),
                            "{\"providerRequestId\":\"" + external.providerRequestId() + "\"}");
                }
                case UNKNOWN -> {
                    payment.requireReconciliation();
                    metrics.divergence();
                    audit.append(null, "PAYMENT_RECONCILIATION_DIVERGENCE", "PaymentAttempt", payment.getId(),
                            "{\"providerRequestId\":\"" + external.providerRequestId() + "\"}");
                }
            }
        }
    }
}
