package br.com.ajudamutua.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.payment-provider.type", havingValue = "asaas-sandbox")
public class AsaasSandboxPaymentProvider implements PaymentProvider, DestinationAwarePaymentProvider {
    static final String SANDBOX_BASE_URL = "https://api-sandbox.asaas.com/v3";

    private final RestClient client;

    public AsaasSandboxPaymentProvider(@Value("${app.payment-provider.asaas.base-url:" + SANDBOX_BASE_URL + "}") String baseUrl,
                                       @Value("${app.payment-provider.asaas.api-key:}") String apiKey,
                                       @Value("${app.payment-provider.asaas.user-agent:FazerOBem/0.1 (sandbox)}") String userAgent) {
        if (!SANDBOX_BASE_URL.equals(baseUrl)) {
            throw new IllegalStateException("Asaas sandbox provider aceita somente o endpoint oficial de Sandbox");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ASAAS_SANDBOX_API_KEY obrigatória para o provider Asaas Sandbox");
        }
        if (!apiKey.startsWith("$aact_hmlg_")) {
            throw new IllegalStateException("A chave configurada não possui prefixo de Sandbox do Asaas");
        }
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("access_token", apiKey)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    RestClient probeClient() {
        return client;
    }

    @Override
    public String providerCode() {
        return "ASAAS_SANDBOX";
    }

    @Override
    public Initiation initiate(UUID paymentId, BigDecimal amount, String idempotencyKey) {
        throw new IllegalStateException("Asaas Sandbox requer destino Pix resolvido");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Initiation initiateWithDestination(UUID paymentId,
                                              BigDecimal amount,
                                              String idempotencyKey,
                                              ResolvedPaymentDestination destination) {
        if (paymentId == null || amount == null || amount.signum() <= 0 || destination == null) {
            throw new IllegalArgumentException("Dados de transferência Asaas inválidos");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("value", amount);
        payload.put("operationType", "PIX");
        payload.put("pixAddressKey", destination.revealForProviderCall());
        payload.put("pixAddressKeyType", destination.pixKeyType().name());
        payload.put("externalReference", paymentId.toString());
        payload.put("description", "Auxilio FazerOBem " + paymentId);

        Map<String, Object> response = client.post()
                .uri("/transfers")
                .body(payload)
                .retrieve()
                .body(Map.class);

        String transferId = requiredString(response, "id");
        return new Initiation(transferId, transferId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StatusResult queryStatus(String providerReference) {
        if (providerReference == null || providerReference.isBlank()) {
            throw new IllegalArgumentException("providerReference obrigatório");
        }
        Map<String, Object> response = client.get()
                .uri("/transfers/{id}", providerReference)
                .retrieve()
                .body(Map.class);
        String status = requiredString(response, "status");
        Object failReason = response == null ? null : response.get("failReason");
        return new StatusResult(mapStatus(status), providerReference,
                failReason == null ? "asaas-sandbox" : "asaas-sandbox: " + failReason);
    }

    static ExternalStatus mapStatus(String status) {
        if (status == null) return ExternalStatus.UNKNOWN;
        return switch (status) {
            case "PENDING", "BANK_PROCESSING" -> ExternalStatus.PROCESSING;
            case "DONE" -> ExternalStatus.SETTLED;
            case "CANCELLED", "FAILED" -> ExternalStatus.FAILED;
            default -> ExternalStatus.UNKNOWN;
        };
    }

    private static String requiredString(Map<String, Object> response, String field) {
        if (response == null || response.get(field) == null || response.get(field).toString().isBlank()) {
            throw new IllegalStateException("Resposta Asaas sem campo obrigatório: " + field);
        }
        return response.get(field).toString();
    }
}
