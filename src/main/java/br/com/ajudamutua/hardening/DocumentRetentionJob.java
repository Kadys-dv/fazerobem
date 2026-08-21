package br.com.ajudamutua.hardening;
import br.com.ajudamutua.repository.AidDocumentRepository; import br.com.ajudamutua.service.*; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional; import java.time.Instant;
@Component public class DocumentRetentionJob { private final AidDocumentRepository docs; private final DocumentStorageService storage; private final AuditService audit; public DocumentRetentionJob(AidDocumentRepository d,DocumentStorageService s,AuditService a){docs=d;storage=s;audit=a;}
 @Scheduled(cron="0 15 3 * * *") @Transactional public void purge(){for(var d:docs.findByExpiresAtBeforeAndDeletedAtIsNull(Instant.now())){storage.delete(d.getStorageKey());d.markDeleted();audit.append(null,"DOCUMENT_RETENTION_PURGED","AidDocument",d.getId(),"{\"sha256\":\""+d.getSha256()+"\"}");}}
}
