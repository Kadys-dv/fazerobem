package br.com.ajudamutua.model;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="consent_records", uniqueConstraints=@UniqueConstraint(columnNames={"memberId","consentType","documentVersion"}))
public class ConsentRecord {
 @Id private UUID id; @Column(nullable=false) private UUID memberId; @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private ConsentType consentType; @Column(nullable=false,length=40) private String documentVersion; @Column(nullable=false) private boolean accepted; @Column(length=80) private String ipPrefix; @Column(nullable=false) private Instant acceptedAt;
 protected ConsentRecord(){} public ConsentRecord(UUID id,UUID memberId,ConsentType type,String version,boolean accepted,String ip,Instant at){this.id=id;this.memberId=memberId;this.consentType=type;this.documentVersion=version;this.accepted=accepted;this.ipPrefix=ip;this.acceptedAt=at;}
 public UUID getId(){return id;} public ConsentType getConsentType(){return consentType;} public String getDocumentVersion(){return documentVersion;} public boolean isAccepted(){return accepted;} public String getIpPrefix(){return ipPrefix;} public Instant getAcceptedAt(){return acceptedAt;}
}
