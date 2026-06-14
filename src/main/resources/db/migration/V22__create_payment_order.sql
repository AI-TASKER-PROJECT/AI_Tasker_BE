CREATE TABLE IF NOT EXISTS payment_order (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    job_id BIGINT NULL,
    milestone_id BIGINT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'VNPAY',
    vnp_txn_ref VARCHAR(100) UNIQUE NOT NULL,
    vnp_transaction_no VARCHAR(100),
    vnp_response_code VARCHAR(20),
    vnp_secure_hash TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_payment_order_business_id
    ON payment_order(business_id);

CREATE INDEX IF NOT EXISTS idx_payment_order_status
    ON payment_order(status);

CREATE INDEX IF NOT EXISTS idx_payment_order_vnp_txn_ref
    ON payment_order(vnp_txn_ref);
