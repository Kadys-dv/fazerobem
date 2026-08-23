# Asaas Sandbox end-to-end homologation

Status: **SANDBOX ONLY — NO-GO for production / real money**.

## Already evidenced

- Render staging is publicly healthy.
- `PAYMENT_PROVIDER_TYPE=asaas-sandbox` loads the Asaas adapter.
- The sanitized authentication probe returned `{"auth":"PASS","provider":"ASAAS_SANDBOX"}` against the real Asaas Sandbox account.
- Production remains fail-closed unless payment initiation is explicitly enabled.
- Pix destinations are encrypted before persistence and are only decrypted into a short-lived redacted object for the provider call.
- Transfer creation is deliberately never retried automatically.

## Safety rules

1. Never store an Asaas API key, raw Pix key or webhook secret in Git, issues, logs or audit payloads.
2. Use only the official Sandbox base URL: `https://api-sandbox.asaas.com/v3`.
3. Use only Sandbox credentials (`$aact_hmlg_...`).
4. Keep real-money/production activation blocked.
5. A timeout/network failure during transfer creation is an **uncertain result**, not permission to submit another POST.
6. A repeated application request must return the existing payment attempt through its idempotency key.
7. Only an explicit provider rejection (HTTP 4xx) may be treated as a definitive initiation failure.

## Free Sandbox prerequisites

Asaas provides fictitious Pix keys for homologation and allows Sandbox balance to be created by confirming a test charge. No real funds are moved.

Before the first transfer:

- create enough fictitious Sandbox balance for the test;
- choose an official fictitious Pix key from the Asaas transfer-testing documentation, or a Pix key from another Sandbox account;
- register that destination against a test member through the admin destination API;
- use a test aid request that is fully approved by the existing application rules;
- keep the transfer amount minimal and clearly identified as homologation.

## Secure destination registration

Admin-only endpoint:

`PUT /api/v1/admin/payment-destinations/{memberId}/pix`

Example body (use a Sandbox-only test key):

```json
{
  "keyType": "EMAIL",
  "pixKey": "<SANDBOX_TEST_PIX_KEY>"
}
```

The API response returns only the masked destination and metadata. It must never return the raw Pix key or ciphertext.

To deactivate:

`DELETE /api/v1/admin/payment-destinations/{memberId}`

## Transfer execution

Use the normal application payment-initiation flow with:

- an approved aid request;
- an ADMIN actor who did not participate in the approvals;
- a unique `Idempotency-Key`;
- sufficient community fund balance;
- `PAYMENT_INITIATION_ENABLED=true` only in the controlled staging/Sandbox environment.

Expected application transitions:

- accepted request: `READY -> PROCESSING` with the Asaas transfer ID persisted as `providerReference`;
- explicit Asaas HTTP 4xx rejection: `READY -> FAILED` with sanitized reason `PROVIDER_REJECTED`;
- timeout/network/ambiguous response: `READY -> RECONCILIATION_REQUIRED`, preserving the original attempt and idempotency key so a duplicate POST cannot be issued.

## Verification after transfer

Validate all of the following:

- Asaas returned a transfer ID;
- the application persisted that ID without persisting the raw Pix key;
- the Sandbox transfer status can be queried;
- repeating the same application request with the same `Idempotency-Key` returns the existing attempt;
- trying a new idempotency key for the same aid while an active/reconciliation attempt exists is blocked;
- audit/outbox records contain only IDs and sanitized reasons;
- no API key or raw Pix destination appears in application logs;
- balance/status changes are consistent with the Sandbox scenario;
- webhook/reconciliation processing is idempotent.

## Evidence to retain

Record only sanitized evidence:

- GitHub commit/run IDs;
- staging URL;
- timestamp;
- application payment-attempt ID;
- Asaas transfer ID;
- masked Pix destination;
- HTTP/result status;
- final application status;
- proof that duplicate submission was blocked;
- proof that no secret/raw Pix key was present in logs/artifacts.

## Production boundary

Passing this runbook proves only technical Sandbox homologation. It does not replace independent pentesting, legal/LGPD review, contractual/compliance homologation, production credentials/KMS controls or final operational approval.
