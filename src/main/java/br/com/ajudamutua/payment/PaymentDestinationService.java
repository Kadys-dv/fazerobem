package br.com.ajudamutua.payment;

import br.com.ajudamutua.crypto.SecretProtector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class PaymentDestinationService {
    private final PaymentDestinationRepository destinations;
    private final SecretProtector protector;

    public PaymentDestinationService(PaymentDestinationRepository destinations,
                                     SecretProtector protector) {
        this.destinations = destinations;
        this.protector = protector;
    }

    @Transactional
    public PaymentDestination savePixDestination(UUID memberId,
                                                 PaymentDestination.PixKeyType keyType,
                                                 String rawPixKey) {
        if (memberId == null) throw new IllegalArgumentException("memberId obrigatório");
        if (keyType == null) throw new IllegalArgumentException("tipo de chave Pix obrigatório");

        String normalized = normalize(keyType, rawPixKey);
        validate(keyType, normalized);

        String ciphertext = protector.encrypt(normalized);
        String fingerprint = sha256(normalized);
        String masked = mask(keyType, normalized);
        String keyId = protector.keyId();

        var existing = destinations.findByMemberId(memberId);
        PaymentDestination entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.replace(keyType, ciphertext, fingerprint, masked, keyId);
        } else {
            entity = new PaymentDestination(
                    UUID.randomUUID(), memberId,
                    PaymentDestination.DestinationType.PIX, keyType,
                    ciphertext, fingerprint, masked, keyId,
                    true, Instant.now());
        }
        return destinations.save(entity);
    }

    @Transactional(readOnly = true)
    public ResolvedPaymentDestination resolveActive(UUID memberId) {
        PaymentDestination destination = destinations.findByMemberIdAndActiveTrue(memberId)
                .orElseThrow(() -> new IllegalStateException("Beneficiário sem destino de pagamento ativo"));
        String plaintext = protector.decrypt(destination.getDestinationCiphertext());
        return new ResolvedPaymentDestination(memberId, destination.getPixKeyType(), plaintext);
    }

    @Transactional
    public void deactivate(UUID memberId) {
        destinations.findByMemberIdAndActiveTrue(memberId).ifPresent(destination -> {
            destination.deactivate();
            destinations.save(destination);
        });
    }

    private static String normalize(PaymentDestination.PixKeyType type, String value) {
        if (value == null) throw new IllegalArgumentException("chave Pix obrigatória");
        String normalized = value.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException("chave Pix obrigatória");
        return switch (type) {
            case EMAIL -> normalized.toLowerCase(Locale.ROOT);
            case CPF, CNPJ, PHONE -> normalized.replaceAll("[^0-9+]", "");
            case EVP -> normalized.toLowerCase(Locale.ROOT);
        };
    }

    private static void validate(PaymentDestination.PixKeyType type, String value) {
        boolean valid = switch (type) {
            case CPF -> value.matches("\\d{11}");
            case CNPJ -> value.matches("\\d{14}");
            case EMAIL -> value.length() <= 77 && value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
            case PHONE -> value.matches("^\\+?\\d{10,15}$");
            case EVP -> value.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
        };
        if (!valid) throw new IllegalArgumentException("formato de chave Pix inválido");
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static String mask(PaymentDestination.PixKeyType type, String value) {
        return switch (type) {
            case CPF -> "***.***.***-" + value.substring(value.length() - 2);
            case CNPJ -> "**.***.***/****-" + value.substring(value.length() - 2);
            case PHONE -> "***" + value.substring(Math.max(0, value.length() - 4));
            case EMAIL -> {
                int at = value.indexOf('@');
                yield (at <= 1 ? "*" : value.substring(0, 1) + "***") + value.substring(at);
            }
            case EVP -> "***-***-***-" + value.substring(value.length() - 4);
        };
    }
}
