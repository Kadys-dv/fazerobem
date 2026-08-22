# Financial Provider Homologation Package — Free-First Preparation

Status: **PREPARED FOR REVIEW — external provider homologation remains PENDING**.

This document prepares the questions and technical evidence needed before requesting production credentials or paid services. Sandbox/free test environments should be used first whenever available.

## Product description for provider review

The service is a mutual-aid community platform. The production design must not promise investment return, yield, guaranteed profit, recruitment commission or unsupported custody behavior. Contributions and aid flows must follow the approved legal/product model.

Before production, the selected provider must confirm that the intended flow is supported under its contractual/compliance rules.

## Contracting/compliance questions

Record a non-sensitive answer/reference for each item:

| Item | Required evidence | Status |
| --- | --- | --- |
| Provider identity and regulated/authorized status appropriate to service | public/provider reference | PENDING |
| Contracting legal entity | approved account owner reference | PENDING |
| KYC/KYB onboarding | provider approval/reference | PENDING |
| Intended contribution/aid flow accepted | written provider confirmation/reference | PENDING |
| Custody/settlement model supported | provider architecture/contract reference | PENDING |
| Sandbox certification requirements | checklist/reference | PENDING |
| Production API version/base URL | controlled configuration reference | PENDING |
| Production credential issuance | secret-manager/KMS reference only | PENDING |
| Credential rotation/revocation procedure | operational procedure reference | PENDING |
| Webhook signing algorithm | provider documentation reference | PENDING |
| Webhook timestamp/replay requirements | provider documentation reference | PENDING |
| Idempotency semantics | provider documentation/test evidence | PENDING |
| Retry/timeout semantics | provider documentation/test evidence | PENDING |
| Reconciliation/status API | provider documentation/test evidence | PENDING |
| Settlement/failure/dispute lifecycle | provider documentation/reference | PENDING |
| Limits/rate limits | provider reference | PENDING |
| SLA/support/escalation | contract/support reference | PENDING |
| Incident responsibilities | contractual reference | PENDING |
| Production cutover approval | provider approval/reference and date | PENDING |

## Free sandbox engineering checks

When the provider offers a free sandbox, validate with synthetic data only:

- successful signed webhook verification;
- invalid signature rejection;
- stale/replayed webhook rejection;
- duplicate event idempotency;
- timeout and retry behavior;
- provider error mapping;
- reconciliation of pending/success/failure states;
- inability for public/client input to force a transaction to `PAID`;
- safe recovery of unknown/reconciliation-required states;
- rate-limit behavior without aggressive load;
- credential rotation procedure in staging if sandbox supports multiple keys.

Store only sanitized test evidence in GitHub Actions artifacts or documentation. Never commit sandbox secrets.

## Production credential gate

Production secrets must not be issued/installed merely because sandbox tests passed.

Required before setting production payment initiation on:

1. provider contracting/compliance homologation = PASS;
2. legal/LGPD/regulatory/accounting decision permits the intended flow;
3. independent security requirements are satisfied;
4. KMS/secret manager and rotation/revocation are provisioned;
5. target hosting/operations rehearsal is approved;
6. final accountable GO decision is recorded.

Until then:

```text
PAYMENT_INITIATION_ENABLED=false
external_provider_homologation=PENDING
production=NO-GO
```

## Provider reviewer record template

After formal homologation, store only a non-sensitive reference:

```text
review_type=FINANCIAL_PROVIDER_HOMOLOGATION
provider=<public provider name>
contracting_entity_reference=<non-sensitive reference>
review_date=<YYYY-MM-DD>
scope=<approved product/flow reference>
decision=<PASS|PASS_WITH_ACTIONS|BLOCKED>
approval_reference=<non-sensitive reference>
open_actions=<non-sensitive action IDs or NONE>
```

Contracts, personal contacts, keys, certificates and confidential compliance material remain outside the public repository.