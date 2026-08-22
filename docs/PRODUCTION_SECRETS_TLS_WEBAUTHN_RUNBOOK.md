# Production Secrets, TLS e WebAuthn Runbook

## Estado

Este documento descreve a fundação de Production Readiness da Fase 9. Ele **não autoriza dinheiro real**.

## Princípio fail-closed

O perfil `production` não possui fallback funcional para credenciais de banco, Redis, WebAuthn, domínio público ou identificadores criptográficos. Configuração ausente ou incoerente deve impedir o startup.

## Contrato mínimo de secrets

Provisionar fora do repositório:

- `DB_URL`, `DB_USER`, `DB_PASSWORD`;
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`;
- `APP_CRYPTO_KEY_ID`;
- `SANDBOX_WEBHOOK_SECRET` enquanto o provedor permanecer sandbox;
- `KMS_PROVIDER` e identificador de chave correspondente;
- `PUBLIC_BASE_URL`;
- `WEBAUTHN_RP_NAME`, `WEBAUTHN_RP_ID`, `WEBAUTHN_ALLOWED_ORIGINS`.

Nenhum desses valores deve ser commitado, colocado em imagem Docker ou reutilizado entre staging e production.

## KMS / Secret Manager

Produção aceita somente provedores explicitamente aprovados pelo validador. `local` é proibido.

Para AWS KMS:

- `KMS_PROVIDER=aws`;
- `AWS_KMS_KEY_ID` obrigatório;
- a identidade da aplicação deve receber apenas as permissões mínimas de encrypt/decrypt necessárias;
- não armazenar credenciais AWS estáticas no repositório; preferir workload identity/role do ambiente.

Um provedor `external` pode ser usado somente quando o bootstrap criptográfico correspondente estiver configurado e validado. Enquanto existir dependência de `APP_CRYPTO_MASTER_KEY_BASE64`, esse valor deve vir exclusivamente do secret manager.

## Rotação

1. criar nova versão da chave/secret sem remover a anterior;
2. atualizar o identificador ativo (`APP_CRYPTO_KEY_ID` ou secret version);
3. executar teste de leitura/escrita e health checks;
4. executar o processo de re-encriptação suportado pela aplicação quando aplicável;
5. confirmar logs/auditoria e ausência de falhas;
6. revogar a versão antiga somente após janela de rollback;
7. registrar data, operador, motivo e evidência da rotação.

Nunca fazer rotação destrutiva sem possibilidade de rollback.

## TLS e proxy

- `PUBLIC_BASE_URL` deve ser HTTPS absoluto e não-local;
- TLS pode terminar no ingress/reverse proxy, mas a aplicação deve receber corretamente `X-Forwarded-Proto`/headers equivalentes de fonte confiável;
- cookies de sessão permanecem `Secure`, `HttpOnly` e `SameSite=Strict`;
- não expor diretamente a porta interna da aplicação à internet;
- o ambiente alvo deve restringir quem pode enviar forwarded headers confiáveis.

## WebAuthn por ambiente

Staging e production devem possuir RP/origins separados.

Produção exige:

- `WEBAUTHN_RP_ID` não-local;
- todos os `WEBAUTHN_ALLOWED_ORIGINS` em HTTPS;
- host do `PUBLIC_BASE_URL` igual ao RP ID ou subdomínio dele;
- origins pertencentes ao mesmo RP ID;
- nenhuma origin `localhost`, IP loopback ou domínio `.local`.

Alterar domínio/RP ID exige planejamento porque credenciais WebAuthn ficam vinculadas ao RP.

## Provisionamento

1. criar banco e Redis privados;
2. criar identidades de workload e secret manager/KMS;
3. provisionar todos os secrets obrigatórios;
4. configurar domínio e certificado TLS;
5. definir RP ID/origins WebAuthn do domínio final;
6. ativar perfil `production`;
7. executar o gate `production-readiness`;
8. validar health/metrics apenas por rede autorizada;
9. registrar evidências e responsáveis.

## Rollback

Em falha durante alteração de secrets/TLS/WebAuthn:

- interromper rollout;
- voltar à versão anterior da aplicação/configuração;
- restaurar a versão anterior do secret/chave ainda válida;
- não alterar estados financeiros para compensar erro de infraestrutura;
- registrar incidente e evidência antes de nova tentativa.

## Proibições

Até um futuro GO de produção:

- não habilitar PIX real;
- não custodiar recursos;
- não usar `KMS_PROVIDER=local` em production;
- não desabilitar cookie Secure/MFA para contornar deploy;
- não adicionar localhost às origins WebAuthn de production;
- não marcar `PAID` manualmente.
