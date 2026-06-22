ALTER TABLE milestones
    ADD COLUMN IF NOT EXISTS duration INT,
    ADD COLUMN IF NOT EXISTS duration_unit VARCHAR(20);

ALTER TABLE milestones
    ADD CONSTRAINT chk_milestones_duration_positive CHECK (duration IS NULL OR duration > 0);

ALTER TABLE milestones
    ADD CONSTRAINT chk_milestones_duration_unit CHECK (duration_unit IS NULL OR duration_unit IN ('DAY', 'WEEK', 'MONTH'));

ALTER TABLE contract_milestones
    ADD COLUMN IF NOT EXISTS duration INT,
    ADD COLUMN IF NOT EXISTS duration_unit VARCHAR(20);

ALTER TABLE contract_milestones
    ADD CONSTRAINT chk_contract_milestones_duration_positive CHECK (duration IS NULL OR duration > 0);

ALTER TABLE contract_milestones
    ADD CONSTRAINT chk_contract_milestones_duration_unit CHECK (duration_unit IS NULL OR duration_unit IN ('DAY', 'WEEK', 'MONTH'));
