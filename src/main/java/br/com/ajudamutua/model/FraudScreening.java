package br.com.ajudamutua.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="fraud_screenings")
public class FraudScreening {
    @Id private UUID id;
    @Column(nullable=false,unique=true) private UUID aidRequestId;
    @Column(nullable=false) private UUID analystUserId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private FraudScreeningStatus status;
    @Column(nullable=false) private int riskScore;
    @Column(nullable=false,length=1000) private String flags;
    @Column(nullable=false,length=1000) private String note;
    @Column(nullable=false) private Instant createdAt;
    protected FraudScreening(){}
    public FraudScreening(UUID id,UUID aidRequestId,UUID analystUserId,FraudScreeningStatus status,int riskScore,String flags,String note,Instant createdAt){this.id=id;this.aidRequestId=aidRequestId;this.analystUserId=analystUserId;this.status=status;this.riskScore=riskScore;this.flags=flags;this.note=note;this.createdAt=createdAt;}
    public UUID getId(){return id;} public UUID getAidRequestId(){return aidRequestId;} public UUID getAnalystUserId(){return analystUserId;} public FraudScreeningStatus getStatus(){return status;} public int getRiskScore(){return riskScore;} public String getFlags(){return flags;} public String getNote(){return note;} public Instant getCreatedAt(){return createdAt;}
}
