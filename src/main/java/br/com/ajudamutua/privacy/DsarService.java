package br.com.ajudamutua.privacy;

import br.com.ajudamutua.crypto.SecretProtector;
import br.com.ajudamutua.model.*;
import br.com.ajudamutua.repository.*;
import br.com.ajudamutua.service.AuditService;
import br.com.ajudamutua.service.CurrentUserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class DsarService {
    private final DsarRequestRepository dsars;
    private final MemberRepository members;
    private final MemberPrivateDataRepository privateData;
    private final ConsentRecordRepository consents;
    private final AidRequestRepository aids;
    private final LedgerEntryRepository ledger;
    private final CurrentUserService current;
    private final SecretProtector crypto;
    private final AuditService audit;
    private final JsonMapper mapper;

    public DsarService(
            DsarRequestRepository dsars,
            MemberRepository members,
            MemberPrivateDataRepository privateData,
            ConsentRecordRepository consents,
            AidRequestRepository aids,
            LedgerEntryRepository ledger,
            CurrentUserService current,
            SecretProtector crypto,
            AuditService audit,
            JsonMapper mapper) {
        this.dsars = dsars;
        this.members = members;
        this.privateData = privateData;
        this.consents = consents;
        this.aids = aids;
        this.ledger = ledger;
        this.current = current;
        this.crypto = crypto;
        this.audit = audit;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasRole('MEMBER')")
    public DsarRequest request(DsarRequestType type) {
        AppUser user = current.require();
        DsarRequest request = dsars.save(new DsarRequest(UUID.randomUUID(), user.getMemberId(), type));
        audit.append(user.getId(), "DSAR_REQUESTED", "DsarRequest", request.getId(),
                "{\"type\":\"" + type + "\"}");
        return request;
    }

    @PreAuthorize("hasRole('MEMBER')")
    public List<DsarRequest> mine() {
        return dsars.findByMemberIdOrderByRequestedAtDesc(current.require().getMemberId());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MEMBER','ADMIN','AUDITOR')")
    public Map<String, Object> export(UUID memberId) {
        AppUser user = current.require();
        if (user.getRole() == UserRole.MEMBER && !Objects.equals(user.getMemberId(), memberId)) {
            throw new IllegalStateException("Acesso negado");
        }

        Member member = members.findById(memberId).orElseThrow();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("member", Map.of(
                "id", member.getId(),
                "name", member.getName(),
                "email", member.getEmail(),
                "status", member.getStatus(),
                "joinedAt", member.getJoinedAt()));

        privateData.findById(memberId).ifPresent(data -> out.put("privateData", Map.of(
                "cpf", crypto.decrypt(data.getCpfCiphertext()),
                "address", crypto.decrypt(data.getAddressCiphertext()),
                "updatedAt", data.getUpdatedAt())));
        out.put("consents", consents.findByMemberIdOrderByAcceptedAtDesc(memberId));
        out.put("aidRequests", aids.findByMemberIdOrderByCreatedAtDesc(memberId));
        out.put("ledger", ledger.findByMemberIdOrderByCreatedAtDesc(memberId));
        audit.append(user.getId(), "DSAR_EXPORT_GENERATED", "Member", memberId, "{}");
        return out;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public DsarRequest complete(UUID id, String notes) {
        AppUser actor = current.require();
        DsarRequest request = dsars.findById(id).orElseThrow();
        String hash = null;
        if (request.getRequestType() == DsarRequestType.ACCESS_EXPORT) {
            try {
                String json = mapper.writeValueAsString(export(request.getMemberId()));
                hash = HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        request.complete(actor.getId(), notes, hash);
        audit.append(actor.getId(), "DSAR_COMPLETED", "DsarRequest", id, "{}");
        return request;
    }
}
