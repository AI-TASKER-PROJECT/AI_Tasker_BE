-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
    review_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    reviewee_id INT NOT NULL,
    rating DECIMAL(2,1) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(contract_id),
    CONSTRAINT fk_reviews_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES account(account_id),
    CONSTRAINT fk_reviews_reviewee
        FOREIGN KEY (reviewee_id) REFERENCES account(account_id),
    CONSTRAINT chk_reviews_rating
        CHECK (rating >= 1 AND rating <= 5)
);

-- Disputes
CREATE TABLE IF NOT EXISTS disputes (
    dispute_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    milestone_id INT,
    assigned_staff_id INT,
    evidence_report TEXT,
    proposed_action VARCHAR(100),
    admin_approved_by INT,
    status VARCHAR(30) NOT NULL DEFAULT 'Open',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_disputes_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(contract_id),
    CONSTRAINT fk_disputes_milestone
        FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id),
    CONSTRAINT fk_disputes_assigned_staff
        FOREIGN KEY (assigned_staff_id) REFERENCES staffs(staff_id),
    CONSTRAINT fk_disputes_admin_approved_by
        FOREIGN KEY (admin_approved_by) REFERENCES account(account_id),
    CONSTRAINT chk_disputes_status
        CHECK (status IN ('Open', 'UnderReview', 'Resolved', 'Rejected', 'Escalated'))
);

-- Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id SERIAL PRIMARY KEY,
    actor_account_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    old_value_json JSONB,
    new_value_json JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_account_id) REFERENCES account(account_id)
);

-- System Settings
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    value_type VARCHAR(20) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_system_settings_updated_by
        FOREIGN KEY (updated_by) REFERENCES account(account_id),
    CONSTRAINT chk_system_settings_value_type
        CHECK (value_type IN ('STRING', 'INT', 'DECIMAL', 'BOOLEAN', 'JSON'))
);

CREATE INDEX IF NOT EXISTS idx_reviews_contract_id ON reviews(contract_id);
CREATE INDEX IF NOT EXISTS idx_disputes_contract_id ON disputes(contract_id);
CREATE INDEX IF NOT EXISTS idx_disputes_status ON disputes(status);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs(actor_account_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_name, entity_id);