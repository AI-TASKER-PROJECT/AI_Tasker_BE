-- Supports the last-assigned tie-breaker without scanning all Staff disputes.
CREATE INDEX IF NOT EXISTS idx_disputes_staff_last_assigned
    ON disputes (assigned_staff_id, staff_review_started_at DESC, dispute_id DESC)
    WHERE assigned_staff_id IS NOT NULL
      AND staff_review_started_at IS NOT NULL;
