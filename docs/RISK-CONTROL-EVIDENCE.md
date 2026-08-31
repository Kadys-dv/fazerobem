# Risk → Control → Evidence

This matrix is the short review entry point for the project's production-readiness controls. It intentionally links engineering mechanisms to concrete risks instead of treating workflow count as a maturity metric.

| Risk | Primary control | Executable evidence |
| --- | --- | --- |
| Financial state corruption under concurrency | transactional invariants, idempotency and controlled state transitions | `load-concurrency.yml`, `pilot-simulation.yml` |
| Ledger/audit tampering | chained ledger and audit records plus verification | security and production-readiness test suites |
| Privileged unilateral action | role separation, dual approval and stronger privileged authentication | `security.yml`, production-readiness tests |
| Duplicate/replayed provider events | idempotency, HMAC verification and replay/time-window controls | provider homologation and security suites |
| Provider state diverges from internal state | explicit PROCESSING/SETTLED/FAILED states and reconciliation | `operational-reconciliation.yml` |
| Staging configuration enables unsafe operation | environment validation and payment kill switch | `staging-config.yml`, `go-live-readiness.yml` |
| Infrastructure/data recovery failure | backup/restore validation and DR rehearsal | `chaos-dr.yml`, `neon-dr-rehearsal.yml` |
| External attack surface regression | dependency/code/secret checks plus staging DAST | `security.yml`, `staging-external-dast.yml` |
| Staging differs from documented readiness | controlled rehearsal with evidence package | `neon-staging-rehearsal.yml`, staging rehearsal package workflow |
| Premature real-money launch | explicit external evidence gate and NO-GO boundary | Phase 9 documentation, go-live readiness gate and human review |

## Review rule

A new workflow or security mechanism should only be added when it covers a risk that is not already adequately evidenced. Prefer extending or composing an existing gate when doing so preserves failure isolation and makes the evidence easier to understand.

## Real-money boundary

Passing automated checks is necessary but not sufficient for real-money operation. Independent security testing, legal/regulatory/LGPD review, provider contractual/compliance homologation and a real staging rehearsal remain external evidence requirements.