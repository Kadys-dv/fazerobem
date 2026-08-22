package br.com.ajudamutua.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class WebhookSignatureService {
    private static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(5);
    private final String secret;

    public WebhookSignatureService(@Value("${app.sandbox.webhook-secret:}") String secret) {
        this.secret = secret;
    }

    public boolean verify(String timestamp, String body, String signature) {
        try {
            if (secret == null || secret.length() < 16 || signature == null || signature.isBlank()) {
                return false;
            }
            Instant eventTime = Instant.parse(timestamp);
            Duration skew = Duration.between(eventTime, Instant.now()).abs();
            if (skew.compareTo(MAX_CLOCK_SKEW) > 0) {
                return false;
            }
            String expected = sign(timestamp + "." + body);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.US_ASCII),
                    signature.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception ignored) {
            return false;
        }
    }

    public String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String sha256(String body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
