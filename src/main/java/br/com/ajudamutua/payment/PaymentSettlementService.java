package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.AidRequest;
import br.com.ajudamutua.model.AidStatus;
import br.com.ajudamutua.model.LedgerEntry;
import br.com.ajudamutua.model.LedgerType;
import br.com.ajudamutua.model.OutboxEvent;
import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.LedgerService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentSettlementService {
    private final AidRequestRepository aids;
    private final OutboxEventRepository outbox;
    private final LedgerService ledger;
    private final AuditService audit;

    public PaymentSettlementService(AidRequestRepository aids,
                                    OutboxEventRepository outbox,
                                    LedgerService ledger,
                                    AuditService audit) {
        this.aids = aids;
        this.outbox = outbox;
        this.ledger = ledger;
        this.audit = audit;
    }

    public void settle(PaymentAttempt payment, String providerReference, String source) {
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
                "Auxílio comunitário liquidado após confirmação externa sandbox");

        aid.markPaid();
        outbox.save(new OutboxEvent(
                UUID.randomUUID(), "AidRequest", aid.getId(), "AID_SETTLED",
                "{\"ledgerEntryId\":\"" + entry.getId() + "\",\"source\":\"" + source + "\"}", Instant.now()));
        audit.append(null, "PAYMENT_SETTLED", "AidRequest", aid.getId(),
                "{\"providerReference\":\"" + providerReference + "\",\"source\":\"" + source + "\"}");
    }
}
