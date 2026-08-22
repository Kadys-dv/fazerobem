# LGPD Evidence Matrix — Pre-Review

Status: **INTERNAL PREPARATION ONLY — legal/LGPD approval remains PENDING**.

This matrix organizes evidence for qualified review without claiming legal approval. Proposed lawful bases and retention periods must be validated before production.

| Processing area | Data / category | Purpose | Proposed basis / status | Minimization / protection | Retention | External validation |
| --- | --- | --- | --- | --- | --- | --- |
| Account/authentication | identifiers, credential metadata, MFA/passkey metadata | create account, authenticate, prevent account takeover | TO_BE_VALIDATED | secrets not logged; privileged MFA; passkey/WebAuthn controls | TO_BE_DEFINED | PENDING |
| Member profile | identity/contact data | operate community membership and communicate with member | TO_BE_VALIDATED | expose only required fields; encrypt sensitive fields | TO_BE_DEFINED | PENDING |
| CPF/identity attributes | CPF and identity-related fields | duplicate/fraud prevention and qualified identity checks where justified | TO_BE_VALIDATED | encrypted representation plus constrained derived values; no plaintext in logs | TO_BE_DEFINED | PENDING |
| Address | address data | eligibility/contact/document support where required | TO_BE_VALIDATED | encrypted at rest; redact common responses | TO_BE_DEFINED | PENDING |
| Aid request | category, justification, amount, status | evaluate and operate mutual-aid request | TO_BE_VALIDATED | collect only information needed for rule/eligibility decision | TO_BE_DEFINED | PENDING |
| Supporting documents | uploaded documents and associated metadata | evidence for aid eligibility when required | TO_BE_VALIDATED | limited-role access; scheduled document lifecycle; auditable access history | policy exists technically; final period TO_BE_DEFINED | PENDING |
| Fraud/security | audit/security events, IP/device/session metadata where collected | abuse prevention, investigation and security | TO_BE_VALIDATED | purpose-limited access; avoid payload/secret/PII leakage | TO_BE_DEFINED | PENDING |
| Financial ledger/audit | contribution/aid transaction metadata and immutable audit records | transparency, reconciliation, integrity and accountability | TO_BE_VALIDATED with accounting/legal input | integrity controls, separation of duties, append-only/audit controls | TO_BE_DEFINED with accounting/legal input | PENDING |
| Webhooks/provider integration | provider identifiers/status, signed event metadata | synchronize sandbox/provider transaction states | TO_BE_VALIDATED | signature/replay/idempotency controls; no production secret in Git | TO_BE_DEFINED | PENDING |
| Incident response | incident records and affected-data metadata | detect, investigate, remediate and notify where required | legal obligation/legitimate-security rationale TO_BE_VALIDATED | need-to-know access and sanitized evidence | TO_BE_DEFINED | PENDING |

## Data-subject rights readiness

Internal requirements to maintain before review:

- confirmation of treatment;
- access/correction workflow;
- deletion/anonymization assessment where legally possible;
- information on sharing/processors;
- consent revocation when consent is actually the lawful basis;
- auditable request lifecycle;
- identity verification that does not collect excessive new data.

Status: `INTERNAL_PREPARED`, final legal validation `PENDING`.

## Controller / operator / subprocessors

The following must be completed out-of-band with actual contracting information before production:

- legal controller identity: `PENDING`;
- operators/processors and contractual roles: `PENDING`;
- Neon/PostgreSQL hosting role and data location: `TO_BE_REVIEWED`;
- Render hosting role and data location: `TO_BE_REVIEWED`;
- Upstash/Redis role and data location: `TO_BE_REVIEWED`;
- financial-provider role: `PENDING_PROVIDER_SELECTION_OR_HOMOLOGATION`;
- any analytics/email/monitoring processor: `TO_BE_DECLARED_IF_USED`.

Do not place confidential contracts, credentials or personal contact information in this public matrix.

## Retention / deletion decisions still required

Qualified legal/accounting review must set explicit periods for at least:

- inactive member/account data;
- rejected/completed aid requests;
- identity/supporting documents;
- security/audit logs;
- financial ledger/reconciliation records;
- incident evidence;
- backups containing personal data.

Until those periods are approved, production remains `NO-GO`.

## Cross-border processing

Inventory the actual regions/providers and document safeguards/contractual basis before production. Current engineering deployment information is evidence for review, not a legal conclusion.

Status: `PENDING_EXTERNAL_REVIEW`.

## Reviewer record template

Store only a non-sensitive record in Git after review:

```text
review_type=LGPD_LEGAL
reviewer_reference=<non-sensitive reference>
review_date=<YYYY-MM-DD>
scope=<document/version/reference>
decision=<PASS|PASS_WITH_ACTIONS|BLOCKED>
open_actions=<non-sensitive action IDs or NONE>
```

Privileged legal advice and personal/confidential reviewer data must remain outside the public repository.