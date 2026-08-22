# Staging Hardening

O staging é sandbox e deve ser isolado de desenvolvimento e de qualquer futura produção.

## Variáveis obrigatórias

O profile `staging` não fornece defaults para banco, usuário, senha, Redis password, chave criptográfica, segredo de webhook, WebAuthn RP/origin, KMS provider e diretório de documentos. Ausência deve impedir startup ou deixar o componente fail-closed.

## Requisitos

- `SPRING_PROFILES_ACTIVE=staging`;
- HTTPS na borda e `X-Forwarded-*` confiável somente do proxy controlado;
- `SESSION_COOKIE_SECURE=true` efetivo pelo profile;
- domínio/RP WebAuthn exclusivo de staging;
- PostgreSQL e Redis exclusivos;
- bucket/diretório de documentos exclusivo;
- secrets injetados pelo ambiente/secret manager, nunca versionados;
- credenciais distintas de dev e de qualquer futura produção;
- acesso administrativo restrito;
- backups separados e restore ensaiado;
- OTLP/Prometheus enviados para stack de observabilidade de staging.

## Gate de promoção

Antes de considerar o staging apto ao piloto simulado, validar:

1. startup falha quando secrets obrigatórios estão ausentes;
2. cookie de sessão possui Secure, HttpOnly e SameSite=Strict;
3. WebAuthn rejeita origin/RP de localhost;
4. endpoints de actuator não revelam secrets ou detalhes de health;
5. dados e documentos de dev não estão acessíveis;
6. backup/restore e chaos tests passam;
7. alertas e runbook foram exercitados.
