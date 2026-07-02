-- V47__add_contract_termination_fields.sql
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS termination_reason TEXT;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS termination_note TEXT;
ALTER TABLE contracts ADD COLUMN IF NOT EXISTS terminated_at TIMESTAMP;
