package br.com.ajudamutua.mfa; import org.junit.jupiter.api.Test; import java.time.Instant; import static org.junit.jupiter.api.Assertions.*;
class TotpServiceTest { @Test void currentCodeValidates(){var t=new TotpService();String s=t.newSecret();String code=t.generate(s,Instant.now().getEpochSecond()/30);assertTrue(t.verify(s,code));assertFalse(t.verify(s,"00000"));} }
