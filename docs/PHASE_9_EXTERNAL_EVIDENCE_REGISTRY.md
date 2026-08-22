# Phase 9 — External Evidence Registry

This registry records status and non-sensitive evidence references. It must never contain credentials, tokens, private contracts, personal data, pentest exploit details that increase attack risk, or production secrets.

| Gate | Status | Evidence owner | Evidence reference | Date | Result / blocker |
|---|---|---|---|---|---|
| SLO/RTO/RPO measurement | PENDING | TBD | TBD | — | Requires designated staging target |
| Real staging rehearsal | PENDING | TBD | TBD | — | Requires accessible real staging |
| Independent pentest | PENDING | Independent assessor | TBD | — | Requires contracted/authorized assessor |
| Legal/accounting/regulatory/LGPD | PENDING | Qualified external reviewers | TBD | — | Requires professional external validation |
| Provider contractual homologation | PENDING | Provider + project owner | TBD | — | Requires provider/compliance contract |
| `v0.2.0-beta` release candidate | BLOCKED | Project owner | TBD | — | Blocked pending technical staging evidence |
| Final real-money production gate | NO-GO | Named human approvers | TBD | — | All external gates must PASS |

## Evidence rules

1. `PASS` requires verifiable evidence, not a narrative assertion.
2. CI can support a gate but cannot replace independent/external evidence.
3. Store hashes/IDs and restricted-system references instead of confidential documents.
4. A failed or expired evidence item returns the dependent gate to `PENDING`/`NO-GO`.
5. Production money movement remains disabled until the final gate records an explicit GO by named authorized humans.
