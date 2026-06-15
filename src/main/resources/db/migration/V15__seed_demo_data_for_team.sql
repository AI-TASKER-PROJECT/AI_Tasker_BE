-- DEV/TEAM DEMO SEED.
-- This migration normalizes local/demo databases to the shared AITASKER dataset.
-- It intentionally keeps only the 4 .local demo accounts and related data.
-- Password plaintext for all accounts: 12345678
TRUNCATE TABLE
    acceptance_criteria,
    system_wallet,
    transactions,
    deliverables,
    disputes,
    contract_change_requests,
    reviews,
    milestones,
    contracts,
    proposals,
    job_domains,
    job_skills,
    jobs,
    portfolios,
    business_profiles,
    expert_profiles,
    staffs,
    audit_logs,
    system_settings,
    domains,
    skills,
    account,
    roles
RESTART IDENTITY CASCADE;

INSERT INTO roles (role_id, role_name) VALUES
    (1, 'BUSINESS'),
    (2, 'EXPERT'),
    (3, 'ADMIN'),
    (4, 'STAFF');

-- Password plaintext for all accounts: 12345678
INSERT INTO account (account_id, email, password, phone, full_name, role_id, status, created_at, updated_at) VALUES
    (1, 'business@aitasker.local', '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0901000001', 'Nova Retail', 1, 'Approved', NOW() - INTERVAL '14 days', NOW() - INTERVAL '1 day'),
    (2, 'expert@aitasker.local', '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0901000002', 'Tran Hoang Nam', 2, 'Approved', NOW() - INTERVAL '13 days', NOW() - INTERVAL '1 day'),
    (3, 'admin@aitasker.local', '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0901000003', 'Le Thu Quan Tri', 3, 'Approved', NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 day'),
    (4, 'staff@aitasker.local', '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC', '0901000004', 'Pham Quoc Huy', 4, 'Approved', NOW() - INTERVAL '18 days', NOW() - INTERVAL '1 day');

INSERT INTO staffs (staff_id, account_id, specialization, created_at, updated_at) VALUES
    (1, 4, 'KYB/KYC profile verification, AI project dispute review, and milestone evidence testing', NOW() - INTERVAL '18 days', NOW() - INTERVAL '1 day');

INSERT INTO business_profiles (business_id, account_id, tax_code, company_name, address, business_license_url, kyb_status, approved_by, created_at, updated_at) VALUES
    (1, 1, '0312345678', 'Nova Retail', 'Quan 1, TP. Ho Chi Minh', 'https://storage.aitasker.local/licenses/nova-retail.pdf', 'Approved', 1, NOW() - INTERVAL '14 days', NOW() - INTERVAL '10 days');

INSERT INTO expert_profiles (expert_id, account_id, national_id, portfolio_url, years_of_experience, kyc_status, approved_by, created_at, updated_at) VALUES
    (1, 2, '079203001234', 'https://portfolio.aitasker.local/tran-hoang-nam', 5, 'Approved', 1, NOW() - INTERVAL '13 days', NOW() - INTERVAL '10 days');

INSERT INTO portfolios (portfolio_id, expert_id, context, data_processing, model_architecture, performance_metrics, poc_url, created_at, updated_at) VALUES
    (1, 1,
     'Built production AI assistants for retail search, product recommendation, and customer support automation.',
     'Designed ETL pipelines for Vietnamese product catalogs, deduplication, labeling workflow, and vector indexing.',
     'Hybrid RAG architecture using PostgreSQL metadata, vector retrieval, reranking, and guarded LLM generation.',
     'Reduced manual support workload by 42%, achieved 91% answer acceptance in pilot, p95 latency under 2.4 seconds.',
     'https://portfolio.aitasker.local/tran-hoang-nam/rag-retail-demo',
     NOW() - INTERVAL '12 days', NOW() - INTERVAL '2 days');

INSERT INTO domains (domain_id, domain_code, domain_name, description, is_active, sort_order, created_at, updated_at) VALUES
    (1, 'AI_PRODUCT_STRATEGY', 'AI Product Strategy', 'Discovery, AI feasibility, roadmap, and measurable product outcomes.', TRUE, 1, NOW(), NOW()),
    (2, 'GENERATIVE_AI', 'Generative AI Applications', 'LLM-powered assistants, content workflows, copilots, and automation.', TRUE, 2, NOW(), NOW()),
    (3, 'NLP', 'Natural Language Processing', 'Text classification, extraction, summarization, semantic search, and RAG.', TRUE, 3, NOW(), NOW()),
    (4, 'COMPUTER_VISION', 'Computer Vision', 'OCR, object detection, image classification, and visual quality control.', TRUE, 4, NOW(), NOW()),
    (5, 'DATA_ENGINEERING', 'Data Engineering', 'Batch/stream pipelines, warehousing, data quality, and orchestration.', TRUE, 5, NOW(), NOW()),
    (6, 'DATA_ANALYTICS_BI', 'Data Analytics & BI', 'Dashboards, KPI modeling, exploratory analytics, and reporting systems.', TRUE, 6, NOW(), NOW()),
    (7, 'MACHINE_LEARNING', 'Machine Learning', 'Predictive modeling, recommendation, forecasting, and evaluation.', TRUE, 7, NOW(), NOW()),
    (8, 'MLOPS', 'MLOps & Model Operations', 'Deployment, monitoring, model registry, CI/CD, and reliability.', TRUE, 8, NOW(), NOW()),
    (9, 'CLOUD_BACKEND', 'Cloud Backend Systems', 'API services, distributed systems, integrations, and cloud infrastructure.', TRUE, 9, NOW(), NOW()),
    (10, 'WEB_PLATFORM', 'Web Platform Engineering', 'Modern web applications, frontend architecture, and API integration.', TRUE, 10, NOW(), NOW()),
    (11, 'MOBILE_PRODUCT', 'Mobile Product Engineering', 'Cross-platform mobile apps, mobile UX, and backend integration.', TRUE, 11, NOW(), NOW()),
    (12, 'UX_UI_DESIGN', 'UX/UI Design Systems', 'Interface design, user flows, accessibility, and design systems.', TRUE, 12, NOW(), NOW()),
    (13, 'BRAND_VISUAL_DESIGN', 'Brand & Visual Design', 'Brand identity, visual direction, presentation, and campaign assets.', TRUE, 13, NOW(), NOW()),
    (14, 'PRODUCT_RESEARCH', 'Product Research', 'User research, market discovery, usability testing, and synthesis.', TRUE, 14, NOW(), NOW()),
    (15, 'CYBERSECURITY', 'Cybersecurity', 'Application security, threat modeling, hardening, and secure review.', TRUE, 15, NOW(), NOW()),
    (16, 'FINTECH_PAYMENTS', 'FinTech & Payments', 'Payment gateway, escrow, wallet, reconciliation, and transaction flows.', TRUE, 16, NOW(), NOW()),
    (17, 'ECOMMERCE_RETAIL', 'E-commerce & Retail Tech', 'Catalog, search, recommendation, CRM, and customer experience systems.', TRUE, 17, NOW(), NOW()),
    (18, 'PROCESS_AUTOMATION', 'Business Process Automation', 'Workflow automation, internal tools, approval flows, and integrations.', TRUE, 18, NOW(), NOW()),
    (19, 'GAME_INTERACTIVE', 'Game & Interactive Experiences', 'Interactive prototypes, simulations, 2D/3D experiences, and creative tech.', TRUE, 19, NOW(), NOW()),
    (20, 'VIBE_CODING_CREATIVE_TECH', 'Vibe Coding & Creative Tech', 'Rapid product prototyping, AI-assisted coding, and expressive digital experiences.', TRUE, 20, NOW(), NOW());

INSERT INTO skills (skill_id, skill_code, skill_name, description, is_active, created_at, updated_at) VALUES
    (1, 'PROMPT_ENGINEERING', 'Prompt Engineering', 'Instruction design, prompt patterns, evaluation, and structured outputs.', TRUE, NOW(), NOW()),
    (2, 'RAG_ARCHITECTURE', 'RAG Architecture', 'Retrieval, reranking, grounding, citations, and knowledge-base design.', TRUE, NOW(), NOW()),
    (3, 'LLM_TOOL_CALLING', 'LLM Tool Calling', 'Function calling, agent workflows, tool routing, and guardrails.', TRUE, NOW(), NOW()),
    (4, 'MODEL_EVALUATION', 'Model Evaluation', 'Offline/online evaluation, benchmark design, test sets, and quality metrics.', TRUE, NOW(), NOW()),
    (5, 'PYTHON', 'Python Engineering', 'Backend services, scripts, data processing, and automation.', TRUE, NOW(), NOW()),
    (6, 'JAVA_SPRING_BOOT', 'Java Spring Boot', 'REST APIs, security, persistence, and enterprise backend development.', TRUE, NOW(), NOW()),
    (7, 'REACT_TYPESCRIPT', 'React TypeScript', 'Component architecture, state management, forms, and API-driven UI.', TRUE, NOW(), NOW()),
    (8, 'POSTGRESQL', 'PostgreSQL', 'Relational modeling, indexing, transactions, and query optimization.', TRUE, NOW(), NOW()),
    (9, 'DOCKER_DEVOPS', 'Docker & DevOps', 'Containerization, local parity, CI/CD, and deployment packaging.', TRUE, NOW(), NOW()),
    (10, 'FIREBASE_STORAGE', 'Firebase Storage', 'File upload, public/private assets, document storage, and access patterns.', TRUE, NOW(), NOW()),
    (11, 'PAYOS_INTEGRATION', 'PayOS Integration', 'Payment link flow, webhook handling, reconciliation, and wallet top-up deposits.', TRUE, NOW(), NOW()),
    (12, 'DATA_PIPELINE', 'Data Pipeline Design', 'ETL/ELT pipelines, orchestration, validation, and monitoring.', TRUE, NOW(), NOW()),
    (13, 'POWER_BI_DASHBOARD', 'BI Dashboarding', 'Metric modeling, visualization, drill-down, and stakeholder dashboards.', TRUE, NOW(), NOW()),
    (14, 'OCR_PIPELINE', 'OCR Pipeline', 'Document preprocessing, extraction, validation, and review workflows.', TRUE, NOW(), NOW()),
    (15, 'COMPUTER_VISION_MODELING', 'Computer Vision Modeling', 'Detection, classification, segmentation, and dataset preparation.', TRUE, NOW(), NOW()),
    (16, 'UX_RESEARCH', 'UX Research', 'Interviewing, usability testing, journey mapping, and insight synthesis.', TRUE, NOW(), NOW()),
    (17, 'DESIGN_SYSTEMS', 'Design Systems', 'Reusable UI components, tokens, interaction states, and accessibility.', TRUE, NOW(), NOW()),
    (18, 'SECURE_AUTH_JWT', 'Secure Auth & JWT', 'Bcrypt, JWT, authorization rules, and account lifecycle controls.', TRUE, NOW(), NOW()),
    (19, 'API_TESTING_SWAGGER', 'API Testing & Swagger', 'Contract documentation, Postman testing, and regression checks.', TRUE, NOW(), NOW()),
    (20, 'CREATIVE_PROTOTYPING', 'Creative Prototyping', 'Fast UI/product experiments, vibe coding, and interactive concept validation.', TRUE, NOW(), NOW());

INSERT INTO jobs (
    job_id, business_id, title, raw_requirements, structured_sow, ai_tag, budget, status,
    planned_duration_value, planned_duration_unit, is_hot, hot_until, published_at, created_at, updated_at
) VALUES
    (1, 1, 'AI chatbot tu van san pham cho website ban le',
     'Can chatbot hoi dap san pham, tim kiem theo nhu cau, goi y combo va chuyen lead cho nhan vien.',
     'Scope: build RAG chatbot, product ingestion pipeline, admin FAQ review, handoff to CRM, and analytics dashboard.',
     'NLP,RAG,LLM', 120000000, 'OPEN', 8, 'WEEK', TRUE, NOW() + INTERVAL '7 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '1 day'),
    (2, 1, 'OCR hoa don va phieu bao hanh tieng Viet',
     'Trich xuat thong tin tu hoa don, so serial, ngay mua, ma san pham va day vao he thong CRM.',
     'Scope: OCR pipeline, validation rules, human review queue, export API, and accuracy report.',
     'CV,OCR', 85000000, 'OPEN', 6, 'WEEK', FALSE, NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'),
    (3, 1, 'Trien khai monitoring cho model recommendation',
     'Can dashboard theo doi drift, latency, ty le click, va canh bao khi model giam hieu qua.',
     'Scope: model monitoring metrics, alerting workflow, retraining checklist, and operations documentation.',
     'MLOPS,DEPLOY', 65000000, 'OPEN', 4, 'WEEK', FALSE, NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day');

INSERT INTO job_domains (job_id, domain_id, created_at) VALUES
    (1, 2, NOW()),
    (1, 3, NOW()),
    (2, 4, NOW()),
    (3, 8, NOW());

INSERT INTO job_skills (job_id, skill_id, required_level, is_mandatory, min_years_experience, created_at) VALUES
    (1, 2, 'Advanced', TRUE, 2, NOW()),
    (1, 3, 'Intermediate', TRUE, 2, NOW()),
    (1, 6, 'Intermediate', FALSE, 1, NOW()),
    (2, 14, 'Advanced', TRUE, 2, NOW()),
    (2, 15, 'Intermediate', TRUE, 1, NOW()),
    (3, 9, 'Intermediate', TRUE, 1, NOW()),
    (3, 8, 'Intermediate', FALSE, 1, NOW());

INSERT INTO proposals (proposal_id, job_id, expert_id, technical_solution, bid_amount, status, created_at, updated_at) VALUES
    (1, 1, 1, 'Use PostgreSQL metadata plus vector retrieval, reranking, answer citations, and CRM handoff events.', 112000000, 'Accepted', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),
    (2, 2, 1, 'Build OCR pipeline with preprocessing, template detection, validation rules, and manual review UI.', 78000000, 'Pending', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (3, 3, 1, 'Deploy metrics collector, drift checks, alert rules, and model health dashboard.', 60000000, 'Rejected', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day');

INSERT INTO contracts (
    contract_id, job_id, business_id, expert_id, technology_used, total_budget, timeline_days,
    nda_signed, status, business_accepted_at, expert_accepted_at, created_at, updated_at
) VALUES
    (1, 1, 1, 1, 'Spring Boot, PostgreSQL, vector search, LLM API, React dashboard', 112000000, 56, TRUE, 'Active', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day'),
    (2, 2, 1, 1, 'Python OCR service, PostgreSQL, reviewer workflow', 78000000, 42, TRUE, 'Completed', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days');

INSERT INTO milestones (milestone_id, job_id, contract_id, milestone_name, funds_allocated, order_index, status, created_at, updated_at) VALUES
    (1, 1, NULL, 'Discovery va solution design', 28000000, 1, 'Released', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),
    (2, 1, NULL, 'RAG chatbot MVP', 52000000, 2, 'Under Review', NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day'),
    (3, 1, NULL, 'Production hardening va handover', 32000000, 3, 'Pending', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),
    (4, 2, NULL, 'OCR extraction delivery', 78000000, 1, 'Released', NOW() - INTERVAL '18 days', NOW() - INTERVAL '2 days');

INSERT INTO acceptance_criteria (criteria_id, milestone_id, description, is_passed, created_at, updated_at) VALUES
    (1, 1, 'SoW, architecture diagram, and delivery plan approved by both parties.', TRUE, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),
    (2, 2, 'Chatbot answers from approved product catalog with visible source references.', TRUE, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),
    (3, 2, 'Fallback and unsafe-answer guardrails are covered by regression tests.', FALSE, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),
    (4, 4, 'OCR extracts invoice number, serial, date, and product code with review export.', TRUE, NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days');

INSERT INTO deliverables (deliverable_id, milestone_id, source_code_url, demo_link, submission_notes, created_at, updated_at) VALUES
    (1, 2, 'https://git.aitasker.local/nova/retail-rag-chatbot', 'https://demo.aitasker.local/nova-rag-chatbot', 'MVP submitted with product ingestion sample and evaluation report.', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (2, 4, 'https://git.aitasker.local/nova/ocr-pipeline', 'https://demo.aitasker.local/nova-ocr', 'Final OCR pipeline delivered with QA report and deployment notes.', NOW() - INTERVAL '8 days', NOW() - INTERVAL '2 days');

INSERT INTO transactions (transaction_id, milestone_id, amount, commission_fee, transaction_type, status, created_at, updated_at) VALUES
    (1, 1, 28000000, 2800000, 'Deposit', 'Success', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (2, 2, 52000000, 5200000, 'Deposit', 'Pending', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (3, 4, 78000000, 7800000, 'Deposit', 'Success', NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
    (4, 4, 78000000, 7800000, 'Payout', 'Success', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

INSERT INTO disputes (dispute_id, contract_id, milestone_id, assigned_staff_id, evidence_report, proposed_action, admin_approved_by, status, created_at, updated_at) VALUES
    (1, 1, 2, 1, 'Business requests additional validation for hallucination guardrails before accepting milestone 2.', 'Hold escrow until staff confirms test checklist.', NULL, 'UnderReview', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours'),
    (2, 2, 4, 1, 'Minor OCR field mismatch was reviewed and closed after expert correction.', 'Release payout after correction accepted.', 3, 'Resolved', NOW() - INTERVAL '7 days', NOW() - INTERVAL '2 days');

INSERT INTO reviews (review_id, contract_id, reviewer_id, reviewee_id, rating, comment, created_at) VALUES
    (1, 2, 1, 2, 4.8, 'Expert delivered a clear OCR workflow and responded quickly to QA feedback.', NOW() - INTERVAL '2 days'),
    (2, 2, 2, 1, 4.7, 'Business provided test invoices and review feedback on time.', NOW() - INTERVAL '2 days');

INSERT INTO contract_change_requests (
    request_id, contract_id, requested_by_account_id, change_type, change_summary,
    proposed_budget, proposed_timeline_days, status, reviewed_by_account_id, reviewed_at, created_at, updated_at
) VALUES
    (1, 1, 1, 'ScopeAdjustment', 'Add 20 extra FAQ scenarios and Vietnamese synonym evaluation set.', 120000000, 60, 'Pending', NULL, NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

INSERT INTO system_settings (setting_key, setting_value, value_type, description, is_active, updated_by_role_id, created_at, updated_at) VALUES
    ('platform_fee_percent', '10', 'DECIMAL', 'Platform commission applied to successful escrow transactions.', TRUE, 3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),
    ('default_sla_days', '7', 'INT', 'Default number of days before milestone auto-approval review.', TRUE, 3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),
    ('auto_assign_staff_enabled', 'true', 'BOOLEAN', 'Automatically assign staff when a dispute is opened.', TRUE, 3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),
    ('max_open_jobs_per_business', '5', 'INT', 'Soft limit for concurrently open jobs per business account.', TRUE, 3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day');

INSERT INTO audit_logs (log_id, actor_account_id, action, entity_name, entity_id, old_value_json, new_value_json, ip_address, user_agent, created_at) VALUES
    (1, 3, 'APPROVE_BUSINESS_PROFILE', 'business_profiles', '1', NULL, '{"kybStatus":"Approved"}', '127.0.0.1', 'AITASKER seed', NOW() - INTERVAL '10 days'),
    (2, 3, 'APPROVE_EXPERT_PROFILE', 'expert_profiles', '1', NULL, '{"kycStatus":"Approved"}', '127.0.0.1', 'AITASKER seed', NOW() - INTERVAL '10 days'),
    (3, 4, 'ASSIGN_DISPUTE', 'disputes', '1', '{"status":"Open"}', '{"status":"UnderReview","assignedStaffId":1}', '127.0.0.1', 'AITASKER seed', NOW() - INTERVAL '12 hours');

INSERT INTO system_wallet (
    system_wallet_id, account_id, role_id, wallet_type, transaction_id,
    deposited_business_count, successful_deposit_count, current_balance, available_balance, escrow_balance,
    total_revenue, holding_balance, disputed_balance, currency, last_synced_at, created_at, updated_at
) VALUES
    (1, 3, 3, 'ADMIN_SYSTEM', 4, 1, 2, 46400000, 18400000, 28000000, 18400000, 28000000, 0, 'VND', NOW(), NOW(), NOW()),
    (2, 1, 1, 'BUSINESS', 4, 0, 0, 94000000, 94000000, 106000000, 0, 0, 0, 'VND', NOW(), NOW(), NOW()),
    (3, 2, 2, 'EXPERT', 4, 0, 0, 78000000, 78000000, 0, 0, 0, 0, 'VND', NOW(), NOW(), NOW()),
    (4, 4, 4, 'STAFF', 4, 0, 0, 0, 0, 0, 0, 0, 0, 'VND', NOW(), NOW(), NOW());

SELECT setval(pg_get_serial_sequence('roles', 'role_id'), COALESCE((SELECT MAX(role_id) FROM roles), 1), TRUE);
SELECT setval(pg_get_serial_sequence('account', 'account_id'), COALESCE((SELECT MAX(account_id) FROM account), 1), TRUE);
SELECT setval(pg_get_serial_sequence('staffs', 'staff_id'), COALESCE((SELECT MAX(staff_id) FROM staffs), 1), TRUE);
SELECT setval(pg_get_serial_sequence('business_profiles', 'business_id'), COALESCE((SELECT MAX(business_id) FROM business_profiles), 1), TRUE);
SELECT setval(pg_get_serial_sequence('expert_profiles', 'expert_id'), COALESCE((SELECT MAX(expert_id) FROM expert_profiles), 1), TRUE);
SELECT setval(pg_get_serial_sequence('portfolios', 'portfolio_id'), COALESCE((SELECT MAX(portfolio_id) FROM portfolios), 1), TRUE);
SELECT setval(pg_get_serial_sequence('domains', 'domain_id'), COALESCE((SELECT MAX(domain_id) FROM domains), 1), TRUE);
SELECT setval(pg_get_serial_sequence('skills', 'skill_id'), COALESCE((SELECT MAX(skill_id) FROM skills), 1), TRUE);
SELECT setval(pg_get_serial_sequence('jobs', 'job_id'), COALESCE((SELECT MAX(job_id) FROM jobs), 1), TRUE);
SELECT setval(pg_get_serial_sequence('proposals', 'proposal_id'), COALESCE((SELECT MAX(proposal_id) FROM proposals), 1), TRUE);
SELECT setval(pg_get_serial_sequence('contracts', 'contract_id'), COALESCE((SELECT MAX(contract_id) FROM contracts), 1), TRUE);
SELECT setval(pg_get_serial_sequence('milestones', 'milestone_id'), COALESCE((SELECT MAX(milestone_id) FROM milestones), 1), TRUE);
SELECT setval(pg_get_serial_sequence('acceptance_criteria', 'criteria_id'), COALESCE((SELECT MAX(criteria_id) FROM acceptance_criteria), 1), TRUE);
SELECT setval(pg_get_serial_sequence('deliverables', 'deliverable_id'), COALESCE((SELECT MAX(deliverable_id) FROM deliverables), 1), TRUE);
SELECT setval(pg_get_serial_sequence('transactions', 'transaction_id'), COALESCE((SELECT MAX(transaction_id) FROM transactions), 1), TRUE);
SELECT setval(pg_get_serial_sequence('disputes', 'dispute_id'), COALESCE((SELECT MAX(dispute_id) FROM disputes), 1), TRUE);
SELECT setval(pg_get_serial_sequence('system_wallet', 'system_wallet_id'), COALESCE((SELECT MAX(system_wallet_id) FROM system_wallet), 1), TRUE);
SELECT setval(pg_get_serial_sequence('reviews', 'review_id'), COALESCE((SELECT MAX(review_id) FROM reviews), 1), TRUE);
SELECT setval(pg_get_serial_sequence('contract_change_requests', 'request_id'), COALESCE((SELECT MAX(request_id) FROM contract_change_requests), 1), TRUE);
SELECT setval(pg_get_serial_sequence('audit_logs', 'log_id'), COALESCE((SELECT MAX(log_id) FROM audit_logs), 1), TRUE);
