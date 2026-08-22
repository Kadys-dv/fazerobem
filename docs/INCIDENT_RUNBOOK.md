# Incident Runbook

Runbook operacional para staging/piloto sandbox. Nunca colocar CPF, tokens, senhas, segredos ou documentos integrais em tickets/logs.

## Severidade

- **SEV-1:** risco de efeito financeiro duplicado, corrupção de ledger/auditoria, acesso privilegiado indevido ou vazamento relevante de PII.
- **SEV-2:** pagamentos presos, reconciliação crescente, indisponibilidade parcial ou falha persistente de webhook.
- **SEV-3:** degradação sem risco de integridade.

## Sinais observáveis

O endpoint `/actuator/prometheus` expõe, entre outras, as métricas operacionais:

- `fazerobem_payment_processing_stuck`;
- `fazerobem_payment_reconciliation_required`;
- `fazerobem_payment_failed_current`;
- `fazerobem_outbox_pending`.

As regras de alerta versionadas ficam em `ops/prometheus/alerts.yml`.

## Procedimento geral

1. Preservar evidências e identificar escopo sem apagar logs.
2. Suspender pagamentos sandbox ou integração externa afetada quando houver risco de integridade.
3. Revogar sessões e rotacionar segredos/chaves afetadas quando necessário.
4. Verificar integridade de ledger e auditoria.
5. Isolar contas privilegiadas suspeitas.
6. Identificar dados pessoais potencialmente afetados e acionar responsável jurídico/DPO quando aplicável.
7. Corrigir causa raiz, restaurar de fonte confiável e reconciliar pagamentos.
8. Registrar relatório pós-incidente e ações preventivas.

## Pagamento preso em PROCESSING

1. Não iniciar manualmente um segundo pagamento.
2. Identificar `aidRequestId`, `paymentAttemptId`, `providerReference` e idempotency key.
3. Consultar auditoria e eventos de webhook.
4. Confirmar o estado do provider sandbox.
5. Se o resultado for incerto, usar `RECONCILIATION_REQUIRED` pelo fluxo suportado.
6. Nunca alterar diretamente ledger ou marcar `PAID` no banco.

## RECONCILIATION_REQUIRED

1. Tratar como SEV-2; elevar para SEV-1 se houver efeito externo sem ledger correspondente.
2. Comparar tentativa, provider, aid status, ledger e audit chain.
3. Registrar evidências antes de qualquer ação administrativa.
4. Usar somente reconciliação auditável.

## Pagamento FAILED

1. Confirmar se houve efeito externo no provider antes de qualquer retry.
2. Se não houve efeito externo, manter o registro FAILED como evidência e abrir nova tentativa apenas pelo fluxo normal.
3. Se houver incerteza, não repetir pagamento: mover a investigação para reconciliação.
4. Verificar audit trail e idempotency key associada.

## Outbox acumulando

1. Verificar disponibilidade do consumidor/destino e erros de publicação.
2. Não apagar eventos pendentes para reduzir a fila artificialmente.
3. Confirmar que a ordem e a idempotência dos consumidores são preservadas antes de retomar.
4. Após recuperação, acompanhar a redução de `fazerobem_outbox_pending` e procurar duplicidades.

## Webhook inválido ou replay

1. Não reprocessar manualmente o payload rejeitado.
2. Preservar `eventId`, timestamp, hash do corpo e motivo da rejeição sem registrar secrets/PII.
3. Em volume anormal, aplicar contenção na borda e avaliar rotação do segredo.
4. Confirmar que nenhuma mutação de pagamento/ledger ocorreu.

## Divergência de ledger/auditoria

1. Declarar SEV-1 e congelar operações sandbox de pagamento.
2. Executar verificadores de integridade sem modificar dados.
3. Preservar snapshot/backup e logs.
4. Não reparar linhas manualmente.
5. Restaurar em ambiente isolado para investigação.

## PostgreSQL/Redis indisponível

1. Suspender ações administrativas que possam produzir efeitos externos.
2. Verificar health checks e conectividade.
3. Após recuperação, validar outbox, tentativas `PROCESSING`, reconciliações e integridade das cadeias.
4. Só retomar o fluxo após as verificações.

## Evidências mínimas

- horário UTC;
- correlation/trace id quando disponível;
- usuário/papel envolvido;
- aid/payment/event ids;
- estado antes/depois;
- hashes relevantes sem PII/secrets;
- decisão operacional e aprovadores.
