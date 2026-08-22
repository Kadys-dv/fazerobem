# Staging Rehearsal Checklist

Este checklist prepara a execução real do rehearsal da Fase 9 no ambiente alvo de staging. Ele **não autoriza dinheiro real** e não substitui pentest, validação jurídica/LGPD ou homologação contratual do provedor financeiro.

## 1. Pré-condições

- [ ] ambiente é staging e está identificado explicitamente;
- [ ] `PAYMENT_INITIATION_ENABLED=false` confirmado antes do início;
- [ ] responsáveis técnicos e aprovador do ensaio identificados;
- [ ] janela de mudança registrada;
- [ ] commit/release candidato registrado;
- [ ] `STAGING_BASE_URL` HTTPS conhecido;
- [ ] DSN de staging carregado por secret manager, sem ser copiado para ticket/log;
- [ ] backup destination disponível e protegido;
- [ ] observabilidade externa acessível;
- [ ] nenhuma credencial real de produção está presente no ambiente.

## 2. Freeze operacional

- [ ] confirmar `PAYMENT_INITIATION_ENABLED=false`;
- [ ] confirmar que nenhuma nova tentativa de pagamento sandbox pode ser criada;
- [ ] registrar quantidade de `PROCESSING`, `RECONCILIATION_REQUIRED` e outbox pendente;
- [ ] registrar timestamp UTC do freeze;
- [ ] preservar tentativas existentes sem alterar status manualmente.

## 3. Snapshot e backup

- [ ] executar `scripts/staging-rehearsal.sh preflight`;
- [ ] executar `scripts/staging-rehearsal.sh backup`;
- [ ] registrar caminho/ID do backup;
- [ ] registrar SHA-256;
- [ ] validar legibilidade do backup sem restaurar sobre o banco ativo;
- [ ] registrar contagens de ledger, audit events, payment attempts e outbox antes do deploy.

## 4. Deploy controlado

- [ ] deploy do commit/release candidato executado pelo mecanismo oficial do ambiente;
- [ ] nenhuma alteração manual no banco;
- [ ] migrations concluídas sem erro;
- [ ] processo/aplicação saudável;
- [ ] timestamp UTC do deploy registrado.

## 5. Validação pós-deploy

- [ ] executar `scripts/staging-rehearsal.sh health`;
- [ ] health externo retorna `UP` via HTTPS;
- [ ] observabilidade externa confirma disponibilidade;
- [ ] ledger íntegro;
- [ ] audit chain íntegra;
- [ ] payment attempts explicáveis;
- [ ] outbox sem perda de eventos;
- [ ] reconciliação externa sem divergência crítica;
- [ ] nenhuma nova iniciação ocorre enquanto freeze estiver ativo.

## 6. Rollback/restore rehearsal

O rehearsal deve provar rollback de aplicação e restore em alvo controlado/isolado. Não sobrescrever o banco ativo de staging sem aprovação explícita prevista na janela.

- [ ] rollback da aplicação executado para release anterior;
- [ ] health externo validado novamente;
- [ ] restore executado em banco alvo isolado ou segundo ambiente de validação;
- [ ] checksum do backup validado;
- [ ] ledger, audit events, payment attempts e outbox comparados com o snapshot pré-deploy;
- [ ] divergências registradas e explicadas;
- [ ] nenhum registro corrigido manualmente para fazer o teste passar.

## 7. Critérios para unfreeze

Todos devem ser verdadeiros:

- [ ] health externo estável;
- [ ] ledger e auditoria íntegros;
- [ ] não há divergência financeira não explicada;
- [ ] reconciliação pendente está dentro do limite operacional aceito;
- [ ] outbox está processando normalmente;
- [ ] rollback/restore foi considerado aprovado;
- [ ] responsável técnico e aprovador registraram aceite.

Mesmo após o rehearsal, manter `PAYMENT_INITIATION_ENABLED=false` enquanto a issue #38 estiver aberta.

## 8. Evidências obrigatórias

Preencher `docs/STAGING_REHEARSAL_EVIDENCE_TEMPLATE.md` e anexar/referenciar:

- timestamps UTC;
- commit/release;
- health checks;
- hashes/checksums;
- contagens pré e pós;
- resultado do rollback/restore;
- links de observabilidade sem secrets;
- incidentes/desvios encontrados;
- nomes/papéis dos responsáveis;
- decisão final `PASS` ou `FAIL` do rehearsal.
