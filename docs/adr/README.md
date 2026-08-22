# Architecture Decision Records

Os ADRs registram decisões que alteram segurança, governança, consistência ou operação do Fazer o Bem.

| ADR | Decisão | Status |
|---|---|---|
| [0001](0001-separation-of-duties.md) | Separação de funções na concessão e pagamento | Accepted |
| [0002](0002-payment-state-authority.md) | `PAID` depende de settlement autenticado | Accepted |
| [0003](0003-ledger-audit-integrity.md) | Ledger e auditoria append-only encadeados | Accepted |
| [0004](0004-idempotency-concurrency.md) | Idempotência + locking + invariantes de banco | Accepted |
| [0005](0005-webhook-authentication.md) | HMAC, timestamp e replay protection | Accepted |
| [0006](0006-sandbox-first.md) | Sandbox obrigatório antes de dinheiro real | Accepted |
| [0007](0007-postgresql-source-of-truth.md) | PostgreSQL como fonte de verdade | Accepted |
| [0008](0008-redis-not-financial-source-of-truth.md) | Redis não é fonte de verdade financeira | Accepted |
| [0009](0009-production-fail-closed.md) | Configuração de produção fail-closed | Accepted |
| [0010](0010-modular-monolith.md) | Monólito modular antes de microserviços | Accepted |

## Matriz de evidência

A relação entre ameaça, controle, teste, gate de CI e evidência está em [`../THREAT_CONTROL_TEST_EVIDENCE.md`](../THREAT_CONTROL_TEST_EVIDENCE.md).

## Formato

Cada ADR contém contexto, decisão, consequências e condições para revisão. Uma decisão aceita não deve ser silenciosamente substituída; uma mudança significativa cria um novo ADR que referencia o anterior.

ADRs e CI não substituem as evidências externas obrigatórias da issue #38.