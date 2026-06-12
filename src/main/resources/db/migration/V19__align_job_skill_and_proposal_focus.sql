-- NOTE FILE: src/main/resources/db/migration/V18__align_job_skill_and_proposal_focus.sql
-- Đây là file gì: Migration cập nhật schema cho flow job posting và proposal matching.
-- Nhiệm vụ: bỏ AI tag/min years cũ, chuẩn hóa level yêu cầu của job skill và lưu domain/skill chuyên gia chọn khi nộp proposal.

UPDATE job_skills
SET required_level = CASE
    WHEN required_level ILIKE 'Advanced' THEN 'Senior'
    WHEN required_level ILIKE 'Intermediate' THEN 'Middle'
    WHEN required_level ILIKE 'Beginner' THEN 'Junior'
    WHEN required_level IS NULL OR TRIM(required_level) = '' THEN 'Junior'
    ELSE 'Junior'
END;

ALTER TABLE job_skills DROP COLUMN IF EXISTS min_years_experience;

ALTER TABLE job_skills DROP CONSTRAINT IF EXISTS chk_job_skills_required_level;
ALTER TABLE job_skills ADD CONSTRAINT chk_job_skills_required_level
CHECK (required_level IS NULL OR required_level IN ('Junior', 'Middle', 'Senior'));

ALTER TABLE proposals ADD COLUMN IF NOT EXISTS domain_id INT;
ALTER TABLE proposals ADD COLUMN IF NOT EXISTS skill_id INT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_proposals_domain'
    ) THEN
        ALTER TABLE proposals ADD CONSTRAINT fk_proposals_domain
            FOREIGN KEY (domain_id) REFERENCES domains(domain_id) ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_proposals_skill'
    ) THEN
        ALTER TABLE proposals ADD CONSTRAINT fk_proposals_skill
            FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE RESTRICT;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_proposals_domain ON proposals(domain_id);
CREATE INDEX IF NOT EXISTS idx_proposals_skill ON proposals(skill_id);

ALTER TABLE jobs DROP COLUMN IF EXISTS ai_tag;
