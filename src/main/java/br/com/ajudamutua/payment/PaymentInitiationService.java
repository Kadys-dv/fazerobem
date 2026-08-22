package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.*;
import br.com.ajudamutua.repository.AidApprovalRepository;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AidPolicyService;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import br.com.ajudamutua.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentInitiationService {
    private static final List<PaymentStatus> ACTIVE_STATUSES = List.of(
            PaymentStatus.READY,
            PaymentStatus.PROCESSING,
            PaymentStatus.SETTLED,
            PaymentStatus.RECONCILIATION_REQUIRED);

    private final AidRequestRepository aids;
    private final AidApprovalRepository approvals;
    private final PaymentAttemptRepository attempts;
    private final OutboxEventRepository outbox;
    private final CurrentUserService current;
    private final AidPolicyService policy;
    private final LedgerService ledger;
    private final AuditService audit;
    private final PaymentProviderGateway provider;

    @Autowired
    public PaymentInitiationService(AidRequestRepository aids,
                                    AidApprovalRepository approvals,
                                    PaymentAttemptRepository attempts,
                                    OutboxEventRepository outbox,
                                    CurrentUserService current,
                                    AidPolicyService policy,
                                    LedgerService ledger,
                                    AuditService audit,
                                    PaymentProviderGateway provider) {
        this.aids = aids;
        this.approvals = approvals;
        this.attempts = attempts;
        this.outbox = outbox;
        this.current = current;
        this.policy = policy;
        this.ledger = ledger;
        this.audit = audit;
        this.provider = provider;
    }

    PaymentInitiationService(AidRequestRepository aids,
                             AidApprovalRepository approvals,
                             PaymentAttemptRepository attempts,
                             OutboxEventRepository outbox,
                             CurrentUserService current,
                             AidPolicyService policy,
                             LedgerService ledger,
                             AuditService audit,
                             PaymentProvider provider) {
        this(aids, approvals, attempts, outbox, current, policy, ledger, audit,
                new PaymentProviderGateway(provider, 1, 2000, 0));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentAttempt initiate(UUID aidId, String idempotencyKey) {
        requireIdempotencyKey(idempotencyKey);
        PaymentAttempt prior = findPrior(idempotencyKey, aidId);
        if (prior != null) return prior;

        AppUser actor = current.require();
        AidRequest aid = aids.findByIdForUpdate(aidId).orElseThrow();
        prior = findPrior(idempotencyKey, aidId);
        if (prior != null) return prior;

        validateReadyForPayment(aid, actor);
        PaymentAttempt payment = attempts.saveAndFlush(new PaymentAttempt(
                UUID.randomUUID(), aidId, idempotencyKey, "SANDBOX", PaymentStatus.READY,
                aid.getAmount(), actor.getId(), Instant.now()));

        PaymentProvider.Initiation initiation = provider.initiate(
                payment.getId(), payment.getAmount(), payment.getIdempotencyKey());
        payment.processing(initiation.providerReference());

        outbox.save(new OutboxEvent(
                UUID.randomUUID(), "PaymentAttempt", payment.getId(), "PAYMENT_PROCESSING",
                "{\"providerReference\":\"" + initiation.providerReference() +
                        "\",\"providerRequestId\":\"" + initiation.providerRequestId() + "\"}", Instant.now()));
        audit.append(actor.getId(), "PAYMENT_INITIATED", "AidRequest", aidId,
                "{\"paymentAttemptId\":\"" + payment.getId() +
                        "\",\"providerRequestId\":\"" + initiation.providerRequestId() + "\"}");
        return payment;
    }

    private PaymentAttempt findPrior(String idempotencyKey, UUID aidId) {
        var prior = attempts.findByIdempotencyKey(idempotencyKey);
        if (prior.isEmpty()) return null;
        if (!prior.get().getAidRequestId().equals(aidId)) throw new IllegalStateException("Idempotency-Key já usada");
        return prior.get();
    }

    private void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency-Key obrigatório");
    }

    private void validateReadyForPayment(AidRequest aid, AppUser actor) {
        UUID aidId = aid.getId();
        if (attempts.existsByAidRequestIdAndStatusIn(aidId, ACTIVE_STATUSES)) throw new IllegalStateException("Já existe tentativa ativa de pagamento para este auxílio");
        if (aid.getStatus() != AidStatus.APPROVED || approvals.countByAidRequestId(aidId) < 2) throw new IllegalStateException("Pedido não está pronto para pagamento");
        if (approvals.findByAidRequestId(aidId).stream().anyMatch(a -> a.getApproverUserId().equals(actor.getId()))) throw new IllegalStateException("Separação de funções violada");
        var eligibility = policy.evaluate(aid);
        if (!eligibility.eligible()) throw new IllegalStateException("Elegibilidade inválida: " + String.join("; ", eligibility.blockers()));
        if (ledger.balance().compareTo(aid.getAmount()) < 0) throw new IllegalStateException("Fundo insuficiente");
    }
}
