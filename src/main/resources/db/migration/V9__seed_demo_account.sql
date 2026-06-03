-- SEED DEMO ACCOUNT DUNG CHUAN TEN FILE FLYWAY.
-- PASSWORD PLAINTEXT: 12345678
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
