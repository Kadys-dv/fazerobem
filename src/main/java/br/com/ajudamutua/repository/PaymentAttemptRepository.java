package br.com.ajudamutua.repository;

import br.com.ajudamutua.model.PaymentAttempt;
import br.com.ajudamutua.model.PaymentStatus;
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

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {
    Optional<PaymentAttempt> findByIdempotencyKey(String key);
    Optional<PaymentAttempt> findByProviderReference(String ref);
    List<PaymentAttempt> findByAidRequestIdOrderByUpdatedAtDesc(UUID aidId);
    boolean existsByAidRequestIdAndStatusIn(UUID aidId, Collection<PaymentStatus> statuses);
    List<PaymentAttempt> findByStatusAndUpdatedAtBefore(PaymentStatus status, Instant cutoff);
    List<PaymentAttempt> findByStatusInAndUpdatedAtBefore(Collection<PaymentStatus> statuses, Instant cutoff);
    long countByStatus(PaymentStatus status);
    long countByStatusAndUpdatedAtBefore(PaymentStatus status, Instant cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentAttempt p where p.providerReference = :ref")
    Optional<PaymentAttempt> findByProviderReferenceForUpdate(@Param("ref") String ref);
}
