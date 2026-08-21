package br.com.ajudamutua.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="ledger_entries")
public class LedgerEntry {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private LedgerType type;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
    private UUID memberId;
    private UUID aidRequestId;
    @Column(nullable=false, length=300) private String description;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false, length=64) private String previousHash;
    @Column(nullable=false, length=64) private String entryHash;

    protected LedgerEntry() {}
    public LedgerEntry(UUID id, LedgerType type, BigDecimal amount, UUID memberId, UUID aidRequestId, String description,
                       Instant createdAt, String previousHash, String entryHash) {
        this.id=id; this.type=type; this.amount=amount; this.memberId=memberId; this.aidRequestId=aidRequestId;
        this.description=description; this.createdAt=createdAt; this.previousHash=previousHash; this.entryHash=entryHash;
    }
    public UUID getId(){return id;} public LedgerType getType(){return type;} public BigDecimal getAmount(){return amount;}
    public UUID getMemberId(){return memberId;} public UUID getAidRequestId(){return aidRequestId;}
    public String getDescription(){return description;} public Instant getCreatedAt(){return createdAt;}
    public String getPreviousHash(){return previousHash;} public String getEntryHash(){return entryHash;}
}
