# Staging Go-Live Readiness Runbook

Este runbook é exclusivo para **staging/sandbox**. Ele não autoriza dinheiro real.

## Pré-condições

- todos os gates da `main` verdes;
- `PAYMENT_INITIATION_ENABLED=false` antes do deploy/restore;
- backup recente criado e checksum validado;
- acesso ao banco, Redis, domínio HTTPS e observabilidade externa;
- nenhuma divergência financeira SEV-1 aberta;
- operador e aprovador identificados no registro do ensaio.

## Freeze de pagamentos

Congelar novas iniciações quando ocorrer qualquer um dos seguintes eventos:

- deploy, rollback ou restore;
- integridade de ledger/auditoria inconclusiva;
- provider indisponível de forma persistente;
- divergência externa sem correspondência interna;
- backlog crítico de `RECONCILIATION_REQUIRED`;
- health externo indisponível;
- PostgreSQL/Redis instável.

O freeze usa `PAYMENT_INITIATION_ENABLED=false`. Ele não altera tentativas existentes, ledger ou status financeiro.

## Backup do staging

```bash
DB_URL="$STAGING_DB_URL" BACKUP_DIRECTORY=./artifacts/backups ./scripts/backup.sh
```

Guardar o `.dump` e `.sha256` em armazenamento protegido, com timestamp UTC e referência do deploy.

## Deploy

1. manter pagamentos congelados;
2. registrar SHA atual e SHA alvo;
3. executar deploy pelo mecanismo do ambiente;
4. executar `scripts/external-health-check.sh` a partir de rede externa à aplicação;
5. validar PostgreSQL, Redis, outbox, reconciliação, ledger e auditoria;
6. executar smoke funcional sandbox sem iniciar pagamento enquanto congelado.

## Critérios para rollback

Rollback imediato se houver:

- health externo não `UP` após janela definida;
- migração ou startup inconsistente;
- erro de integridade financeira;
- crescimento anormal de 5xx;
- falha persistente de banco/Redis;
- comportamento diferente entre provider e estado interno.

## Rollback

1. manter `PAYMENT_INITIATION_ENABLED=false`;
2. preservar logs, métricas e snapshots;
3. reimplantar o último SHA aprovado;
4. se dados precisarem ser restaurados, usar apenas backup validado:

```bash
RESTORE_CONFIRMATION=RESTORE_STAGING DB_URL="$STAGING_DB_URL" ./scripts/restore.sh arquivo.dump
```

5. executar health externo;
6. validar ledger/auditoria, payment attempts, outbox e reconciliação;
7. documentar causa e estado final.

## Unfreeze

`PAYMENT_INITIATION_ENABLED=true` só pode ser aplicado quando todos os itens abaixo estiverem aprovados:

- health externo estável;
- PostgreSQL e Redis saudáveis;
- ledger/auditoria íntegros;
- nenhum payment attempt sem explicação;
- reconciliação abaixo do threshold crítico;
- outbox drenando normalmente;
- provider sandbox disponível;
- operador e aprovador registraram GO do ensaio.

## Ensaio obrigatório

O rehearsal deve comprovar, em staging:

1. freeze;
2. backup;
3. deploy controlado;
4. health externo;
5. alteração controlada/reversível;
6. rollback;
7. restore em ambiente isolado ou alvo autorizado;
8. validação das invariantes;
9. unfreeze apenas após aprovação.

## Evidências mínimas

- timestamps UTC;
- SHA anterior e SHA alvo;
- nome/ID do backup e checksum;
- resultado do health externo;
- métricas antes/depois;
- contagens de payment attempts/outbox;
- resultado de integridade de ledger/auditoria;
- decisão de freeze/unfreeze;
- responsáveis pelo ensaio.

A execução real contra o staging alvo é requisito futuro para qualquer decisão de produção. Este runbook e o CI apenas tornam o procedimento reproduzível e verificável; não substituem o ensaio no ambiente real.
