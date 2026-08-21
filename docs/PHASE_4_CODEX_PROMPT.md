# Prompt Codex — Fase 4: identidade, privacidade e operação financeira segura

Trabalhe no projeto Ajuda Mútua Community existente. Preserve integralmente as regras das Fases 1–3: não criar investimento, ROI, juros, comissão de indicação, recompensa por recrutamento, promessa de retorno, fila de retorno ou saldo individual resgatável derivado de contribuições.

Objetivo da Fase 4: preparar o sistema para uma futura operação real sem movimentar dinheiro automaticamente ainda.

Implemente:
1. KYC básico e estados `UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED` sem armazenar imagens de documento em campos de texto ou logs.
2. Consentimentos versionados de termos, política de privacidade e regulamento comunitário.
3. Dados pessoais sensíveis separados das tabelas públicas; nunca retornar CPF/endereço no dashboard público ou audit metadata.
4. Mascaramento/redação de PII.
5. Política de retenção de documentos e expiração lógica, sem apagar ledger/audit trail.
6. Interface `PaymentProvider` com implementação fake/sandbox.
7. Máquina de estados de pagamento: `READY`, `PROCESSING`, `SETTLED`, `FAILED`, `RECONCILIATION_REQUIRED`.
8. Idempotência persistente para pagamento/reconciliação.
9. Webhook sandbox assinado com HMAC, timestamp e proteção contra replay.
10. Outbox transacional.
11. Testes automatizados de autorização, dupla aprovação, antifraude, limites, idempotência e webhook/replay.
12. Testcontainers PostgreSQL quando compatível.
13. `docs/THREAT_MODEL.md` cobrindo abuso interno, admin comprometido, documento malicioso, fraude, replay, double-spend, ledger e PII.

Restrições:
- Não integrar PIX real nesta fase.
- Não armazenar segredo em código ou `application.yml` versionado.
- Não criar override para ADMIN ignorar elegibilidade.
- Não criar DELETE para ledger, audit, aprovações, triagens ou pagamentos.
- Não permitir que o mesmo ator analise, aprove e pague a mesma solicitação.
- Validar transições no domínio e banco quando possível.
- Manter Java 21, Spring Boot 4.0.8, PostgreSQL e Flyway.
- Ao final, executar `mvn test`; se não puder, explicar exatamente o motivo sem afirmar sucesso.
