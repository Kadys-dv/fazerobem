package br.com.ajudamutua.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "members")
public class Member {
    @Id
    private UUID id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;
    @Column(nullable = false)
    private Instant joinedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus kycStatus = KycStatus.UNVERIFIED;

    protected Member() {}
    public Member(UUID id, String name, String email, MemberStatus status, Instant joinedAt) {
        this.id=id; this.name=name; this.email=email; this.status=status; this.joinedAt=joinedAt;
        this.kycStatus=KycStatus.UNVERIFIED;
    }
    public UUID getId(){ return id; }
    public String getName(){ return name; }
    public String getEmail(){ return email; }
    public MemberStatus getStatus(){ return status; }
    public Instant getJoinedAt(){ return joinedAt; }
    public KycStatus getKycStatus(){ return kycStatus; }
    public void setKycStatus(KycStatus kycStatus){ this.kycStatus = java.util.Objects.requireNonNull(kycStatus); }
}
