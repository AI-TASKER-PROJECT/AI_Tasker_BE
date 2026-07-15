-- Balance Staff dispute routing without weakening the mandatory domain gate.

INSERT INTO system_settings (
    setting_key, setting_value, value_type, description, is_active,
    updated_by_role_id, created_at, updated_at
)
VALUES (
    'dispute_staff_max_active_cases', '5', 'INT',
    'Số tranh chấp đang hoạt động tối đa mà một Staff được nhận tự động.',
    TRUE, NULL, NOW(), NOW()
)
ON CONFLICT (setting_key) DO NOTHING;

-- Supports the workload count used for every routing decision.
CREATE INDEX IF NOT EXISTS idx_disputes_active_staff_workload
    ON disputes (assigned_staff_id)
    WHERE assigned_staff_id IS NOT NULL
      AND status IN (
          'PENDING_SELF_RESOLVE',
          'ESCALATION_REQUESTED',
          'STAFF_REVIEWING',
          'STAFF_DECIDED'
      );

-- Keep at least two seeded dispute-capable Staff profiles per business domain.
-- PROFILE_REVIEW remains isolated from contract-dispute routing.
WITH staff_domain_seed(email, domain_code) AS (
    VALUES
        ('staff10@aitasker.local', 'AI_PRODUCT_STRATEGY'),
        ('staff10@aitasker.local', 'GENERATIVE_AI'),
        ('staff10@aitasker.local', 'NLP'),
        ('staff10@aitasker.local', 'COMPUTER_VISION'),
        ('staff03@aitasker.local', 'MACHINE_LEARNING'),
        ('staff04@aitasker.local', 'DATA_ENGINEERING'),
        ('staff08@aitasker.local', 'DATA_ANALYTICS_BI'),
        ('staff07@aitasker.local', 'MLOPS'),
        ('staff08@aitasker.local', 'WEB_PLATFORM'),
        ('staff06@aitasker.local', 'MOBILE_PRODUCT'),
        ('staff09@aitasker.local', 'UX_UI_DESIGN'),
        ('staff06@aitasker.local', 'BRAND_VISUAL_DESIGN'),
        ('staff10@aitasker.local', 'BRAND_VISUAL_DESIGN'),
        ('staff01@aitasker.local', 'PRODUCT_RESEARCH'),
        ('staff07@aitasker.local', 'FINTECH_PAYMENTS'),
        ('staff09@aitasker.local', 'ECOMMERCE_RETAIL'),
        ('staff03@aitasker.local', 'PROCESS_AUTOMATION'),
        ('staff09@aitasker.local', 'GAME_INTERACTIVE'),
        ('staff01@aitasker.local', 'VIBE_CODING_CREATIVE_TECH')
)
INSERT INTO staff_domains (staff_id, domain_id)
SELECT staff.staff_id, domain.domain_id
FROM staff_domain_seed seed
JOIN account account ON account.email = seed.email
JOIN staffs staff ON staff.account_id = account.account_id
JOIN domains domain ON domain.domain_code = seed.domain_code
ON CONFLICT (staff_id, domain_id) DO NOTHING;
