# Threat → Control → Test → CI Gate → Evidence

Esta matriz conecta riscos relevantes do Fazer o Bem às defesas implementadas e às evidências técnicas disponíveis. Ela **não substitui pentest, validação jurídica/LGPD, rehearsal no staging real ou homologação contratual do provedor**.

| Ameaça / falha | Controle principal | Teste / verificação | Gate CI | Evidência esperada |
|---|---|---|---|---|
| Duas pessoas aprovarem com a mesma identidade / contorno de segregação | dupla aprovação por usuários distintos + papéis separados | testes de autorização e E2E por papel | `security`, `pilot-simulation` | teste rejeita mesma identidade e fluxo só avança com aprovadores distintos |
| ADMIN forçar liquidação | `PAID` apenas após settlement externo autenticado | testes negativos de transição e E2E operacional | `provider-homologation`, `operational-reconciliation` | nenhuma rota/botão autorizado produz `PAID` sem confirmação externa |
| Webhook forjado | HMAC + segredo do provedor + validação temporal | assinatura inválida/ausente | `security`, `provider-homologation` | requests inválidas rejeitadas |
| Replay de webhook | nonce/timestamp/janela temporal + Redis | envio repetido do mesmo evento | `security`, `provider-homologation` | segunda tentativa rejeitada ou tratada idempotentemente |
| Pagamento duplicado por retry | idempotency key + unicidade + transação | retries concorrentes | `load-concurrency`, `provider-homologation` | uma única tentativa/baixa efetiva |
| Corrida entre aprovações/pagamento | locks, transações e invariantes no PostgreSQL | execução concorrente real | `load-concurrency`, `pilot-simulation` | invariantes preservadas sob simultaneidade |
| Redis indisponível | PostgreSQL como fonte de verdade; fail-closed/degradação segura | restart/indisponibilidade Redis | `chaos-dr` | sem criação indevida de estado financeiro final |
| Perda/corrupção de estado persistente | backup + checksum + restore controlado | backup/restore em PostgreSQL de CI | `chaos-dr`, `go-live-readiness` | dados restaurados e checks de integridade passam |
| Alteração silenciosa de ledger/auditoria | cadeia SHA-256 append-only | verificação de integridade/tamper | `security`, `pilot-simulation` | adulteração detectada pela cadeia |
| Configuração insegura em produção | profile production fail-closed | startup incompleto/inseguro | `production-readiness` | aplicação se recusa a subir |
| HTTP/origin/RP ID incorretos | TLS obrigatório + validação WebAuthn por ambiente | cenários negativos de config | `production-readiness`, `staging-config` | startup rejeita configuração inconsistente |
| Credenciais/chaves expostas | secrets externos/KMS + ausência de fallback de produção + secret scan | scans/config validation | `security`, `production-readiness` | nenhum secret funcional obrigatório embutido em production |
| Comprometimento de conta privilegiada | WebAuthn/passkeys + TOTP/MFA | E2E WebAuthn e autorização | `security` | operação privilegiada exige fator configurado |
| Divergência entre estado local e provider | consulta externa + `RECONCILIATION_REQUIRED` | provider pendente/divergente | `provider-homologation`, `operational-reconciliation` | incerteza não vira `PAID`; item fica reconciliável |
| Provider lento/indisponível | timeout + retry/backoff + mesma idempotency key | timeout/falhas transitórias | `provider-homologation` | sem duplicidade e com resultado pendente/reconciliável |
| Backlog financeiro envelhecido | aging + métricas/alertas | pagamentos PROCESSING/reconciliation antigos | `operational-reconciliation` | métricas warning/critical e contadores gerados |
| Deploy defeituoso | freeze de novas iniciações + health externo + rollback | rehearsal automatizado | `go-live-readiness` | fluxo de freeze/deploy/health/rollback validado |
| Restore no ambiente errado | confirmação explícita + target isolado + checksum | tentativas negativas de restore | `staging-rehearsal-package`, `go-live-readiness` | restore perigoso recusado |
| Dependência vulnerável / alteração insegura | OWASP Dependency-Check, CodeQL, SBOM, testes | scans automatizados | `security` | pipeline falha conforme políticas configuradas |
| Regressão de fluxo completo | piloto com membros/pedidos/pagamentos fictícios | simulação end-to-end | `pilot-simulation` | cenário completo atravessa invariantes esperadas |

## Evidência externa ainda pendente

A matriz acima comprova controles internos testáveis. Antes de dinheiro real, permanecem obrigatórias as quatro trilhas da issue #38:

1. rehearsal no staging alvo real;
2. pentest independente com reteste;
3. validação jurídica, contábil, regulatória e LGPD;
4. homologação contratual/compliance do provedor financeiro.

Qualquer uma pendente mantém **NO-GO para dinheiro real**.

## Regra de manutenção

Quando um controle crítico mudar, atualizar simultaneamente:

- threat model;
- ADR relacionado;
- teste automatizado;
- gate de CI quando aplicável;
- esta matriz.

Nenhuma linha deve declarar evidência externa como concluída sem artefato verificável.