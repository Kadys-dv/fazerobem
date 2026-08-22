-- V5 already enforces one active payment attempt per aid through
-- uk_payment_active_per_aid. This migration adds optimistic versioning
-- as an additional concurrency guard for PaymentAttempt.
ALTER TABLE payment_attempts
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
