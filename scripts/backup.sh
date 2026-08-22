#!/usr/bin/env bash
set -euo pipefail

: "${DB_URL:?DB_URL é obrigatório}"
: "${BACKUP_DIRECTORY:=./data/backups}"

command -v pg_dump >/dev/null || { echo "pg_dump não encontrado" >&2; exit 3; }
command -v sha256sum >/dev/null || { echo "sha256sum não encontrado" >&2; exit 3; }

mkdir -p "$BACKUP_DIRECTORY"
out="$BACKUP_DIRECTORY/ajuda-mutua-$(date -u +%Y%m%dT%H%M%SZ).dump"

pg_dump --format=custom --no-owner --no-acl --dbname="$DB_URL" --file="$out"
sha256sum "$out" > "$out.sha256"

if [ ! -s "$out" ]; then
  echo "backup vazio" >&2
  exit 4
fi

echo "$out"
