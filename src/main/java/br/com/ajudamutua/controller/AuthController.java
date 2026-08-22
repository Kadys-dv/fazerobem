package br.com.ajudamutua.controller;

import br.com.ajudamutua.dto.ApiDtos;
import br.com.ajudamutua.model.AppUser;
import br.com.ajudamutua.repository.AppUserRepository;
import br.com.ajudamutua.service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final CommunityService service;
    private final AppUserRepository users;

    public AuthController(CommunityService service, AppUserRepository users) {
        this.service = service;
        this.users = users;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody ApiDtos.RegisterMember in) {
        AppUser u = service.register(in);
        return Map.of(
                "userId", u.getId(),
                "memberId", u.getMemberId(),
                "email", u.getEmail(),
                "role", u.getRole());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        CsrfToken t = (CsrfToken) request.getAttribute("_csrf");
        return Map.of(
                "headerName", t.getHeaderName(),
                "parameterName", t.getParameterName(),
                "token", t.getToken());
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            response.put("authenticated", false);
            return response;
        }

        return users.findByEmailIgnoreCase(authentication.getName())
                .map(u -> {
                    response.put("authenticated", true);
                    response.put("userId", u.getId());
                    response.put("memberId", u.getMemberId());
                    response.put("email", u.getEmail());
                    response.put("role", u.getRole().name());
                    return response;
                })
                .orElseGet(() -> {
                    response.put("authenticated", false);
                    return response;
                });
    }
}
