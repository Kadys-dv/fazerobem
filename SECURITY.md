# Security Policy

Fazer o Bem is currently a sandbox/staging engineering project. Real-money operation remains explicitly out of scope until the external evidence gate is completed.

## Responsible disclosure

Do not publish exploitable vulnerabilities, credentials, PII or financial test data in public issues. Report concerns privately to the repository owner with affected component, reproduction steps, impact and mitigation suggestions.

## Critical review areas

Changes involving authentication, privileged access, PII/LGPD, ledger/audit chains, payment idempotency, reconciliation, webhooks, migrations, cryptographic material or provider integration require tests and explicit risk review.

## Operational boundary

No change may silently enable real PIX, custody, real contributions or production settlement. Moving that boundary requires the documented external security, legal/regulatory and provider evidence.