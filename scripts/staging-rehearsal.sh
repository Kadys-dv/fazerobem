#!/usr/bin/env bash
set -euo pipefail

cmd="${1:-}"

require_staging_ack() {
  : "${STAGING_REHEARSAL_ACK:?STAGING_REHEARSAL_ACK é obrigatório}"
  if [ "$STAGING_REHEARSAL_ACK" != "I_UNDERSTAND_STAGING_ONLY" ]; then
    echo "rehearsal recusado: confirmação de staging inválida" >&2
    exit 2
  fi
}

require_https_base() {
  : "${STAGING_BASE_URL:?STAGING_BASE_URL é obrigatório}"
  case "$STAGING_BASE_URL" in
    https://*) ;;
    *) echo "rehearsal recusado: STAGING_BASE_URL deve usar HTTPS" >&2; exit 3 ;;
  esac
}

case "$cmd" in
  preflight)
    require_staging_ack
    require_https_base
    : "${DB_URL:?DB_URL de staging é obrigatório}"
    if [ "${PAYMENT_INITIATION_ENABLED:-}" != "false" ]; then
      echo "rehearsal recusado: PAYMENT_INITIATION_ENABLED deve ser false" >&2
      exit 4
    fi
    echo "preflight OK: staging reconhecido, HTTPS obrigatório e pagamentos congelados"
    ;;

  backup)
    require_staging_ack
    : "${DB_URL:?DB_URL de staging é obrigatório}"
    bash scripts/backup.sh
    ;;

  health)
    require_staging_ack
    require_https_base
    bash scripts/external-health-check.sh
    ;;

  restore-isolated)
    require_staging_ack
    : "${DB_URL:?DB_URL do alvo isolado é obrigatório}"
    : "${RESTORE_TARGET_KIND:?RESTORE_TARGET_KIND é obrigatório}"
    if [ "$RESTORE_TARGET_KIND" != "isolated-staging-validation" ]; then
      echo "restore recusado: alvo deve ser isolated-staging-validation" >&2
      exit 5
    fi
    if [ "$#" -ne 2 ]; then
      echo "uso: $0 restore-isolated arquivo.dump" >&2
      exit 6
    fi
    RESTORE_CONFIRMATION=RESTORE_STAGING bash scripts/restore.sh "$2"
    ;;

  *)
    cat >&2 <<'USAGE'
uso:
  staging-rehearsal.sh preflight
  staging-rehearsal.sh backup
  staging-rehearsal.sh health
  staging-rehearsal.sh restore-isolated arquivo.dump

variáveis de segurança:
  STAGING_REHEARSAL_ACK=I_UNDERSTAND_STAGING_ONLY
  PAYMENT_INITIATION_ENABLED=false
  STAGING_BASE_URL=https://...
  DB_URL=... (fornecido por secret manager; não registrar em logs)
  RESTORE_TARGET_KIND=isolated-staging-validation (restore apenas)
USAGE
    exit 64
    ;;
esac
