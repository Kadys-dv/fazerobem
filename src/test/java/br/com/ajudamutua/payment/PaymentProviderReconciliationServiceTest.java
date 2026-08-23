package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.AppUser;
import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProviderReconciliationServiceTest {
    @Mock PaymentAttemptRepository attempts;
    @Mock PaymentProviderGateway gateway;
    @Mock PaymentSettlementService settlement;
    @Mock CurrentUserService current;
    @Mock AuditService audit;

    @Test
    void settledRemoteStatusDelegatesToSettlementOnce() {
        PaymentAttempt payment = processing("ref-settled");
        when(attempts.findByIdForUpdate(payment.getId())).thenReturn(Optional.of(payment));
        when(gateway.queryStatus("ref-settled"))
                .thenReturn(new PaymentProvider.StatusResult(PaymentProvider.ExternalStatus.SETTLED, "req", "ok"));
        when(current.require()).thenReturn(admin());

        service().refresh(payment.getId());

        verify(settlement).settle(payment, "ref-settled", "PROVIDER_RECONCILIATION");
        verify(audit).append(any(), eq("PAYMENT_PROVIDER_STATUS_REFRESHED"), eq("PaymentAttempt"), eq(payment.getId()), contains("SETTLED"));
    }

    @Test
    void failedRemoteStatusMarksPaymentFailed() {
        PaymentAttempt payment = processing("ref-failed");
        when(attempts.findByIdForUpdate(payment.getId())).thenReturn(Optional.of(payment));
        when(gateway.queryStatus("ref-failed"))
                .thenReturn(new PaymentProvider.StatusResult(PaymentProvider.ExternalStatus.FAILED, "req", "failed"));
        when(current.require()).thenReturn(admin());

        service().refresh(payment.getId());

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verifyNoInteractions(settlement);
    }

    @Test
    void providerQueryFailureKeepsPaymentForReconciliation() {
        PaymentAttempt payment = processing("ref-timeout");
        when(attempts.findByIdForUpdate(payment.getId())).thenReturn(Optional.of(payment));
        when(gateway.queryStatus("ref-timeout")).thenThrow(new IllegalStateException("timeout"));
        when(current.require()).thenReturn(admin());

        service().refresh(payment.getId());

        assertEquals(PaymentStatus.RECONCILIATION_REQUIRED, payment.getStatus());
        verifyNoInteractions(settlement);
        verify(audit).append(any(), eq("PAYMENT_PROVIDER_STATUS_REFRESHED"), eq("PaymentAttempt"), eq(payment.getId()), contains("provider-query-failed"));
    }

    @Test
    void terminalPaymentDoesNotCallProviderAgain() {
        PaymentAttempt payment = processing("ref-terminal");
        payment.fail("done");
        when(attempts.findByIdForUpdate(payment.getId())).thenReturn(Optional.of(payment));

        PaymentAttempt result = service().refresh(payment.getId());

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verifyNoInteractions(gateway, settlement, current, audit);
    }

    private PaymentProviderReconciliationService service() {
        return new PaymentProviderReconciliationService(attempts, gateway, settlement, current, audit);
    }

    private PaymentAttempt processing(String ref) {
        PaymentAttempt p = new PaymentAttempt(UUID.randomUUID(), UUID.randomUUID(), "key-" + UUID.randomUUID(),
                "ASAAS_SANDBOX", PaymentStatus.READY, new BigDecimal("10.00"), UUID.randomUUID(), Instant.now());
        p.processing(ref);
        return p;
    }

    private AppUser admin() {
        return new AppUser(UUID.randomUUID(), "admin@test.local", "hash", UserRole.ADMIN, null, true, Instant.now());
    }
}
