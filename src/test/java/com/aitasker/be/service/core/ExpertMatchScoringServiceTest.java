package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.PortfolioEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpertMatchScoringServiceTest {
    private final ExpertMatchScoringService service = new ExpertMatchScoringService();

    @Test
    void score_shouldRejectMissingMandatorySkillEvenWhenSubstringLooksSimilar() {
        var result = service.score(portfolio("20", "7", "9"), requirements(), null, false);
        assertTrue(result.isEmpty());
    }

    @Test
    void score_shouldNormalizeOnlyActiveTaxonomyComponents() {
        var result = service.score(portfolio("2,3", "7", "9"), requirements(), null, false).orElseThrow();
        assertEquals(100.0, result.score());
        assertEquals(Set.of("RAG Architecture", "API Testing"), Set.copyOf(result.matchedSkills()));
    }

    private ResolvedJobRequirements requirements() {
        return new ResolvedJobRequirements(
                Map.of(2, "RAG Architecture", 3, "API Testing"),
                Map.of(7, "E-commerce"),
                Map.of(9, "Docker"),
                Set.of(2),
                SowKeywordExtractionResult.builder().build()
        );
    }

    private PortfolioEntity portfolio(String skills, String domains, String technologies) {
        return PortfolioEntity.builder()
                .skillIds(skills)
                .domainIds(domains)
                .technologyIds(technologies)
                .build();
    }
}
