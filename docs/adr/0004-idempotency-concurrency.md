# ADR-0004 — Idempotência e concorrência financeira

**Status:** Accepted  
**Data:** 2026-08-21

## Contexto

Retries de rede, cliques repetidos, múltiplas instâncias e webhooks concorrentes são normais em sistemas distribuídos. Uma checagem `exists` sem coordenação não impede duas transações simultâneas de observarem o mesmo estado e criarem efeitos duplicados.

## Decisão

Aplicar defesa em profundidade:

- `Idempotency-Key` persistida e única;
- lock pessimista no auxílio antes da criação/chamada ao provider;
- rechecagem da chave após adquirir o lock;
- flush das invariantes antes do side effect do provider;
- índice parcial único para no máximo uma tentativa ativa por auxílio;
- `@Version` em `PaymentAttempt`;
- lock pessimista por `providerReference` no webhook;
- `eventId` do webhook persistido e único antes de alterar estado financeiro;
- restrição de um único settlement por auxílio;
- ledger transacional e serializado.

## Consequências

- concorrência para o mesmo auxílio é intencionalmente serializada;
- retries com a mesma chave convergem para o mesmo resultado;
- uma futura alteração que ignore uma camada ainda encontra barreiras no banco;
- locks podem reduzir throughput em hot spots, aceitável para o volume previsto desta fase.

## Revisão

Antes de escalar horizontalmente, executar testes de carga e concorrência em PostgreSQL real e medir lock waits/deadlocks. Não remover restrições de banco apenas por otimização de performance.
