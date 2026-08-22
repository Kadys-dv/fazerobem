# Fazer o Bem

Plataforma de **ajuda mútua e fundo comunitário transparente**, construída para organizar contribuições voluntárias e concessões de auxílio com critérios objetivos, governança, auditoria e segurança.

> Este projeto **não é investimento, HYIP, pirâmide, Ponzi ou produto de rendimento**. Contribuições não geram promessa de retorno, saldo resgatável, juros, prioridade financeira ou recompensa por recrutamento.

## Stack

- Java 21
- Spring Boot 4.0.8
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

A baseline atual de demonstração é **`v0.1.0-alpha`**. Ela permanece 100% sandbox e não autoriza movimentação de recursos reais.

Consulte:

- [`CHANGELOG.md`](CHANGELOG.md)
- [`docs/RELEASE_v0.1.0-alpha.md`](docs/RELEASE_v0.1.0-alpha.md)
- [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md)

## Arquitetura

A visão completa de componentes, papéis, fluxo de auxílio, segurança, pagamentos, ledger, auditoria e CI está em **[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)**.

## Princípios do projeto

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
webhook HMAC confirma liquidação
        ↓
PAID
        ↓
ledger + auditoria + transparência
```

Não existem juros, ROI, planos de investimento, comissão por indicação, recompensa por recrutamento, fila de retorno, promessa de restituição de contribuição ou prioridade baseada no quanto alguém doou.

## Estado atual — Fase 8

A versão atual está em **pilot readiness, operational hardening e validation** e permanece **100% sandbox, sem dinheiro real**.

A Fase 8 consolidou os gates operacionais necessários antes de qualquer avanço para Production Readiness:

- **PR #15**: observabilidade, alertas e base operacional;
- **PR #16**: chaos engineering, disaster recovery e validação de backup/restore;
- **PR #17**: staging hardening fail-closed;
- **PR #23**: simulação completa de piloto sandbox com 50 membros, 20 pedidos e 3 liquidações;
- **PR #24**: onboarding obrigatório, consentimentos versionados e histórico privado de contribuições;
- **PR #25**: gate de carga e concorrência para ledger, auditoria, Redis e corrida de pagamentos.

O fluxo sandbox completo é validado de ponta a ponta:

- `MEMBER`: onboarding, consentimentos, contribuição sandbox, pedido e documento;
- `ANALYST`: parecer e antifraude;
- dois `APPROVER` distintos: dupla aprovação;
- `ADMIN`: inicia a tentativa de pagamento;
- provedor sandbox: confirma por webhook HMAC;
- sistema: `PAID`, lançamento `AID_PAYMENT` no ledger e trilha de auditoria.

A interface administrativa não possui ação para forçar `PAID`: a liquidação depende da confirmação autenticada do provedor sandbox.

### Gates validados na Fase 8

- `frontend-syntax`;
- `staging-config`;
- `security`;
- `chaos-dr`;
- `pilot-simulation`;
- `load-concurrency`.

A simulação de piloto valida dezenas de membros e pedidos concorrentes sem quebrar as invariantes financeiras. O gate de carga/concorrência verifica serialização do ledger e da auditoria, contenção no Redis, exatamente uma tentativa financeira ativa por auxílio e gera métricas de throughput, p95 e p99.

### Controles implementados

- ledger financeiro encadeado por SHA-256;
- trilha de auditoria encadeada;
- dupla aprovação por usuários distintos;
- separação entre ANALYST, APPROVER, ADMIN e AUDITOR;
- KYC e consentimentos versionados;
- onboarding obrigatório antes de operações sensíveis do membro;
- proteção de PII com AES-256-GCM;
- suporte a envelope encryption com AWS KMS;
- WebAuthn/passkeys e TOTP para perfis privilegiados;
- Redis para controles distribuídos;
- idempotência de pagamentos;
- webhook HMAC com proteção contra replay e janela temporal;
- estados de pagamento `PROCESSING`, `SETTLED`, `FAILED` e `RECONCILIATION_REQUIRED`;
- reconciliação explícita de pagamentos incertos;
- relatórios públicos assinados com Ed25519;
- recuperação administrativa com dual control;
- Testcontainers PostgreSQL + Redis;
- E2E Playwright com autenticador WebAuthn virtual;
- E2E live do fluxo completo até `PAID`, ledger e auditoria;
- testes negativos de webhook, replay, idempotência, saldo e débito duplicado;
- testes de carga e concorrência com relatório p95/p99;
- scripts de backup, restore e verificação de integridade;
- SBOM CycloneDX, OWASP Dependency-Check, secret scan e CodeQL.

## Executar localmente

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Aplicação: `http://localhost:8080`

> O profile `dev` é apenas para desenvolvimento. Nunca use credenciais ou chaves de desenvolvimento em produção.

## Segurança

Antes de qualquer piloto com recursos reais, o projeto exige no mínimo:

- `mvn clean verify` reproduzível;
- execução completa dos E2E positivos e negativos;
- pentest independente;
- restauração de backup testada;
- KMS/secret manager real;
- TLS e cookies seguros;
- WebAuthn em domínio HTTPS real;
- revisão jurídica, contábil e regulatória;
- política LGPD formal;
- provedor de pagamento autorizado;
- reconciliação e monitoramento externos.

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

## Importante

A integração financeira desta fase é **SANDBOX**. Não há PIX real, custódia de valores ou promessa de retorno financeiro.

A **Fase 9 permanece bloqueada** até a conclusão do painel operacional (#21) e do gate formal de Production Readiness (#22).

## Licença

A definir antes da primeira release pública estável.
