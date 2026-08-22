package br.com.ajudamutua.recovery;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AccountRecoveryRequestRepository extends JpaRepository<AccountRecoveryRequest,UUID>{ List<AccountRecoveryRequest> findByStatusOrderByCreatedAtDesc(RecoveryStatus status); }
