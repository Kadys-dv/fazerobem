package br.com.ajudamutua.repository;
import br.com.ajudamutua.model.AidDocument; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AidDocumentRepository extends JpaRepository<AidDocument,UUID>{ long countByAidRequestId(UUID id); List<AidDocument> findByAidRequestId(UUID id); List<AidDocument> findByExpiresAtBeforeAndDeletedAtIsNull(java.time.Instant now); }
