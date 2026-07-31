-- Store structured per-criterion feedback for rejected final deliverables without adding a new table.

ALTER TABLE deliverables
    ADD COLUMN IF NOT EXISTS rejected_criteria_feedback JSONB;
