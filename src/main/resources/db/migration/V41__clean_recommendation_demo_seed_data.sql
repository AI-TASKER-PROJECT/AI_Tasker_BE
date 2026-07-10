-- Clean and re-seed recommendation demo data with whitespace-free identifiers.
-- PostgreSQL format('%02s', n) pads strings with spaces, not zeroes, so V39 can
-- create emails such as expert.recommend 1@aitasker.local. Keep V39 immutable
-- and repair existing databases through this additive migration.

DELETE FROM expert_recommendations
WHERE job_posting_id BETWEEN 8601 AND 8610
   OR expert_id BETWEEN 8201 AND 8220;

DELETE FROM proposals
WHERE proposal_id BETWEEN 8801 AND 8820
   OR job_id BETWEEN 8601 AND 8610
   OR expert_id BETWEEN 8201 AND 8220;

DELETE FROM milestones
WHERE milestone_id BETWEEN 8701 AND 8730
   OR job_id BETWEEN 8601 AND 8610;

DELETE FROM job_technologies WHERE job_id BETWEEN 8601 AND 8610;
DELETE FROM job_skills WHERE job_id BETWEEN 8601 AND 8610;
DELETE FROM job_domains WHERE job_id BETWEEN 8601 AND 8610;
DELETE FROM sow WHERE job_id BETWEEN 8601 AND 8610;
DELETE FROM jobs WHERE job_id BETWEEN 8601 AND 8610;

DELETE FROM quota_usage_logs
WHERE account_id BETWEEN 8101 AND 8120
   OR account_id BETWEEN 8401 AND 8410;

DELETE FROM user_quotas
WHERE account_id BETWEEN 8101 AND 8120
   OR account_id BETWEEN 8401 AND 8410;

DELETE FROM system_wallet
WHERE account_id BETWEEN 8101 AND 8120
   OR account_id BETWEEN 8401 AND 8410
   OR system_wallet_id BETWEEN 8901 AND 8930;

DELETE FROM portfolios WHERE portfolio_id BETWEEN 8301 AND 8320 OR expert_id BETWEEN 8201 AND 8220;
DELETE FROM expert_profiles WHERE expert_id BETWEEN 8201 AND 8220 OR account_id BETWEEN 8101 AND 8120;
DELETE FROM business_profiles WHERE business_id BETWEEN 8501 AND 8510 OR account_id BETWEEN 8401 AND 8410;

DELETE FROM account
WHERE account_id BETWEEN 8101 AND 8120
   OR account_id BETWEEN 8401 AND 8410
   OR email LIKE 'expert.recommend%@aitasker.local'
   OR email LIKE 'business.recommend%@aitasker.local';

WITH expert_seed AS (
    SELECT n, lpad(n::text, 2, '0') AS n2, lpad(n::text, 6, '0') AS n6
    FROM generate_series(1, 20) AS n
)
INSERT INTO account (account_id, email, password, phone, full_name, role_id, status, email_verified, created_at, updated_at)
SELECT
    8100 + n,
    'expert.recommend' || n2 || '@aitasker.local',
    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC',
    '0912' || n6,
    'Recommendation Expert ' || n2,
    2,
    'Approved',
    TRUE,
    CURRENT_TIMESTAMP - (n || ' days')::INTERVAL,
    CURRENT_TIMESTAMP
FROM expert_seed
ON CONFLICT (account_id) DO NOTHING;

WITH business_seed AS (
    SELECT n, lpad(n::text, 2, '0') AS n2, lpad(n::text, 6, '0') AS n6
    FROM generate_series(1, 10) AS n
)
INSERT INTO account (account_id, email, password, phone, full_name, role_id, status, email_verified, created_at, updated_at)
SELECT
    8400 + n,
    'business.recommend' || n2 || '@aitasker.local',
    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC',
    '0922' || n6,
    'Recommendation Business ' || n2,
    1,
    'Approved',
    TRUE,
    CURRENT_TIMESTAMP - (n || ' days')::INTERVAL,
    CURRENT_TIMESTAMP
FROM business_seed
ON CONFLICT (account_id) DO NOTHING;

WITH expert_seed AS (
    SELECT n, lpad(n::text, 6, '0') AS n6
    FROM generate_series(1, 20) AS n
)
INSERT INTO expert_profiles (
    expert_id, account_id, national_id, portfolio_url, years_of_experience,
    kyc_status, approved_by, created_at, updated_at
)
SELECT
    8200 + n,
    8100 + n,
    '079206' || n6,
    'https://portfolio.aitasker.local/recommendation/experts/' || n,
    3 + (n % 8),
    'Approved',
    1,
    CURRENT_TIMESTAMP - (n || ' days')::INTERVAL,
    CURRENT_TIMESTAMP
FROM expert_seed
ON CONFLICT (expert_id) DO NOTHING;

WITH business_seed AS (
    SELECT n, lpad(n::text, 4, '0') AS n4, lpad(n::text, 2, '0') AS n2
    FROM generate_series(1, 10) AS n
)
INSERT INTO business_profiles (
    business_id, account_id, tax_code, company_name, address, business_license_url,
    kyb_status, approved_by, created_at, updated_at
)
SELECT
    8500 + n,
    8400 + n,
    '039900' || n4,
    'Recommendation Company ' || n2,
    'District ' || ((n % 12) + 1) || ', Ho Chi Minh City',
    'https://storage.aitasker.local/licenses/recommend-company-' || n || '.pdf',
    'Approved',
    1,
    CURRENT_TIMESTAMP - (n || ' days')::INTERVAL,
    CURRENT_TIMESTAMP
FROM business_seed
ON CONFLICT (business_id) DO NOTHING;

WITH expert_seed AS (
    SELECT n
    FROM generate_series(1, 20) AS n
)
INSERT INTO portfolios (
    portfolio_id, expert_id, domain_ids, skill_ids, technology_ids, years_experience,
    certificates, self_description, created_at, updated_at
)
SELECT
    8300 + n,
    8200 + n,
    CASE
        WHEN n % 4 = 1 THEN '2,3,17,Customer Support,E-commerce,CRM,Order Management'
        WHEN n % 4 = 2 THEN '2,3,18,Customer Support,E-commerce,CRM'
        WHEN n % 4 = 3 THEN '2,3,9,E-commerce,CRM,Order Management'
        ELSE '2,7,17,Customer Support,E-commerce,Order Management'
    END,
    CASE
        WHEN n % 4 = 1 THEN '1,2,3,5,6,AI,Chatbot,RAG / Knowledge Base,API Integration,Spring Boot,NLP'
        WHEN n % 4 = 2 THEN '2,3,4,5,8,AI,RAG / Knowledge Base,NLP,Testing'
        WHEN n % 4 = 3 THEN '5,6,8,9,API Integration,Spring Boot,Deployment,Testing'
        ELSE '1,2,3,5,AI,Chatbot,RAG / Knowledge Base,NLP'
    END,
    CASE
        WHEN n % 4 = 1 THEN '1,2,4,8,9,11'
        WHEN n % 4 = 2 THEN '1,4,8,9,10'
        WHEN n % 4 = 3 THEN '1,2,3,4,7,12'
        ELSE '1,4,8,9,11'
    END,
    3 + (n % 8),
    'AI recommendation testing certificate set ' || n || '; RAG, NLP, API integration, deployment.',
    'Expert ' || n || ' has delivered AI chatbot, RAG knowledge base, e-commerce CRM, order management, API integration, testing, and deployment projects for recommendation matching.',
    CURRENT_TIMESTAMP - (n || ' days')::INTERVAL,
    CURRENT_TIMESTAMP
FROM expert_seed
ON CONFLICT (portfolio_id) DO NOTHING;

WITH business_seed AS (
    SELECT n, lpad(n::text, 2, '0') AS n2
    FROM generate_series(1, 10) AS n
)
INSERT INTO jobs (
    job_id, business_id, title, raw_requirements, structured_sow, budget, status,
    planned_duration_value, planned_duration_unit, is_hot, hot_until, published_at, created_at, updated_at
)
SELECT
    8600 + n,
    8500 + n,
    'Recommendation test AI chatbot and RAG project ' || n2,
    'Build an AI chatbot for e-commerce customer support, CRM handoff, order management, API integration, testing, and deployment.',
    jsonb_build_object(
        'sow', jsonb_build_object(
            'title', 'AI chatbot RAG customer support project ' || n,
            'overview', 'E-commerce business needs AI chatbot, RAG knowledge base, CRM integration, order management, testing, and deployment.',
            'scopeOfWork', 'Design RAG architecture, implement chatbot API, integrate CRM, test quality, and deploy monitoring.'
        )
    )::text,
    90000000 + (n * 5000000),
    'OPEN',
    8,
    'WEEK',
    n <= 3,
    CASE WHEN n <= 3 THEN CURRENT_TIMESTAMP + INTERVAL '10 days' ELSE NULL END,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - (n || ' days')::INTERVAL,
    CURRENT_TIMESTAMP
FROM business_seed
ON CONFLICT (job_id) DO NOTHING;

WITH job_seed AS (
    SELECT n, 8600 + n AS job_id, lpad(n::text, 2, '0') AS n2
    FROM generate_series(1, 10) AS n
)
INSERT INTO sow (job_id, title, overview, objectives, scope_of_work, deliverable, assumptions, out_of_scope, created_at, updated_at)
SELECT
    job_id,
    'Recommendation SOW ' || n2,
    'AI chatbot and RAG knowledge base for e-commerce customer support and CRM workflow.',
    '["Increase support automation","Improve product discovery","Route qualified leads to CRM"]',
    '["RAG architecture","Chatbot API","CRM integration","Testing","Deployment"]',
    '["Working chatbot demo","API documentation","Test report","Deployment guide"]',
    '["Business provides sample product catalog and FAQ"]',
    '["No mobile app build in seed scenario"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM job_seed
ON CONFLICT (job_id) DO NOTHING;

WITH job_seed AS (
    SELECT 8600 + n AS job_id
    FROM generate_series(1, 10) AS n
)
INSERT INTO job_domains (job_id, domain_id, created_at)
SELECT job_id, domain_id, CURRENT_TIMESTAMP
FROM job_seed
CROSS JOIN (VALUES (2), (3), (17)) AS d(domain_id)
ON CONFLICT (job_id, domain_id) DO NOTHING;

WITH job_seed AS (
    SELECT 8600 + n AS job_id
    FROM generate_series(1, 10) AS n
)
INSERT INTO job_skills (job_id, skill_id, is_mandatory, created_at)
SELECT job_id, skill_id, TRUE, CURRENT_TIMESTAMP
FROM job_seed
CROSS JOIN (VALUES (1), (2), (3), (5), (6), (19)) AS s(skill_id)
ON CONFLICT (job_id, skill_id) DO NOTHING;

WITH job_seed AS (
    SELECT 8600 + n AS job_id
    FROM generate_series(1, 10) AS n
)
INSERT INTO job_technologies (job_id, technology_id, created_at)
SELECT job_id, technology_id, CURRENT_TIMESTAMP
FROM job_seed
CROSS JOIN (VALUES (1), (2), (4), (8), (9), (11)) AS t(technology_id)
ON CONFLICT (job_id, technology_id) DO NOTHING;

WITH job_seed AS (
    SELECT n, 8600 + n AS job_id
    FROM generate_series(1, 10) AS n
)
INSERT INTO milestones (
    milestone_id, job_id, contract_id, milestone_name, description, funds_allocated,
    order_index, status, duration, duration_unit, created_at, updated_at
)
SELECT
    8700 + ((n - 1) * 3) + order_index,
    job_id,
    NULL,
    CASE order_index
        WHEN 1 THEN 'Discovery and RAG architecture'
        WHEN 2 THEN 'Chatbot API and CRM integration'
        ELSE 'Testing deployment and handover'
    END,
    CASE order_index
        WHEN 1 THEN 'Clarify e-commerce support requirements and design knowledge base architecture.'
        WHEN 2 THEN 'Build chatbot API, RAG retrieval, CRM handoff, and order management integration.'
        ELSE 'Run testing, prepare deployment guide, and hand over operating checklist.'
    END,
    CASE order_index
        WHEN 1 THEN 25000000
        WHEN 2 THEN 50000000
        ELSE 20000000 + (n * 1000000)
    END,
    order_index,
    CASE order_index
        WHEN 1 THEN 'PENDING'
        WHEN 2 THEN 'IN_PROGRESS'
        ELSE 'DEPOSITED'
    END,
    CASE order_index
        WHEN 1 THEN 2
        WHEN 2 THEN 3
        ELSE 2
    END,
    'WEEK',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM job_seed
CROSS JOIN generate_series(1, 3) AS order_index
ON CONFLICT (milestone_id) DO NOTHING;

WITH proposal_seed AS (
    SELECT
        n,
        ((n - 1) % 10) + 1 AS job_n
    FROM generate_series(1, 20) AS n
)
INSERT INTO proposals (
    proposal_id, job_id, expert_id, technical_solution, proposal_description,
    proposal_file_url, proposal_milestone, bid_amount, status, business_selected, created_at, updated_at
)
SELECT
    8800 + n,
    8600 + job_n,
    8200 + n,
    'Implement a guarded RAG chatbot with product catalog ingestion, CRM handoff API, order context lookup, evaluation tests, and deployment checklist.',
    'Seed proposal ' || n || ' matches the recommendation test job ' || job_n || ' with AI chatbot, RAG, API integration, testing, and deployment experience.',
    'https://storage.aitasker.local/proposals/recommendation/' || n || '.pdf',
    jsonb_build_array(
        jsonb_build_object('name', 'Discovery and RAG architecture', 'budget', 25000000, 'duration', 2, 'durationUnit', 'WEEK'),
        jsonb_build_object('name', 'Chatbot API and CRM integration', 'budget', 50000000, 'duration', 3, 'durationUnit', 'WEEK'),
        jsonb_build_object('name', 'Testing deployment and handover', 'budget', 20000000 + (job_n * 1000000), 'duration', 2, 'durationUnit', 'WEEK')
    )::text,
    95000000 + (job_n * 3000000),
    'Pending',
    FALSE,
    CURRENT_TIMESTAMP - (n || ' hours')::INTERVAL,
    CURRENT_TIMESTAMP
FROM proposal_seed
ON CONFLICT (proposal_id) DO NOTHING;

WITH seeded_accounts AS (
    SELECT 8100 + n AS account_id, 2 AS role_id
    FROM generate_series(1, 20) AS n
    UNION ALL
    SELECT 8400 + n AS account_id, 1 AS role_id
    FROM generate_series(1, 10) AS n
)
INSERT INTO user_quotas (
    account_id, job_post_quota_balance, proposal_quota_balance, premium_expired_at, created_at, updated_at
)
SELECT
    account_id,
    CASE WHEN role_id = 1 THEN 3 ELSE 0 END,
    CASE WHEN role_id = 2 THEN 3 ELSE 0 END,
    CASE WHEN role_id = 1 THEN CURRENT_TIMESTAMP + INTERVAL '90 days' ELSE NULL END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM seeded_accounts
ON CONFLICT (account_id) DO NOTHING;

WITH seeded_accounts AS (
    SELECT 8100 + n AS account_id, 2 AS role_id, 'EXPERT' AS wallet_type, n AS sort_id
    FROM generate_series(1, 20) AS n
    UNION ALL
    SELECT 8400 + n AS account_id, 1 AS role_id, 'BUSINESS' AS wallet_type, 20 + n AS sort_id
    FROM generate_series(1, 10) AS n
)
INSERT INTO system_wallet (
    system_wallet_id, account_id, role_id, wallet_type, current_balance, available_balance,
    escrow_balance, total_revenue, holding_balance, disputed_balance, currency,
    last_synced_at, created_at, updated_at
)
SELECT
    8900 + sort_id,
    account_id,
    role_id,
    wallet_type,
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
    SELECT 8100 + n AS account_id, 'PROPOSAL' AS quota_type, 'INITIAL_EXPERT_GRANT' AS reference_type
    FROM generate_series(1, 20) AS n
    UNION ALL
    SELECT 8400 + n AS account_id, 'JOB_POST' AS quota_type, 'INITIAL_BUSINESS_GRANT' AS reference_type
    FROM generate_series(1, 10) AS n
)
INSERT INTO quota_usage_logs (
    account_id, quota_type, action_type, amount, balance_before, balance_after,
    reference_type, reference_id, created_at
)
SELECT
    account_id,
    quota_type,
    'GRANT',
    3,
    0,
    3,
    reference_type,
    account_id,
    CURRENT_TIMESTAMP
FROM seeded_accounts
WHERE NOT EXISTS (
    SELECT 1
    FROM quota_usage_logs q
    WHERE q.account_id = seeded_accounts.account_id
      AND q.quota_type = seeded_accounts.quota_type
      AND q.action_type = 'GRANT'
      AND q.reference_type = seeded_accounts.reference_type
);

DO $$
DECLARE
    invalid_accounts INT;
    invalid_experts INT;
    invalid_businesses INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_accounts
    FROM account
    WHERE (account_id BETWEEN 8101 AND 8120 OR account_id BETWEEN 8401 AND 8410)
      AND (
          email <> btrim(email)
          OR email LIKE '% %'
          OR phone <> btrim(phone)
          OR phone LIKE '% %'
      );

    SELECT COUNT(*)
    INTO invalid_experts
    FROM expert_profiles
    WHERE expert_id BETWEEN 8201 AND 8220
      AND (
          national_id <> btrim(national_id)
          OR national_id LIKE '% %'
      );

    SELECT COUNT(*)
    INTO invalid_businesses
    FROM business_profiles
    WHERE business_id BETWEEN 8501 AND 8510
      AND (
          tax_code <> btrim(tax_code)
          OR tax_code LIKE '% %'
      );

    IF invalid_accounts > 0 OR invalid_experts > 0 OR invalid_businesses > 0 THEN
        RAISE EXCEPTION 'Recommendation demo seed still contains whitespace in account/profile identifiers. accounts=%, experts=%, businesses=%',
            invalid_accounts, invalid_experts, invalid_businesses;
    END IF;
END $$;

SELECT setval(pg_get_serial_sequence('account', 'account_id'), COALESCE((SELECT MAX(account_id) FROM account), 1), TRUE);
SELECT setval(pg_get_serial_sequence('business_profiles', 'business_id'), COALESCE((SELECT MAX(business_id) FROM business_profiles), 1), TRUE);
SELECT setval(pg_get_serial_sequence('expert_profiles', 'expert_id'), COALESCE((SELECT MAX(expert_id) FROM expert_profiles), 1), TRUE);
SELECT setval(pg_get_serial_sequence('portfolios', 'portfolio_id'), COALESCE((SELECT MAX(portfolio_id) FROM portfolios), 1), TRUE);
SELECT setval(pg_get_serial_sequence('jobs', 'job_id'), COALESCE((SELECT MAX(job_id) FROM jobs), 1), TRUE);
SELECT setval(pg_get_serial_sequence('sow', 'sow_id'), COALESCE((SELECT MAX(sow_id) FROM sow), 1), TRUE);
SELECT setval(pg_get_serial_sequence('milestones', 'milestone_id'), COALESCE((SELECT MAX(milestone_id) FROM milestones), 1), TRUE);
SELECT setval(pg_get_serial_sequence('proposals', 'proposal_id'), COALESCE((SELECT MAX(proposal_id) FROM proposals), 1), TRUE);
SELECT setval(pg_get_serial_sequence('user_quotas', 'quota_id'), COALESCE((SELECT MAX(quota_id) FROM user_quotas), 1), TRUE);
SELECT setval(pg_get_serial_sequence('quota_usage_logs', 'quota_usage_id'), COALESCE((SELECT MAX(quota_usage_id) FROM quota_usage_logs), 1), TRUE);
SELECT setval(pg_get_serial_sequence('system_wallet', 'system_wallet_id'), COALESCE((SELECT MAX(system_wallet_id) FROM system_wallet), 1), TRUE);
