package br.com.ajudamutua.controller;

import br.com.ajudamutua.model.ConsentType;
import br.com.ajudamutua.privacy.MemberConsentPolicy;
import br.com.ajudamutua.privacy.PrivacyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/member/onboarding")
@PreAuthorize("hasRole('MEMBER')")
public class MemberOnboardingController {
    private final MemberConsentPolicy policy;
    private final PrivacyService privacy;

    public MemberOnboardingController(MemberConsentPolicy policy, PrivacyService privacy) {
        this.policy = policy;
        this.privacy = privacy;
    }

    public record OnboardingStatus(
            String version,
            Set<ConsentType> required,
            List<ConsentType> missing,
            boolean complete) {}

    public record CompleteOnboardingRequest(
            @NotNull String version,
            @NotEmpty Set<ConsentType> accepted) {}

    @GetMapping
    public OnboardingStatus status() {
        return currentStatus();
    }

    @PostMapping
    @Transactional
    public OnboardingStatus complete(
            @Valid @RequestBody CompleteOnboardingRequest request,
            HttpServletRequest servletRequest) {
        if (!policy.currentVersion().equals(request.version())) {
            throw new IllegalArgumentException("Versão dos documentos não corresponde à versão vigente");
        }
        if (!request.accepted().containsAll(policy.requiredTypes())) {
            throw new IllegalArgumentException("Todos os documentos obrigatórios devem ser aceitos");
        }

        for (ConsentType type : policy.missingForCurrentMember()) {
            privacy.consent(type, policy.currentVersion(), true, servletRequest.getRemoteAddr());
        }
        return currentStatus();
    }

    private OnboardingStatus currentStatus() {
        var missing = policy.missingForCurrentMember();
        return new OnboardingStatus(
                policy.currentVersion(),
                policy.requiredTypes(),
                missing,
                missing.isEmpty());
    }
}
