package br.com.ajudamutua.repository; import br.com.ajudamutua.model.AuditEvent; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AuditEventRepository extends JpaRepository<AuditEvent,UUID>{ Optional<AuditEvent> findTopByOrderByCreatedAtDesc(); }
