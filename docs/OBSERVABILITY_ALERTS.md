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
- falha do verificador de integridade do ledger/auditoria.

## Alertas mínimos

| Alerta | Severidade | Condição inicial |
|---|---|---|
| Ledger/audit integrity failure | SEV-1 | qualquer ocorrência |
| Duplicate financial effect detected | SEV-1 | qualquer ocorrência |
| Reconciliation required | SEV-2 | qualquer ocorrência; elevar se persistente |
| Payment PROCESSING stale | SEV-2 | acima do SLA definido para staging |
| Webhook auth/replay spike | SEV-2 | crescimento anormal em janela curta |
| PostgreSQL unavailable | SEV-2 | health down persistente |
| Redis unavailable | SEV-2 | health down persistente |
| HTTP 5xx spike | SEV-2/3 | acima da baseline do staging |

Os thresholds finais devem ser calibrados com a simulação do piloto; não devem ser inventados antes de existir uma baseline de tráfego.

## Regras de logging

- logs estruturados e timestamps UTC;
- propagar trace/correlation id;
- registrar IDs técnicos necessários à investigação;
- nunca registrar senha, cookie de sessão, segredo HMAC, chave privada, CPF completo ou documento enviado pelo membro;
- preferir hashes/identificadores opacos para correlação.

## Dashboards sugeridos

1. API: throughput, erro e latência.
2. Financeiro sandbox: PROCESSING/SETTLED/FAILED/RECONCILIATION_REQUIRED.
3. Webhooks: aceitos, inválidos, expirados e replay.
4. Infra: PostgreSQL, Redis, JVM e pool de conexões.
5. Segurança: MFA, sessões privilegiadas, recuperação administrativa e integridade.
