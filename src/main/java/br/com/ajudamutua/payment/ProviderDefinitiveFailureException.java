package br.com.ajudamutua.payment;

public class ProviderDefinitiveFailureException extends RuntimeException {
    public ProviderDefinitiveFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
