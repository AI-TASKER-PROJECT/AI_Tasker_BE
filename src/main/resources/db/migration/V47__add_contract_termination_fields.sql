-- V47__add_contract_termination_fields.sql
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS termination_reason TEXT;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS termination_note TEXT;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS terminated_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS termination_requests (
    termination_request_id BIGSERIAL PRIMARY KEY,
    contract_id INT NOT NULL REFERENCES contracts(contract_id),
    current_milestone_id INT NULL REFERENCES milestones(milestone_id),
    requested_by_account_id INT NOT NULL REFERENCES account(account_id),
    requested_by_role VARCHAR(20) NOT NULL,
    request_reason TEXT NOT NULL,
    request_file_url TEXT NULL,
    assigned_staff_id INT NULL REFERENCES staffs(staff_id),
    status VARCHAR(50) NOT NULL,
    staff_review_started_at TIMESTAMP NULL,
    staff_decided_at TIMESTAMP NULL,
    staff_decision_reason TEXT NULL,
    staff_report TEXT NULL,
    expert_payout_percentage DECIMAL(5,2) NULL,
    expert_payout_amount DECIMAL(19,2) NULL,
    business_refund_amount DECIMAL(19,2) NULL,
    partial_evidence_required BOOLEAN NOT NULL DEFAULT FALSE,
    partial_evidence_submitted_at TIMESTAMP NULL,
    partial_evidence_url TEXT NULL,
    partial_evidence_note TEXT NULL,
    settlement_executed_at TIMESTAMP NULL,
    settlement_wallet_transaction_id BIGINT NULL REFERENCES wallet_transactions(id),
    deposit_refund_required BOOLEAN NOT NULL DEFAULT TRUE,
    deposit_refunded_at TIMESTAMP NULL,
    deposit_refund_transaction_id BIGINT NULL REFERENCES wallet_transactions(id),
    cancelled_at TIMESTAMP NULL,
    cancelled_by_account_id INT NULL REFERENCES account(account_id),
    cancellation_reason TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_termination_requests_status CHECK (status IN (
        'REQUESTED',
        'STAFF_REVIEWING',
        'STAFF_APPROVED',
        'STAFF_REJECTED',
        'AWAITING_SETTLEMENT_EXECUTION',
        'AWAITING_DEPOSIT_REFUND',
        'COMPLETED',
        'CANCELLED'
    ))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_termination_one_active_per_contract
ON termination_requests(contract_id)
WHERE status IN (
  'REQUESTED',
  'STAFF_REVIEWING',
  'STAFF_APPROVED',
  'AWAITING_SETTLEMENT_EXECUTION',
  'AWAITING_DEPOSIT_REFUND'
);

CREATE INDEX IF NOT EXISTS idx_termination_contract_status
ON termination_requests(contract_id, status);

CREATE INDEX IF NOT EXISTS idx_termination_staff_status
ON termination_requests(assigned_staff_id, status);

CREATE TABLE IF NOT EXISTS case_attachments (
    attachment_id BIGSERIAL PRIMARY KEY,
    owner_type VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL,
    uploaded_by_account_id INT NOT NULL REFERENCES account(account_id),
    file_url TEXT NOT NULL,
    file_name TEXT NULL,
    file_type VARCHAR(100) NULL,
    note TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_case_attachments_owner_type CHECK (owner_type IN (
        'DISPUTE',
        'TERMINATION_REQUEST',
        'DELIVERABLE_REJECTION',
        'PARTIAL_EVIDENCE',
        'STAFF_REPORT'
    ))
);

CREATE INDEX IF NOT EXISTS idx_case_attachments_owner
ON case_attachments(owner_type, owner_id);
