package com.aitasker.be.integration;

import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        "rag.ingest-on-startup=false"
})
class ExpertCandidateRepositoryIntegrationTest {
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void exactCatalogQuery_shouldReturnOnlyExactNumericSkillTokens() {
        var candidates = portfolioRepository.findEligibleCandidatesByCatalogIds("19", "", "", 200);

        assertFalse(candidates.isEmpty());
        assertTrue(candidates.stream().allMatch(candidate -> hasExactId(candidate, 19)));
        assertTrue(portfolioRepository.findEligibleCandidatesByCatalogIds("999999", "", "", 200).isEmpty());
    }

    @Test
    @Transactional
    void exactCatalogQuery_shouldExcludeRejectedAccountAndKyc() {
        var candidate = portfolioRepository.findEligibleCandidatesByCatalogIds("19", "", "", 200).get(0);
        Integer expertId = candidate.getExpertId();
        Integer accountId = jdbcTemplate.queryForObject(
                "SELECT account_id FROM expert_profiles WHERE expert_id = ?",
                Integer.class,
                expertId
        );

        jdbcTemplate.update("UPDATE account SET status = 'Rejected' WHERE account_id = ?", accountId);
        assertTrue(portfolioRepository.findEligibleCandidatesByCatalogIds("19", "", "", 200).stream()
                .noneMatch(item -> expertId.equals(item.getExpertId())));

        jdbcTemplate.update("UPDATE account SET status = 'Approved' WHERE account_id = ?", accountId);
        jdbcTemplate.update("UPDATE expert_profiles SET kyc_status = 'Rejected' WHERE expert_id = ?", expertId);
        assertTrue(portfolioRepository.findEligibleCandidatesByCatalogIds("19", "", "", 200).stream()
                .noneMatch(item -> expertId.equals(item.getExpertId())));
    }

    private boolean hasExactId(PortfolioEntity portfolio, int expectedId) {
        return Arrays.stream(portfolio.getSkillIds().split("[^0-9]+"))
                .filter(token -> !token.isBlank())
                .mapToInt(Integer::parseInt)
                .anyMatch(id -> id == expectedId);
    }
}
