# Release Candidate and Final Production Gate

## `v0.2.0-beta` eligibility

A beta release candidate may be proposed only when the designated staging target has produced reproducible technical evidence for health, controlled workflow, backup/restore and measured recovery objectives.

The beta remains sandbox/staging and must default to real payment initiation disabled.

Required release record:
- exact commit SHA;
- CI gate results;
- SBOM/security scan references;
- database migration version;
- staging rehearsal evidence reference;
- observed SLO/RTO/RPO;
- rollback procedure;
- known risks;
- external evidence registry snapshot;
- explicit statement that real money is NO-GO unless the final gate is separately approved.

## Final real-money gate

Decision is fail-closed.

`GO` is permitted only when every item below is PASS/current:

1. real staging rehearsal;
2. measured RTO/RPO;
3. independent pentest and required retests;
4. legal/accounting/regulatory/LGPD validation;
5. financial provider contractual/compliance homologation;
6. release-candidate CI/security gates;
7. production KMS/secrets/TLS/WebAuthn configuration review;
8. monitoring, alerting and incident escalation;
9. current backup/restore evidence;
10. explicit named human approvals.

No automated job, administrator UI action or provider callback may independently convert this governance decision to GO.
