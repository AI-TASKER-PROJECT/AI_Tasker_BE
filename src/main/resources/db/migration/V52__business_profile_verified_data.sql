ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS verified_representative VARCHAR(255);

COMMENT ON COLUMN business_profiles.verified_representative
    IS 'Nguoi dai dien phap ly tu VietQR API, dung de staff doi chieu giay phep kinh doanh';
