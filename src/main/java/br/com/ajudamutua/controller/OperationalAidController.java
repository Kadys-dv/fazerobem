package br.com.ajudamutua.controller;

import br.com.ajudamutua.dto.OperationalAidDtos;
import br.com.ajudamutua.service.OperationalAidService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations/aid-requests")
public class OperationalAidController {
    private final OperationalAidService service;

    public OperationalAidController(OperationalAidService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public OperationalAidDtos.AidCaseDetail detail(@PathVariable UUID id) {
        return service.detail(id);
    }
}
