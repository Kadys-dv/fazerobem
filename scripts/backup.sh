#!/usr/bin/env bash
set -euo pipefail
: "${DB_URL:=postgresql://ajuda:ajuda_dev@localhost:5432/ajuda_mutua}"
: "${BACKUP_DIRECTORY:=./data/backups}"
mkdir -p "$BACKUP_DIRECTORY"
out="$BACKUP_DIRECTORY/ajuda-mutua-$(date -u +%Y%m%dT%H%M%SZ).dump"
pg_dump --format=custom --no-owner --dbname="$DB_URL" --file="$out"
sha256sum "$out" > "$out.sha256"
echo "$out"
