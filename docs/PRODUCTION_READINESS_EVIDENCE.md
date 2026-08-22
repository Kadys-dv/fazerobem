# Production Readiness Evidence

Status: **NO-GO for real money / production financial activation**.

This document consolidates the strongest repository-backed evidence currently available for Phase 9. It distinguishes verified staging/sandbox evidence from external prerequisites that remain unresolved.

## PASS — repository-backed staging evidence

### Neon staging runtime

- Spring/Flyway connected to the real Neon staging branch and validated migrations V1–V8.
- Financial schema invariants remained present after rehearsal.
- Ledger and audit chain locks remained intact.
- Payment initiation remained disabled during the rehearsals.

### Database DR rehearsal

Run: `32589010681`

- controlled synthetic-object loss and restore completed successfully;
- object-level RTO: **1 second**;
- object-level RPO: **0 seconds**;
- Flyway V1–V8 preserved;
- `ledger_chain_lock=1`;
- `audit_chain_lock=1`.

Scope limitation: this proves object-level PostgreSQL recovery in the authorized staging database, not complete platform or internet-facing recovery.

### Full service process rehearsal

Run: `32590814482`

- executable JAR built successfully;
- baseline service became reachable from a separate container boundary;
- service process was terminated and outage was confirmed;
- service restarted successfully;
- measured service RTO: **15 seconds**;
- RTO objective: **<= 1800 seconds**;
- post-recovery Flyway V1–V8: **8/8**;
- post-recovery `ledger_chain_lock=1`;
- post-recovery `audit_chain_lock=1`;
- Redis responded after recovery;
- `PAYMENT_INITIATION_ENABLED=false`.

Artifact: `staging-service-rehearsal-32590814482`

Artifact ID: `9480276469`

Artifact SHA-256: `3b9b5df51ddaa046fa177c4f587da03c5ba4a577db8a76ec90244e892c6b6932`

Scope limitation: the measured 15-second RTO is a CI-hosted staging-profile process restart using the real Neon staging database. It is not evidence of DNS/load-balancer/platform failover from the public internet.

### Observability boundary + passive DAST

Run: `32590814496`

- application endpoint was reachable from a separate container boundary;
- management plane remained bound to loopback and was not reachable from that external container boundary;
- Spring exposed exactly the configured health/prometheus/metrics management endpoints;
- OWASP ZAP baseline completed successfully against the staging-profile application;
- ZAP summary: **0 FAIL, 58 PASS, 9 WARN**;
- no Medium/High finding crossed the configured gate.

Artifact: `staging-observability-dast-32590814496`

Artifact ID: `9480289561`

Artifact SHA-256: `f7790eb96ef1836c8af3260045ead21813e58b1aaa595e6989cff12408c53781`

Observed ZAP warnings included cookie/header/application-classification items. These warnings are not promoted to PASS by omission; they remain inputs for hardening review. This DAST is automated internal evidence and **does not replace an independent penetration test**.

## PENDING — external environment evidence

The following remain pending because the repository and CI cannot independently prove them:

- public-internet health check against the deployed staging service;
- real hosting-platform restart/failover and rollback rehearsal;
- DNS/TLS/reverse-proxy/load-balancer behavior under failure;
- external monitoring/alert delivery and escalation path;
- protected backup storage, retention and restore evidence from the target hosting environment;
- independent penetration test by a qualified third party;
- legal/regulatory/accounting/LGPD validation by qualified reviewers;
- contractual/compliance homologation of the selected financial provider for production use;
- production credential issuance and KMS/secret-manager provisioning;
- final operational approval with named accountable operator/approver.

The detailed external-review requirements remain in `docs/EXTERNAL_VALIDATION_CHECKLIST.md` and `docs/PENTEST_EXTERNAL_SCOPE.md`.

## NO-GO conditions

Real-money activation remains **NO-GO** while any of the following is unresolved:

1. public staging health/failover has not been proven from outside the hosting environment;
2. independent pentest is not completed with blocking findings resolved or formally accepted;
3. legal/LGPD/regulatory/accounting review has not produced an explicit launch decision;
4. financial-provider production contracting/compliance homologation is incomplete;
5. production credentials/KMS/rotation/revocation controls are not provisioned and tested;
6. final go-live rehearsal on the actual target platform has not produced auditable evidence;
7. `PAYMENT_INITIATION_ENABLED` must remain `false` until all production gates are explicitly approved.

## Current decision

**Decision: NO-GO for real money.**

The project has strong repository-backed staging evidence for database recovery, process restart, internal observability boundaries and passive DAST. The remaining blockers are deliberately external and cannot be converted to PASS by code or CI alone.
