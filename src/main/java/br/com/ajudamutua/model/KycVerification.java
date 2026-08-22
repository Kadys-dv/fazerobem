package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="kyc_verifications")
public class KycVerification {
 @Id private UUID id; @Column(nullable=false) private UUID memberId; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private KycStatus status; @Column(nullable=false,length=40) private String provider; @Column(length=120) private String externalReference; private UUID reviewedByUserId; @Column(length=500) private String rejectionReason; @Column(nullable=false) private Instant createdAt; @Column(nullable=false) private Instant updatedAt;
 protected KycVerification(){} public KycVerification(UUID id,UUID memberId,KycStatus status,String provider,String ext,UUID reviewer,String reason,Instant now){this.id=id;this.memberId=memberId;this.status=status;this.provider=provider;this.externalReference=ext;this.reviewedByUserId=reviewer;this.rejectionReason=reason;this.createdAt=now;this.updatedAt=now;}
 public UUID getId(){return id;} public UUID getMemberId(){return memberId;} public KycStatus getStatus(){return status;} public String getProvider(){return provider;} public String getExternalReference(){return externalReference;} public String getRejectionReason(){return rejectionReason;} public Instant getCreatedAt(){return createdAt;}
}
