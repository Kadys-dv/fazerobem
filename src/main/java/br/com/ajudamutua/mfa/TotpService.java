package br.com.ajudamutua.mfa;
import org.springframework.stereotype.Service; import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec; import java.nio.ByteBuffer; import java.security.SecureRandom; import java.time.Instant; import java.util.Base64;
@Service public class TotpService {
 private final SecureRandom random=new SecureRandom();
 public String newSecret(){byte[] b=new byte[20];random.nextBytes(b);return Base64.getEncoder().withoutPadding().encodeToString(b);}
 public boolean verify(String secret,String code){if(code==null||!code.matches("\\d{6}"))return false;long step=Instant.now().getEpochSecond()/30;for(long i=-1;i<=1;i++)if(generate(secret,step+i).equals(code))return true;return false;}
 String generate(String secret,long step){try{byte[] key=Base64.getDecoder().decode(secret);Mac mac=Mac.getInstance("HmacSHA1");mac.init(new SecretKeySpec(key,"HmacSHA1"));byte[] h=mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());int o=h[h.length-1]&15;int n=((h[o]&127)<<24)|((h[o+1]&255)<<16)|((h[o+2]&255)<<8)|(h[o+3]&255);return String.format("%06d",n%1_000_000);}catch(Exception e){throw new IllegalStateException(e);}}
}
