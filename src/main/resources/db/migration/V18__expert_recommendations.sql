ALTER TABLE expert_profiles ADD COLUMN IF NOT EXISTS hourly_rate DECIMAL(18,2);
ALTER TABLE expert_profiles ADD COLUMN IF NOT EXISTS availability VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE';

UPDATE expert_profiles
SET hourly_rate = COALESCE(hourly_rate, 500000),
    availability = COALESCE(NULLIF(availability, ''), 'AVAILABLE');

CREATE TABLE IF NOT EXISTS expert_recommendations (
    recommendation_id SERIAL PRIMARY KEY,
    job_id INT NOT NULL,
    expert_id INT NOT NULL,
    match_score DECIMAL(5,2) NOT NULL,
    matched_skills TEXT,
    reason TEXT NOT NULL,
    risk_notes TEXT,
    suggested_role VARCHAR(255),
    keyword_json TEXT,
    ai_note TEXT,
    candidate_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expert_recommendations_job
        FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_expert_recommendations_expert
        FOREIGN KEY (expert_id) REFERENCES expert_profiles(expert_id) ON DELETE CASCADE,
    CONSTRAINT uq_expert_recommendations_job_expert UNIQUE (job_id, expert_id),
    CONSTRAINT chk_expert_recommendations_score
        CHECK (match_score >= 0 AND match_score <= 100)
);

CREATE INDEX IF NOT EXISTS idx_expert_recommendations_job_score
    ON expert_recommendations(job_id, match_score DESC);
