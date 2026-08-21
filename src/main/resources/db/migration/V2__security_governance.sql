ALTER TABLE aid_requests ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
CREATE TABLE app_users (
 id UUID PRIMARY KEY, email VARCHAR(180) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
 role VARCHAR(20) NOT NULL, member_id UUID REFERENCES members(id), enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE aid_analyses (
 id UUID PRIMARY KEY, aid_request_id UUID NOT NULL REFERENCES aid_requests(id), analyst_user_id UUID NOT NULL REFERENCES app_users(id), opinion VARCHAR(1000) NOT NULL, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE aid_approvals (
 id UUID PRIMARY KEY, aid_request_id UUID NOT NULL REFERENCES aid_requests(id), approver_user_id UUID NOT NULL REFERENCES app_users(id), note VARCHAR(500) NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_aid_approval_request_user UNIQUE(aid_request_id,approver_user_id)
);
CREATE TABLE aid_payments (
 id UUID PRIMARY KEY, idempotency_key VARCHAR(100) NOT NULL UNIQUE, aid_request_id UUID NOT NULL UNIQUE REFERENCES aid_requests(id), paid_by_user_id UUID NOT NULL REFERENCES app_users(id), created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE audit_events (
 id UUID PRIMARY KEY, actor_user_id UUID REFERENCES app_users(id), action VARCHAR(80) NOT NULL, entity_type VARCHAR(80) NOT NULL, entity_id UUID,
 metadata TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL, previous_hash VARCHAR(64) NOT NULL, event_hash VARCHAR(64) NOT NULL UNIQUE
);
CREATE INDEX idx_approvals_aid ON aid_approvals(aid_request_id);
CREATE INDEX idx_audit_created ON audit_events(created_at);
