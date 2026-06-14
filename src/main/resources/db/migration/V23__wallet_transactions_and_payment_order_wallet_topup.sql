CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    system_wallet_id BIGINT NOT NULL,
    account_id INT NOT NULL,
    payment_order_id BIGINT,
    transaction_type VARCHAR(50) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    balance_type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_before NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED',
    reference_type VARCHAR(50),
    reference_id BIGINT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_transactions_system_wallet
        FOREIGN KEY (system_wallet_id) REFERENCES system_wallet(system_wallet_id),
    CONSTRAINT fk_wallet_transactions_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_wallet_transactions_payment_order
        FOREIGN KEY (payment_order_id) REFERENCES payment_order(id),
    CONSTRAINT chk_wallet_transactions_amount_positive
        CHECK (amount > 0),
    CONSTRAINT chk_wallet_transactions_direction
        CHECK (direction IN ('CREDIT', 'DEBIT')),
    CONSTRAINT chk_wallet_transactions_balance_type
        CHECK (balance_type IN ('AVAILABLE', 'ESCROW')),
    CONSTRAINT chk_wallet_transactions_status
        CHECK (status IN ('PENDING', 'POSTED', 'VOID'))
);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_account_id
    ON wallet_transactions(account_id);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_payment_order_id
    ON wallet_transactions(payment_order_id);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_type
    ON wallet_transactions(transaction_type);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_created_at
    ON wallet_transactions(created_at);
