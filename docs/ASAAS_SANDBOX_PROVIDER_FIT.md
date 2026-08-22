# Asaas Sandbox Provider Fit

Status: **REAL PROVIDER SELECTED FOR FREE SANDBOX HOMOLOGATION / LIVE INTEGRATION NOT YET ENABLED**

## Decision

The first real external provider selected for technical sandbox homologation is **Asaas**, using its dedicated Sandbox environment only.

This selection is limited to free technical validation. It is not contractual approval, regulatory approval, production onboarding or authorization to move real funds.

## Why Asaas fits the current project

The project needs an outbound aid-payment rail with external status authority, idempotent processing, webhook-driven state changes and reconciliation. Asaas Sandbox publicly documents:

- a dedicated sandbox base URL (`https://api-sandbox.asaas.com/v3`);
- API-key authentication through the `access_token` header;
- Pix transfers to a destination Pix key;
- sandbox-only simulated Pix transfer flows without moving real funds;
- Webhooks and asynchronous status changes;
- sandbox balance simulation;
- transfer testing with successful and failed scenarios;
- use of fictitious Pix keys or a second Sandbox account for end-to-end debit/credit validation.

## Correct Asaas flow for FazerOBem

The current `PaymentInitiationService` represents **outbound aid payment**, not collection from a donor. Therefore the correct provider operation is an outbound transfer, not creation of a customer charge.

Target API operation:

`POST /v3/transfers`

Core provider data required by that API includes:

- transfer value;
- `operationType=PIX` (or provider automatic selection where deliberately allowed);
- destination `pixAddressKey`;
- description/reference used for reconciliation.

## Architectural gap discovered

The current `PaymentProvider` contract is:

`initiate(UUID paymentId, BigDecimal amount, String idempotencyKey)`

It has no destination/beneficiary payment data. `PaymentInitiationService` also creates a `PaymentAttempt` and invokes the provider using only payment ID, amount and idempotency key.

That is sufficient for the internal deterministic sandbox but **insufficient for a real outbound Pix provider** because a real transfer requires a destination Pix key or bank-account destination.

No Pix key/destination field is currently present in the repository payment flow.

### Decision

Do **not** hard-code a Pix key, do **not** overload the idempotency key, and do **not** place beneficiary financial data in logs, Git or generic audit payloads.

Before implementing an `AsaasPaymentProvider`, the project must introduce a dedicated beneficiary payout-destination abstraction with appropriate encryption/access controls.

## Proposed provider-neutral contract

The provider integration should evolve toward an explicit request object, for example conceptually:

```text
PaymentProvider.initiate(PaymentInitiationCommand command)

PaymentInitiationCommand
- paymentId
- amount
- idempotencyKey
- destinationReference
- transferMethod
```

`destinationReference` should reference protected payout-destination data rather than expose a raw Pix key throughout the domain. The adapter resolves the protected destination at the narrowest possible boundary.

The exact model must preserve:

- separation of duties;
- no plaintext financial destination in logs/outbox/audit events;
- encryption at rest;
- explicit access policy;
- idempotency and duplicate-transfer prevention;
- provider status as authoritative for settlement;
- reconciliation on unknown/ambiguous outcomes.

## Sandbox homologation plan

### Stage A — no credentials required

- [x] select real provider for sandbox evaluation: Asaas;
- [x] map outbound aid flow to Asaas transfer API;
- [x] identify authentication model;
- [x] identify Sandbox base URL;
- [x] identify Pix transfer support;
- [x] identify webhook and asynchronous-status requirements;
- [x] identify the domain-contract gap for payout destination;
- [ ] implement provider-neutral payout-destination model;
- [ ] implement Asaas adapter behind feature/config gate;
- [ ] add adapter contract tests using mocked HTTP responses;
- [ ] add explicit Asaas status mapping tests;
- [ ] add 401/403/429/5xx and timeout behavior tests;
- [ ] add webhook duplicate/replay/state-transition tests specific to Asaas semantics.

### Stage B — free Asaas Sandbox account required

- [ ] create Asaas Sandbox account;
- [ ] generate Sandbox API key;
- [ ] store key only in GitHub/hosting secret storage;
- [ ] never commit the key;
- [ ] add fictitious Sandbox balance;
- [ ] execute a transfer using an official fictitious Pix key;
- [ ] record provider transfer ID and HTTP result without secrets/PII;
- [ ] query resulting status;
- [ ] validate provider Webhook delivery;
- [ ] retry/replay safely and verify no duplicate transfer;
- [ ] simulate/observe failure and reconciliation path;
- [ ] optionally use two Sandbox accounts to validate debit and credit on both sides.

### Stage C — production remains blocked

Even after Sandbox PASS, the following remain `PENDING`:

- provider contractual/compliance homologation;
- KYB/KYC and account approval requirements;
- production fees and limits;
- production Pix/transfer enablement;
- production credential lifecycle;
- legal/LGPD/accounting review;
- independent pentest;
- final operational approval.

## Security requirements for the future Asaas adapter

1. Base URL must be environment-specific and fail closed in production.
2. Sandbox credentials must never be accepted as production credentials.
3. `access_token` must come from secret storage/environment configuration only.
4. HTTP client must use explicit connection/read timeouts.
5. Retry must be limited to safe transient conditions and must preserve request identity.
6. Ambiguous timeout outcomes must enter reconciliation rather than be blindly repeated.
7. Provider response IDs/status may be audited; secrets and raw beneficiary data may not.
8. Webhook processing must be idempotent and state transitions must not permit an event to fabricate settlement.
9. Production payment initiation remains disabled until all production gates are independently approved.

## Current gate status

`real_provider_selected=PASS_FOR_SANDBOX_EVALUATION`

`provider=ASAAS`

`provider_sandbox_environment=AVAILABLE`

`provider_api_contract_mapping=PARTIAL_PASS`

`provider_adapter=PENDING`

`provider_real_sandbox_execution=PENDING`

`external_provider_homologation=PENDING`

`production=NO-GO`
