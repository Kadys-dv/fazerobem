package br.com.ajudamutua.repository; import br.com.ajudamutua.model.AidApproval; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AidApprovalRepository extends JpaRepository<AidApproval,UUID>{ long countByAidRequestId(UUID id); boolean existsByAidRequestIdAndApproverUserId(UUID aid,UUID user); List<AidApproval> findByAidRequestId(UUID aid); }
