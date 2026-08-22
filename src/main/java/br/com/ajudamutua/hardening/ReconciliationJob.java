package br.com.ajudamutua.hardening;
import br.com.ajudamutua.model.*; import br.com.ajudamutua.repository.*; import br.com.ajudamutua.service.AuditService; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional; import java.time.*;
@Component public class ReconciliationJob { private final PaymentAttemptRepository payments; private final AuditService audit; public ReconciliationJob(PaymentAttemptRepository p,AuditService a){payments=p;audit=a;}
 @Scheduled(fixedDelayString="${app.reconciliation.fixed-delay-ms:60000}") @Transactional public void reconcile(){Instant cutoff=Instant.now().minus(Duration.ofMinutes(10));for(var p:payments.findByStatusAndUpdatedAtBefore(PaymentStatus.PROCESSING,cutoff)){p.requireReconciliation();audit.append(null,"PAYMENT_RECONCILIATION_REQUIRED","PaymentAttempt",p.getId(),"{}");}}
}
