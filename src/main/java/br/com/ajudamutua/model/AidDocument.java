package br.com.ajudamutua.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="aid_documents")
public class AidDocument {
    @Id private UUID id;
    @Column(nullable=false) private UUID aidRequestId;
    @Column(nullable=false,length=60) private String documentType;
    @Column(nullable=false,length=255) private String fileName;
    @Column(nullable=false,length=255) private String storageKey;
    @Column(nullable=false,length=100) private String contentType;
    @Column(nullable=false) private long sizeBytes;
    @Column(nullable=false,length=64) private String sha256;
    @Column(nullable=false) private UUID submittedByUserId;
    @Column(nullable=false) private Instant createdAt; private Instant expiresAt; private Instant deletedAt;
    protected AidDocument(){}
    public AidDocument(UUID id, UUID aidRequestId, String documentType, String fileName, String storageKey, String contentType, long sizeBytes, String sha256, UUID submittedByUserId, Instant createdAt){this.id=id;this.aidRequestId=aidRequestId;this.documentType=documentType;this.fileName=fileName;this.storageKey=storageKey;this.contentType=contentType;this.sizeBytes=sizeBytes;this.sha256=sha256;this.submittedByUserId=submittedByUserId;this.createdAt=createdAt;}
    public UUID getId(){return id;} public UUID getAidRequestId(){return aidRequestId;} public String getDocumentType(){return documentType;} public String getFileName(){return fileName;} public String getStorageKey(){return storageKey;} public String getContentType(){return contentType;} public long getSizeBytes(){return sizeBytes;} public String getSha256(){return sha256;} public UUID getSubmittedByUserId(){return submittedByUserId;} public Instant getCreatedAt(){return createdAt;} public Instant getExpiresAt(){return expiresAt;} public Instant getDeletedAt(){return deletedAt;} public void setRetentionUntil(Instant expiresAt){this.expiresAt=expiresAt;} public void markDeleted(){this.deletedAt=Instant.now();}
}
