# Provider Sandbox Technical Evidence

Status: **TECHNICAL SANDBOX GATE PASS / PRODUCTION HOMOLOGATION PENDING**

This document records repository-backed evidence for the internal sandbox payment-provider contract. It does not claim contractual, compliance, regulatory or production-provider approval.

## Provider model currently implemented

The current implementation uses an internal `SandboxPaymentProvider` behind the `PaymentProvider` contract. This sandbox is a deterministic test double used to validate the application-side integration model before a real external provider is selected and homologated.

Therefore:

- sandbox technical behavior can be validated for free in CI;
- no real payment rail or production provider is being claimed;
- production provider identity remains `PENDING`;
- `external_provider_homologation` remains `PENDING`.

## Automated homologation gate

Workflow: `.github/workflows/provider-homologation.yml`

The workflow runs the following focused test suite on pull requests and on `main`:

- `SandboxPaymentProviderContractTest`
- `PaymentServiceTest`
- `WebhookSignatureServiceTest`
- `PaymentServiceReconciliationTest`

This gate validates provider-contract behavior, retry/idempotency behavior, webhook authentication constraints and reconciliation safety.

## Verified technical controls

### Idempotent initiation

`SandboxPaymentProviderContractTest.sameIdempotencyKeyReturnsSameProviderReference` verifies that repeated initiation using the same idempotency key returns the same provider reference and provider request ID.

Decision: **PASS for internal provider contract**.

### Provider status is externally authoritative

`SandboxPaymentProviderContractTest.statusCanBeQueriedAndEvolvesExternally` verifies that status is queried through the provider abstraction and can evolve independently from the application-side initiation call.

Decision: **PASS for sandbox contract semantics**.

### Retry preserves idempotency key

`SandboxPaymentProviderContractTest.gatewayRetriesTransientFailureWithSameIdempotencyKey` verifies a transient provider failure is retried up to the configured limit while preserving the same idempotency key. The test succeeds on the third call and confirms the same request identity.

Decision: **PASS for application-side retry contract**.

Real-provider timeout, retry-after and rate-limit semantics remain provider-specific and `PENDING`.

### Webhook signature validation

`WebhookSignatureServiceTest` verifies:

- a valid signature is accepted;
- invalid, empty or missing signatures are rejected;
- stale signed events outside the accepted timestamp window are rejected;
- future events outside the accepted clock-skew window are rejected;
- body tampering invalidates the signature;
- missing or insufficient signing material fails closed.

Decision: **PASS for internal webhook-authentication contract**.

Real-provider signature algorithm, header names, key rotation and delivery guarantees remain `PENDING` until provider selection.

### Reconciliation cannot settle by acknowledgement

`PaymentServiceReconciliationTest.acknowledgementIsAuditedAndNeverSettlesPayment` verifies that acknowledging a reconciliation case keeps the payment in `RECONCILIATION_REQUIRED` and creates an audit event instead of marking the payment as settled.

The companion test rejects reconciliation acknowledgement for a payment not already in reconciliation state.

Decision: **PASS for reconciliation safety invariant**.

## What this gate proves

The repository currently proves, at application-contract level:

1. stable idempotency behavior;
2. retry without changing request identity;
3. provider status queried through an external-authority abstraction;
4. signed webhook verification with timestamp freshness controls;
5. tamper rejection and fail-closed signature behavior;
6. reconciliation acknowledgement cannot fabricate settlement;
7. the focused provider homologation test suite is continuously executed by CI.

## What remains unknown until a real provider is selected

The following must not be marked PASS from the internal sandbox alone:

- production provider legal identity;
- official API base URLs and supported payment rails;
- authentication mechanism and production credential lifecycle;
- exact idempotency contract and retention period;
- documented timeout and retry recommendations;
- rate limits and HTTP retry semantics;
- provider-specific webhook signature format and key rotation;
- webhook redelivery schedule and maximum delivery age;
- settlement/failure/refund/cancellation state mapping;
- reconciliation/reporting API or files;
- sandbox-versus-production behavioral differences;
- KYB/KYC onboarding requirements;
- contracts, fees, SLAs and support/escalation channels;
- regulatory/compliance suitability for the project's production operating model.

## Current decision

`internal_sandbox_contract=PASS`

`provider_retry_idempotency_contract=PASS`

`internal_webhook_security_contract=PASS`

`internal_reconciliation_safety=PASS`

`real_provider_selected=PENDING`

`real_provider_sandbox_validation=PENDING`

`provider_contract_compliance=PENDING`

`external_provider_homologation=PENDING`

`production=NO-GO`

The next free-first step is to select a provider that offers a no-cost sandbox/documentation path, map its documented contract against `docs/PROVIDER_HOMOLOGATION_PACKAGE.md`, and only then implement a provider-specific adapter. No production credentials are required for that step.