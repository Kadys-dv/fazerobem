package br.com.ajudamutua.recovery;
import jakarta.validation.constraints.NotBlank; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/admin/account-recovery") public class AccountRecoveryController {
 private final AccountRecoveryService service; public AccountRecoveryController(AccountRecoveryService s){service=s;}
 public record CreateRequest(UUID targetUserId,@NotBlank String reason){}
 @PostMapping public AccountRecoveryRequest create(@RequestBody CreateRequest in){return service.create(in.targetUserId(),in.reason());}
 @PostMapping("/{id}/approve") public AccountRecoveryRequest approve(@PathVariable UUID id){return service.approve(id);}
 @PostMapping("/{id}/execute") public AccountRecoveryRequest execute(@PathVariable UUID id){return service.execute(id);}
 @GetMapping("/pending") public List<AccountRecoveryRequest> pending(){return service.pending();}
}
