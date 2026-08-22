package br.com.ajudamutua.controller;
import br.com.ajudamutua.dto.ApiDtos; import br.com.ajudamutua.model.*; import br.com.ajudamutua.privacy.MemberConsentPolicy; import br.com.ajudamutua.service.CommunityService; import jakarta.validation.Valid; import org.springframework.core.io.Resource; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.nio.charset.StandardCharsets; import java.util.*;
@RestController @RequestMapping("/api/v1")
public class CommunityController { private final CommunityService service; private final MemberConsentPolicy consentPolicy; public CommunityController(CommunityService service,MemberConsentPolicy consentPolicy){this.service=service;this.consentPolicy=consentPolicy;}
 @PostMapping("/members") @ResponseStatus(HttpStatus.CREATED) public Member member(@Valid @RequestBody ApiDtos.CreateMember in){return service.createMember(in);}
 @PostMapping("/contributions") @ResponseStatus(HttpStatus.CREATED) public LedgerEntry contribution(@Valid @RequestBody ApiDtos.Contribution in){consentPolicy.requireForCurrentMember();return service.contribute(in);}
 @PostMapping("/aid-requests") @ResponseStatus(HttpStatus.CREATED) public AidRequest aid(@Valid @RequestBody ApiDtos.CreateAid in){consentPolicy.requireForCurrentMember();return service.requestAid(in);}
 @GetMapping("/aid-requests") public List<AidRequest> aids(){return service.aidRequests();}
 @GetMapping("/aid-requests/mine") public List<AidRequest> mine(){return service.myAidRequests();}
 @PostMapping(value="/aid-requests/{id}/documents",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED) public AidDocument document(@PathVariable UUID id,@RequestParam String documentType,@RequestPart("file") MultipartFile file){consentPolicy.requireForCurrentMember();return service.addDocument(id,documentType,file);}
 @GetMapping("/aid-requests/{id}/documents") public List<AidDocument> documents(@PathVariable UUID id){return service.documents(id);}
 @GetMapping("/aid-documents/{id}/content") public ResponseEntity<Resource> documentContent(@PathVariable UUID id){var d=service.documentContent(id); var cd=ContentDisposition.attachment().filename(d.fileName(),StandardCharsets.UTF_8).build(); return ResponseEntity.ok().contentType(MediaType.parseMediaType(d.contentType())).header(HttpHeaders.CONTENT_DISPOSITION,cd.toString()).body(d.resource());}
 @PostMapping("/aid-requests/{id}/analysis") public AidAnalysis analysis(@PathVariable UUID id,@Valid @RequestBody ApiDtos.Analysis in){return service.analyze(id,in.opinion());}
 @PostMapping("/aid-requests/{id}/fraud-screening") public FraudScreening fraud(@PathVariable UUID id,@Valid @RequestBody ApiDtos.FraudReview in){return service.screenFraud(id,in);}
 @GetMapping("/aid-requests/{id}/eligibility") public ApiDtos.EligibilityResult eligibility(@PathVariable UUID id){return service.eligibility(id);}
 @PostMapping("/aid-requests/{id}/approve") public ApiDtos.ApprovalResult approve(@PathVariable UUID id,@Valid @RequestBody ApiDtos.Decision in){return service.approve(id,in.note());}
 @PostMapping("/aid-requests/{id}/reject") public AidRequest reject(@PathVariable UUID id,@Valid @RequestBody ApiDtos.Decision in){return service.reject(id,in.note());}
 @GetMapping("/aid-policies") public List<ApiDtos.CategoryPolicy> policies(){return service.categoryPolicies();}
 @GetMapping("/ledger") public List<LedgerEntry> ledger(){return service.ledger();}
 @GetMapping("/audit-events") public List<AuditEvent> audit(){return service.auditEvents();}
 @GetMapping("/transparency") public ApiDtos.Transparency transparency(){return service.transparency();}
 @GetMapping("/transparency/ledger") public List<ApiDtos.PublicLedgerEntry> publicLedger(){return service.publicLedger();}
}
