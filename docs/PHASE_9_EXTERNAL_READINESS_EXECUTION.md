# Phase 9 — External Readiness Execution

## Status

This document is an execution gate, not evidence that production is approved.

**Current decision: NO-GO for real money.**

A track can only move to PASS when its required evidence was produced by the real target environment or an independent qualified party. CI, local simulation and self-attestation do not substitute external evidence.

## 1. SLO / RTO / RPO

Initial staging objectives to be measured, not assumed:

- API availability objective: 99.9% during the rehearsal window.
- p95 API latency objective: <= 500 ms for non-provider operational endpoints under the defined staging workload.
- payment initiation/reconciliation: measured separately because provider latency is external.
- RTO target: <= 60 minutes from declared recovery start to validated service restoration.
- RPO target: <= 5 minutes for operational data; financial ledger/audit integrity must show no unexplained loss or divergence.

Required evidence: timestamps, workload definition, Prometheus/OTel export, backup identifier/checksum, restore start/end, integrity verification, observed RTO/RPO and PASS/FAIL.

## 2. Real staging rehearsal

Use `STAGING_REHEARSAL_CHECKLIST.md` and `STAGING_REHEARSAL_EVIDENCE_TEMPLATE.md` only against the designated staging target.

Required sequence: preflight -> payment freeze -> backup -> deploy -> external health -> controlled workflow -> rollback/restore exercise -> ledger/audit validation -> evidence archive -> explicit unfreeze decision.

Never use production credentials, real PIX, real contributions or real personal data in this rehearsal.

## 3. Independent pentest

Scope must include authentication, authorization/RBAC, WebAuthn/TOTP privileged flows, IDOR/BOLA, CSRF/CORS, injection, SSRF where applicable, webhook authentication/replay, payment idempotency, PII protection, administrative recovery, rate limiting and abuse cases.

Required evidence: signed/identified third-party report, severity methodology, finding IDs, remediation commits/PRs, retest result. Critical/high findings remain blocking until accepted by an authorized risk owner or remediated and independently retested.

## 4. Legal / accounting / regulatory / LGPD validation

External counsel/qualified professionals must validate the actual operating model, including mutual-aid characterization, terms, consent, lawful bases, controller/operator roles, retention/deletion, data-subject rights, incident handling, accounting/tax treatment and whether any financial/payment regulation applies.

Required evidence: dated opinion/checklist identifying reviewer, scope, assumptions, blocking conditions and required changes. Repository documentation is preparation only and is not legal advice.

## 5. Financial provider contractual homologation

Required before production credentials: authorized provider selected, contract/compliance approval, production account ownership, SLA/support/escalation, limits, webhook requirements, reconciliation contract, incident responsibilities, credential issuance/rotation/revocation and sandbox-to-production cutover procedure.

Required evidence: provider approval reference and non-secret metadata. Never commit contracts containing confidential data, API keys, client secrets, certificates or production webhook secrets.

## 6. Release candidate

Only after the technical staging evidence is reproducible may a release candidate such as `v0.2.0-beta` be proposed. The tag must not imply real-money approval.

Release candidate evidence must include commit SHA, SBOM/security gates, migration status, rollback procedure, known risks, external-gate status and explicit `PAYMENT_INITIATION_ENABLED=false` default until final approval.

## 7. Final production gate

Real-money production requires all of the following simultaneously:

- real staging rehearsal PASS;
- measured RTO/RPO within approved objectives;
- independent pentest PASS/retest accepted;
- legal/accounting/regulatory/LGPD approval;
- provider contractual/compliance homologation;
- all required CI gates green on the release candidate;
- secrets/KMS/TLS/WebAuthn production configuration independently checked;
- monitoring/on-call/escalation operational;
- backup/restore evidence current;
- named human approvers record a GO decision.

Any missing item => **NO-GO**.

## 8. Evidence registry

Use `docs/PHASE_9_EXTERNAL_EVIDENCE_REGISTRY.md`. Evidence links may point to restricted systems; store only non-sensitive references/hashes in the public repository.
