package br.com.ajudamutua.payment;

import br.com.ajudamutua.crypto.SecretProtector;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentDestinationServiceTest {

    @Test
    void pixKeyIsEncryptedBeforePersistenceAndOnlyMaskedValueIsExposed() {
        PaymentDestinationRepository repository = mock(PaymentDestinationRepository.class);
        SecretProtector protector = mock(SecretProtector.class);
        UUID memberId = UUID.randomUUID();

        when(repository.findByMemberId(memberId)).thenReturn(Optional.empty());
        when(protector.encrypt("user@example.com")).thenReturn("ciphertext-value");
        when(protector.keyId()).thenReturn("test-key");
        when(repository.save(any(PaymentDestination.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentDestinationService service = new PaymentDestinationService(repository, protector);
        PaymentDestination saved = service.savePixDestination(
                memberId, PaymentDestination.PixKeyType.EMAIL, " User@Example.com ");

        assertEquals("ciphertext-value", saved.getDestinationCiphertext());
        assertEquals("u***@example.com", saved.getDestinationMasked());
        assertEquals(64, saved.getDestinationFingerprint().length());
        assertNotEquals("user@example.com", saved.getDestinationFingerprint());
        verify(protector).encrypt("user@example.com");
        verify(repository).save(any(PaymentDestination.class));
    }

    @Test
    void resolvedDestinationRedactsToStringAndCanBeCleared() {
        PaymentDestinationRepository repository = mock(PaymentDestinationRepository.class);
        SecretProtector protector = mock(SecretProtector.class);
        UUID memberId = UUID.randomUUID();
        PaymentDestination destination = new PaymentDestination(
                UUID.randomUUID(), memberId,
                PaymentDestination.DestinationType.PIX,
                PaymentDestination.PixKeyType.PHONE,
                "cipher", "fingerprint", "***1234", "test-key", true,
                java.time.Instant.now());

        when(repository.findByMemberIdAndActiveTrue(memberId)).thenReturn(Optional.of(destination));
        when(protector.decrypt("cipher")).thenReturn("+5513999991234");

        PaymentDestinationService service = new PaymentDestinationService(repository, protector);
        ResolvedPaymentDestination resolved = service.resolveActive(memberId);

        assertEquals("+5513999991234", resolved.revealForProviderCall());
        assertFalse(resolved.toString().contains("+5513999991234"));
        assertTrue(resolved.toString().contains("[REDACTED]"));

        resolved.close();
        assertThrows(IllegalStateException.class, resolved::revealForProviderCall);
    }

    @Test
    void invalidPixKeyNeverReachesProtectorOrRepository() {
        PaymentDestinationRepository repository = mock(PaymentDestinationRepository.class);
        SecretProtector protector = mock(SecretProtector.class);
        PaymentDestinationService service = new PaymentDestinationService(repository, protector);

        assertThrows(IllegalArgumentException.class, () -> service.savePixDestination(
                UUID.randomUUID(), PaymentDestination.PixKeyType.CPF, "123"));

        verifyNoInteractions(protector);
        verifyNoInteractions(repository);
    }
}
