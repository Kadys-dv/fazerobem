# Fase 8 — Pilot Readiness

A Fase 8 prepara o Fazer o Bem para um piloto controlado **sem dinheiro real**.

## Objetivos

1. Observabilidade e resposta a incidentes.
2. Chaos testing e disaster recovery verificável.
3. Staging endurecido com HTTPS, secrets externos e isolamento de dados.
4. Simulação de piloto com usuários e operações fictícias.

## Gates

### PR #15 — Observability & incident response
- métricas de pagamentos e webhooks;
- alertas documentados;
- runbook de incidentes;
- health/metrics sem exposição indevida.

### PR #16 — Chaos & disaster recovery
- falha/restart de PostgreSQL e Redis;
- interrupção durante processamento;
- restore em banco limpo;
- verificação de ledger/auditoria após restore;
- nenhuma duplicação de efeito financeiro.

### PR #17 — Staging hardening
- profile de staging separado;
- cookies Secure e TLS obrigatório;
- WebAuthn com RP/origin reais;
- secrets obrigatórios fora do repositório;
- banco/Redis/storage isolados;
- configuração fail-closed para chaves ausentes.

### Issue #18 — Pilot simulation
- 50 membros fictícios por padrão;
- contribuições e pedidos sandbox em sessões independentes;
- múltiplas submissões concorrentes;
- amostra processada integralmente até `PAID`;
- validação de dupla aprovação, idempotência, replay, ledger e auditoria;
- gate reproduzível em CI pelo workflow `pilot-simulation`.

Evidência e critérios executáveis: [`PILOT_SIMULATION.md`](PILOT_SIMULATION.md).

## Regra de segurança

A conclusão desta fase não autoriza PIX real, custódia, cartão ou qualquer movimentação financeira real. Revisão jurídica/regulatória, pentest independente e provedor autorizado continuam sendo gates externos obrigatórios.
