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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceReconciliationTest {
    @Mock PaymentInitiationService initiation;
    @Mock PaymentWebhookService webhooks;
    @Mock PaymentAttemptRepository attempts;
    @Mock CurrentUserService current;
    @Mock AuditService audit;

    @Test
    void acknowledgementIsAuditedAndNeverSettlesPayment() {
        UUID paymentId=UUID.randomUUID();
        UUID aidId=UUID.randomUUID();
        UUID adminId=UUID.randomUUID();
        PaymentAttempt payment=new PaymentAttempt(paymentId,aidId,"key","SANDBOX",PaymentStatus.READY,new BigDecimal("10.00"),adminId,Instant.now());
        payment.requireReconciliation();
        AppUser admin=new AppUser(adminId,"admin@test.local","hash", UserRole.ADMIN,null,true,Instant.now());
        when(attempts.findById(paymentId)).thenReturn(Optional.of(payment));
        when(current.require()).thenReturn(admin);
        PaymentService service=new PaymentService(initiation,webhooks,attempts,current,audit);

        PaymentAttempt result=service.acknowledgeReconciliation(paymentId,"Contato com provedor iniciado");

        assertEquals(PaymentStatus.RECONCILIATION_REQUIRED,result.getStatus());
        verify(audit).append(org.mockito.ArgumentMatchers.eq(adminId),org.mockito.ArgumentMatchers.eq("PAYMENT_RECONCILIATION_ACKNOWLEDGED"),org.mockito.ArgumentMatchers.eq("PaymentAttempt"),org.mockito.ArgumentMatchers.eq(paymentId),org.mockito.ArgumentMatchers.contains("Contato com provedor iniciado"));
    }

    @Test
    void acknowledgementRejectsNonReconciliationPayment() {
        UUID paymentId=UUID.randomUUID();
        PaymentAttempt payment=new PaymentAttempt(paymentId,UUID.randomUUID(),"key2","SANDBOX",PaymentStatus.READY,new BigDecimal("10.00"),UUID.randomUUID(),Instant.now());
        when(attempts.findById(paymentId)).thenReturn(Optional.of(payment));
        PaymentService service=new PaymentService(initiation,webhooks,attempts,current,audit);

        assertThrows(IllegalStateException.class,()->service.acknowledgeReconciliation(paymentId,"nota"));
    }
}
