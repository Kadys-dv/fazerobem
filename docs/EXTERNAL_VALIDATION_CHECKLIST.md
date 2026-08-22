# External Validation Checklist

## Legal / accounting / regulatory / LGPD

External qualified reviewers must record:

- operating entity and jurisdiction;
- characterization of contributions and aid;
- confirmation that no investment/return/recruitment promise exists;
- terms of use and member disclosures;
- consent records and lawful bases by processing purpose;
- controller/operator/subprocessor roles;
- data inventory, minimization, retention and deletion;
- data-subject request procedure;
- security incident and breach-notification procedure;
- cross-border processing, if any;
- accounting/tax treatment;
- payment/financial-regulatory obligations and prohibitions;
- required policy/product changes before launch;
- reviewer identity, date, scope and final PASS/BLOCKED decision.

Do not store privileged legal opinions or personal/confidential data in the public repository. Store a non-sensitive reference/hash only.

## Financial provider contractual/compliance homologation

Before production credentials are requested/installed, verify:

- provider identity and authorization appropriate to the intended service;
- contracting entity and approved account owner;
- compliance/KYC/KYB requirements completed where applicable;
- supported payment flow and prohibition of unsupported custody behavior;
- API version and production base URL controlled out-of-band;
- production credential issuance, KMS/secret-manager storage, rotation and revocation;
- signed webhook algorithm, timestamp/replay rules and secret rotation;
- idempotency semantics;
- timeout/retry semantics;
- reconciliation/status API contract;
- settlement/failure/dispute behavior;
- SLA, support and escalation contacts;
- incident responsibilities;
- limits/rate limits;
- sandbox-to-production certification/cutover steps;
- contractual approval reference and date.

No API key, secret, certificate private key, confidential contract or personal contact data belongs in Git.
