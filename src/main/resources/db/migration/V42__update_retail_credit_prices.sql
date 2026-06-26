-- V42__update_retail_credit_prices.sql
-- Update retail credit purchase prices to low test values.

UPDATE system_settings
SET setting_value = '100',
    description = 'Price for one Business job-post credit in VND.',
    updated_at = CURRENT_TIMESTAMP
WHERE setting_key = 'credit.job_post.price_vnd';

UPDATE system_settings
SET setting_value = '50',
    description = 'Price for one Expert proposal credit in VND.',
    updated_at = CURRENT_TIMESTAMP
WHERE setting_key = 'credit.proposal.price_vnd';
