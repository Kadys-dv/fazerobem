package br.com.ajudamutua.controller;

import br.com.ajudamutua.model.PaymentStatus;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.AuditEventRepository;
import br.com.ajudamutua.repository.PaymentAttemptRepository;
import br.com.ajudamutua.service.CurrentUserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations")
public class OperationalDashboardController {
    private final AidRequestRepository aidRequests;
    private final PaymentAttemptRepository payments;
    private final AuditEventRepository auditEvents;
    private final CurrentUserService currentUser;

    public OperationalDashboardController(AidRequestRepository aidRequests,
                                          PaymentAttemptRepository payments,
                                          AuditEventRepository auditEvents,
                                          CurrentUserService currentUser) {
        this.aidRequests = aidRequests;
        this.payments = payments;
        this.auditEvents = auditEvents;
        this.currentUser = currentUser;
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        currentUser.require();
        long pending = aidRequests.findAll().stream().filter(x -> "PENDING".equals(x.getStatus().name())).count();
        long reconciliation = payments.countByStatus(PaymentStatus.RECONCILIATION_REQUIRED);
        long stuck = payments.countByStatusAndUpdatedAtBefore(PaymentStatus.PROCESSING, Instant.now().minus(10, ChronoUnit.MINUTES));
        return Map.of("pending", pending, "reconciliation", reconciliation, "stuckPayments", stuck);
    }

    @GetMapping("/audit-events")
    public List<AuditRow> auditEvents() {
        var user = currentUser.require();
        if (user.getRole() != UserRole.AUDITOR) {
            throw new IllegalStateException("Acesso restrito ao AUDITOR");
        }
        return auditEvents.findTop100ByOrderByCreatedAtDesc().stream()
                .map(x -> new AuditRow(x.getId(), x.getActorUserId(), x.getAction(), x.getEntityType(), x.getEntityId(), x.getMetadata(), x.getCreatedAt(), x.getEventHash()))
                .toList();
    }

    public record AuditRow(UUID id, UUID actorUserId, String action, String entityType, UUID entityId,
                           String metadata, Instant createdAt, String eventHash) {}
}
