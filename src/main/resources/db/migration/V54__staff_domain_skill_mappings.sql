-- US-050: Staff Dispute Inbox & Specialization Routing
-- Add structured staff-domain and staff-skill mapping tables.

CREATE TABLE IF NOT EXISTS staff_domains (
    staff_id INTEGER NOT NULL REFERENCES staffs(staff_id),
    domain_id INTEGER NOT NULL REFERENCES domains(domain_id),
    PRIMARY KEY (staff_id, domain_id)
);

CREATE TABLE IF NOT EXISTS staff_skills (
    staff_id INTEGER NOT NULL REFERENCES staffs(staff_id),
    skill_id INTEGER NOT NULL REFERENCES skills(skill_id),
    PRIMARY KEY (staff_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_staff_domains_domain_id ON staff_domains(domain_id);
CREATE INDEX IF NOT EXISTS idx_staff_skills_skill_id ON staff_skills(skill_id);

-- Backfill demo Staff (staff_id=1, Pham Quoc Huy) with explicit domain
-- and skill mappings covering all demo job domains and skills.
-- Do not parse free-text specialization; these are explicit assignments.

INSERT INTO staff_domains (staff_id, domain_id) VALUES
    (1, 2),   -- Generative AI Applications
    (1, 3),   -- Natural Language Processing
    (1, 4),   -- Computer Vision
    (1, 8),   -- MLOps & Model Operations
    (1, 17);  -- E-commerce & Retail Tech

INSERT INTO staff_skills (staff_id, skill_id) VALUES
    (1, 1),   -- Prompt Engineering
    (1, 2),   -- RAG Architecture
    (1, 3),   -- LLM Tool Calling
    (1, 5),   -- Python Engineering
    (1, 6),   -- Java Spring Boot
    (1, 8),   -- PostgreSQL
    (1, 9),   -- Docker & DevOps
    (1, 14),  -- OCR Pipeline
    (1, 15),  -- Computer Vision Modeling
    (1, 19);  -- API Testing & Swagger
