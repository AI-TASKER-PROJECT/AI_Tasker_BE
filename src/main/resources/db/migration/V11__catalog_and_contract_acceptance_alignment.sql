-- ALIGN 4 CATALOG TABLES WITH THE EXCEL DB DESCRIPTION WITHOUT EDITING OLD MIGRATIONS.
ALTER TABLE domains ADD COLUMN IF NOT EXISTS domain_code VARCHAR(50);
UPDATE domains
SET domain_code = UPPER(REGEXP_REPLACE(domain_name, '[^A-Za-z0-9]+', '_', 'g'))
WHERE domain_code IS NULL OR domain_code = '';
ALTER TABLE domains ALTER COLUMN domain_code SET NOT NULL;
ALTER TABLE domains ALTER COLUMN domain_name TYPE VARCHAR(255);
ALTER TABLE domains ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE domains ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_domains_code'
    ) THEN
        ALTER TABLE domains ADD CONSTRAINT uq_domains_code UNIQUE (domain_code);
    END IF;
END $$;

ALTER TABLE skills ADD COLUMN IF NOT EXISTS skill_code VARCHAR(50);
UPDATE skills
SET skill_code = UPPER(REGEXP_REPLACE(skill_name, '[^A-Za-z0-9]+', '_', 'g'))
WHERE skill_code IS NULL OR skill_code = '';
ALTER TABLE skills ALTER COLUMN skill_code SET NOT NULL;
ALTER TABLE skills ALTER COLUMN skill_name TYPE VARCHAR(255);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_skills_code'
    ) THEN
        ALTER TABLE skills ADD CONSTRAINT uq_skills_code UNIQUE (skill_code);
    END IF;
END $$;

ALTER TABLE job_skills ADD COLUMN IF NOT EXISTS required_level VARCHAR(50);
ALTER TABLE job_skills ADD COLUMN IF NOT EXISTS is_mandatory BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE job_skills ADD COLUMN IF NOT EXISTS min_years_experience INT;

-- STORE BOTH PARTY ACCEPTANCE SO CONTRACT ACTIVATION CAN MATCH CON-01.
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS business_accepted_at TIMESTAMP;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS expert_accepted_at TIMESTAMP;
