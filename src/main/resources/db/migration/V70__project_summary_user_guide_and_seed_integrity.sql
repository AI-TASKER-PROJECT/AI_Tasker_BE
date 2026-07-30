-- Store the usage guide only on the final deliverable of a successfully
-- completed project. Existing completed demo projects receive deterministic
-- guide paths so the summary screen has coherent fixtures.
ALTER TABLE deliverables
    ADD COLUMN IF NOT EXISTS user_guide_file_url TEXT;

UPDATE deliverables
SET user_guide_file_url = CASE deliverable_id
    WHEN 6005 THEN 'https://assets.example.test/deliverables/6005-huong-dan-su-dung.pdf'
    WHEN 6008 THEN 'https://assets.example.test/deliverables/6008-huong-dan-van-hanh.docx'
END
WHERE deliverable_id IN (6005, 6008);

INSERT INTO notifications (
    receiver_account_id, actor_account_id, type, title, message, target_url,
    is_read, read_at, metadata, idempotency_key, created_at
)
SELECT participant.receiver_account_id,
       NULL,
       'PROJECT_SUMMARY_READY',
       'Tổng kết dự án đã sẵn sàng',
       'Toàn bộ cột mốc của dự án "' || c.contract_title || '" đã hoàn thành. Bạn có thể xem thông tin và kết quả bàn giao tại trang tổng kết.',
       '/contracts/' || c.contract_id || '/summary',
       FALSE,
       NULL,
       jsonb_build_object('contractId', c.contract_id)::text,
       'SEED:PROJECT_SUMMARY_READY:' || c.contract_id || ':' || participant.receiver_account_id,
       c.updated_at
FROM contracts c
JOIN business_profiles bp ON bp.business_id = c.business_id
JOIN expert_profiles ep ON ep.expert_id = c.expert_id
CROSS JOIN LATERAL (VALUES (bp.account_id), (ep.account_id)) AS participant(receiver_account_id)
WHERE c.contract_id IN (4005, 4006)
ON CONFLICT DO NOTHING;

-- A project has exactly one primary domain. Keep the domain that best
-- represents each demo project's business problem; ids remain unchanged, so
-- proposals, contracts, milestones, ledgers and notifications keep their
-- existing referential links.
WITH primary_domain(job_id, domain_id) AS (
    VALUES
        (1001, 27), (1002, 7), (1003, 10), (1004, 21), (1005, 18),
        (1006, 20), (1007, 19), (1008, 22), (1009, 23), (1010, 23)
)
DELETE FROM job_domains jd
USING primary_domain selected
WHERE jd.job_id = selected.job_id
  AND jd.domain_id <> selected.domain_id;

-- Demo identifiers follow Vietnamese data shapes: tax codes contain 10
-- digits and citizen identity numbers contain 12 digits.
UPDATE business_profiles
SET tax_code = '01000000' || lpad(business_id::text, 2, '0'),
    updated_at = CURRENT_TIMESTAMP
WHERE business_id BETWEEN 1 AND 10;

UPDATE expert_profiles
SET national_id = '079000000' || lpad(expert_id::text, 3, '0'),
    updated_at = CURRENT_TIMESTAMP
WHERE expert_id BETWEEN 1 AND 10;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM jobs j
        WHERE j.job_id BETWEEN 1001 AND 1010
          AND (SELECT COUNT(*) FROM job_domains jd WHERE jd.job_id = j.job_id) <> 1
    ) THEN
        RAISE EXCEPTION 'Every job must have exactly one domain';
    END IF;

    IF EXISTS (SELECT 1 FROM business_profiles WHERE business_id BETWEEN 1 AND 10 AND tax_code !~ '^[0-9]{10}$')
       OR EXISTS (SELECT 1 FROM expert_profiles WHERE expert_id BETWEEN 1 AND 10 AND national_id !~ '^[0-9]{12}$') THEN
        RAISE EXCEPTION 'Demo tax codes or citizen identity numbers have an invalid format';
    END IF;

    IF EXISTS (
        SELECT 1 FROM proposals p
        LEFT JOIN jobs j ON j.job_id = p.job_id
        LEFT JOIN expert_profiles ep ON ep.expert_id = p.expert_id
        WHERE p.proposal_id BETWEEN 3001 AND 3012
          AND (j.job_id IS NULL OR ep.expert_id IS NULL)
    ) OR EXISTS (
        SELECT 1 FROM contracts c
        LEFT JOIN jobs j ON j.job_id = c.job_id
        LEFT JOIN proposals p ON p.proposal_id = c.proposal_id
        LEFT JOIN business_profiles bp ON bp.business_id = c.business_id
        LEFT JOIN expert_profiles ep ON ep.expert_id = c.expert_id
        WHERE c.contract_id BETWEEN 4001 AND 4006
          AND (j.job_id IS NULL OR p.proposal_id IS NULL
           OR bp.business_id IS NULL OR ep.expert_id IS NULL
           OR p.job_id <> c.job_id OR p.expert_id <> c.expert_id
           OR j.business_id <> c.business_id)
    ) THEN
        RAISE EXCEPTION 'Demo job, proposal, contract or participant links are inconsistent';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM deliverables d
        JOIN milestones m ON m.milestone_id = d.milestone_id
        WHERE d.user_guide_file_url IS NOT NULL
          AND m.order_index <> (SELECT MAX(m2.order_index) FROM milestones m2 WHERE m2.job_id = m.job_id)
    ) THEN
        RAISE EXCEPTION 'A usage guide may only belong to the final milestone';
    END IF;
END $$;
