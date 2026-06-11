-- Portfolios
CREATE TABLE IF NOT EXISTS portfolios (
    portfolio_id SERIAL PRIMARY KEY,
    expert_id INT NOT NULL UNIQUE,
    context TEXT NOT NULL,
    data_processing TEXT NOT NULL,
    model_architecture TEXT NOT NULL,
    performance_metrics TEXT NOT NULL,
    poc_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolios_expert
        FOREIGN KEY (expert_id) REFERENCES expert_profiles(expert_id)
);

-- Jobs
CREATE TABLE IF NOT EXISTS jobs (
    job_id SERIAL PRIMARY KEY,
    business_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    raw_requirements TEXT NOT NULL,
    structured_sow TEXT,
    ai_tag VARCHAR(50),
    budget DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Open',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jobs_business
        FOREIGN KEY (business_id) REFERENCES business_profiles(business_id),
    CONSTRAINT chk_jobs_budget_positive
        CHECK (budget > 0),
    CONSTRAINT chk_jobs_status
        CHECK (status IN ('Draft', 'Open', 'Closed', 'Cancelled'))
);

-- Proposals
CREATE TABLE IF NOT EXISTS proposals (
    proposal_id SERIAL PRIMARY KEY,
    job_id INT NOT NULL,
    expert_id INT NOT NULL,
    technical_solution TEXT NOT NULL,
    bid_amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Submitted',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_proposals_job
        FOREIGN KEY (job_id) REFERENCES jobs(job_id),
    CONSTRAINT fk_proposals_expert
        FOREIGN KEY (expert_id) REFERENCES expert_profiles(expert_id),
    CONSTRAINT uq_proposals_job_expert UNIQUE (job_id, expert_id),
    CONSTRAINT chk_proposals_bid_positive
        CHECK (bid_amount > 0),
    CONSTRAINT chk_proposals_status
        CHECK (status IN ('Submitted', 'Shortlisted', 'Accepted', 'Rejected', 'Withdrawn'))
);

CREATE INDEX IF NOT EXISTS idx_jobs_business_id ON jobs(business_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_proposals_job_id ON proposals(job_id);
CREATE INDEX IF NOT EXISTS idx_proposals_expert_id ON proposals(expert_id);
CREATE INDEX IF NOT EXISTS idx_proposals_status ON proposals(status);