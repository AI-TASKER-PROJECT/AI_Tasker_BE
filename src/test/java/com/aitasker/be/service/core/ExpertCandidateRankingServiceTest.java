package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertCandidateRankingServiceTest {
    @Mock private JobRepository jobRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private JobRequirementResolver jobRequirementResolver;

    private ExpertCandidateRankingService service;

    @BeforeEach
    void setUp() {
        service = new ExpertCandidateRankingService(
                jobRepository,
                portfolioRepository,
                reviewRepository,
                jobRequirementResolver,
                new ExpertMatchScoringService()
        );
    }

    @Test
    void findTopCandidates_shouldUseOneExactIdQueryAndApplyMandatorySkillGate() {
        JobEntity job = JobEntity.builder().jobId(1).build();
        ResolvedJobRequirements requirements = requirements();
        PortfolioEntity strong = portfolio(10, 100, "2,19", "17", "9", 5);
        PortfolioEntity substringCollision = portfolio(11, 101, "20,190", "17", "9", 8);

        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(jobRequirementResolver.resolve(job)).thenReturn(requirements);
        when(portfolioRepository.findEligibleCandidatesByCatalogIds("2,19", "17", "9", 200))
                .thenReturn(List.of(strong, substringCollision));
        when(reviewRepository.findAverageRatingsByExpertIds(List.of(100, 101))).thenReturn(List.of());

        ExpertCandidateSearchResponse response = service.findTopCandidatesByJobPostingId(1);

        assertEquals(1, response.getCandidates().size());
        assertEquals(100, response.getCandidates().get(0).getExpertId());
        assertEquals(100.0, response.getCandidates().get(0).getMatchScore());
        assertTrue(response.getCandidates().get(0).getMatchedSkills().contains("RAG Architecture"));
        verify(portfolioRepository).findEligibleCandidatesByCatalogIds("2,19", "17", "9", 200);
    }

    private ResolvedJobRequirements requirements() {
        Map<Integer, String> skills = new LinkedHashMap<>();
        skills.put(2, "RAG Architecture");
        skills.put(19, "API Testing & Swagger");
        return new ResolvedJobRequirements(
                skills,
                Map.of(17, "E-commerce & Retail Tech"),
                Map.of(9, "Docker"),
                Set.of(2),
                SowKeywordExtractionResult.builder()
                        .skills(List.copyOf(skills.values()))
                        .domains(List.of("E-commerce & Retail Tech"))
                        .keywords(List.of("RAG Architecture", "API Testing & Swagger", "E-commerce & Retail Tech", "Docker"))
                        .build()
        );
    }

    private PortfolioEntity portfolio(
            int portfolioId,
            int expertId,
            String skillIds,
            String domainIds,
            String technologyIds,
            int years
    ) {
        return PortfolioEntity.builder()
                .portfolioId(portfolioId)
                .expertId(expertId)
                .skillIds(skillIds)
                .domainIds(domainIds)
                .technologyIds(technologyIds)
                .yearsExperience(years)
                .certificates("")
                .selfDescription("Relevant delivery experience")
                .build();
    }
}
