package br.com.ajudamutua.mfa;

import br.com.ajudamutua.crypto.SecretProtector;
import br.com.ajudamutua.model.AppUser;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.AppUserRepository;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mfa")
public class MfaController {
    private final CurrentUserService current;
    private final AppUserRepository users;
    private final TotpService totp;
    private final SecretProtector crypto;
    private final AuditService audit;

    public MfaController(CurrentUserService current, AppUserRepository users, TotpService totp,
                         SecretProtector crypto, AuditService audit) {
        this.current = current;
        this.users = users;
        this.totp = totp;
        this.crypto = crypto;
        this.audit = audit;
    }

    public record CodeRequest(String code) {}

    @GetMapping("/status")
    public Map<String, Boolean> status() {
        AppUser user = current.require();
        if (user.getRole() == UserRole.MEMBER) {
            throw new IllegalStateException("MFA disponível apenas para perfis privilegiados nesta fase");
        }
        return Map.of("enabled", user.isMfaEnabled());
    }

    @PostMapping("/enroll")
    public Map<String, String> enroll() {
        AppUser user = current.require();
        if (user.getRole() == UserRole.MEMBER) {
            throw new IllegalStateException("MFA obrigatório apenas para perfis privilegiados nesta fase");
        }
        if (user.isMfaEnabled()) {
            throw new IllegalStateException("MFA já habilitado; use a verificação da sessão");
        }
        String secret = totp.newSecret();
        user.configureMfa(crypto.encrypt(secret), false);
        users.save(user);
        audit.append(user.getId(), "MFA_ENROLL_STARTED", "AppUser", user.getId(), "{}");
        return Map.of("secret", secret, "algorithm", "TOTP-SHA1", "digits", "6", "period", "30");
    }

    @PostMapping("/confirm")
    public Map<String, Boolean> confirm(@RequestBody CodeRequest in, HttpSession session) {
        AppUser user = current.require();
        String encrypted = user.getMfaSecretEnc();
        if (encrypted == null) {
            throw new IllegalStateException("MFA não iniciado");
        }
        if (!totp.verify(crypto.decrypt(encrypted), in.code())) {
            throw new IllegalArgumentException("Código MFA inválido");
        }
        user.configureMfa(encrypted, true);
        users.save(user);
        session.setAttribute("MFA_VERIFIED", Boolean.TRUE);
        audit.append(user.getId(), "MFA_ENABLED", "AppUser", user.getId(), "{}");
        return Map.of("enabled", true);
    }

    @PostMapping("/verify")
    public Map<String, Boolean> verify(@RequestBody CodeRequest in, HttpSession session) {
        AppUser user = current.require();
        if (!user.isMfaEnabled()) {
            throw new IllegalStateException("MFA não habilitado");
        }
        if (!totp.verify(crypto.decrypt(user.getMfaSecretEnc()), in.code())) {
            throw new IllegalArgumentException("Código MFA inválido");
        }
        session.setAttribute("MFA_VERIFIED", Boolean.TRUE);
        audit.append(user.getId(), "MFA_SESSION_VERIFIED", "AppUser", user.getId(), "{}");
        return Map.of("verified", true);
    }
}
