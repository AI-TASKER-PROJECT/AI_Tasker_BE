ALTER TABLE account ADD COLUMN IF NOT EXISTS failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE account ADD COLUMN IF NOT EXISTS lockout_count INT NOT NULL DEFAULT 0;
ALTER TABLE account ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP NULL;
ALTER TABLE account ADD COLUMN IF NOT EXISTS lock_reason VARCHAR(80) NULL;
ALTER TABLE account ADD COLUMN IF NOT EXISTS last_failed_login_at TIMESTAMP NULL;
ALTER TABLE account ADD COLUMN IF NOT EXISTS status_before_lock VARCHAR(20) NULL;

CREATE INDEX IF NOT EXISTS idx_account_locked_until ON account(locked_until);
CREATE INDEX IF NOT EXISTS idx_account_lock_reason ON account(lock_reason);

UPDATE account SET lock_reason = 'ADMIN_LOCKED' WHERE status = 'Lock' AND lock_reason IS NULL;
