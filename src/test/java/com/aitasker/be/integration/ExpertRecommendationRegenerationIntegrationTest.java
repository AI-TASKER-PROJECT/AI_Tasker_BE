package com.aitasker.be.integration;

import com.aitasker.be.dto.candidate.ExpertRecommendationListResponse;
import com.aitasker.be.service.core.ExpertRecommendationService;
import com.aitasker.be.service.core.PaymentWalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.config.import=",
        "DB_HOST=127.0.0.1",
        "DB_PORT=5433",
        "DB_NAME=${TEST_DB_NAME:aitasker_db}",
        "DB_USER=aitasker",
        "DB_PASSWORD=aitasker123",
        "APP_JWT_SECRET=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "app.jwt.secret=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh}",
        "spring.datasource.username=aitasker",
        "spring.datasource.password=aitasker123",
        "openai.api-key=",
        "rag.ingest-on-startup=false"
})
class ExpertRecommendationRegenerationIntegrationTest {
    private static final int JOB_ID = 1_600_001;
    private static final int MANDATORY_SKILL_ID = 1_600_101;
    private static final int OPTIONAL_SKILL_ID = 1_600_102;
    private static final int DOMAIN_ID = 1_600_201;
    private static final int TECHNOLOGY_ID = 1_600_301;

    private static final int BASELINE_ACCOUNT_ID = 1_600_401;
    private static final int BASELINE_EXPERT_ID = 1_600_411;
    private static final int BASELINE_PORTFOLIO_ID = 1_600_421;

    private static final int STRONG_ACCOUNT_ID = 1_600_501;
    private static final int STRONG_EXPERT_ID = 1_600_511;
    private static final int STRONG_PORTFOLIO_ID = 1_600_521;

    @Autowired private ExpertRecommendationService recommendationService;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean private PaymentWalletService paymentWalletService;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM expert_recommendations WHERE job_posting_id = ?", JOB_ID);
        jdbcTemplate.update("DELETE FROM jobs WHERE job_id = ?", JOB_ID);
        jdbcTemplate.update("DELETE FROM portfolios WHERE portfolio_id IN (?, ?)", BASELINE_PORTFOLIO_ID, STRONG_PORTFOLIO_ID);
        jdbcTemplate.update("DELETE FROM expert_profiles WHERE expert_id IN (?, ?)", BASELINE_EXPERT_ID, STRONG_EXPERT_ID);
        jdbcTemplate.update("DELETE FROM account WHERE account_id IN (?, ?)", BASELINE_ACCOUNT_ID, STRONG_ACCOUNT_ID);
        jdbcTemplate.update("DELETE FROM technologies WHERE technology_id = ?", TECHNOLOGY_ID);
        jdbcTemplate.update("DELETE FROM domains WHERE domain_id = ?", DOMAIN_ID);
        jdbcTemplate.update("DELETE FROM skills WHERE skill_id IN (?, ?)", MANDATORY_SKILL_ID, OPTIONAL_SKILL_ID);
    }

    @Test
    void regenerate_shouldAddNewHigherCompatibilityExpertToTopRecommendations() {
        createJobRequirements();
        createExpert(
                BASELINE_ACCOUNT_ID,
                BASELINE_EXPERT_ID,
                BASELINE_PORTFOLIO_ID,
                "baseline.regeneration@aitasker.test",
                String.valueOf(MANDATORY_SKILL_ID),
                "",
                "",
                3
        );

        ExpertRecommendationListResponse firstGeneration = recommendationService.generateRecommendations((long) JOB_ID);

        assertEquals(1, firstGeneration.getRecommendations().size());
        assertEquals((long) BASELINE_EXPERT_ID, firstGeneration.getRecommendations().getFirst().getExpertId());
        double baselineScore = firstGeneration.getRecommendations().getFirst().getMatchScore();

        createExpert(
                STRONG_ACCOUNT_ID,
                STRONG_EXPERT_ID,
                STRONG_PORTFOLIO_ID,
                "strong.regeneration@aitasker.test",
                MANDATORY_SKILL_ID + "," + OPTIONAL_SKILL_ID,
                String.valueOf(DOMAIN_ID),
                String.valueOf(TECHNOLOGY_ID),
                10
        );

        ExpertRecommendationListResponse secondGeneration = assertDoesNotThrow(
                () -> recommendationService.generateRecommendations((long) JOB_ID),
                "Regeneration must replace the previous persisted snapshot without violating V58 uniqueness constraints"
        );

        assertEquals((long) STRONG_EXPERT_ID, secondGeneration.getRecommendations().getFirst().getExpertId());
        assertEquals(100.0, secondGeneration.getRecommendations().getFirst().getMatchScore());
        assertTrue(secondGeneration.getRecommendations().getFirst().getMatchScore() > baselineScore);
        assertEquals(2, secondGeneration.getRecommendations().size());

        List<Integer> persistedExpertIds = jdbcTemplate.queryForList(
                """
                        SELECT expert_id
                        FROM expert_recommendations
                        WHERE job_posting_id = ?
                        ORDER BY rank_position
                        """,
                Integer.class,
                JOB_ID
        );
        assertEquals(List.of(STRONG_EXPERT_ID, BASELINE_EXPERT_ID), persistedExpertIds);
    }

    private void createJobRequirements() {
        Integer businessId = jdbcTemplate.queryForObject("""
                SELECT bp.business_id
                FROM business_profiles bp
                JOIN account a ON a.account_id=bp.account_id
                WHERE a.email='business@aitasker.local'
                """, Integer.class);
        jdbcTemplate.update("""
                INSERT INTO skills (
                    skill_id, skill_code, skill_name, description, is_active, created_at, updated_at
                ) VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, MANDATORY_SKILL_ID, "REGEN_REQUIRED", "Regeneration Required Skill", "Required fixture skill");
        jdbcTemplate.update("""
                INSERT INTO skills (
                    skill_id, skill_code, skill_name, description, is_active, created_at, updated_at
                ) VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, OPTIONAL_SKILL_ID, "REGEN_OPTIONAL", "Regeneration Optional Skill", "Optional fixture skill");
        jdbcTemplate.update("""
                INSERT INTO domains (
                    domain_id, domain_code, domain_name, description, is_active, sort_order, created_at, updated_at
                ) VALUES (?, ?, ?, ?, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, DOMAIN_ID, "REGEN_DOMAIN", "Regeneration Test Domain", "Domain fixture");
        jdbcTemplate.update("""
                INSERT INTO technologies (
                    technology_id, technology_code, technology_name, description, is_active, sort_order, created_at, updated_at
                ) VALUES (?, ?, ?, ?, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, TECHNOLOGY_ID, "REGEN_TECH", "Regeneration Test Technology", "Technology fixture");
        jdbcTemplate.update("""
                INSERT INTO jobs (
                    job_id, business_id, title, raw_requirements, budget, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, 1000000, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, JOB_ID, businessId, "Expert recommendation regeneration test", "Test deterministic recommendation regeneration");
        jdbcTemplate.update(
                "INSERT INTO job_skills (job_id, skill_id, is_mandatory, created_at) VALUES (?, ?, TRUE, CURRENT_TIMESTAMP)",
                JOB_ID,
                MANDATORY_SKILL_ID
        );
        jdbcTemplate.update(
                "INSERT INTO job_skills (job_id, skill_id, is_mandatory, created_at) VALUES (?, ?, FALSE, CURRENT_TIMESTAMP)",
                JOB_ID,
                OPTIONAL_SKILL_ID
        );
        jdbcTemplate.update(
                "INSERT INTO job_domains (job_id, domain_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                JOB_ID,
                DOMAIN_ID
        );
        jdbcTemplate.update(
                "INSERT INTO job_technologies (job_id, technology_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                JOB_ID,
                TECHNOLOGY_ID
        );
    }

    private void createExpert(
            int accountId,
            int expertId,
            int portfolioId,
            String email,
            String skillIds,
            String domainIds,
            String technologyIds,
            int yearsExperience
    ) {
        jdbcTemplate.update("""
                INSERT INTO account (
                    account_id, email, password, full_name, role_id, status, email_verified, created_at, updated_at
                ) VALUES (?, ?, 'test-password', ?, 2, 'Approved', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, accountId, email, "Regeneration Expert " + expertId);
        jdbcTemplate.update("""
                INSERT INTO expert_profiles (
                    expert_id, account_id, national_id, portfolio_url, years_of_experience,
                    kyc_status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, 'Approved', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                expertId,
                accountId,
                "REGEN-" + expertId,
                "https://portfolio.aitasker.test/experts/" + expertId,
                yearsExperience
        );
        jdbcTemplate.update("""
                INSERT INTO portfolios (
                    portfolio_id, expert_id, domain_ids, skill_ids, technology_ids, years_experience,
                    certificates, self_description, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, '', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                portfolioId,
                expertId,
                domainIds,
                skillIds,
                technologyIds,
                yearsExperience,
                "Integration fixture for recommendation regeneration"
        );
    }
}
