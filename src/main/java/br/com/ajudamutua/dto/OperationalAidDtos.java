package br.com.ajudamutua.dto;

import br.com.ajudamutua.model.AidRequest;
import br.com.ajudamutua.model.FraudScreeningStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OperationalAidDtos {
    private OperationalAidDtos() {}

    public record AnalysisSummary(UUID id, UUID analystUserId, String opinion, Instant createdAt) {}

    public record FraudSummary(
            UUID id,
            UUID analystUserId,
            FraudScreeningStatus status,
            int riskScore,
            String flags,
            String note,
            Instant createdAt) {}

    public record ApprovalSummary(UUID id, UUID approverUserId, String note, Instant createdAt) {}

    public record AidCaseDetail(
            AidRequest request,
            ApiDtos.EligibilityResult eligibility,
            List<ApiDtos.AidDocumentSummary> documents,
            List<AnalysisSummary> analyses,
            FraudSummary fraudScreening,
            List<ApprovalSummary> approvals) {}
}
