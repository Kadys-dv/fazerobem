# ADR-0002 — Autoridade do estado de pagamento

**Status:** Accepted  
**Data:** 2026-08-21

## Contexto

Permitir que um operador administrativo marque manualmente um auxílio como pago transforma erro ou abuso de UI em efeito financeiro e enfraquece a evidência de settlement.

## Decisão

O estado `PAID` do auxílio somente pode surgir como consequência de um `PaymentAttempt` liquidado por evento autenticado do provider.

O `ADMIN` inicia a tentativa, mas não possui operação genérica de “marcar como pago”. Estados incertos vão para `RECONCILIATION_REQUIRED`; falhas ficam `FAILED`.

## Consequências

- a fonte de verdade do settlement fica separada da vontade do operador;
- auditoria consegue relacionar `providerReference`, webhook, `PaymentAttempt`, ledger e `AidRequest`;
- indisponibilidade do provider pode atrasar confirmação, mas não justifica bypass silencioso.

## Revisão

Uma integração real pode introduzir reconciliação manual assistida, porém qualquer override deverá exigir evidência externa, dual control e auditoria própria; nunca um simples botão de `PAID`.
