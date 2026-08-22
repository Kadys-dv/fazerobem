# Observability & Alerts

Baseline de observabilidade para staging/piloto sandbox.

## Sinais prioritários

- taxa de HTTP 5xx;
- latência p95/p99;
- disponibilidade PostgreSQL e Redis;
- tentativas de pagamento em `PROCESSING` acima do SLA;
- quantidade/taxa de `RECONCILIATION_REQUIRED`;
- pagamentos `FAILED`;
- webhooks rejeitados por assinatura, timestamp ou replay;
- crescimento da outbox não processada;
- falhas de autenticação/MFA em contas privilegiadas;
- falha do verificador de integridade do ledger/auditoria;
- latência e erro de consultas externas de reconciliação;
- divergências entre estado interno e provider sandbox.

## Aging operacional de pagamentos

Os thresholds são configuráveis e devem ser calibrados por ambiente:

- `app.reconciliation.scan-after` — idade mínima para o job consultar o provider; default `PT10M`;
- `app.observability.payment-stuck-after` — warning de `PROCESSING`; default `PT10M`;
- `app.observability.payment-critical-after` — condição crítica de aging; default `PT30M`.

A configuração não autoriza liquidação manual. Aging apenas determina quando consultar, medir e alertar.

## Métricas de reconciliação

- `fazerobem.payment.processing.stuck` — `PROCESSING` além do warning;
- `fazerobem.payment.processing.critical` — `PROCESSING` além do critical;
- `fazerobem.payment.reconciliation.required` — backlog total em reconciliação;
- `fazerobem.payment.reconciliation.aged` — reconciliações além do critical;
- `fazerobem.reconciliation.scanned.total` — itens examinados pelo job;
- `fazerobem.reconciliation.settled.total` — confirmações externas liquidadas pelo caminho financeiro único;
- `fazerobem.reconciliation.failed.total` — falhas confirmadas externamente;
- `fazerobem.reconciliation.pending.total` — itens ainda pendentes externamente;
- `fazerobem.reconciliation.divergence.total` — divergências/estado externo desconhecido;
- `fazerobem.reconciliation.provider_unavailable.total` — consultas externas que falharam;
- `fazerobem.reconciliation.provider_query` — timer/histograma de latência da consulta externa.

## Alertas mínimos

| Alerta | Severidade | Condição inicial |
|---|---|---|
| Ledger/audit integrity failure | SEV-1 | qualquer ocorrência |
| Duplicate financial effect detected | SEV-1 | qualquer ocorrência |
| Provider/internal divergence | SEV-2 | qualquer incremento persistente de `reconciliation.divergence.total` |
| Reconciliation aged | SEV-2 | `payment.reconciliation.aged > 0` |
| Payment PROCESSING critical | SEV-2 | `payment.processing.critical > 0` |
| Provider unavailable | SEV-2/3 | falhas repetidas de consulta externa em janela curta |
| Payment PROCESSING stale | SEV-3 | `payment.processing.stuck > 0` |
| Webhook auth/replay spike | SEV-2 | crescimento anormal em janela curta |
| PostgreSQL unavailable | SEV-2 | health down persistente |
| Redis unavailable | SEV-2 | health down persistente |
| HTTP 5xx spike | SEV-2/3 | acima da baseline do staging |

Os thresholds finais de taxa/janela devem ser calibrados no staging; os defaults de aging servem como baseline operacional inicial, não como SLA contratual.

## Resposta operacional

1. confirmar se o provider sandbox está disponível;
2. verificar `provider_query` e `provider_unavailable.total`;
3. localizar os IDs técnicos em auditoria sem expor dados sensíveis;
4. comparar status interno com consulta externa;
5. se externo estiver `SETTLED`, deixar o caminho automático de reconciliação chamar o serviço único de settlement;
6. se externo estiver `PROCESSING`/`UNKNOWN`, manter `RECONCILIATION_REQUIRED` e investigar;
7. nunca alterar manualmente para `PAID` nem lançar débito no ledger.

## Regras de logging

- logs estruturados e timestamps UTC;
- propagar trace/correlation id;
- registrar IDs técnicos necessários à investigação;
- nunca registrar senha, cookie de sessão, segredo HMAC, chave privada, CPF completo ou documento enviado pelo membro;
- preferir hashes/identificadores opacos para correlação.

## Dashboards sugeridos

1. API: throughput, erro e latência.
2. Financeiro sandbox: PROCESSING/SETTLED/FAILED/RECONCILIATION_REQUIRED + aging.
3. Reconciliação externa: scanned, settled, pending, divergence, provider unavailable e latência.
4. Webhooks: aceitos, inválidos, expirados e replay.
5. Infra: PostgreSQL, Redis, JVM e pool de conexões.
6. Segurança: MFA, sessões privilegiadas, recuperação administrativa e integridade.
