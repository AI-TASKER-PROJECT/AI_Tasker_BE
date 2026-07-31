ALTER TABLE contract_change_requests
    ADD COLUMN IF NOT EXISTS proposed_scope TEXT,
    ADD COLUMN IF NOT EXISTS proposed_milestones JSONB,
    ADD COLUMN IF NOT EXISTS review_note TEXT;

ALTER TABLE contracts
    ADD COLUMN IF NOT EXISTS contract_scope TEXT;
