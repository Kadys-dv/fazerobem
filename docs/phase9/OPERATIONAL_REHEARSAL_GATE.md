# Phase 9 operational rehearsal gate

## Goal

Turn staging readiness into measurable operational evidence before any production discussion. This gate is fail-closed and does not permit real-money movement.

## Required evidence

### Availability and observability
- external health endpoint reachable from outside the hosting network;
- health checks do not expose secrets or sensitive internals;
- application, database, Redis, reconciliation and provider-sandbox failures are observable;
- operator can identify stuck `PROCESSING` and `RECONCILIATION_REQUIRED` payments without querying the database manually;
- timestamps and commit SHA are captured for each rehearsal.

### RTO
Record the time from declared service interruption until the application is externally healthy and the critical synthetic flow is usable again.

Target for the staging rehearsal: **RTO <= 30 minutes**.

RTO is PASS only when measured from real timestamps. A runbook estimate is not evidence.

### RPO
Record the latest durable synthetic financial/audit event before the recovery point and compare it with the recovered state.

Target for the staging rehearsal: **RPO <= 5 minutes**.

RPO is PASS only when ledger/audit continuity and expected records are verified after recovery.

### Freeze and rollback
- payment initiation must be disabled before destructive rehearsal actions;
- no role may force `PAID` during the rehearsal;
- deploy rollback must restore the prior application revision;
- database recovery must use an isolated Neon recovery branch or approved restore mechanism;
- ledger and audit integrity checks must pass after recovery.

### Security validation
Run only against an explicitly authorized staging endpoint with synthetic data.

Minimum free-tool baseline:
- OWASP ZAP baseline/passive scan;
- TLS/header checks;
- unauthenticated endpoint inventory;
- role-boundary regression tests already present in CI;
- no active destructive ZAP rules against financial/state-changing endpoints unless separately approved.

A self-run ZAP scan is **DAST evidence**, not an independent pentest.

## Evidence record

For each run record:

- date/time UTC;
- application commit SHA;
- staging URL/identifier (redacted if needed);
- Neon project and branch identifier;
- recovery point identifier;
- freeze start/end timestamps;
- failure declaration timestamp;
- recovery healthy timestamp;
- measured RTO;
- pre-recovery durable-event timestamp;
- newest recovered durable-event timestamp;
- observed RPO;
- ledger integrity result;
- audit integrity result;
- DAST result and report hash;
- operator/reviewer;
- final PASS/FAIL.

## Final rule

This gate remains `FAIL/PENDING` if any required timestamp, integrity check or external evidence is missing. Real money remains **NO-GO** until this gate and all external legal/provider/pentest gates are independently approved.
