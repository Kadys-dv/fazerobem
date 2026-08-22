ALTER TABLE payment_attempts
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX uk_payment_active_per_aid
    ON payment_attempts(aid_request_id)
    WHERE status IN ('READY', 'PROCESSING', 'SETTLED', 'RECONCILIATION_REQUIRED');
