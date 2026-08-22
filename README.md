# Fazer o Bem

Plataforma de **ajuda mútua e fundo comunitário transparente**, construída para organizar contribuições voluntárias e concessões de auxílio com critérios objetivos, governança, auditoria e segurança.

> Este projeto **não é investimento, HYIP, pirâmide, Ponzi ou produto de rendimento**. Contribuições não geram promessa de retorno, saldo resgatável, juros, prioridade financeira ou recompensa por recrutamento.

## Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Redis
- WebAuthn / Passkeys
- TOTP
- AWS KMS opcional
- OpenTelemetry / Prometheus
- Testcontainers
- Playwright

## Release baseline

A baseline pública continua **`v0.1.0-alpha`** e permanece **100% sandbox/staging**. Nenhum recurso real é recebido, custodiado ou liquidado pelo projeto nesta fase.

Consulte:

- [`CHANGELOG.md`](CHANGELOG.md)
- [`docs/RELEASE_v0.1.0-alpha.md`](docs/RELEASE_v0.1.0-alpha.md)
- [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md)

## Arquitetura

A visão completa de componentes, papéis, fluxo de auxílio, segurança, pagamentos, ledger, auditoria e CI está em **[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)**.

## Fluxo principal

```text
contribuição voluntária
        ↓
fundo comunitário
        ↓
pedido de auxílio + documentos
        ↓
elegibilidade objetiva
        ↓
análise + antifraude
        ↓
dupla aprovação independente
        ↓
admin inicia pagamento sandbox
        ↓
provedor confirma externamente
        ↓
PAID
        ↓
ledger + auditoria + transparência
```

Não existem juros, ROI, planos de investimento, comissão por indicação, recompensa por recrutamento, fila de retorno, promessa de restituição de contribuição ou prioridade baseada no quanto alguém doou.

## Estado atual — Fase 9 / External Evidence Gate

A Fase 8 foi concluída e o gate técnico de entrada na **Fase 9 — Production Readiness** foi aprovado. Desde então o projeto avançou em quatro blocos internos:

1. **Production security foundation** — configuração de produção fail-closed, secrets/KMS, TLS/cookies seguros e WebAuthn por ambiente.
2. **Sandbox provider homologation** — contrato de provedor, idempotência, timeout/retry, webhook assinado e reconciliação externa em sandbox.
3. **Operational reconciliation** — aging, métricas, alertas e varredura segura de pagamentos incertos.
4. **Staging go-live readiness** — kill switch de pagamentos, backup/restore fail-closed, health externo, runbook de deploy/rollback e rehearsal reproduzível em CI.

O projeto está agora na **issue #38 — External Evidence Gate before real-money production**.

### Decisão atual

**GO:** continuar Production Readiness em sandbox/staging.

**NO-GO:** dinheiro real, PIX real, custódia, contribuição real ou liquidação financeira de produção.

O NO-GO permanece até existirem evidências externas verificáveis para as quatro trilhas abaixo:

- rehearsal executado no staging alvo real;
- pentest independente com tratamento/reteste dos achados relevantes;
- validação jurídica, contábil, regulatória e LGPD;
- homologação contratual/compliance do provedor financeiro.

## Controles implementados

- ledger financeiro encadeado por SHA-256;
- trilha de auditoria encadeada;
- dupla aprovação por usuários distintos;
- separação entre ANALYST, APPROVER, ADMIN e AUDITOR;
- onboarding, elegibilidade, documentos e consentimentos versionados;
- proteção de PII com AES-256-GCM;
- suporte a envelope encryption com AWS KMS;
- WebAuthn/passkeys e TOTP para perfis privilegiados;
- Redis para controles distribuídos;
- idempotência de pagamentos;
- webhook HMAC com proteção contra replay e janela temporal;
- estados `PROCESSING`, `SETTLED`, `FAILED` e `RECONCILIATION_REQUIRED`;
- reconciliação externa sem ação administrativa capaz de forçar `PAID`;
- kill switch de novas iniciações de pagamento;
- relatórios públicos assinados com Ed25519;
- recuperação administrativa com dual control;
- Testcontainers PostgreSQL + Redis;
- E2E Playwright com WebAuthn virtual;
- testes negativos de webhook, replay, idempotência, saldo e débito duplicado;
- carga/concorrência com métricas p95/p99;
- chaos engineering e disaster recovery;
- scripts de backup/restore com checksum e confirmação explícita;
- SBOM CycloneDX, OWASP Dependency-Check, secret scan e CodeQL.

## Gates de CI atuais

A baseline de qualidade inclui:

- `frontend-syntax`;
- `staging-config`;
- `security`;
- `chaos-dr`;
- `pilot-simulation`;
- `load-concurrency`;
- `production-readiness`;
- `provider-homologation`;
- `operational-reconciliation`;
- `go-live-readiness`;
- `staging-rehearsal-package`.

Esses gates comprovam o comportamento interno e reproduzível do software, mas **não substituem as evidências externas da issue #38**.

## Executar localmente

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Aplicação: `http://localhost:8080`

> O profile `dev` é apenas para desenvolvimento. Nunca reutilize credenciais, chaves ou defaults de desenvolvimento em staging/produção.

## Segurança e produção

Antes de qualquer discussão de ativação com recursos reais, permanecem obrigatórios:

- pentest independente;
- revisão jurídica, contábil, regulatória e LGPD;
- KMS/secret manager real e processo de rotação;
- TLS/domínio/WebAuthn reais;
- provedor financeiro autorizado e contrato homologado;
- monitoramento/reconciliação externos;
- backup/restore exercitado no staging alvo real;
- runbook de go-live/rollback validado com evidências.

Consulte:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/THREAT_MODEL.md`](docs/THREAT_MODEL.md)
- [`docs/SECURITY_CHECKS.md`](docs/SECURITY_CHECKS.md)
- [`docs/PENTEST_READINESS_CHECKLIST.md`](docs/PENTEST_READINESS_CHECKLIST.md)
- [`docs/INCIDENT_RUNBOOK.md`](docs/INCIDENT_RUNBOOK.md)
- [`docs/CHAOS_DR_PLAN.md`](docs/CHAOS_DR_PLAN.md)
- [`docs/OBSERVABILITY_ALERTS.md`](docs/OBSERVABILITY_ALERTS.md)
- [`docs/LGPD.md`](docs/LGPD.md)
- [`docs/PHASE_8_OPERATIONAL_STATUS.md`](docs/PHASE_8_OPERATIONAL_STATUS.md)
- [`docs/PHASE_9_READINESS_DECISION.md`](docs/PHASE_9_READINESS_DECISION.md)
- [`docs/STAGING_REHEARSAL_CHECKLIST.md`](docs/STAGING_REHEARSAL_CHECKLIST.md)
- [`docs/STAGING_REHEARSAL_EVIDENCE_TEMPLATE.md`](docs/STAGING_REHEARSAL_EVIDENCE_TEMPLATE.md)

## Importante

A integração financeira continua **SANDBOX/STAGING ONLY**. O projeto não recebe nem movimenta dinheiro real nesta fase.

## Licença

A definir antes da primeira release pública estável.
