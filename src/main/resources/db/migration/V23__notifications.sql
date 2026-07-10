-- NOTE FILE: src/main/resources/db/migration/V23__notifications.sql
-- Đây là file gì: Migration tạo bảng thông báo để lưu notification realtime và lịch sử thông báo của từng tài khoản.
-- Mục đích note: mô tả vai trò migration, không note rải vào từng câu SQL.

CREATE TABLE IF NOT EXISTS notifications (
    notification_id SERIAL PRIMARY KEY,
    receiver_account_id INT NOT NULL,
    actor_account_id INT,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    target_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_account_id) REFERENCES account(account_id),
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_account_id) REFERENCES account(account_id)
);

CREATE INDEX IF NOT EXISTS idx_notifications_receiver_created_at
    ON notifications(receiver_account_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_receiver_unread
    ON notifications(receiver_account_id, is_read);
