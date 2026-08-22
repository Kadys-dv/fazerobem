# Chaos & Disaster Recovery Plan

Execução somente em CI/staging sandbox.

## Invariantes

Após qualquer falha/restart/restore:

- um auxílio nunca possui mais de um efeito financeiro liquidado;
- `PAID` exige settlement válido e ledger correspondente;
- ledger e auditoria continuam verificáveis;
- webhook replay não produz nova mutação;
- tentativa incerta não é convertida silenciosamente em sucesso;
- operações podem permanecer bloqueadas até reconciliação.

## Cenários

| Cenário | Injeção | Verificação |
|---|---|---|
| PostgreSQL restart | reiniciar container durante janela controlada | aplicação recupera conexão; nenhuma duplicação; integridade válida |
| Redis restart | reiniciar container | sessão/rate controls falham de forma previsível; nenhum efeito financeiro duplicado |
| app kill after provider initiation | terminar processo antes do webhook | tentativa permanece rastreável e reconciliável |
| duplicate SETTLED webhook | enviar mesmo eventId novamente | replay rejeitado; um único ledger debit |
| concurrent SETTLED | eventos concorrentes | lock/unique constraints preservam exactly-once financial effect |
| provider unknown status | status não reconhecido | `RECONCILIATION_REQUIRED` |
| backup/restore | restaurar em DB limpo | contagens e cadeias de integridade conferem |

## Critério de aprovação

Um cenário só passa quando o estado final pode ser explicado por ledger, auditoria e payment attempt. Correção manual direta no banco invalida o ensaio.
