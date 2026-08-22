# Fazer o Bem v0.1.0-alpha

Primeira baseline versionada de demonstração da plataforma Fazer o Bem.

## Objetivo desta alpha

Congelar um ponto técnico reproduzível do projeto antes de continuar o hardening de concorrência, observabilidade e preparação para pentest.

Esta versão demonstra, exclusivamente em sandbox, o ciclo completo de ajuda comunitária:

`contribuição → pedido → documentos → análise → antifraude → dupla aprovação → pagamento sandbox → webhook autenticado → PAID → ledger → auditoria`.

## O que está validado

- separação de papéis MEMBER / ANALYST / APPROVER / ADMIN / AUDITOR;
- duas aprovações de usuários distintos;
- bloqueio de voto duplicado;
- idempotência de iniciação de pagamento;
- webhook HMAC e proteção contra replay;
- caminhos SETTLED, FAILED e RECONCILIATION_REQUIRED;
- ledger e auditoria encadeados;
- fluxo E2E live até PAID;
- testes de WebAuthn com autenticador virtual;
- PostgreSQL e Redis via Testcontainers/CI;
- SBOM, secret scan e CodeQL.

## Não é produção

`v0.1.0-alpha` não autoriza movimentação de recursos reais. Esta baseline não substitui pentest, revisão jurídica, contábil, LGPD, homologação de provedor financeiro, KMS/secret manager real, TLS de produção, backup/restore validado e plano de resposta a incidentes.

## Verificação recomendada antes de criar a tag

```bash
mvn clean verify
npm ci
npx playwright test
```

Além disso, todos os workflows obrigatórios do GitHub Actions devem estar verdes no commit que receberá a tag.

## Artefatos de referência

- `README.md`
- `CHANGELOG.md`
- `docs/ARCHITECTURE.md`
- `docs/THREAT_MODEL.md`
- `docs/SECURITY_CHECKS.md`
- `docs/PENTEST_READINESS_CHECKLIST.md`
- `docs/PHASE_7_VALIDATION.md`
