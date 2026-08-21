package br.com.ajudamutua.service;

import br.com.ajudamutua.dto.ApiDtos;
import br.com.ajudamutua.model.*;
import br.com.ajudamutua.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AidPolicyService {
    private static final BigDecimal MAX_FUND_SHARE = new BigDecimal("0.20");
    private static final BigDecimal MIN_RESERVE_SHARE = new BigDecimal("0.30");

    private final MemberRepository members;
    private final AidRequestRepository aids;
    private final AidDocumentRepository documents;
    private final FraudScreeningRepository fraud;
    private final AidAnalysisRepository analyses;
    private final LedgerEntryRepository ledger;

    public AidPolicyService(MemberRepository members, AidRequestRepository aids, AidDocumentRepository documents,
                            FraudScreeningRepository fraud, AidAnalysisRepository analyses, LedgerEntryRepository ledger) {
        this.members=members; this.aids=aids; this.documents=documents; this.fraud=fraud; this.analyses=analyses; this.ledger=ledger;
    }

    public record Policy(int waitingDays, int cooldownDays, BigDecimal maxAmount, boolean documentRequired) {}

    public Policy policy(AidCategory category) {
        return switch (category) {
            case FOOD -> new Policy(14, 30, new BigDecimal("600.00"), false);
            case HEALTH -> new Policy(7, 45, new BigDecimal("2500.00"), true);
            case HOUSING -> new Policy(30, 90, new BigDecimal("1800.00"), true);
            case UTILITIES -> new Policy(30, 60, new BigDecimal("900.00"), true);
            case EMPLOYMENT -> new Policy(30, 90, new BigDecimal("1500.00"), true);
            case EDUCATION -> new Policy(60, 120, new BigDecimal("1800.00"), true);
            case GENERAL_EMERGENCY -> new Policy(30, 60, new BigDecimal("1000.00"), true);
        };
    }

    public ApiDtos.EligibilityResult evaluate(AidRequest aid) {
        Member member = members.findById(aid.getMemberId()).orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));
        Policy p = policy(aid.getCategory());
        List<String> blockers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Instant now = Instant.now();

        int effectiveWaiting = aid.isEmergency() && (aid.getCategory()==AidCategory.HEALTH || aid.getCategory()==AidCategory.FOOD)
                ? 0 : p.waitingDays();
        Instant eligibleSince = member.getJoinedAt().plus(effectiveWaiting, ChronoUnit.DAYS);
        if (now.isBefore(eligibleSince)) blockers.add("Carência de " + effectiveWaiting + " dias ainda não cumprida");
        if (aid.getAmount().compareTo(p.maxAmount()) > 0) blockers.add("Valor excede o teto da categoria: R$ " + p.maxAmount());

        List<AidRequest> paid = aids.findByMemberIdAndStatusOrderByCreatedAtDesc(member.getId(), AidStatus.PAID);
        if (!paid.isEmpty()) {
            Instant nextAllowed = paid.getFirst().getCreatedAt().plus(p.cooldownDays(), ChronoUnit.DAYS);
            if (now.isBefore(nextAllowed)) blockers.add("Período mínimo entre auxílios ainda não cumprido");
        }

        long paidLast365 = aids.countByMemberIdAndStatusAndCreatedAtAfter(member.getId(), AidStatus.PAID, now.minus(365, ChronoUnit.DAYS));
        if (paidLast365 >= 3) blockers.add("Limite anual de 3 auxílios por membro atingido");
        else if (paidLast365 == 2) warnings.add("Este será o terceiro auxílio do membro em 12 meses");

        long docCount = documents.countByAidRequestId(aid.getId());
        if (p.documentRequired() && docCount == 0) blockers.add("Documento comprobatório obrigatório para esta categoria");

        if (analyses.findByAidRequestId(aid.getId()).isEmpty()) blockers.add("Parecer de analista obrigatório");

        var screening = fraud.findByAidRequestId(aid.getId());
        if (screening.isEmpty()) blockers.add("Triagem antifraude obrigatória");
        else {
            switch (screening.get().getStatus()) {
                case CLEARED -> { if (screening.get().getRiskScore() >= 60) warnings.add("Triagem liberada com risco elevado"); }
                case PENDING, REVIEW_REQUIRED -> blockers.add("Triagem antifraude ainda requer revisão");
                case BLOCKED -> blockers.add("Pedido bloqueado pela triagem antifraude");
            }
        }

        BigDecimal balance = ledger.balance();
        BigDecimal maxByShare = balance.multiply(MAX_FUND_SHARE).setScale(2, RoundingMode.DOWN);
        BigDecimal reserve = balance.multiply(MIN_RESERVE_SHARE).setScale(2, RoundingMode.UP);
        BigDecimal availableAboveReserve = balance.subtract(reserve).max(BigDecimal.ZERO);
        BigDecimal fundLimit = maxByShare.min(availableAboveReserve);
        if (aid.getAmount().compareTo(fundLimit) > 0) blockers.add("Valor excede o limite prudencial atual do fundo: R$ " + fundLimit);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) blockers.add("Fundo sem saldo disponível");

        return new ApiDtos.EligibilityResult(aid.getId(), blockers.isEmpty(), List.copyOf(blockers), List.copyOf(warnings),
                p.maxAmount(), fundLimit, effectiveWaiting, p.cooldownDays(), p.documentRequired(), docCount,
                screening.map(s -> s.getStatus().name()).orElse("MISSING"));
    }
}
