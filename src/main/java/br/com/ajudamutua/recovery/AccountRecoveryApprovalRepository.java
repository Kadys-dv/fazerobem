package br.com.ajudamutua.recovery;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AccountRecoveryApprovalRepository extends JpaRepository<AccountRecoveryApproval,UUID>{ long countByRecoveryRequestId(UUID requestId); boolean existsByRecoveryRequestIdAndApproverUserId(UUID requestId,UUID approverId); List<AccountRecoveryApproval> findByRecoveryRequestId(UUID requestId); }
