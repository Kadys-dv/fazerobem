package br.com.ajudamutua.security;

import br.com.ajudamutua.hardening.LoginAttemptService;
import br.com.ajudamutua.hardening.MfaEnforcementFilter;
import br.com.ajudamutua.hardening.RateLimitFilter;
import br.com.ajudamutua.repository.AppUserRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    UserDetailsService userDetailsService(AppUserRepository users) {
        return username -> {
            var u = users.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(Instant.now())) {
                throw new LockedException("Conta temporariamente bloqueada");
            }
            return User.withUsername(u.getEmail())
                    .password(u.getPasswordHash())
                    .roles(u.getRole().name())
                    .disabled(!u.isEnabled())
                    .build();
        };
    }

    @Bean
    SecurityFilterChain security(
            HttpSecurity http,
            RateLimitFilter rate,
            MfaEnforcementFilter mfa,
            LoginAttemptService loginAttempts,
            @Value("${app.webauthn.rp-name:Ajuda Mutua Community}") String rpName,
            @Value("${app.webauthn.rp-id:localhost}") String rpId,
            @Value("${app.webauthn.allowed-origins:http://localhost:8080}") String origins) throws Exception {

        http.authorizeHttpRequests(a -> a
                        .requestMatchers("/operations.html", "/operations.js", "/operations-dashboard.js", "/operations.css")
                        .hasAnyRole("ANALYST", "APPROVER", "ADMIN", "AUDITOR")
                        .requestMatchers(
                                "/health",
                                "/", "/index.html", "/app.css", "/app.js", "/member-onboarding.js",
                                "/terms.html", "/privacy.html", "/community-rules.html",
                                "/manifest.webmanifest", "/sw.js", "/icon.svg",
                                "/api/v1/transparency", "/api/v1/transparency/ledger",
                                "/api/v1/aid-policies",
                                "/api/v1/auth/register", "/api/v1/auth/csrf", "/api/v1/auth/me",
                                "/api/v1/sandbox/webhooks/payment",
                                "/api/v1/transparency/reports/**",
                                "/webauthn/authenticate/options", "/login/webauthn", "/login/webauthn.js")
                        .permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(request ->
                                "/api/v1/sandbox/webhooks/payment".equals(request.getRequestURI())))
                .webAuthn(w -> w
                        .rpName(rpName)
                        .rpId(rpId)
                        .allowedOrigins(origins.split(",")))
                .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives(
                        "default-src 'self'; object-src 'none'; frame-ancestors 'none'; " +
                        "base-uri 'self'; form-action 'self'; script-src 'self'; style-src 'self'")))
                .sessionManagement(sm -> sm
                        .sessionFixation(sf -> sf.migrateSession())
                        .maximumSessions(1))
                .formLogin(f -> f
                        .successHandler((req, res, auth) -> {
                            loginAttempts.success(auth.getName(), req.getRemoteAddr());
                            res.sendRedirect("/");
                        })
                        .failureHandler((req, res, ex) -> {
                            String email = req.getParameter("username");
                            loginAttempts.failure(email, req.getRemoteAddr());
                            res.sendRedirect("/?loginError=1");
                        })
                        .permitAll())
                .logout(l -> l
                        .logoutSuccessHandler((req, res, auth) -> res.sendRedirect("/"))
                        .permitAll())
                .addFilterBefore(rate, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(mfa, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
