package br.com.ajudamutua.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {
    @Id private UUID id;
    @Column(nullable = false) private UUID aidRequestId;
    @Column(nullable = false, unique = true, length = 100) private String idempotencyKey;
    @Column(nullable = false, length = 40) private String provider;
    @Column(unique = true, length = 120) private String providerReference;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private PaymentStatus status;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private UUID initiatedByUserId;
    @Column(length = 500) private String failureReason;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;

    protected PaymentAttempt() {}

    public PaymentAttempt(UUID id, UUID aidId, String key, String provider, PaymentStatus status,
                          BigDecimal amount, UUID actor, Instant now) {
        this.id=id; this.aidRequestId=aidId; this.idempotencyKey=key; this.provider=provider;
        this.status=status; this.amount=amount; this.initiatedByUserId=actor; this.createdAt=now; this.updatedAt=now;
    }

    public void processing(String ref){require(PaymentStatus.READY);status=PaymentStatus.PROCESSING;providerReference=ref;updatedAt=Instant.now();}
    public void settle(){if(status!=PaymentStatus.PROCESSING&&status!=PaymentStatus.RECONCILIATION_REQUIRED)throw new IllegalStateException("Transição de pagamento inválida");status=PaymentStatus.SETTLED;updatedAt=Instant.now();}
    public void fail(String reason){if(status==PaymentStatus.SETTLED)throw new IllegalStateException("Pagamento liquidado não pode falhar");status=PaymentStatus.FAILED;failureReason=reason;updatedAt=Instant.now();}
    public void requireReconciliation(){if(status==PaymentStatus.SETTLED)throw new IllegalStateException("Pagamento liquidado");status=PaymentStatus.RECONCILIATION_REQUIRED;updatedAt=Instant.now();}
    private void require(PaymentStatus expected){if(status!=expected)throw new IllegalStateException("Transição de pagamento inválida");}

    public UUID getId(){return id;} public UUID getAidRequestId(){return aidRequestId;} public UUID getInitiatedByUserId(){return initiatedByUserId;}
    public String getIdempotencyKey(){return idempotencyKey;} public String getProviderReference(){return providerReference;}
    public PaymentStatus getStatus(){return status;} public BigDecimal getAmount(){return amount;} public Instant getUpdatedAt(){return updatedAt;}
    public long getVersion(){return version;}
}
