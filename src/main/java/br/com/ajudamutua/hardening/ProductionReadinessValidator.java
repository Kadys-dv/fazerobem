package br.com.ajudamutua.hardening;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("production")
public class ProductionReadinessValidator implements ApplicationRunner {
    private final Environment env;

    public ProductionReadinessValidator(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        validate(env);
    }

    static void validate(Environment env) {
        require(env, "spring.datasource.url");
        require(env, "spring.datasource.username");
        require(env, "spring.datasource.password");
        require(env, "spring.data.redis.host");
        require(env, "spring.data.redis.port");
        require(env, "spring.data.redis.password");
        require(env, "app.crypto.key-id");
        require(env, "app.sandbox.webhook-secret");
        require(env, "app.webauthn.rp-name");
        String rpId = require(env, "app.webauthn.rp-id");
        String origins = require(env, "app.webauthn.allowed-origins");
        String publicBaseUrl = require(env, "app.production.public-base-url");
        String kmsProvider = require(env, "app.kms.provider").toLowerCase(Locale.ROOT);

        if (!env.getProperty("server.servlet.session.cookie.secure", Boolean.class, false)) {
            fail("server.servlet.session.cookie.secure must be true in production");
        }
        if (!env.getProperty("app.production.tls-required", Boolean.class, false)) {
            fail("app.production.tls-required must be true in production");
        }
        if (!env.getProperty("app.security.mfa-required-for-privileged", Boolean.class, false)) {
            fail("Privileged MFA must remain enabled in production");
        }

        URI base = httpsUri(publicBaseUrl, "PUBLIC_BASE_URL");
        if (isLocalHost(base.getHost())) fail("PUBLIC_BASE_URL cannot target localhost in production");
        if (rpId.equalsIgnoreCase("localhost") || isLocalHost(rpId)) fail("WEBAUTHN_RP_ID cannot be localhost in production");
        if (!hostMatchesRp(base.getHost(), rpId)) fail("PUBLIC_BASE_URL host must match WEBAUTHN_RP_ID");

        List<String> allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        if (allowedOrigins.isEmpty()) fail("At least one WebAuthn origin is required");
        for (String origin : allowedOrigins) {
            URI uri = httpsUri(origin, "WEBAUTHN_ALLOWED_ORIGINS");
            if (isLocalHost(uri.getHost())) fail("WebAuthn origins cannot target localhost in production");
            if (!hostMatchesRp(uri.getHost(), rpId)) fail("WebAuthn origin host must match WEBAUTHN_RP_ID: " + origin);
        }

        if (kmsProvider.equals("local")) fail("KMS_PROVIDER=local is forbidden in production");
        if (kmsProvider.equals("aws") && blank(env.getProperty("app.kms.aws-key-id"))) {
            fail("AWS_KMS_KEY_ID is required when KMS_PROVIDER=aws");
        }
        if (!kmsProvider.equals("aws") && !kmsProvider.equals("external")) {
            fail("Unsupported production KMS provider: " + kmsProvider);
        }

        if (kmsProvider.equals("external") && blank(env.getProperty("app.crypto.master-key-base64"))) {
            fail("APP_CRYPTO_MASTER_KEY_BASE64 is required for external KMS bootstrap until an external protector is configured");
        }
    }

    private static String require(Environment env, String key) {
        String value = env.getProperty(key);
        if (blank(value)) fail("Missing required production property: " + key);
        return value.trim();
    }

    private static URI httpsUri(String value, String label) {
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || blank(uri.getHost())) {
                fail(label + " must be an absolute HTTPS URL");
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(label + " must be a valid HTTPS URL", ex);
        }
    }

    private static boolean hostMatchesRp(String host, String rpId) {
        if (host == null || rpId == null) return false;
        String h = host.toLowerCase(Locale.ROOT);
        String rp = rpId.toLowerCase(Locale.ROOT);
        return h.equals(rp) || h.endsWith("." + rp);
    }

    private static boolean isLocalHost(String host) {
        if (host == null) return true;
        String h = host.toLowerCase(Locale.ROOT);
        return h.equals("localhost") || h.equals("127.0.0.1") || h.equals("::1") || h.endsWith(".local");
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static void fail(String message) {
        throw new IllegalStateException("Production readiness validation failed: " + message);
    }
}
