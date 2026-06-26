-- V40__update_credit_prices.sql
-- US-023: Update credit prices to low test values.
-- For existing databases that already ran V30.

UPDATE system_settings SET setting_value = '200' WHERE setting_key = 'credit.job_post.price_vnd';
UPDATE system_settings SET setting_value = '100' WHERE setting_key = 'credit.proposal.price_vnd';
