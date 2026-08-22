package br.com.ajudamutua.repository;
import br.com.ajudamutua.model.AidAnalysis; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AidAnalysisRepository extends JpaRepository<AidAnalysis,UUID>{ List<AidAnalysis> findByAidRequestId(UUID id); }
