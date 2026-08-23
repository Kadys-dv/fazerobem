package br.com.ajudamutua.payment;

import br.com.ajudamutua.dto.ApiDtos;
import br.com.ajudamutua.model.*;
import br.com.ajudamutua.repository.AidApprovalRepository;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.OutboxEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AidPolicyService;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import br.com.ajudamutua.service.LedgerService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentInitiationUncertainResultTest {

    @Test
    void uncertainProviderResultIsPersistedAndSameIdempotencyKeyCannotPostAgain() {
        AidRequestRepository aids = mock(AidRequestRepository.class);
        AidApprovalRepository approvals = mock(AidApprovalRepository.class);
        PaymentAttemptRepository attempts = mock(PaymentAttemptRepository.class);
        OutboxEventRepository outbox = mock(OutboxEventRepository.class);
        CurrentUserService current = mock(CurrentUserService.class);
        AidPolicyService policy = mock(AidPolicyService.class);
        LedgerService ledger = mock(LedgerService.class);
        AuditService audit = mock(AuditService.class);
        PaymentProviderGateway gateway = mock(PaymentProviderGateway.class);

        UUID aidId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("10.00");
        AidRequest aid = new AidRequest(aidId, memberId, amount, AidCategory.HEALTH, "sandbox homologation", true,
                AidStatus.PENDING, Instant.now());
        aid.approve("approved");
        AppUser admin = new AppUser(adminId, "admin@test.local", "x", UserRole.ADMIN, null, true, Instant.now());
        AtomicReference<PaymentAttempt> persisted = new AtomicReference<>();

        when(attempts.findByIdempotencyKey("asaas-uncertain-1"))
                .thenAnswer(invocation -> Optional.ofNullable(persisted.get()));
        when(current.require()).thenReturn(admin);
        when(aids.findByIdForUpdate(aidId)).thenReturn(Optional.of(aid));
        when(attempts.existsByAidRequestIdAndStatusIn(eq(aidId), anyCollection())).thenReturn(false);
        when(approvals.countByAidRequestId(aidId)).thenReturn(2L);
        when(approvals.findByAidRequestId(aidId)).thenReturn(List.of());
        when(policy.evaluate(aid)).thenReturn(new ApiDtos.EligibilityResult(
                aidId, true, List.of(), List.of(), new BigDecimal("2500.00"), new BigDecimal("1000.00"),
                0, 45, true, 1, "CLEARED"));
        when(ledger.balance()).thenReturn(new BigDecimal("100.00"));
        when(gateway.providerCode()).thenReturn("ASAAS_SANDBOX");
        when(attempts.saveAndFlush(any(PaymentAttempt.class))).thenAnswer(invocation -> {
            PaymentAttempt payment = invocation.getArgument(0);
            persisted.set(payment);
            return payment;
        });
        when(gateway.initiate(any(UUID.class), eq(memberId), eq(amount), eq("asaas-uncertain-1")))
                .thenThrow(new ProviderUncertainResultException("timeout", new RuntimeException("network")));

        PaymentInitiationService service = new PaymentInitiationService(
                aids, approvals, attempts, outbox, current, policy, ledger, audit, gateway, new PaymentOperationGate(true));

        PaymentAttempt first = service.initiate(aidId, "asaas-uncertain-1");
        assertEquals(PaymentStatus.RECONCILIATION_REQUIRED, first.getStatus());

        PaymentAttempt retry = service.initiate(aidId, "asaas-uncertain-1");
        assertSame(first, retry);
        verify(gateway, times(1)).initiate(any(UUID.class), eq(memberId), eq(amount), eq("asaas-uncertain-1"));
        verify(outbox).save(argThat(event -> event != null));
        verify(audit).append(eq(adminId), eq("PAYMENT_INITIATION_UNCERTAIN"), eq("AidRequest"), eq(aidId),
                contains(first.getId().toString()));
    }
}
