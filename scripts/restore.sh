#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "uso: RESTORE_CONFIRMATION=RESTORE_STAGING DB_URL=... $0 arquivo.dump" >&2
  exit 2
fi

: "${DB_URL:?DB_URL é obrigatório}"
: "${RESTORE_CONFIRMATION:?RESTORE_CONFIRMATION=RESTORE_STAGING é obrigatório}"

if [ "$RESTORE_CONFIRMATION" != "RESTORE_STAGING" ]; then
  echo "restore recusado: confirmação inválida" >&2
  exit 3
fi

command -v pg_restore >/dev/null || { echo "pg_restore não encontrado" >&2; exit 3; }
command -v sha256sum >/dev/null || { echo "sha256sum não encontrado" >&2; exit 3; }

file="$1"
[ -f "$file" ] || { echo "arquivo de backup não encontrado" >&2; exit 4; }
[ -f "$file.sha256" ] || { echo "checksum não encontrado" >&2; exit 4; }

sha256sum -c "$file.sha256"
pg_restore --clean --if-exists --no-owner --no-acl --dbname="$DB_URL" "$file"
