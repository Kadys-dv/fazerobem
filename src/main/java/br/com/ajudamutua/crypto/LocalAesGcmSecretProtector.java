package br.com.ajudamutua.crypto;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import javax.crypto.Cipher; import javax.crypto.spec.GCMParameterSpec; import javax.crypto.spec.SecretKeySpec; import java.nio.charset.StandardCharsets; import java.security.SecureRandom; import java.util.Base64;
@Component @ConditionalOnProperty(name="app.kms.provider",havingValue="local",matchIfMissing=true)
public class LocalAesGcmSecretProtector implements SecretProtector {
 private final byte[] key; private final String keyId; private final SecureRandom random=new SecureRandom();
 public LocalAesGcmSecretProtector(@Value("${app.crypto.master-key-base64:}") String raw,@Value("${app.crypto.key-id:local-v1}") String keyId){
  this.keyId=keyId; if(raw==null||raw.isBlank()){this.key=null;return;} byte[] decoded=Base64.getDecoder().decode(raw); if(decoded.length!=32)throw new IllegalStateException("APP_CRYPTO_MASTER_KEY_BASE64 deve ter 32 bytes"); this.key=decoded;
 }
 public String encrypt(String plaintext){if(plaintext==null)return null; requireKey(); try{byte[] iv=new byte[12];random.nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));byte[] out=c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));return "v1:"+keyId+":"+Base64.getEncoder().encodeToString(iv)+":"+Base64.getEncoder().encodeToString(out);}catch(Exception e){throw new IllegalStateException("Falha ao criptografar PII",e);}}
 public String decrypt(String ciphertext){if(ciphertext==null)return null; requireKey(); try{String[] p=ciphertext.split(":",4);if(p.length!=4||!"v1".equals(p[0]))throw new IllegalArgumentException("Ciphertext inválido");byte[] iv=Base64.getDecoder().decode(p[2]), data=Base64.getDecoder().decode(p[3]);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));return new String(c.doFinal(data),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException("Falha ao descriptografar PII",e);}}
 public String keyId(){return keyId;} private void requireKey(){if(key==null)throw new IllegalStateException("Chave de criptografia externa não configurada");}
}
