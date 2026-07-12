ALTER TABLE wallet_transactions
    ADD COLUMN IF NOT EXISTS operation_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS operation_leg VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_operation_key
    ON wallet_transactions(operation_key);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wallet_transactions_operation_key_leg
    ON wallet_transactions(operation_key, operation_leg)
    WHERE operation_key IS NOT NULL AND operation_leg IS NOT NULL;
