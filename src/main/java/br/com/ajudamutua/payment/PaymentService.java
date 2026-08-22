package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentInitiationService initiation;
    private final PaymentWebhookService webhooks;
    private final PaymentAttemptRepository attempts;
    private final CurrentUserService current;
    private final AuditService audit;

    public PaymentService(PaymentInitiationService initiation,
                          PaymentWebhookService webhooks,
                          PaymentAttemptRepository attempts,
                          CurrentUserService current,
                          AuditService audit) {
        this.initiation = initiation;
        this.webhooks = webhooks;
        this.attempts = attempts;
        this.current = current;
        this.audit = audit;
    }

    public PaymentAttempt initiate(UUID aidId, String idempotencyKey) {
        return initiation.initiate(aidId, idempotencyKey);
    }

    public void handleWebhook(String eventId,
                              String timestamp,
                              String signature,
                              String body,
                              String providerReference,
                              String status) {
        webhooks.handle(eventId, timestamp, signature, body, providerReference, status);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public List<PaymentAttempt> attempts(UUID aidId) {
        return attempts.findByAidRequestIdOrderByUpdatedAtDesc(aidId);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentAttempt acknowledgeReconciliation(UUID paymentId, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Nota de reconciliação obrigatória");
        }
        PaymentAttempt payment = attempts.findById(paymentId).orElseThrow();
        if (payment.getStatus() != PaymentStatus.RECONCILIATION_REQUIRED) {
            throw new IllegalStateException("Pagamento não está aguardando reconciliação");
        }
        var actor = current.require();
        String safe = note.trim().replace("\\", "\\\\").replace("\"", "\\\"");
        audit.append(actor.getId(), "PAYMENT_RECONCILIATION_ACKNOWLEDGED", "PaymentAttempt", paymentId,
                "{\"aidRequestId\":\"" + payment.getAidRequestId() + "\",\"note\":\"" + safe + "\"}");
        return payment;
    }
}
