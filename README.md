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

## Estado atual — Fase 7

A versão atual está em **validation, financial hardening & pentest readiness** e permanece sem dinheiro real.

O fluxo sandbox completo já é validado de ponta a ponta:

- `MEMBER`: contribuição sandbox, pedido e documento;
- `ANALYST`: parecer e antifraude;
- dois `APPROVER` distintos: dupla aprovação;
- `ADMIN`: inicia a tentativa de pagamento;
- provedor sandbox: confirma por webhook HMAC;
- sistema: `PAID`, lançamento `AID_PAYMENT` no ledger e trilha de auditoria.

A interface administrativa não possui ação para forçar `PAID`: a liquidação depende da confirmação autenticada do provedor sandbox.

### Controles implementados

- ledger financeiro encadeado por SHA-256;
- trilha de auditoria encadeada;
- dupla aprovação por usuários distintos;
- separação entre ANALYST, APPROVER, ADMIN e AUDITOR;
- KYC e consentimentos versionados;
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

- `docs/ARCHITECTURE.md`
- `docs/THREAT_MODEL.md`
- `docs/SECURITY_CHECKS.md`
- `docs/PENTEST_READINESS_CHECKLIST.md`
- `docs/PHASE_7_VALIDATION.md`

## Importante

A integração financeira desta fase é **SANDBOX**. Não há PIX real, custódia de valores ou promessa de retorno financeiro.

## Licença

A definir antes da primeira release pública estável.
