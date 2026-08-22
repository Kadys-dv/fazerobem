package br.com.ajudamutua.recovery;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="account_recovery_requests")
public class AccountRecoveryRequest {
 @Id private UUID id; @Column(nullable=false) private UUID targetUserId; @Column(nullable=false) private UUID requestedByUserId;
 @Column(nullable=false,length=500) private String reason; @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private RecoveryStatus status;
 @Column(nullable=false) private Instant createdAt; private Instant approvedAt; private Instant executedAt; @Column(nullable=false) private Instant expiresAt;
 protected AccountRecoveryRequest(){}
 public AccountRecoveryRequest(UUID id,UUID target,UUID requester,String reason,Instant now,Instant expires){this.id=id;targetUserId=target;requestedByUserId=requester;this.reason=reason;status=RecoveryStatus.PENDING;createdAt=now;expiresAt=expires;}
 public UUID getId(){return id;} public UUID getTargetUserId(){return targetUserId;} public UUID getRequestedByUserId(){return requestedByUserId;} public String getReason(){return reason;} public RecoveryStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public Instant getExpiresAt(){return expiresAt;}
 public void markApproved(){if(status!=RecoveryStatus.PENDING)throw new IllegalStateException("Recuperação não está pendente");status=RecoveryStatus.APPROVED;approvedAt=Instant.now();}
 public void markExecuted(){if(status!=RecoveryStatus.APPROVED)throw new IllegalStateException("Recuperação não aprovada");status=RecoveryStatus.EXECUTED;executedAt=Instant.now();}
 public void expire(){if(status==RecoveryStatus.PENDING||status==RecoveryStatus.APPROVED)status=RecoveryStatus.EXPIRED;}
}
