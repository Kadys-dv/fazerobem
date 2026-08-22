#!/usr/bin/env bash
set -euo pipefail

: "${STAGING_BASE_URL:?STAGING_BASE_URL é obrigatório}"
: "${HEALTH_PATH:=/health}"
: "${HEALTH_TIMEOUT_SECONDS:=10}"

if [[ "$STAGING_BASE_URL" != https://* ]] && [ "${ALLOW_HTTP_FOR_TEST:-false}" != "true" ]; then
  echo "health check recusado: staging deve usar HTTPS" >&2
  exit 2
fi

url="${STAGING_BASE_URL%/}${HEALTH_PATH}"
body="$(curl --fail --silent --show-error --max-time "$HEALTH_TIMEOUT_SECONDS" "$url")"

echo "$body" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' || {
  echo "health check falhou: status UP não encontrado" >&2
  exit 3
}

echo "external health OK: $url"
