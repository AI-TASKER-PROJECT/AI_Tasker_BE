-- MAIN FLOW 1: REGISTER & IDENTIFIER.
-- Account status thay the is_active de phan biet cho duyet, da duyet, bi tu choi va bi khoa.
ALTER TABLE account ADD COLUMN IF NOT EXISTS status VARCHAR(20);
UPDATE account
SET status = CASE WHEN COALESCE(is_active, TRUE) THEN 'Approved' ELSE 'Lock' END
WHERE status IS NULL OR status = '';
ALTER TABLE account ALTER COLUMN status SET DEFAULT 'Pending';
ALTER TABLE account ALTER COLUMN status SET NOT NULL;

ALTER TABLE account DROP CONSTRAINT IF EXISTS chk_account_status;
ALTER TABLE account ADD CONSTRAINT chk_account_status
CHECK (status IN ('Pending', 'Approved', 'Rejected', 'Lock'));

DROP INDEX IF EXISTS idx_account_status;
CREATE INDEX idx_account_status ON account(status);

ALTER TABLE account DROP COLUMN IF EXISTS is_active;

-- KYC expert khong con luu anh CCCD, chi giu national_id va bo sung bang chung nang luc.
ALTER TABLE expert_profiles ADD COLUMN IF NOT EXISTS portfolio_url VARCHAR(255);
ALTER TABLE expert_profiles ADD COLUMN IF NOT EXISTS years_of_experience INT;
UPDATE expert_profiles
SET portfolio_url = COALESCE(portfolio_url, 'https://portfolio.aitasker.local/legacy/expert-' || expert_id),
    years_of_experience = COALESCE(years_of_experience, 1)
WHERE portfolio_url IS NULL OR years_of_experience IS NULL;
ALTER TABLE expert_profiles ALTER COLUMN portfolio_url SET NOT NULL;
ALTER TABLE expert_profiles ALTER COLUMN years_of_experience SET NOT NULL;
ALTER TABLE expert_profiles DROP CONSTRAINT IF EXISTS chk_expert_profiles_years;
ALTER TABLE expert_profiles ADD CONSTRAINT chk_expert_profiles_years CHECK (years_of_experience >= 0);
ALTER TABLE expert_profiles DROP COLUMN IF EXISTS id_card_front_url;
ALTER TABLE expert_profiles DROP COLUMN IF EXISTS id_card_back_url;

ALTER TABLE staffs ALTER COLUMN specialization TYPE VARCHAR(255);

-- Chuan hoa cac code demo cu de tranh dung unique domain_name/skill_name khi seed catalog moi.
UPDATE domains SET domain_code = 'COMPUTER_VISION', sort_order = 4, updated_at = NOW()
WHERE domain_name = 'Computer Vision' AND domain_code <> 'COMPUTER_VISION';
UPDATE skills SET skill_code = 'OCR_PIPELINE', updated_at = NOW()
WHERE skill_name = 'OCR Pipeline' AND skill_code <> 'OCR_PIPELINE';

INSERT INTO domains (domain_code, domain_name, description, is_active, sort_order, created_at, updated_at) VALUES
    ('AI_PRODUCT_STRATEGY', 'AI Product Strategy', 'Discovery, AI feasibility, roadmap, and measurable product outcomes.', TRUE, 1, NOW(), NOW()),
    ('GENERATIVE_AI', 'Generative AI Applications', 'LLM-powered assistants, content workflows, copilots, and automation.', TRUE, 2, NOW(), NOW()),
    ('NLP', 'Natural Language Processing', 'Text classification, extraction, summarization, semantic search, and RAG.', TRUE, 3, NOW(), NOW()),
    ('COMPUTER_VISION', 'Computer Vision', 'OCR, object detection, image classification, and visual quality control.', TRUE, 4, NOW(), NOW()),
    ('DATA_ENGINEERING', 'Data Engineering', 'Batch/stream pipelines, warehousing, data quality, and orchestration.', TRUE, 5, NOW(), NOW()),
    ('DATA_ANALYTICS_BI', 'Data Analytics & BI', 'Dashboards, KPI modeling, exploratory analytics, and reporting systems.', TRUE, 6, NOW(), NOW()),
    ('MACHINE_LEARNING', 'Machine Learning', 'Predictive modeling, recommendation, forecasting, and evaluation.', TRUE, 7, NOW(), NOW()),
    ('MLOPS', 'MLOps & Model Operations', 'Deployment, monitoring, model registry, CI/CD, and reliability.', TRUE, 8, NOW(), NOW()),
    ('CLOUD_BACKEND', 'Cloud Backend Systems', 'API services, distributed systems, integrations, and cloud infrastructure.', TRUE, 9, NOW(), NOW()),
    ('WEB_PLATFORM', 'Web Platform Engineering', 'Modern web applications, frontend architecture, and API integration.', TRUE, 10, NOW(), NOW()),
    ('MOBILE_PRODUCT', 'Mobile Product Engineering', 'Cross-platform mobile apps, mobile UX, and backend integration.', TRUE, 11, NOW(), NOW()),
    ('UX_UI_DESIGN', 'UX/UI Design Systems', 'Interface design, user flows, accessibility, and design systems.', TRUE, 12, NOW(), NOW()),
    ('BRAND_VISUAL_DESIGN', 'Brand & Visual Design', 'Brand identity, visual direction, presentation, and campaign assets.', TRUE, 13, NOW(), NOW()),
    ('PRODUCT_RESEARCH', 'Product Research', 'User research, market discovery, usability testing, and synthesis.', TRUE, 14, NOW(), NOW()),
    ('CYBERSECURITY', 'Cybersecurity', 'Application security, threat modeling, hardening, and secure review.', TRUE, 15, NOW(), NOW()),
    ('FINTECH_PAYMENTS', 'FinTech & Payments', 'Payment gateway, escrow, wallet, reconciliation, and transaction flows.', TRUE, 16, NOW(), NOW()),
    ('ECOMMERCE_RETAIL', 'E-commerce & Retail Tech', 'Catalog, search, recommendation, CRM, and customer experience systems.', TRUE, 17, NOW(), NOW()),
    ('PROCESS_AUTOMATION', 'Business Process Automation', 'Workflow automation, internal tools, approval flows, and integrations.', TRUE, 18, NOW(), NOW()),
    ('GAME_INTERACTIVE', 'Game & Interactive Experiences', 'Interactive prototypes, simulations, 2D/3D experiences, and creative tech.', TRUE, 19, NOW(), NOW()),
    ('VIBE_CODING_CREATIVE_TECH', 'Vibe Coding & Creative Tech', 'Rapid product prototyping, AI-assisted coding, and expressive digital experiences.', TRUE, 20, NOW(), NOW())
ON CONFLICT (domain_code) DO UPDATE SET
    domain_name = EXCLUDED.domain_name,
    description = EXCLUDED.description,
    is_active = TRUE,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW();

INSERT INTO skills (skill_code, skill_name, description, is_active, created_at, updated_at) VALUES
    ('PROMPT_ENGINEERING', 'Prompt Engineering', 'Instruction design, prompt patterns, evaluation, and structured outputs.', TRUE, NOW(), NOW()),
    ('RAG_ARCHITECTURE', 'RAG Architecture', 'Retrieval, reranking, grounding, citations, and knowledge-base design.', TRUE, NOW(), NOW()),
    ('LLM_TOOL_CALLING', 'LLM Tool Calling', 'Function calling, agent workflows, tool routing, and guardrails.', TRUE, NOW(), NOW()),
    ('MODEL_EVALUATION', 'Model Evaluation', 'Offline/online evaluation, benchmark design, test sets, and quality metrics.', TRUE, NOW(), NOW()),
    ('PYTHON', 'Python Engineering', 'Backend services, scripts, data processing, and automation.', TRUE, NOW(), NOW()),
    ('JAVA_SPRING_BOOT', 'Java Spring Boot', 'REST APIs, security, persistence, and enterprise backend development.', TRUE, NOW(), NOW()),
    ('REACT_TYPESCRIPT', 'React TypeScript', 'Component architecture, state management, forms, and API-driven UI.', TRUE, NOW(), NOW()),
    ('POSTGRESQL', 'PostgreSQL', 'Relational modeling, indexing, transactions, and query optimization.', TRUE, NOW(), NOW()),
    ('DOCKER_DEVOPS', 'Docker & DevOps', 'Containerization, local parity, CI/CD, and deployment packaging.', TRUE, NOW(), NOW()),
    ('FIREBASE_STORAGE', 'Firebase Storage', 'File upload, public/private assets, document storage, and access patterns.', TRUE, NOW(), NOW()),
    ('PAYOS_INTEGRATION', 'PayOS Integration', 'Payment link flow, webhook handling, reconciliation, and wallet top-up deposits.', TRUE, NOW(), NOW()),
    ('DATA_PIPELINE', 'Data Pipeline Design', 'ETL/ELT pipelines, orchestration, validation, and monitoring.', TRUE, NOW(), NOW()),
    ('POWER_BI_DASHBOARD', 'BI Dashboarding', 'Metric modeling, visualization, drill-down, and stakeholder dashboards.', TRUE, NOW(), NOW()),
    ('OCR_PIPELINE', 'OCR Pipeline', 'Document preprocessing, extraction, validation, and review workflows.', TRUE, NOW(), NOW()),
    ('COMPUTER_VISION_MODELING', 'Computer Vision Modeling', 'Detection, classification, segmentation, and dataset preparation.', TRUE, NOW(), NOW()),
    ('UX_RESEARCH', 'UX Research', 'Interviewing, usability testing, journey mapping, and insight synthesis.', TRUE, NOW(), NOW()),
    ('DESIGN_SYSTEMS', 'Design Systems', 'Reusable UI components, tokens, interaction states, and accessibility.', TRUE, NOW(), NOW()),
    ('SECURE_AUTH_JWT', 'Secure Auth & JWT', 'Bcrypt, JWT, authorization rules, and account lifecycle controls.', TRUE, NOW(), NOW()),
    ('API_TESTING_SWAGGER', 'API Testing & Swagger', 'Contract documentation, Postman testing, and regression checks.', TRUE, NOW(), NOW()),
    ('CREATIVE_PROTOTYPING', 'Creative Prototyping', 'Fast UI/product experiments, vibe coding, and interactive concept validation.', TRUE, NOW(), NOW())
ON CONFLICT (skill_code) DO UPDATE SET
    skill_name = EXCLUDED.skill_name,
    description = EXCLUDED.description,
    is_active = TRUE,
    updated_at = NOW();
