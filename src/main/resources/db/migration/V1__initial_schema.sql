CREATE TABLE members (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL,
  joined_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE aid_requests (
  id UUID PRIMARY KEY,
  member_id UUID NOT NULL REFERENCES members(id),
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  reason VARCHAR(1000) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  decided_at TIMESTAMPTZ,
  decision_note VARCHAR(500)
);
CREATE TABLE ledger_entries (
  id UUID PRIMARY KEY,
  type VARCHAR(30) NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  member_id UUID REFERENCES members(id),
  aid_request_id UUID REFERENCES aid_requests(id),
  description VARCHAR(300) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  previous_hash VARCHAR(64) NOT NULL,
  entry_hash VARCHAR(64) NOT NULL UNIQUE
);
CREATE INDEX idx_ledger_created_at ON ledger_entries(created_at);
CREATE INDEX idx_aid_status ON aid_requests(status);
