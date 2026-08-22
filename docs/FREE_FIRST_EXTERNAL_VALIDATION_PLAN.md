# Free-First External Validation Plan

## Objective
Advance staging and production-readiness evidence while prioritizing no-cost tooling, public documentation, community review and reproducible CI evidence. Paid services are a last resort and must not be represented as completed until an independent reviewer actually performs them.

## Rules

1. Prefer free/open-source tooling and existing GitHub Actions capacity.
2. Do not weaken gates to obtain a PASS.
3. Never turn self-assessment into an `independent_pentest=PASS` or legal/LGPD approval.
4. Keep real-money activation `NO-GO` while mandatory independent/external approvals remain unresolved.
5. Never commit secrets, personal data, confidential contracts or privileged legal opinions.

## Track A — Security, zero-cost first

### Already automated
- dependency/security CI gates;
- internal staging-profile passive DAST;
- real public-staging OWASP ZAP Baseline;
- public health/TLS/header evidence;
- management-surface exposure probes.

### Additional free self-assessment
Use only on authorized staging targets and synthetic accounts:

- OWASP ZAP Baseline / passive scan;
- OWASP ASVS as a manual verification checklist;
- OWASP Web Security Testing Guide for scenario coverage;
- Semgrep Community rules where useful;
- dependency/SBOM review already present in CI;
- manual authorization checks for IDOR/BOLA, RBAC and separation of duties;
- WebAuthn/session/CSRF/CORS/header review;
- webhook replay/idempotency negative tests;
- synthetic rate-limit/abuse tests that do not create denial of service.

Self-assessment output is `INTERNAL_PASS`, `WARNING` or `FAIL`. It never substitutes for `independent_pentest`.

### Free independent-review avenues
Before paying for a commercial pentest, seek legitimate no-cost or pro-bono review through:

- local OWASP community/chapter contacts;
- university information-security labs or extension programs;
- trusted security professionals willing to review an open-source/community project;
- coordinated responsible-disclosure review after a vulnerability disclosure policy is ready.

Any independent reviewer must receive the authorized scope in `docs/PENTEST_EXTERNAL_SCOPE.md` and must not test production, real payment rails or third-party systems without explicit authorization.

## Track B — LGPD/legal preparation, zero-cost first

Repository preparation can be completed at no cost, but legal approval must remain external.

Prepare and maintain:

- data inventory by field/category;
- purpose for each processing activity;
- proposed lawful basis marked `TO_BE_VALIDATED`;
- controller/operator/subprocessor map;
- retention/deletion matrix;
- data-subject rights procedure;
- incident and breach-response procedure;
- access-control and encryption summary;
- international/cross-border processing inventory;
- list of processors/providers and what data each receives;
- draft privacy notice and terms for review.

Use official public guidance from Brazilian authorities and legislation as research input, but do not call that legal approval.

A qualified reviewer must ultimately record a non-sensitive decision reference: `PASS`, `BLOCKED` or `PASS_WITH_ACTIONS`, date and scope. Privileged legal advice remains outside the public repository.

## Track C — Financial-provider homologation, zero-cost first

Before requesting paid production services or credentials:

- use provider sandbox/free test environment when offered;
- validate API contract, idempotency, webhook signature, replay protection, retries and reconciliation with synthetic data;
- keep `PAYMENT_INITIATION_ENABLED=false`;
- document expected custody/settlement model;
- verify that the intended product flow does not silently create unsupported custody, investment, yield or recruitment behavior;
- prepare the contractual/compliance questions from `docs/EXTERNAL_VALIDATION_CHECKLIST.md`.

Provider production homologation remains `PENDING` until the selected provider formally accepts the contracting entity/use case and issues the required production approval/credentials.

## Track D — Hosting/operations, zero-cost first

Using the current free staging stack where feasible:

- repeat external `/health` checks from GitHub Actions;
- retain DAST evidence artifacts;
- rehearse application deploy/rollback without real-money activation;
- rehearse database recovery only in authorized staging branches;
- document cold-start behavior of free hosting separately from failure recovery;
- maintain backup/restore instructions and checksums;
- record measured service recovery times honestly without renaming them disaster-recovery RTO when the scope is narrower.

Do not purchase uptime/monitoring services until free checks are insufficient for the next gate.

## Evidence states

- `PASS`: objective gate passed with appropriate evidence for that gate.
- `INTERNAL_PASS`: project self-assessment passed but independence is required for the final gate.
- `WARNING`: non-blocking issue that remains tracked.
- `PENDING`: required external or independent validation not yet completed.
- `BLOCKED`: reviewer or evidence found a launch blocker.
- `NO-GO`: production/real-money activation is not authorized.

## Current free-first order of work

1. Keep public staging health + passive DAST reproducible.
2. Complete repository data inventory and LGPD evidence matrix.
3. Complete provider-homologation questionnaire/package without production secrets.
4. Expand internal security checklist using OWASP ASVS/WSTG concepts.
5. Seek no-cost independent review/pro-bono opportunities.
6. Pay for an independent service only if required evidence cannot reasonably be obtained through a qualified no-cost reviewer.

## Production decision

Free tooling can provide strong engineering evidence, but it does not remove the need for independent approval where the risk model requires independence. Real-money production remains `NO-GO` until the mandatory external gates in `docs/PRODUCTION_READINESS_EVIDENCE.md` are resolved.