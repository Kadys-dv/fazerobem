package br.com.ajudamutua.recovery;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="account_recovery_approvals",uniqueConstraints=@UniqueConstraint(columnNames={"recovery_request_id","approver_user_id"}))
public class AccountRecoveryApproval {
 @Id private UUID id; @Column(nullable=false) private UUID recoveryRequestId; @Column(nullable=false) private UUID approverUserId; @Column(nullable=false) private Instant approvedAt;
 protected AccountRecoveryApproval(){} public AccountRecoveryApproval(UUID id,UUID req,UUID approver,Instant at){this.id=id;recoveryRequestId=req;approverUserId=approver;approvedAt=at;}
 public UUID getApproverUserId(){return approverUserId;}
}
