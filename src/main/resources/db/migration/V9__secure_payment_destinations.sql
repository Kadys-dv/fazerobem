CREATE TABLE payment_destinations (
    id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    destination_type VARCHAR(20) NOT NULL,
    pix_key_type VARCHAR(20) NOT NULL,
    destination_ciphertext TEXT NOT NULL,
    destination_fingerprint VARCHAR(64) NOT NULL,
    destination_masked VARCHAR(120) NOT NULL,
    encryption_key_id VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_payment_destination_member UNIQUE (member_id),
    CONSTRAINT fk_payment_destination_member FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE INDEX idx_payment_destination_member_active
    ON payment_destinations(member_id, active);
