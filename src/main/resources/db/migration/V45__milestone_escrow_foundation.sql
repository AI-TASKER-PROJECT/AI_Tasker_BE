ALTER TABLE contracts DROP CONSTRAINT IF EXISTS chk_contracts_status;
ALTER TABLE contracts ADD CONSTRAINT chk_contracts_status 
    CHECK (status IN ('DRAFT', 'PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'TERMINATION_PENDING', 'TERMINATED'));

ALTER TABLE contract_milestones DROP CONSTRAINT IF EXISTS chk_contract_milestones_status;
ALTER TABLE contract_milestones ADD CONSTRAINT chk_contract_milestones_status 
    CHECK (status IN ('PENDING', 'DEPOSITED', 'IN_PROGRESS', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'DISPUTED', 'COMPLETED', 'CANCELLED'));