package br.com.ajudamutua.payment;
import java.math.BigDecimal; import java.util.UUID;
public interface PaymentProvider { Initiation initiate(UUID paymentId, BigDecimal amount); record Initiation(String providerReference){} }
