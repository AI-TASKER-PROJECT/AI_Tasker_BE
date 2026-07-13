-- US-056: participant negotiation history for a milestone dispute.
CREATE TABLE dispute_self_resolve_replies (
    reply_id BIGSERIAL PRIMARY KEY,
    dispute_id INT NOT NULL REFERENCES disputes(dispute_id),
    actor_account_id INT NOT NULL REFERENCES account(account_id),
    actor_role VARCHAR(30) NOT NULL,
    reply_type VARCHAR(50) NOT NULL,
    proposed_action VARCHAR(50),
    message TEXT NOT NULL,
    proposed_due_at TIMESTAMP,
    accepted_reply_id BIGINT REFERENCES dispute_self_resolve_replies(reply_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dispute_self_resolve_reply_role CHECK (actor_role IN ('BUSINESS', 'EXPERT')),
    CONSTRAINT chk_dispute_self_resolve_reply_type CHECK (reply_type IN (
        'ACCEPT_REQUEST', 'COUNTER_PROPOSAL', 'REQUEST_ADJUSTMENT', 'ACCEPT_PROPOSAL'
    )),
    CONSTRAINT chk_dispute_self_resolve_proposed_action CHECK (proposed_action IS NULL OR proposed_action IN (
        'CONTINUE_REVISION', 'ACCEPT_DELIVERABLE', 'CONTINUE_NEXT_MILESTONE', 'PARTIAL_REFUND', 'OTHER'
    ))
);

CREATE INDEX idx_dispute_self_resolve_replies_timeline
    ON dispute_self_resolve_replies(dispute_id, created_at, reply_id);
