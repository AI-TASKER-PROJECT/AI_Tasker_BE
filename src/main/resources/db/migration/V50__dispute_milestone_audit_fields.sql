-- V50__dispute_milestone_audit_fields.sql
-- Spec SPEC-MILESTONE-DISPUTER.md sections 13.3.2 and 13.3.4 (B1):
-- add the audit/traceability columns that concrete system-behavior steps
-- (9.5, 10.2, 10.6, 10.7, 10.8, 10.9, 10.10, 11.7) set but the schema was missing.
-- Additive only; no data deletion. All new columns are nullable.

-- disputes: audit + settlement traceability fields
ALTER TABLE disputes
    ADD COLUMN IF NOT EXISTS initiated_by_account_id INT,
    ADD COLUMN IF NOT EXISTS initiation_type VARCHAR(80),
    ADD COLUMN IF NOT EXISTS escalation_requested_by_account_id INT,
    ADD COLUMN IF NOT EXISTS escalation_requested_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS staff_review_started_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS staff_decided_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS intervention_rejected_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS intervention_rejection_reason TEXT,
    ADD COLUMN IF NOT EXISTS staff_report TEXT,
    ADD COLUMN IF NOT EXISTS staff_proposed_expert_amount DECIMAL(19, 2),
    ADD COLUMN IF NOT EXISTS business_refund_amount DECIMAL(19, 2),
    ADD COLUMN IF NOT EXISTS settlement_executed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS settlement_wallet_transaction_id BIGINT,
    ADD COLUMN IF NOT EXISTS cancelled_by_account_id INT,
    ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

-- initiation_type enum (spec 7.5). NULL allowed for legacy rows.
ALTER TABLE disputes DROP CONSTRAINT IF EXISTS chk_disputes_initiation_type;
ALTER TABLE disputes ADD CONSTRAINT chk_disputes_initiation_type CHECK (
    initiation_type IS NULL OR initiation_type IN (
        'BUSINESS_REJECTED_DELIVERABLE',
        'EXPERT_SCOPE_CONCERN',
        'EXPERT_NO_REVIEW_RESPONSE',
        'EXPERT_BAD_FAITH_REJECTION',
        'OTHER'
    )
);

-- Foreign keys for the new dispute audit columns.
ALTER TABLE disputes DROP CONSTRAINT IF EXISTS fk_disputes_initiated_by_account;
ALTER TABLE disputes ADD CONSTRAINT fk_disputes_initiated_by_account
    FOREIGN KEY (initiated_by_account_id) REFERENCES account(account_id);

ALTER TABLE disputes DROP CONSTRAINT IF EXISTS fk_disputes_escalation_requested_by_account;
ALTER TABLE disputes ADD CONSTRAINT fk_disputes_escalation_requested_by_account
    FOREIGN KEY (escalation_requested_by_account_id) REFERENCES account(account_id);

ALTER TABLE disputes DROP CONSTRAINT IF EXISTS fk_disputes_settlement_wallet_transaction;
ALTER TABLE disputes ADD CONSTRAINT fk_disputes_settlement_wallet_transaction
    FOREIGN KEY (settlement_wallet_transaction_id) REFERENCES wallet_transactions(id);

ALTER TABLE disputes DROP CONSTRAINT IF EXISTS fk_disputes_cancelled_by_account;
ALTER TABLE disputes ADD CONSTRAINT fk_disputes_cancelled_by_account
    FOREIGN KEY (cancelled_by_account_id) REFERENCES account(account_id);

-- milestones: dispute/termination settlement source references (spec 10.10 step 12, 11.7 step 13)
ALTER TABLE milestones
    ADD COLUMN IF NOT EXISTS resolved_by_dispute_id INT,
    ADD COLUMN IF NOT EXISTS resolved_by_termination_request_id BIGINT;

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS fk_milestones_resolved_by_dispute;
ALTER TABLE milestones ADD CONSTRAINT fk_milestones_resolved_by_dispute
    FOREIGN KEY (resolved_by_dispute_id) REFERENCES disputes(dispute_id);

ALTER TABLE milestones DROP CONSTRAINT IF EXISTS fk_milestones_resolved_by_termination;
ALTER TABLE milestones ADD CONSTRAINT fk_milestones_resolved_by_termination
    FOREIGN KEY (resolved_by_termination_request_id) REFERENCES termination_requests(termination_request_id);

-- Recommended indexes (spec 13.3.2 / 13.3.4)
CREATE INDEX IF NOT EXISTS idx_disputes_contract_status ON disputes(contract_id, status);
CREATE INDEX IF NOT EXISTS idx_disputes_milestone_status ON disputes(milestone_id, status);
CREATE INDEX IF NOT EXISTS idx_disputes_assigned_staff_status ON disputes(assigned_staff_id, status);
CREATE INDEX IF NOT EXISTS idx_milestones_contract_status ON milestones(contract_id, status);
CREATE INDEX IF NOT EXISTS idx_milestones_settlement_source ON milestones(settlement_source_type, settlement_source_id);
