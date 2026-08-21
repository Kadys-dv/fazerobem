ALTER TABLE app_users ADD COLUMN IF NOT EXISTS mfa_secret_enc TEXT;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP WITH TIME ZONE;

ALTER TABLE member_private_data ADD COLUMN IF NOT EXISTS cpf_ciphertext TEXT;
ALTER TABLE member_private_data ADD COLUMN IF NOT EXISTS address_ciphertext TEXT;
ALTER TABLE member_private_data ADD COLUMN IF NOT EXISTS encryption_key_id VARCHAR(80);

ALTER TABLE aid_documents ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE aid_documents ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE IF NOT EXISTS login_security_events (
 id UUID PRIMARY KEY, user_id UUID, email_hash VARCHAR(64), event_type VARCHAR(40) NOT NULL, ip_prefix VARCHAR(80), created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_login_security_events_created ON login_security_events(created_at);

CREATE TABLE IF NOT EXISTS secret_versions (
 id UUID PRIMARY KEY, secret_name VARCHAR(80) NOT NULL, key_id VARCHAR(80) NOT NULL, activated_at TIMESTAMP WITH TIME ZONE NOT NULL, retired_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_secret_versions_active ON secret_versions(secret_name) WHERE retired_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_active_per_aid
ON payment_attempts(aid_request_id)
WHERE status IN ('READY','PROCESSING','SETTLED','RECONCILIATION_REQUIRED');
