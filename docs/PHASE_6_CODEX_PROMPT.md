# Prompt Codex — Fase 6: piloto operacional sem dinheiro real

Trabalhe no projeto Ajuda Mútua Community preservando integralmente as Fases 1–5 e todas as regras anti-pirâmide.

Objetivo: preparar observabilidade, operação e validação de governança em ambiente piloto, ainda sem PIX real.

Implemente: WebAuthn/passkeys reais para perfis privilegiados como alternativa ao TOTP; Redis para rate limiting/lockout distribuído; KMS provider real atrás de SecretProtector; rotação versionada com recriptografia; dashboards de métricas sem PII; OpenTelemetry; testes end-to-end; testes de propriedades do ledger; chaos tests de webhook/reconciliação; backup/restore testado; DSAR LGPD completo; painel de governança e relatórios de transparência assinados; CI com dependency-check/CodeQL/secret scanning/SBOM.

Não integrar PIX real, adquirente real ou custódia de valores sem revisão jurídica/regulatória independente, threat model atualizado e aprovação explícita. Não criar investimento, ROI, juros, comissão por indicação, fila de retorno, promessa de retorno ou saldo resgatável derivado de contribuições.
