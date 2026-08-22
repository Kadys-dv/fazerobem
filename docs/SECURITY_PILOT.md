# Segurança do piloto

- Sem PIX real: apenas `SandboxPaymentProvider`.
- Perfis ANALYST, APPROVER, ADMIN e AUDITOR exigem MFA TOTP por sessão.
- 5 falhas de login bloqueiam a conta por 15 minutos.
- Rate limiting em memória protege a borda do MVP; produção deve usar gateway/Redis distribuído.
- PII usa abstração `SecretProtector`; implementação local é AES-256-GCM com chave externa. Em produção substituir por KMS/HSM.
- `/actuator/health`, métricas e logs não devem conter PII.
- Outbox é publicada de forma idempotente marcando `published_at`.
- Pagamentos PROCESSING antigos entram em `RECONCILIATION_REQUIRED`, nunca são liquidados automaticamente por inferência.
- Documentos expirados são removidos em job auditável.
- `mvn package` gera CycloneDX SBOM.
