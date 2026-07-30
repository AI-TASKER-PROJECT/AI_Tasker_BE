-- Replace all accumulated demo/account/catalog-owned data with one coherent,
-- deterministic dataset. System settings, packages, roles, knowledge, and the
-- exact credentials of the four internal accounts are preserved.

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM account WHERE account_id IN (1, 2, 3, 4)) <> 4
       OR NOT EXISTS (SELECT 1 FROM account WHERE account_id = 1 AND email = 'business@aitasker.local')
       OR NOT EXISTS (SELECT 1 FROM account WHERE account_id = 2 AND email = 'expert@aitasker.local')
       OR NOT EXISTS (SELECT 1 FROM account WHERE account_id = 3 AND email = 'admin@aitasker.local')
       OR NOT EXISTS (SELECT 1 FROM account WHERE account_id = 4 AND email = 'staff@aitasker.local') THEN
        RAISE EXCEPTION 'Cannot rebuild demo data: the four internal accounts are missing or have unexpected ids';
    END IF;
END $$;

CREATE TEMP TABLE preserved_internal_accounts AS
SELECT * FROM account WHERE account_id IN (1, 2, 3, 4);

TRUNCATE TABLE account, domains, skills, technologies RESTART IDENTITY CASCADE;

INSERT INTO account
SELECT * FROM preserved_internal_accounts ORDER BY account_id;

-- Internal credentials remain byte-for-byte unchanged. Only presentation and
-- mutable account-state fields are normalized.
UPDATE account
SET full_name = CASE account_id
        WHEN 1 THEN 'Nguyễn Minh Quân'
        WHEN 2 THEN 'Trần Hoàng Nam'
        WHEN 3 THEN 'Lê Thu Hà'
        WHEN 4 THEN 'Phạm Quốc Huy'
    END,
    phone = CASE account_id
        WHEN 1 THEN '0901000001'
        WHEN 2 THEN '0901000002'
        WHEN 3 THEN '0901000003'
        WHEN 4 THEN '0901000004'
    END,
    status = 'Approved',
    email_verified = TRUE,
    failed_login_attempts = 0,
    lockout_count = 0,
    locked_until = NULL,
    lock_reason = NULL,
    last_failed_login_at = NULL,
    status_before_lock = NULL,
    active_token_version = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE account_id IN (1, 2, 3, 4);

INSERT INTO domains (domain_id, domain_code, domain_name, description, is_active, sort_order, created_at, updated_at) VALUES
    (1,  'ARTIFICIAL_INTELLIGENCE', 'Artificial Intelligence', 'Giải pháp trí tuệ nhân tạo tổng quát cho sản phẩm và vận hành.', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2,  'MACHINE_LEARNING', 'Machine Learning', 'Mô hình học máy có giám sát, không giám sát và học tăng cường.', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3,  'GENERATIVE_AI', 'Generative AI', 'Ứng dụng mô hình tạo sinh cho nội dung, trợ lý và tự động hóa.', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4,  'NATURAL_LANGUAGE_PROCESSING', 'Natural Language Processing', 'Xử lý, phân loại, tìm kiếm và sinh ngôn ngữ tự nhiên.', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5,  'COMPUTER_VISION', 'Computer Vision', 'Phân tích ảnh, video, OCR và nhận dạng đối tượng.', TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6,  'SPEECH_AUDIO_AI', 'Speech and Audio AI', 'Nhận dạng tiếng nói, tổng hợp giọng nói và phân tích âm thanh.', TRUE, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7,  'RECOMMENDER_SYSTEMS', 'Recommender Systems', 'Cá nhân hóa và xếp hạng sản phẩm, nội dung hoặc ứng viên.', TRUE, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8,  'DATA_SCIENCE', 'Data Science', 'Phân tích định lượng, thí nghiệm và mô hình dự báo.', TRUE, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9,  'DATA_ENGINEERING', 'Data Engineering', 'Kho dữ liệu, pipeline dữ liệu và xử lý theo lô hoặc thời gian thực.', TRUE, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'BUSINESS_INTELLIGENCE', 'Business Intelligence', 'Mô hình dữ liệu và dashboard phục vụ quyết định kinh doanh.', TRUE, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'ADVANCED_ANALYTICS', 'Advanced Analytics', 'Phân tích nguyên nhân, dự báo và tối ưu hóa hoạt động.', TRUE, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'MLOPS', 'MLOps', 'Triển khai, giám sát, quản trị phiên bản và vận hành mô hình.', TRUE, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'AI_PLATFORM', 'AI Platform Engineering', 'Nền tảng và API dùng chung cho nhiều ứng dụng AI.', TRUE, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'CLOUD_AI', 'Cloud AI', 'Giải pháp AI trên hạ tầng điện toán đám mây.', TRUE, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'EDGE_AI_IOT', 'Edge AI and IoT', 'Suy luận tại thiết bị biên và hệ thống cảm biến thông minh.', TRUE, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'ROBOTICS_AUTOMATION', 'Robotics and Automation', 'Điều khiển robot và tự động hóa quy trình vật lý.', TRUE, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'HEALTHCARE_AI', 'Healthcare AI', 'Hỗ trợ phân tích dữ liệu và quy trình chăm sóc sức khỏe.', TRUE, 17, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 'FINTECH_AI', 'Fintech AI', 'Chấm điểm, phát hiện gian lận và tự động hóa dịch vụ tài chính.', TRUE, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 'RETAIL_ECOMMERCE_AI', 'Retail and E-commerce AI', 'Cá nhân hóa, dự báo nhu cầu và tối ưu vận hành bán lẻ.', TRUE, 19, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 'MANUFACTURING_AI', 'Manufacturing AI', 'Bảo trì dự báo, thị giác kiểm lỗi và tối ưu sản xuất.', TRUE, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'LOGISTICS_AI', 'Logistics AI', 'Tối ưu tuyến đường, tồn kho và năng lực giao nhận.', TRUE, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 'AGRICULTURE_AI', 'Agriculture AI', 'Phân tích mùa vụ, sâu bệnh và tài nguyên nông nghiệp.', TRUE, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'EDUCATION_AI', 'Education AI', 'Cá nhân hóa học tập và hỗ trợ quản lý giáo dục.', TRUE, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 'CYBERSECURITY_AI', 'Cybersecurity AI', 'Phát hiện bất thường, rủi ro và mối đe dọa an ninh.', TRUE, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 'LEGAL_AI', 'Legal AI', 'Tìm kiếm, phân loại và trích xuất thông tin pháp lý.', TRUE, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 'MARKETING_AI', 'Marketing AI', 'Phân khúc khách hàng và tối ưu chiến dịch tiếp thị.', TRUE, 26, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'CUSTOMER_SERVICE_AI', 'Customer Service AI', 'Trợ lý hội thoại, phân luồng và hỗ trợ khách hàng.', TRUE, 27, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 'DOCUMENT_INTELLIGENCE', 'Document Intelligence', 'OCR, trích xuất và kiểm tra dữ liệu tài liệu.', TRUE, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 'AI_QUALITY_ASSURANCE', 'AI Quality Assurance', 'Đánh giá chất lượng, độ an toàn và khả năng hồi quy của hệ thống AI.', TRUE, 29, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 'PROFILE_REVIEW', 'Profile Review', 'Lĩnh vực nội bộ dành riêng cho nhân sự xét duyệt hồ sơ.', TRUE, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO skills (skill_id, skill_code, skill_name, description, is_active, created_at, updated_at) VALUES
    (1, 'PYTHON_PROGRAMMING', 'Python Programming', 'Phát triển dịch vụ, pipeline và mô hình bằng Python.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'JAVA_PROGRAMMING', 'Java Programming', 'Phát triển backend và hệ thống doanh nghiệp bằng Java.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'TYPESCRIPT_PROGRAMMING', 'TypeScript Programming', 'Phát triển ứng dụng web an toàn kiểu dữ liệu.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'SQL_QUERYING', 'SQL Querying', 'Thiết kế và tối ưu truy vấn dữ liệu quan hệ.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'DATA_MODELING', 'Data Modeling', 'Thiết kế mô hình dữ liệu nghiệp vụ và phân tích.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'ETL_PIPELINES', 'ETL and ELT Pipelines', 'Xây dựng pipeline thu thập, biến đổi và kiểm soát dữ liệu.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'STATISTICAL_ANALYSIS', 'Statistical Analysis', 'Phân tích thống kê, thí nghiệm và suy luận.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'MACHINE_LEARNING', 'Machine Learning', 'Huấn luyện và đánh giá mô hình học máy.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'DEEP_LEARNING', 'Deep Learning', 'Xây dựng và tối ưu mạng nơ-ron sâu.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'NLP_ENGINEERING', 'NLP Engineering', 'Tiền xử lý, tìm kiếm và mô hình hóa ngôn ngữ.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'COMPUTER_VISION', 'Computer Vision', 'Xử lý ảnh, video và nhận dạng đối tượng.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'LLM_APPLICATIONS', 'LLM Application Development', 'Xây dựng ứng dụng dựa trên mô hình ngôn ngữ lớn.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'PROMPT_ENGINEERING', 'Prompt Engineering', 'Thiết kế, kiểm thử và quản trị prompt.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'RAG_SYSTEMS', 'Retrieval-Augmented Generation', 'Thiết kế truy xuất tri thức cho ứng dụng tạo sinh.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'VECTOR_SEARCH', 'Vector Search', 'Lập chỉ mục và truy vấn dữ liệu vector.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'FEATURE_ENGINEERING', 'Feature Engineering', 'Tạo và quản trị đặc trưng cho mô hình.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'MODEL_EVALUATION', 'Model Evaluation', 'Xây dựng bộ đo, benchmark và kiểm tra sai lệch.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 'MLOPS_OPERATIONS', 'MLOps Operations', 'Đóng gói, triển khai và giám sát mô hình.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 'API_DESIGN', 'API Design', 'Thiết kế REST API rõ ràng, an toàn và có phiên bản.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 'MICROSERVICES', 'Microservices', 'Thiết kế dịch vụ phân tán và tích hợp sự kiện.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'TEST_AUTOMATION', 'Test Automation', 'Tự động hóa kiểm thử API, UI và hồi quy.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 'DATA_VISUALIZATION', 'Data Visualization', 'Trình bày dữ liệu bằng dashboard và biểu đồ.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'CLOUD_ARCHITECTURE', 'Cloud Architecture', 'Thiết kế hạ tầng đám mây có khả năng mở rộng.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 'CONTAINERIZATION', 'Containerization', 'Đóng gói và vận hành workload bằng container.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 'KUBERNETES_OPERATIONS', 'Kubernetes Operations', 'Triển khai và quản trị workload trên Kubernetes.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 'CI_CD', 'CI/CD', 'Tự động hóa build, kiểm thử và phát hành.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'SECURITY_ENGINEERING', 'Security Engineering', 'Thiết kế kiểm soát truy cập và bảo vệ dữ liệu.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 'PRODUCT_DISCOVERY', 'Product Discovery', 'Làm rõ vấn đề, người dùng và tiêu chí thành công.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 'PROJECT_MANAGEMENT', 'Project Management', 'Lập kế hoạch, quản trị phạm vi và rủi ro dự án.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 'TECHNICAL_WRITING', 'Technical Writing', 'Viết tài liệu kỹ thuật, vận hành và bàn giao.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO technologies (technology_id, technology_code, technology_name, description, is_active, sort_order, created_at, updated_at) VALUES
    (1, 'PYTHON', 'Python', 'Ngôn ngữ cho dữ liệu, AI và dịch vụ backend.', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'SPRING_BOOT', 'Spring Boot', 'Framework Java cho dịch vụ doanh nghiệp.', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'TYPESCRIPT', 'TypeScript', 'Ngôn ngữ typed cho web và Node.js.', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'REACT', 'React', 'Thư viện xây dựng giao diện web.', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'POSTGRESQL', 'PostgreSQL', 'Cơ sở dữ liệu quan hệ có hỗ trợ JSON và vector.', TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'REDIS', 'Redis', 'Kho dữ liệu trong bộ nhớ cho cache và hàng đợi nhẹ.', TRUE, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'DOCKER', 'Docker', 'Đóng gói môi trường chạy bằng container.', TRUE, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'KUBERNETES', 'Kubernetes', 'Điều phối và mở rộng container.', TRUE, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'AWS', 'Amazon Web Services', 'Nền tảng điện toán đám mây AWS.', TRUE, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'AZURE', 'Microsoft Azure', 'Nền tảng điện toán đám mây Azure.', TRUE, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'GCP', 'Google Cloud Platform', 'Nền tảng điện toán đám mây Google.', TRUE, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'PYTORCH', 'PyTorch', 'Framework học sâu và nghiên cứu mô hình.', TRUE, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'TENSORFLOW', 'TensorFlow', 'Framework huấn luyện và triển khai mô hình.', TRUE, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'SCIKIT_LEARN', 'scikit-learn', 'Thư viện học máy truyền thống cho Python.', TRUE, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'HUGGING_FACE', 'Hugging Face', 'Hệ sinh thái mô hình và dữ liệu AI mở.', TRUE, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'OPENAI_API', 'OpenAI API', 'API mô hình tạo sinh và embedding.', TRUE, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'LANGCHAIN', 'LangChain', 'Framework điều phối ứng dụng LLM.', TRUE, 17, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 'LLAMAINDEX', 'LlamaIndex', 'Framework dữ liệu và truy xuất cho LLM.', TRUE, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 'PGVECTOR', 'pgvector', 'Phần mở rộng tìm kiếm vector cho PostgreSQL.', TRUE, 19, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 'KAFKA', 'Apache Kafka', 'Nền tảng streaming và tích hợp sự kiện.', TRUE, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'AIRFLOW', 'Apache Airflow', 'Điều phối pipeline dữ liệu theo lịch.', TRUE, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 'SPARK', 'Apache Spark', 'Xử lý dữ liệu phân tán quy mô lớn.', TRUE, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'DBT', 'dbt', 'Biến đổi và kiểm thử dữ liệu phân tích.', TRUE, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 'POWER_BI', 'Power BI', 'Nền tảng dashboard và phân tích doanh nghiệp.', TRUE, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 'TABLEAU', 'Tableau', 'Nền tảng trực quan hóa và khám phá dữ liệu.', TRUE, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 'MLFLOW', 'MLflow', 'Theo dõi thí nghiệm và quản trị vòng đời mô hình.', TRUE, 26, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'FASTAPI', 'FastAPI', 'Framework Python cho API hiệu năng cao.', TRUE, 27, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 'NODE_JS', 'Node.js', 'Runtime JavaScript cho dịch vụ và công cụ.', TRUE, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 'OPENCV', 'OpenCV', 'Thư viện xử lý ảnh và thị giác máy tính.', TRUE, 29, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 'PLAYWRIGHT', 'Playwright', 'Framework tự động hóa và kiểm thử trình duyệt.', TRUE, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- All non-internal accounts share the documented demo password 12345678.
INSERT INTO account (account_id, email, password, phone, full_name, role_id, status, email_verified, created_at, updated_at) VALUES
    (5,  'business.anphu@aitasker.local',       '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000005', 'Đặng Thu Trang', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '170 days', CURRENT_TIMESTAMP),
    (6,  'business.binhminh@aitasker.local',    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000006', 'Võ Hoàng Long', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '160 days', CURRENT_TIMESTAMP),
    (7,  'business.cuulong@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000007', 'Nguyễn Thị Mai', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '150 days', CURRENT_TIMESTAMP),
    (8,  'business.daiviet@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000008', 'Trần Quốc Bảo', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '140 days', CURRENT_TIMESTAMP),
    (9,  'business.ecomviet@aitasker.local',     '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000009', 'Lê Ngọc Hân', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '130 days', CURRENT_TIMESTAMP),
    (10, 'business.farmtech@aitasker.local',     '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000010', 'Phạm Đức Minh', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '120 days', CURRENT_TIMESTAMP),
    (11, 'business.giaoducso@aitasker.local',   '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000011', 'Bùi Thanh Thảo', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '110 days', CURRENT_TIMESTAMP),
    (12, 'business.haidang@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000012', 'Hồ Minh Khang', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP),
    (13, 'business.innotech@aitasker.local',    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0902000013', 'Đỗ Hải Yến', 1, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP),
    (14, 'expert.ngocanh@aitasker.local',        '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000014', 'Nguyễn Ngọc Anh', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '175 days', CURRENT_TIMESTAMP),
    (15, 'expert.quanghuy@aitasker.local',       '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000015', 'Lê Quang Huy', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '165 days', CURRENT_TIMESTAMP),
    (16, 'expert.thuphuong@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000016', 'Trần Thu Phương', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '155 days', CURRENT_TIMESTAMP),
    (17, 'expert.minhkhoi@aitasker.local',       '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000017', 'Phạm Minh Khôi', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '145 days', CURRENT_TIMESTAMP),
    (18, 'expert.hoaian@aitasker.local',         '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000018', 'Vũ Hoài An', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '135 days', CURRENT_TIMESTAMP),
    (19, 'expert.tuankiet@aitasker.local',       '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000019', 'Đặng Tuấn Kiệt', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '125 days', CURRENT_TIMESTAMP),
    (20, 'expert.lanchi@aitasker.local',         '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000020', 'Bùi Lan Chi', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '115 days', CURRENT_TIMESTAMP),
    (21, 'expert.giabao@aitasker.local',         '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000021', 'Hoàng Gia Bảo', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '105 days', CURRENT_TIMESTAMP),
    (22, 'expert.khanhlinh@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0913000022', 'Đỗ Khánh Linh', 2, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '95 days', CURRENT_TIMESTAMP),
    (23, 'staff.aiOperations@aitasker.local',   '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000023', 'Nguyễn Minh An', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '180 days', CURRENT_TIMESTAMP),
    (24, 'staff.dataQuality@aitasker.local',    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000024', 'Trần Bảo Châu', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '170 days', CURRENT_TIMESTAMP),
    (25, 'staff.computerVision@aitasker.local', '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000025', 'Lê Quang Duy', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '160 days', CURRENT_TIMESTAMP),
    (26, 'staff.fintechRisk@aitasker.local',    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000026', 'Phạm Thu Hà', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '150 days', CURRENT_TIMESTAMP),
    (27, 'staff.retailSupport@aitasker.local',  '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000027', 'Hoàng Gia Huy', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '140 days', CURRENT_TIMESTAMP),
    (28, 'staff.logistics@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000028', 'Đỗ Ngọc Khánh', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '130 days', CURRENT_TIMESTAMP),
    (29, 'staff.healthcare@aitasker.local',     '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000029', 'Bùi Lan Nhi', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '120 days', CURRENT_TIMESTAMP),
    (30, 'staff.education@aitasker.local',      '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000030', 'Võ Thành Phúc', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '110 days', CURRENT_TIMESTAMP),
    (31, 'staff.security@aitasker.local',       '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0924000031', 'Đặng Mai Trang', 4, 'Approved', TRUE, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP);

INSERT INTO staffs (staff_id, account_id, specialization, created_at, updated_at) VALUES
    (1, 4,  'Profile Review', CURRENT_TIMESTAMP - INTERVAL '180 days', CURRENT_TIMESTAMP),
    (2, 23, 'AI Platform Operations', CURRENT_TIMESTAMP - INTERVAL '175 days', CURRENT_TIMESTAMP),
    (3, 24, 'Data Quality and Analytics', CURRENT_TIMESTAMP - INTERVAL '165 days', CURRENT_TIMESTAMP),
    (4, 25, 'Computer Vision Delivery', CURRENT_TIMESTAMP - INTERVAL '155 days', CURRENT_TIMESTAMP),
    (5, 26, 'Fintech Risk', CURRENT_TIMESTAMP - INTERVAL '145 days', CURRENT_TIMESTAMP),
    (6, 27, 'Retail and Customer Service', CURRENT_TIMESTAMP - INTERVAL '135 days', CURRENT_TIMESTAMP),
    (7, 28, 'Logistics Optimization', CURRENT_TIMESTAMP - INTERVAL '125 days', CURRENT_TIMESTAMP),
    (8, 29, 'Healthcare Data', CURRENT_TIMESTAMP - INTERVAL '115 days', CURRENT_TIMESTAMP),
    (9, 30, 'Education Technology', CURRENT_TIMESTAMP - INTERVAL '105 days', CURRENT_TIMESTAMP),
    (10, 31, 'AI Security and Quality', CURRENT_TIMESTAMP - INTERVAL '95 days', CURRENT_TIMESTAMP);

INSERT INTO business_profiles (business_id, account_id, tax_code, company_name, address, business_license_url, kyb_status, approved_by, verified_representative, created_at, updated_at) VALUES
    (1, 1,  'DEMO-TAX-BIZ-001', 'Công ty Demo Nova Retail', 'Quận 1, Thành phố Hồ Chí Minh', 'https://assets.example.test/business/demo-nova-retail-license.pdf', 'Approved', 1, 'Nguyễn Minh Quân', CURRENT_TIMESTAMP - INTERVAL '178 days', CURRENT_TIMESTAMP),
    (2, 5,  'DEMO-TAX-BIZ-002', 'Công ty Demo An Phú Analytics', 'Quận Hải Châu, Thành phố Đà Nẵng', 'https://assets.example.test/business/demo-an-phu-license.pdf', 'Approved', 1, 'Đặng Thu Trang', CURRENT_TIMESTAMP - INTERVAL '168 days', CURRENT_TIMESTAMP),
    (3, 6,  'DEMO-TAX-BIZ-003', 'Công ty Demo Bình Minh Logistics', 'Thành phố Thủ Đức, Thành phố Hồ Chí Minh', 'https://assets.example.test/business/demo-binh-minh-license.pdf', 'Approved', 1, 'Võ Hoàng Long', CURRENT_TIMESTAMP - INTERVAL '158 days', CURRENT_TIMESTAMP),
    (4, 7,  'DEMO-TAX-BIZ-004', 'Công ty Demo Cửu Long Fintech', 'Quận Ninh Kiều, Thành phố Cần Thơ', 'https://assets.example.test/business/demo-cuu-long-license.pdf', 'Approved', 1, 'Nguyễn Thị Mai', CURRENT_TIMESTAMP - INTERVAL '148 days', CURRENT_TIMESTAMP),
    (5, 8,  'DEMO-TAX-BIZ-005', 'Công ty Demo Đại Việt Manufacturing', 'Thành phố Biên Hòa, Tỉnh Đồng Nai', 'https://assets.example.test/business/demo-dai-viet-license.pdf', 'Approved', 1, 'Trần Quốc Bảo', CURRENT_TIMESTAMP - INTERVAL '138 days', CURRENT_TIMESTAMP),
    (6, 9,  'DEMO-TAX-BIZ-006', 'Công ty Demo Ecom Việt', 'Quận Cầu Giấy, Thành phố Hà Nội', 'https://assets.example.test/business/demo-ecom-viet-license.pdf', 'Approved', 1, 'Lê Ngọc Hân', CURRENT_TIMESTAMP - INTERVAL '128 days', CURRENT_TIMESTAMP),
    (7, 10, 'DEMO-TAX-BIZ-007', 'Công ty Demo FarmTech Xanh', 'Thành phố Đà Lạt, Tỉnh Lâm Đồng', 'https://assets.example.test/business/demo-farmtech-license.pdf', 'Approved', 1, 'Phạm Đức Minh', CURRENT_TIMESTAMP - INTERVAL '118 days', CURRENT_TIMESTAMP),
    (8, 11, 'DEMO-TAX-BIZ-008', 'Công ty Demo Giáo Dục Số', 'Quận Bình Thạnh, Thành phố Hồ Chí Minh', 'https://assets.example.test/business/demo-giao-duc-so-license.pdf', 'Approved', 1, 'Bùi Thanh Thảo', CURRENT_TIMESTAMP - INTERVAL '108 days', CURRENT_TIMESTAMP),
    (9, 12, 'DEMO-TAX-BIZ-009', 'Công ty Demo Hải Đăng Health', 'Quận Ngô Quyền, Thành phố Hải Phòng', 'https://assets.example.test/business/demo-hai-dang-license.pdf', 'Approved', 1, 'Hồ Minh Khang', CURRENT_TIMESTAMP - INTERVAL '98 days', CURRENT_TIMESTAMP),
    (10, 13, 'DEMO-TAX-BIZ-010', 'Công ty Demo Innotech Việt', 'Quận Nam Từ Liêm, Thành phố Hà Nội', 'https://assets.example.test/business/demo-innotech-license.pdf', 'Approved', 1, 'Đỗ Hải Yến', CURRENT_TIMESTAMP - INTERVAL '88 days', CURRENT_TIMESTAMP);

INSERT INTO expert_profiles (expert_id, account_id, national_id, portfolio_url, years_of_experience, kyc_status, approved_by, created_at, updated_at) VALUES
    (1, 2,  'DEMO-EXPERT-ID-001', 'https://assets.example.test/experts/tran-hoang-nam/portfolio.pdf', 8, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '173 days', CURRENT_TIMESTAMP),
    (2, 14, 'DEMO-EXPERT-ID-002', 'https://assets.example.test/experts/nguyen-ngoc-anh/portfolio.pdf', 6, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '168 days', CURRENT_TIMESTAMP),
    (3, 15, 'DEMO-EXPERT-ID-003', 'https://assets.example.test/experts/le-quang-huy/portfolio.pdf', 9, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '158 days', CURRENT_TIMESTAMP),
    (4, 16, 'DEMO-EXPERT-ID-004', 'https://assets.example.test/experts/tran-thu-phuong/portfolio.pdf', 5, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '148 days', CURRENT_TIMESTAMP),
    (5, 17, 'DEMO-EXPERT-ID-005', 'https://assets.example.test/experts/pham-minh-khoi/portfolio.pdf', 7, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '138 days', CURRENT_TIMESTAMP),
    (6, 18, 'DEMO-EXPERT-ID-006', 'https://assets.example.test/experts/vu-hoai-an/portfolio.pdf', 10, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '128 days', CURRENT_TIMESTAMP),
    (7, 19, 'DEMO-EXPERT-ID-007', 'https://assets.example.test/experts/dang-tuan-kiet/portfolio.pdf', 6, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '118 days', CURRENT_TIMESTAMP),
    (8, 20, 'DEMO-EXPERT-ID-008', 'https://assets.example.test/experts/bui-lan-chi/portfolio.pdf', 8, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '108 days', CURRENT_TIMESTAMP),
    (9, 21, 'DEMO-EXPERT-ID-009', 'https://assets.example.test/experts/hoang-gia-bao/portfolio.pdf', 5, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '98 days', CURRENT_TIMESTAMP),
    (10, 22, 'DEMO-EXPERT-ID-010', 'https://assets.example.test/experts/do-khanh-linh/portfolio.pdf', 7, 'Approved', 1, CURRENT_TIMESTAMP - INTERVAL '88 days', CURRENT_TIMESTAMP);

INSERT INTO portfolios (portfolio_id, expert_id, domain_ids, skill_ids, technology_ids, years_experience, certificates, self_description, created_at, updated_at) VALUES
    (1, 1, '3,4,13,27', '1,10,12,14,15,19', '1,5,16,17,19,27', 8, 'Demo certificate: LLM Application Architecture; Demo certificate: Secure API Design', 'Kiến trúc sư AI chuyên xây dựng trợ lý RAG tiếng Việt, API tích hợp và hệ thống đánh giá có truy vết nguồn.', CURRENT_TIMESTAMP - INTERVAL '170 days', CURRENT_TIMESTAMP),
    (2, 2, '5,20,28', '1,9,11,17,21', '1,12,13,29,30', 6, 'Demo certificate: Computer Vision Engineering; Demo certificate: Test Automation', 'Kỹ sư thị giác máy tính có kinh nghiệm OCR, kiểm lỗi sản phẩm và tự động hóa benchmark mô hình.', CURRENT_TIMESTAMP - INTERVAL '165 days', CURRENT_TIMESTAMP),
    (3, 3, '8,9,10,11', '1,4,5,6,7,22', '1,5,20,21,22,23,24', 9, 'Demo certificate: Data Engineering; Demo certificate: Business Intelligence', 'Chuyên gia dữ liệu thiết kế pipeline, kho dữ liệu và dashboard quản trị với chỉ số được định nghĩa rõ ràng.', CURRENT_TIMESTAMP - INTERVAL '155 days', CURRENT_TIMESTAMP),
    (4, 4, '18,24,29', '1,7,8,17,21,27', '1,5,9,14,30', 5, 'Demo certificate: Fraud Analytics; Demo certificate: AI Quality Assurance', 'Kỹ sư học máy tập trung phát hiện gian lận, đánh giá rủi ro và kiểm thử hồi quy cho mô hình tài chính.', CURRENT_TIMESTAMP - INTERVAL '145 days', CURRENT_TIMESTAMP),
    (5, 5, '7,19,26', '1,8,16,17,22,28', '1,5,9,14,24,25', 7, 'Demo certificate: Recommendation Systems; Demo certificate: Product Analytics', 'Chuyên gia hệ thống gợi ý và phân tích khách hàng cho bán lẻ, thương mại điện tử và tiếp thị đa kênh.', CURRENT_TIMESTAMP - INTERVAL '135 days', CURRENT_TIMESTAMP),
    (6, 6, '12,13,14', '1,18,20,23,24,25,26', '7,8,9,10,11,20,26', 10, 'Demo certificate: Cloud Architecture; Demo certificate: MLOps Professional', 'Kiến trúc sư nền tảng AI phụ trách triển khai, quan sát, CI/CD và vận hành mô hình trên nhiều môi trường.', CURRENT_TIMESTAMP - INTERVAL '125 days', CURRENT_TIMESTAMP),
    (7, 7, '15,21,22', '1,6,8,16,18,23', '1,7,9,20,21,22', 6, 'Demo certificate: IoT Analytics; Demo certificate: Data Pipeline Operations', 'Kỹ sư dữ liệu và Edge AI xây dựng pipeline cảm biến, dự báo vận hành và tối ưu logistics.', CURRENT_TIMESTAMP - INTERVAL '115 days', CURRENT_TIMESTAMP),
    (8, 8, '4,6,23,27', '1,10,12,13,17,30', '1,15,16,17,18,27', 8, 'Demo certificate: NLP Engineering; Demo certificate: Conversational AI', 'Chuyên gia NLP phát triển trợ giảng, tổng hợp nội dung và trợ lý hội thoại có kiểm soát chất lượng.', CURRENT_TIMESTAMP - INTERVAL '105 days', CURRENT_TIMESTAMP),
    (9, 9, '5,17,28', '1,9,11,17,27', '1,10,12,13,29', 5, 'Demo certificate: Healthcare Imaging; Demo certificate: Data Privacy Foundations', 'Kỹ sư thị giác y tế tập trung tiền xử lý ảnh, đánh giá mô hình và bảo vệ dữ liệu nhạy cảm.', CURRENT_TIMESTAMP - INTERVAL '95 days', CURRENT_TIMESTAMP),
    (10, 10, '3,12,24,29', '1,12,14,17,18,21,27', '1,7,8,15,16,19,26', 7, 'Demo certificate: Responsible Generative AI; Demo certificate: Kubernetes Operations', 'Kỹ sư GenAI và MLOps xây dựng quy trình triển khai an toàn, giám sát chất lượng và kiểm thử bảo mật.', CURRENT_TIMESTAMP - INTERVAL '85 days', CURRENT_TIMESTAMP);

INSERT INTO staff_domains (staff_id, domain_id) VALUES
    (1,30),
    (2,1),(2,12),(2,13),(2,14),
    (3,8),(3,9),(3,10),(3,11),
    (4,5),(4,20),(4,28),
    (5,18),(5,24),(5,29),
    (6,7),(6,19),(6,26),(6,27),
    (7,15),(7,21),(7,22),
    (8,17),(8,28),(8,29),
    (9,4),(9,6),(9,23),(9,27),
    (10,3),(10,12),(10,24),(10,29);

INSERT INTO staff_skills (staff_id, skill_id) VALUES
    (1,28),(1,29),(1,30),
    (2,18),(2,20),(2,23),(2,25),(2,26),
    (3,4),(3,5),(3,6),(3,7),(3,22),
    (4,9),(4,11),(4,17),(4,21),
    (5,7),(5,8),(5,17),(5,27),
    (6,8),(6,16),(6,22),(6,28),
    (7,6),(7,16),(7,18),(7,23),
    (8,5),(8,7),(8,17),(8,27),
    (9,10),(9,12),(9,13),(9,30),
    (10,17),(10,18),(10,21),(10,27);

INSERT INTO jobs (
    job_id, business_id, title, raw_requirements, budget, status,
    planned_duration_value, planned_duration_unit, is_hot, hot_until,
    published_at, created_at, updated_at
) VALUES
    (1001, 1, 'Trợ lý RAG chăm sóc khách hàng đa kênh', 'Xây dựng trợ lý tiếng Việt trả lời từ kho tri thức đã duyệt, trích nguồn, chuyển tiếp hội thoại và đo tỷ lệ trả lời đúng.', 120000000, 'DRAFT', 8, 'WEEK', FALSE, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '80 days', CURRENT_TIMESTAMP),
    (1002, 1, 'Hệ thống gợi ý sản phẩm cho chuỗi bán lẻ', 'Xây dựng pipeline hành vi, mô hình gợi ý, API xếp hạng và dashboard theo dõi độ chính xác cùng tỷ lệ chuyển đổi.', 150000000, 'OPEN', 10, 'WEEK', TRUE, CURRENT_TIMESTAMP + INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '52 days', CURRENT_TIMESTAMP - INTERVAL '55 days', CURRENT_TIMESTAMP),
    (1003, 2, 'Nền tảng dự báo nhu cầu và tồn kho', 'Thiết kế kho dữ liệu và mô hình dự báo theo cửa hàng, sản phẩm, mùa vụ; cung cấp cảnh báo và dashboard vận hành.', 90000000, 'DRAFT', 8, 'WEEK', FALSE, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP),
    (1004, 3, 'Tối ưu tuyến giao nhận bằng dữ liệu thời gian thực', 'Tích hợp dữ liệu đơn hàng và phương tiện, dự báo thời gian đến, tối ưu tuyến và cung cấp API điều phối.', 180000000, 'OPEN', 10, 'WEEK', TRUE, CURRENT_TIMESTAMP + INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '39 days', CURRENT_TIMESTAMP - INTERVAL '42 days', CURRENT_TIMESTAMP),
    (1005, 4, 'Phát hiện giao dịch bất thường cho ví điện tử', 'Xây dựng đặc trưng, mô hình phát hiện bất thường, API chấm điểm và quy trình giải thích cảnh báo cho bộ phận rủi ro.', 110000000, 'OPEN', 8, 'WEEK', FALSE, NULL, CURRENT_TIMESTAMP - INTERVAL '36 days', CURRENT_TIMESTAMP - INTERVAL '38 days', CURRENT_TIMESTAMP),
    (1006, 5, 'Thị giác máy tính kiểm lỗi dây chuyền đóng gói', 'Thu thập ảnh mẫu, huấn luyện mô hình phát hiện lỗi, triển khai suy luận tại nhà máy và dashboard chất lượng.', 240000000, 'IN_PROGRESS', 10, 'WEEK', FALSE, NULL, CURRENT_TIMESTAMP - INTERVAL '70 days', CURRENT_TIMESTAMP - INTERVAL '74 days', CURRENT_TIMESTAMP),
    (1007, 6, 'Cá nhân hóa tìm kiếm cho sàn thương mại điện tử', 'Xây dựng tìm kiếm kết hợp từ khóa và vector, xếp hạng cá nhân hóa, benchmark ngoại tuyến và theo dõi chỉ số trực tuyến.', 135000000, 'IN_PROGRESS', 8, 'WEEK', FALSE, NULL, CURRENT_TIMESTAMP - INTERVAL '66 days', CURRENT_TIMESTAMP - INTERVAL '69 days', CURRENT_TIMESTAMP),
    (1008, 7, 'Giám sát sâu bệnh cây trồng từ ảnh thiết bị biên', 'Xây dựng bộ dữ liệu ảnh, mô hình phân loại sâu bệnh, API đồng bộ và gói suy luận tối ưu cho thiết bị tại nông trại.', 300000000, 'IN_PROGRESS', 10, 'WEEK', FALSE, NULL, CURRENT_TIMESTAMP - INTERVAL '62 days', CURRENT_TIMESTAMP - INTERVAL '65 days', CURRENT_TIMESTAMP),
    (1009, 8, 'Trợ giảng AI cho khóa học lập trình', 'Xây dựng trợ giảng RAG theo giáo trình, sinh gợi ý theo cấp độ, kiểm soát nội dung và dashboard đánh giá học tập.', 160000000, 'CLOSED', 8, 'WEEK', FALSE, NULL, CURRENT_TIMESTAMP - INTERVAL '150 days', CURRENT_TIMESTAMP - INTERVAL '155 days', CURRENT_TIMESTAMP - INTERVAL '35 days'),
    (1010, 8, 'Phân tích phản hồi học viên và cảnh báo bỏ học', 'Xây dựng pipeline phản hồi, mô hình phân loại chủ đề và rủi ro bỏ học, dashboard can thiệp và tài liệu vận hành.', 210000000, 'CLOSED', 10, 'WEEK', FALSE, NULL, CURRENT_TIMESTAMP - INTERVAL '210 days', CURRENT_TIMESTAMP - INTERVAL '215 days', CURRENT_TIMESTAMP - INTERVAL '90 days');

INSERT INTO sow (
    sow_id, job_id, title, overview, objectives, scope_of_work, deliverable,
    assumptions, out_of_scope, created_at, updated_at
) VALUES
    (1, 1001, 'SoW - Trợ lý RAG chăm sóc khách hàng đa kênh', 'Trợ lý hội thoại tiếng Việt sử dụng duy nhất tài liệu đã duyệt và chuyển tiếp an toàn cho nhân viên.', '["Tự động hóa câu hỏi lặp lại","Hiển thị nguồn cho từng câu trả lời","Đo chất lượng theo bộ câu hỏi chuẩn"]', '["Chuẩn hóa kho tri thức","Xây dựng pipeline RAG và API hội thoại","Tích hợp chuyển tiếp nhân viên","Thiết lập đánh giá và giám sát"]', '["Dịch vụ RAG có API","Bộ đánh giá tối thiểu 200 câu hỏi","Dashboard chất lượng","Tài liệu triển khai và vận hành"]', '["Doanh nghiệp cung cấp FAQ và chính sách đã duyệt","Môi trường thử nghiệm có quyền truy cập API"]', '["Không xây dựng tổng đài thoại","Không tự động thay đổi chính sách nguồn"]', CURRENT_TIMESTAMP - INTERVAL '80 days', CURRENT_TIMESTAMP),
    (2, 1002, 'SoW - Hệ thống gợi ý sản phẩm cho chuỗi bán lẻ', 'Hệ thống xếp hạng sản phẩm theo hành vi và tồn kho, có benchmark và khả năng giải thích chỉ số.', '["Tăng mức liên quan của danh sách gợi ý","Giảm sản phẩm hết hàng trong kết quả","Theo dõi tác động theo nhóm cửa hàng"]', '["Phân tích sự kiện hành vi","Xây dựng đặc trưng và baseline","Phát triển API xếp hạng","Thiết lập dashboard và kế hoạch A/B test"]', '["Pipeline đặc trưng","Mô hình gợi ý có báo cáo benchmark","API xếp hạng","Dashboard và hướng dẫn A/B test"]', '["Dữ liệu hành vi đã được ẩn danh","Doanh nghiệp cung cấp danh mục và tồn kho mẫu"]', '["Không vận hành A/B test trên production","Không thay đổi hệ thống POS"]', CURRENT_TIMESTAMP - INTERVAL '55 days', CURRENT_TIMESTAMP),
    (3, 1003, 'SoW - Nền tảng dự báo nhu cầu và tồn kho', 'Nền tảng dữ liệu và dự báo phục vụ kế hoạch nhập hàng theo cửa hàng và sản phẩm.', '["Hợp nhất dữ liệu bán hàng","Dự báo theo tuần","Cảnh báo rủi ro thiếu hoặc dư tồn kho"]', '["Thiết kế mô hình dữ liệu","Xây dựng pipeline ETL","Huấn luyện và backtest dự báo","Phát triển dashboard cảnh báo"]', '["Kho dữ liệu phân tích","Pipeline có kiểm tra chất lượng","Mô hình dự báo và báo cáo sai số","Dashboard vận hành"]', '["Có tối thiểu 18 tháng dữ liệu lịch sử","Mã sản phẩm và cửa hàng ổn định"]', '["Không tự động đặt hàng với nhà cung cấp","Không thay thế hệ thống ERP"]', CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP),
    (4, 1004, 'SoW - Tối ưu tuyến giao nhận bằng dữ liệu thời gian thực', 'Dịch vụ tối ưu tuyến giao nhận có xét tải trọng, thời gian cam kết và trạng thái phương tiện.', '["Giảm quãng đường di chuyển","Cải thiện độ chính xác ETA","Cung cấp phương án điều phối có giải thích"]', '["Chuẩn hóa sự kiện đơn hàng và GPS","Xây dựng mô hình ETA","Phát triển bộ tối ưu tuyến và API","Kiểm thử tải và kịch bản lỗi"]', '["Pipeline sự kiện","Mô hình ETA","API tối ưu tuyến","Báo cáo hiệu năng và runbook"]', '["Dữ liệu GPS mẫu có tần suất ổn định","Quy tắc tải trọng được doanh nghiệp xác nhận"]', '["Không điều khiển trực tiếp thiết bị xe","Không triển khai ứng dụng tài xế"]', CURRENT_TIMESTAMP - INTERVAL '42 days', CURRENT_TIMESTAMP),
    (5, 1005, 'SoW - Phát hiện giao dịch bất thường cho ví điện tử', 'Dịch vụ chấm điểm bất thường theo thời gian gần thực và hỗ trợ chuyên viên rủi ro điều tra.', '["Phát hiện mẫu giao dịch đáng ngờ","Giảm cảnh báo sai","Giải thích các yếu tố chính của điểm rủi ro"]', '["Phân tích dữ liệu và nhãn","Xây dựng đặc trưng và mô hình","Phát triển API chấm điểm","Thiết kế dashboard điều tra và audit"]', '["Bộ đặc trưng được phiên bản hóa","Mô hình và báo cáo precision/recall","API chấm điểm","Dashboard và tài liệu kiểm soát"]', '["Dữ liệu đã loại bỏ thông tin định danh trực tiếp","Chuyên viên cung cấp quy tắc nghiệp vụ hiện hành"]', '["Không tự động khóa tài khoản","Không thực hiện quyết định tín dụng"]', CURRENT_TIMESTAMP - INTERVAL '38 days', CURRENT_TIMESTAMP),
    (6, 1006, 'SoW - Thị giác máy tính kiểm lỗi dây chuyền đóng gói', 'Hệ thống phát hiện lỗi bao bì từ ảnh camera với luồng gắn nhãn, huấn luyện và suy luận tại nhà máy.', '["Phát hiện đúng các nhóm lỗi đã thống nhất","Đáp ứng độ trễ tại dây chuyền","Lưu bằng chứng cho lần dự đoán lỗi"]', '["Khảo sát ảnh và điều kiện sáng","Xây dựng bộ dữ liệu và mô hình","Tối ưu suy luận tại biên","Tích hợp dashboard và giám sát drift"]', '["Bộ dữ liệu gắn nhãn","Mô hình thị giác và benchmark","Gói triển khai tại biên","Dashboard chất lượng và runbook"]', '["Camera và vị trí lắp đặt ổn định","Doanh nghiệp duyệt định nghĩa từng nhóm lỗi"]', '["Không điều khiển cơ cấu loại sản phẩm","Không thay đổi phần cứng camera"]', CURRENT_TIMESTAMP - INTERVAL '74 days', CURRENT_TIMESTAMP),
    (7, 1007, 'SoW - Cá nhân hóa tìm kiếm cho sàn thương mại điện tử', 'Tìm kiếm kết hợp lexical và vector, sau đó xếp hạng lại theo ngữ cảnh người dùng và tồn kho.', '["Cải thiện NDCG trên bộ truy vấn chuẩn","Giữ thời gian phản hồi trong SLA","Theo dõi chất lượng theo nhóm truy vấn"]', '["Phân tích log tìm kiếm","Xây dựng chỉ mục kết hợp","Phát triển mô hình rerank","Thiết lập benchmark và quan sát production"]', '["Bộ truy vấn đánh giá","Dịch vụ tìm kiếm và rerank","Báo cáo tải và chất lượng","Dashboard cùng tài liệu vận hành"]', '["Có log truy vấn và click đã ẩn danh","Danh mục sản phẩm có mô tả chuẩn hóa"]', '["Không thiết kế lại giao diện cửa hàng","Không thực hiện chiến dịch quảng cáo"]', CURRENT_TIMESTAMP - INTERVAL '69 days', CURRENT_TIMESTAMP),
    (8, 1008, 'SoW - Giám sát sâu bệnh cây trồng từ ảnh thiết bị biên', 'Giải pháp nhận dạng nhóm sâu bệnh mục tiêu và đồng bộ kết quả từ thiết bị biên lên nền tảng quản lý.', '["Chuẩn hóa dữ liệu ảnh theo mùa vụ","Đạt recall mục tiêu cho nhóm bệnh ưu tiên","Vận hành ngoại tuyến khi mạng không ổn định"]', '["Thiết kế quy trình thu thập và gắn nhãn","Huấn luyện mô hình thị giác","Tối ưu gói suy luận biên","Xây dựng API đồng bộ và dashboard"]', '["Bộ dữ liệu phiên bản hóa","Mô hình và báo cáo đánh giá","Gói suy luận thiết bị biên","API, dashboard và hướng dẫn vận hành"]', '["Thiết bị thử nghiệm đáp ứng cấu hình tối thiểu","Chuyên gia nông nghiệp xác nhận nhãn"]', '["Không điều khiển phun thuốc tự động","Không chẩn đoán ngoài danh mục bệnh đã duyệt"]', CURRENT_TIMESTAMP - INTERVAL '65 days', CURRENT_TIMESTAMP),
    (9, 1009, 'SoW - Trợ giảng AI cho khóa học lập trình', 'Trợ giảng dựa trên giáo trình nội bộ, đưa gợi ý theo từng bước và lưu bằng chứng đánh giá an toàn.', '["Giảm thời gian chờ hỗ trợ","Không cung cấp toàn bộ đáp án ngay lập tức","Đo mức hữu ích theo bài học"]', '["Chuẩn hóa giáo trình và bài tập","Xây dựng RAG và chính sách gợi ý","Tích hợp lớp học","Thiết lập đánh giá và dashboard"]', '["Trợ giảng RAG","Bộ kiểm thử an toàn và học thuật","Tích hợp lớp học","Dashboard và tài liệu bàn giao"]', '["Giáo trình và lời giải mẫu được phê duyệt","Nền tảng học tập cung cấp API thử nghiệm"]', '["Không tự động chấm điểm cuối kỳ","Không sinh nội dung ngoài giáo trình"]', CURRENT_TIMESTAMP - INTERVAL '155 days', CURRENT_TIMESTAMP - INTERVAL '35 days'),
    (10, 1010, 'SoW - Phân tích phản hồi học viên và cảnh báo bỏ học', 'Pipeline phân tích phản hồi đa kênh, phân loại chủ đề và cảnh báo sớm cho đội hỗ trợ học viên.', '["Hợp nhất phản hồi theo học viên","Phân loại nguyên nhân chính","Ưu tiên trường hợp cần can thiệp"]', '["Thiết kế mô hình dữ liệu","Xây dựng pipeline NLP","Huấn luyện mô hình rủi ro","Phát triển dashboard và quy trình can thiệp"]', '["Pipeline dữ liệu","Mô hình chủ đề và rủi ro","Dashboard can thiệp","Báo cáo đánh giá và runbook"]', '["Dữ liệu đồng ý sử dụng cho mục đích hỗ trợ","Đội học vụ xác nhận quy trình can thiệp"]', '["Không tự động đình chỉ học viên","Không sử dụng dữ liệu cho quảng cáo"]', CURRENT_TIMESTAMP - INTERVAL '215 days', CURRENT_TIMESTAMP - INTERVAL '90 days');

INSERT INTO milestones (
    milestone_id, job_id, contract_id, milestone_name, description,
    funds_allocated, order_index, status, duration, duration_unit,
    created_at, updated_at
) VALUES
    (2001,1001,NULL,'Khảo sát yêu cầu và thiết kế RAG','Hoàn thiện kiến trúc, nguồn dữ liệu, luồng hội thoại và bộ chỉ số đánh giá.',30000000,1,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '79 days',CURRENT_TIMESTAMP),
    (2002,1001,NULL,'Phát triển trợ lý và tích hợp','Xây dựng ingestion, retrieval, API hội thoại và chuyển tiếp nhân viên.',60000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '79 days',CURRENT_TIMESTAMP),
    (2003,1001,NULL,'Đánh giá triển khai và bàn giao','Chạy bộ đánh giá, cấu hình giám sát và bàn giao tài liệu vận hành.',30000000,3,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '79 days',CURRENT_TIMESTAMP),
    (2004,1002,NULL,'Chuẩn hóa dữ liệu và baseline','Xây dựng dữ liệu hành vi, tiêu chí offline và mô hình baseline.',35000000,1,'PENDING',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '54 days',CURRENT_TIMESTAMP),
    (2005,1002,NULL,'Phát triển mô hình và API xếp hạng','Huấn luyện mô hình, tích hợp tồn kho và phát triển API phục vụ.',75000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '54 days',CURRENT_TIMESTAMP),
    (2006,1002,NULL,'Dashboard và kế hoạch thử nghiệm','Xây dựng dashboard, kiểm thử tải và thiết kế kế hoạch A/B test.',40000000,3,'PENDING',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '54 days',CURRENT_TIMESTAMP),
    (2007,1003,NULL,'Thiết kế kho dữ liệu','Mô hình hóa dữ liệu bán hàng, tồn kho, sản phẩm và cửa hàng.',20000000,1,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '44 days',CURRENT_TIMESTAMP),
    (2008,1003,NULL,'Pipeline và mô hình dự báo','Xây dựng ETL, kiểm tra chất lượng, huấn luyện và backtest dự báo.',45000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '44 days',CURRENT_TIMESTAMP),
    (2009,1003,NULL,'Dashboard cảnh báo và bàn giao','Cung cấp dashboard tồn kho, cảnh báo và tài liệu vận hành.',25000000,3,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '44 days',CURRENT_TIMESTAMP),
    (2010,1004,NULL,'Tích hợp dữ liệu và mô hình ETA','Chuẩn hóa đơn hàng, GPS và xây dựng mô hình dự báo thời gian đến.',40000000,1,'PENDING',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '41 days',CURRENT_TIMESTAMP),
    (2011,1004,NULL,'Bộ tối ưu tuyến và API điều phối','Phát triển thuật toán tối ưu có ràng buộc và API điều phối.',90000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '41 days',CURRENT_TIMESTAMP),
    (2012,1004,NULL,'Kiểm thử tải và runbook','Kiểm thử kịch bản thực tế, khả năng chịu lỗi và bàn giao runbook.',50000000,3,'PENDING',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '41 days',CURRENT_TIMESTAMP),
    (2013,1005,NULL,'Phân tích dữ liệu và thiết kế đặc trưng','Đánh giá nhãn, rò rỉ dữ liệu và xây dựng danh mục đặc trưng.',25000000,1,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '37 days',CURRENT_TIMESTAMP),
    (2014,1005,NULL,'Mô hình và API chấm điểm rủi ro','Huấn luyện mô hình, hiệu chỉnh ngưỡng và phát triển API chấm điểm.',55000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '37 days',CURRENT_TIMESTAMP),
    (2015,1005,NULL,'Dashboard điều tra và audit','Xây dựng dashboard giải thích, audit và tài liệu kiểm soát.',30000000,3,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '37 days',CURRENT_TIMESTAMP),
    (2016,1006,4002,'Khảo sát ảnh và xây dựng bộ dữ liệu','Chốt nhóm lỗi, tiêu chuẩn ảnh và hoàn thiện dữ liệu gắn nhãn.',60000000,1,'IN_PROGRESS',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '73 days',CURRENT_TIMESTAMP),
    (2017,1006,4002,'Huấn luyện mô hình và suy luận biên','Huấn luyện, benchmark và tối ưu mô hình cho thiết bị nhà máy.',120000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '73 days',CURRENT_TIMESTAMP),
    (2018,1006,4002,'Tích hợp dashboard và bàn giao','Tích hợp bằng chứng lỗi, dashboard chất lượng và runbook.',60000000,3,'PENDING',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '73 days',CURRENT_TIMESTAMP),
    (2019,1007,4003,'Bộ truy vấn và chỉ mục kết hợp','Hoàn thiện bộ đánh giá, chỉ mục từ khóa và chỉ mục vector.',30000000,1,'COMPLETED',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '68 days',CURRENT_TIMESTAMP-INTERVAL '34 days'),
    (2020,1007,4003,'Mô hình rerank và API tìm kiếm','Phát triển rerank cá nhân hóa và API đáp ứng SLA.',70000000,2,'IN_PROGRESS',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '68 days',CURRENT_TIMESTAMP),
    (2021,1007,4003,'Quan sát chất lượng và bàn giao','Xây dựng dashboard nhóm truy vấn, cảnh báo và tài liệu vận hành.',35000000,3,'PENDING',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '68 days',CURRENT_TIMESTAMP),
    (2022,1008,4004,'Thu thập và gắn nhãn dữ liệu ảnh','Chuẩn hóa quy trình ảnh theo mùa vụ và hoàn thiện bộ nhãn.',75000000,1,'UNDER_REVIEW',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '64 days',CURRENT_TIMESTAMP),
    (2023,1008,4004,'Mô hình và gói suy luận thiết bị biên','Huấn luyện mô hình và tối ưu hiệu năng cho thiết bị mục tiêu.',150000000,2,'PENDING',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '64 days',CURRENT_TIMESTAMP),
    (2024,1008,4004,'API đồng bộ dashboard và bàn giao','Phát triển API đồng bộ, dashboard và hướng dẫn vận hành.',75000000,3,'PENDING',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '64 days',CURRENT_TIMESTAMP),
    (2025,1009,4005,'Chuẩn hóa giáo trình và chính sách gợi ý','Chuẩn hóa nguồn, mức gợi ý và quy tắc an toàn học thuật.',40000000,1,'COMPLETED',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '154 days',CURRENT_TIMESTAMP-INTERVAL '120 days'),
    (2026,1009,4005,'Phát triển trợ giảng và tích hợp lớp học','Xây dựng RAG, chính sách hội thoại và tích hợp nền tảng học tập.',80000000,2,'COMPLETED',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '154 days',CURRENT_TIMESTAMP-INTERVAL '75 days'),
    (2027,1009,4005,'Đánh giá dashboard và bàn giao','Chạy kiểm thử an toàn, hoàn thiện dashboard và bàn giao.',40000000,3,'COMPLETED',2,'WEEK',CURRENT_TIMESTAMP-INTERVAL '154 days',CURRENT_TIMESTAMP-INTERVAL '35 days'),
    (2028,1010,4006,'Mô hình dữ liệu và pipeline phản hồi','Hợp nhất dữ liệu phản hồi và kiểm soát chất lượng đầu vào.',50000000,1,'COMPLETED',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '214 days',CURRENT_TIMESTAMP-INTERVAL '180 days'),
    (2029,1010,4006,'Mô hình chủ đề và rủi ro bỏ học','Huấn luyện, hiệu chỉnh và giải thích mô hình chủ đề cùng rủi ro.',105000000,2,'COMPLETED',4,'WEEK',CURRENT_TIMESTAMP-INTERVAL '214 days',CURRENT_TIMESTAMP-INTERVAL '135 days'),
    (2030,1010,4006,'Dashboard can thiệp và bàn giao','Hoàn thiện dashboard, quy trình can thiệp và runbook.',55000000,3,'COMPLETED',3,'WEEK',CURRENT_TIMESTAMP-INTERVAL '214 days',CURRENT_TIMESTAMP-INTERVAL '90 days');

INSERT INTO acceptance_criteria (criteria_id, milestone_id, description, sort_order, created_at, updated_at)
SELECT
    ((m.milestone_id - 2001) * 3) + criterion.order_index,
    m.milestone_id,
    CASE criterion.order_index
        WHEN 1 THEN 'Hoàn thành đúng phạm vi đã mô tả cho milestone: ' || m.milestone_name || '.'
        WHEN 2 THEN 'Có bằng chứng kiểm thử và chỉ số đo lường phù hợp cho: ' || m.milestone_name || '.'
        ELSE 'Tài liệu nghiệm thu, vận hành và bàn giao của ' || m.milestone_name || ' được cập nhật đầy đủ.'
    END,
    criterion.order_index,
    m.created_at,
    m.updated_at
FROM milestones m
CROSS JOIN (VALUES (1), (2), (3)) AS criterion(order_index)
WHERE m.milestone_id BETWEEN 2001 AND 2030;

WITH mappings(job_id, domain_ids, skill_ids, technology_ids) AS (
    VALUES
        (1001, ARRAY[3,4,27],    ARRAY[1,10,12,14,15,19], ARRAY[1,5,16,17,19,27]),
        (1002, ARRAY[7,19,26],   ARRAY[1,4,8,16,17,22],   ARRAY[1,5,9,14,24,25]),
        (1003, ARRAY[8,9,10],    ARRAY[1,4,5,6,7,22],     ARRAY[1,5,21,23,24]),
        (1004, ARRAY[9,11,21],   ARRAY[1,6,8,16,19,23],   ARRAY[1,5,20,22,27]),
        (1005, ARRAY[18,24,29],  ARRAY[1,7,8,17,19,27],   ARRAY[1,5,9,14,30]),
        (1006, ARRAY[5,20,29],   ARRAY[1,9,11,17,18,21],  ARRAY[1,7,12,13,26,29]),
        (1007, ARRAY[7,13,19],   ARRAY[1,8,15,16,17,19],  ARRAY[1,5,9,14,19,27]),
        (1008, ARRAY[5,15,22],   ARRAY[1,9,11,16,18,23],  ARRAY[1,7,11,12,29]),
        (1009, ARRAY[3,4,23],    ARRAY[1,10,12,13,14,17], ARRAY[1,5,15,16,17,27]),
        (1010, ARRAY[4,8,23],    ARRAY[1,4,6,7,10,22],    ARRAY[1,5,21,23,24])
)
INSERT INTO job_domains (job_id, domain_id, created_at)
SELECT job_id, domain_id, CURRENT_TIMESTAMP
FROM mappings CROSS JOIN LATERAL unnest(domain_ids) AS domain_id;

WITH mappings(job_id, skill_ids) AS (
    VALUES
        (1001, ARRAY[1,10,12,14,15,19]), (1002, ARRAY[1,4,8,16,17,22]),
        (1003, ARRAY[1,4,5,6,7,22]), (1004, ARRAY[1,6,8,16,19,23]),
        (1005, ARRAY[1,7,8,17,19,27]), (1006, ARRAY[1,9,11,17,18,21]),
        (1007, ARRAY[1,8,15,16,17,19]), (1008, ARRAY[1,9,11,16,18,23]),
        (1009, ARRAY[1,10,12,13,14,17]), (1010, ARRAY[1,4,6,7,10,22])
)
INSERT INTO job_skills (job_id, skill_id, is_mandatory, created_at)
SELECT job_id, skill_id, skill_id = skill_ids[1], CURRENT_TIMESTAMP
FROM mappings CROSS JOIN LATERAL unnest(skill_ids) AS skill_id;

WITH mappings(job_id, technology_ids) AS (
    VALUES
        (1001, ARRAY[1,5,16,17,19,27]), (1002, ARRAY[1,5,9,14,24,25]),
        (1003, ARRAY[1,5,21,23,24]), (1004, ARRAY[1,5,20,22,27]),
        (1005, ARRAY[1,5,9,14,30]), (1006, ARRAY[1,7,12,13,26,29]),
        (1007, ARRAY[1,5,9,14,19,27]), (1008, ARRAY[1,7,11,12,29]),
        (1009, ARRAY[1,5,15,16,17,27]), (1010, ARRAY[1,5,21,23,24])
)
INSERT INTO job_technologies (job_id, technology_id, created_at)
SELECT job_id, technology_id, CURRENT_TIMESTAMP
FROM mappings CROSS JOIN LATERAL unnest(technology_ids) AS technology_id;

-- Keep the legacy jobs.structured_sow field valid and aligned with the
-- normalized sow/milestone records used by the current API.
UPDATE jobs j
SET structured_sow = jsonb_build_object(
        'sow', jsonb_build_object(
            'title', s.title,
            'overview', s.overview,
            'objectives', s.objectives::jsonb,
            'scopeOfWork', s.scope_of_work::jsonb,
            'deliverables', s.deliverable::jsonb,
            'assumptions', s.assumptions::jsonb,
            'outOfScope', s.out_of_scope::jsonb
        ),
        'milestones', (
            SELECT jsonb_agg(jsonb_build_object(
                'milestoneId', m.milestone_id,
                'name', m.milestone_name,
                'description', m.description,
                'budget', m.funds_allocated,
                'duration', m.duration,
                'durationUnit', m.duration_unit
            ) ORDER BY m.order_index)
            FROM milestones m WHERE m.job_id = j.job_id
        )
    )::text
FROM sow s
WHERE s.job_id = j.job_id;

INSERT INTO proposals (
    proposal_id, job_id, expert_id, technical_solution, bid_amount, status,
    proposal_description, proposal_file_url, proposal_milestone,
    business_selected, created_at, updated_at
) VALUES
    (3001,1002,1,'Xây dựng baseline theo implicit feedback, tách candidate generation và rerank, sau đó đánh giá NDCG cùng coverage theo cửa hàng.',145000000,'Pending','Đề xuất dựa trên kinh nghiệm xây dựng API AI và hệ thống đánh giá có giám sát.', 'https://assets.example.test/proposals/3001.pdf','[{"milestoneId":2004,"proposedBudget":34000000},{"milestoneId":2005,"proposedBudget":72000000},{"milestoneId":2006,"proposedBudget":39000000}]',FALSE,CURRENT_TIMESTAMP-INTERVAL '48 days',CURRENT_TIMESTAMP),
    (3002,1002,2,'Sử dụng embedding hình ảnh sản phẩm làm tín hiệu chính và bổ sung kiểm thử chất lượng dữ liệu.',148000000,'Rejected','Giải pháp mạnh về hình ảnh nhưng chưa đáp ứng đầy đủ tín hiệu hành vi và tồn kho bắt buộc.', 'https://assets.example.test/proposals/3002.pdf','[{"milestoneId":2004,"proposedBudget":35000000},{"milestoneId":2005,"proposedBudget":74000000},{"milestoneId":2006,"proposedBudget":39000000}]',FALSE,CURRENT_TIMESTAMP-INTERVAL '47 days',CURRENT_TIMESTAMP-INTERVAL '44 days'),
    (3003,1004,3,'Thiết kế pipeline Kafka, mô hình ETA theo tuyến và bộ tối ưu có ràng buộc tải trọng cùng khung giờ giao.',172000000,'Pending','Đề xuất bao gồm benchmark ETA, kiểm thử tải API và runbook xử lý dữ liệu GPS trễ.', 'https://assets.example.test/proposals/3003.pdf','[{"milestoneId":2010,"proposedBudget":38000000},{"milestoneId":2011,"proposedBudget":86000000},{"milestoneId":2012,"proposedBudget":48000000}]',FALSE,CURRENT_TIMESTAMP-INTERVAL '35 days',CURRENT_TIMESTAMP),
    (3004,1004,7,'Xây dựng pipeline GPS theo thời gian thực, mô hình ETA và thuật toán tối ưu tuyến có khả năng chạy lại khi có sự cố.',175000000,'Pending','Kinh nghiệm dữ liệu cảm biến và logistics phù hợp với yêu cầu điều phối thời gian thực.', 'https://assets.example.test/proposals/3004.pdf','[{"milestoneId":2010,"proposedBudget":39000000},{"milestoneId":2011,"proposedBudget":88000000},{"milestoneId":2012,"proposedBudget":48000000}]',FALSE,CURRENT_TIMESTAMP-INTERVAL '34 days',CURRENT_TIMESTAMP),
    (3005,1005,4,'Xây dựng đặc trưng giao dịch theo cửa sổ thời gian, mô hình bất thường kết hợp rule và dashboard giải thích cho chuyên viên.',105000000,'Accepted','Đề xuất đáp ứng tiêu chí precision/recall, giải thích cảnh báo, audit và kiểm soát dữ liệu nhạy cảm.', 'https://assets.example.test/proposals/3005.pdf','[{"milestoneId":2013,"proposedBudget":24000000},{"milestoneId":2014,"proposedBudget":52000000},{"milestoneId":2015,"proposedBudget":29000000}]',TRUE,CURRENT_TIMESTAMP-INTERVAL '32 days',CURRENT_TIMESTAMP-INTERVAL '29 days'),
    (3006,1006,2,'Thiết kế quy trình dữ liệu ảnh, mô hình phát hiện lỗi và gói suy luận biên có benchmark độ trễ tại dây chuyền.',230000000,'Accepted','Đề xuất liên kết trực tiếp với kinh nghiệm thị giác, benchmark và tự động hóa kiểm thử.', 'https://assets.example.test/proposals/3006.pdf','[{"milestoneId":2016,"proposedBudget":55000000},{"milestoneId":2017,"proposedBudget":115000000},{"milestoneId":2018,"proposedBudget":60000000}]',TRUE,CURRENT_TIMESTAMP-INTERVAL '68 days',CURRENT_TIMESTAMP-INTERVAL '64 days'),
    (3007,1007,5,'Kết hợp BM25, vector retrieval và rerank cá nhân hóa; đo NDCG, latency và coverage theo nhóm truy vấn.',130000000,'Accepted','Đề xuất phù hợp với hồ sơ hệ thống gợi ý và phân tích hành vi thương mại điện tử.', 'https://assets.example.test/proposals/3007.pdf','[{"milestoneId":2019,"proposedBudget":30000000},{"milestoneId":2020,"proposedBudget":65000000},{"milestoneId":2021,"proposedBudget":35000000}]',TRUE,CURRENT_TIMESTAMP-INTERVAL '64 days',CURRENT_TIMESTAMP-INTERVAL '60 days'),
    (3008,1008,7,'Xây dựng dữ liệu theo mùa vụ, mô hình thị giác nhẹ và cơ chế đồng bộ kết quả khi kết nối không ổn định.',285000000,'Accepted','Kinh nghiệm Edge AI, pipeline cảm biến và vận hành thiết bị phù hợp với bối cảnh nông trại.', 'https://assets.example.test/proposals/3008.pdf','[{"milestoneId":2022,"proposedBudget":70000000},{"milestoneId":2023,"proposedBudget":145000000},{"milestoneId":2024,"proposedBudget":70000000}]',TRUE,CURRENT_TIMESTAMP-INTERVAL '60 days',CURRENT_TIMESTAMP-INTERVAL '56 days'),
    (3009,1009,8,'Xây dựng trợ giảng RAG theo giáo trình, chính sách gợi ý từng bước và bộ kiểm thử an toàn học thuật.',150000000,'Accepted','Đề xuất tận dụng kinh nghiệm NLP và conversational AI cho sản phẩm giáo dục.', 'https://assets.example.test/proposals/3009.pdf','[{"milestoneId":2025,"proposedBudget":35000000},{"milestoneId":2026,"proposedBudget":75000000},{"milestoneId":2027,"proposedBudget":40000000}]',TRUE,CURRENT_TIMESTAMP-INTERVAL '145 days',CURRENT_TIMESTAMP-INTERVAL '140 days'),
    (3010,1010,3,'Thiết kế kho phản hồi, pipeline NLP, mô hình chủ đề và rủi ro cùng dashboard cho đội học vụ.',200000000,'Accepted','Đề xuất có thế mạnh dữ liệu, BI và quy trình kiểm soát chất lượng đầu vào.', 'https://assets.example.test/proposals/3010.pdf','[{"milestoneId":2028,"proposedBudget":45000000},{"milestoneId":2029,"proposedBudget":100000000},{"milestoneId":2030,"proposedBudget":55000000}]',TRUE,CURRENT_TIMESTAMP-INTERVAL '205 days',CURRENT_TIMESTAMP-INTERVAL '200 days'),
    (3011,1004,1,'Xây dựng trợ lý điều phối dựa trên LLM để đề xuất tuyến từ mô tả tự do.',165000000,'Rejected','Giải pháp chưa đáp ứng ràng buộc tối ưu định lượng và SLA thời gian thực của dự án.', 'https://assets.example.test/proposals/3011.pdf','[{"milestoneId":2010,"proposedBudget":35000000},{"milestoneId":2011,"proposedBudget":83000000},{"milestoneId":2012,"proposedBudget":47000000}]',FALSE,CURRENT_TIMESTAMP-INTERVAL '33 days',CURRENT_TIMESTAMP-INTERVAL '30 days'),
    (3012,1005,2,'Áp dụng OCR chứng từ làm tín hiệu chính cho mô hình rủi ro giao dịch.',103000000,'Rejected','Phạm vi OCR không phù hợp với dữ liệu giao dịch cấu trúc và tiêu chí phát hiện bất thường.', 'https://assets.example.test/proposals/3012.pdf','[{"milestoneId":2013,"proposedBudget":23000000},{"milestoneId":2014,"proposedBudget":51000000},{"milestoneId":2015,"proposedBudget":29000000}]',FALSE,CURRENT_TIMESTAMP-INTERVAL '31 days',CURRENT_TIMESTAMP-INTERVAL '28 days');

INSERT INTO contracts (
    contract_id, job_id, business_id, expert_id, total_budget, timeline_days,
    status, proposal_id, contract_title, business_accepted_at,
    expert_accepted_at, business_nda_signed_at, expert_nda_signed_at,
    activated_at, contract_scope, created_at, updated_at
) VALUES
    (4001,1005,4,4,105000000,56,'DRAFT',3005,'Hợp đồng phát hiện giao dịch bất thường cho ví điện tử',CURRENT_TIMESTAMP-INTERVAL '27 days',NULL,NULL,NULL,NULL,'Xây dựng đặc trưng, mô hình, API chấm điểm và dashboard điều tra theo SoW của job 1005.',CURRENT_TIMESTAMP-INTERVAL '28 days',CURRENT_TIMESTAMP),
    (4002,1006,5,2,230000000,70,'ACTIVE',3006,'Hợp đồng thị giác máy tính kiểm lỗi dây chuyền đóng gói',CURRENT_TIMESTAMP-INTERVAL '62 days',CURRENT_TIMESTAMP-INTERVAL '62 days',CURRENT_TIMESTAMP-INTERVAL '61 days',CURRENT_TIMESTAMP-INTERVAL '61 days',CURRENT_TIMESTAMP-INTERVAL '59 days','Xây dựng dữ liệu ảnh, mô hình, gói suy luận biên và dashboard chất lượng theo SoW của job 1006.',CURRENT_TIMESTAMP-INTERVAL '63 days',CURRENT_TIMESTAMP),
    (4003,1007,6,5,130000000,56,'ACTIVE',3007,'Hợp đồng cá nhân hóa tìm kiếm cho sàn thương mại điện tử',CURRENT_TIMESTAMP-INTERVAL '58 days',CURRENT_TIMESTAMP-INTERVAL '58 days',CURRENT_TIMESTAMP-INTERVAL '57 days',CURRENT_TIMESTAMP-INTERVAL '57 days',CURRENT_TIMESTAMP-INTERVAL '55 days','Xây dựng chỉ mục kết hợp, mô hình rerank, API tìm kiếm và hệ thống quan sát theo SoW của job 1007.',CURRENT_TIMESTAMP-INTERVAL '59 days',CURRENT_TIMESTAMP),
    (4004,1008,7,7,285000000,70,'ACTIVE',3008,'Hợp đồng giám sát sâu bệnh cây trồng từ thiết bị biên',CURRENT_TIMESTAMP-INTERVAL '54 days',CURRENT_TIMESTAMP-INTERVAL '54 days',CURRENT_TIMESTAMP-INTERVAL '53 days',CURRENT_TIMESTAMP-INTERVAL '53 days',CURRENT_TIMESTAMP-INTERVAL '51 days','Xây dựng dữ liệu, mô hình thị giác, gói suy luận biên và API đồng bộ theo SoW của job 1008.',CURRENT_TIMESTAMP-INTERVAL '55 days',CURRENT_TIMESTAMP),
    (4005,1009,8,8,150000000,56,'COMPLETED',3009,'Hợp đồng trợ giảng AI cho khóa học lập trình',CURRENT_TIMESTAMP-INTERVAL '138 days',CURRENT_TIMESTAMP-INTERVAL '138 days',CURRENT_TIMESTAMP-INTERVAL '137 days',CURRENT_TIMESTAMP-INTERVAL '137 days',CURRENT_TIMESTAMP-INTERVAL '135 days','Xây dựng trợ giảng RAG, kiểm soát học thuật, tích hợp lớp học và dashboard theo SoW của job 1009.',CURRENT_TIMESTAMP-INTERVAL '139 days',CURRENT_TIMESTAMP-INTERVAL '35 days'),
    (4006,1010,8,3,200000000,70,'CLOSED',3010,'Hợp đồng phân tích phản hồi và cảnh báo bỏ học',CURRENT_TIMESTAMP-INTERVAL '198 days',CURRENT_TIMESTAMP-INTERVAL '198 days',CURRENT_TIMESTAMP-INTERVAL '197 days',CURRENT_TIMESTAMP-INTERVAL '197 days',CURRENT_TIMESTAMP-INTERVAL '195 days','Xây dựng pipeline phản hồi, mô hình chủ đề/rủi ro, dashboard và quy trình can thiệp theo SoW của job 1010.',CURRENT_TIMESTAMP-INTERVAL '199 days',CURRENT_TIMESTAMP-INTERVAL '88 days');

INSERT INTO contract_milestones (
    contract_milestone_id, contract_id, job_milestone_id, milestone_name,
    description, original_budget, final_budget, order_index, status,
    duration, duration_unit, criteria_snapshot, deliverable_expectation,
    in_progress_started_at, escrow_released_at, created_at, updated_at
)
SELECT
    ((c.contract_id - 4001) * 3) + m.order_index,
    c.contract_id,
    m.milestone_id,
    m.milestone_name,
    m.description,
    m.funds_allocated,
    (proposal_item.item ->> 'proposedBudget')::numeric,
    m.order_index,
    m.status,
    m.duration,
    m.duration_unit,
    (SELECT string_agg(ac.description, E'\n' ORDER BY ac.sort_order) FROM acceptance_criteria ac WHERE ac.milestone_id = m.milestone_id),
    m.description,
    CASE WHEN m.status IN ('IN_PROGRESS','UNDER_REVIEW','COMPLETED') THEN c.activated_at + ((m.order_index - 1) * 21 || ' days')::interval END,
    CASE WHEN m.status = 'COMPLETED' THEN m.updated_at END,
    c.created_at,
    m.updated_at
FROM contracts c
JOIN proposals p ON p.proposal_id = c.proposal_id
JOIN milestones m ON m.job_id = c.job_id
CROSS JOIN LATERAL jsonb_array_elements(p.proposal_milestone::jsonb) AS proposal_item(item)
WHERE (proposal_item.item ->> 'milestoneId')::integer = m.milestone_id;

INSERT INTO expert_recommendations (
    id, job_posting_id, expert_id, portfolio_id, rank_position, match_score,
    ai_reason, matched_skills, matched_domains, business_selected, created_at
) VALUES
    (1,1002,5,5,1,94.50,'Phù hợp trực tiếp với hệ thống gợi ý, dữ liệu hành vi và phân tích bán lẻ.','8,16,17,22','7,19,26',FALSE,CURRENT_TIMESTAMP-INTERVAL '49 days'),
    (2,1002,3,3,2,86.00,'Có nền tảng dữ liệu và BI mạnh cho pipeline đặc trưng và dashboard.','4,5,6,22','8,9,10',FALSE,CURRENT_TIMESTAMP-INTERVAL '49 days'),
    (3,1002,1,1,3,81.50,'Có kinh nghiệm API AI và đánh giá nhưng ít dự án gợi ý hơn hai ứng viên đầu.','1,17,19','3,13',FALSE,CURRENT_TIMESTAMP-INTERVAL '49 days'),
    (4,1004,7,7,1,95.00,'Phù hợp với dữ liệu cảm biến, pipeline thời gian thực và tối ưu logistics.','6,16,18,23','15,21',FALSE,CURRENT_TIMESTAMP-INTERVAL '36 days'),
    (5,1004,3,3,2,88.50,'Mạnh về data engineering, streaming và kiểm soát chất lượng dữ liệu.','4,5,6,22','8,9,11',FALSE,CURRENT_TIMESTAMP-INTERVAL '36 days'),
    (6,1004,6,6,3,79.00,'Có năng lực nền tảng và triển khai nhưng ít kinh nghiệm tối ưu tuyến chuyên sâu.','18,20,23,26','12,13,14',FALSE,CURRENT_TIMESTAMP-INTERVAL '36 days');

INSERT INTO user_quotas (
    quota_id, account_id, job_post_quota_balance, proposal_quota_balance,
    badge_expired_at, premium_expired_at, created_at, updated_at
)
SELECT
    a.account_id,
    a.account_id,
    CASE a.account_id
        WHEN 1 THEN 32
        WHEN 5 THEN 3 WHEN 6 THEN 2 WHEN 7 THEN 2 WHEN 8 THEN 2
        WHEN 9 THEN 2 WHEN 10 THEN 2 WHEN 11 THEN 1 WHEN 12 THEN 3 WHEN 13 THEN 3
        ELSE 0
    END,
    CASE a.account_id
        WHEN 2 THEN 1 WHEN 14 THEN 0 WHEN 15 THEN 1 WHEN 16 THEN 2
        WHEN 17 THEN 2 WHEN 18 THEN 3 WHEN 19 THEN 1 WHEN 20 THEN 2
        WHEN 21 THEN 3 WHEN 22 THEN 3
        ELSE 0
    END,
    CASE WHEN a.account_id = 1 THEN CURRENT_TIMESTAMP + INTERVAL '90 days' END,
    CASE WHEN a.account_id = 1 THEN CURRENT_TIMESTAMP + INTERVAL '90 days' END,
    CURRENT_TIMESTAMP - INTERVAL '80 days',
    CURRENT_TIMESTAMP
FROM account a;

-- Initial and consumption quota history uses the same balances as user_quotas.
INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT account_id, 'JOB_POST', 'GRANT', 3, 0, 3, 'INITIAL_BUSINESS_GRANT', account_id, CURRENT_TIMESTAMP - INTERVAL '79 days'
FROM account WHERE role_id = 1;

INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
) VALUES
    (1, 'JOB_POST', 'PURCHASE', 30, 3, 33, 'MEMBERSHIP_PURCHASE', 1, CURRENT_TIMESTAMP - INTERVAL '75 days');

WITH published_jobs AS (
    SELECT
        bp.account_id,
        j.job_id,
        j.published_at,
        row_number() OVER (PARTITION BY bp.account_id ORDER BY j.published_at, j.job_id) AS consume_number,
        CASE WHEN bp.account_id = 1 THEN 33 ELSE 3 END AS starting_balance
    FROM jobs j
    JOIN business_profiles bp ON bp.business_id = j.business_id
    WHERE j.published_at IS NOT NULL
)
INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT
    account_id, 'JOB_POST', 'CONSUME', 1,
    starting_balance - consume_number + 1,
    starting_balance - consume_number,
    'JOB', job_id, published_at
FROM published_jobs;

INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT account_id, 'PROPOSAL', 'GRANT', 3, 0, 3, 'INITIAL_EXPERT_GRANT', account_id, CURRENT_TIMESTAMP - INTERVAL '79 days'
FROM account WHERE role_id = 2;

WITH submitted_proposals AS (
    SELECT
        ep.account_id,
        p.proposal_id,
        p.created_at,
        row_number() OVER (PARTITION BY ep.account_id ORDER BY p.created_at, p.proposal_id) AS consume_number
    FROM proposals p
    JOIN expert_profiles ep ON ep.expert_id = p.expert_id
)
INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT account_id, 'PROPOSAL', 'CONSUME', 1, 4 - consume_number, 3 - consume_number,
       'PROPOSAL', proposal_id, created_at
FROM submitted_proposals;

INSERT INTO system_wallet (
    system_wallet_id, account_id, role_id, wallet_type, deposited_business_count,
    successful_deposit_count, total_revenue, holding_balance, disputed_balance,
    currency, last_synced_at, current_balance, available_balance, escrow_balance,
    created_at, updated_at
)
SELECT
    CASE a.account_id WHEN 3 THEN 1 WHEN 1 THEN 2 WHEN 2 THEN 3 ELSE a.account_id END,
    a.account_id,
    a.role_id,
    CASE r.role_name WHEN 'ADMIN' THEN 'ADMIN_SYSTEM' ELSE r.role_name END,
    0, 0, 0, 0, 0, 'VND', CURRENT_TIMESTAMP, 0, 0, 0,
    a.created_at, CURRENT_TIMESTAMP
FROM account a
JOIN roles r ON r.role_id = a.role_id;

INSERT INTO payment_order (
    id, account_id, amount, provider, purpose, provider_txn_ref,
    provider_transaction_no, provider_response_code, provider_order_code,
    provider_payment_link_id, checkout_url, cancel_url, return_url,
    status, description, created_at, updated_at, paid_at
)
SELECT
    5000 + row_number() OVER (ORDER BY a.account_id),
    a.account_id,
    CASE WHEN r.role_name = 'BUSINESS' THEN 400000000 ELSE 80000000 END,
    'PAYOS', 'WALLET_TOPUP',
    'DEMO-TOPUP-' || lpad(a.account_id::text, 2, '0'),
    'DEMO-PAYOS-TXN-' || lpad(a.account_id::text, 2, '0'),
    '00',
    7600000000 + a.account_id,
    'demo-payment-link-' || a.account_id,
    'https://pay.example.test/checkout/' || a.account_id,
    'https://app.example.test/wallet/topup/cancel',
    'https://app.example.test/wallet/topup/success',
    'PAID',
    CASE r.role_name
        WHEN 'BUSINESS' THEN 'Nạp 400.000.000 VND vào ví Business demo'
        ELSE 'Nạp 80.000.000 VND vào ví Expert demo'
    END,
    CURRENT_TIMESTAMP - INTERVAL '240 days' + (a.account_id || ' minutes')::interval,
    CURRENT_TIMESTAMP - INTERVAL '240 days' + (a.account_id || ' minutes')::interval,
    CURRENT_TIMESTAMP - INTERVAL '240 days' + (a.account_id || ' minutes')::interval
FROM account a
JOIN roles r ON r.role_id = a.role_id
WHERE r.role_name IN ('BUSINESS', 'EXPERT');

CREATE TEMP TABLE seed_wallet_events (
    event_order INTEGER PRIMARY KEY,
    account_id INTEGER NOT NULL,
    payment_order_id BIGINT,
    transaction_type VARCHAR(80) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    balance_type VARCHAR(20) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    reference_type VARCHAR(80),
    reference_id BIGINT,
    description TEXT,
    contract_id INTEGER,
    milestone_id INTEGER,
    metadata JSONB,
    operation_key VARCHAR(150),
    operation_leg VARCHAR(80),
    created_at TIMESTAMP NOT NULL
);

-- One transparent top-up event for every Business and Expert wallet.
INSERT INTO seed_wallet_events
SELECT
    1000 + a.account_id,
    a.account_id,
    po.id,
    'TOPUP', 'CREDIT', 'AVAILABLE', po.amount,
    'PAYMENT_ORDER', po.id,
    po.description,
    NULL, NULL,
    jsonb_build_object('provider','PAYOS','providerOrderCode',po.provider_order_code),
    'TOPUP:' || po.id,
    'AVAILABLE_CREDIT',
    po.paid_at
FROM account a
JOIN roles r ON r.role_id = a.role_id
JOIN payment_order po ON po.account_id = a.account_id
WHERE r.role_name IN ('BUSINESS', 'EXPERT');

-- Membership purchases: purchaser debit and matching platform revenue credit.
INSERT INTO seed_wallet_events VALUES
    (2001,1,NULL,'MEMBERSHIP_PURCHASE','DEBIT','AVAILABLE',1000,'MEMBERSHIP_PURCHASE',1,'Mua gói Business Premium demo',NULL,NULL,'{"packageCode":"BUSINESS_PREMIUM"}'::jsonb,'MEMBERSHIP_PURCHASE:1','PURCHASER_AVAILABLE_DEBIT',CURRENT_TIMESTAMP-INTERVAL '75 days'),
    (2002,3,NULL,'MEMBERSHIP_PURCHASE','CREDIT','AVAILABLE',1000,'MEMBERSHIP_PURCHASE',1,'Doanh thu gói Business Premium demo',NULL,NULL,'{"purchaserAccountId":1,"packageCode":"BUSINESS_PREMIUM"}'::jsonb,'MEMBERSHIP_PURCHASE:1','PLATFORM_REVENUE_CREDIT',CURRENT_TIMESTAMP-INTERVAL '75 days'),
    (2003,2,NULL,'MEMBERSHIP_PURCHASE','DEBIT','AVAILABLE',100,'MEMBERSHIP_PURCHASE',2,'Mua gói Expert Standard demo',NULL,NULL,'{"packageCode":"EXPERT_STANDARD"}'::jsonb,'MEMBERSHIP_PURCHASE:2','PURCHASER_AVAILABLE_DEBIT',CURRENT_TIMESTAMP-INTERVAL '74 days'),
    (2004,3,NULL,'MEMBERSHIP_PURCHASE','CREDIT','AVAILABLE',100,'MEMBERSHIP_PURCHASE',2,'Doanh thu gói Expert Standard demo',NULL,NULL,'{"purchaserAccountId":2,"packageCode":"EXPERT_STANDARD"}'::jsonb,'MEMBERSHIP_PURCHASE:2','PLATFORM_REVENUE_CREDIT',CURRENT_TIMESTAMP-INTERVAL '74 days'),
    (2005,6,NULL,'MEMBERSHIP_PURCHASE','DEBIT','AVAILABLE',200,'MEMBERSHIP_PURCHASE',3,'Mua gói Business Standard demo',NULL,NULL,'{"packageCode":"BUSINESS_STANDARD"}'::jsonb,'MEMBERSHIP_PURCHASE:3','PURCHASER_AVAILABLE_DEBIT',CURRENT_TIMESTAMP-INTERVAL '40 days'),
    (2006,3,NULL,'MEMBERSHIP_PURCHASE','CREDIT','AVAILABLE',200,'MEMBERSHIP_PURCHASE',3,'Doanh thu gói Business Standard demo',NULL,NULL,'{"purchaserAccountId":6,"packageCode":"BUSINESS_STANDARD"}'::jsonb,'MEMBERSHIP_PURCHASE:3','PLATFORM_REVENUE_CREDIT',CURRENT_TIMESTAMP-INTERVAL '40 days');

-- Participant contract deposits. Every hold is represented by the same two
-- ledger legs produced by WalletLedgerService.holdEscrowFromAvailable().
WITH deposits(event_base, contract_id, account_id, owner_role, amount, happened_at) AS (
    VALUES
        (3000,4002,8, 'BUSINESS',46000000::numeric,CURRENT_TIMESTAMP-INTERVAL '60 days'),
        (3010,4002,14,'EXPERT',  23000000::numeric,CURRENT_TIMESTAMP-INTERVAL '59 days'),
        (3020,4003,9, 'BUSINESS',26000000::numeric,CURRENT_TIMESTAMP-INTERVAL '56 days'),
        (3030,4003,17,'EXPERT',  13000000::numeric,CURRENT_TIMESTAMP-INTERVAL '55 days'),
        (3040,4004,10,'BUSINESS',57000000::numeric,CURRENT_TIMESTAMP-INTERVAL '52 days'),
        (3050,4004,19,'EXPERT',  28500000::numeric,CURRENT_TIMESTAMP-INTERVAL '51 days'),
        (3060,4005,11,'BUSINESS',30000000::numeric,CURRENT_TIMESTAMP-INTERVAL '136 days'),
        (3070,4005,20,'EXPERT',  15000000::numeric,CURRENT_TIMESTAMP-INTERVAL '135 days'),
        (3080,4006,11,'BUSINESS',40000000::numeric,CURRENT_TIMESTAMP-INTERVAL '196 days'),
        (3090,4006,15,'EXPERT',  20000000::numeric,CURRENT_TIMESTAMP-INTERVAL '195 days')
)
INSERT INTO seed_wallet_events
SELECT event_base + leg.n, account_id, NULL,
       CASE owner_role WHEN 'BUSINESS' THEN 'CONTRACT_SECURITY_DEPOSIT_HOLD' ELSE 'EXPERT_CONTRACT_DEPOSIT_HOLD' END,
       CASE leg.n WHEN 1 THEN 'DEBIT' ELSE 'HOLD' END,
       CASE leg.n WHEN 1 THEN 'AVAILABLE' ELSE 'ESCROW' END,
       amount, 'CONTRACT_DEPOSIT', contract_id,
       CASE owner_role WHEN 'BUSINESS' THEN 'Doanh nghiệp ký quỹ cho hợp đồng ' ELSE 'Chuyên gia ký quỹ cho hợp đồng ' END || contract_id,
       contract_id, NULL,
       jsonb_build_object('contractId',contract_id,'ownerRole',owner_role),
       'CONTRACT_DEPOSIT:' || contract_id || ':' || owner_role,
       CASE leg.n WHEN 1 THEN 'AVAILABLE_DEBIT' ELSE 'ESCROW_HOLD' END,
       happened_at
FROM deposits CROSS JOIN (VALUES (1),(2)) AS leg(n);

-- The closed contract has had both participant deposits refunded.
WITH refunds(event_base, contract_id, account_id, owner_role, amount, happened_at) AS (
    VALUES
        (3100,4006,11,'BUSINESS',40000000::numeric,CURRENT_TIMESTAMP-INTERVAL '89 days'),
        (3110,4006,15,'EXPERT',  20000000::numeric,CURRENT_TIMESTAMP-INTERVAL '89 days')
)
INSERT INTO seed_wallet_events
SELECT event_base + leg.n, account_id, NULL,
       CASE owner_role WHEN 'BUSINESS' THEN 'CONTRACT_SECURITY_DEPOSIT_REFUND' ELSE 'EXPERT_CONTRACT_DEPOSIT_REFUND' END,
       CASE leg.n WHEN 1 THEN 'RELEASE' ELSE 'CREDIT' END,
       CASE leg.n WHEN 1 THEN 'ESCROW' ELSE 'AVAILABLE' END,
       amount, 'CONTRACT_DEPOSIT', contract_id,
       'Hoàn ký quỹ ' || lower(owner_role) || ' cho hợp đồng ' || contract_id,
       contract_id, NULL,
       jsonb_build_object('contractId',contract_id,'ownerRole',owner_role,'resolution','REFUNDED'),
       'CONTRACT_DEPOSIT_REFUND:' || contract_id || ':' || owner_role,
       CASE leg.n WHEN 1 THEN 'ESCROW_RELEASE' ELSE 'AVAILABLE_CREDIT' END,
       happened_at
FROM refunds CROSS JOIN (VALUES (1),(2)) AS leg(n);

-- Milestone escrow holds for active/reviewed/completed work.
WITH escrows(event_base, contract_id, milestone_id, business_account_id, amount, happened_at) AS (
    VALUES
        (4000,4002,2016,8, 55000000::numeric,CURRENT_TIMESTAMP-INTERVAL '45 days'),
        (4010,4003,2019,9, 30000000::numeric,CURRENT_TIMESTAMP-INTERVAL '48 days'),
        (4020,4003,2020,9, 65000000::numeric,CURRENT_TIMESTAMP-INTERVAL '31 days'),
        (4030,4004,2022,10,70000000::numeric,CURRENT_TIMESTAMP-INTERVAL '43 days'),
        (4040,4005,2025,11,35000000::numeric,CURRENT_TIMESTAMP-INTERVAL '130 days'),
        (4050,4005,2026,11,75000000::numeric,CURRENT_TIMESTAMP-INTERVAL '112 days'),
        (4060,4005,2027,11,40000000::numeric,CURRENT_TIMESTAMP-INTERVAL '70 days'),
        (4070,4006,2028,11,45000000::numeric,CURRENT_TIMESTAMP-INTERVAL '188 days'),
        (4080,4006,2029,11,100000000::numeric,CURRENT_TIMESTAMP-INTERVAL '160 days'),
        (4090,4006,2030,11,55000000::numeric,CURRENT_TIMESTAMP-INTERVAL '120 days')
)
INSERT INTO seed_wallet_events
SELECT event_base + leg.n, business_account_id, NULL,
       'MILESTONE_ESCROW_DEPOSIT',
       CASE leg.n WHEN 1 THEN 'DEBIT' ELSE 'HOLD' END,
       CASE leg.n WHEN 1 THEN 'AVAILABLE' ELSE 'ESCROW' END,
       amount, 'MILESTONE', milestone_id,
       'Ký quỹ milestone ' || milestone_id || ' thuộc hợp đồng ' || contract_id,
       contract_id, milestone_id,
       jsonb_build_object('contractId',contract_id,'milestoneId',milestone_id),
       'MILESTONE_ESCROW:' || milestone_id,
       CASE leg.n WHEN 1 THEN 'AVAILABLE_DEBIT' ELSE 'ESCROW_HOLD' END,
       happened_at
FROM escrows CROSS JOIN (VALUES (1),(2)) AS leg(n);

-- Releases for completed milestones: debit Business escrow and credit the
-- contract Expert's available balance under one operation key.
WITH releases(event_base, contract_id, milestone_id, business_account_id, expert_account_id, amount, happened_at) AS (
    VALUES
        (5000,4003,2019,9, 17, 30000000::numeric,CURRENT_TIMESTAMP-INTERVAL '34 days'),
        (5010,4005,2025,11,20, 35000000::numeric,CURRENT_TIMESTAMP-INTERVAL '120 days'),
        (5020,4005,2026,11,20, 75000000::numeric,CURRENT_TIMESTAMP-INTERVAL '75 days'),
        (5030,4005,2027,11,20, 40000000::numeric,CURRENT_TIMESTAMP-INTERVAL '35 days'),
        (5040,4006,2028,11,15, 45000000::numeric,CURRENT_TIMESTAMP-INTERVAL '180 days'),
        (5050,4006,2029,11,15,100000000::numeric,CURRENT_TIMESTAMP-INTERVAL '135 days'),
        (5060,4006,2030,11,15, 55000000::numeric,CURRENT_TIMESTAMP-INTERVAL '90 days')
)
INSERT INTO seed_wallet_events
SELECT event_base + leg.n,
       CASE leg.n WHEN 1 THEN business_account_id ELSE expert_account_id END,
       NULL,
       'MILESTONE_ESCROW_RELEASE',
       CASE leg.n WHEN 1 THEN 'DEBIT' ELSE 'CREDIT' END,
       CASE leg.n WHEN 1 THEN 'ESCROW' ELSE 'AVAILABLE' END,
       amount, 'MILESTONE', milestone_id,
       CASE leg.n WHEN 1 THEN 'Giải ngân escrow milestone ' ELSE 'Nhận giải ngân milestone ' END || milestone_id,
       contract_id, milestone_id,
       jsonb_build_object('contractId',contract_id,'milestoneId',milestone_id),
       'MILESTONE_RELEASE:' || milestone_id,
       CASE leg.n WHEN 1 THEN 'ESCROW_DEBIT' ELSE 'AVAILABLE_CREDIT' END,
       happened_at
FROM releases CROSS JOIN (VALUES (1),(2)) AS leg(n);

INSERT INTO wallet_transactions (
    system_wallet_id, account_id, payment_order_id, transaction_type,
    direction, balance_type, amount, balance_before, balance_after, status,
    reference_type, reference_id, description, created_at, contract_id,
    milestone_id, metadata, operation_key, operation_leg
)
SELECT
    sw.system_wallet_id,
    calculated.account_id,
    calculated.payment_order_id,
    calculated.transaction_type,
    calculated.direction,
    calculated.balance_type,
    calculated.amount,
    calculated.balance_after - calculated.delta,
    calculated.balance_after,
    'POSTED',
    calculated.reference_type,
    calculated.reference_id,
    calculated.description,
    calculated.created_at,
    calculated.contract_id,
    calculated.milestone_id,
    calculated.metadata,
    calculated.operation_key,
    calculated.operation_leg
FROM (
    SELECT
        e.*,
        CASE WHEN e.direction IN ('CREDIT','HOLD') THEN e.amount ELSE -e.amount END AS delta,
        SUM(CASE WHEN e.direction IN ('CREDIT','HOLD') THEN e.amount ELSE -e.amount END)
            OVER (PARTITION BY e.account_id, e.balance_type ORDER BY e.created_at, e.event_order
                  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS balance_after
    FROM seed_wallet_events e
) calculated
JOIN system_wallet sw ON sw.account_id = calculated.account_id
ORDER BY calculated.created_at, calculated.event_order;

-- Derive each user wallet from posted ledger deltas.
WITH balances AS (
    SELECT
        account_id,
        COALESCE(SUM(CASE WHEN balance_type = 'AVAILABLE' THEN
            CASE WHEN direction IN ('CREDIT','HOLD') THEN amount ELSE -amount END ELSE 0 END), 0) AS available,
        COALESCE(SUM(CASE WHEN balance_type = 'ESCROW' THEN
            CASE WHEN direction IN ('CREDIT','HOLD') THEN amount ELSE -amount END ELSE 0 END), 0) AS escrow,
        COALESCE(SUM(CASE WHEN balance_type = 'HOLDING' THEN
            CASE WHEN direction IN ('CREDIT','HOLD') THEN amount ELSE -amount END ELSE 0 END), 0) AS holding,
        COALESCE(SUM(CASE WHEN balance_type = 'DISPUTE' THEN
            CASE WHEN direction IN ('CREDIT','HOLD') THEN amount ELSE -amount END ELSE 0 END), 0) AS disputed
    FROM wallet_transactions
    GROUP BY account_id
)
UPDATE system_wallet sw
SET available_balance = b.available,
    escrow_balance = b.escrow,
    holding_balance = b.holding,
    disputed_balance = b.disputed,
    current_balance = b.available + b.escrow + b.holding + b.disputed,
    last_synced_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
FROM balances b
WHERE b.account_id = sw.account_id;

-- Match the exact aggregate source used by SystemWalletService.syncWallet().
WITH platform_totals AS (
    SELECT
        COALESCE(SUM(amount) FILTER (
            WHERE direction = 'DEBIT' AND balance_type = 'AVAILABLE'
              AND transaction_type IN ('MEMBERSHIP_PURCHASE','CREDIT_PURCHASE')
        ),0) AS revenue,
        COALESCE(SUM(CASE
            WHEN direction = 'HOLD' AND balance_type = 'ESCROW' THEN amount
            WHEN direction IN ('RELEASE','DEBIT') AND balance_type = 'ESCROW' THEN -amount
            ELSE 0
        END),0) AS escrow
    FROM wallet_transactions
)
UPDATE system_wallet sw
SET total_revenue = p.revenue,
    available_balance = p.revenue,
    escrow_balance = p.escrow,
    current_balance = p.revenue + p.escrow,
    holding_balance = 0,
    disputed_balance = 0,
    last_synced_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
FROM platform_totals p
WHERE sw.account_id = 3;

INSERT INTO membership_purchases (
    purchase_id, account_id, package_id, amount, status, badge_start_at,
    badge_end_at, wallet_transaction_id, created_at, updated_at
) VALUES
    (1,1,3,1000,'SUCCESS',CURRENT_TIMESTAMP-INTERVAL '75 days',CURRENT_TIMESTAMP+INTERVAL '15 days',(SELECT id FROM wallet_transactions WHERE operation_key='MEMBERSHIP_PURCHASE:1' AND operation_leg='PURCHASER_AVAILABLE_DEBIT'),CURRENT_TIMESTAMP-INTERVAL '75 days',CURRENT_TIMESTAMP-INTERVAL '75 days'),
    (2,2,4,100,'SUCCESS',CURRENT_TIMESTAMP-INTERVAL '74 days',CURRENT_TIMESTAMP-INTERVAL '44 days',(SELECT id FROM wallet_transactions WHERE operation_key='MEMBERSHIP_PURCHASE:2' AND operation_leg='PURCHASER_AVAILABLE_DEBIT'),CURRENT_TIMESTAMP-INTERVAL '74 days',CURRENT_TIMESTAMP-INTERVAL '74 days'),
    (3,6,1,200,'SUCCESS',CURRENT_TIMESTAMP-INTERVAL '40 days',CURRENT_TIMESTAMP-INTERVAL '10 days',(SELECT id FROM wallet_transactions WHERE operation_key='MEMBERSHIP_PURCHASE:3' AND operation_leg='PURCHASER_AVAILABLE_DEBIT'),CURRENT_TIMESTAMP-INTERVAL '40 days',CURRENT_TIMESTAMP-INTERVAL '40 days');

INSERT INTO contract_deposits (
    deposit_id, contract_id, business_id, deposit_amount, held_amount,
    refunded_amount, resolved_amount, status, hold_transaction_id,
    refund_transaction_id, admin_id, admin_note, paid_at, refunded_at,
    owner_account_id, owner_role, required_percentage, required_amount,
    penalty_amount, resolution_type, resolved_at, created_at, updated_at
) VALUES
    (1,4002,5,46000000,46000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4002:BUSINESS' AND operation_leg='ESCROW_HOLD'),NULL,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '60 days',NULL,8,'BUSINESS',20,46000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '60 days',CURRENT_TIMESTAMP),
    (2,4002,5,23000000,23000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4002:EXPERT' AND operation_leg='ESCROW_HOLD'),NULL,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '59 days',NULL,14,'EXPERT',10,23000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '59 days',CURRENT_TIMESTAMP),
    (3,4003,6,26000000,26000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4003:BUSINESS' AND operation_leg='ESCROW_HOLD'),NULL,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '56 days',NULL,9,'BUSINESS',20,26000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '56 days',CURRENT_TIMESTAMP),
    (4,4003,6,13000000,13000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4003:EXPERT' AND operation_leg='ESCROW_HOLD'),NULL,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '55 days',NULL,17,'EXPERT',10,13000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '55 days',CURRENT_TIMESTAMP),
    (5,4004,7,57000000,57000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4004:BUSINESS' AND operation_leg='ESCROW_HOLD'),NULL,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '52 days',NULL,10,'BUSINESS',20,57000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '52 days',CURRENT_TIMESTAMP),
    (6,4004,7,28500000,28500000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4004:EXPERT' AND operation_leg='ESCROW_HOLD'),NULL,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '51 days',NULL,19,'EXPERT',10,28500000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '51 days',CURRENT_TIMESTAMP),
    (7,4005,8,30000000,30000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4005:BUSINESS' AND operation_leg='ESCROW_HOLD'),NULL,NULL,'Chờ hoàn ký quỹ sau khi đóng hợp đồng.',CURRENT_TIMESTAMP-INTERVAL '136 days',NULL,11,'BUSINESS',20,30000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '136 days',CURRENT_TIMESTAMP),
    (8,4005,8,15000000,15000000,0,0,'HELD',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4005:EXPERT' AND operation_leg='ESCROW_HOLD'),NULL,NULL,'Chờ hoàn ký quỹ sau khi đóng hợp đồng.',CURRENT_TIMESTAMP-INTERVAL '135 days',NULL,20,'EXPERT',10,15000000,0,NULL,NULL,CURRENT_TIMESTAMP-INTERVAL '135 days',CURRENT_TIMESTAMP),
    (9,4006,8,40000000,0,40000000,0,'REFUNDED',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4006:BUSINESS' AND operation_leg='ESCROW_HOLD'),(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT_REFUND:4006:BUSINESS' AND operation_leg='AVAILABLE_CREDIT'),3,'Đã hoàn ký quỹ khi hợp đồng được đóng.',CURRENT_TIMESTAMP-INTERVAL '196 days',CURRENT_TIMESTAMP-INTERVAL '89 days',11,'BUSINESS',20,40000000,0,'REFUND',CURRENT_TIMESTAMP-INTERVAL '89 days',CURRENT_TIMESTAMP-INTERVAL '196 days',CURRENT_TIMESTAMP-INTERVAL '89 days'),
    (10,4006,8,20000000,0,20000000,0,'REFUNDED',(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT:4006:EXPERT' AND operation_leg='ESCROW_HOLD'),(SELECT id FROM wallet_transactions WHERE operation_key='CONTRACT_DEPOSIT_REFUND:4006:EXPERT' AND operation_leg='AVAILABLE_CREDIT'),3,'Đã hoàn ký quỹ khi hợp đồng được đóng.',CURRENT_TIMESTAMP-INTERVAL '195 days',CURRENT_TIMESTAMP-INTERVAL '89 days',15,'EXPERT',10,20000000,0,'REFUND',CURRENT_TIMESTAMP-INTERVAL '89 days',CURRENT_TIMESTAMP-INTERVAL '195 days',CURRENT_TIMESTAMP-INTERVAL '89 days');

INSERT INTO deliverables (
    deliverable_id, milestone_id, source_code_url, source_code_file_url,
    demo_link, submission_notes, submission_round, status,
    created_at, updated_at
) VALUES
    (6001,2019,'https://git.example.test/search/hybrid-index','https://assets.example.test/deliverables/6001-source.zip','https://demo.example.test/search/index','Bàn giao chỉ mục kết hợp, bộ truy vấn đánh giá và báo cáo NDCG.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '35 days',CURRENT_TIMESTAMP-INTERVAL '34 days'),
    (6002,2022,'https://git.example.test/agriculture/dataset-pipeline','https://assets.example.test/deliverables/6002-source.zip','https://demo.example.test/agriculture/dataset','Bàn giao pipeline dữ liệu, hướng dẫn gắn nhãn và báo cáo phân bố bộ ảnh.',1,'SUBMITTED',CURRENT_TIMESTAMP-INTERVAL '2 days',CURRENT_TIMESTAMP-INTERVAL '2 days'),
    (6003,2025,'https://git.example.test/education/content-policy','https://assets.example.test/deliverables/6003-source.zip','https://demo.example.test/tutor/policy','Bàn giao kho giáo trình chuẩn hóa và chính sách gợi ý theo cấp độ.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '121 days',CURRENT_TIMESTAMP-INTERVAL '120 days'),
    (6004,2026,'https://git.example.test/education/rag-tutor','https://assets.example.test/deliverables/6004-source.zip','https://demo.example.test/tutor/chat','Bàn giao dịch vụ RAG, tích hợp lớp học và bộ kiểm thử an toàn.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '76 days',CURRENT_TIMESTAMP-INTERVAL '75 days'),
    (6005,2027,'https://git.example.test/education/tutor-dashboard','https://assets.example.test/deliverables/6005-source.zip','https://demo.example.test/tutor/dashboard','Bàn giao dashboard chất lượng, runbook và biên bản nghiệm thu cuối.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '36 days',CURRENT_TIMESTAMP-INTERVAL '35 days'),
    (6006,2028,'https://git.example.test/education/feedback-pipeline','https://assets.example.test/deliverables/6006-source.zip','https://demo.example.test/retention/pipeline','Bàn giao mô hình dữ liệu, pipeline phản hồi và báo cáo kiểm soát chất lượng.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '181 days',CURRENT_TIMESTAMP-INTERVAL '180 days'),
    (6007,2029,'https://git.example.test/education/retention-model','https://assets.example.test/deliverables/6007-source.zip','https://demo.example.test/retention/model','Bàn giao mô hình chủ đề, rủi ro bỏ học và báo cáo đánh giá theo nhóm.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '136 days',CURRENT_TIMESTAMP-INTERVAL '135 days'),
    (6008,2030,'https://git.example.test/education/intervention-dashboard','https://assets.example.test/deliverables/6008-source.zip','https://demo.example.test/retention/dashboard','Bàn giao dashboard can thiệp, tài liệu vận hành và checklist nghiệm thu.',1,'APPROVED',CURRENT_TIMESTAMP-INTERVAL '91 days',CURRENT_TIMESTAMP-INTERVAL '90 days');

INSERT INTO reviews (review_id, contract_id, reviewer_id, reviewee_id, rating, comment, created_at) VALUES
    (1,4006,11,15,5,'Chuyên gia bám sát SoW, minh bạch tiến độ và bàn giao đầy đủ pipeline, mô hình cùng tài liệu vận hành.',CURRENT_TIMESTAMP-INTERVAL '87 days'),
    (2,4006,15,11,5,'Doanh nghiệp cung cấp dữ liệu đúng cam kết, phản hồi nghiệm thu rõ ràng và phối hợp đúng timeline.',CURRENT_TIMESTAMP-INTERVAL '87 days');

-- Audit rows use the same actions/entity names emitted by runtime services.
INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT 4, 'Duyệt hồ sơ doanh nghiệp', 'business_profiles', bp.business_id::text,
       '{"kybStatus":"Pending"}'::jsonb,
       jsonb_build_object('kybStatus','Approved','accountId',bp.account_id,'companyName',bp.company_name),
       bp.created_at
FROM business_profiles bp;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT 4, 'Duyệt hồ sơ chuyên gia', 'expert_profiles', ep.expert_id::text,
       '{"kycStatus":"Pending"}'::jsonb,
       jsonb_build_object('kycStatus','Approved','accountId',ep.account_id,'portfolioUrl',ep.portfolio_url),
       ep.created_at
FROM expert_profiles ep;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Tạo job nháp', 'jobs', j.job_id::text, NULL,
       jsonb_build_object('title',j.title,'status','DRAFT','budget',j.budget),
       j.created_at
FROM jobs j JOIN business_profiles bp ON bp.business_id = j.business_id;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Đổi trạng thái job', 'jobs', j.job_id::text,
       '{"status":"DRAFT"}'::jsonb,
       jsonb_build_object('status',j.status,'publishedAt',j.published_at),
       j.published_at
FROM jobs j JOIN business_profiles bp ON bp.business_id = j.business_id
WHERE j.published_at IS NOT NULL;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT ep.account_id, 'Gửi proposal', 'proposals', p.proposal_id::text, NULL,
       jsonb_build_object('jobId',p.job_id,'expertId',p.expert_id,'bidAmount',p.bid_amount,'status','Pending'),
       p.created_at
FROM proposals p JOIN expert_profiles ep ON ep.expert_id = p.expert_id;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Duyệt proposal', 'proposals', p.proposal_id::text,
       '{"status":"Pending"}'::jsonb,
       jsonb_build_object('status',p.status,'jobId',p.job_id,'expertId',p.expert_id),
       p.updated_at
FROM proposals p
JOIN jobs j ON j.job_id = p.job_id
JOIN business_profiles bp ON bp.business_id = j.business_id
WHERE p.status <> 'Pending';

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Tạo hợp đồng nháp', 'contracts', c.contract_id::text, NULL,
       jsonb_build_object('jobId',c.job_id,'proposalId',c.proposal_id,'businessId',c.business_id,'expertId',c.expert_id,'totalBudget',c.total_budget,'status','DRAFT'),
       c.created_at
FROM contracts c JOIN business_profiles bp ON bp.business_id = c.business_id;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT participant.account_id, 'Xác nhận hợp đồng', 'contracts', c.contract_id::text,
       NULL, jsonb_build_object('party',participant.party,'acceptedAt',participant.accepted_at), participant.accepted_at
FROM contracts c
JOIN business_profiles bp ON bp.business_id = c.business_id
JOIN expert_profiles ep ON ep.expert_id = c.expert_id
CROSS JOIN LATERAL (VALUES
    (bp.account_id,'BUSINESS',c.business_accepted_at),
    (ep.account_id,'EXPERT',c.expert_accepted_at)
) AS participant(account_id,party,accepted_at)
WHERE participant.accepted_at IS NOT NULL;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Kích hoạt hợp đồng', 'contracts', c.contract_id::text,
       '{"status":"PENDING"}'::jsonb,
       jsonb_build_object('status',CASE WHEN c.status IN ('ACTIVE','COMPLETED','CLOSED') THEN 'ACTIVE' ELSE c.status END,'activatedAt',c.activated_at),
       c.activated_at
FROM contracts c JOIN business_profiles bp ON bp.business_id = c.business_id
WHERE c.activated_at IS NOT NULL;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Hoàn tất hợp đồng', 'contracts', c.contract_id::text,
       '{"status":"ACTIVE"}'::jsonb, jsonb_build_object('status',c.status), c.updated_at
FROM contracts c JOIN business_profiles bp ON bp.business_id = c.business_id
WHERE c.status IN ('COMPLETED','CLOSED');

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Tạo milestone', 'milestones', m.milestone_id::text, NULL,
       jsonb_build_object('jobId',m.job_id,'name',m.milestone_name,'budget',m.funds_allocated,'duration',m.duration,'durationUnit',m.duration_unit),
       m.created_at
FROM milestones m JOIN jobs j ON j.job_id=m.job_id JOIN business_profiles bp ON bp.business_id=j.business_id;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT ep.account_id, 'Nộp sản phẩm bàn giao', 'deliverables', d.deliverable_id::text, NULL,
       jsonb_build_object('milestoneId',d.milestone_id,'status','SUBMITTED','sourceCodeUrl',d.source_code_url), d.created_at
FROM deliverables d
JOIN milestones m ON m.milestone_id=d.milestone_id
JOIN contracts c ON c.job_id=m.job_id
JOIN expert_profiles ep ON ep.expert_id=c.expert_id;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT bp.account_id, 'Duyệt milestone', 'milestones', m.milestone_id::text,
       '{"status":"UNDER_REVIEW"}'::jsonb, '{"status":"COMPLETED"}'::jsonb, m.updated_at
FROM milestones m
JOIN jobs j ON j.job_id=m.job_id
JOIN business_profiles bp ON bp.business_id=j.business_id
WHERE m.status='COMPLETED';

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at)
SELECT po.account_id::integer, 'Nạp tiền vào ví thành công', 'payment_order', po.id::text,
       '{"status":"PENDING"}'::jsonb,
       jsonb_build_object('status','PAID','amount',po.amount,'providerOrderCode',po.provider_order_code),
       po.paid_at
FROM payment_order po;

INSERT INTO audit_logs (actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, created_at) VALUES
    (1,'Mua gói thành viên','membership_purchases','1',NULL,'{"packageCode":"BUSINESS_PREMIUM","amount":1000}'::jsonb,CURRENT_TIMESTAMP-INTERVAL '75 days'),
    (2,'Mua gói thành viên','membership_purchases','2',NULL,'{"packageCode":"EXPERT_STANDARD","amount":100}'::jsonb,CURRENT_TIMESTAMP-INTERVAL '74 days'),
    (6,'Mua gói thành viên','membership_purchases','3',NULL,'{"packageCode":"BUSINESS_STANDARD","amount":200}'::jsonb,CURRENT_TIMESTAMP-INTERVAL '40 days');

-- Notification rows are also the persistence source for WebSocket history.
INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT bp.account_id, 4, 'PROFILE_REVIEWED', 'Hồ sơ đã được duyệt',
       'Hồ sơ doanh nghiệp của bạn đã được nhân sự xét duyệt.', '/business/kyb',
       TRUE, bp.created_at + INTERVAL '1 hour',
       jsonb_build_object('profileType','BUSINESS','profileId',bp.business_id)::text,
       'SEED:PROFILE:BUSINESS:'||bp.business_id, bp.created_at
FROM business_profiles bp;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT ep.account_id, 4, 'PROFILE_REVIEWED', 'Hồ sơ đã được duyệt',
       'Hồ sơ chuyên gia của bạn đã được nhân sự xét duyệt.', '/expert/profile',
       TRUE, ep.created_at + INTERVAL '1 hour',
       jsonb_build_object('profileType','EXPERT','profileId',ep.expert_id)::text,
       'SEED:PROFILE:EXPERT:'||ep.expert_id, ep.created_at
FROM expert_profiles ep;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT po.account_id::integer, NULL, 'WALLET_TOPUP_SUCCEEDED', 'Nạp tiền thành công',
       'Ví của bạn đã được nạp ' || trim(to_char(po.amount,'FM999G999G999G999')) || ' VND.', '/wallet',
       TRUE, po.paid_at + INTERVAL '2 hours',
       jsonb_build_object('orderCode',po.provider_order_code,'amount',po.amount)::text,
       'SEED:TOPUP:'||po.id, po.paid_at
FROM payment_order po;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT bp.account_id, ep.account_id, 'PROPOSAL_CREATED', 'Có proposal mới',
       'Chuyên gia ' || ea.full_name || ' đã gửi proposal cho dự án "' || j.title || '".',
       '/business/jobs/'||j.job_id||'/proposals',
       p.created_at < CURRENT_TIMESTAMP-INTERVAL '30 days',
       CASE WHEN p.created_at < CURRENT_TIMESTAMP-INTERVAL '30 days' THEN p.created_at+INTERVAL '1 day' END,
       jsonb_build_object('jobId',j.job_id,'proposalId',p.proposal_id,'expertId',p.expert_id)::text,
       'SEED:PROPOSAL:CREATED:'||p.proposal_id, p.created_at
FROM proposals p
JOIN jobs j ON j.job_id=p.job_id
JOIN business_profiles bp ON bp.business_id=j.business_id
JOIN expert_profiles ep ON ep.expert_id=p.expert_id
JOIN account ea ON ea.account_id=ep.account_id;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT ep.account_id, bp.account_id, 'PROPOSAL_REVIEWED', 'Kết quả proposal',
       'Proposal của bạn cho dự án "' || j.title || '" ' || CASE p.status WHEN 'Accepted' THEN 'đã được chấp nhận.' ELSE 'đã bị từ chối.' END,
       '/expert/proposals', TRUE, p.updated_at+INTERVAL '1 hour',
       jsonb_build_object('jobId',j.job_id,'proposalId',p.proposal_id,'status',p.status)::text,
       'SEED:PROPOSAL:REVIEWED:'||p.proposal_id, p.updated_at
FROM proposals p
JOIN jobs j ON j.job_id=p.job_id
JOIN business_profiles bp ON bp.business_id=j.business_id
JOIN expert_profiles ep ON ep.expert_id=p.expert_id
WHERE p.status <> 'Pending';

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT ep.account_id, bp.account_id, 'CONTRACT_CREATED', 'Có hợp đồng mới',
       'Doanh nghiệp đã tạo hợp đồng "' || c.contract_title || '" từ proposal được chấp nhận.',
       '/contracts/'||c.contract_id, TRUE, c.created_at+INTERVAL '2 hours',
       jsonb_build_object('contractId',c.contract_id,'jobId',c.job_id,'proposalId',c.proposal_id)::text,
       'SEED:CONTRACT:CREATED:'||c.contract_id, c.created_at
FROM contracts c
JOIN business_profiles bp ON bp.business_id=c.business_id
JOIN expert_profiles ep ON ep.expert_id=c.expert_id;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT participant.receiver_account_id, bp.account_id, 'CONTRACT_ACTIVATED', 'Hợp đồng đã được kích hoạt',
       'Hai bên đã ký và hoàn tất ký quỹ cho hợp đồng "' || c.contract_title || '".',
       '/contracts/'||c.contract_id, c.status IN ('COMPLETED','CLOSED'),
       CASE WHEN c.status IN ('COMPLETED','CLOSED') THEN c.activated_at+INTERVAL '1 day' END,
       jsonb_build_object('contractId',c.contract_id,'status','ACTIVE')::text,
       'SEED:CONTRACT:ACTIVATED:'||c.contract_id||':'||participant.receiver_account_id,
       c.activated_at
FROM contracts c
JOIN business_profiles bp ON bp.business_id=c.business_id
JOIN expert_profiles ep ON ep.expert_id=c.expert_id
CROSS JOIN LATERAL (VALUES (bp.account_id),(ep.account_id)) AS participant(receiver_account_id)
WHERE c.activated_at IS NOT NULL;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT bp.account_id, ep.account_id, 'DELIVERABLE_SUBMITTED', 'Có sản phẩm bàn giao mới',
       'Chuyên gia đã nộp sản phẩm cho milestone "' || m.milestone_name || '".',
       '/contracts/'||c.contract_id||'/workspace?milestoneId='||m.milestone_id,
       d.status='APPROVED', CASE WHEN d.status='APPROVED' THEN d.updated_at END,
       jsonb_build_object('contractId',c.contract_id,'milestoneId',m.milestone_id,'deliverableId',d.deliverable_id)::text,
       'SEED:DELIVERABLE:'||d.deliverable_id, d.created_at
FROM deliverables d
JOIN milestones m ON m.milestone_id=d.milestone_id
JOIN contracts c ON c.job_id=m.job_id
JOIN business_profiles bp ON bp.business_id=c.business_id
JOIN expert_profiles ep ON ep.expert_id=c.expert_id;

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT ep.account_id, bp.account_id, 'MILESTONE_APPROVED', 'Milestone đã được duyệt',
       'Doanh nghiệp đã duyệt milestone "' || m.milestone_name || '" và ghi nhận giải ngân.',
       '/contracts/'||c.contract_id||'/workspace?milestoneId='||m.milestone_id,
       TRUE, m.updated_at,
       jsonb_build_object('contractId',c.contract_id,'milestoneId',m.milestone_id)::text,
       'SEED:MILESTONE:APPROVED:'||m.milestone_id, m.updated_at
FROM milestones m
JOIN contracts c ON c.job_id=m.job_id
JOIN business_profiles bp ON bp.business_id=c.business_id
JOIN expert_profiles ep ON ep.expert_id=c.expert_id
WHERE m.status='COMPLETED';

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT participant.receiver_account_id, bp.account_id, 'CONTRACT_COMPLETED', 'Hợp đồng đã hoàn tất',
       'Tất cả milestone của hợp đồng "' || c.contract_title || '" đã hoàn thành.',
       '/contracts/'||c.contract_id, TRUE, c.updated_at,
       jsonb_build_object('contractId',c.contract_id,'status',c.status)::text,
       'SEED:CONTRACT:COMPLETED:'||c.contract_id||':'||participant.receiver_account_id,
       c.updated_at
FROM contracts c
JOIN business_profiles bp ON bp.business_id=c.business_id
JOIN expert_profiles ep ON ep.expert_id=c.expert_id
CROSS JOIN LATERAL (VALUES (bp.account_id),(ep.account_id)) AS participant(receiver_account_id)
WHERE c.status IN ('COMPLETED','CLOSED');

-- Abort the migration instead of allowing a partially coherent fixture.
DO $$
DECLARE
    shared_password CONSTANT VARCHAR := '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC';
BEGIN
    IF (SELECT COUNT(*) FROM account) <> 31
       OR (SELECT COUNT(*) FROM account a JOIN roles r ON r.role_id=a.role_id WHERE r.role_name='ADMIN') <> 1
       OR (SELECT COUNT(*) FROM account a JOIN roles r ON r.role_id=a.role_id WHERE r.role_name='BUSINESS') <> 10
       OR (SELECT COUNT(*) FROM account a JOIN roles r ON r.role_id=a.role_id WHERE r.role_name='EXPERT') <> 10
       OR (SELECT COUNT(*) FROM account a JOIN roles r ON r.role_id=a.role_id WHERE r.role_name='STAFF') <> 10 THEN
        RAISE EXCEPTION 'Demo account count or role distribution is invalid';
    END IF;

    IF EXISTS (
        SELECT account_id,email,password FROM preserved_internal_accounts
        EXCEPT SELECT account_id,email,password FROM account WHERE account_id IN (1,2,3,4)
    ) OR EXISTS (SELECT 1 FROM account WHERE account_id > 4 AND password <> shared_password) THEN
        RAISE EXCEPTION 'Internal credentials changed or a non-internal password differs from the shared demo password';
    END IF;

    IF (SELECT COUNT(*) FROM domains) <> 30
       OR (SELECT COUNT(*) FROM skills) <> 30
       OR (SELECT COUNT(*) FROM technologies) <> 30 THEN
        RAISE EXCEPTION 'Catalog rebuild must contain exactly 30 domains, 30 skills, and 30 technologies';
    END IF;

    IF (SELECT COUNT(*) FROM business_profiles) <> 10
       OR (SELECT COUNT(*) FROM expert_profiles) <> 10
       OR (SELECT COUNT(*) FROM portfolios) <> 10
       OR (SELECT COUNT(*) FROM staffs) <> 10
       OR EXISTS (SELECT 1 FROM expert_profiles ep LEFT JOIN portfolios p ON p.expert_id=ep.expert_id WHERE p.portfolio_id IS NULL) THEN
        RAISE EXCEPTION 'Profile, portfolio, or Staff coverage is incomplete';
    END IF;

    IF (SELECT COUNT(*) FROM staff_domains sd JOIN domains d ON d.domain_id=sd.domain_id WHERE d.domain_code='PROFILE_REVIEW') <> 1
       OR NOT EXISTS (SELECT 1 FROM staff_domains sd JOIN domains d ON d.domain_id=sd.domain_id WHERE sd.staff_id=1 AND d.domain_code='PROFILE_REVIEW')
       OR (SELECT COUNT(*) FROM staff_domains WHERE staff_id=1) <> 1
       OR EXISTS (SELECT 1 FROM job_domains jd JOIN domains d ON d.domain_id=jd.domain_id WHERE d.domain_code='PROFILE_REVIEW') THEN
        RAISE EXCEPTION 'PROFILE_REVIEW must belong only to the internal review Staff and never to a job';
    END IF;

    IF (SELECT COUNT(*) FROM jobs) <> 10
       OR EXISTS (
            SELECT 1 FROM jobs j
            LEFT JOIN sow s ON s.job_id=j.job_id
            WHERE s.sow_id IS NULL OR s.overview IS NULL OR s.objectives IS NULL
               OR s.scope_of_work IS NULL OR s.deliverable IS NULL
               OR s.assumptions IS NULL OR s.out_of_scope IS NULL
               OR jsonb_typeof(j.structured_sow::jsonb) <> 'object'
       )
       OR EXISTS (SELECT 1 FROM jobs j WHERE (SELECT COUNT(*) FROM milestones m WHERE m.job_id=j.job_id) <> 3)
       OR EXISTS (SELECT 1 FROM milestones m WHERE (SELECT COUNT(*) FROM acceptance_criteria ac WHERE ac.milestone_id=m.milestone_id) <> 3)
       OR EXISTS (SELECT 1 FROM jobs j WHERE j.budget <> (SELECT SUM(m.funds_allocated) FROM milestones m WHERE m.job_id=j.job_id))
       OR EXISTS (SELECT 1 FROM jobs j WHERE NOT EXISTS (SELECT 1 FROM job_domains jd WHERE jd.job_id=j.job_id)
                                      OR NOT EXISTS (SELECT 1 FROM job_skills js WHERE js.job_id=j.job_id)
                                      OR NOT EXISTS (SELECT 1 FROM job_technologies jt WHERE jt.job_id=j.job_id)) THEN
        RAISE EXCEPTION 'A job is missing valid SoW, milestones, criteria, catalog mappings, or exact budget allocation';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM proposals p
        WHERE jsonb_typeof(p.proposal_milestone::jsonb) <> 'array'
           OR (SELECT COUNT(*) FROM jsonb_array_elements(p.proposal_milestone::jsonb)) <> 3
           OR p.bid_amount <> (SELECT SUM((item->>'proposedBudget')::numeric) FROM jsonb_array_elements(p.proposal_milestone::jsonb) item)
           OR EXISTS (
                SELECT 1 FROM jsonb_array_elements(p.proposal_milestone::jsonb) item
                LEFT JOIN milestones m ON m.milestone_id=(item->>'milestoneId')::integer AND m.job_id=p.job_id
                WHERE m.milestone_id IS NULL
           )
    ) THEN
        RAISE EXCEPTION 'A proposal milestone payload is invalid or does not reconcile to its bid/job';
    END IF;

    IF EXISTS (
        SELECT 1 FROM contracts c
        JOIN proposals p ON p.proposal_id=c.proposal_id
        JOIN jobs j ON j.job_id=c.job_id
        WHERE p.status <> 'Accepted' OR p.job_id<>c.job_id OR p.expert_id<>c.expert_id OR j.business_id<>c.business_id
           OR c.contract_title IS NULL OR c.contract_scope IS NULL
           OR (SELECT COUNT(*) FROM contract_milestones cm WHERE cm.contract_id=c.contract_id) <> 3
           OR c.total_budget <> (SELECT SUM(cm.final_budget) FROM contract_milestones cm WHERE cm.contract_id=c.contract_id)
    ) THEN
        RAISE EXCEPTION 'A contract is not traceable to its accepted proposal/job/participants or milestone budget';
    END IF;

    IF (SELECT COUNT(*) FROM system_wallet) <> 31
       OR EXISTS (SELECT 1 FROM system_wallet WHERE current_balance <> available_balance+escrow_balance+holding_balance+disputed_balance)
       OR EXISTS (SELECT 1 FROM system_wallet WHERE current_balance<0 OR available_balance<0 OR escrow_balance<0 OR holding_balance<0 OR disputed_balance<0)
       OR EXISTS (
            WITH ordered AS (
                SELECT id,balance_before,
                       COALESCE(lag(balance_after) OVER (PARTITION BY account_id,balance_type ORDER BY created_at,id),0) expected_before
                FROM wallet_transactions
            )
            SELECT 1 FROM ordered WHERE balance_before<>expected_before
       ) THEN
        RAISE EXCEPTION 'Wallet balances or chronological ledger before/after values do not reconcile';
    END IF;

    IF EXISTS (
        SELECT 1 FROM system_wallet sw
        WHERE sw.account_id=3 AND (
            sw.total_revenue <> (SELECT COALESCE(SUM(amount),0) FROM wallet_transactions WHERE status='POSTED' AND direction='DEBIT' AND balance_type='AVAILABLE' AND transaction_type IN ('MEMBERSHIP_PURCHASE','CREDIT_PURCHASE'))
            OR sw.available_balance<>sw.total_revenue
            OR sw.escrow_balance <> (SELECT COALESCE(SUM(CASE WHEN direction='HOLD' AND balance_type='ESCROW' THEN amount WHEN direction IN ('RELEASE','DEBIT') AND balance_type='ESCROW' THEN -amount ELSE 0 END),0) FROM wallet_transactions WHERE status='POSTED')
        )
    ) THEN
        RAISE EXCEPTION 'Admin wallet revenue or escrow does not match SystemWalletService aggregate sources';
    END IF;

    IF EXISTS (SELECT 1 FROM jobs j WHERE NOT EXISTS (SELECT 1 FROM audit_logs a WHERE a.entity_name='jobs' AND a.entity_id=j.job_id::text))
       OR EXISTS (SELECT 1 FROM proposals p WHERE NOT EXISTS (SELECT 1 FROM audit_logs a WHERE a.entity_name='proposals' AND a.entity_id=p.proposal_id::text))
       OR EXISTS (SELECT 1 FROM contracts c WHERE NOT EXISTS (SELECT 1 FROM audit_logs a WHERE a.entity_name='contracts' AND a.entity_id=c.contract_id::text))
       OR EXISTS (SELECT 1 FROM proposals p WHERE NOT EXISTS (SELECT 1 FROM notifications n WHERE n.idempotency_key='SEED:PROPOSAL:CREATED:'||p.proposal_id)) THEN
        RAISE EXCEPTION 'Required audit or notification history is incomplete';
    END IF;
END $$;

SELECT setval(pg_get_serial_sequence('account','account_id'),(SELECT MAX(account_id) FROM account),TRUE);
SELECT setval(pg_get_serial_sequence('staffs','staff_id'),(SELECT MAX(staff_id) FROM staffs),TRUE);
SELECT setval(pg_get_serial_sequence('business_profiles','business_id'),(SELECT MAX(business_id) FROM business_profiles),TRUE);
SELECT setval(pg_get_serial_sequence('expert_profiles','expert_id'),(SELECT MAX(expert_id) FROM expert_profiles),TRUE);
SELECT setval(pg_get_serial_sequence('portfolios','portfolio_id'),(SELECT MAX(portfolio_id) FROM portfolios),TRUE);
SELECT setval(pg_get_serial_sequence('domains','domain_id'),(SELECT MAX(domain_id) FROM domains),TRUE);
SELECT setval(pg_get_serial_sequence('skills','skill_id'),(SELECT MAX(skill_id) FROM skills),TRUE);
SELECT setval(pg_get_serial_sequence('technologies','technology_id'),(SELECT MAX(technology_id) FROM technologies),TRUE);
SELECT setval(pg_get_serial_sequence('jobs','job_id'),(SELECT MAX(job_id) FROM jobs),TRUE);
SELECT setval(pg_get_serial_sequence('sow','sow_id'),(SELECT MAX(sow_id) FROM sow),TRUE);
SELECT setval(pg_get_serial_sequence('milestones','milestone_id'),(SELECT MAX(milestone_id) FROM milestones),TRUE);
SELECT setval(pg_get_serial_sequence('acceptance_criteria','criteria_id'),(SELECT MAX(criteria_id) FROM acceptance_criteria),TRUE);
SELECT setval(pg_get_serial_sequence('proposals','proposal_id'),(SELECT MAX(proposal_id) FROM proposals),TRUE);
SELECT setval(pg_get_serial_sequence('contracts','contract_id'),(SELECT MAX(contract_id) FROM contracts),TRUE);
SELECT setval(pg_get_serial_sequence('contract_milestones','contract_milestone_id'),(SELECT MAX(contract_milestone_id) FROM contract_milestones),TRUE);
SELECT setval(pg_get_serial_sequence('expert_recommendations','id'),(SELECT MAX(id) FROM expert_recommendations),TRUE);
SELECT setval(pg_get_serial_sequence('user_quotas','quota_id'),(SELECT MAX(quota_id) FROM user_quotas),TRUE);
SELECT setval(pg_get_serial_sequence('payment_order','id'),(SELECT MAX(id) FROM payment_order),TRUE);
SELECT setval(pg_get_serial_sequence('membership_purchases','purchase_id'),(SELECT MAX(purchase_id) FROM membership_purchases),TRUE);
SELECT setval(pg_get_serial_sequence('contract_deposits','deposit_id'),(SELECT MAX(deposit_id) FROM contract_deposits),TRUE);
SELECT setval(pg_get_serial_sequence('deliverables','deliverable_id'),(SELECT MAX(deliverable_id) FROM deliverables),TRUE);
SELECT setval(pg_get_serial_sequence('reviews','review_id'),(SELECT MAX(review_id) FROM reviews),TRUE);
SELECT setval(pg_get_serial_sequence('system_wallet','system_wallet_id'),(SELECT MAX(system_wallet_id) FROM system_wallet),TRUE);
