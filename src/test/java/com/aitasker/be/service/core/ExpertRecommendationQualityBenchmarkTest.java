package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.PortfolioEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpertRecommendationQualityBenchmarkTest {
    private final ExpertMatchScoringService scoringService = new ExpertMatchScoringService();

    @ParameterizedTest(name = "{0}")
    @MethodSource("groups")
    void benchmark_shouldMeetRecallPrecisionAndNdcgTargets(
            String group,
            int skillId,
            int domainId,
            int technologyId
    ) {
        ResolvedJobRequirements requirements = new ResolvedJobRequirements(
                Map.of(skillId, group + " skill"),
                Map.of(domainId, group + " domain"),
                Map.of(technologyId, group + " technology"),
                Set.of(skillId),
                SowKeywordExtractionResult.builder().build()
        );
        List<LabeledScore> ranked = new ArrayList<>();
        IntStream.range(0, 5).forEach(index -> score(ranked, portfolio(skillId, domainId, technologyId), requirements, true));
        IntStream.range(0, 5).forEach(index -> score(ranked, portfolio(900 + index, 800 + index, 700 + index), requirements, false));
        ranked.sort(java.util.Comparator.comparingDouble(LabeledScore::score).reversed());

        long relevantRetrieved = ranked.stream().limit(20).filter(LabeledScore::relevant).count();
        long relevantTopFive = ranked.stream().limit(5).filter(LabeledScore::relevant).count();
        double recallAt20 = relevantRetrieved / 5.0;
        double precisionAt5 = relevantTopFive / 5.0;
        double dcg = IntStream.range(0, Math.min(5, ranked.size()))
                .mapToDouble(index -> ranked.get(index).relevant() ? 1.0 / log2(index + 2) : 0.0)
                .sum();
        double idealDcg = IntStream.range(0, 5).mapToDouble(index -> 1.0 / log2(index + 2)).sum();

        assertEquals(1.0, recallAt20);
        assertEquals(1.0, precisionAt5);
        assertEquals(1.0, dcg / idealDcg, 0.0001);
    }

    private void score(
            List<LabeledScore> scores,
            PortfolioEntity portfolio,
            ResolvedJobRequirements requirements,
            boolean relevant
    ) {
        scoringService.score(portfolio, requirements, null, false)
                .ifPresent(result -> scores.add(new LabeledScore(result.score(), relevant)));
    }

    private PortfolioEntity portfolio(int skillId, int domainId, int technologyId) {
        return PortfolioEntity.builder()
                .skillIds(String.valueOf(skillId))
                .domainIds(String.valueOf(domainId))
                .technologyIds(String.valueOf(technologyId))
                .build();
    }

    private double log2(int value) {
        return Math.log(value) / Math.log(2.0);
    }

    private static Stream<Arguments> groups() {
        return Stream.of(
                Arguments.of("API Testing", 19, 9, 2),
                Arguments.of("BI Dashboard", 13, 6, 7),
                Arguments.of("Computer Vision", 15, 4, 1),
                Arguments.of("Data Pipeline", 12, 5, 7),
                Arguments.of("Generic AI", 1, 2, 11),
                Arguments.of("Chatbot RAG", 2, 2, 11)
        );
    }

    private record LabeledScore(double score, boolean relevant) {
    }
}
