-- Flow 4-5 v2.3: progress-report acknowledgement gate.
-- Legacy reports predate the gate, so they are acknowledged during backfill.

ALTER TABLE milestone_progress_reports
    ADD COLUMN IF NOT EXISTS acknowledgement_state VARCHAR(30),
    ADD COLUMN IF NOT EXISTS acknowledged_by_account_id INT REFERENCES account(account_id),
    ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP;

UPDATE milestone_progress_reports
SET acknowledgement_state = 'ACKNOWLEDGED'
WHERE acknowledgement_state IS NULL;

ALTER TABLE milestone_progress_reports
    ALTER COLUMN acknowledgement_state SET DEFAULT 'PENDING_BUSINESS_ACK',
    ALTER COLUMN acknowledgement_state SET NOT NULL;

ALTER TABLE milestone_progress_reports
    DROP CONSTRAINT IF EXISTS chk_progress_report_acknowledgement_state;
ALTER TABLE milestone_progress_reports
    ADD CONSTRAINT chk_progress_report_acknowledgement_state
        CHECK (acknowledgement_state IN ('PENDING_BUSINESS_ACK', 'ACKNOWLEDGED'));

CREATE INDEX IF NOT EXISTS idx_progress_reports_pending_ack
    ON milestone_progress_reports(contract_id, milestone_id, acknowledgement_state, created_at DESC);
