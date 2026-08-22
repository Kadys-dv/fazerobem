package br.com.ajudamutua.model;
import jakarta.persistence.*; import com.fasterxml.jackson.annotation.JsonIgnore; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="member_private_data")
public class MemberPrivateData {
 @Id private UUID memberId; @Column(length=64) private String cpfHash; @Column(length=4) private String cpfLast4; @Column(length=255) private String addressRedacted; @Column(nullable=false) private Instant updatedAt; @Column(columnDefinition="TEXT") private String cpfCiphertext; @Column(columnDefinition="TEXT") private String addressCiphertext; @Column(length=80) private String encryptionKeyId;
 protected MemberPrivateData(){} public MemberPrivateData(UUID memberId,String cpfHash,String cpfLast4,String addressRedacted,Instant updatedAt){this.memberId=memberId;this.cpfHash=cpfHash;this.cpfLast4=cpfLast4;this.addressRedacted=addressRedacted;this.updatedAt=updatedAt;}
 public UUID getMemberId(){return memberId;} public String getCpfLast4(){return cpfLast4;} public String getAddressRedacted(){return addressRedacted;} public Instant getUpdatedAt(){return updatedAt;} @JsonIgnore public String getCpfCiphertext(){return cpfCiphertext;} @JsonIgnore public String getAddressCiphertext(){return addressCiphertext;} public String getEncryptionKeyId(){return encryptionKeyId;} public void protect(String cpfCiphertext,String addressCiphertext,String keyId){this.cpfCiphertext=cpfCiphertext;this.addressCiphertext=addressCiphertext;this.encryptionKeyId=keyId;}
}
