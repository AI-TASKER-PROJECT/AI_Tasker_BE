-- NOTE FILE: src/main/resources/db/migration/V19__remove_skill_level_and_audit_trace.sql
-- Đây là file gì: Migration dọn schema theo yêu cầu mới của flow job skill và audit log.
-- Nhiệm vụ: bỏ trình độ yêu cầu của kỹ năng và bỏ dữ liệu truy vết IP/User-Agent khỏi audit log.

ALTER TABLE job_skills DROP CONSTRAINT IF EXISTS chk_job_skills_required_level;
ALTER TABLE job_skills DROP COLUMN IF EXISTS required_level;

ALTER TABLE audit_logs DROP COLUMN IF EXISTS ip_address;
ALTER TABLE audit_logs DROP COLUMN IF EXISTS user_agent;
