package br.com.ajudamutua.repository;

import br.com.ajudamutua.model.AidRequest;
import br.com.ajudamutua.model.AidStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AidRequestRepository extends JpaRepository<AidRequest, UUID> {
    List<AidRequest> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
    List<AidRequest> findByMemberIdAndStatusOrderByCreatedAtDesc(UUID memberId, AidStatus status);
    long countByMemberIdAndStatusAndCreatedAtAfter(UUID memberId, AidStatus status, Instant after);
    boolean existsByMemberIdAndStatusIn(UUID memberId, Collection<AidStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AidRequest a where a.id = :id")
    Optional<AidRequest> findByIdForUpdate(@Param("id") UUID id);
}
