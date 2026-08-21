#!/usr/bin/env bash
set -euo pipefail
if [ "$#" -ne 1 ]; then echo "uso: $0 arquivo.dump" >&2; exit 2; fi
: "${DB_URL:=postgresql://ajuda:ajuda_dev@localhost:5432/ajuda_mutua}"
file="$1"
sha256sum -c "$file.sha256"
pg_restore --clean --if-exists --no-owner --dbname="$DB_URL" "$file"
