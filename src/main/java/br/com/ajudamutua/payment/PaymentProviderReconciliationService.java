package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentProviderReconciliationService {
    private final PaymentAttemptRepository attempts;
    private final PaymentProviderGateway gateway;
    private final PaymentSettlementService settlement;
    private final CurrentUserService current;
    private final AuditService audit;

    public PaymentProviderReconciliationService(PaymentAttemptRepository attempts,
                                                PaymentProviderGateway gateway,
                                                PaymentSettlementService settlement,
                                                CurrentUserService current,
                                                AuditService audit) {
        this.attempts = attempts;
        this.gateway = gateway;
        this.settlement = settlement;
        this.current = current;
        this.audit = audit;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentAttempt refresh(UUID paymentId) {
        PaymentAttempt payment = attempts.findByIdForUpdate(paymentId).orElseThrow();
        if (payment.getStatus() == PaymentStatus.SETTLED || payment.getStatus() == PaymentStatus.FAILED) {
            return payment;
        }
        String ref = payment.getProviderReference();
        if (ref == null || ref.isBlank()) {
            payment.requireReconciliation();
            appendAudit(payment, "UNKNOWN", "missing-provider-reference");
            return payment;
        }

        PaymentProvider.StatusResult remote;
        try {
            remote = gateway.queryStatus(ref);
        } catch (RuntimeException ex) {
            payment.requireReconciliation();
            appendAudit(payment, "UNKNOWN", "provider-query-failed");
            return payment;
        }

        switch (remote.status()) {
            case SETTLED -> settlement.settle(payment, ref, "PROVIDER_RECONCILIATION");
            case FAILED -> payment.fail("Provider confirmou falha");
            case PROCESSING -> {
                if (payment.getStatus() == PaymentStatus.RECONCILIATION_REQUIRED) {
                    // Keep manual attention while the provider is still processing an uncertain initiation.
                    payment.requireReconciliation();
                }
            }
            case UNKNOWN -> payment.requireReconciliation();
        }
        appendAudit(payment, remote.status().name(), "provider-status-refresh");
        return payment;
    }

    private void appendAudit(PaymentAttempt payment, String remoteStatus, String source) {
        UUID actor = current.require().getId();
        audit.append(actor, "PAYMENT_PROVIDER_STATUS_REFRESHED", "PaymentAttempt", payment.getId(),
                "{\"aidRequestId\":\"" + payment.getAidRequestId() + "\",\"remoteStatus\":\"" + remoteStatus
                        + "\",\"source\":\"" + source + "\"}");
    }
}
