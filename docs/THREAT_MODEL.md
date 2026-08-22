# Threat Model — Fazer o Bem

## Ativos críticos
PII de membros, documentos comprobatórios, consentimentos, decisões de elegibilidade, ledger, trilha de auditoria e estado dos pagamentos.

## Ameaças e controles
- **Conta ADMIN comprometida:** ADMIN não pode aprovar auxílio; pagamento exige duas aprovações distintas e elegibilidade válida.
- **Abuso interno:** papéis separados entre ANALYST, APPROVER e ADMIN; eventos administrativos entram no audit trail encadeado.
- **Documento malicioso:** upload restrito a tipos e tamanho controlados; armazenamento fora da pasta pública; SHA-256 por arquivo.
- **Fraude de membro:** triagem antifraude, limites de categoria, carência, cooldown e comprovantes.
- **Replay de webhook:** HMAC-SHA256, timestamp máximo de 5 minutos e `event_id` único persistido.
- **Double-spend:** idempotência persistente e somente um pagamento `SETTLED` por pedido no banco.
- **Alteração do ledger:** ledger e audit trail não possuem DELETE e são encadeados por hash.
- **Vazamento de PII:** dados privados ficam separados da transparência pública e são protegidos por criptografia autenticada.
- **Falha de provedor:** máquina de estados inclui `RECONCILIATION_REQUIRED`; nenhum pagamento real existe nesta fase.
- **Segredo vazado:** segredos vêm de variável de ambiente ou KMS/secret manager e não devem ser versionados.

## Limites atuais
KYC e pagamento permanecem sandbox. Antes de produção são necessários revisão jurídica/LGPD, pentest independente, KMS/secret manager, provedor KYC real, processador de pagamento regulado e política operacional formal.
