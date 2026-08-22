package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentOperationGateTest {

    @Test
    void allowsInitiationWhenGateIsEnabled() {
        PaymentOperationGate gate = new PaymentOperationGate(true);
        assertDoesNotThrow(gate::requireInitiationAllowed);
    }

    @Test
    void blocksNewInitiationWhenGateIsFrozen() {
        PaymentOperationGate gate = new PaymentOperationGate(false);
        IllegalStateException error = assertThrows(IllegalStateException.class, gate::requireInitiationAllowed);
        assertEquals("Iniciação de pagamentos está congelada operacionalmente", error.getMessage());
    }
}
