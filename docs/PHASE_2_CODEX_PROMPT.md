# Prompt Codex — Fase 2: Segurança, papéis e dupla aprovação

Você está trabalhando no projeto `ajuda-mutua-community`, um MVP Java 21 + Spring Boot 4.0.8 + PostgreSQL + Flyway.

## Contexto obrigatório
O produto é uma comunidade de ajuda mútua. NÃO é investimento, HYIP, pirâmide, Ponzi ou sistema de rendimento. É proibido adicionar:
- promessa de retorno financeiro;
- juros/rentabilidade sobre contribuições;
- fila que paga membros com dinheiro de membros posteriores;
- bônus financeiro por indicação/recrutamento;
- saldo individual resgatável derivado de contribuição;
- multiplicação de valores depositados.

Contribuição é voluntária e irreversível para o fundo comunitário. Auxílio é independente do valor contribuído e depende de critérios, aprovação e disponibilidade do fundo.

## Objetivo da fase
Adicionar autenticação e autorização reais, papéis e dupla aprovação para pagamentos, preservando o ledger imutável existente.

## Requisitos
1. Adicionar Spring Security.
2. Criar usuários autenticáveis separados da entidade `Member` quando necessário.
3. Papéis mínimos: MEMBER, ANALYST, APPROVER, ADMIN, AUDITOR.
4. Nunca armazenar senha em texto puro. Usar Argon2id ou BCrypt via Spring Security.
5. Criar login seguro com sessão HTTP-only/SameSite ou JWT curto + refresh seguro. Prefira sessão server-side se não houver necessidade arquitetural de JWT.
6. Adicionar CSRF quando aplicável.
7. `MEMBER` pode ver seu perfil, criar pedido de auxílio, visualizar seus próprios pedidos e registrar contribuição somente por fluxo permitido.
8. `ANALYST` pode analisar pedido e registrar parecer, mas não pagar.
9. `APPROVER` pode aprovar/rejeitar pedidos.
10. Pagamento de auxílio exige DUAS aprovações distintas de usuários diferentes.
11. Um aprovador não pode aprovar duas vezes o mesmo pedido.
12. Quem criou/analisou o pedido não deve ser suficiente sozinho para liberar pagamento.
13. `AUDITOR` é somente leitura e pode ver ledger, aprovações e audit trail.
14. `ADMIN` não pode apagar ledger, aprovações ou audit trail.
15. Criar tabela append-only `audit_events` com id, actorUserId, action, entityType, entityId, metadata, timestamp, previousHash e eventHash.
16. Não criar endpoints DELETE para ledger/audit.
17. Manter `ledger_entries` append-only.
18. Alterar pagamento para somente ocorrer quando pedido estiver APPROVED, houver 2 aprovações válidas e distintas e saldo suficiente.
19. Adicionar optimistic locking (`@Version`) em `AidRequest`.
20. Adicionar idempotency key para endpoint de pagamento.
21. Criar migração Flyway V2.
22. Criar testes de integração de autorização, dupla aprovação, idempotência, fundo insuficiente e armazenamento seguro de senha.
23. Não remover funcionalidades existentes.
24. Atualizar README com setup, contas demo apenas em profile `dev`, fluxos e modelo de segurança.

## Qualidade
Faça o menor patch coerente possível, use transações corretamente, não exponha stack trace, use DTOs, valide ownership/roles no backend, evite logs sensíveis e rode `mvn test` ao final. Se algum requisito conflitar com a versão real das dependências, adapte usando APIs compatíveis com Spring Boot 4.0.8 e documente.
