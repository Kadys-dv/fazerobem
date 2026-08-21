package br.com.ajudamutua.integrity;
import br.com.ajudamutua.model.*; import br.com.ajudamutua.repository.*; import org.springframework.stereotype.Service; import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.*;
@Service public class ChainIntegrityService {
 private static final String GENESIS="0".repeat(64); private final LedgerEntryRepository ledger; private final AuditEventRepository audit;
 public ChainIntegrityService(LedgerEntryRepository l,AuditEventRepository a){ledger=l;audit=a;}
 public record Result(boolean valid,int checked,String firstBrokenId,String message){}
 public Result ledger(){var list=ledger.findAll(org.springframework.data.domain.Sort.by("createdAt").ascending());String prev=GENESIS;int n=0;for(var e:list){n++;String payload=e.getId()+"|"+e.getType()+"|"+e.getAmount().toPlainString()+"|"+e.getMemberId()+"|"+e.getAidRequestId()+"|"+e.getDescription()+"|"+e.getCreatedAt()+"|"+prev;String hash=sha(payload);if(!Objects.equals(prev,e.getPreviousHash())||!Objects.equals(hash,e.getEntryHash()))return new Result(false,n,e.getId().toString(),"Ledger hash chain inválida");prev=e.getEntryHash();}return new Result(true,n,null,"OK");}
 public Result audit(){var list=audit.findAll(org.springframework.data.domain.Sort.by("createdAt").ascending());String prev=GENESIS;int n=0;for(var e:list){n++;String payload=e.getId()+"|"+e.getActorUserId()+"|"+e.getAction()+"|"+e.getEntityType()+"|"+e.getEntityId()+"|"+e.getMetadata()+"|"+e.getCreatedAt()+"|"+prev;String hash=sha(payload);if(!Objects.equals(prev,e.getPreviousHash())||!Objects.equals(hash,e.getEventHash()))return new Result(false,n,e.getId().toString(),"Audit hash chain inválida");prev=e.getEventHash();}return new Result(true,n,null,"OK");}
 private String sha(String x){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(x.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
