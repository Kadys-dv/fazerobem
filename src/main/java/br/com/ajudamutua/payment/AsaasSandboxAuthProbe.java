package br.com.ajudamutua.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.payment-provider.type", havingValue = "asaas-sandbox")
public class AsaasSandboxAuthProbe {
    private final RestClient client;

    public AsaasSandboxAuthProbe(AsaasSandboxPaymentProvider provider) {
        this.client = provider.probeClient();
    }

    @SuppressWarnings("unchecked")
    public boolean authenticated() {
        Map<String, Object> response = client.get()
                .uri("/myAccount")
                .retrieve()
                .body(Map.class);
        return response != null && !response.isEmpty();
    }
}
