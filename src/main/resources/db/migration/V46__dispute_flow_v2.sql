-- V46__dispute_flow_v2.sql
ALTER TABLE contract_milestones ADD COLUMN resubmit_count INT DEFAULT 0;

ALTER TABLE disputes ADD COLUMN initiated_by VARCHAR(20);
ALTER TABLE disputes ADD COLUMN escalation_reason TEXT;
ALTER TABLE disputes ADD COLUMN escalation_evidence_file VARCHAR(255);
ALTER TABLE disputes ADD COLUMN staff_decision_percentage INT;
ALTER TABLE disputes ADD COLUMN staff_decision_note TEXT;
ALTER TABLE disputes ADD COLUMN previous_milestone_status VARCHAR(50);
ALTER TABLE disputes ADD COLUMN resolution_type VARCHAR(50);
ALTER TABLE disputes ADD COLUMN resolved_at TIMESTAMP;
ALTER TABLE disputes ADD COLUMN cancelled_at TIMESTAMP;

ALTER TABLE disputes DROP CONSTRAINT IF EXISTS chk_disputes_status;
ALTER TABLE disputes ADD CONSTRAINT chk_disputes_status CHECK (status IN ('Open','UnderReview','Resolved','Escalated', 'PENDING_SELF_RESOLVE', 'ESCALATION_REQUESTED', 'STAFF_REVIEWING', 'STAFF_DECIDED', 'INTERVENTION_REJECTED', 'RESOLVED', 'CANCELLED'));

CREATE UNIQUE INDEX IF NOT EXISTS uq_disputes_one_active_per_milestone
ON disputes(milestone_id)
WHERE milestone_id IS NOT NULL
  AND status IN ('PENDING_SELF_RESOLVE', 'ESCALATION_REQUESTED', 'STAFF_REVIEWING', 'STAFF_DECIDED');
