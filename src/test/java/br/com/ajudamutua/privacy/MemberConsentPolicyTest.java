package br.com.ajudamutua.privacy;

import br.com.ajudamutua.model.AppUser;
import br.com.ajudamutua.model.ConsentType;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.ConsentRecordRepository;
import br.com.ajudamutua.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MemberConsentPolicyTest {

    @Test
    void blocksMemberWhenAnyCurrentConsentIsMissing() {
        var repo = mock(ConsentRecordRepository.class);
        var current = mock(CurrentUserService.class);
        UUID memberId = UUID.randomUUID();
        when(current.require()).thenReturn(new AppUser(UUID.randomUUID(), "member@test.local", "hash", UserRole.MEMBER, memberId, true, Instant.now()));
        when(repo.existsByMemberIdAndConsentTypeAndDocumentVersionAndAcceptedTrue(any(), any(), anyString())).thenReturn(false);

        var policy = new MemberConsentPolicy(repo, current);
        ReflectionTestUtils.setField(policy, "currentVersion", "2026-08-v1");

        assertThrows(IllegalStateException.class, policy::requireForCurrentMember);
    }

    @Test
    void allowsMemberWhenAllCurrentConsentsAreAccepted() {
        var repo = mock(ConsentRecordRepository.class);
        var current = mock(CurrentUserService.class);
        UUID memberId = UUID.randomUUID();
        when(current.require()).thenReturn(new AppUser(UUID.randomUUID(), "member@test.local", "hash", UserRole.MEMBER, memberId, true, Instant.now()));
        when(repo.existsByMemberIdAndConsentTypeAndDocumentVersionAndAcceptedTrue(memberId, ConsentType.TERMS, "2026-08-v1")).thenReturn(true);
        when(repo.existsByMemberIdAndConsentTypeAndDocumentVersionAndAcceptedTrue(memberId, ConsentType.PRIVACY_POLICY, "2026-08-v1")).thenReturn(true);
        when(repo.existsByMemberIdAndConsentTypeAndDocumentVersionAndAcceptedTrue(memberId, ConsentType.COMMUNITY_RULES, "2026-08-v1")).thenReturn(true);

        var policy = new MemberConsentPolicy(repo, current);
        ReflectionTestUtils.setField(policy, "currentVersion", "2026-08-v1");

        assertDoesNotThrow(policy::requireForCurrentMember);
    }

    @Test
    void doesNotApplyMemberOnboardingToPrivilegedRoles() {
        var repo = mock(ConsentRecordRepository.class);
        var current = mock(CurrentUserService.class);
        when(current.require()).thenReturn(new AppUser(UUID.randomUUID(), "admin@test.local", "hash", UserRole.ADMIN, null, true, Instant.now()));

        var policy = new MemberConsentPolicy(repo, current);
        ReflectionTestUtils.setField(policy, "currentVersion", "2026-08-v1");

        assertDoesNotThrow(policy::requireForCurrentMember);
        verifyNoInteractions(repo);
    }
}
