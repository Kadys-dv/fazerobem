ALTER TABLE aid_requests ADD COLUMN category VARCHAR(40) NOT NULL DEFAULT 'GENERAL_EMERGENCY';
ALTER TABLE aid_requests ADD COLUMN emergency BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE aid_documents (
 id UUID PRIMARY KEY,
 aid_request_id UUID NOT NULL REFERENCES aid_requests(id),
 document_type VARCHAR(60) NOT NULL,
 file_name VARCHAR(255) NOT NULL,
 storage_key VARCHAR(255) NOT NULL UNIQUE,
 content_type VARCHAR(100) NOT NULL,
 size_bytes BIGINT NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 5242880),
 sha256 VARCHAR(64) NOT NULL,
 submitted_by_user_id UUID NOT NULL REFERENCES app_users(id),
 created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_aid_documents_request ON aid_documents(aid_request_id);
CREATE UNIQUE INDEX uk_aid_document_hash_request ON aid_documents(aid_request_id, sha256);

CREATE TABLE fraud_screenings (
 id UUID PRIMARY KEY,
 aid_request_id UUID NOT NULL UNIQUE REFERENCES aid_requests(id),
 analyst_user_id UUID NOT NULL REFERENCES app_users(id),
 status VARCHAR(30) NOT NULL,
 risk_score INTEGER NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
 flags VARCHAR(1000) NOT NULL,
 note VARCHAR(1000) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_fraud_status ON fraud_screenings(status);
