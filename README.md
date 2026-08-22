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

Não existem:

- juros, ROI ou rendimento;
- planos de investimento;
- comissão por indicação;
- recompensa por recrutamento;
- fila de retorno;
- promessa de restituição de contribuição;
- prioridade baseada no quanto alguém doou.

## Estado atual — Fase 7

A versão atual está em **validation, financial hardening & pentest readiness** e permanece sem dinheiro real.

O fluxo sandbox completo já é validado automaticamente de ponta a ponta:

```text
MEMBER
  → contribuição sandbox
  → pedido de auxílio + documento
ANALYST
  → parecer + antifraude
APPROVER #1
  → primeira aprovação
APPROVER #2
  → segunda aprovação independente
ADMIN
  → inicia tentativa de pagamento
SANDBOX PROVIDER
  → webhook HMAC SETTLED
SISTEMA
  → PAID + AID_PAYMENT no ledger + auditoria
```

O painel operacional permite que `ADMIN` inicie uma tentativa de pagamento somente para pedidos `APPROVED`. `ADMIN` e `AUDITOR` podem acompanhar o histórico e os estados da tentativa. A interface **não possui ação para forçar um pedido para `PAID`**: a liquidação depende da confirmação autenticada do provedor sandbox.

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
- scripts de backup, restore e verificação de integridade;
- SBOM CycloneDX, OWASP Dependency-Check, secret scan e CodeQL.

### Gate financeiro validado em sandbox

O E2E de governança financeira verifica automaticamente que:

1. o pedido só chega a `APPROVED` após análise, antifraude e duas aprovações distintas;
2. o admin consegue apenas iniciar a tentativa;
3. a tentativa entra em `PROCESSING`;
4. o webhook de settlement precisa de assinatura HMAC válida;
5. o settlement leva o auxílio para `PAID`;
6. existe exatamente um lançamento `AID_PAYMENT` correspondente no ledger;
7. `PAYMENT_INITIATED` e `PAYMENT_SETTLED` ficam registrados na auditoria.

### Próxima etapa

A etapa em desenvolvimento é o **hardening dos caminhos de falha do pagamento**, cobrindo principalmente:

- replay de webhook;
- assinatura HMAC inválida;
- webhook fora da janela temporal;
- pagamento `FAILED`;
- `RECONCILIATION_REQUIRED`;
- repetição de iniciação com a mesma chave de idempotência;
- prevenção de dupla liquidação/débito duplicado;
- saldo insuficiente;
- recuperação e reconciliação administrativa auditável.

Nenhum desses testes utiliza dinheiro real.

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

- `docs/THREAT_MODEL.md`
- `docs/SECURITY_CHECKS.md`
- `docs/PENTEST_READINESS_CHECKLIST.md`
- `docs/PHASE_7_VALIDATION.md`

## Importante

A integração financeira desta fase é **SANDBOX**. Não há PIX real, custódia de valores ou promessa de retorno financeiro.

O objetivo técnico atual é provar segurança, governança, consistência contábil e comportamento previsível diante de falhas antes de considerar qualquer integração financeira real.

## Licença

A definir antes da primeira release pública estável.
