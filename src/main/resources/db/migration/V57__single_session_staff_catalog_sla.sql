-- US-056: single active account session, Vietnamese catalog descriptions,
-- additional Staff fixtures, and 3-day milestone review SLA.

ALTER TABLE account
    ADD COLUMN IF NOT EXISTS active_token_version INT NOT NULL DEFAULT 0;

UPDATE system_settings
SET setting_value = '3',
    description = 'Số ngày SLA mặc định để tự động duyệt milestone đang chờ review.',
    updated_at = NOW()
WHERE setting_key = 'default_sla_days';

INSERT INTO system_settings (
    setting_key, setting_value, value_type, description, is_active, updated_by_role_id, created_at, updated_at
)
SELECT 'default_sla_days', '3', 'INT',
       'Số ngày SLA mặc định để tự động duyệt milestone đang chờ review.',
       TRUE, NULL, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_settings WHERE setting_key = 'default_sla_days'
);

INSERT INTO domains (
    domain_code, domain_name, description, is_active, sort_order, created_at, updated_at
)
VALUES (
    'PROFILE_REVIEW',
    'Xét duyệt hồ sơ',
    'Lĩnh vực nội bộ dùng để phân quyền Staff xét duyệt hồ sơ KYB/KYC.',
    TRUE,
    999,
    NOW(),
    NOW()
)
ON CONFLICT (domain_code) DO UPDATE SET
    domain_name = EXCLUDED.domain_name,
    description = EXCLUDED.description,
    is_active = TRUE,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW();

UPDATE domains
SET description = CASE domain_code
    WHEN 'AI_PRODUCT_STRATEGY' THEN 'Khám phá nhu cầu, đánh giá tính khả thi AI, lập lộ trình sản phẩm và đo lường kết quả.'
    WHEN 'GENERATIVE_AI' THEN 'Trợ lý LLM, quy trình tạo nội dung, copilot và tự động hóa bằng AI tạo sinh.'
    WHEN 'NLP' THEN 'Phân loại văn bản, trích xuất thông tin, tóm tắt, tìm kiếm ngữ nghĩa và RAG.'
    WHEN 'COMPUTER_VISION' THEN 'OCR, nhận diện đối tượng, phân loại hình ảnh và kiểm tra chất lượng bằng thị giác máy.'
    WHEN 'DATA_ENGINEERING' THEN 'Pipeline batch/stream, kho dữ liệu, chất lượng dữ liệu và điều phối luồng xử lý.'
    WHEN 'DATA_ANALYTICS_BI' THEN 'Dashboard, mô hình KPI, phân tích khám phá và hệ thống báo cáo.'
    WHEN 'MACHINE_LEARNING' THEN 'Mô hình dự đoán, gợi ý, dự báo và đánh giá hiệu năng mô hình.'
    WHEN 'MLOPS' THEN 'Triển khai, giám sát, quản lý model registry, CI/CD và độ tin cậy hệ thống ML.'
    WHEN 'CLOUD_BACKEND' THEN 'Dịch vụ API, hệ thống phân tán, tích hợp và hạ tầng cloud.'
    WHEN 'WEB_PLATFORM' THEN 'Ứng dụng web hiện đại, kiến trúc frontend và tích hợp API.'
    WHEN 'MOBILE_PRODUCT' THEN 'Ứng dụng di động đa nền tảng, trải nghiệm mobile và tích hợp backend.'
    WHEN 'UX_UI_DESIGN' THEN 'Thiết kế giao diện, luồng người dùng, khả năng truy cập và design system.'
    WHEN 'BRAND_VISUAL_DESIGN' THEN 'Nhận diện thương hiệu, định hướng hình ảnh, thiết kế trình bày và tài sản chiến dịch.'
    WHEN 'PRODUCT_RESEARCH' THEN 'Nghiên cứu người dùng, khám phá thị trường, kiểm thử khả dụng và tổng hợp insight.'
    WHEN 'CYBERSECURITY' THEN 'Bảo mật ứng dụng, threat modeling, hardening và review an toàn.'
    WHEN 'FINTECH_PAYMENTS' THEN 'Cổng thanh toán, escrow, ví, đối soát và luồng giao dịch.'
    WHEN 'ECOMMERCE_RETAIL' THEN 'Catalog, tìm kiếm, gợi ý, CRM và hệ thống trải nghiệm khách hàng bán lẻ.'
    WHEN 'PROCESS_AUTOMATION' THEN 'Tự động hóa quy trình, công cụ nội bộ, luồng phê duyệt và tích hợp.'
    WHEN 'GAME_INTERACTIVE' THEN 'Nguyên mẫu tương tác, mô phỏng, trải nghiệm 2D/3D và công nghệ sáng tạo.'
    WHEN 'VIBE_CODING_CREATIVE_TECH' THEN 'Làm nguyên mẫu nhanh, lập trình với AI và trải nghiệm số giàu tính biểu đạt.'
    ELSE description
END,
updated_at = NOW()
WHERE domain_code IN (
    'AI_PRODUCT_STRATEGY', 'GENERATIVE_AI', 'NLP', 'COMPUTER_VISION',
    'DATA_ENGINEERING', 'DATA_ANALYTICS_BI', 'MACHINE_LEARNING', 'MLOPS',
    'CLOUD_BACKEND', 'WEB_PLATFORM', 'MOBILE_PRODUCT', 'UX_UI_DESIGN',
    'BRAND_VISUAL_DESIGN', 'PRODUCT_RESEARCH', 'CYBERSECURITY',
    'FINTECH_PAYMENTS', 'ECOMMERCE_RETAIL', 'PROCESS_AUTOMATION',
    'GAME_INTERACTIVE', 'VIBE_CODING_CREATIVE_TECH'
);

UPDATE skills
SET description = CASE skill_code
    WHEN 'PROMPT_ENGINEERING' THEN 'Thiết kế chỉ dẫn, mẫu prompt, đánh giá và đầu ra có cấu trúc.'
    WHEN 'RAG_ARCHITECTURE' THEN 'Truy xuất, rerank, grounding, trích dẫn nguồn và thiết kế knowledge base.'
    WHEN 'LLM_TOOL_CALLING' THEN 'Function calling, agent workflow, định tuyến công cụ và guardrail.'
    WHEN 'MODEL_EVALUATION' THEN 'Đánh giá offline/online, thiết kế benchmark, tập test và chỉ số chất lượng.'
    WHEN 'PYTHON' THEN 'Dịch vụ backend, script, xử lý dữ liệu và tự động hóa bằng Python.'
    WHEN 'JAVA_SPRING_BOOT' THEN 'REST API, bảo mật, persistence và phát triển backend doanh nghiệp.'
    WHEN 'REACT_TYPESCRIPT' THEN 'Kiến trúc component, quản lý state, form và UI kết nối API.'
    WHEN 'POSTGRESQL' THEN 'Mô hình quan hệ, index, transaction và tối ưu truy vấn.'
    WHEN 'DOCKER_DEVOPS' THEN 'Đóng gói container, parity môi trường local, CI/CD và triển khai.'
    WHEN 'FIREBASE_STORAGE' THEN 'Upload file, tài sản công khai/riêng tư, lưu trữ tài liệu và mẫu truy cập.'
    WHEN 'PAYOS_INTEGRATION' THEN 'Luồng payment link, webhook, đối soát và nạp tiền vào ví.'
    WHEN 'DATA_PIPELINE' THEN 'Thiết kế ETL/ELT, điều phối, kiểm định và giám sát pipeline.'
    WHEN 'POWER_BI_DASHBOARD' THEN 'Mô hình metric, trực quan hóa, drill-down và dashboard cho stakeholder.'
    WHEN 'OCR_PIPELINE' THEN 'Tiền xử lý tài liệu, trích xuất, kiểm định và quy trình review.'
    WHEN 'COMPUTER_VISION_MODELING' THEN 'Phát hiện, phân loại, segmentation và chuẩn bị tập dữ liệu hình ảnh.'
    WHEN 'UX_RESEARCH' THEN 'Phỏng vấn, kiểm thử khả dụng, bản đồ hành trình và tổng hợp insight.'
    WHEN 'DESIGN_SYSTEMS' THEN 'Component UI tái sử dụng, token, trạng thái tương tác và accessibility.'
    WHEN 'SECURE_AUTH_JWT' THEN 'Bcrypt, JWT, quy tắc phân quyền và kiểm soát vòng đời tài khoản.'
    WHEN 'API_TESTING_SWAGGER' THEN 'Tài liệu hợp đồng API, kiểm thử Postman và regression check.'
    WHEN 'CREATIVE_PROTOTYPING' THEN 'Thử nghiệm UI/sản phẩm nhanh, vibe coding và xác thực ý tưởng tương tác.'
    ELSE description
END,
updated_at = NOW()
WHERE skill_code IN (
    'PROMPT_ENGINEERING', 'RAG_ARCHITECTURE', 'LLM_TOOL_CALLING',
    'MODEL_EVALUATION', 'PYTHON', 'JAVA_SPRING_BOOT', 'REACT_TYPESCRIPT',
    'POSTGRESQL', 'DOCKER_DEVOPS', 'FIREBASE_STORAGE', 'PAYOS_INTEGRATION',
    'DATA_PIPELINE', 'POWER_BI_DASHBOARD', 'OCR_PIPELINE',
    'COMPUTER_VISION_MODELING', 'UX_RESEARCH', 'DESIGN_SYSTEMS',
    'SECURE_AUTH_JWT', 'API_TESTING_SWAGGER', 'CREATIVE_PROTOTYPING'
);

UPDATE staffs st
SET specialization = 'Xét duyệt hồ sơ KYB/KYC',
    updated_at = NOW()
FROM account a
WHERE st.account_id = a.account_id
  AND a.email = 'staff@aitasker.local';

WITH staff_profile_review AS (
    SELECT st.staff_id, d.domain_id
    FROM account a
    JOIN staffs st ON st.account_id = a.account_id
    JOIN domains d ON d.domain_code = 'PROFILE_REVIEW'
    WHERE a.email = 'staff@aitasker.local'
)
DELETE FROM staff_domains sd
USING staff_profile_review spr
WHERE sd.staff_id = spr.staff_id
  AND sd.domain_id <> spr.domain_id;

WITH staff_profile_review AS (
    SELECT st.staff_id, d.domain_id
    FROM account a
    JOIN staffs st ON st.account_id = a.account_id
    JOIN domains d ON d.domain_code = 'PROFILE_REVIEW'
    WHERE a.email = 'staff@aitasker.local'
)
INSERT INTO staff_domains (staff_id, domain_id)
SELECT staff_id, domain_id
FROM staff_profile_review
ON CONFLICT (staff_id, domain_id) DO NOTHING;

WITH staff_seed(email, phone, full_name, specialization) AS (
    VALUES
        ('staff01@aitasker.local', '0902000001', 'Nguyen Minh An', 'AI product strategy, RAG review, and contract dispute analysis'),
        ('staff02@aitasker.local', '0902000002', 'Tran Bao Chau', 'Computer vision, OCR evidence review, and quality assurance'),
        ('staff03@aitasker.local', '0902000003', 'Le Quang Duy', 'Data engineering, BI analytics, and reporting validation'),
        ('staff04@aitasker.local', '0902000004', 'Pham Thu Ha', 'MLOps, cloud backend, and deployment incident review'),
        ('staff05@aitasker.local', '0902000005', 'Hoang Gia Huy', 'Fintech payments, wallet reconciliation, and transaction disputes'),
        ('staff06@aitasker.local', '0902000006', 'Do Ngoc Khanh', 'UX research, design systems, and product acceptance review'),
        ('staff07@aitasker.local', '0902000007', 'Bui Lan Nhi', 'Cybersecurity, secure auth, and API testing review'),
        ('staff08@aitasker.local', '0902000008', 'Vo Thanh Phuc', 'E-commerce retail, process automation, and CRM workflows'),
        ('staff09@aitasker.local', '0902000009', 'Dang Mai Trang', 'Mobile product, web platform, and frontend integration review'),
        ('staff10@aitasker.local', '0902000010', 'Phan Duc Viet', 'Game interactive, creative prototyping, and AI-assisted product validation')
)
INSERT INTO account (
    email, password, phone, full_name, role_id, status, email_verified,
    active_token_version, created_at, updated_at
)
SELECT
    email,
    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC',
    phone,
    full_name,
    4,
    'Approved',
    TRUE,
    0,
    NOW(),
    NOW()
FROM staff_seed
ON CONFLICT (email) DO UPDATE SET
    phone = EXCLUDED.phone,
    full_name = EXCLUDED.full_name,
    role_id = 4,
    status = 'Approved',
    email_verified = TRUE,
    updated_at = NOW();

WITH staff_seed(email, specialization) AS (
    VALUES
        ('staff01@aitasker.local', 'AI product strategy, RAG review, and contract dispute analysis'),
        ('staff02@aitasker.local', 'Computer vision, OCR evidence review, and quality assurance'),
        ('staff03@aitasker.local', 'Data engineering, BI analytics, and reporting validation'),
        ('staff04@aitasker.local', 'MLOps, cloud backend, and deployment incident review'),
        ('staff05@aitasker.local', 'Fintech payments, wallet reconciliation, and transaction disputes'),
        ('staff06@aitasker.local', 'UX research, design systems, and product acceptance review'),
        ('staff07@aitasker.local', 'Cybersecurity, secure auth, and API testing review'),
        ('staff08@aitasker.local', 'E-commerce retail, process automation, and CRM workflows'),
        ('staff09@aitasker.local', 'Mobile product, web platform, and frontend integration review'),
        ('staff10@aitasker.local', 'Game interactive, creative prototyping, and AI-assisted product validation')
)
INSERT INTO staffs (account_id, specialization, created_at, updated_at)
SELECT a.account_id, s.specialization, NOW(), NOW()
FROM staff_seed s
JOIN account a ON a.email = s.email
ON CONFLICT (account_id) DO UPDATE SET
    specialization = EXCLUDED.specialization,
    updated_at = NOW();

WITH staff_domain_seed(email, domain_code) AS (
    VALUES
        ('staff01@aitasker.local', 'AI_PRODUCT_STRATEGY'),
        ('staff01@aitasker.local', 'GENERATIVE_AI'),
        ('staff01@aitasker.local', 'NLP'),
        ('staff02@aitasker.local', 'COMPUTER_VISION'),
        ('staff02@aitasker.local', 'MACHINE_LEARNING'),
        ('staff03@aitasker.local', 'DATA_ENGINEERING'),
        ('staff03@aitasker.local', 'DATA_ANALYTICS_BI'),
        ('staff04@aitasker.local', 'MLOPS'),
        ('staff04@aitasker.local', 'CLOUD_BACKEND'),
        ('staff05@aitasker.local', 'FINTECH_PAYMENTS'),
        ('staff05@aitasker.local', 'CYBERSECURITY'),
        ('staff06@aitasker.local', 'UX_UI_DESIGN'),
        ('staff06@aitasker.local', 'PRODUCT_RESEARCH'),
        ('staff07@aitasker.local', 'CYBERSECURITY'),
        ('staff07@aitasker.local', 'CLOUD_BACKEND'),
        ('staff08@aitasker.local', 'ECOMMERCE_RETAIL'),
        ('staff08@aitasker.local', 'PROCESS_AUTOMATION'),
        ('staff09@aitasker.local', 'WEB_PLATFORM'),
        ('staff09@aitasker.local', 'MOBILE_PRODUCT'),
        ('staff10@aitasker.local', 'GAME_INTERACTIVE'),
        ('staff10@aitasker.local', 'VIBE_CODING_CREATIVE_TECH')
)
INSERT INTO staff_domains (staff_id, domain_id)
SELECT st.staff_id, d.domain_id
FROM staff_domain_seed s
JOIN account a ON a.email = s.email
JOIN staffs st ON st.account_id = a.account_id
JOIN domains d ON d.domain_code = s.domain_code
ON CONFLICT (staff_id, domain_id) DO NOTHING;

WITH staff_skill_seed(email, skill_code) AS (
    VALUES
        ('staff01@aitasker.local', 'PROMPT_ENGINEERING'),
        ('staff01@aitasker.local', 'RAG_ARCHITECTURE'),
        ('staff01@aitasker.local', 'LLM_TOOL_CALLING'),
        ('staff02@aitasker.local', 'OCR_PIPELINE'),
        ('staff02@aitasker.local', 'COMPUTER_VISION_MODELING'),
        ('staff02@aitasker.local', 'MODEL_EVALUATION'),
        ('staff03@aitasker.local', 'DATA_PIPELINE'),
        ('staff03@aitasker.local', 'POWER_BI_DASHBOARD'),
        ('staff03@aitasker.local', 'POSTGRESQL'),
        ('staff04@aitasker.local', 'DOCKER_DEVOPS'),
        ('staff04@aitasker.local', 'JAVA_SPRING_BOOT'),
        ('staff04@aitasker.local', 'POSTGRESQL'),
        ('staff05@aitasker.local', 'PAYOS_INTEGRATION'),
        ('staff05@aitasker.local', 'SECURE_AUTH_JWT'),
        ('staff05@aitasker.local', 'API_TESTING_SWAGGER'),
        ('staff06@aitasker.local', 'UX_RESEARCH'),
        ('staff06@aitasker.local', 'DESIGN_SYSTEMS'),
        ('staff07@aitasker.local', 'SECURE_AUTH_JWT'),
        ('staff07@aitasker.local', 'API_TESTING_SWAGGER'),
        ('staff08@aitasker.local', 'DATA_PIPELINE'),
        ('staff08@aitasker.local', 'REACT_TYPESCRIPT'),
        ('staff09@aitasker.local', 'REACT_TYPESCRIPT'),
        ('staff09@aitasker.local', 'FIREBASE_STORAGE'),
        ('staff10@aitasker.local', 'CREATIVE_PROTOTYPING'),
        ('staff10@aitasker.local', 'PROMPT_ENGINEERING')
)
INSERT INTO staff_skills (staff_id, skill_id)
SELECT st.staff_id, sk.skill_id
FROM staff_skill_seed s
JOIN account a ON a.email = s.email
JOIN staffs st ON st.account_id = a.account_id
JOIN skills sk ON sk.skill_code = s.skill_code
ON CONFLICT (staff_id, skill_id) DO NOTHING;
