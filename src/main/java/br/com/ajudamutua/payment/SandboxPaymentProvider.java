package br.com.ajudamutua.payment;
import org.springframework.stereotype.Component; import java.math.BigDecimal; import java.util.UUID;
@Component public class SandboxPaymentProvider implements PaymentProvider { public Initiation initiate(UUID paymentId, BigDecimal amount){ return new Initiation("sandbox-"+paymentId); } }
