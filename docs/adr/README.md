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

## Formato

Cada ADR contém contexto, decisão, consequências e condições para revisão. Uma decisão aceita não deve ser silenciosamente substituída; uma mudança significativa cria um novo ADR que referencia o anterior.
