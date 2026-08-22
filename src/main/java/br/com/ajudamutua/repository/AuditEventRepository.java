package br.com.ajudamutua.repository;

import br.com.ajudamutua.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    Optional<AuditEvent> findTopByOrderByCreatedAtDesc();
    List<AuditEvent> findTop100ByOrderByCreatedAtDesc();
}
