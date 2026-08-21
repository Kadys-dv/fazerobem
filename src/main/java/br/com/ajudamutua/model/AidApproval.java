package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="aid_approvals")
public class AidApproval {
 @Id private UUID id; @Column(nullable=false) private UUID aidRequestId; @Column(nullable=false) private UUID approverUserId;
 @Column(nullable=false,length=500) private String note; @Column(nullable=false) private Instant createdAt;
 protected AidApproval(){} public AidApproval(UUID id,UUID aidRequestId,UUID approverUserId,String note,Instant createdAt){this.id=id;this.aidRequestId=aidRequestId;this.approverUserId=approverUserId;this.note=note;this.createdAt=createdAt;}
 public UUID getId(){return id;} public UUID getAidRequestId(){return aidRequestId;} public UUID getApproverUserId(){return approverUserId;} public String getNote(){return note;} public Instant getCreatedAt(){return createdAt;}
}
