package com.aitasker.be.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PremiumExpirationMigrationTest {
    @Test
    void migration_shouldBackfillPremiumExpirationAndDropOldFlag() throws Exception {
        String schema = "premium_migration_test_" + System.nanoTime();
        String url = System.getenv().getOrDefault("TEST_DB_URL",
                "jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh");
        String username = System.getenv().getOrDefault("DB_USER", "aitasker");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "aitasker123");
        LocalDateTime future = LocalDateTime.now().plusDays(12).withNano(0);

        try (var connection = DriverManager.getConnection(url, username, password);
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            statement.execute("SET search_path TO " + schema);
            statement.execute("""
                    CREATE TABLE user_quotas (
                        quota_id BIGSERIAL PRIMARY KEY,
                        account_id INTEGER NOT NULL UNIQUE,
                        job_post_quota_balance INTEGER NOT NULL,
                        proposal_quota_balance INTEGER NOT NULL,
                        badge_expired_at TIMESTAMP,
                        premium_recommendation_visible BOOLEAN NOT NULL DEFAULT FALSE
                    )
                    """);
            try (var insert = connection.prepareStatement("""
                    INSERT INTO user_quotas (
                        account_id,
                        job_post_quota_balance,
                        proposal_quota_balance,
                        badge_expired_at,
                        premium_recommendation_visible
                    ) VALUES (1, 0, 0, ?, TRUE)
                    """)) {
                insert.setTimestamp(1, Timestamp.valueOf(future));
                insert.executeUpdate();
            }

            String migrationSql = Files.readString(Path.of(
                    "src/main/resources/db/migration/V32__premium_expiration_entitlement.sql"));
            for (String sql : migrationSql.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }

            try (var result = statement.executeQuery("SELECT premium_expired_at FROM user_quotas WHERE account_id = 1")) {
                result.next();
                assertEquals(Timestamp.valueOf(future), result.getTimestamp(1));
            }
            try (var columns = connection.getMetaData().getColumns(null, schema, "user_quotas", "premium_recommendation_visible")) {
                assertFalse(columns.next());
            }
        } finally {
            try (var connection = DriverManager.getConnection(url, username, password);
                 var statement = connection.createStatement()) {
                statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            }
        }
    }
}
