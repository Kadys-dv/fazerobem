package br.com.ajudamutua.controller;

import br.com.ajudamutua.dto.ApiDtos;
import br.com.ajudamutua.model.LedgerType;
import br.com.ajudamutua.repository.LedgerEntryRepository;
import br.com.ajudamutua.service.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/member")
public class MemberFinanceController {

    private final CurrentUserService currentUser;
    private final LedgerEntryRepository ledger;

    public MemberFinanceController(CurrentUserService currentUser, LedgerEntryRepository ledger) {
        this.currentUser = currentUser;
        this.ledger = ledger;
    }

    @GetMapping("/contributions")
    @PreAuthorize("hasRole('MEMBER')")
    public List<ApiDtos.PublicLedgerEntry> contributions() {
        var user = currentUser.require();
        return ledger.findByMemberIdOrderByCreatedAtDesc(user.getMemberId()).stream()
                .filter(entry -> entry.getType() == LedgerType.CONTRIBUTION)
                .map(entry -> new ApiDtos.PublicLedgerEntry(
                        entry.getType().name(),
                        entry.getAmount(),
                        entry.getDescription(),
                        entry.getCreatedAt(),
                        entry.getEntryHash()))
                .toList();
    }
}
