# Fase 7 — Validação independente e readiness

Esta fase não habilita dinheiro real. O objetivo é aumentar a confiança no piloto antes de qualquer homologação externa.

## Controles adicionados

- verificação interna da cadeia SHA-256 do ledger e da auditoria;
- recuperação de conta administrativa com dual control: 1 solicitante + 2 aprovadores distintos + 1 executor distinto;
- expiração de solicitações de recuperação em 1 hora;
- sessão privilegiada limitada a uma sessão concorrente por usuário;
- `SameSite=Strict`, `HttpOnly`, suporte a cookie `Secure` e CSP restritiva;
- graceful shutdown para reduzir interrupções durante operações;
- smoke test Testcontainers PostgreSQL + Redis;
- base Playwright para E2E e WebAuthn virtual authenticator;
- verificador offline Ed25519 para relatórios públicos;
- validação automatizada de backup → restore → consulta de integridade;
- checklist de chaos testing, pentest e segregação sandbox/produção.

## Critérios de bloqueio para produção

Não operar com dinheiro real enquanto qualquer item estiver pendente: build/testes reproduzíveis; pentest independente; restauração de backup testada; KMS real; TLS válido; cookie Secure; segredo de webhook fora do repositório; WebAuthn em domínio HTTPS; runbook de incidentes ensaiado; revisão jurídica/regulatória; reconciliação monitorada; alertas externos ativos.
