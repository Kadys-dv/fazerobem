package br.com.ajudamutua.controller;
import br.com.ajudamutua.model.*; import br.com.ajudamutua.payment.PaymentService; import br.com.ajudamutua.privacy.PrivacyService; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.constraints.*; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1") public class Phase4Controller {
 private final PrivacyService privacy; private final PaymentService payments; public Phase4Controller(PrivacyService p,PaymentService ps){privacy=p;payments=ps;}
 public record PrivateDataRequest(@NotBlank String cpf,String address){} public record ConsentRequest(@NotNull ConsentType type,@NotBlank String version,boolean accepted){} public record KycReview(@NotNull KycStatus status,String reason){} public record WebhookBody(@NotBlank String providerReference,@NotBlank String status){}
 @PostMapping("/privacy/private-data") public MemberPrivateData privateData(@RequestBody PrivateDataRequest in){return privacy.savePrivateData(in.cpf(),in.address());}
 @PostMapping("/privacy/consents") public ConsentRecord consent(@RequestBody ConsentRequest in,HttpServletRequest req){return privacy.consent(in.type(),in.version(),in.accepted(),req.getRemoteAddr());}
 @GetMapping("/privacy/consents/{memberId}") public List<ConsentRecord> consents(@PathVariable UUID memberId){return privacy.consents(memberId);}
 @GetMapping("/privacy/private-data/{memberId}") public PrivacyService.PiiView privateDataView(@PathVariable UUID memberId,@RequestHeader(value="X-Access-Purpose",required=false) String purpose){return privacy.privateData(memberId,purpose);}
 @PostMapping("/kyc/submit") public KycVerification submit(){return privacy.submitKyc();}
 @PostMapping("/kyc/{memberId}/review") public KycVerification review(@PathVariable UUID memberId,@RequestBody KycReview in){return privacy.review(memberId,in.status(),in.reason());}
 @PostMapping("/payments/{aidId}/initiate") public PaymentAttempt initiate(@PathVariable UUID aidId,@RequestHeader("Idempotency-Key") String key){return payments.initiate(aidId,key);}
 @GetMapping("/payments/{aidId}") public List<PaymentAttempt> attempts(@PathVariable UUID aidId){return payments.attempts(aidId);}
 @PostMapping("/sandbox/webhooks/payment") @ResponseStatus(HttpStatus.NO_CONTENT) public void webhook(@RequestHeader("X-Event-Id") String eventId,@RequestHeader("X-Timestamp") String timestamp,@RequestHeader("X-Signature") String signature,@RequestBody WebhookBody in){String body="{\"providerReference\":\""+in.providerReference()+"\",\"status\":\""+in.status()+"\"}";payments.handleWebhook(eventId,timestamp,signature,body,in.providerReference(),in.status());}
}
