package br.com.ajudamutua.service;

import br.com.ajudamutua.dto.ApiDtos;
import br.com.ajudamutua.dto.OperationalAidDtos;
import br.com.ajudamutua.model.AidDocument;
import br.com.ajudamutua.model.FraudScreening;
import br.com.ajudamutua.repository.AidAnalysisRepository;
import br.com.ajudamutua.repository.AidApprovalRepository;
import br.com.ajudamutua.repository.AidDocumentRepository;
import br.com.ajudamutua.repository.AidRequestRepository;
import br.com.ajudamutua.repository.FraudScreeningRepository;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationalAidService {
    private final AidRequestRepository aids;
    private final AidDocumentRepository documents;
    private final AidAnalysisRepository analyses;
    private final FraudScreeningRepository fraud;
    private final AidApprovalRepository approvals;
    private final AidPolicyService policy;

    public OperationalAidService(
            AidRequestRepository aids,
            AidDocumentRepository documents,
            AidAnalysisRepository analyses,
            FraudScreeningRepository fraud,
            AidApprovalRepository approvals,
            AidPolicyService policy) {
        this.aids = aids;
        this.documents = documents;
        this.analyses = analyses;
        this.fraud = fraud;
        this.approvals = approvals;
        this.policy = policy;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ANALYST','APPROVER','ADMIN','AUDITOR')")
    public OperationalAidDtos.AidCaseDetail detail(UUID id) {
        var request = aids.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));

        var documentSummaries = documents.findByAidRequestId(id).stream()
                .sorted(Comparator.comparing(AidDocument::getCreatedAt))
                .map(this::documentSummary)
                .toList();

        var analysisSummaries = analyses.findByAidRequestId(id).stream()
                .sorted(Comparator.comparing(a -> a.getCreatedAt()))
                .map(a -> new OperationalAidDtos.AnalysisSummary(
                        a.getId(), a.getAnalystUserId(), a.getOpinion(), a.getCreatedAt()))
                .toList();

        var fraudSummary = fraud.findByAidRequestId(id)
                .map(this::fraudSummary)
                .orElse(null);

        var approvalSummaries = approvals.findByAidRequestId(id).stream()
                .sorted(Comparator.comparing(a -> a.getCreatedAt()))
                .map(a -> new OperationalAidDtos.ApprovalSummary(
                        a.getId(), a.getApproverUserId(), a.getNote(), a.getCreatedAt()))
                .toList();

        return new OperationalAidDtos.AidCaseDetail(
                request,
                policy.evaluate(request),
                documentSummaries,
                analysisSummaries,
                fraudSummary,
                approvalSummaries);
    }

    private ApiDtos.AidDocumentSummary documentSummary(AidDocument d) {
        return new ApiDtos.AidDocumentSummary(
                d.getId(), d.getDocumentType(), d.getFileName(), d.getContentType(), d.getSizeBytes(), d.getCreatedAt());
    }

    private OperationalAidDtos.FraudSummary fraudSummary(FraudScreening f) {
        return new OperationalAidDtos.FraudSummary(
                f.getId(), f.getAnalystUserId(), f.getStatus(), f.getRiskScore(), f.getFlags(), f.getNote(), f.getCreatedAt());
    }
}
