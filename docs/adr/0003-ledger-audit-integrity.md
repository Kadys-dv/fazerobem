# ADR-0003 — Integridade de ledger e auditoria

**Status:** Accepted  
**Data:** 2026-08-21

## Contexto

Saldo financeiro e histórico administrativo são evidências críticas. Updates/deletes silenciosos dificultariam detectar fraude, erro ou comprometimento do banco.

## Decisão

Usar estruturas append-only no fluxo normal, encadeadas por hash SHA-256 e serializadas por um lock de cadeia. Cada entrada referencia o hash anterior; a auditoria segue o mesmo princípio.

O ledger registra valores assinados e um `AID_PAYMENT` é criado somente no settlement.

## Consequências

- adulterações históricas ficam detectáveis por verificação da cadeia;
- escrita é deliberadamente serializada, favorecendo consistência sobre throughput;
- hash chaining não substitui controle de acesso, backup, assinatura externa ou imutabilidade de infraestrutura.

## Revisão

Antes de escala elevada, medir contenção do lock. Qualquer evolução para particionamento ou event store deve preservar verificabilidade e exactly-once financial effect.
