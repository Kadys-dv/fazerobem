package br.com.ajudamutua.security;

import br.com.ajudamutua.model.AppUser;
import br.com.ajudamutua.model.UserRole;
import br.com.ajudamutua.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@Configuration
@Profile("staging")
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = "enabled", havingValue = "true")
public class StagingAdminBootstrapConfig {

    @Bean
    CommandLineRunner stagingAdminBootstrap(AppUserRepository users,
                                            PasswordEncoder encoder,
                                            @Value("${app.bootstrap-admin.email:}") String configuredEmail,
                                            @Value("${app.bootstrap-admin.password:}") String configuredPassword) {
        return args -> {
            String email = configuredEmail == null ? "" : configuredEmail.trim().toLowerCase();
            String password = configuredPassword == null ? "" : configuredPassword;

            if (email.isBlank() || !email.contains("@")) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL must be a valid email when staging admin bootstrap is enabled");
            }
            if (password.length() < 12) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must contain at least 12 characters");
            }

            var existing = users.findByEmailIgnoreCase(email);
            if (existing.isPresent()) {
                if (existing.get().getRole() != UserRole.ADMIN) {
                    throw new IllegalStateException("Bootstrap admin email already belongs to a non-ADMIN account");
                }
                return;
            }

            users.save(new AppUser(
                    UUID.randomUUID(),
                    email,
                    encoder.encode(password),
                    UserRole.ADMIN,
                    null,
                    true,
                    Instant.now()
            ));
        };
    }
}
