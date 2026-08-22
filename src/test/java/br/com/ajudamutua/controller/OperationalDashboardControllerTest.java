package br.com.ajudamutua.controller;

import br.com.ajudamutua.model.AuditEvent;
import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.AuditEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class OperationalDashboardControllerTest {

    @Test
    void auditorCanReadRecentAuditTrail() {
        var aidRequests = Mockito.mock(AidRequestRepository.class);
        var payments = Mockito.mock(PaymentAttemptRepository.class);
        var audits = Mockito.mock(AuditEventRepository.class);
        var current = Mockito.mock(CurrentUserService.class);
        var user = Mockito.mock(br.com.ajudamutua.model.AppUser.class);
        when(user.getRole()).thenReturn(UserRole.AUDITOR);
        when(current.require()).thenReturn(user);
        var event = new AuditEvent(UUID.randomUUID(), UUID.randomUUID(), "TEST", "AidRequest", UUID.randomUUID(), "{}", Instant.now(), "0".repeat(64), "1".repeat(64));
        when(audits.findTop100ByOrderByCreatedAtDesc()).thenReturn(List.of(event));
        var controller = new OperationalDashboardController(aidRequests, payments, audits, current);
        assertEquals(1, controller.auditEvents().size());
    }

    @Test
    void nonAuditorCannotReadGlobalAuditTrail() {
        var aidRequests = Mockito.mock(AidRequestRepository.class);
        var payments = Mockito.mock(PaymentAttemptRepository.class);
        var audits = Mockito.mock(AuditEventRepository.class);
        var current = Mockito.mock(CurrentUserService.class);
        var user = Mockito.mock(br.com.ajudamutua.model.AppUser.class);
        when(user.getRole()).thenReturn(UserRole.ADMIN);
        when(current.require()).thenReturn(user);
        var controller = new OperationalDashboardController(aidRequests, payments, audits, current);
        assertThrows(IllegalStateException.class, controller::auditEvents);
    }
}
