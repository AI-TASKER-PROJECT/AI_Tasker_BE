-- Flow 4-5 v2.2. Additive migration; existing payments are never fabricated.

ALTER TABLE contract_deposits DROP CONSTRAINT IF EXISTS contract_deposits_contract_id_key;
ALTER TABLE contract_deposits
    ADD COLUMN IF NOT EXISTS owner_account_id INT REFERENCES account(account_id),
    ADD COLUMN IF NOT EXISTS owner_role VARCHAR(20),
    ADD COLUMN IF NOT EXISTS required_percentage DECIMAL(5,2),
    ADD COLUMN IF NOT EXISTS required_amount DECIMAL(19,2),
    ADD COLUMN IF NOT EXISTS penalty_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS penalty_beneficiary_account_id INT REFERENCES account(account_id),
    ADD COLUMN IF NOT EXISTS penalty_transaction_id BIGINT REFERENCES wallet_transactions(id),
    ADD COLUMN IF NOT EXISTS resolution_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;

UPDATE contract_deposits cd
SET owner_role = 'BUSINESS',
    owner_account_id = bp.account_id,
    required_percentage = 20.00,
    required_amount = cd.deposit_amount
FROM contracts c
JOIN business_profiles bp ON bp.business_id = c.business_id
WHERE c.contract_id = cd.contract_id
  AND cd.owner_role IS NULL;

ALTER TABLE contract_deposits
    ALTER COLUMN owner_role SET NOT NULL,
    ALTER COLUMN required_percentage SET NOT NULL,
    ALTER COLUMN required_amount SET NOT NULL;

ALTER TABLE contract_deposits DROP CONSTRAINT IF EXISTS chk_contract_deposits_owner_role;
ALTER TABLE contract_deposits ADD CONSTRAINT chk_contract_deposits_owner_role
    CHECK (owner_role IN ('BUSINESS', 'EXPERT'));
CREATE UNIQUE INDEX IF NOT EXISTS uq_contract_deposits_contract_role
    ON contract_deposits(contract_id, owner_role);
ALTER TABLE contract_deposits DROP CONSTRAINT IF EXISTS chk_contract_deposits_status;
ALTER TABLE contract_deposits ADD CONSTRAINT chk_contract_deposits_status
    CHECK (status IN (
        'UNPAID','HELD','PARTIALLY_REFUNDED','REFUNDED','ADMIN_RESOLVED','PENALTY_SETTLED'
    ));

ALTER TABLE milestones
    ADD COLUMN IF NOT EXISTS reject_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_rejection_feedback TEXT;
ALTER TABLE milestones DROP CONSTRAINT IF EXISTS chk_milestones_status;
ALTER TABLE milestones ADD CONSTRAINT chk_milestones_status
    CHECK (status IN ('PENDING','DEPOSITED','IN_PROGRESS','OVERDUE','UNDER_REVIEW','DISPUTED','COMPLETED','CANCELLED'));

ALTER TABLE contract_milestones
    ADD COLUMN IF NOT EXISTS reject_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_rejection_feedback TEXT;
ALTER TABLE contract_milestones DROP CONSTRAINT IF EXISTS chk_contract_milestones_status;
ALTER TABLE contract_milestones ADD CONSTRAINT chk_contract_milestones_status
    CHECK (status IN ('PENDING','DEPOSITED','IN_PROGRESS','OVERDUE','UNDER_REVIEW','DISPUTED','COMPLETED','CANCELLED'));

ALTER TABLE deliverables
    ADD COLUMN IF NOT EXISTS submission_round INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    ADD COLUMN IF NOT EXISTS rejection_feedback TEXT,
    ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP;
ALTER TABLE deliverables DROP CONSTRAINT IF EXISTS chk_deliverables_status;
ALTER TABLE deliverables ADD CONSTRAINT chk_deliverables_status
    CHECK (status IN ('SUBMITTED','APPROVED','REJECTED','SUPERSEDED'));
CREATE INDEX IF NOT EXISTS idx_deliverables_milestone_round
    ON deliverables(milestone_id, submission_round DESC);

ALTER TABLE milestone_progress_reports
    ADD COLUMN IF NOT EXISTS source_code_url TEXT,
    ADD COLUMN IF NOT EXISTS demo_link TEXT,
    ADD COLUMN IF NOT EXISTS submission_notes TEXT,
    ADD COLUMN IF NOT EXISTS business_feedback TEXT,
    ADD COLUMN IF NOT EXISTS feedback_category VARCHAR(30),
    ADD COLUMN IF NOT EXISTS feedback_severity VARCHAR(20),
    ADD COLUMN IF NOT EXISTS feedback_dod_items JSONB,
    ADD COLUMN IF NOT EXISTS requires_adjustment BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS feedback_by_account_id INT REFERENCES account(account_id),
    ADD COLUMN IF NOT EXISTS feedback_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS milestone_progress_report_requests (
    progress_report_request_id BIGSERIAL PRIMARY KEY,
    contract_id INT NOT NULL REFERENCES contracts(contract_id),
    milestone_id INT NOT NULL REFERENCES milestones(milestone_id),
    requested_by_account_id INT NOT NULL REFERENCES account(account_id),
    request_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    progress_report_id BIGINT REFERENCES milestone_progress_reports(progress_report_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_progress_report_request_status
        CHECK (status IN ('PENDING','SUBMITTED','EXPIRED','CANCELLED')),
    CONSTRAINT uq_progress_report_request_number
        UNIQUE (contract_id, milestone_id, request_number)
);
CREATE INDEX IF NOT EXISTS idx_progress_report_requests_milestone_due
    ON milestone_progress_report_requests(contract_id, milestone_id, status, due_at);
CREATE UNIQUE INDEX IF NOT EXISTS uq_progress_report_request_pending
    ON milestone_progress_report_requests(contract_id, milestone_id)
    WHERE status = 'PENDING';

ALTER TABLE disputes
    ADD COLUMN IF NOT EXISTS evidence_collection_due_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS staff_access_scope VARCHAR(50),
    ADD COLUMN IF NOT EXISTS staff_access_expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS staff_sla_due_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS staff_sla_escalated_at TIMESTAMP;

ALTER TABLE termination_requests
    ADD COLUMN IF NOT EXISTS expert_response_due_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expert_responded_at TIMESTAMP;
ALTER TABLE termination_requests DROP CONSTRAINT IF EXISTS chk_termination_requests_status;
ALTER TABLE termination_requests ADD CONSTRAINT chk_termination_requests_status CHECK (status IN (
    'AWAITING_EXPERT_RESPONSE','REQUESTED','STAFF_REVIEWING','STAFF_APPROVED',
    'STAFF_REJECTED','AWAITING_SETTLEMENT_EXECUTION','AWAITING_DEPOSIT_REFUND',
    'COMPLETED','CANCELLED'
));
DROP INDEX IF EXISTS uq_termination_one_active_per_contract;
CREATE UNIQUE INDEX uq_termination_one_active_per_contract
    ON termination_requests(contract_id)
    WHERE status IN (
      'AWAITING_EXPERT_RESPONSE','REQUESTED','STAFF_REVIEWING','STAFF_APPROVED',
      'AWAITING_SETTLEMENT_EXECUTION','AWAITING_DEPOSIT_REFUND'
    );
