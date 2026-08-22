package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="aid_analyses")
public class AidAnalysis {
 @Id private UUID id; @Column(nullable=false) private UUID aidRequestId; @Column(nullable=false) private UUID analystUserId; @Column(nullable=false,length=1000) private String opinion; @Column(nullable=false) private Instant createdAt;
 protected AidAnalysis(){} public AidAnalysis(UUID id,UUID aidRequestId,UUID analystUserId,String opinion,Instant createdAt){this.id=id;this.aidRequestId=aidRequestId;this.analystUserId=analystUserId;this.opinion=opinion;this.createdAt=createdAt;}
 public UUID getId(){return id;} public UUID getAidRequestId(){return aidRequestId;} public UUID getAnalystUserId(){return analystUserId;} public String getOpinion(){return opinion;} public Instant getCreatedAt(){return createdAt;}
}
