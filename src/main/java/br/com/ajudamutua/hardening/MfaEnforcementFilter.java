package br.com.ajudamutua.hardening;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.security.core.Authentication; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.Set;
@Component public class MfaEnforcementFilter extends OncePerRequestFilter {
 private final boolean required; private static final Set<String> PRIV=Set.of("ROLE_ANALYST","ROLE_APPROVER","ROLE_ADMIN","ROLE_AUDITOR");
 public MfaEnforcementFilter(@Value("${app.security.mfa-required-for-privileged:true}") boolean required){this.required=required;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String uri=req.getRequestURI(); if(!required||uri.startsWith("/api/v1/mfa/")||uri.startsWith("/api/v1/auth/")||uri.startsWith("/api/v1/transparency")||uri.startsWith("/api/v1/aid-policies")||uri.startsWith("/api/v1/sandbox/webhooks")||!uri.startsWith("/api/v1/")){chain.doFilter(req,res);return;}
  Authentication a=SecurityContextHolder.getContext().getAuthentication(); boolean privileged=a!=null&&a.isAuthenticated()&&a.getAuthorities().stream().anyMatch(x->PRIV.contains(x.getAuthority())); boolean passkey=a!=null&&a.isAuthenticated()&&a.getClass().getName().contains("WebAuthnAuthentication"); if(passkey&&req.getSession(false)!=null) req.getSession(false).setAttribute("MFA_VERIFIED",Boolean.TRUE);
  if(privileged&&!Boolean.TRUE.equals(req.getSession(false)==null?null:req.getSession(false).getAttribute("MFA_VERIFIED"))){res.setStatus(428);res.setContentType("application/json");res.getWriter().write("{\"error\":\"MFA_REQUIRED\"}");return;} chain.doFilter(req,res);
 }
}
