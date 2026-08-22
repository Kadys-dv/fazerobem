# ADR 0009 — Configuração de produção fail-closed

**Status:** Accepted

## Contexto

Defaults de desenvolvimento como localhost, HTTP, secrets locais ou KMS local são convenientes em dev, mas perigosos em produção.

## Decisão

O profile de produção exige configuração explícita para secrets, KMS, TLS/origin, WebAuthn RP ID/origins e cookies seguros. Ausência ou incoerência deve impedir o startup.

## Consequências

- produção não herda fallback funcional de desenvolvimento;
- erros de configuração falham antes de aceitar tráfego;
- alterações de domínio/origin exigem atualização coordenada do WebAuthn;
- provisionamento e rotação precisam seguir runbook auditável.

## Condições para revisão

Somente se houver mecanismo de configuração gerenciada que mantenha fail-closed e validação equivalente.

## Evidências relacionadas

`production-readiness`, `staging-config` e runbooks de secrets/KMS/TLS/WebAuthn.