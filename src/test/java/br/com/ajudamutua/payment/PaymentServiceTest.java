package br.com.ajudamutua.payment;

import br.com.ajudamutua.dto.ApiDtos;
import br.com.ajudamutua.model.*;
import br.com.ajudamutua.repository.*;
import br.com.ajudamutua.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock AidRequestRepository aids;
    @Mock AidApprovalRepository approvals;
    @Mock PaymentAttemptRepository attempts;
    @Mock WebhookEventRepository webhooks;
    @Mock OutboxEventRepository outbox;
    @Mock CurrentUserService current;
    @Mock AidPolicyService policy;
    @Mock LedgerService ledger;
    @Mock AuditService audit;
    @Mock PaymentProvider provider;
    @Mock WebhookSignatureService signatures;

    private PaymentInitiationService initiation;
    private PaymentWebhookService webhook;

    @BeforeEach
    void setUp() {
        initiation = new PaymentInitiationService(aids, approvals, attempts, outbox, current, policy, ledger, audit, provider);
        webhook = new PaymentWebhookService(aids, attempts, webhooks, outbox, ledger, audit, signatures);
    }

    @Test
    void sameIdempotencyKeyReturnsExistingAttemptWithoutCallingProvider() {
        UUID aidId = UUID.randomUUID();
        PaymentAttempt prior = attempt(aidId, "same-key", PaymentStatus.PROCESSING);
        when(attempts.findByIdempotencyKey("same-key")).thenReturn(Optional.of(prior));

        PaymentAttempt result = initiation.initiate(aidId, "same-key");

        assertSame(prior, result);
        verifyNoInteractions(provider, ledger, policy, current);
    }

    @Test
    void idempotencyKeyCannotBeReusedForAnotherAid() {
        PaymentAttempt prior = attempt(UUID.randomUUID(), "same-key", PaymentStatus.PROCESSING);
        when(attempts.findByIdempotencyKey("same-key")).thenReturn(Optional.of(prior));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> initiation.initiate(UUID.randomUUID(), "same-key"));

        assertTrue(error.getMessage().contains("Idempotency-Key já usada"));
        verifyNoInteractions(provider);
    }

    @Test
    void sameKeyIsRecheckedAfterAidLockForConcurrentRetry() {
        UUID aidId = UUID.randomUUID();
        AidRequest aid = approvedAid(aidId, UUID.randomUUID(), new BigDecimal("10.00"));
        PaymentAttempt prior = attempt(aidId, "race-key", PaymentStatus.PROCESSING);
        AppUser admin = new AppUser(UUID.randomUUID(), "admin@test.local", "x", UserRole.ADMIN, null, true, Instant.now());

        when(attempts.findByIdempotencyKey("race-key")).thenReturn(Optional.empty(), Optional.of(prior));
        when(current.require()).thenReturn(admin);
        when(aids.findByIdForUpdate(aidId)).thenReturn(Optional.of(aid));

        PaymentAttempt result = initiation.initiate(aidId, "race-key");

        assertSame(prior, result);
        verify(attempts, never()).existsByAidRequestIdAndStatusIn(any(), anyCollection());
        verifyNoInteractions(provider, ledger, policy);
    }

    @Test
    void differentConcurrentKeyIsBlockedAfterAidLockWhenActiveAttemptExists() {
        UUID aidId = UUID.randomUUID();
        AidRequest aid = approvedAid(aidId, UUID.randomUUID(), new BigDecimal("10.00"));
        AppUser admin = new AppUser(UUID.randomUUID(), "admin@test.local", "x", UserRole.ADMIN, null, true, Instant.now());

        when(attempts.findByIdempotencyKey("new-key")).thenReturn(Optional.empty());
        when(current.require()).thenReturn(admin);
        when(aids.findByIdForUpdate(aidId)).thenReturn(Optional.of(aid));
        when(attempts.existsByAidRequestIdAndStatusIn(eq(aidId), anyCollection())).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> initiation.initiate(aidId, "new-key"));

        assertEquals("Já existe tentativa ativa de pagamento para este auxílio", error.getMessage());
        verifyNoInteractions(provider);
    }

    @Test
    void insufficientFundBlocksBeforeProviderCall() {
        UUID aidId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        AidRequest aid = approvedAid(aidId, memberId, new BigDecimal("100.00"));
        AppUser admin = new AppUser(UUID.randomUUID(), "admin@test.local", "x", UserRole.ADMIN, null, true, Instant.now());

        when(attempts.findByIdempotencyKey("fund-check")).thenReturn(Optional.empty());
        when(current.require()).thenReturn(admin);
        when(aids.findByIdForUpdate(aidId)).thenReturn(Optional.of(aid));
        when(attempts.existsByAidRequestIdAndStatusIn(eq(aidId), anyCollection())).thenReturn(false);
        when(approvals.countByAidRequestId(aidId)).thenReturn(2L);
        when(approvals.findByAidRequestId(aidId)).thenReturn(List.of());
        when(policy.evaluate(aid)).thenReturn(eligible(aidId));
        when(ledger.balance()).thenReturn(new BigDecimal("99.99"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> initiation.initiate(aidId, "fund-check"));

        assertEquals("Fundo insuficiente", error.getMessage());
        verifyNoInteractions(provider);
        verify(attempts, never()).saveAndFlush(any());
    }

    @Test
    void replayedWebhookIsRejectedBeforePaymentMutation() {
        String body = "{\"providerReference\":\"sandbox-x\",\"status\":\"SETTLED\"}";
        when(signatures.verify(anyString(), eq(body), anyString())).thenReturn(true);
        when(webhooks.existsByEventId("event-1")).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> webhook.handle("event-1", Instant.now().toString(), "sig", body, "sandbox-x", "SETTLED"));

        assertEquals("Webhook replay detectado", error.getMessage());
        verify(attempts, never()).findByProviderReferenceForUpdate(anyString());
        verifyNoInteractions(ledger);
    }

    @Test
    void failedWebhookMarksAttemptFailedWithoutLedgerDebit() {
        PaymentAttempt payment = processingAttempt(UUID.randomUUID(), "failed-ref");
        stubAcceptedWebhook("event-failed", payment, "failed-ref");

        webhook.handle("event-failed", Instant.now().toString(), "sig",
                "{\"providerReference\":\"failed-ref\",\"status\":\"FAILED\"}", "failed-ref", "FAILED");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verifyNoInteractions(ledger);
    }

    @Test
    void unknownProviderStatusRequiresReconciliationWithoutLedgerDebit() {
        PaymentAttempt payment = processingAttempt(UUID.randomUUID(), "unknown-ref");
        stubAcceptedWebhook("event-unknown", payment, "unknown-ref");

        webhook.handle("event-unknown", Instant.now().toString(), "sig",
                "{\"providerReference\":\"unknown-ref\",\"status\":\"PENDING_REVIEW\"}", "unknown-ref", "PENDING_REVIEW");

        assertEquals(PaymentStatus.RECONCILIATION_REQUIRED, payment.getStatus());
        verifyNoInteractions(ledger);
    }

    @Test
    void secondSettlementCannotCreateSecondLedgerDebit() {
        UUID aidId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        PaymentAttempt payment = processingAttempt(aidId, "settled-ref");
        AidRequest aid = approvedAid(aidId, memberId, new BigDecimal("10.00"));
        LedgerEntry entry = new LedgerEntry(UUID.randomUUID(), LedgerType.AID_PAYMENT, new BigDecimal("-10.00"),
                memberId, aidId, "test", Instant.now(), "0".repeat(64), "1".repeat(64));

        when(signatures.verify(anyString(), anyString(), anyString())).thenReturn(true);
        when(webhooks.existsByEventId(anyString())).thenReturn(false);
        when(webhooks.saveAndFlush(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attempts.findByProviderReferenceForUpdate("settled-ref")).thenReturn(Optional.of(payment));
        when(aids.findById(aidId)).thenReturn(Optional.of(aid));
        when(ledger.append(eq(LedgerType.AID_PAYMENT), eq(new BigDecimal("-10.00")), eq(memberId), eq(aidId), anyString()))
                .thenReturn(entry);

        String body = "{\"providerReference\":\"settled-ref\",\"status\":\"SETTLED\"}";
        webhook.handle("event-settle-1", Instant.now().toString(), "sig", body, "settled-ref", "SETTLED");

        assertEquals(PaymentStatus.SETTLED, payment.getStatus());
        assertEquals(AidStatus.PAID, aid.getStatus());
        verify(ledger, times(1)).append(any(), any(), any(), any(), anyString());

        assertThrows(IllegalStateException.class,
                () -> webhook.handle("event-settle-2", Instant.now().toString(), "sig", body, "settled-ref", "SETTLED"));
        verify(ledger, times(1)).append(any(), any(), any(), any(), anyString());
    }

    private void stubAcceptedWebhook(String eventId, PaymentAttempt payment, String providerReference) {
        when(signatures.verify(anyString(), anyString(), anyString())).thenReturn(true);
        when(webhooks.existsByEventId(eventId)).thenReturn(false);
        when(webhooks.saveAndFlush(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attempts.findByProviderReferenceForUpdate(providerReference)).thenReturn(Optional.of(payment));
    }

    private PaymentAttempt attempt(UUID aidId, String key, PaymentStatus status) {
        PaymentAttempt payment = new PaymentAttempt(UUID.randomUUID(), aidId, key, "SANDBOX", PaymentStatus.READY,
                new BigDecimal("10.00"), UUID.randomUUID(), Instant.now());
        if (status == PaymentStatus.PROCESSING || status == PaymentStatus.SETTLED || status == PaymentStatus.RECONCILIATION_REQUIRED) {
            payment.processing("ref-" + UUID.randomUUID());
        }
        if (status == PaymentStatus.SETTLED) payment.settle();
        if (status == PaymentStatus.RECONCILIATION_REQUIRED) payment.requireReconciliation();
        if (status == PaymentStatus.FAILED) payment.fail("test");
        return payment;
    }

    private PaymentAttempt processingAttempt(UUID aidId, String providerReference) {
        PaymentAttempt payment = new PaymentAttempt(UUID.randomUUID(), aidId, "key-" + UUID.randomUUID(), "SANDBOX",
                PaymentStatus.READY, new BigDecimal("10.00"), UUID.randomUUID(), Instant.now());
        payment.processing(providerReference);
        return payment;
    }

    private AidRequest approvedAid(UUID aidId, UUID memberId, BigDecimal amount) {
        AidRequest aid = new AidRequest(aidId, memberId, amount, AidCategory.HEALTH, "test", true, AidStatus.PENDING, Instant.now());
        aid.approve("approved");
        return aid;
    }

    private ApiDtos.EligibilityResult eligible(UUID aidId) {
        return new ApiDtos.EligibilityResult(aidId, true, List.of(), List.of(), new BigDecimal("2500.00"),
                new BigDecimal("1000.00"), 0, 45, true, 1, "CLEARED");
    }
}
