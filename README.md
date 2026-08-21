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

## Princípios do projeto

O fluxo financeiro é deliberadamente simples:

```text
contribuição voluntária
        ↓
fundo comunitário
        ↓
pedido de auxílio
        ↓
elegibilidade objetiva
        ↓
análise antifraude
        ↓
dupla aprovação
        ↓
pagamento sandbox
        ↓
ledger + auditoria + transparência
```

Não existem:

- juros, ROI ou rendimento;
- planos de investimento;
- comissão por indicação;
- recompensa por recrutamento;
- fila de retorno;
- promessa de restituição de contribuição;
- prioridade baseada no quanto alguém doou.

## Estado atual — Fase 7

A versão atual está em **validation & pentest readiness** e permanece sem dinheiro real.

Principais controles já previstos no código:

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
- webhook HMAC com proteção contra replay;
- reconciliação explícita de pagamentos incertos;
- relatórios públicos assinados com Ed25519;
- recuperação administrativa com dual control;
- Testcontainers PostgreSQL + Redis;
- base E2E Playwright com autenticador WebAuthn virtual;
- scripts de backup, restore e verificação de integridade;
- SBOM CycloneDX, OWASP Dependency-Check e CodeQL.

## Executar localmente

### 1. Infraestrutura

```bash
docker compose up -d
```

### 2. Aplicação

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Aplicação:

```text
http://localhost:8080
```

> O profile `dev` é apenas para desenvolvimento. Nunca use suas credenciais/chaves padrão em produção.

## Segurança

Antes de qualquer piloto com recursos reais, o projeto exige no mínimo:

- `mvn clean verify` reproduzível;
- execução completa dos E2E;
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

- `docs/THREAT_MODEL.md`
- `docs/SECURITY_CHECKS.md`
- `docs/PENTEST_READINESS_CHECKLIST.md`
- `docs/PHASE_7_VALIDATION.md`

## Importante

A integração financeira desta fase é **SANDBOX**. Não há PIX real, custódia de valores ou promessa de retorno financeiro.

## Licença

A definir antes da primeira release pública estável.
