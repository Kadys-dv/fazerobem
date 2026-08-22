# Neon DR rehearsal — scoped database recovery

## Purpose

Measure a real, reversible recovery path against the isolated Neon `runtime-staging` branch without touching production data or enabling real payments.

## Scope

This rehearsal measures recovery of an isolated synthetic database object using `pg_dump`/`pg_restore` over TLS. It verifies the recovery marker plus critical post-recovery invariants (`Flyway V1-V8`, `ledger_chain_lock`, `audit_chain_lock`).

It does **not** claim full-service RTO for the web application, infrastructure failover, legal readiness, independent pentest approval, or financial provider production approval.

## Gates

- database recovery RTO target: <= 30 minutes;
- observed backup-to-incident RPO target: <= 5 minutes;
- Flyway V1-V8 must remain present;
- ledger and audit chain locks must remain intact;
- backup SHA-256 must be recorded;
- only synthetic rehearsal data may be destroyed;
- `PAYMENT_INITIATION_ENABLED` remains outside this workflow and real money stays NO-GO.

## Evidence

The workflow `neon-dr-rehearsal` produces an artifact containing:

- workflow run ID and commit;
- backup SHA-256;
- backup, incident and recovery timestamps;
- measured RTO/RPO seconds;
- Flyway and chain-lock validation results;
- explicit scope label preventing this result from being misrepresented as a full production DR certification.

## Failure behavior

Any missing secret, missing TLS requirement, failed dump/restore, marker mismatch, broken financial invariant, or threshold breach fails the workflow closed. Cleanup attempts to remove the synthetic marker even on failure.
