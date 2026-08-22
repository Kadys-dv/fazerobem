# ADR 0008 — Redis não é fonte de verdade financeira

**Status:** Accepted

## Contexto

Redis é útil para rate limit, replay protection, coordenação distribuída e controles transitórios. Esses dados possuem perfil operacional diferente de ledger, auditoria e settlement.

## Decisão

Redis nunca será autoridade para saldo, liquidação, estado final de pagamento ou concessão. A indisponibilidade ou perda de Redis não pode produzir perda financeira nem permitir criação de `PAID`.

## Consequências

- falhas de Redis devem degradar de forma segura ou fail-closed conforme o controle;
- dados financeiros autoritativos permanecem no PostgreSQL;
- TTL e evicção de Redis não podem alterar a verdade financeira;
- testes de chaos devem cobrir indisponibilidade/restart de Redis.

## Condições para revisão

Somente mediante redesign explícito com garantias persistentes e auditoráveis equivalentes ao banco transacional.

## Evidências relacionadas

`chaos-dr`, `load-concurrency`, `security` e testes com Redis/Testcontainers.