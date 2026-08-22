# Changelog

Todas as mudanças relevantes do projeto são registradas neste arquivo.

O formato segue a ideia de Keep a Changelog e o versionamento usa SemVer quando houver releases formais.

## [0.1.0-alpha] - 2026-08-21

### Added
- PWA do membro com cadastro, autenticação, contribuição sandbox e pedidos de auxílio.
- Upload protegido de documentos comprobatórios.
- Painel operacional para ANALYST, APPROVER, ADMIN e AUDITOR.
- Elegibilidade objetiva, triagem antifraude e dupla aprovação independente.
- Pagamento sandbox com idempotência, webhook HMAC, replay protection e reconciliação.
- Ledger financeiro encadeado por SHA-256 e trilha de auditoria encadeada.
- WebAuthn/passkeys, TOTP e controles de acesso por função.
- Proteção de PII com AES-256-GCM e suporte opcional a AWS KMS.
- PostgreSQL, Flyway, Redis, Testcontainers e Playwright.
- E2E completo do fluxo MEMBER → ANALYST → APPROVERS → ADMIN → SETTLED → PAID.
- SBOM CycloneDX, secret scan, CodeQL e gates de CI.
- Documentação visual de arquitetura com diagramas Mermaid.

### Changed
- Camada de pagamento separada em `PaymentInitiationService`, `PaymentWebhookService` e fachada `PaymentService`.
- Janela temporal de webhook endurecida para comparação exata de cinco minutos.
- Readiness de PostgreSQL/Redis e estabilidade dos testes E2E reforçados.

### Security
- Rejeição de assinatura HMAC inválida, corpo adulterado e timestamps fora da janela.
- Bloqueio de replay por `eventId`.
- Testes para `FAILED`, `RECONCILIATION_REQUIRED`, saldo insuficiente e prevenção de segundo débito.
- Separação de funções entre análise, aprovação e iniciação do pagamento.

### Known limitations
- Somente sandbox; não existe PIX, cartão ou custódia real.
- Release ainda não passou por pentest independente.
- AWS KMS/secret manager real, TLS de produção e domínio WebAuthn real ainda não são parte desta alpha.
- Revisão jurídica, contábil, LGPD e regulatória continua obrigatória antes de qualquer piloto real.
