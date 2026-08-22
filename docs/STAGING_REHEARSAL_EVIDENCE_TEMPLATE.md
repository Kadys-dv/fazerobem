# Staging Rehearsal Evidence

> Template de evidência. Copiar para um registro datado ao executar o rehearsal real. Não incluir senhas, tokens, DSNs completos, PII ou documentos pessoais.

## Identificação

- Data/hora UTC de início:
- Data/hora UTC de fim:
- Ambiente:
- Commit/release candidato:
- Release anterior para rollback:
- Responsável técnico:
- Aprovador:
- Issue/Change ticket:
- Resultado final: `PASS` / `FAIL`

## Freeze

- `PAYMENT_INITIATION_ENABLED=false` confirmado: sim/não
- Timestamp UTC:
- PROCESSING no início:
- RECONCILIATION_REQUIRED no início:
- Outbox pendente no início:
- Evidência/consulta usada:

## Backup

- Identificador/caminho seguro do backup:
- SHA-256:
- Timestamp UTC:
- Ferramenta/versão:
- Validação de leitura/checksum: PASS/FAIL

## Snapshot de integridade pré-deploy

| Evidência | Valor | Resultado |
| --- | ---: | --- |
| ledger entries | | |
| audit events | | |
| payment attempts | | |
| outbox pending | | |
| PROCESSING | | |
| RECONCILIATION_REQUIRED | | |

- Verificação de ledger:
- Verificação de audit chain:
- Observações:

## Deploy

- Mecanismo utilizado:
- Timestamp UTC:
- Commit/release efetivamente implantado:
- Migrations: PASS/FAIL
- Logs relevantes sem secrets:

## Health e observabilidade pós-deploy

- URL base redigida/identificador do serviço:
- HTTPS válido: PASS/FAIL
- `/actuator/health`: PASS/FAIL
- Monitor externo: PASS/FAIL
- Alertas ativos durante o ensaio:
- Evidências/links:

## Reconciliação pós-deploy

- PROCESSING:
- RECONCILIATION_REQUIRED:
- Divergências externas encontradas:
- Provider unavailable/errors:
- Decisão operacional:

## Rollback

- Motivo do rollback no rehearsal: exercício controlado
- Timestamp UTC:
- Release restaurada:
- Health após rollback: PASS/FAIL
- Observações:

## Restore isolado

- Alvo isolado de restore:
- Confirmação explícita utilizada: sim/não
- Checksum validado: PASS/FAIL
- Restore concluído: PASS/FAIL

### Comparação pós-restore

| Evidência | Pré-backup | Pós-restore | Igual/explicado |
| --- | ---: | ---: | --- |
| ledger entries | | | |
| audit events | | | |
| payment attempts | | | |
| outbox | | | |

- Ledger íntegro: PASS/FAIL
- Audit chain íntegra: PASS/FAIL
- Tentativas financeiras explicáveis: PASS/FAIL
- Outbox preservado: PASS/FAIL
- Divergências e justificativas:

## Critérios de unfreeze

- Health externo estável: PASS/FAIL
- Ledger/auditoria íntegros: PASS/FAIL
- Sem divergência financeira inexplicada: PASS/FAIL
- Reconciliação dentro dos limites: PASS/FAIL
- Outbox saudável: PASS/FAIL
- Rollback/restore aprovado: PASS/FAIL

**Importante:** enquanto a issue #38 estiver aberta, o rehearsal aprovado não autoriza ativar pagamentos reais nem alterar a decisão `NO-GO`.

## Achados e ações

| Severidade | Achado | Owner | Prazo | Status |
| --- | --- | --- | --- | --- |
| | | | | |

## Assinatura da decisão

- Responsável técnico — nome/papel/data:
- Aprovador — nome/papel/data:
- Decisão do rehearsal: `PASS` / `FAIL`
- Justificativa:
