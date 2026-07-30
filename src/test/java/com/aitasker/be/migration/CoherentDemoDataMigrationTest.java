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
            assertEquals(67, intValue(connection,
                    "SELECT MAX(version::integer) FROM flyway_schema_history WHERE success"));
            assertEquals(31, intValue(connection, "SELECT COUNT(*) FROM account"));
            assertEquals(1, roleCount(connection, "ADMIN"));
            assertEquals(10, roleCount(connection, "BUSINESS"));
            assertEquals(10, roleCount(connection, "EXPERT"));
            assertEquals(10, roleCount(connection, "STAFF"));
            assertEquals(30, intValue(connection, "SELECT COUNT(*) FROM domains"));
            assertEquals(30, intValue(connection, "SELECT COUNT(*) FROM skills"));
            assertEquals(30, intValue(connection, "SELECT COUNT(*) FROM technologies"));
            assertEquals(10, intValue(connection, "SELECT COUNT(*) FROM business_profiles"));
            assertEquals(10, intValue(connection, "SELECT COUNT(*) FROM expert_profiles"));
            assertEquals(10, intValue(connection, "SELECT COUNT(*) FROM portfolios"));
            assertEquals(10, intValue(connection, "SELECT COUNT(*) FROM staffs"));
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
                    WHERE current_balance<>available_balance+escrow_balance+holding_balance+disputed_balance
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
                WHERE r.role_name=?
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
