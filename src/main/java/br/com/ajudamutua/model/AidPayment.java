package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="aid_payments")
public class AidPayment {
 @Id private UUID id; @Column(nullable=false,unique=true,length=100) private String idempotencyKey; @Column(nullable=false,unique=true) private UUID aidRequestId; @Column(nullable=false) private UUID paidByUserId; @Column(nullable=false) private Instant createdAt;
 protected AidPayment(){} public AidPayment(UUID id,String key,UUID aidRequestId,UUID paidByUserId,Instant createdAt){this.id=id;this.idempotencyKey=key;this.aidRequestId=aidRequestId;this.paidByUserId=paidByUserId;this.createdAt=createdAt;}
 public UUID getId(){return id;} public String getIdempotencyKey(){return idempotencyKey;} public UUID getAidRequestId(){return aidRequestId;} public UUID getPaidByUserId(){return paidByUserId;} public Instant getCreatedAt(){return createdAt;}
}
