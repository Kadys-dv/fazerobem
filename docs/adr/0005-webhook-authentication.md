# ADR-0005 — Autenticação e replay protection de webhooks

**Status:** Accepted  
**Data:** 2026-08-21

## Contexto

O webhook de pagamento cruza uma trust boundary e pode causar mudança financeira. HTTPS sozinho autentica o servidor para o cliente, não prova que o payload recebido foi produzido pelo provider esperado nem impede replay de uma mensagem válida.

## Decisão

O webhook deve exigir:

- assinatura HMAC-SHA256 sobre `timestamp.body`;
- comparação de assinatura em tempo constante;
- segredo fora do código e com comprimento mínimo;
- timestamp ISO-8601 dentro de uma janela exata de 5 minutos;
- `X-Event-Id` obrigatório e único persistido;
- hash SHA-256 do corpo armazenado como evidência;
- persistência/flush do evento antes de mutação financeira;
- lookup da `providerReference` persistida pelo sistema.

## Consequências

- mensagens adulteradas, antigas e repetidas são rejeitadas;
- rotação de segredo precisa ser planejada para produção;
- HMAC não elimina necessidade de TLS, rate limiting e allowlist/mTLS se o provider real suportar.

## Revisão

Ao integrar provider real, adotar o mecanismo oficial mais forte disponível (assinaturas assimétricas, mTLS, JWKS, IP allowlist como defesa adicional) sem reduzir replay protection e idempotência existentes.
