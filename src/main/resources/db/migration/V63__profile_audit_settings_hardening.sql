-- Hide obsolete seed/demo settings from operational configuration without deleting history.
UPDATE system_settings
SET is_active = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE setting_key IN (
    'platform_fee_percent',
    'auto_assign_staff_enabled',
    'max_open_jobs_per_business'
);

INSERT INTO system_settings (
    setting_key,
    setting_value,
    value_type,
    description,
    is_active,
    created_at,
    updated_at
) VALUES
    ('default_sla_days', '3', 'INT', 'So ngay truoc khi milestone UNDER_REVIEW duoc SLA auto-approve.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dispute_staff_max_active_cases', '5', 'INT', 'So tranh chap STAFF_REVIEWING toi da moi staff co the xu ly dong thoi.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('credit.job_post.price_vnd', '200', 'DECIMAL', 'Gia moi credit dang du an cua Business tinh bang VND.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('credit.proposal.price_vnd', '100', 'DECIMAL', 'Gia moi credit nop de xuat cua Expert tinh bang VND.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO UPDATE SET
    value_type = EXCLUDED.value_type,
    description = EXCLUDED.description,
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP;
