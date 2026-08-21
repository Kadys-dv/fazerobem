package br.com.ajudamutua.controller;
import org.springframework.http.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(AccessDeniedException.class) public ResponseEntity<Map<String,Object>> denied(){return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("timestamp",Instant.now(),"error","Acesso negado"));}
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class}) public ResponseEntity<Map<String,Object>> bad(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now(),"error",e.getMessage()));}
}
