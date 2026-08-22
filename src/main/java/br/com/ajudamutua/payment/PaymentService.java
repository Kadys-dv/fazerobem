package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentInitiationService initiation;
    private final PaymentWebhookService webhooks;
    private final PaymentAttemptRepository attempts;

    public PaymentService(PaymentInitiationService initiation,
                          PaymentWebhookService webhooks,
                          PaymentAttemptRepository attempts) {
        this.initiation = initiation;
        this.webhooks = webhooks;
        this.attempts = attempts;
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
}
