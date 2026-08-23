package br.com.ajudamutua.payment;

import br.com.ajudamutua.model.PaymentAttempt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments/attempts")
public class PaymentProviderReconciliationController {
    private final PaymentProviderReconciliationService reconciliation;

    public PaymentProviderReconciliationController(PaymentProviderReconciliationService reconciliation) {
        this.reconciliation = reconciliation;
    }

    @PostMapping("/{paymentId}/refresh-provider")
    public PaymentAttempt refreshProvider(@PathVariable UUID paymentId) {
        return reconciliation.refresh(paymentId);
    }
}
