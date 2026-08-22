package br.com.ajudamutua.controller;

import br.com.ajudamutua.repository.AppUserRepository;
import br.com.ajudamutua.service.CommunityService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class AuthControllerTest {

    @Test
    void anonymousAuthContextDoesNotExposeIdentityData() {
        AuthController controller = new AuthController(
                mock(CommunityService.class),
                mock(AppUserRepository.class));

        Map<String, Object> response = controller.me(null);

        assertEquals(Boolean.FALSE, response.get("authenticated"));
        assertFalse(response.containsKey("userId"));
        assertFalse(response.containsKey("memberId"));
        assertFalse(response.containsKey("email"));
        assertNull(response.get("role"));
    }
}
