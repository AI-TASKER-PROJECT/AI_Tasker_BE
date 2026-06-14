CREATE TABLE IF NOT EXISTS payment_order (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT,
    business_id BIGINT,
    job_id BIGINT NULL,
    milestone_id BIGINT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'PAYOS',
    purpose VARCHAR(50) NOT NULL DEFAULT 'WALLET_TOPUP',
    provider_txn_ref VARCHAR(100) UNIQUE,
    provider_transaction_no VARCHAR(100),
    provider_response_code VARCHAR(20),
    provider_secure_hash TEXT,
    provider_order_code BIGINT,
    provider_payment_link_id VARCHAR(100),
    checkout_url TEXT,
    cancel_url TEXT,
    return_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_payment_order_business_id
    ON payment_order(business_id);

CREATE INDEX IF NOT EXISTS idx_payment_order_account_id
    ON payment_order(account_id);

CREATE INDEX IF NOT EXISTS idx_payment_order_purpose
    ON payment_order(purpose);

CREATE INDEX IF NOT EXISTS idx_payment_order_status
    ON payment_order(status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_order_provider_order_code
    ON payment_order(provider_order_code)
    WHERE provider_order_code IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payment_order_provider_payment_link_id
    ON payment_order(provider_payment_link_id);
