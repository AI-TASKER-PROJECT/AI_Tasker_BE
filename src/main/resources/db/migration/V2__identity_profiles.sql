-- Account
CREATE TABLE IF NOT EXISTS account (
    account_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    full_name VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_role
        FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Staffs
CREATE TABLE IF NOT EXISTS staffs (
    staff_id SERIAL PRIMARY KEY,
    account_id INT NOT NULL UNIQUE,
    specialization VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staffs_account
        FOREIGN KEY (account_id) REFERENCES account(account_id)
);

-- Business Profiles
CREATE TABLE IF NOT EXISTS business_profiles (
    business_id SERIAL PRIMARY KEY,
    account_id INT NOT NULL UNIQUE,
    tax_code VARCHAR(50) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    address TEXT,
    business_license_url VARCHAR(255),
    kyb_status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    approved_by INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_profiles_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_business_profiles_approved_by
        FOREIGN KEY (approved_by) REFERENCES staffs(staff_id),
    CONSTRAINT chk_business_profiles_kyb_status
        CHECK (kyb_status IN ('Pending', 'Approved', 'Rejected'))
);

-- Expert Profiles
CREATE TABLE IF NOT EXISTS expert_profiles (
    expert_id SERIAL PRIMARY KEY,
    account_id INT NOT NULL UNIQUE,
    national_id VARCHAR(50) NOT NULL UNIQUE,
    id_card_front_url VARCHAR(255),
    id_card_back_url VARCHAR(255),
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    approved_by INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expert_profiles_account
        FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_expert_profiles_approved_by
        FOREIGN KEY (approved_by) REFERENCES staffs(staff_id),
    CONSTRAINT chk_expert_profiles_kyc_status
        CHECK (kyc_status IN ('Pending', 'Approved', 'Rejected'))
);

CREATE INDEX IF NOT EXISTS idx_account_role_id ON account(role_id);
CREATE INDEX IF NOT EXISTS idx_business_profiles_status ON business_profiles(kyb_status);
CREATE INDEX IF NOT EXISTS idx_expert_profiles_status ON expert_profiles(kyc_status);