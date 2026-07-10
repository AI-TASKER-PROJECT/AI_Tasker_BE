ALTER TABLE user_quotas
    ADD COLUMN IF NOT EXISTS premium_expired_at TIMESTAMP;

UPDATE user_quotas
SET premium_expired_at = badge_expired_at
WHERE premium_recommendation_visible = TRUE
  AND badge_expired_at IS NOT NULL
  AND premium_expired_at IS NULL;

ALTER TABLE user_quotas
    DROP COLUMN IF EXISTS premium_recommendation_visible;

CREATE INDEX IF NOT EXISTS idx_user_quotas_premium_expired_at
    ON user_quotas(premium_expired_at);
