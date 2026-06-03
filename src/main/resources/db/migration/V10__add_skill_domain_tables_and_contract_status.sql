-- BO SUNG CAC BANG TAG DOMAIN/SKILL THEO DATABASE DESCRIPTION MOI TU EXCEL.
CREATE TABLE IF NOT EXISTS domains (
    domain_id SERIAL PRIMARY KEY,
    domain_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS skills (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS job_domains (
    job_id INT NOT NULL,
    domain_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (job_id, domain_id),
    CONSTRAINT fk_job_domains_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_job_domains_domain FOREIGN KEY (domain_id) REFERENCES domains(domain_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS job_skills (
    job_id INT NOT NULL,
    skill_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (job_id, skill_id),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_job_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_job_domains_domain ON job_domains(domain_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_skill ON job_skills(skill_id);

-- DONG BO STATUS CONTRACT DE PHU HOP LUONG DAM PHAN THUC TE CUA BUSINESS RULE.
ALTER TABLE contracts DROP CONSTRAINT IF EXISTS chk_contracts_status;
ALTER TABLE contracts ADD CONSTRAINT chk_contracts_status
CHECK (status IN ('Draft', 'Negotiating', 'Active', 'Completed', 'Cancelled', 'Terminated'));
