-- Automatic jobs are first-class audit actors and must not be attributed to an
-- Admin, Business, or Expert account.
ALTER TABLE audit_logs
    ALTER COLUMN actor_account_id DROP NOT NULL;

UPDATE audit_logs
SET actor_account_id = NULL,
    action = 'Hệ thống tự động duyệt và giải ngân cột mốc khi hết hạn nghiệm thu'
WHERE action IN (
    'MILESTONE_REVIEW_SLA_AUTO_APPROVED',
    'Tự động duyệt milestone quá SLA',
    'Hệ thống tự động duyệt và giải ngân cột mốc khi hết hạn nghiệm thu'
);

UPDATE notifications
SET actor_account_id = NULL
WHERE type = 'MILESTONE_SLA_AUTO_APPROVED';
