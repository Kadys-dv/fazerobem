package br.com.ajudamutua.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_destinations",
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_destination_member", columnNames = "member_id"))
public class PaymentDestination {
    @Id
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_type", nullable = false, length = 20)
    private DestinationType destinationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pix_key_type", nullable = false, length = 20)
    private PixKeyType pixKeyType;

    @JsonIgnore
    @Column(name = "destination_ciphertext", nullable = false, columnDefinition = "TEXT")
    private String destinationCiphertext;

    @Column(name = "destination_fingerprint", nullable = false, length = 64)
    private String destinationFingerprint;

    @Column(name = "destination_masked", nullable = false, length = 120)
    private String destinationMasked;

    @Column(name = "encryption_key_id", nullable = false, length = 120)
    private String encryptionKeyId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PaymentDestination() {}

    public PaymentDestination(UUID id,
                              UUID memberId,
                              DestinationType destinationType,
                              PixKeyType pixKeyType,
                              String destinationCiphertext,
                              String destinationFingerprint,
                              String destinationMasked,
                              String encryptionKeyId,
                              boolean active,
                              Instant updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.destinationType = destinationType;
        this.pixKeyType = pixKeyType;
        this.destinationCiphertext = destinationCiphertext;
        this.destinationFingerprint = destinationFingerprint;
        this.destinationMasked = destinationMasked;
        this.encryptionKeyId = encryptionKeyId;
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getMemberId() { return memberId; }
    public DestinationType getDestinationType() { return destinationType; }
    public PixKeyType getPixKeyType() { return pixKeyType; }
    @JsonIgnore public String getDestinationCiphertext() { return destinationCiphertext; }
    public String getDestinationFingerprint() { return destinationFingerprint; }
    public String getDestinationMasked() { return destinationMasked; }
    public String getEncryptionKeyId() { return encryptionKeyId; }
    public boolean isActive() { return active; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void replace(PixKeyType keyType,
                        String ciphertext,
                        String fingerprint,
                        String masked,
                        String keyId) {
        this.destinationType = DestinationType.PIX;
        this.pixKeyType = keyType;
        this.destinationCiphertext = ciphertext;
        this.destinationFingerprint = fingerprint;
        this.destinationMasked = masked;
        this.encryptionKeyId = keyId;
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public enum DestinationType { PIX }
    public enum PixKeyType { CPF, CNPJ, EMAIL, PHONE, EVP }
}
