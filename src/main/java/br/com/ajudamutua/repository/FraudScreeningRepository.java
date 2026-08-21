package br.com.ajudamutua.repository;
import br.com.ajudamutua.model.FraudScreening; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface FraudScreeningRepository extends JpaRepository<FraudScreening,UUID>{ Optional<FraudScreening> findByAidRequestId(UUID id); }
