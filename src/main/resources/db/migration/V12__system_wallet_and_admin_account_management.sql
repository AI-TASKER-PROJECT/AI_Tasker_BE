CREATE TABLE IF NOT EXISTS system_wallet (
    system_wallet_id BIGSERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_id BIGINT,
    deposited_business_count INT NOT NULL DEFAULT 0,
    successful_deposit_count INT NOT NULL DEFAULT 0,
    total_revenue NUMERIC(18, 2) NOT NULL DEFAULT 0,
    holding_balance NUMERIC(18, 2) NOT NULL DEFAULT 0,
    disputed_balance NUMERIC(18, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    last_synced_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_system_wallet_account FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_system_wallet_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
    CONSTRAINT chk_system_wallet_non_negative CHECK (
        deposited_business_count >= 0
        AND successful_deposit_count >= 0
        AND total_revenue >= 0
        AND holding_balance >= 0
        AND disputed_balance >= 0
    )
);

CREATE INDEX IF NOT EXISTS idx_system_wallet_account_id ON system_wallet(account_id);
CREATE INDEX IF NOT EXISTS idx_system_wallet_transaction_id ON system_wallet(transaction_id);
