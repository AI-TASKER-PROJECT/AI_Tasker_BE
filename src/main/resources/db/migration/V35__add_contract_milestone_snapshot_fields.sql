ALTER TABLE contract_milestones
    ADD COLUMN IF NOT EXISTS criteria_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS deliverable_expectation TEXT;
