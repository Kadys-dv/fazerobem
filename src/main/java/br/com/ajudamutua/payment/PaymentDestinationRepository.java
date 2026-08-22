package br.com.ajudamutua.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentDestinationRepository extends JpaRepository<PaymentDestination, UUID> {
    Optional<PaymentDestination> findByMemberIdAndActiveTrue(UUID memberId);
    Optional<PaymentDestination> findByMemberId(UUID memberId);
}
