-- Spring Security WebAuthn persistence (PostgreSQL-compatible)
CREATE TABLE IF NOT EXISTS user_entities (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  display_name VARCHAR(255) NOT NULL
);
CREATE TABLE IF NOT EXISTS user_credentials (
  credential_id VARCHAR(1000) PRIMARY KEY,
  user_entity_user_id VARCHAR(1000) NOT NULL,
  public_key BYTEA NOT NULL,
  signature_count BIGINT,
  uv_initialized BOOLEAN,
  backup_eligible BOOLEAN NOT NULL,
  authenticator_transports VARCHAR(1000),
  public_key_credential_type VARCHAR(100),
  backup_state BOOLEAN NOT NULL,
  attestation_object BYTEA,
  attestation_client_data_json BYTEA,
  created TIMESTAMP WITH TIME ZONE,
  last_used TIMESTAMP WITH TIME ZONE,
  label VARCHAR(1000) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_user_credentials_user ON user_credentials(user_entity_user_id);

CREATE TABLE IF NOT EXISTS dsar_requests (
  id UUID PRIMARY KEY,
  member_id UUID NOT NULL REFERENCES members(id),
  request_type VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  requested_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ,
  processed_by_user_id UUID REFERENCES app_users(id),
  notes VARCHAR(1000),
  export_sha256 VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS idx_dsar_member_created ON dsar_requests(member_id, requested_at DESC);

CREATE TABLE IF NOT EXISTS key_rotation_events (
  id UUID PRIMARY KEY,
  from_key_id VARCHAR(120),
  to_key_id VARCHAR(120) NOT NULL,
  records_reencrypted INTEGER NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS signed_transparency_reports (
  id UUID PRIMARY KEY,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  payload_json TEXT NOT NULL,
  payload_sha256 VARCHAR(64) NOT NULL,
  signature_base64 TEXT NOT NULL,
  signing_key_id VARCHAR(120) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  UNIQUE(period_start, period_end)
);

CREATE TABLE IF NOT EXISTS ledger_chain_lock (id INTEGER PRIMARY KEY);
INSERT INTO ledger_chain_lock(id) VALUES (1) ON CONFLICT DO NOTHING;
CREATE TABLE IF NOT EXISTS audit_chain_lock (id INTEGER PRIMARY KEY);
INSERT INTO audit_chain_lock(id) VALUES (1) ON CONFLICT DO NOTHING;
