package br.com.ajudamutua.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/provider-probe")
@ConditionalOnProperty(name = "app.payment-provider.type", havingValue = "asaas-sandbox")
public class AsaasSandboxProbeController {
    private final AsaasSandboxAuthProbe probe;

    public AsaasSandboxProbeController(AsaasSandboxAuthProbe probe) {
        this.probe = probe;
    }

    @GetMapping("/asaas")
    public ResponseEntity<Map<String, String>> probe() {
        try {
            boolean authenticated = probe.authenticated();
            return authenticated
                    ? ResponseEntity.ok(Map.of("provider", "ASAAS_SANDBOX", "auth", "PASS"))
                    : ResponseEntity.status(502).body(Map.of("provider", "ASAAS_SANDBOX", "auth", "FAIL"));
        } catch (Exception ignored) {
            return ResponseEntity.status(502).body(Map.of("provider", "ASAAS_SANDBOX", "auth", "FAIL"));
        }
    }
}
