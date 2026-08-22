package br.com.ajudamutua.payment;
import org.junit.jupiter.api.Test; import java.time.Instant; import static org.junit.jupiter.api.Assertions.*;
class WebhookSignatureServiceTest { @Test void validSignatureIsAccepted(){var s=new WebhookSignatureService("1234567890abcdef1234567890abcdef");String ts=Instant.now().toString();String body="{\"providerReference\":\"sandbox-x\",\"status\":\"SETTLED\"}";assertTrue(s.verify(ts,body,s.sign(ts+"."+body)));assertFalse(s.verify(ts,body,"00"));} }
