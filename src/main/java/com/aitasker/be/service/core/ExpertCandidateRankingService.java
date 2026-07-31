package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.candidate.ExpertCandidateResponse;
import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpertCandidateRankingService {
    private static final int MAX_RETRIEVED_CANDIDATES = 200;
    private static final int MAX_RANKED_CANDIDATES = 20;

    private final JobRepository jobRepository;
    private final PortfolioRepository portfolioRepository;
    private final ReviewRepository reviewRepository;
    private final JobRequirementResolver jobRequirementResolver;
    private final ExpertMatchScoringService scoringService;

    @Transactional(readOnly = true)
    public ExpertCandidateSearchResponse findTopCandidatesByJobPostingId(Integer jobPostingId) {
        JobEntity job = jobRepository.findById(jobPostingId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        ResolvedJobRequirements requirements = jobRequirementResolver.resolve(job);

        if (requirements.isEmpty()) {
            return response(jobPostingId, requirements, List.of());
        }

        List<PortfolioEntity> portfolios = portfolioRepository.findEligibleCandidatesByCatalogIds(
                csv(requirements.skillIds()),
                csv(requirements.domainIds()),
                csv(requirements.technologyIds()),
                MAX_RETRIEVED_CANDIDATES
        );
        Map<Integer, BigDecimal> ratingsByExpertId = loadRatings(portfolios);
        boolean ratingComponentActive = !ratingsByExpertId.isEmpty();

        List<ExpertCandidateResponse> candidates = portfolios.stream()
                .map(portfolio -> scoringService.score(
                                portfolio,
                                requirements,
                                ratingsByExpertId.get(portfolio.getExpertId()),
                                ratingComponentActive
                        )
                        .map(score -> toResponse(portfolio, score))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .sorted(this::compareCandidates)
                .limit(MAX_RANKED_CANDIDATES)
                .toList();

        return response(jobPostingId, requirements, candidates);
    }

    private ExpertCandidateSearchResponse response(
            Integer jobPostingId,
            ResolvedJobRequirements requirements,
            List<ExpertCandidateResponse> candidates
    ) {
        return ExpertCandidateSearchResponse.builder()
                .jobPostingId(jobPostingId)
                .keywords(requirements.keywordSummary())
                .candidates(candidates)
                .build();
    }

    private Map<Integer, BigDecimal> loadRatings(List<PortfolioEntity> portfolios) {
        List<Integer> expertIds = portfolios.stream()
                .map(PortfolioEntity::getExpertId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (expertIds.isEmpty()) {
            return Map.of();
        }

        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        for (ReviewRepository.ExpertRatingProjection row : reviewRepository.findAverageRatingsByExpertIds(expertIds)) {
            if (row.getExpertId() != null && row.getAverageRating() != null) {
                result.put(row.getExpertId(), row.getAverageRating());
            }
        }
        return result;
    }

    private ExpertCandidateResponse toResponse(
            PortfolioEntity portfolio,
            ExpertMatchScoringService.ScoredPortfolio score
    ) {
        return ExpertCandidateResponse.builder()
                .expertId(portfolio.getExpertId())
                .portfolioId(portfolio.getPortfolioId())
                .matchScore(score.score())
                .matchedSkills(score.matchedSkills())
                .matchedDomains(score.matchedDomains())
                .yearsExperience(portfolio.getYearsExperience() == null ? 0 : portfolio.getYearsExperience())
                .certificates(portfolio.getCertificates())
                .selfDescription(portfolio.getSelfDescription())
                .build();
    }

    private int compareCandidates(ExpertCandidateResponse left, ExpertCandidateResponse right) {
        int scoreComparison = Double.compare(value(right.getMatchScore()), value(left.getMatchScore()));
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        int experienceComparison = Integer.compare(value(right.getYearsExperience()), value(left.getYearsExperience()));
        if (experienceComparison != 0) {
            return experienceComparison;
        }
        return Integer.compare(value(left.getExpertId()), value(right.getExpertId()));
    }

    private String csv(Collection<Integer> values) {
        return values.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
    }

    private double value(Double value) {
        return value == null ? 0.0 : value;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
