-- NOTE FILE: src/main/resources/db/migration/V29__contract_flow_status_alignment.sql
-- Align job and milestone status constraints with the contract lifecycle from SPEC-CONSTRACT.md.

ALTER TABLE jobs DROP CONSTRAINT IF EXISTS chk_jobs_status;
ALTER TABLE jobs ADD CONSTRAINT chk_jobs_status
CHECK (status IN ('DRAFT', 'OPEN', 'PROPOSAL_REVIEW', 'IN_PROGRESS', 'CLOSED', 'CANCELLED'));

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_status;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_status
CHECK (status IN ('Pending', 'Deposited', 'Under Review', 'Released', 'Completed'));