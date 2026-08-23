package br.com.ajudamutua.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payment-destinations")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentDestinationAdminController {
    private final PaymentDestinationService destinations;

    public PaymentDestinationAdminController(PaymentDestinationService destinations) {
        this.destinations = destinations;
    }

    @PutMapping("/{memberId}/pix")
    public DestinationView savePix(@PathVariable UUID memberId,
                                   @Valid @RequestBody SavePixDestinationRequest request) {
        PaymentDestination saved = destinations.savePixDestination(memberId, request.getKeyType(), request.getPixKey());
        return DestinationView.from(saved);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID memberId) {
        destinations.deactivate(memberId);
        return ResponseEntity.noContent().build();
    }

    public static final class SavePixDestinationRequest {
        @NotNull
        private PaymentDestination.PixKeyType keyType;
        @NotBlank
        private String pixKey;

        public SavePixDestinationRequest() {}

        public PaymentDestination.PixKeyType getKeyType() { return keyType; }
        public void setKeyType(PaymentDestination.PixKeyType keyType) { this.keyType = keyType; }
        public String getPixKey() { return pixKey; }
        public void setPixKey(String pixKey) { this.pixKey = pixKey; }

        @Override
        public String toString() {
            return "SavePixDestinationRequest{keyType=" + keyType + ", pixKey=[REDACTED]}";
        }
    }

    public record DestinationView(UUID memberId,
                                  PaymentDestination.PixKeyType keyType,
                                  String masked,
                                  boolean active,
                                  Instant updatedAt) {
        static DestinationView from(PaymentDestination destination) {
            return new DestinationView(
                    destination.getMemberId(),
                    destination.getPixKeyType(),
                    destination.getDestinationMasked(),
                    destination.isActive(),
                    destination.getUpdatedAt());
        }
    }
}
