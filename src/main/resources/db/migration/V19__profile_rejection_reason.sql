ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);

ALTER TABLE expert_profiles
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);
