# ADR 0007 — PostgreSQL como fonte de verdade

**Status:** Accepted

## Contexto

O sistema possui PostgreSQL, Redis, jobs assíncronos e integrações externas. Estados financeiros e de governança não podem depender de um componente volátil ou eventualmente consistente.

## Decisão

PostgreSQL é a fonte de verdade para membros, pedidos, aprovações, payment attempts, ledger, auditoria e reconciliação. Redis pode acelerar controles distribuídos, mas não define estado financeiro final.

## Consequências

- invariantes críticas devem ser protegidas também por transações/restrições de banco;
- recuperação após perda de Redis deve ser possível sem perda de verdade financeira;
- caches podem ser reconstruídos a partir do PostgreSQL;
- qualquer mudança de estado financeiro relevante deve sobreviver a restart do processo.

## Condições para revisão

Revisar apenas se uma nova fonte persistente oferecer garantias de consistência, auditabilidade e recuperação equivalentes ou superiores.

## Evidências relacionadas

`load-concurrency`, `chaos-dr`, `pilot-simulation` e testes Testcontainers com PostgreSQL real.