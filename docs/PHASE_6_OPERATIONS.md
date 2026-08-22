# Fase 6 — piloto operacional sem dinheiro real

## Passkeys / WebAuthn
O projeto usa `spring-security-webauthn`, RP configurável por `WEBAUTHN_RP_ID`, `WEBAUTHN_RP_NAME` e `WEBAUTHN_ALLOWED_ORIGINS`. Credenciais WebAuthn são persistidas no PostgreSQL nas tabelas `user_entities` e `user_credentials`.

Em produção, use HTTPS e um RP ID correspondente ao domínio real. Registro de passkey exige sessão autenticada. Login por passkey é tratado como MFA verificada para perfis privilegiados; TOTP continua como alternativa.

## Redis
Redis é usado para rate limiting e contagem distribuída de falhas de login. Em indisponibilidade do Redis, rate limiting degrada para memória local; bloqueio de conta continua persistido no PostgreSQL.

## KMS
`KMS_PROVIDER=local` usa AES-256-GCM local apenas para desenvolvimento. `KMS_PROVIDER=aws-kms` usa envelope encryption: uma data key AES-256 é criada pelo AWS KMS, a PII é cifrada localmente com GCM e somente a data key cifrada é armazenada.

Configuração AWS:
- `KMS_PROVIDER=aws-kms`
- `AWS_KMS_KEY_ID=alias/ajuda-mutua-pii`
- credenciais AWS por workload identity/role, nunca no repositório.

Rotação: `POST /api/v1/admin/crypto/rotate` recriptografa registros cuja `encryption_key_id` difere da chave ativa. Mantenha permissão de decrypt nas chaves antigas até a rotação terminar e ser validada.

## DSAR / LGPD
Endpoints em `/api/v1/privacy/dsar` permitem pedido de acesso/exportação, correção, apagamento e histórico de consentimentos. O export inclui os dados do próprio titular, consentimentos, solicitações de auxílio e ledger associado. Registros financeiros/auditoria não devem ser apagados quando houver obrigação de retenção; a política jurídica deve definir anonimização versus retenção.

## Relatórios de transparência assinados
Relatórios são assinados com Ed25519. Configure:
- `TRANSPARENCY_ED25519_PRIVATE_KEY_BASE64` (PKCS#8)
- `TRANSPARENCY_ED25519_PUBLIC_KEY_BASE64` (X.509)
- `TRANSPARENCY_SIGNING_KEY_ID`

A chave privada não deve estar em Git. O endpoint público `/api/v1/transparency/reports/verification` publica algoritmo, key id e chave pública para verificação independente.

## Backup e restore
`scripts/backup.sh` cria `pg_dump` custom + SHA-256. `scripts/restore.sh` valida o hash antes de restaurar. A restauração deve ser testada periodicamente em banco efêmero separado; nunca faça teste destrutivo no banco piloto.

## Ledger concorrente
Fase 6 adiciona locks transacionais PostgreSQL (`ledger_chain_lock` e `audit_chain_lock`) para serializar append e impedir bifurcação de `previous_hash` sob concorrência.

## Observabilidade
Actuator expõe `health`, `metrics` e `prometheus`. Micrometer Tracing/OpenTelemetry pode exportar OTLP usando `OTEL_EXPORTER_OTLP_ENDPOINT`. Nenhum atributo de trace deve conter CPF, endereço, documento, token, segredo ou payload de webhook completo.

## Gate de produção
Esta fase continua SANDBOX. Não habilitar PIX real, custódia ou pagamento real antes de revisão jurídica/regulatória independente, pentest, teste de restore, runbook exercitado e aprovação formal de risco.
