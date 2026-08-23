package br.com.ajudamutua.payment;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProviderGatewaySafetyTest {

    @Test
    void http4xxIsDefinitiveFailureNotUncertain() {
        UUID memberId = UUID.randomUUID();
        PaymentDestinationService destinations = org.mockito.Mockito.mock(PaymentDestinationService.class);
        org.mockito.Mockito.when(destinations.resolveActive(memberId))
                .thenReturn(new ResolvedPaymentDestination(memberId, PaymentDestination.PixKeyType.EMAIL, "test@example.invalid"));

        TestDestinationProvider provider = new TestDestinationProvider(() -> {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        });

        PaymentProviderGateway gateway = new PaymentProviderGateway(provider, destinations, 3, 1000, 0);
        assertThrows(ProviderDefinitiveFailureException.class,
                () -> gateway.initiate(UUID.randomUUID(), memberId, new BigDecimal("10.00"), "idem-1"));
        assertEquals(1, provider.calls.get());
    }

    @Test
    void transportFailureIsUncertainAndNeverRetried() {
        UUID memberId = UUID.randomUUID();
        PaymentDestinationService destinations = org.mockito.Mockito.mock(PaymentDestinationService.class);
        org.mockito.Mockito.when(destinations.resolveActive(memberId))
                .thenReturn(new ResolvedPaymentDestination(memberId, PaymentDestination.PixKeyType.EMAIL, "test@example.invalid"));

        TestDestinationProvider provider = new TestDestinationProvider(() -> {
            throw new RuntimeException("network failure");
        });

        PaymentProviderGateway gateway = new PaymentProviderGateway(provider, destinations, 3, 1000, 0);
        assertThrows(ProviderUncertainResultException.class,
                () -> gateway.initiate(UUID.randomUUID(), memberId, new BigDecimal("10.00"), "idem-2"));
        assertEquals(1, provider.calls.get());
    }

    private static final class TestDestinationProvider implements PaymentProvider, DestinationAwarePaymentProvider {
        private final Runnable behavior;
        private final AtomicInteger calls = new AtomicInteger();

        private TestDestinationProvider(Runnable behavior) {
            this.behavior = behavior;
        }

        @Override
        public Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Initiation initiateWithDestination(UUID paymentId, BigDecimal amount, String idempotencyKey,
                                                  ResolvedPaymentDestination destination) {
            calls.incrementAndGet();
            behavior.run();
            return new Initiation("never", "never");
        }

        @Override
        public StatusResult queryStatus(String providerReference) {
            return new StatusResult(ExternalStatus.UNKNOWN, providerReference, "test");
        }
    }
}
