package br.com.ajudamutua.hardening;

import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.payment.PaymentProvider;
import br.com.ajudamutua.payment.PaymentProviderGateway;
import br.com.ajudamutua.payment.PaymentSettlementService;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AuditService;
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

    public ReconciliationJob(PaymentAttemptRepository payments,
                             AuditService audit,
                             PaymentProviderGateway provider,
                             PaymentSettlementService settlement) {
        this.payments = payments;
        this.audit = audit;
        this.provider = provider;
        this.settlement = settlement;
    }

    @Scheduled(fixedDelayString = "${app.reconciliation.fixed-delay-ms:60000}")
    @Transactional
    public void reconcile() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(10));
        for (var payment : payments.findByStatusInAndUpdatedAtBefore(
                List.of(PaymentStatus.PROCESSING, PaymentStatus.RECONCILIATION_REQUIRED), cutoff)) {
            if (payment.getProviderReference() == null || payment.getProviderReference().isBlank()) {
                payment.requireReconciliation();
                audit.append(null, "PAYMENT_RECONCILIATION_REQUIRED", "PaymentAttempt", payment.getId(),
                        "{\"reason\":\"missing_provider_reference\"}");
                continue;
            }

            PaymentProvider.StatusResult external;
            try {
                external = provider.queryStatus(payment.getProviderReference());
            } catch (RuntimeException ex) {
                payment.requireReconciliation();
                audit.append(null, "PAYMENT_RECONCILIATION_PROVIDER_UNAVAILABLE", "PaymentAttempt", payment.getId(), "{}");
                continue;
            }

            switch (external.status()) {
                case SETTLED -> settlement.settle(payment, payment.getProviderReference(), "RECONCILIATION");
                case FAILED -> payment.fail("Provider confirmed failure during reconciliation");
                case PROCESSING -> {
                    if (payment.getStatus() != PaymentStatus.RECONCILIATION_REQUIRED) {
                        payment.requireReconciliation();
                    }
                    audit.append(null, "PAYMENT_RECONCILIATION_PENDING", "PaymentAttempt", payment.getId(),
                            "{\"providerRequestId\":\"" + external.providerRequestId() + "\"}");
                }
                case UNKNOWN -> {
                    payment.requireReconciliation();
                    audit.append(null, "PAYMENT_RECONCILIATION_DIVERGENCE", "PaymentAttempt", payment.getId(),
                            "{\"providerRequestId\":\"" + external.providerRequestId() + "\"}");
                }
            }
        }
    }
}
