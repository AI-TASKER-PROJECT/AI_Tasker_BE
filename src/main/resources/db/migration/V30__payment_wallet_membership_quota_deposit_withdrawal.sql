-- Payment wallet MVP from SPEC-PAYMENT.md.
-- Keep payment_order for PayOS top-up and use wallet_transactions for real balance movement.

ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_wallet_transactions_direction;
ALTER TABLE wallet_transactions ADD CONSTRAINT chk_wallet_transactions_direction
    CHECK (direction IN ('CREDIT', 'DEBIT', 'HOLD', 'RELEASE'));

ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_wallet_transactions_balance_type;
ALTER TABLE wallet_transactions ADD CONSTRAINT chk_wallet_transactions_balance_type
    CHECK (balance_type IN ('AVAILABLE', 'ESCROW', 'HOLDING', 'DISPUTE'));

ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_wallet_transactions_status;
ALTER TABLE wallet_transactions ADD CONSTRAINT chk_wallet_transactions_status
    CHECK (status IN ('PENDING', 'POSTED', 'SUCCESS', 'FAILED', 'CANCELLED', 'VOID'));

ALTER TABLE system_wallet DROP CONSTRAINT IF EXISTS chk_system_wallet_balances_non_negative;
ALTER TABLE system_wallet ADD CONSTRAINT chk_system_wallet_balances_non_negative CHECK (
    current_balance >= 0
    AND available_balance >= 0
    AND escrow_balance >= 0
    AND holding_balance >= 0
    AND disputed_balance >= 0
);

ALTER TABLE contracts DROP CONSTRAINT IF EXISTS chk_contracts_status;
ALTER TABLE contracts ADD CONSTRAINT chk_contracts_status
    CHECK (status IN ('Draft', 'Negotiating', 'PendingDeposit', 'Active', 'Completed', 'Closed', 'Cancelled', 'Terminated'));

CREATE TABLE IF NOT EXISTS membership_packages (
    package_id BIGSERIAL PRIMARY KEY,
    role_type VARCHAR(20) NOT NULL,
    package_code VARCHAR(80) NOT NULL UNIQUE,
    package_name VARCHAR(120) NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    badge_duration_days INT NOT NULL,
    job_post_quota INT NOT NULL DEFAULT 0,
    proposal_quota INT NOT NULL DEFAULT 0,
    recommend_visibility BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_membership_packages_role CHECK (role_type IN ('BUSINESS', 'EXPERT')),
    CONSTRAINT chk_membership_packages_price CHECK (price >= 0),
    CONSTRAINT chk_membership_packages_duration CHECK (badge_duration_days > 0),
    CONSTRAINT chk_membership_packages_quota CHECK (job_post_quota >= 0 AND proposal_quota >= 0)
);

CREATE INDEX IF NOT EXISTS idx_membership_packages_role
    ON membership_packages(role_type);
CREATE INDEX IF NOT EXISTS idx_membership_packages_active
    ON membership_packages(is_active);

CREATE TABLE IF NOT EXISTS membership_purchases (
    purchase_id BIGSERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    package_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    badge_start_at TIMESTAMP NOT NULL,
    badge_end_at TIMESTAMP NOT NULL,
    wallet_transaction_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_membership_purchases_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_membership_purchases_package
        FOREIGN KEY (package_id) REFERENCES membership_packages(package_id),
    CONSTRAINT fk_membership_purchases_wallet_transaction
        FOREIGN KEY (wallet_transaction_id) REFERENCES wallet_transactions(id),
    CONSTRAINT chk_membership_purchases_amount CHECK (amount >= 0),
    CONSTRAINT chk_membership_purchases_status CHECK (status IN ('SUCCESS', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_membership_purchases_badge_range CHECK (badge_end_at >= badge_start_at)
);

CREATE INDEX IF NOT EXISTS idx_membership_purchases_account
    ON membership_purchases(account_id);
CREATE INDEX IF NOT EXISTS idx_membership_purchases_package
    ON membership_purchases(package_id);

CREATE TABLE IF NOT EXISTS user_quotas (
    quota_id BIGSERIAL PRIMARY KEY,
    account_id INT NOT NULL UNIQUE,
    job_post_quota_balance INT NOT NULL DEFAULT 0,
    proposal_quota_balance INT NOT NULL DEFAULT 0,
    badge_expired_at TIMESTAMP,
    premium_recommendation_visible BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_quotas_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT chk_user_quotas_non_negative
        CHECK (job_post_quota_balance >= 0 AND proposal_quota_balance >= 0)
);

CREATE INDEX IF NOT EXISTS idx_user_quotas_badge_expired_at
    ON user_quotas(badge_expired_at);

CREATE TABLE IF NOT EXISTS quota_usage_logs (
    quota_usage_id BIGSERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    quota_type VARCHAR(20) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    balance_before INT NOT NULL,
    balance_after INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quota_usage_logs_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT chk_quota_usage_type CHECK (quota_type IN ('JOB_POST', 'PROPOSAL')),
    CONSTRAINT chk_quota_usage_action CHECK (action_type IN ('GRANT', 'PURCHASE', 'CONSUME', 'ADJUST')),
    CONSTRAINT chk_quota_usage_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_quota_usage_balances CHECK (balance_before >= 0 AND balance_after >= 0)
);

CREATE INDEX IF NOT EXISTS idx_quota_usage_logs_account
    ON quota_usage_logs(account_id);
CREATE INDEX IF NOT EXISTS idx_quota_usage_logs_reference
    ON quota_usage_logs(reference_type, reference_id);

CREATE TABLE IF NOT EXISTS contract_deposits (
    deposit_id BIGSERIAL PRIMARY KEY,
    contract_id INT NOT NULL UNIQUE,
    business_id INT NOT NULL,
    deposit_amount NUMERIC(19, 2) NOT NULL,
    held_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    refunded_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    resolved_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    hold_transaction_id BIGINT,
    refund_transaction_id BIGINT,
    admin_id INT,
    admin_note TEXT,
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contract_deposits_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(contract_id),
    CONSTRAINT fk_contract_deposits_business
        FOREIGN KEY (business_id) REFERENCES business_profiles(business_id),
    CONSTRAINT fk_contract_deposits_hold_transaction
        FOREIGN KEY (hold_transaction_id) REFERENCES wallet_transactions(id),
    CONSTRAINT fk_contract_deposits_refund_transaction
        FOREIGN KEY (refund_transaction_id) REFERENCES wallet_transactions(id),
    CONSTRAINT fk_contract_deposits_admin
        FOREIGN KEY (admin_id) REFERENCES account(account_id),
    CONSTRAINT chk_contract_deposits_amounts CHECK (
        deposit_amount >= 0
        AND held_amount >= 0
        AND refunded_amount >= 0
        AND resolved_amount >= 0
    ),
    CONSTRAINT chk_contract_deposits_status
        CHECK (status IN ('UNPAID', 'HELD', 'PARTIALLY_REFUNDED', 'REFUNDED', 'ADMIN_RESOLVED'))
);

CREATE INDEX IF NOT EXISTS idx_contract_deposits_business
    ON contract_deposits(business_id);
CREATE INDEX IF NOT EXISTS idx_contract_deposits_status
    ON contract_deposits(status);

CREATE TABLE IF NOT EXISTS withdrawal_requests (
    withdrawal_id BIGSERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    wallet_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    bank_name VARCHAR(120) NOT NULL,
    bank_account_number VARCHAR(80) NOT NULL,
    bank_account_holder VARCHAR(160) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    hold_transaction_id BIGINT,
    review_transaction_id BIGINT,
    admin_id INT,
    admin_note TEXT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_withdrawal_requests_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_withdrawal_requests_wallet
        FOREIGN KEY (wallet_id) REFERENCES system_wallet(system_wallet_id),
    CONSTRAINT fk_withdrawal_requests_hold_transaction
        FOREIGN KEY (hold_transaction_id) REFERENCES wallet_transactions(id),
    CONSTRAINT fk_withdrawal_requests_review_transaction
        FOREIGN KEY (review_transaction_id) REFERENCES wallet_transactions(id),
    CONSTRAINT fk_withdrawal_requests_admin
        FOREIGN KEY (admin_id) REFERENCES account(account_id),
    CONSTRAINT chk_withdrawal_requests_amount CHECK (amount > 0),
    CONSTRAINT chk_withdrawal_requests_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_withdrawal_requests_account
    ON withdrawal_requests(account_id);
CREATE INDEX IF NOT EXISTS idx_withdrawal_requests_status
    ON withdrawal_requests(status);

INSERT INTO membership_packages (
    role_type,
    package_code,
    package_name,
    price,
    badge_duration_days,
    job_post_quota,
    proposal_quota,
    recommend_visibility,
    is_active
) VALUES
('BUSINESS', 'BUSINESS_STANDARD', 'Business Standard', 200, 30, 0, 0, FALSE, TRUE),
('BUSINESS', 'BUSINESS_PLUS', 'Business Plus', 500, 60, 10, 0, FALSE, TRUE),
('BUSINESS', 'BUSINESS_PREMIUM', 'Business Premium', 1000, 90, 30, 0, TRUE, TRUE),
('EXPERT', 'EXPERT_STANDARD', 'Expert Standard', 100, 30, 0, 0, FALSE, TRUE),
('EXPERT', 'EXPERT_PLUS', 'Expert Plus', 200, 60, 0, 30, FALSE, TRUE),
('EXPERT', 'EXPERT_PREMIUM', 'Expert Premium', 600, 90, 0, 90, FALSE, TRUE)
ON CONFLICT (package_code) DO UPDATE SET
    package_name = EXCLUDED.package_name,
    price = EXCLUDED.price,
    badge_duration_days = EXCLUDED.badge_duration_days,
    job_post_quota = EXCLUDED.job_post_quota,
    proposal_quota = EXCLUDED.proposal_quota,
    recommend_visibility = EXCLUDED.recommend_visibility,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO system_settings (
    setting_key,
    setting_value,
    value_type,
    description,
    is_active,
    created_at,
    updated_at
) VALUES
('credit.job_post.price_vnd', '200', 'DECIMAL', 'Price for one Business job-post credit in VND.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('credit.proposal.price_vnd', '100', 'DECIMAL', 'Price for one Expert proposal credit in VND.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO UPDATE SET
    setting_value = EXCLUDED.setting_value,
    value_type = EXCLUDED.value_type,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_quotas (
    account_id,
    job_post_quota_balance,
    proposal_quota_balance,
    premium_recommendation_visible,
    created_at,
    updated_at
)
SELECT
    a.account_id,
    CASE WHEN r.role_name = 'BUSINESS' THEN 0 ELSE 0 END,
    CASE WHEN r.role_name = 'EXPERT' THEN 3 ELSE 0 END,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
JOIN roles r ON r.role_id = a.role_id
WHERE r.role_name IN ('BUSINESS', 'EXPERT')
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO quota_usage_logs (
    account_id,
    quota_type,
    action_type,
    amount,
    balance_before,
    balance_after,
    reference_type,
    reference_id
)
SELECT
    a.account_id,
    'PROPOSAL',
    'GRANT',
    3,
    0,
    3,
    'INITIAL_EXPERT_GRANT',
    a.account_id
FROM account a
JOIN roles r ON r.role_id = a.role_id
WHERE r.role_name = 'EXPERT'
  AND NOT EXISTS (
      SELECT 1
      FROM quota_usage_logs q
      WHERE q.account_id = a.account_id
        AND q.quota_type = 'PROPOSAL'
        AND q.action_type = 'GRANT'
        AND q.reference_type = 'INITIAL_EXPERT_GRANT'
  );
