# Prompt Codex — Fase 5: produção controlada e conformidade

Trabalhe no projeto Ajuda Mútua Community existente e preserve integralmente as regras anti-pirâmide e as Fases 1–4.

Objetivo: preparar uma implantação piloto controlada sem habilitar captação pública nem promessa de retorno.

Implemente: criptografia de PII em repouso com chave externa/KMS abstraction; rotação de segredos; MFA/passkeys para papéis privilegiados; política ABAC para PII; retenção/expiração de documentos em job auditável; reconciliação periódica; outbox publisher idempotente; métricas/health checks sem PII; rate limiting; lockout e alertas de login; testes de concorrência de pagamento; Testcontainers PostgreSQL; SBOM e dependency scanning; documentação LGPD e runbook de incidente.

Não integrar PIX real sem adaptador separado, revisão jurídica/regulatória e aprovação explícita. Não criar ROI, juros, bônus de indicação, promessa de retorno, fila de retorno ou saldo resgatável de contribuição.
