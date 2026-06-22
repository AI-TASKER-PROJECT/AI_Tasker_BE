-- V38__update_membership_package_prices.sql
-- US-023: Update membership package prices to low test values.
-- For existing databases that already ran V30.

UPDATE membership_packages SET price = 200  WHERE package_code = 'BUSINESS_STANDARD';
UPDATE membership_packages SET price = 500  WHERE package_code = 'BUSINESS_PLUS';
UPDATE membership_packages SET price = 1000 WHERE package_code = 'BUSINESS_PREMIUM';
UPDATE membership_packages SET price = 100  WHERE package_code = 'EXPERT_STANDARD';
UPDATE membership_packages SET price = 200  WHERE package_code = 'EXPERT_PLUS';
UPDATE membership_packages SET price = 600  WHERE package_code = 'EXPERT_PREMIUM';
