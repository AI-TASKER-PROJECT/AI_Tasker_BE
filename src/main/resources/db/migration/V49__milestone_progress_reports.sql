-- Anchor timestamp needed to compute progress-report checkpoint due dates
-- (midpoint / pre-deadline of the milestone timeline).
ALTER TABLE contract_milestones
    ADD COLUMN IF NOT EXISTS in_progress_started_at TIMESTAMP;

-- Backfill milestones already IN_PROGRESS before this migration so their
-- checkpoint calculation has a reference point.
UPDATE contract_milestones
SET in_progress_started_at = updated_at
WHERE status = 'IN_PROGRESS'
  AND in_progress_started_at IS NULL;

CREATE TABLE IF NOT EXISTS milestone_progress_reports (
    progress_report_id BIGSERIAL PRIMARY KEY,
    contract_id INT NOT NULL REFERENCES contracts(contract_id),
    milestone_id INT NOT NULL REFERENCES milestones(milestone_id),
    submitted_by_account_id INT NOT NULL REFERENCES account(account_id),
    checkpoint_type VARCHAR(20) NULL,
    content TEXT NOT NULL,
    percent_complete INT NULL,
    attachment_url TEXT NULL,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_progress_report_checkpoint_type CHECK (checkpoint_type IN ('MIDPOINT', 'PRE_DEADLINE') OR checkpoint_type IS NULL),
    CONSTRAINT chk_progress_report_percent CHECK (percent_complete IS NULL OR (percent_complete >= 0 AND percent_complete <= 100))
);

CREATE INDEX IF NOT EXISTS idx_milestone_progress_reports_milestone
    ON milestone_progress_reports (milestone_id, created_at);
