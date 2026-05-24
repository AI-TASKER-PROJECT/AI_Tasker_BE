CREATE TABLE IF NOT EXISTS roles (
    role_id INT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO
    roles(role_id, role_name)
VALUES
    (1, 'BUSINESS'),
    (2, 'EXPERT'),
    (3, 'ADMIN'),
    (4, 'STAFF') ON CONFLICT (role_id) DO NOTHING;