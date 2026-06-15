-- NOTE FILE: src/main/resources/db/migration/V21__sow_and_platform_acceptance_criteria.sql
-- Đây là file gì: Migration bổ sung bảng SoW, mô tả milestone và chuyển tiêu chí nghiệm thu thành dữ liệu danh mục của nền tảng.
-- Nhiệm vụ: lưu cấu trúc SoW do AI generate, cho milestone chọn nhiều tiêu chí nghiệm thu và loại bỏ trạng thái pass/fail không còn dùng.

CREATE TABLE IF NOT EXISTS sow (
    sow_id SERIAL PRIMARY KEY,
    job_id INT NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    overview TEXT,
    objectives TEXT,
    scope_of_work TEXT,
    deliverable TEXT,
    assumptions TEXT,
    out_of_scope TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sow_job
        FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sow_job_id ON sow(job_id);

INSERT INTO sow (job_id, title, overview, scope_of_work, deliverable, created_at, updated_at)
SELECT job_id, title, structured_sow, structured_sow, 'Bộ bàn giao theo phạm vi công việc đã thống nhất.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM jobs
WHERE structured_sow IS NOT NULL
ON CONFLICT (job_id) DO NOTHING;

ALTER TABLE milestones ADD COLUMN IF NOT EXISTS description TEXT;

UPDATE milestones
SET description = CASE milestone_id
    WHEN 1 THEN 'Hoàn thiện pipeline nhận diện dữ liệu đầu vào và chuẩn bị mẫu kiểm thử.'
    WHEN 2 THEN 'Huấn luyện, đánh giá mô hình và tinh chỉnh ngưỡng chất lượng.'
    WHEN 3 THEN 'Triển khai luồng kiểm thử, logging và tài liệu vận hành.'
    WHEN 4 THEN 'Hoàn thiện báo cáo nghiệm thu, hướng dẫn bàn giao và cấu hình triển khai.'
    ELSE COALESCE(description, 'Mô tả milestone theo phạm vi công việc của job.')
END
WHERE description IS NULL;

ALTER TABLE acceptance_criteria ADD COLUMN IF NOT EXISTS criteria_code VARCHAR(100);
ALTER TABLE acceptance_criteria ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE acceptance_criteria ADD COLUMN IF NOT EXISTS is_active BOOLEAN;
ALTER TABLE acceptance_criteria ADD COLUMN IF NOT EXISTS sort_order INT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'acceptance_criteria' AND column_name = 'milestone_id'
    ) THEN
        ALTER TABLE acceptance_criteria ALTER COLUMN milestone_id DROP NOT NULL;
    END IF;
END $$;

UPDATE acceptance_criteria
SET criteria_code = COALESCE(NULLIF(criteria_code, ''), 'LEGACY_CRITERIA_' || criteria_id),
    category = COALESCE(NULLIF(category, ''), 'LEGACY'),
    is_active = COALESCE(is_active, TRUE),
    sort_order = COALESCE(sort_order, criteria_id),
    updated_at = CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uq_acceptance_criteria_code ON acceptance_criteria(criteria_code);

INSERT INTO acceptance_criteria (criteria_code, category, description, is_active, sort_order, created_at, updated_at) VALUES
('REQ_CLEAR_SCOPE', 'Yêu cầu', 'Phạm vi, mục tiêu và ràng buộc nghiệp vụ được mô tả rõ ràng, không còn điểm mơ hồ ảnh hưởng triển khai.', TRUE, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REQ_SAMPLE_DATA_READY', 'Yêu cầu', 'Dữ liệu mẫu hoặc dữ liệu kiểm thử được cung cấp đầy đủ để xác minh chức năng chính.', TRUE, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REQ_ACCEPTANCE_DOCUMENTED', 'Yêu cầu', 'Tiêu chí nghiệm thu, dữ liệu đầu vào và kết quả mong đợi được ghi nhận trong tài liệu bàn giao.', TRUE, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DATA_SCHEMA_VALID', 'Dữ liệu', 'Cấu trúc dữ liệu đầu vào và đầu ra đúng schema đã thống nhất.', TRUE, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DATA_QUALITY_CHECKED', 'Dữ liệu', 'Dữ liệu được kiểm tra thiếu, trùng, sai định dạng và có báo cáo chất lượng dữ liệu.', TRUE, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DATA_PRIVACY_PROTECTED', 'Dữ liệu', 'Dữ liệu nhạy cảm được ẩn, mã hóa hoặc xử lý theo yêu cầu bảo mật của dự án.', TRUE, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MODEL_BASELINE_REPORTED', 'Mô hình AI', 'Mô hình có baseline, chỉ số đánh giá và giải thích ngắn gọn về cách đo hiệu quả.', TRUE, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MODEL_METRIC_TARGET_MET', 'Mô hình AI', 'Các chỉ số chất lượng chính đạt ngưỡng mục tiêu đã thống nhất.', TRUE, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MODEL_ERROR_ANALYSIS_INCLUDED', 'Mô hình AI', 'Có phân tích lỗi chính, trường hợp biên và đề xuất cải thiện sau nghiệm thu.', TRUE, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('API_CONTRACT_STABLE', 'API', 'API có endpoint, request, response và mã lỗi ổn định theo tài liệu.', TRUE, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('API_AUTHORIZATION_VALID', 'API', 'API kiểm tra xác thực và phân quyền đúng với vai trò người dùng liên quan.', TRUE, 110, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('API_PERFORMANCE_ACCEPTABLE', 'API', 'Thời gian phản hồi API đạt mức chấp nhận được trên dữ liệu kiểm thử.', TRUE, 120, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UI_FLOW_COMPLETE', 'Giao diện', 'Luồng giao diện chính hoạt động đầy đủ từ nhập liệu, xử lý đến hiển thị kết quả.', TRUE, 130, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UI_RESPONSIVE_VERIFIED', 'Giao diện', 'Giao diện hiển thị ổn định trên kích thước màn hình phổ biến, không vỡ layout.', TRUE, 140, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UI_ERROR_STATE_HANDLED', 'Giao diện', 'Giao diện có trạng thái lỗi, loading và thông báo phản hồi rõ ràng cho người dùng.', TRUE, 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TEST_UNIT_PASSED', 'Kiểm thử', 'Các kiểm thử đơn vị cho logic chính chạy thành công.', TRUE, 160, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TEST_INTEGRATION_PASSED', 'Kiểm thử', 'Các kiểm thử tích hợp cho API, database hoặc dịch vụ liên quan chạy thành công.', TRUE, 170, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TEST_UAT_SCENARIOS_PASSED', 'Kiểm thử', 'Các kịch bản nghiệm thu người dùng được chạy và ghi nhận kết quả đạt.', TRUE, 180, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SECURITY_NO_CRITICAL_ISSUE', 'Bảo mật', 'Không còn lỗi bảo mật nghiêm trọng trong phạm vi bàn giao.', TRUE, 190, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SECURITY_UPLOAD_VALIDATED', 'Bảo mật', 'File upload được kiểm tra loại file, kích thước và quyền truy cập phù hợp.', TRUE, 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DEPLOY_ENV_READY', 'Triển khai', 'Môi trường triển khai có cấu hình, biến môi trường và hướng dẫn chạy rõ ràng.', TRUE, 210, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DEPLOY_ROLLBACK_GUIDE', 'Triển khai', 'Có hướng dẫn rollback hoặc khôi phục khi triển khai gặp lỗi.', TRUE, 220, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DOC_TECHNICAL_COMPLETE', 'Tài liệu', 'Tài liệu kỹ thuật mô tả kiến trúc, cách chạy, cấu hình và các quyết định quan trọng.', TRUE, 230, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DOC_USER_GUIDE_COMPLETE', 'Tài liệu', 'Tài liệu hướng dẫn sử dụng mô tả thao tác chính và lưu ý khi vận hành.', TRUE, 240, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HANDOVER_SOURCE_AVAILABLE', 'Bàn giao', 'Mã nguồn, tài liệu, script và tài nguyên liên quan được bàn giao đầy đủ.', TRUE, 250, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HANDOVER_DEMO_ACCEPTED', 'Bàn giao', 'Demo nghiệm thu được thực hiện và doanh nghiệp xác nhận kết quả phù hợp.', TRUE, 260, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (criteria_code) DO UPDATE SET
    category = EXCLUDED.category,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS milestone_acceptance_criteria (
    milestone_id INT NOT NULL,
    criteria_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (milestone_id, criteria_id),
    CONSTRAINT fk_mac_milestone
        FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id) ON DELETE CASCADE,
    CONSTRAINT fk_mac_criteria
        FOREIGN KEY (criteria_id) REFERENCES acceptance_criteria(criteria_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_mac_criteria_id ON milestone_acceptance_criteria(criteria_id);

INSERT INTO milestone_acceptance_criteria (milestone_id, criteria_id, created_at)
SELECT milestone_id, criteria_id, CURRENT_TIMESTAMP
FROM acceptance_criteria
WHERE milestone_id IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE acceptance_criteria DROP CONSTRAINT IF EXISTS fk_acceptance_criteria_milestone;
ALTER TABLE acceptance_criteria DROP COLUMN IF EXISTS milestone_id;
ALTER TABLE acceptance_criteria DROP COLUMN IF EXISTS is_passed;

ALTER TABLE acceptance_criteria ALTER COLUMN criteria_code SET NOT NULL;
ALTER TABLE acceptance_criteria ALTER COLUMN description SET NOT NULL;
ALTER TABLE acceptance_criteria ALTER COLUMN is_active SET NOT NULL;
ALTER TABLE acceptance_criteria ALTER COLUMN sort_order SET NOT NULL;

SELECT setval(pg_get_serial_sequence('sow', 'sow_id'), COALESCE((SELECT MAX(sow_id) FROM sow), 1), TRUE);
SELECT setval(pg_get_serial_sequence('acceptance_criteria', 'criteria_id'), COALESCE((SELECT MAX(criteria_id) FROM acceptance_criteria), 1), TRUE);
