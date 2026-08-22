package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.*;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.repository.WebhookEventRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentWebhookService {
    private final AidRequestRepository aids;
    private final PaymentAttemptRepository attempts;
    private final WebhookEventRepository webhooks;
    private final OutboxEventRepository outbox;
    private final LedgerService ledger;
    private final AuditService audit;
    private final WebhookSignatureService signatures;

    public PaymentWebhookService(AidRequestRepository aids,
                                 PaymentAttemptRepository attempts,
                                 WebhookEventRepository webhooks,
                                 OutboxEventRepository outbox,
                                 LedgerService ledger,
                                 AuditService audit,
                                 WebhookSignatureService signatures) {
        this.aids = aids;
        this.attempts = attempts;
        this.webhooks = webhooks;
        this.outbox = outbox;
        this.ledger = ledger;
        this.audit = audit;
        this.signatures = signatures;
    }

    @Transactional
    public void handle(String eventId,
                       String timestamp,
                       String signature,
                       String body,
                       String providerReference,
                       String providerStatus) {
        validateAuthenticity(eventId, timestamp, signature, body);

        WebhookEvent event = webhooks.save(new WebhookEvent(
                UUID.randomUUID(), "SANDBOX", eventId, WebhookSignatureService.sha256(body),
                Instant.parse(timestamp), Instant.now()));

        PaymentAttempt payment = attempts.findByProviderReference(providerReference)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento desconhecido"));

        if ("SETTLED".equalsIgnoreCase(providerStatus)) {
            settle(payment, providerReference);
        } else if ("FAILED".equalsIgnoreCase(providerStatus)) {
            payment.fail("Sandbox provider failure");
        } else {
            payment.requireReconciliation();
        }

        event.processed();
    }

    private void validateAuthenticity(String eventId, String timestamp, String signature, String body) {
        if (!signatures.verify(timestamp, body, signature)) {
            throw new IllegalStateException("Assinatura de webhook inválida");
        }
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("X-Event-Id obrigatório");
        }
        if (webhooks.existsByEventId(eventId)) {
            throw new IllegalStateException("Webhook replay detectado");
        }
    }

    private void settle(PaymentAttempt payment, String providerReference) {
        payment.settle();
        AidRequest aid = aids.findById(payment.getAidRequestId()).orElseThrow();
        if (aid.getStatus() == AidStatus.PAID) {
            return;
        }

        LedgerEntry entry = ledger.append(
                LedgerType.AID_PAYMENT,
                aid.getAmount().negate(),
                aid.getMemberId(),
                aid.getId(),
                "Auxílio comunitário liquidado via sandbox");

        aid.markPaid();
        outbox.save(new OutboxEvent(
                UUID.randomUUID(), "AidRequest", aid.getId(), "AID_SETTLED",
                "{\"ledgerEntryId\":\"" + entry.getId() + "\"}", Instant.now()));
        audit.append(null, "PAYMENT_SETTLED", "AidRequest", aid.getId(),
                "{\"providerReference\":\"" + providerReference + "\"}");
    }
}
