package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name="signed_transparency_reports") public class SignedTransparencyReport {
 @Id private UUID id; private LocalDate periodStart; private LocalDate periodEnd; @Column(columnDefinition="TEXT") private String payloadJson; @Column(length=64) private String payloadSha256; @Column(columnDefinition="TEXT") private String signatureBase64; @Column(length=120) private String signingKeyId; private Instant createdAt;
 protected SignedTransparencyReport(){} public SignedTransparencyReport(UUID id,LocalDate s,LocalDate e,String payload,String hash,String sig,String keyId){this.id=id;periodStart=s;periodEnd=e;payloadJson=payload;payloadSha256=hash;signatureBase64=sig;signingKeyId=keyId;createdAt=Instant.now();}
 public UUID getId(){return id;} public LocalDate getPeriodStart(){return periodStart;} public LocalDate getPeriodEnd(){return periodEnd;} public String getPayloadJson(){return payloadJson;} public String getPayloadSha256(){return payloadSha256;} public String getSignatureBase64(){return signatureBase64;} public String getSigningKeyId(){return signingKeyId;} public Instant getCreatedAt(){return createdAt;}
}
