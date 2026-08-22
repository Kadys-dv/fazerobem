# Fase 8 — Operational Status

## Objetivo

Registrar o estado operacional comprovado da Fase 8 antes do painel operacional e do gate da Fase 9.

## Baseline

A aplicação permanece **100% sandbox**. Nenhum gate descrito aqui autoriza dinheiro real, custódia de recursos, PIX real ou promessa de retorno financeiro.

## Entregas consolidadas

- PR #15 — observabilidade e alertas operacionais;
- PR #16 — chaos engineering, disaster recovery e restore verificado;
- PR #17 — staging hardening fail-closed;
- PR #23 — simulação de piloto sandbox;
- PR #24 — onboarding/consentimentos e histórico privado de contribuições;
- PR #25 — carga e concorrência.

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
- `SECURITY_CHECKS.md` — verificações de segurança.

## O que ainda não está autorizado

Mesmo com os gates verdes, o sistema **não está autorizado para dinheiro real**. Antes disso ainda são necessários, no mínimo:

- pentest independente;
- validação jurídica, contábil e regulatória;
- KMS/secret manager de produção;
- TLS, domínio e WebAuthn reais;
- provedor financeiro autorizado;
- processos externos de reconciliação e monitoramento;
- política LGPD formalmente validada;
- operação humana comprovada pelo painel operacional.

## Próximo gate

A sequência de desenvolvimento permanece:

1. concluir documentação da Fase 8 (#20);
2. implementar painel operacional utilizável (#21);
3. somente então avaliar o gate da Fase 9 (#22).

Não iniciar Production Readiness antes dessa sequência.
