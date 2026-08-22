# Fase 8 — Simulação de piloto sandbox

Esta etapa valida o comportamento operacional do Fazer o Bem com volume de usuários fictícios antes de testes de carga dedicados e antes de qualquer discussão de Production Readiness.

## Escopo executável

O workflow `pilot-simulation` executa uma jornada reproduzível em ambiente isolado com PostgreSQL e Redis efêmeros:

- 50 membros fictícios registrados com sessões independentes;
- 50 contribuições sandbox;
- 20 pedidos de auxílio de saúde;
- 20 documentos comprobatórios enviados;
- criação dos membros em lotes com concorrência configurável de 10;
- 3 pedidos processados integralmente por ANALYST, dois APPROVER distintos e ADMIN;
- liquidação apenas por webhook sandbox assinado com HMAC;
- retry idempotente de iniciação de pagamento;
- replay intencional de webhook, que deve ser rejeitado;
- conferência final de ledger, auditoria, pagamentos e transparência.

Os valores são configuráveis pelas variáveis `PILOT_MEMBER_COUNT`, `PILOT_AID_COUNT`, `PILOT_PAID_SAMPLE_COUNT` e `PILOT_CONCURRENCY`.

## Invariantes obrigatórias

A execução falha se qualquer uma destas condições for violada:

1. um pagamento liquidado possuir menos ou mais de duas aprovações esperadas na amostra validada;
2. os dois registros de aprovação não forem preservados na auditoria;
3. existir mais de um `AID_PAYMENT` para o mesmo auxílio liquidado;
4. um retry com a mesma `Idempotency-Key` produzir outra tentativa/provider reference;
5. replay do mesmo `eventId` de webhook for aceito novamente;
6. existir mais de uma tentativa financeira ativa por auxílio no banco;
7. existir `PaymentAttempt` `SETTLED` cujo auxílio não esteja `PAID`.

## Limites desta etapa

Esta simulação não é benchmark e não define capacidade de produção. Testes de carga, p95/p99 e limites de throughput pertencem à etapa posterior da Fase 8.

Nenhum dinheiro real, PIX, cartão ou custódia é habilitado. Todo o fluxo financeiro continua 100% sandbox.
