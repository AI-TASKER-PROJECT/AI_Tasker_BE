-- Seed multi-domain Expert demo profiles for AI recommendation matching.
-- Password plaintext for all seeded accounts: 12345678

DELETE FROM quota_usage_logs
WHERE account_id BETWEEN 9101 AND 9130;

DELETE FROM user_quotas
WHERE account_id BETWEEN 9101 AND 9130;

DELETE FROM system_wallet
WHERE account_id BETWEEN 9101 AND 9130
   OR system_wallet_id BETWEEN 9401 AND 9430;

DELETE FROM expert_recommendations
WHERE expert_id BETWEEN 9201 AND 9230;

DELETE FROM proposals
WHERE expert_id BETWEEN 9201 AND 9230;

DELETE FROM portfolios
WHERE portfolio_id BETWEEN 9301 AND 9330
   OR expert_id BETWEEN 9201 AND 9230;

DELETE FROM expert_profiles
WHERE expert_id BETWEEN 9201 AND 9230
   OR account_id BETWEEN 9101 AND 9130;

DELETE FROM account
WHERE account_id BETWEEN 9101 AND 9130
   OR email LIKE 'expert.multidomain%@aitasker.local';

WITH seed AS (
    SELECT *
    FROM (VALUES
        (1, 'api-testing', 'API Testing Expert 01', '0913000001'),
        (2, 'api-testing', 'API Testing Expert 02', '0913000002'),
        (3, 'api-testing', 'API Testing Expert 03', '0913000003'),
        (4, 'api-testing', 'API Testing Expert 04', '0913000004'),
        (5, 'api-testing', 'API Testing Expert 05', '0913000005'),
        (6, 'bi-dashboard', 'BI Dashboard Expert 01', '0913000006'),
        (7, 'bi-dashboard', 'BI Dashboard Expert 02', '0913000007'),
        (8, 'bi-dashboard', 'BI Dashboard Expert 03', '0913000008'),
        (9, 'bi-dashboard', 'BI Dashboard Expert 04', '0913000009'),
        (10, 'bi-dashboard', 'BI Dashboard Expert 05', '0913000010'),
        (11, 'computer-vision', 'Computer Vision Expert 01', '0913000011'),
        (12, 'computer-vision', 'Computer Vision Expert 02', '0913000012'),
        (13, 'computer-vision', 'Computer Vision Expert 03', '0913000013'),
        (14, 'computer-vision', 'Computer Vision Expert 04', '0913000014'),
        (15, 'computer-vision', 'Computer Vision Expert 05', '0913000015'),
        (16, 'data-pipeline', 'Data Pipeline Expert 01', '0913000016'),
        (17, 'data-pipeline', 'Data Pipeline Expert 02', '0913000017'),
        (18, 'data-pipeline', 'Data Pipeline Expert 03', '0913000018'),
        (19, 'data-pipeline', 'Data Pipeline Expert 04', '0913000019'),
        (20, 'data-pipeline', 'Data Pipeline Expert 05', '0913000020'),
        (21, 'generic-ai', 'Generic AI Expert 01', '0913000021'),
        (22, 'generic-ai', 'Generic AI Expert 02', '0913000022'),
        (23, 'generic-ai', 'Generic AI Expert 03', '0913000023'),
        (24, 'generic-ai', 'Generic AI Expert 04', '0913000024'),
        (25, 'generic-ai', 'Generic AI Expert 05', '0913000025'),
        (26, 'chatbot-rag', 'Chatbot RAG Expert 01', '0913000026'),
        (27, 'chatbot-rag', 'Chatbot RAG Expert 02', '0913000027'),
        (28, 'chatbot-rag', 'Chatbot RAG Expert 03', '0913000028'),
        (29, 'chatbot-rag', 'Chatbot RAG Expert 04', '0913000029'),
        (30, 'chatbot-rag', 'Chatbot RAG Expert 05', '0913000030')
    ) AS v(n, category, full_name, phone)
)
INSERT INTO account (account_id, email, password, phone, full_name, role_id, status, email_verified, created_at, updated_at)
SELECT
    9100 + n,
    'expert.multidomain' || lpad(n::text, 2, '0') || '@aitasker.local',
    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC',
    phone,
    full_name,
    2,
    'Approved',
    TRUE,
    CURRENT_TIMESTAMP - (n || ' hours')::INTERVAL,
    CURRENT_TIMESTAMP
FROM seed
ON CONFLICT (account_id) DO NOTHING;

WITH seed AS (
    SELECT n
    FROM generate_series(1, 30) AS n
)
INSERT INTO expert_profiles (
    expert_id, account_id, national_id, portfolio_url, years_of_experience,
    kyc_status, approved_by, created_at, updated_at
)
SELECT
    9200 + n,
    9100 + n,
    '079306' || lpad(n::text, 6, '0'),
    'https://portfolio.aitasker.local/multidomain/experts/' || n,
    3 + (n % 7),
    'Approved',
    1,
    CURRENT_TIMESTAMP - (n || ' hours')::INTERVAL,
    CURRENT_TIMESTAMP
FROM seed
ON CONFLICT (expert_id) DO NOTHING;

WITH seed AS (
    SELECT *
    FROM (VALUES
        (1, 'api-testing'), (2, 'api-testing'), (3, 'api-testing'), (4, 'api-testing'), (5, 'api-testing'),
        (6, 'bi-dashboard'), (7, 'bi-dashboard'), (8, 'bi-dashboard'), (9, 'bi-dashboard'), (10, 'bi-dashboard'),
        (11, 'computer-vision'), (12, 'computer-vision'), (13, 'computer-vision'), (14, 'computer-vision'), (15, 'computer-vision'),
        (16, 'data-pipeline'), (17, 'data-pipeline'), (18, 'data-pipeline'), (19, 'data-pipeline'), (20, 'data-pipeline'),
        (21, 'generic-ai'), (22, 'generic-ai'), (23, 'generic-ai'), (24, 'generic-ai'), (25, 'generic-ai'),
        (26, 'chatbot-rag'), (27, 'chatbot-rag'), (28, 'chatbot-rag'), (29, 'chatbot-rag'), (30, 'chatbot-rag')
    ) AS v(n, category)
),
portfolio_data AS (
    SELECT
        n,
        category,
        CASE category
            WHEN 'api-testing' THEN '9,10,15,Cloud Backend Systems,Web Platform Engineering,Cybersecurity,API testing,Swagger,Postman,QA automation'
            WHEN 'bi-dashboard' THEN '5,6,9,Data Engineering,Data Analytics & BI,Cloud Backend Systems,BI dashboard,analytics,KPI reporting'
            WHEN 'computer-vision' THEN '4,7,8,Computer Vision,Machine Learning,MLOps,object detection,image classification,OCR,quality control'
            WHEN 'data-pipeline' THEN '5,6,9,Data Engineering,Data Analytics & BI,Cloud Backend Systems,ETL,ELT,data warehouse,data quality'
            WHEN 'generic-ai' THEN '1,2,3,7,18,AI Product Strategy,Generative AI Applications,Natural Language Processing,Machine Learning,Business Process Automation,text classification,information extraction'
            ELSE '2,3,17,18,Generative AI Applications,Natural Language Processing,E-commerce & Retail Tech,Business Process Automation,Chatbot,RAG,Customer Support,Order Management'
        END AS domain_ids,
        CASE category
            WHEN 'api-testing' THEN '4,5,6,8,9,18,19,Model Evaluation,Python Engineering,Java Spring Boot,PostgreSQL,Docker & DevOps,Secure Auth & JWT,API Testing & Swagger,Swagger,Postman,JWT,RBAC,CI CD,Regression Testing'
            WHEN 'bi-dashboard' THEN '5,8,12,13,17,Python Engineering,PostgreSQL,Data Pipeline Design,BI Dashboarding,Design Systems,SQL,Data Modeling,Data Visualization,Export Report'
            WHEN 'computer-vision' THEN '4,5,9,14,15,Model Evaluation,Python Engineering,Docker & DevOps,OCR Pipeline,Computer Vision Modeling,Object Detection,Image Classification,Dataset Annotation,Inference API'
            WHEN 'data-pipeline' THEN '5,8,9,12,13,Python Engineering,PostgreSQL,Docker & DevOps,Data Pipeline Design,BI Dashboarding,ETL,Data Warehouse,Data Quality,Monitoring,Scheduling'
            WHEN 'generic-ai' THEN '1,3,4,5,9,18,Prompt Engineering,LLM Tool Calling,Model Evaluation,Python Engineering,Docker & DevOps,Secure Auth & JWT,AI Assistant,Text Classification,Information Extraction,API Integration'
            ELSE '1,2,3,4,5,9,Prompt Engineering,RAG Architecture,LLM Tool Calling,Model Evaluation,Python Engineering,Docker & DevOps,AI Chatbot,RAG / Knowledge Base,NLP,API Integration,Conversation Design,Testing'
        END AS skill_ids,
        CASE category
            WHEN 'api-testing' THEN '1,2,3,4,7,8,Python,Java Spring Boot,React TypeScript,PostgreSQL,Docker,OpenAI API'
            WHEN 'bi-dashboard' THEN '1,3,4,7,12,Python,React TypeScript,PostgreSQL,Docker,Apache Kafka'
            WHEN 'computer-vision' THEN '1,4,7,8,10,Python,PostgreSQL,Docker,OpenAI API,OCR'
            WHEN 'data-pipeline' THEN '1,4,7,12,Python,PostgreSQL,Docker,Apache Kafka'
            WHEN 'generic-ai' THEN '1,2,4,7,8,11,Python,Java Spring Boot,PostgreSQL,Docker,OpenAI API,LangChain'
            ELSE '1,2,4,7,8,9,11,Python,Java Spring Boot,PostgreSQL,Docker,OpenAI API,RAG,LangChain'
        END AS technology_ids,
        CASE category
            WHEN 'api-testing' THEN 'Certified API QA automation expert with Swagger, Postman, JWT, RBAC, CI/CD, regression testing, validation testing, and backend release quality experience.'
            WHEN 'bi-dashboard' THEN 'Certified BI dashboard expert with KPI modeling, SQL analytics, dashboard UX, PostgreSQL reporting, CSV/PDF export, role-based metrics, and operational analytics experience.'
            WHEN 'computer-vision' THEN 'Certified computer vision expert with dataset annotation, object detection, image classification, OCR, model evaluation, inference API, Docker deployment, and monitoring experience.'
            WHEN 'data-pipeline' THEN 'Certified data engineering expert with ETL, ELT, warehouse modeling, PostgreSQL sync, incremental load, data quality checks, scheduler monitoring, and runbook experience.'
            WHEN 'generic-ai' THEN 'Certified AI automation expert with text classification, information extraction, structured JSON output, prompt engineering, API integration, evaluation, and business workflow automation experience.'
            ELSE 'Certified chatbot and RAG expert with knowledge-base design, LLM tool calling, customer support automation, order lookup integration, conversation testing, and deployment experience.'
        END AS certificates,
        CASE category
            WHEN 'api-testing' THEN 'Expert has delivered API testing suites for marketplace backends using Swagger OpenAPI, Postman collections, automated regression scripts, JWT role testing, CI/CD reports, validation-error coverage, and release smoke checks.'
            WHEN 'bi-dashboard' THEN 'Expert has built BI dashboards for marketplace operations, including revenue KPIs, job and contract status metrics, wallet transactions, staff performance, PostgreSQL models, exports, filters, and role-based dashboard access.'
            WHEN 'computer-vision' THEN 'Expert has delivered computer vision systems for quality control, including image dataset preparation, annotation workflow, object detection, classification, precision recall F1 reporting, inference APIs, and deployment documentation.'
            WHEN 'data-pipeline' THEN 'Expert has implemented data pipelines for analytics, including source-to-target mapping, PostgreSQL extraction, CSV/API ingestion, incremental sync, data quality validation, alerting, monitoring, and recovery runbooks.'
            WHEN 'generic-ai' THEN 'Expert has built AI assistants that classify user requests, extract entities, generate structured JSON suggestions, integrate with REST APIs, log model output, evaluate accuracy, and document model limitations.'
            ELSE 'Expert has built customer support chatbots using RAG knowledge bases, FAQ and policy retrieval, order API lookup, Vietnamese and English responses, confidence fallback, human handoff, test reports, and production handover guides.'
        END AS self_description
    FROM seed
)
INSERT INTO portfolios (
    portfolio_id, expert_id, domain_ids, skill_ids, technology_ids, years_experience,
    certificates, self_description, created_at, updated_at
)
SELECT
    9300 + n,
    9200 + n,
    domain_ids,
    skill_ids,
    technology_ids,
    3 + (n % 7),
    certificates,
    self_description,
    CURRENT_TIMESTAMP - (n || ' hours')::INTERVAL,
    CURRENT_TIMESTAMP
FROM portfolio_data
ON CONFLICT (portfolio_id) DO NOTHING;

WITH seeded_accounts AS (
    SELECT 9100 + n AS account_id
    FROM generate_series(1, 30) AS n
)
INSERT INTO user_quotas (
    account_id, job_post_quota_balance, proposal_quota_balance, premium_expired_at,
    created_at, updated_at
)
SELECT
    account_id,
    0,
    10,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM seeded_accounts
ON CONFLICT (account_id) DO NOTHING;

WITH seeded_accounts AS (
    SELECT 9100 + n AS account_id, n AS sort_id
    FROM generate_series(1, 30) AS n
)
INSERT INTO system_wallet (
    system_wallet_id, account_id, role_id, wallet_type, current_balance, available_balance,
    escrow_balance, total_revenue, holding_balance, disputed_balance, currency,
    last_synced_at, created_at, updated_at
)
SELECT
    9400 + sort_id,
    account_id,
    2,
    'EXPERT',
    0,
    0,
    0,
    0,
    0,
    0,
    'VND',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM seeded_accounts
ON CONFLICT (account_id) DO NOTHING;

WITH seeded_accounts AS (
    SELECT 9100 + n AS account_id
    FROM generate_series(1, 30) AS n
)
INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT
    account_id,
    'PROPOSAL',
    'GRANT',
    10,
    0,
    10,
    'MULTI_DOMAIN_EXPERT_SEED',
    account_id,
    CURRENT_TIMESTAMP
FROM seeded_accounts
WHERE NOT EXISTS (
    SELECT 1
    FROM quota_usage_logs q
    WHERE q.account_id = seeded_accounts.account_id
      AND q.quota_type = 'PROPOSAL'
      AND q.action_type = 'GRANT'
      AND q.reference_type = 'MULTI_DOMAIN_EXPERT_SEED'
);

DO $$
DECLARE
    seeded_accounts INT;
    seeded_profiles INT;
    seeded_portfolios INT;
BEGIN
    SELECT COUNT(*) INTO seeded_accounts
    FROM account
    WHERE account_id BETWEEN 9101 AND 9130
      AND role_id = 2
      AND status = 'Approved';

    SELECT COUNT(*) INTO seeded_profiles
    FROM expert_profiles
    WHERE expert_id BETWEEN 9201 AND 9230
      AND kyc_status = 'Approved';

    SELECT COUNT(*) INTO seeded_portfolios
    FROM portfolios
    WHERE portfolio_id BETWEEN 9301 AND 9330
      AND (
          skill_ids ILIKE '%API Testing%'
          OR skill_ids ILIKE '%BI Dashboard%'
          OR skill_ids ILIKE '%Computer Vision%'
          OR skill_ids ILIKE '%Data Pipeline%'
          OR skill_ids ILIKE '%Text Classification%'
          OR skill_ids ILIKE '%RAG%'
      );

    IF seeded_accounts <> 30 OR seeded_profiles <> 30 OR seeded_portfolios <> 30 THEN
        RAISE EXCEPTION 'Multi-domain recommendation seed incomplete. accounts=%, profiles=%, portfolios=%',
            seeded_accounts, seeded_profiles, seeded_portfolios;
    END IF;
END $$;

SELECT setval(pg_get_serial_sequence('account', 'account_id'), COALESCE((SELECT MAX(account_id) FROM account), 1), TRUE);
SELECT setval(pg_get_serial_sequence('expert_profiles', 'expert_id'), COALESCE((SELECT MAX(expert_id) FROM expert_profiles), 1), TRUE);
SELECT setval(pg_get_serial_sequence('portfolios', 'portfolio_id'), COALESCE((SELECT MAX(portfolio_id) FROM portfolios), 1), TRUE);
SELECT setval(pg_get_serial_sequence('user_quotas', 'quota_id'), COALESCE((SELECT MAX(quota_id) FROM user_quotas), 1), TRUE);
SELECT setval(pg_get_serial_sequence('quota_usage_logs', 'quota_usage_id'), COALESCE((SELECT MAX(quota_usage_id) FROM quota_usage_logs), 1), TRUE);
SELECT setval(pg_get_serial_sequence('system_wallet', 'system_wallet_id'), COALESCE((SELECT MAX(system_wallet_id) FROM system_wallet), 1), TRUE);
