package br.com.ajudamutua.payment;

public class ProviderUncertainResultException extends RuntimeException {
    public ProviderUncertainResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
