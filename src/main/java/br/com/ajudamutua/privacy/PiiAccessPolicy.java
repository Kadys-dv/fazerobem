package br.com.ajudamutua.privacy;
import br.com.ajudamutua.model.*; import br.com.ajudamutua.service.CurrentUserService; import org.springframework.stereotype.Component; import java.util.*;
@Component public class PiiAccessPolicy {
 private static final Set<String> PURPOSES=Set.of("KYC_REVIEW","FRAUD_REVIEW","DATA_SUBJECT_REQUEST","AUDIT"); private final CurrentUserService current;
 public PiiAccessPolicy(CurrentUserService c){current=c;}
 public void require(UUID memberId,String purpose){AppUser u=current.require(); if(u.getRole()==UserRole.MEMBER){if(!Objects.equals(u.getMemberId(),memberId))throw new IllegalStateException("Acesso PII negado");return;} if(!Set.of(UserRole.ANALYST,UserRole.ADMIN,UserRole.AUDITOR).contains(u.getRole()))throw new IllegalStateException("Papel sem acesso PII");if(purpose==null||!PURPOSES.contains(purpose.trim().toUpperCase()))throw new IllegalArgumentException("Finalidade de acesso PII obrigatória");}
}
