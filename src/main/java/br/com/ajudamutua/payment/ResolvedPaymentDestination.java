package br.com.ajudamutua.payment;

import java.util.UUID;

/**
 * Short-lived decrypted destination value. This object must never be serialized,
 * logged or persisted. Its toString() is deliberately redacted.
 */
public final class ResolvedPaymentDestination implements AutoCloseable {
    private final UUID memberId;
    private final PaymentDestination.PixKeyType pixKeyType;
    private char[] pixKey;

    public ResolvedPaymentDestination(UUID memberId,
                                      PaymentDestination.PixKeyType pixKeyType,
                                      String pixKey) {
        this.memberId = memberId;
        this.pixKeyType = pixKeyType;
        this.pixKey = pixKey.toCharArray();
    }

    public UUID memberId() { return memberId; }
    public PaymentDestination.PixKeyType pixKeyType() { return pixKeyType; }

    public String revealForProviderCall() {
        if (pixKey == null) throw new IllegalStateException("Payment destination already cleared");
        return new String(pixKey);
    }

    @Override
    public String toString() {
        return "ResolvedPaymentDestination{memberId=" + memberId + ", pixKeyType=" + pixKeyType + ", pixKey=[REDACTED]}";
    }

    @Override
    public void close() {
        if (pixKey != null) {
            java.util.Arrays.fill(pixKey, '\0');
            pixKey = null;
        }
    }
}
