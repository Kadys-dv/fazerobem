package br.com.ajudamutua.repository; import br.com.ajudamutua.model.AppUser; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AppUserRepository extends JpaRepository<AppUser,UUID>{ Optional<AppUser> findByEmailIgnoreCase(String email); }
