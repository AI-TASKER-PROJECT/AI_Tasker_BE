ALTER TABLE invoices DROP CONSTRAINT IF EXISTS fk_invoices_transaction;
DROP TABLE IF EXISTS invoices;

ALTER TABLE system_settings DROP CONSTRAINT IF EXISTS fk_system_settings_updated_by;
ALTER TABLE system_settings RENAME COLUMN updated_by TO updated_by_role_id;
ALTER TABLE system_settings
    ADD CONSTRAINT fk_system_settings_updated_by_role
    FOREIGN KEY (updated_by_role_id) REFERENCES roles(role_id);

ALTER TABLE milestones ADD COLUMN IF NOT EXISTS job_id INT;
UPDATE milestones m
SET job_id = c.job_id
FROM contracts c
WHERE m.contract_id = c.contract_id
  AND m.job_id IS NULL;

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS fk_milestones_contract;
ALTER TABLE milestones ALTER COLUMN contract_id DROP NOT NULL;
ALTER TABLE milestones ALTER COLUMN job_id SET NOT NULL;
ALTER TABLE milestones
    ADD CONSTRAINT fk_milestones_job
    FOREIGN KEY (job_id) REFERENCES jobs(job_id);

DROP INDEX IF EXISTS idx_milestones_contract_id;
CREATE INDEX IF NOT EXISTS idx_milestones_job_id ON milestones(job_id);

ALTER TABLE system_wallet ADD COLUMN IF NOT EXISTS role_id INT;
ALTER TABLE system_wallet ADD COLUMN IF NOT EXISTS wallet_type VARCHAR(30);
ALTER TABLE system_wallet ADD COLUMN IF NOT EXISTS current_balance NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE system_wallet ADD COLUMN IF NOT EXISTS available_balance NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE system_wallet ADD COLUMN IF NOT EXISTS escrow_balance NUMERIC(18, 2) NOT NULL DEFAULT 0;

UPDATE system_wallet sw
SET role_id = a.role_id,
    wallet_type = CASE
        WHEN r.role_name = 'ADMIN' THEN 'ADMIN_SYSTEM'
        WHEN r.role_name = 'BUSINESS' THEN 'BUSINESS'
        WHEN r.role_name = 'EXPERT' THEN 'EXPERT'
        ELSE r.role_name
    END,
    current_balance = COALESCE(NULLIF(sw.total_revenue + sw.holding_balance, 0), sw.current_balance),
    available_balance = COALESCE(sw.total_revenue, sw.available_balance),
    escrow_balance = COALESCE(sw.holding_balance, sw.escrow_balance)
FROM account a
JOIN roles r ON r.role_id = a.role_id
WHERE sw.account_id = a.account_id;

ALTER TABLE system_wallet ALTER COLUMN role_id SET NOT NULL;
ALTER TABLE system_wallet ALTER COLUMN wallet_type SET NOT NULL;
ALTER TABLE system_wallet
    ADD CONSTRAINT fk_system_wallet_role FOREIGN KEY (role_id) REFERENCES roles(role_id);
ALTER TABLE system_wallet
    ADD CONSTRAINT chk_system_wallet_type CHECK (wallet_type IN ('ADMIN_SYSTEM', 'BUSINESS', 'EXPERT', 'STAFF'));
ALTER TABLE system_wallet
    ADD CONSTRAINT chk_system_wallet_balances_non_negative CHECK (
        current_balance >= 0
        AND available_balance >= 0
        AND escrow_balance >= 0
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_system_wallet_account_id ON system_wallet(account_id);
CREATE INDEX IF NOT EXISTS idx_system_wallet_role_id ON system_wallet(role_id);
CREATE INDEX IF NOT EXISTS idx_system_wallet_type ON system_wallet(wallet_type);
