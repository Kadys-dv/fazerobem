package br.com.ajudamutua.integrity;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/v1/ops/integrity") @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')") public class IntegrityController {
 private final ChainIntegrityService service; public IntegrityController(ChainIntegrityService s){service=s;}
 @GetMapping public Map<String,Object> verify(){var l=service.ledger();var a=service.audit();return Map.of("valid",l.valid()&&a.valid(),"ledger",l,"audit",a);}
}
