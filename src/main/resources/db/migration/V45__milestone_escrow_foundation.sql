ALTER TABLE contracts DROP CONSTRAINT IF EXISTS chk_contracts_status;
ALTER TABLE contracts ADD CONSTRAINT chk_contracts_status 
    CHECK (status IN ('DRAFT', 'PENDING', 'ACTIVE', 'TERMINATION_PENDING', 'COMPLETED', 'TERMINATED', 'CLOSED', 'CANCELLED'));

ALTER TABLE milestones
    ADD COLUMN IF NOT EXISTS escrow_released_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS settlement_source_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS settlement_source_id BIGINT;

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_status;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_status
    CHECK (status IN ('PENDING', 'DEPOSITED', 'IN_PROGRESS', 'UNDER_REVIEW', 'DISPUTED', 'COMPLETED', 'CANCELLED'));

ALTER TABLE contract_milestones
    ADD COLUMN IF NOT EXISTS escrow_released_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS settlement_source_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS settlement_source_id BIGINT;

ALTER TABLE contract_milestones DROP CONSTRAINT IF EXISTS chk_contract_milestones_status;
ALTER TABLE contract_milestones ADD CONSTRAINT chk_contract_milestones_status 
    CHECK (status IN ('PENDING', 'DEPOSITED', 'IN_PROGRESS', 'UNDER_REVIEW', 'DISPUTED', 'COMPLETED', 'CANCELLED'));

ALTER TABLE wallet_transactions
    ADD COLUMN IF NOT EXISTS contract_id INT REFERENCES contracts(contract_id),
    ADD COLUMN IF NOT EXISTS milestone_id INT REFERENCES milestones(milestone_id),
    ADD COLUMN IF NOT EXISTS metadata JSONB;
