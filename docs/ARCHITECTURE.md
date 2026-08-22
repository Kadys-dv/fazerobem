# Arquitetura — Fazer o Bem

Este documento resume a arquitetura atual da plataforma **Fazer o Bem**, com foco em fluxo de ajuda, segurança, governança, pagamentos sandbox, dados e auditabilidade.

> O projeto permanece sem dinheiro real. A camada de pagamento descrita aqui usa apenas provider sandbox.

## 1. Visão geral

```mermaid
flowchart LR
    MEMBER[Membro / PWA]
    OPS[Painel operacional]
    API[Spring Boot API]
    AUTH[Spring Security\nWebAuthn / TOTP]
    DOMAIN[Serviços de domínio]
    PG[(PostgreSQL)]
    REDIS[(Redis)]
    STORE[Document storage]
    PROVIDER[Payment Provider\nSANDBOX]
    OBS[OpenTelemetry / Prometheus]

    MEMBER --> API
    OPS --> API
    API --> AUTH
    AUTH --> DOMAIN
    DOMAIN --> PG
    DOMAIN --> REDIS
    DOMAIN --> STORE
    DOMAIN --> PROVIDER
    API --> OBS
```

A aplicação é um monólito modular Spring Boot. O frontend do membro e o painel operacional consomem a mesma API, enquanto as regras críticas ficam centralizadas no backend.

## 2. Fluxo completo de auxílio

```mermaid
flowchart TD
    C[Contribuição voluntária sandbox] --> FUND[Fundo comunitário]
    FUND --> REQUEST[Pedido de auxílio]
    REQUEST --> DOCS[Documentos comprobatórios]
    DOCS --> ELIG[Elegibilidade objetiva]
    ELIG --> ANALYSIS[Parecer do ANALYST]
    ANALYSIS --> FRAUD[Triagem antifraude]
    FRAUD --> A1[APPROVER #1]
    A1 --> A2[APPROVER #2 distinto]
    A2 --> APPROVED[APPROVED]
    APPROVED --> INIT[ADMIN inicia pagamento]
    INIT --> PROCESSING[PROCESSING]
    PROCESSING --> WEBHOOK[Webhook HMAC]
    WEBHOOK -->|SETTLED| PAID[PAID]
    WEBHOOK -->|FAILED| FAILED[FAILED]
    WEBHOOK -->|status incerto| RECON[RECONCILIATION_REQUIRED]
    PAID --> LEDGER[Ledger AID_PAYMENT]
    PAID --> AUDIT[Auditoria]
```

O estado `PAID` não pode ser forçado pelo painel administrativo. A liquidação é consequência do webhook autenticado do provider sandbox.

## 3. Separação de responsabilidades por papel

```mermaid
flowchart LR
    MEMBER[MEMBER] -->|cria| REQUEST[Pedido]
    ANALYST[ANALYST] -->|parecer + antifraude| REQUEST
    APPROVER1[APPROVER #1] -->|1ª decisão| REQUEST
    APPROVER2[APPROVER #2] -->|2ª decisão| REQUEST
    ADMIN[ADMIN] -->|inicia pagamento| PAYMENT[PaymentAttempt]
    AUDITOR[AUDITOR] -->|somente leitura operacional| REQUEST
    AUDITOR -->|somente leitura| PAYMENT
```

Regras principais:

- analista não aprova;
- aprovador não pode aprovar duas vezes o mesmo pedido;
- os dois aprovadores precisam ser usuários distintos;
- quem aprovou o pedido não pode iniciar o pagamento;
- auditor não altera estado financeiro;
- admin inicia a tentativa, mas não liquida manualmente.

## 4. Arquitetura da camada de pagamento

```mermaid
flowchart LR
    CTRL[Phase4Controller]
    FACADE[PaymentService\nfacade]
    INIT[PaymentInitiationService]
    WEB[PaymentWebhookService]
    ATTEMPTS[(PaymentAttemptRepository)]
    POLICY[AidPolicyService]
    LEDGER[LedgerService]
    AUDIT[AuditService]
    OUTBOX[(OutboxEventRepository)]
    PROVIDER[SandboxPaymentProvider]
    SIG[WebhookSignatureService]
    EVENTS[(WebhookEventRepository)]

    CTRL --> FACADE
    FACADE --> INIT
    FACADE --> WEB
    FACADE --> ATTEMPTS

    INIT --> POLICY
    INIT --> LEDGER
    INIT --> PROVIDER
    INIT --> OUTBOX
    INIT --> AUDIT

    WEB --> SIG
    WEB --> EVENTS
    WEB --> ATTEMPTS
    WEB --> LEDGER
    WEB --> OUTBOX
    WEB --> AUDIT
```

A refatoração separa iniciação e processamento de webhook para reduzir o número de responsabilidades dentro de uma única classe e permitir testes menores e mais específicos.

## 5. Proteções do pagamento

```mermaid
flowchart TD
    REQ[Solicitação de pagamento] --> KEY{Idempotency-Key válida?}
    KEY -->|não| DENY1[Bloqueia]
    KEY -->|sim| PRIOR{Já existe tentativa?}
    PRIOR -->|mesma chave / mesmo auxílio| RETURN[Retorna tentativa anterior]
    PRIOR -->|mesma chave / outro auxílio| DENY2[Bloqueia reutilização]
    PRIOR -->|não| APPROVAL{2 aprovações distintas?}
    APPROVAL -->|não| DENY3[Bloqueia]
    APPROVAL -->|sim| SoD{Admin participou da aprovação?}
    SoD -->|sim| DENY4[Separação de funções]
    SoD -->|não| ELIG{Elegível?}
    ELIG -->|não| DENY5[Bloqueia]
    ELIG -->|sim| BAL{Saldo suficiente?}
    BAL -->|não| DENY6[Bloqueia antes do provider]
    BAL -->|sim| PROVIDER[Inicia no provider sandbox]
```

### Webhook

```mermaid
flowchart TD
    W[Webhook recebido] --> SIG{HMAC válida?}
    SIG -->|não| R1[Rejeita]
    SIG -->|sim| TIME{Timestamp dentro de ±5 min?}
    TIME -->|não| R2[Rejeita]
    TIME -->|sim| EVENT{eventId já visto?}
    EVENT -->|sim| R3[Replay bloqueado]
    EVENT -->|não| STATUS{Status do provider}
    STATUS -->|SETTLED| SETTLE[Liquida uma vez]
    STATUS -->|FAILED| FAIL[FAILED sem débito]
    STATUS -->|outro| REC[RECONCILIATION_REQUIRED]
```

A tabela `webhook_events` possui `eventId` único, reforçando a proteção contra replay também no banco.

## 6. Ledger e auditoria

```mermaid
flowchart LR
    EVENT[Evento de domínio] --> AUDIT[AuditService]
    AUDIT --> ALOCK[(audit_chain_lock)]
    ALOCK --> AE[(audit_events)]
    AE --> AHASH[previousHash + eventHash]

    PAYMENT[Movimento financeiro] --> LEDGER[LedgerService]
    LEDGER --> LLOCK[(ledger_chain_lock)]
    LLOCK --> LE[(ledger_entries)]
    LE --> LHASH[previousHash + entryHash]
```

As cadeias de auditoria e ledger usam SHA-256 e locks no banco para serializar a criação dos registros encadeados.

## 7. Dados sensíveis e identidade

```mermaid
flowchart LR
    USER[Usuário] --> AUTH[Spring Security]
    AUTH --> PASSKEY[WebAuthn / Passkey]
    AUTH --> TOTP[TOTP]
    USER --> PII[Dados privados]
    PII --> AES[AES-256-GCM]
    AES --> KMS[Local key / AWS KMS]
    KMS --> PG[(PostgreSQL)]
```

Perfis privilegiados podem exigir MFA. Dados pessoais privados são protegidos antes de serem persistidos e a arquitetura prevê envelope encryption com AWS KMS.

## 8. Persistência

Principais grupos de dados:

| Grupo | Exemplos |
|---|---|
| Membros e identidade | `members`, `app_users`, KYC, consentimentos |
| Auxílio | `aid_requests`, documentos, análises, antifraude, aprovações |
| Pagamento | `payment_attempts`, `webhook_events`, `outbox_events` |
| Integridade | `ledger_entries`, `audit_events` |
| Privacidade | dados privados criptografados, DSAR |
| Recuperação | solicitações e aprovações administrativas |

As mudanças de schema são controladas pelo Flyway.

## 9. Testes e pipeline

```mermaid
flowchart LR
    PUSH[Push / PR] --> BUILD[mvn verify]
    PUSH --> SYNTAX[node --check]
    PUSH --> SECRET[Gitleaks]
    PUSH --> CODEQL[CodeQL]
    PUSH --> DEPS[Dependency review]
    BUILD --> TC[Testcontainers\nPostgreSQL + Redis]
    BUILD --> SBOM[CycloneDX SBOM]
    PUSH --> E2E[Playwright]
    E2E --> MEMBER[Fluxo membro]
    E2E --> GOV[Governança]
    E2E --> PAY[Settlement sandbox]
```

O fluxo positivo e os caminhos negativos críticos são testados antes do merge.

## 10. Limites atuais

O sistema **não deve ser tratado como pronto para operação financeira real**. Antes de qualquer piloto com recursos reais ainda são necessários, entre outros:

- pentest independente;
- infraestrutura de produção e gestão real de segredos;
- domínio HTTPS e WebAuthn real;
- política LGPD formal e processos operacionais;
- revisão jurídica, contábil e regulatória;
- provider financeiro autorizado;
- reconciliação externa e monitoramento operacional;
- testes de desastre e restauração em ambiente semelhante a produção.

## 11. Decisão arquitetural principal

A regra central do projeto é que **nenhum ator isolado controla todo o ciclo do auxílio**. A arquitetura distribui criação, análise, aprovação, iniciação de pagamento e confirmação de settlement entre papéis e eventos diferentes, deixando evidências no ledger e na auditoria.
