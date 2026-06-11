-- DONG BO SCHEMA THEO FILE EXCEL MOI MA KHONG SUA LICH SU MIGRATION CU.

ALTER TABLE jobs ADD COLUMN IF NOT EXISTS planned_duration_value INT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS planned_duration_unit VARCHAR(20);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS is_hot BOOLEAN DEFAULT FALSE;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS hot_until TIMESTAMP;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;

ALTER TABLE jobs DROP CONSTRAINT IF EXISTS chk_jobs_status;
ALTER TABLE jobs ADD CONSTRAINT chk_jobs_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'CANCELLED'));

ALTER TABLE proposals DROP CONSTRAINT IF EXISTS chk_proposals_status;
ALTER TABLE proposals ADD CONSTRAINT chk_proposals_status CHECK (status IN ('Pending', 'Accepted', 'Rejected'));
ALTER TABLE proposals ALTER COLUMN status SET DEFAULT 'Pending';

ALTER TABLE contracts DROP CONSTRAINT IF EXISTS chk_contracts_status;
ALTER TABLE contracts ADD CONSTRAINT chk_contracts_status CHECK (status IN ('Draft', 'Active', 'Completed', 'Terminated'));

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_status;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_status CHECK (status IN ('Pending', 'Deposited', 'Under Review', 'Released'));
ALTER TABLE milestones ALTER COLUMN status SET DEFAULT 'Pending';

ALTER TABLE transactions ALTER COLUMN transaction_id TYPE BIGINT;
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS chk_transactions_type;
ALTER TABLE transactions ADD CONSTRAINT chk_transactions_type CHECK (transaction_type IN ('Deposit', 'Payout', 'Refund'));
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS chk_transactions_status;
ALTER TABLE transactions ADD CONSTRAINT chk_transactions_status CHECK (status IN ('Pending', 'Success', 'Failed'));

ALTER TABLE invoices ALTER COLUMN invoice_id TYPE BIGINT;
ALTER TABLE invoices ALTER COLUMN transaction_id TYPE BIGINT;

ALTER TABLE disputes DROP CONSTRAINT IF EXISTS chk_disputes_status;
ALTER TABLE disputes ADD CONSTRAINT chk_disputes_status CHECK (status IN ('Open', 'UnderReview', 'Resolved', 'Rejected', 'Escalated'));
