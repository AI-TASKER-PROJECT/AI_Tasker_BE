-- Expand the recommendation pool with realistic, specialization-driven Expert
-- profiles. The seed uses reserved @aitasker.local addresses and natural names;
-- catalog IDs remain exact numeric IDs because recommendation matching treats the
-- legacy portfolio taxonomy columns as delimited identifiers.

CREATE TEMP TABLE curated_expert_seed (
    email VARCHAR(255) PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    national_id VARCHAR(50) NOT NULL UNIQUE,
    portfolio_slug VARCHAR(120) NOT NULL UNIQUE,
    years_experience INT NOT NULL,
    domain_ids VARCHAR(255) NOT NULL,
    skill_ids VARCHAR(255) NOT NULL,
    technology_ids TEXT NOT NULL,
    certificates TEXT NOT NULL,
    self_description TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO curated_expert_seed (
    email, phone, full_name, national_id, portfolio_slug, years_experience,
    domain_ids, skill_ids, technology_ids, certificates, self_description
) VALUES
    (
        'nguyen.mai.linh@aitasker.local', '0935101101', 'Nguyễn Mai Linh',
        '079204001101', 'nguyen-mai-linh', 8,
        '1,2,5,12,14,20', '1,9,11,17,18,23,24,30', '7,8,11,12,13,26,29',
        'Google Professional Machine Learning Engineer; NVIDIA Deep Learning Institute - Computer Vision; Kubernetes and Cloud Native Associate',
        'Kỹ sư Computer Vision triển khai pipeline huấn luyện, đánh giá và dịch vụ suy luận trên Google Cloud. Có kinh nghiệm đưa mô hình kiểm lỗi hình ảnh từ thử nghiệm sang môi trường vận hành có giám sát.'
    ),
    (
        'tran.quoc.bao@aitasker.local', '0935101102', 'Trần Quốc Bảo',
        '079204001102', 'tran-quoc-bao', 7,
        '5,14,17,28,29', '1,9,11,17,21,23,27,30', '7,11,12,13,29,30',
        'Google Cloud Professional Cloud Architect; TensorFlow Developer Certificate; Healthcare Data Security Foundations',
        'Chuyên phát triển giải pháp thị giác y tế và xử lý tài liệu hình ảnh trên Google Cloud, chú trọng đánh giá sai lệch mô hình, bảo vệ dữ liệu nhạy cảm và kiểm thử hồi quy.'
    ),
    (
        'pham.thuy.duong@aitasker.local', '0935101103', 'Phạm Thùy Dương',
        '079204001103', 'pham-thuy-duong', 6,
        '5,15,20,22,29', '1,8,9,11,16,17,18,23', '7,11,13,26,29',
        'Google Professional Data Engineer; OpenCV University - Computer Vision; MLflow Model Lifecycle',
        'Kỹ sư AI xây dựng mô hình nhận diện cho nông nghiệp và sản xuất, từ chuẩn bị dữ liệu, huấn luyện đến triển khai thử nghiệm trên GCP và thiết bị biên.'
    ),
    (
        'le.minh.khoa@aitasker.local', '0935101104', 'Lê Minh Khoa',
        '079204001104', 'le-minh-khoa', 9,
        '3,4,13,14,27', '1,2,3,10,12,13,14,15,19,23,27', '2,3,4,5,7,11,16,17,18,19,27,28',
        'Google Cloud Generative AI Leader; LangChain for Production; Secure API Design Professional',
        'Kiến trúc sư ứng dụng GenAI thiết kế trợ lý RAG tiếng Việt, API tích hợp và giao diện nghiệp vụ. Có kinh nghiệm kết hợp Spring Boot, React và dịch vụ mô hình trên nền tảng đám mây.'
    ),
    (
        'vo.ngoc.ha@aitasker.local', '0935101105', 'Võ Ngọc Hà',
        '079204001105', 'vo-ngoc-ha', 8,
        '8,9,10,11,19', '1,4,5,6,7,16,17,22,23,30', '5,11,14,20,21,22,23,24',
        'Google Professional Data Engineer; dbt Analytics Engineering; Microsoft Power BI Data Analyst',
        'Chuyên gia dữ liệu xây dựng kho dữ liệu, pipeline dự báo nhu cầu và dashboard điều hành. Ưu tiên định nghĩa chỉ số rõ ràng, kiểm soát chất lượng dữ liệu và khả năng chạy lại.'
    ),
    (
        'dang.anh.tuan@aitasker.local', '0935101106', 'Đặng Anh Tuấn',
        '079204001106', 'dang-anh-tuan', 7,
        '7,8,19,26', '1,4,5,8,16,17,22,28,29', '5,14,22,24,25',
        'Recommender Systems Specialization; Tableau Certified Data Analyst; Product Analytics Micro-Certification',
        'Chuyên xây dựng hệ thống gợi ý và cá nhân hóa cho bán lẻ, kết hợp dữ liệu hành vi, thử nghiệm sản phẩm và dashboard theo dõi tác động kinh doanh.'
    ),
    (
        'bui.khanh.vy@aitasker.local', '0935101107', 'Bùi Khánh Vy',
        '079204001107', 'bui-khanh-vy', 6,
        '2,11,18,24,29', '1,4,7,8,16,17,21,27,30', '5,9,14,22,30',
        'AWS Machine Learning Specialty; Certified Fraud Examiner - Analytics Track; Playwright Automation',
        'Kỹ sư Machine Learning tập trung phát hiện gian lận và chấm điểm rủi ro, có kinh nghiệm kiểm thử mô hình, giải thích kết quả và bảo vệ pipeline dữ liệu tài chính.'
    ),
    (
        'hoang.duc.long@aitasker.local', '0935101108', 'Hoàng Đức Long',
        '079204001108', 'hoang-duc-long', 11,
        '12,13,14,24,29', '2,18,19,20,21,23,24,25,26,27,29', '2,5,6,7,8,9,10,11,20,26',
        'Certified Kubernetes Administrator; AWS Solutions Architect Professional; Google Professional Cloud DevOps Engineer',
        'Kiến trúc sư nền tảng AI phụ trách microservice, Kubernetes, CI/CD, quan sát hệ thống và kiểm soát bảo mật trên môi trường đa đám mây.'
    ),
    (
        'nguyen.hai.yen@aitasker.local', '0935101109', 'Nguyễn Hải Yến',
        '079204001109', 'nguyen-hai-yen', 7,
        '8,9,15,21', '1,4,6,8,16,18,23,30', '5,7,11,20,21,22',
        'Google Professional Data Engineer; Apache Kafka Fundamentals; Airflow DAG Authoring',
        'Kỹ sư dữ liệu và Edge AI xây dựng pipeline cảm biến, xử lý luồng thời gian thực và mô hình dự báo phục vụ vận hành logistics.'
    ),
    (
        'phan.gia.han@aitasker.local', '0935101110', 'Phan Gia Hân',
        '079204001110', 'phan-gia-han', 5,
        '4,6,23,27', '3,10,12,13,14,17,19,28,30', '3,4,5,11,15,16,17,18,27,28',
        'Hugging Face NLP Course; Google Cloud Digital Leader; Technical Writing for Software Teams',
        'Kỹ sư NLP phát triển trợ giảng và trợ lý hội thoại, chú trọng nội dung tiếng Việt, đánh giá chất lượng câu trả lời và trải nghiệm người dùng trên web.'
    ),
    (
        'truong.nhat.minh@aitasker.local', '0935101111', 'Trương Nhật Minh',
        '079204001111', 'truong-nhat-minh', 8,
        '4,25,28,29', '1,10,12,14,15,17,19,21,27,30', '5,15,16,17,18,19,27,30',
        'Document AI Professional; Privacy Engineering Foundations; ISTQB Test Automation Engineer',
        'Chuyên gia Document Intelligence xây dựng luồng OCR, trích xuất dữ liệu và RAG cho hồ sơ pháp lý, kèm kiểm thử chất lượng và cơ chế truy vết nguồn.'
    ),
    (
        'do.thanh.lam@aitasker.local', '0935101112', 'Đỗ Thanh Lam',
        '079204001112', 'do-thanh-lam', 10,
        '2,5,15,16,20', '1,8,9,11,16,17,18,23,24,26', '7,8,11,12,13,26,29',
        'Google Professional Machine Learning Engineer; ROS Industrial Training; OpenCV Certified AI Professional',
        'Kỹ sư Robotics và Computer Vision triển khai nhận diện lỗi, theo dõi vật thể và tối ưu mô hình cho dây chuyền sản xuất, từ PoC đến đóng gói container.'
    );

INSERT INTO account (
    email, password, phone, full_name, role_id, status, email_verified,
    failed_login_attempts, lockout_count, active_token_version, created_at, updated_at
)
SELECT
    seed.email,
    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC',
    seed.phone,
    seed.full_name,
    role.role_id,
    'Approved',
    TRUE,
    0,
    0,
    0,
    CURRENT_TIMESTAMP - (seed.years_experience * INTERVAL '9 days'),
    CURRENT_TIMESTAMP
FROM curated_expert_seed seed
CROSS JOIN (SELECT role_id FROM roles WHERE role_name = 'EXPERT') role
ON CONFLICT (email) DO UPDATE SET
    phone = EXCLUDED.phone,
    full_name = EXCLUDED.full_name,
    role_id = EXCLUDED.role_id,
    status = 'Approved',
    email_verified = TRUE,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO expert_profiles (
    account_id, national_id, portfolio_url, years_of_experience,
    kyc_status, approved_by, rejection_reason, created_at, updated_at
)
SELECT
    account.account_id,
    seed.national_id,
    'https://assets.example.test/experts/curated/' || seed.portfolio_slug || '/portfolio.pdf',
    seed.years_experience,
    'Approved',
    1,
    NULL,
    account.created_at,
    CURRENT_TIMESTAMP
FROM curated_expert_seed seed
JOIN account ON account.email = seed.email
ON CONFLICT (account_id) DO UPDATE SET
    national_id = EXCLUDED.national_id,
    portfolio_url = EXCLUDED.portfolio_url,
    years_of_experience = EXCLUDED.years_of_experience,
    kyc_status = 'Approved',
    approved_by = EXCLUDED.approved_by,
    rejection_reason = NULL,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO portfolios (
    expert_id, domain_ids, skill_ids, technology_ids, years_experience,
    certificates, self_description, created_at, updated_at
)
SELECT
    expert.expert_id,
    seed.domain_ids,
    seed.skill_ids,
    seed.technology_ids,
    seed.years_experience,
    seed.certificates,
    seed.self_description,
    expert.created_at,
    CURRENT_TIMESTAMP
FROM curated_expert_seed seed
JOIN account ON account.email = seed.email
JOIN expert_profiles expert ON expert.account_id = account.account_id
ON CONFLICT (expert_id) DO UPDATE SET
    domain_ids = EXCLUDED.domain_ids,
    skill_ids = EXCLUDED.skill_ids,
    technology_ids = EXCLUDED.technology_ids,
    years_experience = EXCLUDED.years_experience,
    certificates = EXCLUDED.certificates,
    self_description = EXCLUDED.self_description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_quotas (
    account_id, job_post_quota_balance, proposal_quota_balance,
    badge_expired_at, premium_expired_at, created_at, updated_at
)
SELECT account.account_id, 0, 5, NULL, NULL, account.created_at, CURRENT_TIMESTAMP
FROM curated_expert_seed seed
JOIN account ON account.email = seed.email
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT
    account.account_id, 'PROPOSAL', 'GRANT', 5, 0, 5,
    'CURATED_EXPERT_SEED', account.account_id, CURRENT_TIMESTAMP
FROM curated_expert_seed seed
JOIN account ON account.email = seed.email
WHERE NOT EXISTS (
    SELECT 1
    FROM quota_usage_logs existing
    WHERE existing.account_id = account.account_id
      AND existing.quota_type = 'PROPOSAL'
      AND existing.action_type = 'GRANT'
      AND existing.reference_type = 'CURATED_EXPERT_SEED'
);

INSERT INTO system_wallet (
    account_id, role_id, wallet_type, current_balance, available_balance,
    escrow_balance, holding_balance, disputed_balance, total_revenue,
    deposited_business_count, successful_deposit_count, currency,
    last_synced_at, created_at, updated_at
)
SELECT
    account.account_id, account.role_id, 'EXPERT', 0, 0, 0, 0, 0, 0,
    0, 0, 'VND', CURRENT_TIMESTAMP, account.created_at, CURRENT_TIMESTAMP
FROM curated_expert_seed seed
JOIN account ON account.email = seed.email
ON CONFLICT (account_id) DO NOTHING;

DO $$
DECLARE
    complete_seed_count INT;
    computer_vision_cloud_count INT;
BEGIN
    SELECT COUNT(*) INTO complete_seed_count
    FROM curated_expert_seed seed
    JOIN account ON account.email = seed.email
    JOIN expert_profiles expert ON expert.account_id = account.account_id
    JOIN portfolios portfolio ON portfolio.expert_id = expert.expert_id
    JOIN user_quotas quota ON quota.account_id = account.account_id
    JOIN system_wallet wallet ON wallet.account_id = account.account_id
    WHERE account.status = 'Approved'
      AND expert.kyc_status = 'Approved'
      AND quota.proposal_quota_balance >= 5
      AND wallet.wallet_type = 'EXPERT';

    IF complete_seed_count <> 12 THEN
        RAISE EXCEPTION 'Curated recommendation Expert seed incomplete: expected 12, found %',
            complete_seed_count;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM curated_expert_seed
        WHERE full_name ~ '[0-9]'
           OR lower(full_name) ~ '(expert|chuyên gia|demo)'
    ) THEN
        RAISE EXCEPTION 'Curated Expert names must be natural person names';
    END IF;

    SELECT COUNT(*) INTO computer_vision_cloud_count
    FROM curated_expert_seed seed
    WHERE EXISTS (
        SELECT 1
        FROM regexp_split_to_table(seed.skill_ids, '[^0-9]+') token
        WHERE token = '11'
    )
      AND EXISTS (
        SELECT 1
        FROM regexp_split_to_table(seed.skill_ids, '[^0-9]+') token
        WHERE token = '23'
    )
      AND EXISTS (
        SELECT 1
        FROM regexp_split_to_table(seed.domain_ids, '[^0-9]+') token
        WHERE token = '5'
    )
      AND EXISTS (
        SELECT 1
        FROM regexp_split_to_table(seed.technology_ids, '[^0-9]+') token
        WHERE token = '11'
    );

    IF computer_vision_cloud_count < 3 THEN
        RAISE EXCEPTION 'Need at least 3 Computer Vision + Cloud Architecture + GCP Experts';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM domains catalog
        WHERE catalog.is_active = TRUE
          AND catalog.domain_code <> 'PROFILE_REVIEW'
          AND NOT EXISTS (
              SELECT 1
              FROM portfolios portfolio,
                   LATERAL regexp_split_to_table(portfolio.domain_ids, '[^0-9]+') token
              WHERE token ~ '^[0-9]{1,9}$'
                AND token::INT = catalog.domain_id
          )
    ) OR EXISTS (
        SELECT 1
        FROM skills catalog
        WHERE catalog.is_active = TRUE
          AND NOT EXISTS (
              SELECT 1
              FROM portfolios portfolio,
                   LATERAL regexp_split_to_table(portfolio.skill_ids, '[^0-9]+') token
              WHERE token ~ '^[0-9]{1,9}$'
                AND token::INT = catalog.skill_id
          )
    ) OR EXISTS (
        SELECT 1
        FROM technologies catalog
        WHERE catalog.is_active = TRUE
          AND NOT EXISTS (
              SELECT 1
              FROM portfolios portfolio,
                   LATERAL regexp_split_to_table(portfolio.technology_ids, '[^0-9]+') token
              WHERE token ~ '^[0-9]{1,9}$'
                AND token::INT = catalog.technology_id
          )
    ) THEN
        RAISE EXCEPTION 'Curated Expert portfolios do not cover the active public catalog';
    END IF;
END $$;
