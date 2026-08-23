package br.com.ajudamutua.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentOperationGate {
    private final boolean initiationEnabled;

    public PaymentOperationGate(@Value("${app.payments.initiation-enabled:false}") boolean initiationEnabled) {
        this.initiationEnabled = initiationEnabled;
    }

    public void requireInitiationAllowed() {
        if (!initiationEnabled) {
            throw new IllegalStateException("Iniciação de pagamentos está congelada operacionalmente");
        }
    }

    public boolean isInitiationEnabled() {
        return initiationEnabled;
    }
}
