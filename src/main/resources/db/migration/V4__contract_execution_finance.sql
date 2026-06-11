-- Contracts
CREATE TABLE IF NOT EXISTS contracts (
    contract_id SERIAL PRIMARY KEY,
    job_id INT NOT NULL,
    business_id INT NOT NULL,
    expert_id INT NOT NULL,
    technology_used VARCHAR(255),
    total_budget DECIMAL(18,2) NOT NULL,
    timeline_days INT NOT NULL,
    nda_signed BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'Draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contracts_job
        FOREIGN KEY (job_id) REFERENCES jobs(job_id),
    CONSTRAINT fk_contracts_business
        FOREIGN KEY (business_id) REFERENCES business_profiles(business_id),
    CONSTRAINT fk_contracts_expert
        FOREIGN KEY (expert_id) REFERENCES expert_profiles(expert_id),
    CONSTRAINT chk_contracts_budget_positive
        CHECK (total_budget > 0),
    CONSTRAINT chk_contracts_timeline_positive
        CHECK (timeline_days > 0),
    CONSTRAINT chk_contracts_status
        CHECK (status IN ('Draft', 'Negotiating', 'Active', 'Completed', 'Terminated', 'Cancelled'))
);

-- Contract Change Requests
CREATE TABLE IF NOT EXISTS contract_change_requests (
    request_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    requested_by_account_id INT NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    change_summary TEXT NOT NULL,
    proposed_budget DECIMAL(18,2),
    proposed_timeline_days INT,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    reviewed_by_account_id INT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ccr_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(contract_id),
    CONSTRAINT fk_ccr_requested_by
        FOREIGN KEY (requested_by_account_id) REFERENCES account(account_id),
    CONSTRAINT fk_ccr_reviewed_by
        FOREIGN KEY (reviewed_by_account_id) REFERENCES account(account_id),
    CONSTRAINT chk_ccr_status
        CHECK (status IN ('Pending', 'Accepted', 'Rejected', 'Countered'))
);

-- Milestones
CREATE TABLE IF NOT EXISTS milestones (
    milestone_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    milestone_name VARCHAR(255) NOT NULL,
    funds_allocated DECIMAL(18,2) NOT NULL,
    order_index INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_milestones_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(contract_id),
    CONSTRAINT chk_milestones_funds_positive
        CHECK (funds_allocated >= 0),
    CONSTRAINT chk_milestones_order_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_milestones_status
        CHECK (status IN ('Pending', 'InProgress', 'Submitted', 'Approved', 'Rejected', 'AutoApproved'))
);

-- Acceptance Criteria
CREATE TABLE IF NOT EXISTS acceptance_criteria (
    criteria_id SERIAL PRIMARY KEY,
    milestone_id INT NOT NULL,
    description TEXT NOT NULL,
    is_passed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acceptance_criteria_milestone
        FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id)
);

-- Deliverables
CREATE TABLE IF NOT EXISTS deliverables (
    deliverable_id SERIAL PRIMARY KEY,
    milestone_id INT NOT NULL,
    source_code_url VARCHAR(255),
    demo_link VARCHAR(255),
    submission_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deliverables_milestone
        FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id)
);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id SERIAL PRIMARY KEY,
    milestone_id INT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    commission_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    transaction_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_milestone
        FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id),
    CONSTRAINT chk_transactions_amount_non_negative
        CHECK (amount >= 0),
    CONSTRAINT chk_transactions_fee_non_negative
        CHECK (commission_fee >= 0),
    CONSTRAINT chk_transactions_type
        CHECK (transaction_type IN ('EscrowDeposit', 'Payout', 'Refund', 'Penalty')),
    CONSTRAINT chk_transactions_status
        CHECK (status IN ('Pending', 'Processing', 'Success', 'Failed', 'Cancelled'))
);

-- Invoices
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL UNIQUE,
    bank_tx_code VARCHAR(100),
    receipt_img_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

CREATE INDEX IF NOT EXISTS idx_contracts_job_id ON contracts(job_id);
CREATE INDEX IF NOT EXISTS idx_contracts_business_id ON contracts(business_id);
CREATE INDEX IF NOT EXISTS idx_contracts_expert_id ON contracts(expert_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_milestones_contract_id ON milestones(contract_id);
CREATE INDEX IF NOT EXISTS idx_transactions_milestone_id ON transactions(milestone_id);