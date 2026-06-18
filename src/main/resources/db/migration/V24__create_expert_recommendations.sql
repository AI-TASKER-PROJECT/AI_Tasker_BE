CREATE TABLE IF NOT EXISTS expert_recommendations (
    id BIGSERIAL PRIMARY KEY,
    job_posting_id BIGINT NOT NULL,
    expert_id BIGINT NOT NULL,
    portfolio_id BIGINT,
    rank_position INT NOT NULL,
    match_score DECIMAL(5,2),
    ai_reason TEXT,
    matched_skills TEXT,
    matched_domains TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_expert_recommendations_job_rank
    ON expert_recommendations(job_posting_id, rank_position);

CREATE INDEX IF NOT EXISTS idx_expert_recommendations_expert
    ON expert_recommendations(expert_id);
