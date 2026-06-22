-- Grant Business accounts the same initial free-credit behavior as Expert accounts,
-- and persist Business selection state for AI expert recommendations.

ALTER TABLE proposals
    ADD COLUMN IF NOT EXISTS business_selected BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE expert_recommendations
    ADD COLUMN IF NOT EXISTS business_selected BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE user_quotas uq
SET
    job_post_quota_balance = uq.job_post_quota_balance + 3,
    updated_at = CURRENT_TIMESTAMP
FROM account a
JOIN roles r ON r.role_id = a.role_id
WHERE uq.account_id = a.account_id
  AND r.role_name = 'BUSINESS'
  AND NOT EXISTS (
      SELECT 1
      FROM quota_usage_logs q
      WHERE q.account_id = a.account_id
        AND q.quota_type = 'JOB_POST'
        AND q.action_type = 'GRANT'
        AND q.reference_type = 'INITIAL_BUSINESS_GRANT'
  );

INSERT INTO user_quotas (
    account_id,
    job_post_quota_balance,
    proposal_quota_balance,
    created_at,
    updated_at
)
SELECT
    a.account_id,
    3,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
JOIN roles r ON r.role_id = a.role_id
WHERE r.role_name = 'BUSINESS'
  AND NOT EXISTS (
      SELECT 1
      FROM user_quotas uq
      WHERE uq.account_id = a.account_id
  );

INSERT INTO quota_usage_logs (
    account_id,
    quota_type,
    action_type,
    amount,
    balance_before,
    balance_after,
    reference_type,
    reference_id
)
SELECT
    a.account_id,
    'JOB_POST',
    'GRANT',
    3,
    uq.job_post_quota_balance - 3,
    uq.job_post_quota_balance,
    'INITIAL_BUSINESS_GRANT',
    a.account_id
FROM account a
JOIN roles r ON r.role_id = a.role_id
JOIN user_quotas uq ON uq.account_id = a.account_id
WHERE r.role_name = 'BUSINESS'
  AND NOT EXISTS (
      SELECT 1
      FROM quota_usage_logs q
      WHERE q.account_id = a.account_id
        AND q.quota_type = 'JOB_POST'
        AND q.action_type = 'GRANT'
        AND q.reference_type = 'INITIAL_BUSINESS_GRANT'
  );
