package br.com.ajudamutua.hardening;
import br.com.ajudamutua.repository.OutboxEventRepository; import org.slf4j.*; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
@Component public class OutboxPublisherJob { private static final Logger log=LoggerFactory.getLogger(OutboxPublisherJob.class); private final OutboxEventRepository repo; public OutboxPublisherJob(OutboxEventRepository r){repo=r;}
 @Scheduled(fixedDelayString="${app.outbox.fixed-delay-ms:5000}") @Transactional public void publish(){for(var e:repo.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()){log.info("outbox eventId={} type={}",e.getId(),e.getEventType());e.markPublished();}}
}
