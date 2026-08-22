package br.com.ajudamutua.service;

import br.com.ajudamutua.model.SignedTransparencyReport;
import br.com.ajudamutua.repository.SignedTransparencyReportRepository;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class TransparencyReportService {
    private final CommunityService community;
    private final SignedTransparencyReportRepository repo;
    private final CurrentUserService current;
    private final AuditService audit;
    private final JsonMapper mapper;
    private final String privateKey;
    private final String publicKey;
    private final String keyId;

    public TransparencyReportService(
            CommunityService community,
            SignedTransparencyReportRepository repo,
            CurrentUserService current,
            AuditService audit,
            JsonMapper mapper,
            @Value("${TRANSPARENCY_ED25519_PRIVATE_KEY_BASE64:}") String privateKey,
            @Value("${TRANSPARENCY_ED25519_PUBLIC_KEY_BASE64:}") String publicKey,
            @Value("${TRANSPARENCY_SIGNING_KEY_ID:transparency-v1}") String keyId) {
        this.community = community;
        this.repo = repo;
        this.current = current;
        this.audit = audit;
        this.mapper = mapper;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.keyId = keyId;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public SignedTransparencyReport generate(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Período inválido");
        }
        requireKey();
        try {
            Map<String, Object> payload = new TreeMap<>();
            payload.put("periodStart", start);
            payload.put("periodEnd", end);
            payload.put("generatedAt", Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            payload.put("summary", community.transparency());
            payload.put("ledger", community.publicLedger());

            String json = mapper.writeValueAsString(payload);
            String hash = sha(json);
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(KeyFactory.getInstance("Ed25519").generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey))));
            signature.update(json.getBytes(StandardCharsets.UTF_8));
            String sig = Base64.getEncoder().encodeToString(signature.sign());

            SignedTransparencyReport out = repo.save(new SignedTransparencyReport(
                    UUID.randomUUID(), start, end, json, hash, sig, keyId));
            audit.append(
                    current.require().getId(),
                    "TRANSPARENCY_REPORT_SIGNED",
                    "SignedTransparencyReport",
                    out.getId(),
                    "{\"sha256\":\"" + hash + "\"}");
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar relatório", e);
        }
    }

    public Optional<SignedTransparencyReport> latest() {
        return repo.findTopByOrderByCreatedAtDesc();
    }

    public Map<String, String> verificationInfo() {
        return Map.of(
                "algorithm", "Ed25519",
                "keyId", keyId,
                "publicKeyBase64", publicKey == null ? "" : publicKey);
    }

    private void requireKey() {
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("TRANSPARENCY_ED25519_PRIVATE_KEY_BASE64 não configurada");
        }
    }

    private String sha(String value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
