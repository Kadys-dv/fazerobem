package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.model.WebhookEvent;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.repository.WebhookEventRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentWebhookService {
    private final PaymentAttemptRepository attempts;
    private final WebhookEventRepository webhooks;
    private final WebhookSignatureService signatures;
    private final PaymentSettlementService settlement;

    @Autowired
    public PaymentWebhookService(PaymentAttemptRepository attempts,
                                 WebhookEventRepository webhooks,
                                 WebhookSignatureService signatures,
                                 PaymentSettlementService settlement) {
        this.attempts = attempts;
        this.webhooks = webhooks;
        this.signatures = signatures;
        this.settlement = settlement;
    }

    PaymentWebhookService(AidRequestRepository aids,
                          PaymentAttemptRepository attempts,
                          WebhookEventRepository webhooks,
                          OutboxEventRepository outbox,
                          LedgerService ledger,
                          AuditService audit,
                          WebhookSignatureService signatures) {
        this(attempts, webhooks, signatures, new PaymentSettlementService(aids, outbox, ledger, audit));
    }

    @Transactional
    public void handle(String eventId, String timestamp, String signature, String body,
                       String providerReference, String providerStatus) {
        validateAuthenticity(eventId, timestamp, signature, body);
        WebhookEvent event = webhooks.saveAndFlush(new WebhookEvent(
                UUID.randomUUID(), "SANDBOX", eventId, WebhookSignatureService.sha256(body),
                Instant.parse(timestamp), Instant.now()));
        PaymentAttempt payment = attempts.findByProviderReferenceForUpdate(providerReference)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento desconhecido"));
        if ("SETTLED".equalsIgnoreCase(providerStatus)) settlement.settle(payment, providerReference, "WEBHOOK");
        else if ("FAILED".equalsIgnoreCase(providerStatus)) payment.fail("Sandbox provider failure");
        else payment.requireReconciliation();
        event.processed();
    }

    private void validateAuthenticity(String eventId, String timestamp, String signature, String body) {
        if (!signatures.verify(timestamp, body, signature)) throw new IllegalStateException("Assinatura de webhook inválida");
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("X-Event-Id obrigatório");
        if (webhooks.existsByEventId(eventId)) throw new IllegalStateException("Webhook replay detectado");
    }
}
