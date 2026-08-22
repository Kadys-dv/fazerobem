# Neon runtime staging evidence — 2026-08-22

## Scope

This evidence records the real GitHub Actions rehearsal executed against the isolated Neon `runtime-staging` branch. It does not authorize production or real-money movement.

## Execution

- Workflow: `neon-staging-rehearsal`
- GitHub Actions run: `32587344968`
- Result: `success`
- Target database: Neon `neondb`
- Target branch: `runtime-staging`
- Payment initiation: disabled
- Test data policy: synthetic only

## Verified steps

The workflow completed all of the following successfully:

1. Redis service initialization.
2. Repository checkout and Java 21 setup.
3. Presence validation for the three Neon staging repository secrets.
4. Ephemeral application secret generation.
5. Spring Boot startup with the `staging` profile against Neon.
6. Flyway application/validation for V1 through V8.
7. Financial schema checks, including critical indexes and chain-lock tables.
8. Application cleanup and Redis/container shutdown.

The failure-log step was skipped because the run had no failure.

## Security properties

- Database credentials remained in GitHub Actions secrets and were not committed.
- JDBC configuration required TLS.
- Runtime cryptographic/application test secrets were ephemeral.
- `PAYMENT_INITIATION_ENABLED=false` remained enforced.
- No real personal data or real provider transaction was introduced.

## Readiness interpretation

**PASS:** application-to-Neon connectivity, Spring Boot startup, Redis dependency, Flyway runtime migration path, and core financial schema verification.

**NOT YET PROVEN:** external public staging endpoint, measured RTO/RPO, full rollback/restore rehearsal, external DAST/pentest, legal/LGPD approval, contractual provider homologation, and real-money readiness.

Production with real money remains **NO-GO**.
