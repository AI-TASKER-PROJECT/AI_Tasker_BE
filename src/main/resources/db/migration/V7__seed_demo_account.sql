-- NOTE FILE: src/main/resources/db/migration/V7__seed_demo_account.sql
-- Đây là file gì: Migration seed tài khoản demo để kiểm thử trạng thái account bị khóa.
-- Nhiệm vụ: Thêm account expert mẫu test@mail.com nếu chưa tồn tại, phục vụ test đăng nhập và kiểm tra khóa tài khoản.
-- Seed demo account: test@mail.com
-- Password plaintext tương ứng: 12345678
-- is_active = false theo dữ liệu bạn đang dùng để test case account bị khóa
INSERT INTO account (
    email,
    password,
    phone,
    full_name,
    role_id,
    is_active,
    created_at,
    updated_at
)
SELECT
    'test@mail.com',
    '$2a$10$ZoOaEjQxlTUW.Rd9m0EEpefjYq735RpolRJMsjb5xz/5Z0DJp2RaC',
    '0973354426',
    'Dori',
    r.role_id,
    false,
    NOW(),
    NOW()
FROM roles r
WHERE r.role_name = 'EXPERT'
  AND NOT EXISTS (
    SELECT 1 FROM account a WHERE a.email = 'test@mail.com'
  );
