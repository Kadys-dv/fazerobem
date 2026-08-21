package br.com.ajudamutua.model;
import jakarta.persistence.*; import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="app_users")
public class AppUser {
 @Id private UUID id;
 @Column(nullable=false,unique=true,length=180) private String email;
 @Column(nullable=false,length=100) private String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private UserRole role;
 private UUID memberId;
 @Column(nullable=false) private boolean enabled;
 @Column(nullable=false) private Instant createdAt;
 private String mfaSecretEnc; @Column(nullable=false) private boolean mfaEnabled; @Column(nullable=false) private int failedLoginAttempts; private Instant lockedUntil;
 protected AppUser(){}
 public AppUser(UUID id,String email,String passwordHash,UserRole role,UUID memberId,boolean enabled,Instant createdAt){this.id=id;this.email=email;this.passwordHash=passwordHash;this.role=role;this.memberId=memberId;this.enabled=enabled;this.createdAt=createdAt;}
 public UUID getId(){return id;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public UserRole getRole(){return role;} public UUID getMemberId(){return memberId;} public boolean isEnabled(){return enabled;} public Instant getCreatedAt(){return createdAt;} @JsonIgnore public String getMfaSecretEnc(){return mfaSecretEnc;} public boolean isMfaEnabled(){return mfaEnabled;} public int getFailedLoginAttempts(){return failedLoginAttempts;} public Instant getLockedUntil(){return lockedUntil;} public void configureMfa(String secret,boolean enabled){this.mfaSecretEnc=secret;this.mfaEnabled=enabled;} public void loginFailed(){failedLoginAttempts++; if(failedLoginAttempts>=5)lockedUntil=Instant.now().plusSeconds(900);} public void loginSucceeded(){failedLoginAttempts=0;lockedUntil=null;} public void lockForSeconds(long seconds){lockedUntil=Instant.now().plusSeconds(seconds);} public void resetMfa(){this.mfaSecretEnc=null;this.mfaEnabled=false;} public void unlock(){this.failedLoginAttempts=0;this.lockedUntil=null;}
}
