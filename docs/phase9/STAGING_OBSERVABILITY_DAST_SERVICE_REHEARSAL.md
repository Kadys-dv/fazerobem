# Phase 9 — staging observability, DAST and service rehearsal

## Purpose

This block converts the existing Micrometer/Prometheus/OpenTelemetry support into an auditable staging gate, then measures process-level service recovery against the real Neon `runtime-staging` database.

Money movement remains disabled. `PAYMENT_INITIATION_ENABLED=false` is mandatory in both rehearsals.

## Observability plane

The Spring Boot management plane is bound only to `127.0.0.1` on a dedicated port (`18081` by default). The exposed management endpoints are limited to:

- `health`;
- `prometheus`;
- `metrics`.

Health details remain disabled. The public application port does not expose the management plane.

## Out-of-process health probe

`staging-observability-dast` starts the application with the `staging` profile and real Neon staging database, then probes the application from a separate container boundary. The same container boundary must not be able to reach the management port.

This is evidence of network/process isolation inside the CI staging rehearsal. It is **not** evidence of an internet-hosted external health check. That remains `PENDING` until a real hosted staging URL exists.

## DAST

OWASP ZAP Baseline is executed as a passive scan against the staging-profile application. Medium or high risk findings fail the workflow. The JSON and HTML reports are retained as GitHub Actions artifacts for 30 days.

This is an internal DAST rehearsal and must not be described as an independent penetration test.

## Service recovery rehearsal

`staging-service-rehearsal`:

1. starts the full Spring Boot service against Neon staging and Redis;
2. confirms readiness from a separate container boundary;
3. terminates the application process and confirms the outage;
4. starts the service again with the same ephemeral runtime secrets;
5. measures process-level service RTO;
6. verifies management health after recovery;
7. verifies Flyway V1–V8, ledger chain lock and audit chain lock;
8. verifies Redis remains reachable;
9. uploads the evidence artifact.

The current objective is `service_rto_seconds <= 1800`.

## Evidence boundaries

A green result proves a CI-hosted staging-profile process restart against the real Neon staging database. It does not prove multi-node failover, DNS/CDN recovery, load-balancer failover, internet availability, provider failover, independent pentest, legal approval or production readiness for real money.

Those external items remain fail-closed and `PENDING`.
