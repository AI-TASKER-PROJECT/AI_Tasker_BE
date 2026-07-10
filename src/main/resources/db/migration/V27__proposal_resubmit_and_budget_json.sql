-- NOTE FILE: src/main/resources/db/migration/V27__proposal_resubmit_and_budget_json.sql
-- Đây là file gì: Migration chỉnh bảng proposals cho luồng chuyên gia gửi lại proposal và đề xuất ngân sách milestone dạng JSON.
-- Nhiệm vụ: bỏ domain/skill khỏi proposal, thêm mô tả, file proposal và JSON ngân sách milestone đề xuất.

ALTER TABLE proposals DROP COLUMN IF EXISTS domain_id;
ALTER TABLE proposals DROP COLUMN IF EXISTS skill_id;

ALTER TABLE proposals DROP CONSTRAINT IF EXISTS uq_proposals_job_expert;
DROP INDEX IF EXISTS uq_proposals_job_expert_active;
CREATE UNIQUE INDEX uq_proposals_job_expert_active
    ON proposals(job_id, expert_id)
    WHERE lower(status) <> 'rejected';

ALTER TABLE proposals ADD COLUMN IF NOT EXISTS proposal_description TEXT;
ALTER TABLE proposals ADD COLUMN IF NOT EXISTS proposal_file_url VARCHAR(1024);
ALTER TABLE proposals ADD COLUMN IF NOT EXISTS proposal_milestone TEXT;
