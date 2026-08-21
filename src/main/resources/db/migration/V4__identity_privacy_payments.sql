ALTER TABLE members ADD COLUMN kyc_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED';

CREATE TABLE member_private_data (
 member_id UUID PRIMARY KEY REFERENCES members(id),
 cpf_hash VARCHAR(64),
 cpf_last4 VARCHAR(4),
 address_redacted VARCHAR(255),
 updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE kyc_verifications (
 id UUID PRIMARY KEY,
 member_id UUID NOT NULL REFERENCES members(id),
 status VARCHAR(20) NOT NULL,
 provider VARCHAR(40) NOT NULL,
 external_reference VARCHAR(120),
 reviewed_by_user_id UUID REFERENCES app_users(id),
 rejection_reason VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_kyc_member_created ON kyc_verifications(member_id, created_at DESC);

CREATE TABLE consent_records (
 id UUID PRIMARY KEY,
 member_id UUID NOT NULL REFERENCES members(id),
 consent_type VARCHAR(40) NOT NULL,
 document_version VARCHAR(40) NOT NULL,
 accepted BOOLEAN NOT NULL,
 ip_prefix VARCHAR(80),
 accepted_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_consent_version UNIQUE(member_id, consent_type, document_version)
);

ALTER TABLE aid_documents ADD COLUMN expires_at TIMESTAMPTZ;
ALTER TABLE aid_documents ADD COLUMN logically_expired BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE payment_attempts (
 id UUID PRIMARY KEY,
 aid_request_id UUID NOT NULL REFERENCES aid_requests(id),
 idempotency_key VARCHAR(100) NOT NULL UNIQUE,
 provider VARCHAR(40) NOT NULL,
 provider_reference VARCHAR(120) UNIQUE,
 status VARCHAR(40) NOT NULL,
 amount NUMERIC(19,2) NOT NULL CHECK(amount > 0),
 initiated_by_user_id UUID NOT NULL REFERENCES app_users(id),
 failure_reason VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uk_payment_settled_per_aid ON payment_attempts(aid_request_id) WHERE status='SETTLED';

CREATE TABLE webhook_events (
 id UUID PRIMARY KEY,
 provider VARCHAR(40) NOT NULL,
 event_id VARCHAR(120) NOT NULL UNIQUE,
 payload_sha256 VARCHAR(64) NOT NULL,
 event_timestamp TIMESTAMPTZ NOT NULL,
 received_at TIMESTAMPTZ NOT NULL,
 processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE reconciliation_records (
 id UUID PRIMARY KEY,
 payment_attempt_id UUID NOT NULL REFERENCES payment_attempts(id),
 idempotency_key VARCHAR(100) NOT NULL UNIQUE,
 expected_status VARCHAR(40) NOT NULL,
 observed_status VARCHAR(40) NOT NULL,
 result VARCHAR(40) NOT NULL,
 created_by_user_id UUID NOT NULL REFERENCES app_users(id),
 created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_events (
 id UUID PRIMARY KEY,
 aggregate_type VARCHAR(80) NOT NULL,
 aggregate_id UUID,
 event_type VARCHAR(80) NOT NULL,
 payload TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 published_at TIMESTAMPTZ,
 attempts INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_outbox_pending ON outbox_events(published_at, created_at);
