CREATE TABLE IF NOT EXISTS account_recovery_requests (
  id UUID PRIMARY KEY,
  target_user_id UUID NOT NULL REFERENCES app_users(id),
  requested_by_user_id UUID NOT NULL REFERENCES app_users(id),
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  approved_at TIMESTAMPTZ,
  executed_at TIMESTAMPTZ,
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS account_recovery_approvals (
  id UUID PRIMARY KEY,
  recovery_request_id UUID NOT NULL REFERENCES account_recovery_requests(id),
  approver_user_id UUID NOT NULL REFERENCES app_users(id),
  approved_at TIMESTAMPTZ NOT NULL,
  UNIQUE(recovery_request_id, approver_user_id)
);
CREATE INDEX IF NOT EXISTS idx_recovery_target_status ON account_recovery_requests(target_user_id, status);
CREATE INDEX IF NOT EXISTS idx_recovery_approvals_req ON account_recovery_approvals(recovery_request_id);

CREATE TABLE IF NOT EXISTS operational_alerts (
  id UUID PRIMARY KEY,
  alert_type VARCHAR(80) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  entity_type VARCHAR(80),
  entity_id UUID,
  message VARCHAR(1000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  acknowledged_at TIMESTAMPTZ,
  acknowledged_by_user_id UUID REFERENCES app_users(id)
);
CREATE INDEX IF NOT EXISTS idx_operational_alerts_open ON operational_alerts(acknowledged_at, created_at DESC);
