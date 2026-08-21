package br.com.ajudamutua.controller;
import br.com.ajudamutua.dto.ApiDtos; import br.com.ajudamutua.model.AppUser; import br.com.ajudamutua.service.CommunityService; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.security.web.csrf.CsrfToken; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {private final CommunityService service; public AuthController(CommunityService service){this.service=service;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> register(@Valid @RequestBody ApiDtos.RegisterMember in){AppUser u=service.register(in); return Map.of("userId",u.getId(),"memberId",u.getMemberId(),"email",u.getEmail(),"role",u.getRole());}
 @GetMapping("/csrf") public Map<String,String> csrf(HttpServletRequest request){CsrfToken t=(CsrfToken)request.getAttribute("_csrf"); return Map.of("headerName",t.getHeaderName(),"parameterName",t.getParameterName(),"token",t.getToken());}
}
