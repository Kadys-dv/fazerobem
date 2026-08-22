# ADR 0010 — Monólito modular antes de microserviços

**Status:** Accepted

## Contexto

O domínio já possui fluxos críticos de governança, pagamento, auditoria, reconciliação e segurança. Dividir cedo em microserviços aumentaria superfície de falha, consistência distribuída e custo operacional sem necessidade comprovada.

## Decisão

Manter um monólito modular com limites claros entre domínios enquanto escala, isolamento operacional ou requisitos organizacionais não justificarem separação física.

## Consequências

- transações críticas permanecem simples e auditáveis;
- deploy, rollback e observabilidade têm menor complexidade;
- módulos devem evitar acoplamento indevido mesmo compartilhando processo;
- futuras extrações exigem contrato explícito, ownership e análise de consistência.

## Condições para revisão

Revisar quando houver evidência de gargalo de escala, necessidade de isolamento de falha, ciclos de deploy independentes ou ownership por equipes distintas.

## Evidências relacionadas

`load-concurrency`, `chaos-dr`, `go-live-readiness` e arquitetura atual documentada.