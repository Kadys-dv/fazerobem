package br.com.ajudamutua.privacy;

import br.com.ajudamutua.model.AppUser;
import br.com.ajudamutua.model.ConsentType;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.ConsentRecordRepository;
import br.com.ajudamutua.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class MemberConsentPolicy {
    private static final EnumSet<ConsentType> REQUIRED = EnumSet.of(
            ConsentType.TERMS,
            ConsentType.PRIVACY_POLICY,
            ConsentType.COMMUNITY_RULES
    );

    private final ConsentRecordRepository consents;
    private final CurrentUserService currentUser;

    @Value("${app.legal.current-version:2026-08-v1}")
    private String currentVersion;

    public MemberConsentPolicy(ConsentRecordRepository consents, CurrentUserService currentUser) {
        this.consents = consents;
        this.currentUser = currentUser;
    }

    public void requireForCurrentMember() {
        AppUser user = currentUser.require();
        if (user.getRole() != UserRole.MEMBER) {
            return;
        }
        var missing = missingFor(user);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Primeiro acesso pendente. Aceite os documentos vigentes antes de continuar: "
                            + String.join(", ", missing.stream().map(Enum::name).toList()));
        }
    }

    public List<ConsentType> missingForCurrentMember() {
        AppUser user = currentUser.require();
        if (user.getRole() != UserRole.MEMBER) {
            return List.of();
        }
        return missingFor(user);
    }

    public Set<ConsentType> requiredTypes() {
        return EnumSet.copyOf(REQUIRED);
    }

    public String currentVersion() {
        return currentVersion;
    }

    private List<ConsentType> missingFor(AppUser user) {
        if (user.getMemberId() == null) {
            throw new IllegalStateException("Conta de membro sem vínculo cadastral");
        }
        return REQUIRED.stream()
                .filter(type -> !consents.existsByMemberIdAndConsentTypeAndDocumentVersionAndAcceptedTrue(
                        user.getMemberId(), type, currentVersion))
                .toList();
    }
}
