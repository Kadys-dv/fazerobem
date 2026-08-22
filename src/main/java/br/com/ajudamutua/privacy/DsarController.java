package br.com.ajudamutua.privacy;

import br.com.ajudamutua.model.DsarRequest;
import br.com.ajudamutua.model.DsarRequestType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/privacy/dsar")
public class DsarController {
    private final DsarService service;

    public DsarController(DsarService service) {
        this.service = service;
    }

    public record DsarCreateRequest(DsarRequestType type) {}
    public record CompleteBody(@NotBlank String notes) {}

    @PostMapping
    public DsarRequest create(@RequestBody DsarCreateRequest in) {
        return service.request(in.type());
    }

    @GetMapping("/mine")
    public List<DsarRequest> mine() {
        return service.mine();
    }

    @GetMapping("/export/{memberId}")
    public Map<String, Object> export(@PathVariable UUID memberId) {
        return service.export(memberId);
    }

    @PostMapping("/{id}/complete")
    public DsarRequest complete(@PathVariable UUID id, @RequestBody CompleteBody in) {
        return service.complete(id, in.notes());
    }
}
