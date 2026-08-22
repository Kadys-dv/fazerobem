package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="dsar_requests") public class DsarRequest {
 @Id private UUID id; private UUID memberId; @Enumerated(EnumType.STRING) private DsarRequestType requestType; @Enumerated(EnumType.STRING) private DsarStatus status; private Instant requestedAt; private Instant completedAt; private UUID processedByUserId; @Column(length=1000) private String notes; @Column(length=64) private String exportSha256;
 protected DsarRequest(){} public DsarRequest(UUID id,UUID memberId,DsarRequestType t){this.id=id;this.memberId=memberId;this.requestType=t;this.status=DsarStatus.OPEN;this.requestedAt=Instant.now();}
 public UUID getId(){return id;} public UUID getMemberId(){return memberId;} public DsarRequestType getRequestType(){return requestType;} public DsarStatus getStatus(){return status;} public Instant getRequestedAt(){return requestedAt;} public Instant getCompletedAt(){return completedAt;} public String getNotes(){return notes;} public String getExportSha256(){return exportSha256;}
 public void complete(UUID actor,String notes,String hash){status=DsarStatus.COMPLETED;processedByUserId=actor;completedAt=Instant.now();this.notes=notes;exportSha256=hash;} public void reject(UUID actor,String notes){status=DsarStatus.REJECTED;processedByUserId=actor;completedAt=Instant.now();this.notes=notes;}
}
