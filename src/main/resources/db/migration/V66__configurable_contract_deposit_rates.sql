INSERT INTO system_settings (
    setting_key, setting_value, value_type, description, is_active, created_at, updated_at
)
VALUES
    ('contract.deposit.business_percentage', '20.00', 'DECIMAL', 'Ty le ky quy hop dong cua doanh nghiep, tinh theo tong ngan sach chot.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('contract.deposit.expert_percentage', '10.00', 'DECIMAL', 'Ty le ky quy hop dong cua chuyen gia, tinh theo tong ngan sach chot.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;

UPDATE contracts c
SET total_budget = totals.final_budget,
    updated_at = CURRENT_TIMESTAMP
FROM (
    SELECT contract_id, SUM(final_budget) AS final_budget
    FROM contract_milestones
    GROUP BY contract_id
) totals
WHERE c.contract_id = totals.contract_id
  AND totals.final_budget > 0
  AND c.total_budget IS DISTINCT FROM totals.final_budget;
