-- Add a focused group of natural, domain-diverse Experts who all satisfy the
-- NLP + LLM application + RAG + API design + model evaluation skill bundle.
-- Saved recommendations are intentionally not seeded so Business flows still
-- exercise the real deterministic ranking and optional AI-reason pipeline.

CREATE TEMP TABLE nlp_rag_expert_seed (
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

INSERT INTO nlp_rag_expert_seed (
    email, phone, full_name, national_id, portfolio_slug, years_experience,
    domain_ids, skill_ids, technology_ids, certificates, self_description
) VALUES
    (
        'tran.minh.chau@aitasker.local', '0935201201', 'Trần Minh Châu',
        '079204001201', 'tran-minh-chau', 9,
        '3,4,13,14,27', '3,10,12,13,14,15,17,19,20,21,23,27,30',
        '3,4,5,7,11,15,16,17,18,19,27,28',
        'Google Cloud Generative AI Leader; LangChain for Production Applications; API Security Fundamentals',
        'Kiến trúc sư giải pháp GenAI chuyên xây dựng trợ lý RAG cho doanh nghiệp, kết nối nguồn dữ liệu nội bộ qua API và thiết lập bộ đánh giá câu trả lời có truy vết nguồn.'
    ),
    (
        'nguyen.hoang.phuc@aitasker.local', '0935201202', 'Nguyễn Hoàng Phúc',
        '079204001202', 'nguyen-hoang-phuc', 11,
        '3,4,25,28,29', '1,10,12,14,15,17,19,21,27,30',
        '5,7,9,15,16,17,18,19,27,30',
        'Document AI Professional; Privacy Engineering Foundations; ISTQB AI Testing',
        'Chuyên gia NLP và Document Intelligence triển khai hệ thống hỏi đáp hồ sơ pháp lý, chú trọng phân quyền dữ liệu, đánh giá độ trung thực và kiểm thử chống prompt injection.'
    ),
    (
        'lam.gia.bao@aitasker.local', '0935201203', 'Lâm Gia Bảo',
        '079204001203', 'lam-gia-bao', 7,
        '3,4,6,13,27', '2,10,12,13,14,15,17,19,20,21,23,30',
        '2,5,6,7,10,15,16,17,18,19,27',
        'Microsoft Azure AI Engineer Associate; Conversational AI Design; Secure Microservices Professional',
        'Kỹ sư hội thoại đa ngôn ngữ xây dựng chatbot chăm sóc khách hàng, dịch vụ RAG và API tích hợp CRM với bộ tiêu chí đánh giá chất lượng theo từng nhóm câu hỏi.'
    ),
    (
        'vu.thu.trang@aitasker.local', '0935201204', 'Vũ Thu Trang',
        '079204001204', 'vu-thu-trang', 6,
        '3,4,23,27,29', '3,10,12,13,14,15,17,19,21,28,30',
        '3,4,5,11,15,16,17,18,19,27,28,30',
        'Hugging Face NLP Course; Learning Analytics Certificate; Playwright Test Automation',
        'Kỹ sư NLP phát triển trợ giảng RAG và công cụ hỗ trợ học tập, kết hợp đánh giá tự động, kiểm thử giao diện và phản hồi của giảng viên để cải thiện chất lượng.'
    ),
    (
        'phan.khoi.nguyen@aitasker.local', '0935201205', 'Phan Khôi Nguyên',
        '079204001205', 'phan-khoi-nguyen', 8,
        '3,8,9,10,13', '1,4,5,6,10,12,14,15,17,19,22,23,24,30',
        '5,7,8,11,16,17,18,19,20,21,22,23,27',
        'Google Professional Data Engineer; dbt Analytics Engineering; Retrieval Systems Evaluation',
        'Kỹ sư dữ liệu xây dựng nền tảng RAG trên kho dữ liệu doanh nghiệp, API truy vấn có kiểm soát và dashboard theo dõi độ chính xác, độ trễ cùng mức sử dụng nguồn.'
    ),
    (
        'dinh.ngoc.mai@aitasker.local', '0935201206', 'Đinh Ngọc Mai',
        '079204001206', 'dinh-ngoc-mai', 10,
        '3,4,17,28,29', '1,10,12,14,15,17,19,21,27,29,30',
        '5,7,11,15,16,17,18,19,27,30',
        'Healthcare AI Foundations; Google Professional Cloud Architect; Responsible Generative AI',
        'Chuyên gia AI y tế thiết kế trợ lý tra cứu tri thức lâm sàng, API tích hợp và quy trình đánh giá có chuyên gia kiểm duyệt, ưu tiên bảo mật và giải thích nguồn thông tin.'
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
    CURRENT_TIMESTAMP - (seed.years_experience * INTERVAL '8 days'),
    CURRENT_TIMESTAMP
FROM nlp_rag_expert_seed seed
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
    'https://assets.example.test/experts/nlp-rag/' || seed.portfolio_slug || '/portfolio.pdf',
    seed.years_experience,
    'Approved',
    1,
    NULL,
    account.created_at,
    CURRENT_TIMESTAMP
FROM nlp_rag_expert_seed seed
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
FROM nlp_rag_expert_seed seed
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
FROM nlp_rag_expert_seed seed
JOIN account ON account.email = seed.email
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT
    account.account_id, 'PROPOSAL', 'GRANT', 5, 0, 5,
    'NLP_RAG_EXPERT_SEED', account.account_id, CURRENT_TIMESTAMP
FROM nlp_rag_expert_seed seed
JOIN account ON account.email = seed.email
WHERE NOT EXISTS (
    SELECT 1
    FROM quota_usage_logs existing
    WHERE existing.account_id = account.account_id
      AND existing.quota_type = 'PROPOSAL'
      AND existing.action_type = 'GRANT'
      AND existing.reference_type = 'NLP_RAG_EXPERT_SEED'
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
FROM nlp_rag_expert_seed seed
JOIN account ON account.email = seed.email
ON CONFLICT (account_id) DO NOTHING;

DO $$
DECLARE
    complete_seed_count INT;
    complete_skill_bundle_count INT;
BEGIN
    SELECT COUNT(*) INTO complete_seed_count
    FROM nlp_rag_expert_seed seed
    JOIN account ON account.email = seed.email
    JOIN expert_profiles expert ON expert.account_id = account.account_id
    JOIN portfolios portfolio ON portfolio.expert_id = expert.expert_id
    JOIN user_quotas quota ON quota.account_id = account.account_id
    JOIN system_wallet wallet ON wallet.account_id = account.account_id
    WHERE account.status = 'Approved'
      AND expert.kyc_status = 'Approved'
      AND quota.proposal_quota_balance >= 5
      AND wallet.wallet_type = 'EXPERT'
      AND NULLIF(TRIM(portfolio.certificates), '') IS NOT NULL
      AND NULLIF(TRIM(portfolio.self_description), '') IS NOT NULL;

    IF complete_seed_count <> 6 THEN
        RAISE EXCEPTION 'NLP/RAG recommendation Expert seed incomplete: expected 6, found %',
            complete_seed_count;
    END IF;

    SELECT COUNT(*) INTO complete_skill_bundle_count
    FROM nlp_rag_expert_seed seed
    WHERE NOT EXISTS (
        SELECT required.skill_id
        FROM (VALUES (10), (12), (14), (17), (19)) required(skill_id)
        WHERE NOT EXISTS (
            SELECT 1
            FROM regexp_split_to_table(seed.skill_ids, '[^0-9]+') token
            WHERE token ~ '^[0-9]{1,9}$'
              AND token::INT = required.skill_id
        )
    );

    IF complete_skill_bundle_count <> 6 THEN
        RAISE EXCEPTION 'Every NLP/RAG Expert must contain skills 10, 12, 14, 17 and 19';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM nlp_rag_expert_seed
        WHERE full_name ~ '[0-9]'
           OR lower(full_name) ~ '(expert|chuyên gia|demo)'
    ) THEN
        RAISE EXCEPTION 'NLP/RAG Expert names must be natural person names';
    END IF;
END $$;
