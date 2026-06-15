-- NOTE FILE: src/main/resources/db/migration/V25__technology_and_contract_milestones.sql
-- Đây là file gì: Migration bổ sung danh mục technology cho job/portfolio và chi tiết milestone theo hợp đồng.
-- Nhiệm vụ: tạo bảng technologies, job_technologies, nâng bảng contracts, tạo contract_milestones và bỏ technology_used.

CREATE TABLE IF NOT EXISTS technologies (
    technology_id SERIAL PRIMARY KEY,
    technology_code VARCHAR(50) NOT NULL UNIQUE,
    technology_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_technologies (
    job_id INT NOT NULL,
    technology_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (job_id, technology_id),
    CONSTRAINT fk_job_technologies_job
        FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_job_technologies_technology
        FOREIGN KEY (technology_id) REFERENCES technologies(technology_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_job_technologies_technology ON job_technologies(technology_id);

ALTER TABLE portfolios ADD COLUMN IF NOT EXISTS technology_ids TEXT;
UPDATE portfolios SET technology_ids = '' WHERE technology_ids IS NULL;
ALTER TABLE portfolios ALTER COLUMN technology_ids SET NOT NULL;

ALTER TABLE contracts ADD COLUMN IF NOT EXISTS proposal_id INT;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS contract_title VARCHAR(255);
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS business_nda_signed_at TIMESTAMP;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS expert_nda_signed_at TIMESTAMP;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'contracts'
          AND column_name = 'nda_signed'
    ) THEN
        UPDATE contracts
        SET business_nda_signed_at = COALESCE(business_nda_signed_at, updated_at),
            expert_nda_signed_at = COALESCE(expert_nda_signed_at, updated_at)
        WHERE nda_signed = TRUE;
    END IF;
END $$;
ALTER TABLE contracts DROP COLUMN IF EXISTS technology_used;
ALTER TABLE contracts DROP COLUMN IF EXISTS nda_signed;

ALTER TABLE contracts DROP CONSTRAINT IF EXISTS fk_contracts_proposal;
ALTER TABLE contracts ADD CONSTRAINT fk_contracts_proposal
    FOREIGN KEY (proposal_id) REFERENCES proposals(proposal_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_contracts_proposal_id
    ON contracts(proposal_id)
    WHERE proposal_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS contract_milestones (
    contract_milestone_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    job_milestone_id INT NOT NULL,
    milestone_name VARCHAR(255) NOT NULL,
    description TEXT,
    original_budget DECIMAL(18,2) NOT NULL,
    final_budget DECIMAL(18,2) NOT NULL,
    order_index INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contract_milestones_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    CONSTRAINT fk_contract_milestones_job_milestone
        FOREIGN KEY (job_milestone_id) REFERENCES milestones(milestone_id) ON DELETE RESTRICT,
    CONSTRAINT chk_contract_milestones_original_budget
        CHECK (original_budget >= 0),
    CONSTRAINT chk_contract_milestones_final_budget
        CHECK (final_budget >= 0),
    CONSTRAINT chk_contract_milestones_order_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_contract_milestones_status
        CHECK (status IN ('Pending', 'InProgress', 'Submitted', 'Approved', 'Rejected', 'AutoApproved'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_contract_milestones_contract_job_milestone
    ON contract_milestones(contract_id, job_milestone_id);
CREATE INDEX IF NOT EXISTS idx_contract_milestones_contract
    ON contract_milestones(contract_id);

INSERT INTO technologies (technology_code, technology_name, description, sort_order)
VALUES
    ('PYTHON', 'Python', 'Ngôn ngữ lập trình phổ biến cho AI, dữ liệu và backend.', 1),
    ('JAVA_SPRING_BOOT', 'Java Spring Boot', 'Framework backend Java dùng xây dựng REST API và hệ thống doanh nghiệp.', 2),
    ('REACT_TYPESCRIPT', 'React TypeScript', 'Công nghệ frontend dùng xây dựng giao diện web hiện đại.', 3),
    ('POSTGRESQL', 'PostgreSQL', 'Hệ quản trị cơ sở dữ liệu quan hệ.', 4),
    ('REDIS', 'Redis', 'Bộ nhớ đệm và message store tốc độ cao.', 5),
    ('FIREBASE_STORAGE', 'Firebase Storage', 'Dịch vụ lưu trữ file, ảnh và tài liệu trên cloud.', 6),
    ('DOCKER', 'Docker', 'Công nghệ đóng gói và chạy ứng dụng bằng container.', 7),
    ('OPENAI_API', 'OpenAI API', 'API mô hình AI dùng sinh nội dung, phân tích và trợ lý thông minh.', 8),
    ('RAG', 'RAG', 'Kiến trúc truy xuất tri thức kết hợp mô hình ngôn ngữ.', 9),
    ('OCR', 'OCR', 'Công nghệ nhận dạng ký tự từ ảnh hoặc tài liệu.', 10),
    ('LANGCHAIN', 'LangChain', 'Framework xây dựng ứng dụng LLM và workflow AI.', 11),
    ('KAFKA', 'Apache Kafka', 'Nền tảng streaming dữ liệu và xử lý sự kiện.', 12)
ON CONFLICT (technology_code) DO NOTHING;
