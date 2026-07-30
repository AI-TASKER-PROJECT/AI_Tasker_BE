ALTER TABLE contract_milestones
    ADD COLUMN IF NOT EXISTS review_started_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS review_due_at TIMESTAMP;

INSERT INTO system_settings (
    setting_key, setting_value, value_type, description, is_active, created_at, updated_at
)
SELECT
    'milestone_review_sla_duration',
    COALESCE(NULLIF(TRIM(setting_value), ''), '3') || ':DAY',
    'STRING',
    'Thoi gian cho Business nghiem thu milestone, dinh dang so:MINUTE|HOUR|DAY.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM system_settings
WHERE setting_key = 'default_sla_days'
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO system_settings (
    setting_key, setting_value, value_type, description, is_active, created_at, updated_at
)
SELECT
    'milestone_review_sla_duration', '3:DAY', 'STRING',
    'Thoi gian cho Business nghiem thu milestone, dinh dang so:MINUTE|HOUR|DAY.',
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM system_settings WHERE setting_key = 'milestone_review_sla_duration'
);

UPDATE system_settings
SET is_active = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE setting_key = 'default_sla_days';

WITH configured_duration AS (
    SELECT
        split_part(setting_value, ':', 1)::INTEGER AS duration_value,
        split_part(setting_value, ':', 2) AS duration_unit
    FROM system_settings
    WHERE setting_key = 'milestone_review_sla_duration'
), latest_deliverable AS (
    SELECT milestone_id, MAX(created_at) AS submitted_at
    FROM deliverables
    GROUP BY milestone_id
)
UPDATE contract_milestones cm
SET review_started_at = latest_deliverable.submitted_at,
    review_due_at = latest_deliverable.submitted_at
        + CASE configured_duration.duration_unit
            WHEN 'MINUTE' THEN configured_duration.duration_value * INTERVAL '1 minute'
            WHEN 'HOUR' THEN configured_duration.duration_value * INTERVAL '1 hour'
            ELSE configured_duration.duration_value * INTERVAL '1 day'
          END
FROM latest_deliverable, configured_duration
WHERE cm.job_milestone_id = latest_deliverable.milestone_id
  AND cm.status = 'UNDER_REVIEW'
  AND cm.review_due_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_contract_milestones_review_sla_due
    ON contract_milestones (review_due_at, contract_milestone_id)
    WHERE status = 'UNDER_REVIEW' AND review_due_at IS NOT NULL;
