ALTER TABLE portfolios
    ADD COLUMN IF NOT EXISTS domain_ids VARCHAR(255),
    ADD COLUMN IF NOT EXISTS skill_ids VARCHAR(255),
    ADD COLUMN IF NOT EXISTS years_experience INT,
    ADD COLUMN IF NOT EXISTS certificates TEXT,
    ADD COLUMN IF NOT EXISTS self_description TEXT;

UPDATE portfolios p
SET
    domain_ids = COALESCE(NULLIF(p.domain_ids, ''), '2,3,5'),
    skill_ids = COALESCE(NULLIF(p.skill_ids, ''), '2,3,5,8'),
    years_experience = COALESCE(p.years_experience, e.years_of_experience, 0),
    certificates = COALESCE(p.certificates, ''),
    self_description = COALESCE(
        NULLIF(p.self_description, ''),
        'Chuyen gia AI co kinh nghiem xay dung RAG, xu ly du lieu san pham, thiet ke API va trien khai giai phap AI cho doanh nghiep.'
    )
FROM expert_profiles e
WHERE p.expert_id = e.expert_id;

INSERT INTO portfolios (
    expert_id,
    domain_ids,
    skill_ids,
    years_experience,
    certificates,
    self_description,
    created_at,
    updated_at
)
SELECT
    e.expert_id,
    '2,3,5',
    '2,3,5,8',
    COALESCE(e.years_of_experience, 0),
    '',
    'Chuyen gia AI co kinh nghiem phan tich yeu cau, xu ly du lieu, xay dung backend va trien khai giai phap AI cho doanh nghiep.',
    NOW(),
    NOW()
FROM expert_profiles e
WHERE NOT EXISTS (
    SELECT 1
    FROM portfolios p
    WHERE p.expert_id = e.expert_id
);

ALTER TABLE portfolios
    ALTER COLUMN domain_ids SET NOT NULL,
    ALTER COLUMN skill_ids SET NOT NULL,
    ALTER COLUMN years_experience SET NOT NULL,
    ALTER COLUMN self_description SET NOT NULL;

ALTER TABLE portfolios
    DROP COLUMN IF EXISTS context,
    DROP COLUMN IF EXISTS data_processing,
    DROP COLUMN IF EXISTS model_architecture,
    DROP COLUMN IF EXISTS performance_metrics,
    DROP COLUMN IF EXISTS poc_url;

ALTER TABLE portfolios
    DROP CONSTRAINT IF EXISTS chk_portfolios_years_experience,
    ADD CONSTRAINT chk_portfolios_years_experience CHECK (years_experience >= 0);
