# Neon staging evidence — 2026-08-22

## Scope

External PostgreSQL staging resource provisioned for Phase 9 rehearsal. This evidence does **not** authorize real money and does not replace independent pentest, legal/LGPD review, or financial-provider contractual homologation.

## Resource

- Provider: Neon Postgres
- Plan intent: Free/staging
- Project: `fazerobem-staging`
- Project ID: `wandering-rain-53278285`
- Default branch: `main` (`br-orange-cell-avhs8nw5`)
- Isolated rehearsal branch: `staging-rehearsal` (`br-billowing-scene-avtx5ji1`)
- Database: `neondb`
- Credentials/connection URI: **not stored in Git**

## Verification

At `2026-08-22T16:06:07.483Z`, an authenticated query against the isolated `staging-rehearsal` branch returned:

- database: `neondb`
- role: `neondb_owner`
- branch reachable: PASS

The provider reported PostgreSQL 18.6 for the project. The connection URI supplied by Neon requires TLS parameters; the URI and password remain outside the repository.

## Isolation

The rehearsal branch is a child of the default Neon branch. Staging/rehearsal writes must target this branch and must contain only synthetic data.

## Current gate status

- external PostgreSQL resource: PASS
- isolated rehearsal database branch: PASS
- application deployed against external staging: PENDING
- Flyway schema applied by application: PENDING
- end-to-end external rehearsal: PENDING
- RTO/RPO measured end-to-end: PENDING
- independent pentest: PENDING
- legal/LGPD professional validation: PENDING
- provider contractual homologation: PENDING
- real money: NO-GO

## Security rules

1. Never commit Neon passwords or full connection strings.
2. Use environment/secret injection only.
3. Keep synthetic data only.
4. Preserve fail-closed production/staging configuration.
5. Do not mark external gates PASS from repository CI alone.
