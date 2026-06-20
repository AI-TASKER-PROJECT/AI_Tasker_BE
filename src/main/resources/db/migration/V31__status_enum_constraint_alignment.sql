ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_wallet_transactions_status;
UPDATE wallet_transactions
SET status = 'POSTED'
WHERE status IS DISTINCT FROM 'POSTED';
ALTER TABLE wallet_transactions ALTER COLUMN status SET DEFAULT 'POSTED';
ALTER TABLE wallet_transactions ADD CONSTRAINT chk_wallet_transactions_status
    CHECK (status IN ('POSTED'));

ALTER TABLE contracts DROP CONSTRAINT IF EXISTS chk_contracts_status;
UPDATE contracts
SET status = CASE status
    WHEN 'Draft' THEN 'DRAFT'
    WHEN 'Negotiating' THEN 'DRAFT'
    WHEN 'PendingDeposit' THEN 'PENDING'
    WHEN 'Active' THEN 'ACTIVE'
    WHEN 'Completed' THEN 'COMPLETED'
    WHEN 'Closed' THEN 'COMPLETED'
    WHEN 'Cancelled' THEN 'CANCELLED'
    WHEN 'Terminated' THEN 'CANCELLED'
    ELSE status
END;
ALTER TABLE contracts ALTER COLUMN status SET DEFAULT 'DRAFT';
ALTER TABLE contracts ADD CONSTRAINT chk_contracts_status
    CHECK (status IN ('DRAFT', 'PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED'));

ALTER TABLE jobs DROP CONSTRAINT IF EXISTS chk_jobs_status;
UPDATE jobs
SET status = CASE status
    WHEN 'Draft' THEN 'DRAFT'
    WHEN 'Open' THEN 'OPEN'
    WHEN 'PROPOSAL_REVIEW' THEN 'OPEN'
    WHEN 'CANCELLED' THEN 'CLOSED'
    WHEN 'Cancelled' THEN 'CLOSED'
    ELSE status
END;
ALTER TABLE jobs ALTER COLUMN status SET DEFAULT 'DRAFT';
ALTER TABLE jobs ADD CONSTRAINT chk_jobs_status
    CHECK (status IN ('DRAFT', 'OPEN', 'IN_PROGRESS', 'CLOSED'));

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_status;
UPDATE milestones
SET status = CASE status
    WHEN 'Pending' THEN 'PENDING'
    WHEN 'Deposited' THEN 'DEPOSITED'
    WHEN 'Under Review' THEN 'UNDER_REVIEW'
    WHEN 'Released' THEN 'COMPLETED'
    WHEN 'Completed' THEN 'COMPLETED'
    WHEN 'InProgress' THEN 'IN_PROGRESS'
    WHEN 'Submitted' THEN 'UNDER_REVIEW'
    WHEN 'Approved' THEN 'COMPLETED'
    WHEN 'Rejected' THEN 'DISPUTED'
    WHEN 'AutoApproved' THEN 'COMPLETED'
    ELSE status
END;
ALTER TABLE milestones ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_status
    CHECK (status IN ('PENDING', 'DEPOSITED', 'IN_PROGRESS', 'UNDER_REVIEW', 'DISPUTED', 'COMPLETED'));

ALTER TABLE contract_milestones DROP CONSTRAINT IF EXISTS chk_contract_milestones_status;
UPDATE contract_milestones
SET status = CASE status
    WHEN 'Pending' THEN 'PENDING'
    WHEN 'Deposited' THEN 'DEPOSITED'
    WHEN 'Under Review' THEN 'UNDER_REVIEW'
    WHEN 'Released' THEN 'COMPLETED'
    WHEN 'Completed' THEN 'COMPLETED'
    WHEN 'InProgress' THEN 'IN_PROGRESS'
    WHEN 'Submitted' THEN 'UNDER_REVIEW'
    WHEN 'Approved' THEN 'COMPLETED'
    WHEN 'Rejected' THEN 'DISPUTED'
    WHEN 'AutoApproved' THEN 'COMPLETED'
    ELSE status
END;
ALTER TABLE contract_milestones ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE contract_milestones ADD CONSTRAINT chk_contract_milestones_status
    CHECK (status IN ('PENDING', 'DEPOSITED', 'IN_PROGRESS', 'UNDER_REVIEW', 'DISPUTED', 'COMPLETED'));
