package br.com.ajudamutua.repository; import br.com.ajudamutua.model.AidPayment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AidPaymentRepository extends JpaRepository<AidPayment,UUID>{ Optional<AidPayment> findByIdempotencyKey(String key); boolean existsByAidRequestId(UUID id); }
