package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="audit_events")
public class AuditEvent {
 @Id private UUID id; private UUID actorUserId; @Column(nullable=false,length=80) private String action; @Column(nullable=false,length=80) private String entityType; private UUID entityId;
 @Column(nullable=false,columnDefinition="text") private String metadata; @Column(nullable=false) private Instant createdAt; @Column(nullable=false,length=64) private String previousHash; @Column(nullable=false,length=64,unique=true) private String eventHash;
 protected AuditEvent(){} public AuditEvent(UUID id,UUID actorUserId,String action,String entityType,UUID entityId,String metadata,Instant createdAt,String previousHash,String eventHash){this.id=id;this.actorUserId=actorUserId;this.action=action;this.entityType=entityType;this.entityId=entityId;this.metadata=metadata;this.createdAt=createdAt;this.previousHash=previousHash;this.eventHash=eventHash;}
 public UUID getId(){return id;} public UUID getActorUserId(){return actorUserId;} public String getAction(){return action;} public String getEntityType(){return entityType;} public UUID getEntityId(){return entityId;} public String getMetadata(){return metadata;} public Instant getCreatedAt(){return createdAt;} public String getPreviousHash(){return previousHash;} public String getEventHash(){return eventHash;}
}
