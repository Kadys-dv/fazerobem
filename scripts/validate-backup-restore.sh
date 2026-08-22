#!/usr/bin/env bash
set -euo pipefail
: "${DB_URL_HOST:=localhost}"; : "${DB_PORT:=5432}"; : "${DB_NAME:=ajuda_mutua}"; : "${DB_USER:=ajuda}"; : "${PGPASSWORD:=ajuda_dev}"
out="${1:-./data/backups/phase7-validation.dump}"; mkdir -p "$(dirname "$out")"
pg_dump -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" -Fc "$DB_NAME" > "$out"
sha256sum "$out" > "$out.sha256"; sha256sum -c "$out.sha256"
restore_db="${DB_NAME}_restore_check"; dropdb -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" --if-exists "$restore_db"; createdb -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" "$restore_db"
pg_restore -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$restore_db" "$out"
psql -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$restore_db" -Atc "select count(*) from ledger_entries; select count(*) from audit_events;"
dropdb -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" "$restore_db"; echo "OK: backup -> restore validado"
