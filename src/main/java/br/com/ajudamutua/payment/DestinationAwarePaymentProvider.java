package br.com.ajudamutua.payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Capability for providers that require a beneficiary payment destination.
 * The resolved destination is short-lived and must never be logged or persisted.
 */
public interface DestinationAwarePaymentProvider {
    PaymentProvider.Initiation initiateWithDestination(UUID paymentId,
                                                       BigDecimal amount,
                                                       String idempotencyKey,
                                                       ResolvedPaymentDestination destination);
}
