#!/usr/bin/env bash
set -euo pipefail

: "${DB_URL_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${DB_NAME:=ajuda_mutua}"
: "${DB_USER:=ajuda}"
: "${PGPASSWORD:=ajuda_dev}"
: "${PG_CONTAINER:=}"

out="${1:-./data/backups/phase8-validation.dump}"
mkdir -p "$(dirname "$out")"

psql_exec() {
  local database="$1"; shift
  if [[ -n "$PG_CONTAINER" ]]; then
    docker exec -e PGPASSWORD="$PGPASSWORD" "$PG_CONTAINER" psql -U "$DB_USER" -d "$database" "$@"
  else
    psql -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$database" "$@"
  fi
}

critical_counts() {
  local database="$1"
  psql_exec "$database" -At -F '|' -c \
    "select
       (select count(*) from ledger_entries),
       (select count(*) from audit_events),
       (select count(*) from payment_attempts),
       (select count(*) from webhook_events),
       (select count(*) from aid_requests);"
}

before="$(critical_counts "$DB_NAME")"

if [[ -n "$PG_CONTAINER" ]]; then
  container_dump="/tmp/fazerobem-phase8.dump"
  docker exec -e PGPASSWORD="$PGPASSWORD" "$PG_CONTAINER" pg_dump -U "$DB_USER" -Fc "$DB_NAME" -f "$container_dump"
  docker cp "$PG_CONTAINER:$container_dump" "$out"
else
  pg_dump -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" -Fc "$DB_NAME" > "$out"
fi

sha256sum "$out" > "$out.sha256"
sha256sum -c "$out.sha256"

restore_db="${DB_NAME}_restore_check"
if [[ -n "$PG_CONTAINER" ]]; then
  docker exec -e PGPASSWORD="$PGPASSWORD" "$PG_CONTAINER" dropdb -U "$DB_USER" --if-exists "$restore_db"
  docker exec -e PGPASSWORD="$PGPASSWORD" "$PG_CONTAINER" createdb -U "$DB_USER" "$restore_db"
  docker cp "$out" "$PG_CONTAINER:/tmp/fazerobem-phase8-restore.dump"
  docker exec -e PGPASSWORD="$PGPASSWORD" "$PG_CONTAINER" pg_restore -U "$DB_USER" -d "$restore_db" /tmp/fazerobem-phase8-restore.dump
else
  dropdb -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" --if-exists "$restore_db"
  createdb -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" "$restore_db"
  pg_restore -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$restore_db" "$out"
fi

after="$(critical_counts "$restore_db")"
if [[ "$before" != "$after" ]]; then
  echo "ERRO: contagens críticas divergiram após restore"
  echo "origem=$before"
  echo "restore=$after"
  exit 1
fi

psql_exec "$restore_db" -Atc \
  "select count(*) from payment_attempts p join aid_requests a on a.id=p.aid_request_id where p.status='SETTLED' and a.status <> 'PAID';" | grep -qx '0'

psql_exec "$restore_db" -Atc \
  "select count(*) from (select aid_request_id from payment_attempts where status in ('READY','PROCESSING','SETTLED','RECONCILIATION_REQUIRED') group by aid_request_id having count(*) > 1) x;" | grep -qx '0'

if [[ -n "$PG_CONTAINER" ]]; then
  docker exec -e PGPASSWORD="$PGPASSWORD" "$PG_CONTAINER" dropdb -U "$DB_USER" "$restore_db"
else
  dropdb -h "$DB_URL_HOST" -p "$DB_PORT" -U "$DB_USER" "$restore_db"
fi

echo "OK: backup -> restore preservou contagens e invariantes financeiras"
