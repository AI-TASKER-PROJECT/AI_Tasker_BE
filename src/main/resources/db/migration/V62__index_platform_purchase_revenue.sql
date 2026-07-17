CREATE INDEX IF NOT EXISTS idx_wallet_transactions_platform_purchase_revenue
    ON wallet_transactions (transaction_type)
    WHERE status = 'POSTED'
      AND direction = 'DEBIT'
      AND balance_type = 'AVAILABLE'
      AND transaction_type IN ('MEMBERSHIP_PURCHASE', 'CREDIT_PURCHASE');
