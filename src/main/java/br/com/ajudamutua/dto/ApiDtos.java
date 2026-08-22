package br.com.ajudamutua.dto;
import br.com.ajudamutua.model.*;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
public final class ApiDtos { private ApiDtos(){}
 public record CreateMember(@NotBlank String name,@Email @NotBlank String email){}
 public record RegisterMember(@NotBlank String name,@Email @NotBlank String email,@NotBlank @Size(min=10,max=100) String password){}
 public record Contribution(@NotNull UUID memberId,@NotNull @DecimalMin("1.00") BigDecimal amount){}
 public record CreateAid(@NotNull UUID memberId,@NotNull @DecimalMin("1.00") BigDecimal amount,@NotNull AidCategory category,@NotBlank @Size(max=1000) String reason,boolean emergency){}
 public record Decision(@NotBlank @Size(max=500) String note){}
 public record Analysis(@NotBlank @Size(max=1000) String opinion){}
 public record FraudReview(@NotNull FraudScreeningStatus status,@Min(0) @Max(100) int riskScore,@NotNull @Size(max=1000) String flags,@NotBlank @Size(max=1000) String note){}
 public record Transparency(BigDecimal balance,long activeMembers,long totalAidRequests,long paidAidRequests){}
 public record ApprovalResult(UUID aidRequestId,long approvalsRequired,long approvalsReceived,String status){}
 public record PublicLedgerEntry(String type,BigDecimal amount,String description,Instant createdAt,String entryHash){}
 public record EligibilityResult(UUID aidRequestId, boolean eligible, List<String> blockers, List<String> warnings,
                                 BigDecimal categoryMaxAmount, BigDecimal currentFundLimit, int waitingDays, int cooldownDays,
                                 boolean documentRequired, long documentCount, String fraudStatus){}
 public record CategoryPolicy(AidCategory category,int waitingDays,int cooldownDays,BigDecimal maxAmount,boolean documentRequired){}
 public record AidDocumentSummary(UUID id,String documentType,String fileName,String contentType,long sizeBytes,Instant createdAt){}
}
