-- V45 converts the fixed platform acceptance-criteria catalog into milestone-owned rows.
-- Existing linked descriptions are cloned per milestone before the join table is removed.

ALTER TABLE acceptance_criteria
    ADD COLUMN IF NOT EXISTS milestone_id INT;

INSERT INTO acceptance_criteria (
    criteria_code,
    description,
    is_active,
    sort_order,
    created_at,
    updated_at,
    milestone_id
)
SELECT
    'MIGRATED_' || mac.milestone_id || '_' || ac.criteria_id,
    ac.description,
    TRUE,
    ROW_NUMBER() OVER (
        PARTITION BY mac.milestone_id
        ORDER BY ac.sort_order, ac.criteria_id
    ),
    COALESCE(ac.created_at, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP,
    mac.milestone_id
FROM milestone_acceptance_criteria mac
JOIN acceptance_criteria ac ON ac.criteria_id = mac.criteria_id;

DROP TABLE milestone_acceptance_criteria;

-- Rows without milestone ownership are the old fixed catalog (including the 26 seeded rows).
DELETE FROM acceptance_criteria
WHERE milestone_id IS NULL;

DROP INDEX IF EXISTS uq_acceptance_criteria_code;

ALTER TABLE acceptance_criteria
    DROP COLUMN criteria_code,
    DROP COLUMN is_active;

ALTER TABLE acceptance_criteria
    ALTER COLUMN milestone_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_acceptance_criteria_milestone'
          AND conrelid = 'acceptance_criteria'::regclass
    ) THEN
        ALTER TABLE acceptance_criteria
            ADD CONSTRAINT fk_acceptance_criteria_milestone
            FOREIGN KEY (milestone_id)
            REFERENCES milestones(milestone_id)
            ON DELETE CASCADE;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_acceptance_criteria_milestone_order
    ON acceptance_criteria (milestone_id, sort_order, criteria_id);
