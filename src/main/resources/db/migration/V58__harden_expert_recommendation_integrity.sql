-- Release A keeps portfolio taxonomy columns unchanged and hardens only the
-- persisted recommendation boundary.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM expert_recommendations
        GROUP BY job_posting_id, expert_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot harden expert_recommendations: duplicate job/expert rows exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM expert_recommendations
        GROUP BY job_posting_id, rank_position
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot harden expert_recommendations: duplicate ranks exist within a job';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM expert_recommendations er
        LEFT JOIN jobs j ON j.job_id = er.job_posting_id
        WHERE j.job_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Cannot harden expert_recommendations: orphan job references exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM expert_recommendations er
        LEFT JOIN expert_profiles ep ON ep.expert_id = er.expert_id
        WHERE ep.expert_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Cannot harden expert_recommendations: orphan expert references exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM expert_recommendations er
        LEFT JOIN portfolios p ON p.portfolio_id = er.portfolio_id
        WHERE er.portfolio_id IS NOT NULL
          AND p.portfolio_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Cannot harden expert_recommendations: orphan portfolio references exist';
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_expert_recommendations_job_expert'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT uq_expert_recommendations_job_expert
            UNIQUE (job_posting_id, expert_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_expert_recommendations_job_rank'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT uq_expert_recommendations_job_rank
            UNIQUE (job_posting_id, rank_position);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_expert_recommendations_rank'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT chk_expert_recommendations_rank
            CHECK (rank_position BETWEEN 1 AND 5);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_expert_recommendations_score'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT chk_expert_recommendations_score
            CHECK (match_score IS NULL OR match_score BETWEEN 0 AND 100);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_expert_recommendations_job'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT fk_expert_recommendations_job
            FOREIGN KEY (job_posting_id) REFERENCES jobs(job_id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_expert_recommendations_expert'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT fk_expert_recommendations_expert
            FOREIGN KEY (expert_id) REFERENCES expert_profiles(expert_id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_expert_recommendations_portfolio'
          AND conrelid = 'expert_recommendations'::regclass
    ) THEN
        ALTER TABLE expert_recommendations
            ADD CONSTRAINT fk_expert_recommendations_portfolio
            FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_expert_recommendations_portfolio
    ON expert_recommendations(portfolio_id);
