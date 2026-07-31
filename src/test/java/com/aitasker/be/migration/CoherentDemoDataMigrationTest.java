package com.aitasker.be.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoherentDemoDataMigrationTest {
    private static final String DEFAULT_URL =
            "jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh";

    @Test
    void migration_shouldCreateOneCoherentAndReconciledDemoDataset() throws Exception {
        String url = System.getenv().getOrDefault("TEST_DB_URL", DEFAULT_URL);
        String username = System.getenv().getOrDefault("DB_USER", "aitasker");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "aitasker123");

        Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            assertEquals(72, intValue(connection,
                    "SELECT MAX(version::integer) FROM flyway_schema_history WHERE success"));
            assertEquals(1, intValue(connection, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema='public' AND table_name='audit_logs'
                      AND column_name='actor_account_id' AND is_nullable='YES'
                    """));
            assertEquals(1, intValue(connection, """
                    SELECT COUNT(*) FROM system_settings
                    WHERE setting_key='milestone_review_sla_duration'
                      AND setting_value ~ '^[1-9][0-9]*:(MINUTE|HOUR|DAY)$'
                      AND is_active=TRUE
                    """));
            assertEquals(0, intValue(connection, """
                    SELECT COUNT(*) FROM system_settings
                    WHERE setting_key='default_sla_days' AND is_active=TRUE
                    """));
            assertEquals(2, intValue(connection, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema='public' AND table_name='contract_milestones'
                      AND column_name IN ('review_started_at','review_due_at')
                    """));
            assertEquals(49, intValue(connection,
                    "SELECT COUNT(*) FROM account WHERE email LIKE '%@aitasker.local'"));
            assertEquals(1, roleCount(connection, "ADMIN"));
            assertEquals(10, roleCount(connection, "BUSINESS"));
            assertEquals(28, roleCount(connection, "EXPERT"));
            assertEquals(10, roleCount(connection, "STAFF"));
            assertEquals(30, intValue(connection, "SELECT COUNT(*) FROM domains"));
            assertEquals(30, intValue(connection, "SELECT COUNT(*) FROM skills"));
            assertEquals(30, intValue(connection, "SELECT COUNT(*) FROM technologies"));
            assertEquals(10, intValue(connection, """
                    SELECT COUNT(*) FROM business_profiles bp
                    JOIN account a ON a.account_id=bp.account_id
                    WHERE a.email LIKE '%@aitasker.local'
                    """));
            assertEquals(28, intValue(connection, """
                    SELECT COUNT(*) FROM expert_profiles ep
                    JOIN account a ON a.account_id=ep.account_id
                    WHERE a.email LIKE '%@aitasker.local'
                    """));
            assertEquals(28, intValue(connection, """
                    SELECT COUNT(*) FROM portfolios p
                    JOIN expert_profiles ep ON ep.expert_id=p.expert_id
                    JOIN account a ON a.account_id=ep.account_id
                    WHERE a.email LIKE '%@aitasker.local'
                    """));
            assertEquals(10, intValue(connection, """
                    SELECT COUNT(*) FROM staffs s
                    JOIN account a ON a.account_id=s.account_id
                    WHERE a.email LIKE '%@aitasker.local'
                    """));
            assertEquals(12, intValue(connection, """
                    SELECT COUNT(*)
                    FROM expert_profiles ep
                    JOIN account a ON a.account_id=ep.account_id
                    JOIN portfolios p ON p.expert_id=ep.expert_id
                    JOIN user_quotas uq ON uq.account_id=a.account_id
                    JOIN system_wallet sw ON sw.account_id=a.account_id
                    WHERE ep.portfolio_url LIKE 'https://assets.example.test/experts/curated/%'
                      AND a.status='Approved' AND ep.kyc_status='Approved'
                      AND uq.proposal_quota_balance>=5 AND sw.wallet_type='EXPERT'
                    """));
            assertTrue(intValue(connection, """
                    SELECT COUNT(*)
                    FROM portfolios p
                    JOIN expert_profiles ep ON ep.expert_id=p.expert_id
                    WHERE ep.portfolio_url LIKE 'https://assets.example.test/experts/curated/%'
                      AND string_to_array(p.skill_ids, ',') @> ARRAY['11','23']
                      AND string_to_array(p.domain_ids, ',') @> ARRAY['5']
                      AND string_to_array(p.technology_ids, ',') @> ARRAY['11']
                    """) >= 3);
            assertEquals(6, intValue(connection, """
                    SELECT COUNT(*)
                    FROM expert_profiles ep
                    JOIN account a ON a.account_id=ep.account_id
                    JOIN portfolios p ON p.expert_id=ep.expert_id
                    JOIN user_quotas uq ON uq.account_id=a.account_id
                    JOIN system_wallet sw ON sw.account_id=a.account_id
                    WHERE ep.portfolio_url LIKE 'https://assets.example.test/experts/nlp-rag/%'
                      AND a.status='Approved' AND ep.kyc_status='Approved'
                      AND uq.proposal_quota_balance>=5 AND sw.wallet_type='EXPERT'
                      AND string_to_array(p.skill_ids, ',') @> ARRAY['10','12','14','17','19']
                    """));
            assertEquals(0, intValue(connection, """
                    SELECT COUNT(*) FROM jobs j
                    WHERE NOT EXISTS (SELECT 1 FROM sow s WHERE s.job_id=j.job_id)
                       OR (SELECT COUNT(*) FROM milestones m WHERE m.job_id=j.job_id)<>3
                    """));
            assertEquals(0, intValue(connection, """
                    SELECT COUNT(*) FROM proposals p
                    WHERE p.bid_amount<>(
                        SELECT SUM((item->>'proposedBudget')::numeric)
                        FROM jsonb_array_elements(p.proposal_milestone::jsonb) item
                    )
                    """));
            assertEquals(0, intValue(connection, """
                    SELECT COUNT(*) FROM system_wallet
                    WHERE account_id BETWEEN 1 AND 31
                      AND wallet_type <> 'ADMIN_SYSTEM'
                      AND current_balance<>available_balance+escrow_balance+holding_balance+disputed_balance
                    """));
            assertEquals(0, intValue(connection, """
                    SELECT COUNT(*) FROM job_domains jd
                    JOIN domains d ON d.domain_id=jd.domain_id
                    WHERE d.domain_code='PROFILE_REVIEW'
                    """));

            try (var statement = connection.prepareStatement(
                    "SELECT password FROM account WHERE account_id=5")) {
                try (var result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertTrue(new BCryptPasswordEncoder().matches("12345678", result.getString(1)));
                }
            }
        }
    }

    private int roleCount(Connection connection, String roleName) throws Exception {
        try (var statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM account a JOIN roles r ON r.role_id=a.role_id
                WHERE r.role_name=? AND a.email LIKE '%@aitasker.local'
                """)) {
            statement.setString(1, roleName);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private int intValue(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}
