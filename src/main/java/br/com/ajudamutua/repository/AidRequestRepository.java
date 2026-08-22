package br.com.ajudamutua.repository;
import br.com.ajudamutua.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.Instant; import java.util.*;
public interface AidRequestRepository extends JpaRepository<AidRequest,UUID>{
    List<AidRequest> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
    List<AidRequest> findByMemberIdAndStatusOrderByCreatedAtDesc(UUID memberId, AidStatus status);
    long countByMemberIdAndStatusAndCreatedAtAfter(UUID memberId, AidStatus status, Instant after);
    boolean existsByMemberIdAndStatusIn(UUID memberId, Collection<AidStatus> statuses);
}
