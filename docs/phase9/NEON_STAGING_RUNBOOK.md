# Neon staging runbook

## Purpose

Run Phase 9 PostgreSQL rehearsal against an isolated Neon branch without real personal data or real financial movement.

## Provisioned topology

- Neon project: `fazerobem-staging`
- Database: `neondb`
- Rehearsal branch: `staging-rehearsal`
- Credentials: external secret only; never commit URI/password

## Application configuration

Inject the Neon JDBC URL, database username and password through the hosting platform's secret/environment facility. TLS must be required by the client connection. Do not copy the provider URI into source, workflow YAML, screenshots, issues, logs or evidence artifacts.

Use the project's existing staging/production fail-closed configuration. Do not relax WebAuthn origin/RP-ID, secure cookies, proxy/TLS, KMS or provider-secret requirements merely to make staging boot.

## Rehearsal sequence

1. Record application commit SHA and Neon branch ID.
2. Start from synthetic-only data.
3. Boot application and let Flyway apply the repository migrations.
4. Verify Flyway history and expected application tables.
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

## Acceptance

PASS requires application + database + external health + business flow + rollback/recovery evidence. A reachable Neon database by itself is not a staging rehearsal PASS.

## Free-plan caveat

The free Neon tier is suitable for this controlled staging exercise, but free-tier quotas/restore windows are not production SLAs. Production readiness must not infer availability or recovery guarantees from the free staging plan.
