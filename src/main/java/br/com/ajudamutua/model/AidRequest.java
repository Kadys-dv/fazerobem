package br.com.ajudamutua.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aid_requests")
public class AidRequest {
    @Id private UUID id;
    @Column(nullable=false) private UUID memberId;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private AidCategory category;
    @Column(nullable=false, length=1000) private String reason;
    @Column(nullable=false) private boolean emergency;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private AidStatus status;
    @Column(nullable=false) private Instant createdAt;
    private Instant decidedAt;
    @Column(length=500) private String decisionNote;
    @Version private long version;

    protected AidRequest() {}
    public AidRequest(UUID id, UUID memberId, BigDecimal amount, AidCategory category, String reason, boolean emergency, AidStatus status, Instant createdAt) {
        this.id=id; this.memberId=memberId; this.amount=amount; this.category=category; this.reason=reason; this.emergency=emergency; this.status=status; this.createdAt=createdAt;
    }
    public void approve(String note){ this.status=AidStatus.APPROVED; this.decidedAt=Instant.now(); this.decisionNote=note; }
    public void reject(String note){ this.status=AidStatus.REJECTED; this.decidedAt=Instant.now(); this.decisionNote=note; }
    public void markPaid(){ this.status=AidStatus.PAID; }
    public UUID getId(){return id;} public UUID getMemberId(){return memberId;} public BigDecimal getAmount(){return amount;} public AidCategory getCategory(){return category;}
    public String getReason(){return reason;} public boolean isEmergency(){return emergency;} public AidStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
    public Instant getDecidedAt(){return decidedAt;} public String getDecisionNote(){return decisionNote;} public long getVersion(){return version;}
}
