# Neon staging runbook

## Purpose

Run Phase 9 PostgreSQL rehearsal against isolated Neon branches without real personal data or real financial movement.

## Provisioned topology

- Neon project: `fazerobem-staging`
- Database: `neondb`
- Schema-validation branch: `staging-rehearsal`
- Runtime branch: `runtime-staging`
- Credentials: external secret only; never commit URI/password

The schema-validation branch is evidence that the repository's V1-V8 SQL is PostgreSQL-compatible when applied in order. It is **not** the runtime source of truth because it was created outside Flyway history.

The runtime branch intentionally starts empty. The Spring application must create its schema there through Flyway so `flyway_schema_history` is authoritative.

## Required GitHub Secrets

The manual workflow `.github/workflows/neon-staging-rehearsal.yml` requires:

- `NEON_STAGING_JDBC_URL`
- `NEON_STAGING_DB_USER`
- `NEON_STAGING_DB_PASSWORD`

No secret value belongs in Git, issues, screenshots, workflow output or evidence artifacts. The JDBC URL must require TLS.

## Application configuration

Inject the Neon JDBC URL, database username and password through the hosting/CI secret facility. TLS must be required by the client connection. Do not copy the provider URI into source, workflow YAML, screenshots, issues, logs or evidence artifacts.

Use the project's existing staging fail-closed configuration. Do not relax WebAuthn origin/RP-ID, secure cookies, proxy/TLS, KMS or provider-secret requirements merely to make staging boot. `PAYMENT_INITIATION_ENABLED` remains `false` for this rehearsal.

## Rehearsal sequence

1. Record application commit SHA and Neon branch ID.
2. Start from the clean `runtime-staging` branch and synthetic-only data.
3. Boot application with profile `staging` and let Flyway apply repository migrations.
4. Verify successful Flyway V1-V8 history and expected application tables/indexes.
5. Execute external health/readiness checks.
6. Run synthetic member/request/approval/payment-sandbox flow.
7. Verify ledger and audit invariants.
8. Record baseline latency/error metrics.
9. Exercise payment freeze.
10. Create a database recovery point/branch before destructive rehearsal steps.
11. Exercise application rollback and database restore/recovery procedure.
12. Re-run ledger/audit integrity checks.
13. Record measured RTO and observed RPO.
14. Store redacted evidence with timestamp and hashes.

## Current evidence

Verified on 2026-08-22:

- Neon project exists and is reachable through the managed connector;
- `staging-rehearsal` accepted V1-V8 as individually prepared PostgreSQL statements;
- 29 application tables exist on `staging-rehearsal`;
- critical financial indexes exist: `uk_payment_settled_per_aid`, `uk_payment_active_per_aid`, `idx_ledger_created_at`, `idx_audit_created`;
- `payment_attempts.version` exists;
- `ledger_chain_lock` and `audit_chain_lock` each contain exactly one lock row;
- core business tables contain zero records;
- `runtime-staging` was created clean with zero public tables so Flyway can remain authoritative.

This evidence proves database compatibility only. It does **not** prove application boot, full staging rehearsal, recovery objectives or external security.

## Acceptance

PASS requires application + database + external health + business flow + rollback/recovery evidence. A reachable Neon database or manually compatible schema by itself is not a staging rehearsal PASS.

## Free-plan caveat

The free Neon tier is suitable for this controlled staging exercise, but free-tier quotas/restore windows are not production SLAs. Production readiness must not infer availability or recovery guarantees from the free staging plan.
