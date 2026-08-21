# Plano de chaos testing

1. Derrubar Redis durante login/rate-limit: a aplicação deve degradar de forma segura e registrar o evento; nenhum bypass de MFA.
2. Repetir o mesmo webhook: deve ocorrer rejeição por `event_id` já processado e nenhum segundo lançamento no ledger.
3. Enviar webhook desconhecido/fora de ordem: não liquidar automaticamente; direcionar para reconciliação.
4. Reiniciar aplicação com pagamento em `PROCESSING`: após timeout, estado deve ir para `RECONCILIATION_REQUIRED`, nunca presumir `SETTLED`.
5. Executar duas contribuições/pagamentos concorrentes: a cadeia do ledger deve permanecer linear.
6. Indisponibilidade PostgreSQL: retornar falha; não aceitar operação financeira em memória.
7. Falha após criação de outbox e antes de publicação: reinício deve retomar itens `published_at IS NULL` sem duplicar efeito.
