# Fase 8 — Operational Status

## Objetivo

Registrar o estado operacional comprovado da Fase 8 e a evidência usada pelo gate de entrada na Fase 9.

## Baseline

A aplicação permanece **100% sandbox**. Nenhum gate descrito aqui autoriza dinheiro real, custódia de recursos, PIX real ou promessa de retorno financeiro.

## Entregas consolidadas

- PR #15 — observabilidade e alertas operacionais;
- PR #16 — chaos engineering, disaster recovery e restore verificado;
- PR #17 — staging hardening fail-closed;
- PR #23 — simulação de piloto sandbox;
- PR #24 — onboarding/consentimentos e histórico privado de contribuições;
- PR #25 — carga e concorrência;
- PR #26 — sincronização documental da Fase 8;
- PR #27 — evolução do painel operacional;
- PR #28 — safeguards, reconciliação segura e E2E de segregação de funções.

## Simulação de piloto

O gate `pilot-simulation` exercita o fluxo com:

- 50 membros fictícios;
- contribuições sandbox;
- 20 pedidos de auxílio com documentos;
- operações concorrentes em lotes;
- 3 pedidos processados integralmente até `PAID`;
- análise, antifraude, dupla aprovação e separação de funções;
- retry com a mesma `Idempotency-Key`;
- tentativa de replay do webhook;
- validação direta das invariantes no PostgreSQL.

O objetivo do gate é demonstrar que o fluxo funcional preserva as invariantes financeiras e de governança sob um piloto controlado.

## Carga e concorrência

O gate `load-concurrency` usa PostgreSQL, Redis e Testcontainers para validar:

- 80 appends concorrentes no ledger;
- 80 appends concorrentes na auditoria;
- 600 operações concorrentes no Redis;
- 20 racers tentando criar uma tentativa financeira ativa para o mesmo auxílio;
- ausência de forks e links órfãos nas cadeias;
- exatamente uma tentativa financeira ativa por auxílio;
- throughput, p95, p99 e taxa de erros por cenário.

O relatório de cada execução é publicado como artifact do GitHub Actions.

## Painel operacional

A operação humana da Fase 8 cobre ANALYST, APPROVER, ADMIN e AUDITOR pela interface, preservando segregação de funções. O painel oferece fila, filtros, detalhe, documentos, antifraude, dupla aprovação, pagamentos sandbox, trilha de auditoria e tratamento seguro de reconciliação.

Nenhuma ação administrativa força `PAID`. Uma nota em `RECONCILIATION_REQUIRED` é apenas evidência operacional auditada; a liquidação permanece dependente da confirmação externa autenticada.

## Gates obrigatórios atuais

A baseline da Fase 8 considera os seguintes workflows como evidência operacional:

- `frontend-syntax`;
- `staging-config`;
- `security`;
- `chaos-dr`;
- `pilot-simulation`;
- `load-concurrency`.

## Documentos operacionais relacionados

- `ARCHITECTURE.md` — arquitetura e fronteiras do sistema;
- `OBSERVABILITY_ALERTS.md` — sinais, métricas e alertas;
- `INCIDENT_RUNBOOK.md` — resposta a incidentes;
- `CHAOS_DR_PLAN.md` — falhas controladas e disaster recovery;
- `LGPD.md` — baseline de privacidade e pontos pendentes de validação jurídica;
- `PENTEST_READINESS_CHECKLIST.md` — preparação para avaliação independente;
- `THREAT_MODEL.md` — ameaças e controles;
- `SECURITY_CHECKS.md` — verificações de segurança;
- `PHASE_9_READINESS_DECISION.md` — decisão formal do gate de entrada na Fase 9.

## O que ainda não está autorizado

Mesmo com os gates verdes, o sistema **não está autorizado para dinheiro real**. Antes disso ainda são necessários, no mínimo:

- pentest independente;
- validação jurídica, contábil e regulatória;
- KMS/secret manager de produção;
- TLS, domínio e WebAuthn reais;
- provedor financeiro autorizado;
- processos externos de reconciliação e monitoramento;
- política LGPD formalmente validada;
- backup/restore e runbook de go-live no ambiente real.

## Resultado da Fase 8

As pré-condições técnicas internas para **iniciar trabalho de Production Readiness** foram concluídas. Isso é um GO para começar a Fase 9 em sandbox/staging, e **não** um GO para dinheiro real.

A decisão, os bloqueadores externos e o escopo autorizado estão registrados em `docs/PHASE_9_READINESS_DECISION.md`.