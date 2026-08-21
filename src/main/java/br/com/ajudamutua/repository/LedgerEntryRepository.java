package br.com.ajudamutua.repository;
import br.com.ajudamutua.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    Optional<LedgerEntry> findTopByOrderByCreatedAtDesc();
    List<LedgerEntry> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
    @Query("select coalesce(sum(l.amount),0) from LedgerEntry l")
    java.math.BigDecimal balance();
}
