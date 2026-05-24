-- System settings
INSERT INTO system_settings(
    setting_key, setting_value, value_type, description, is_active, updated_by
) VALUES
('platform_fee_percent', '10', 'DECIMAL', 'Phần trăm phí nền tảng', TRUE, NULL),
('default_sla_days', '7', 'INT', 'Số ngày SLA mặc định để auto-approve', TRUE, NULL),
('auto_assign_staff_enabled', 'true', 'BOOLEAN', 'Bật/tắt tự động gán staff xử lý ticket', TRUE, NULL)
ON CONFLICT (setting_key) DO NOTHING;