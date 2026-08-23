package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProviderGatewaySafetyTest {

    @Test
    void http4xxIsDefinitiveFailureNotUncertain() {
        UUID memberId = UUID.randomUUID();
        PaymentDestinationService destinations = org.mockito.Mockito.mock(PaymentDestinationService.class);
        org.mockito.Mockito.when(destinations.resolveActive(memberId))
                .thenReturn(new ResolvedPaymentDestination(memberId, PaymentDestination.PixKeyType.EMAIL, "test@example.invalid"));

        DestinationAwarePaymentProvider provider = new DestinationAwarePaymentProvider() {
            @Override
            public PaymentProvider.Initiation initiateWithDestination(UUID paymentId, BigDecimal amount, String idempotencyKey,
                                                                      ResolvedPaymentDestination destination) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            }
        };

        PaymentProviderGateway gateway = new PaymentProviderGateway(provider, destinations, 3, 1000, 0);
        assertThrows(ProviderDefinitiveFailureException.class,
                () -> gateway.initiate(UUID.randomUUID(), memberId, new BigDecimal("10.00"), "idem-1"));
    }

    @Test
    void transportFailureIsUncertainAndNeverRetried() {
        UUID memberId = UUID.randomUUID();
        PaymentDestinationService destinations = org.mockito.Mockito.mock(PaymentDestinationService.class);
        org.mockito.Mockito.when(destinations.resolveActive(memberId))
                .thenReturn(new ResolvedPaymentDestination(memberId, PaymentDestination.PixKeyType.EMAIL, "test@example.invalid"));

        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        DestinationAwarePaymentProvider provider = new DestinationAwarePaymentProvider() {
            @Override
            public PaymentProvider.Initiation initiateWithDestination(UUID paymentId, BigDecimal amount, String idempotencyKey,
                                                                      ResolvedPaymentDestination destination) {
                calls.incrementAndGet();
                throw new RuntimeException("network failure");
            }
        };

        PaymentProviderGateway gateway = new PaymentProviderGateway(provider, destinations, 3, 1000, 0);
        assertThrows(ProviderUncertainResultException.class,
                () -> gateway.initiate(UUID.randomUUID(), memberId, new BigDecimal("10.00"), "idem-2"));
        org.junit.jupiter.api.Assertions.assertEquals(1, calls.get());
    }
}
