# Staging SLO / RTO / RPO

These are initial engineering objectives for measurement in the designated staging environment. They are not production promises or evidence of achievement.

## Service objectives

| Signal | Initial objective | Measurement evidence |
|---|---:|---|
| API availability during rehearsal window | >= 99.9% | external probes + monitoring export |
| p95 non-provider API latency under defined staging workload | <= 500 ms | histogram/export with workload definition |
| reconciliation job success | 100% of scheduled controlled rehearsal executions | job metrics + audit evidence |
| stuck `PROCESSING` items | 0 beyond approved aging threshold after reconciliation window | operational dashboard/export |
| unexplained ledger/audit divergence | 0 | integrity verification |

Provider-dependent latency must be reported separately from application latency.

## Recovery objectives

- **RTO target:** <= 60 minutes from declared recovery start until external health, critical workflow and integrity checks are validated.
- **RPO target:** <= 5 minutes for operational data.
- **Financial integrity condition:** no unexplained ledger/audit loss, duplication or divergence is acceptable, even when the time-based RPO target is met.

## Required measurement record

For each rehearsal capture:

- environment identifier (non-secret);
- commit SHA and migration version;
- workload definition;
- measurement start/end UTC timestamps;
- external health results;
- p50/p95/p99 where applicable;
- backup ID and SHA-256 checksum;
- failure declaration timestamp;
- restore/recovery start timestamp;
- service restoration timestamp;
- calculated observed RTO;
- last durable record timestamp before failure and first validated record after restore;
- calculated observed RPO;
- ledger and audit integrity results;
- operator and reviewer;
- PASS/FAIL with blocker references.

Do not mark an objective achieved from CI simulation alone. Real staging evidence is required by issue #38.
