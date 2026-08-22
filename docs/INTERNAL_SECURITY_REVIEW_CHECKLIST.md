# Internal Security Review Checklist — Free-First

Status: **INTERNAL SELF-ASSESSMENT — does not replace independent pentest**.

Use only against authorized staging environments and synthetic accounts/data.

## Authentication and session

- [ ] WebAuthn/passkey flow rejects invalid origin/RP assertions.
- [ ] Privileged paths require the intended MFA/passkey controls.
- [ ] Session cookies use Secure/HttpOnly/SameSite controls appropriate to the flow.
- [ ] Logout/session invalidation behaves as expected.
- [ ] CSRF protections are present where browser session state is used.
- [ ] CORS does not allow arbitrary origins with credentials.

## Authorization

- [ ] MEMBER cannot perform ANALYST/APPROVER/ADMIN/AUDITOR actions.
- [ ] ANALYST cannot self-approve decisions that require separation of duties.
- [ ] Object-level access checks prevent IDOR/BOLA for member, aid and document resources.
- [ ] Administrative endpoints are not exposed without authorization.
- [ ] Public transparency endpoints do not expose personal data.

## Input and application security

- [ ] API validation rejects malformed/oversized/unexpected inputs.
- [ ] Injection-oriented negative tests do not bypass application rules.
- [ ] File/document upload restrictions are tested with safe synthetic files.
- [ ] Error responses do not disclose secrets, stack traces or personal data.
- [ ] Security headers are reviewed on public endpoints.

## Financial and provider controls

- [ ] `PAYMENT_INITIATION_ENABLED=false` in staging unless an explicitly authorized sandbox rehearsal requires otherwise.
- [ ] Client/public input cannot force a transaction/payment attempt to `PAID`.
- [ ] Duplicate provider events are idempotent.
- [ ] Invalid webhook signatures fail closed.
- [ ] Stale/replayed webhook events fail according to the intended policy.
- [ ] Unknown provider states enter safe reconciliation/manual-review behavior.
- [ ] Ledger/audit integrity checks remain intact after negative tests.

## Abuse resistance

- [ ] Authentication and sensitive flows have reasonable rate/abuse controls.
- [ ] Tests remain low-volume and do not create denial of service on shared/free infrastructure.
- [ ] Enumeration responses do not disclose unnecessary account/member existence information.

## Data protection

- [ ] Logs/artifacts contain no plaintext credentials, tokens or sensitive PII.
- [ ] Sensitive identity/address/document data is protected according to the documented design.
- [ ] Least-privilege access is maintained for privileged roles.
- [ ] Backup/restore evidence is sanitized before public storage.

## Automated evidence to retain

- GitHub Actions security gates;
- SBOM/dependency evidence where available;
- internal passive DAST artifact;
- external public-staging passive DAST artifact;
- health/TLS/header evidence;
- negative provider/webhook tests;
- authorization/regression tests.

## Result template

```text
review_type=INTERNAL_SECURITY_SELF_ASSESSMENT
review_date=<YYYY-MM-DD>
commit=<SHA>
target=<authorized staging target>
critical_open=<count>
high_open=<count>
medium_open=<count>
result=<INTERNAL_PASS|WARNING|FAIL>
independent_pentest=PENDING
```

`INTERNAL_PASS` is useful engineering evidence but must never be renamed to `independent_pentest=PASS`.