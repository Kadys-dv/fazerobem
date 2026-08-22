# Staging External DAST Evidence

## Objetivo

Registrar de forma reproduzível as evidências externas do staging público antes de qualquer promoção de Production Readiness.

Alvo atual:

`https://fazerobem-staging.onrender.com`

## Workflow

O workflow manual `.github/workflows/staging-external-dast.yml` valida:

1. `GET /health` pela internet, com retry/backoff para cold start do Render Free.
2. tempos observados de DNS, conexão, TLS, TTFB e total.
3. tempo até a primeira resposta válida (`external_service_recovery_time_ms`).
4. superfície pública dos endpoints de management.
5. headers de segurança relevantes.
6. OWASP ZAP Baseline/passive scan contra o staging público real.

## Critérios

`external_internet_health=PASS` somente quando a execução real obtiver HTTP 200 e JSON com `status=UP`.

`external_passive_dast=PASS` somente quando o ZAP concluir e não houver achados MEDIUM/HIGH.

Erros do scanner, indisponibilidade do alvo ou relatório ausente não são convertidos em PASS.

## O que permanece externo

Mesmo após health e DAST passarem, estes itens permanecem pendentes até evidência independente correspondente:

- `independent_pentest=PENDING`
- `legal_lgpd_review=PENDING`
- `external_provider_homologation=PENDING`

Portanto, o estado de produção permanece:

`production=NO-GO`

até a conclusão formal dos requisitos externos obrigatórios.

## Evidências

Cada execução publica um artifact por 30 dias contendo:

- `evidence/external-health/`
- `evidence/external-zap/zap-report.json`
- `evidence/external-zap/zap-report.html`
- `evidence/external-zap/risk-summary.txt`
- `evidence/external-summary.txt`

A evidência versionada nunca deve conter secrets, tokens, senhas ou conteúdo sensível de endpoints administrativos.
