package com.aitasker.be.service.core;

import com.aitasker.be.entity.PortfolioEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ExpertMatchScoringService {
    private static final double SKILL_WEIGHT = 45.0;
    private static final double DOMAIN_WEIGHT = 25.0;
    private static final double TECHNOLOGY_WEIGHT = 15.0;
    private static final double RATING_WEIGHT = 5.0;

    public Optional<ScoredPortfolio> score(
            PortfolioEntity portfolio,
            ResolvedJobRequirements requirements,
            BigDecimal averageRating,
            boolean ratingComponentActive
    ) {
        Set<Integer> portfolioSkillIds = parseCatalogIds(portfolio.getSkillIds());
        Set<Integer> portfolioDomainIds = parseCatalogIds(portfolio.getDomainIds());
        Set<Integer> portfolioTechnologyIds = parseCatalogIds(portfolio.getTechnologyIds());

        if (!portfolioSkillIds.containsAll(requirements.mandatorySkillIds())) {
            return Optional.empty();
        }

        Set<Integer> matchedSkillIds = intersection(portfolioSkillIds, requirements.skillsById().keySet());
        Set<Integer> matchedDomainIds = intersection(portfolioDomainIds, requirements.domainsById().keySet());
        Set<Integer> matchedTechnologyIds = intersection(
                portfolioTechnologyIds,
                requirements.technologiesById().keySet()
        );
        if (matchedSkillIds.isEmpty() && matchedDomainIds.isEmpty() && matchedTechnologyIds.isEmpty()) {
            return Optional.empty();
        }

        double weightedScore = 0.0;
        double activeWeight = 0.0;
        if (!requirements.skillsById().isEmpty()) {
            weightedScore += coverage(matchedSkillIds.size(), requirements.skillsById().size()) * SKILL_WEIGHT;
            activeWeight += SKILL_WEIGHT;
        }
        if (!requirements.domainsById().isEmpty()) {
            weightedScore += coverage(matchedDomainIds.size(), requirements.domainsById().size()) * DOMAIN_WEIGHT;
            activeWeight += DOMAIN_WEIGHT;
        }
        if (!requirements.technologiesById().isEmpty()) {
            weightedScore += coverage(matchedTechnologyIds.size(), requirements.technologiesById().size())
                    * TECHNOLOGY_WEIGHT;
            activeWeight += TECHNOLOGY_WEIGHT;
        }
        if (ratingComponentActive) {
            double normalizedRating = averageRating == null
                    ? 0.0
                    : Math.max(0.0, Math.min(1.0, averageRating.doubleValue() / 5.0));
            weightedScore += normalizedRating * RATING_WEIGHT;
            activeWeight += RATING_WEIGHT;
        }

        double score = activeWeight == 0.0 ? 0.0 : round2(weightedScore / activeWeight * 100.0);
        return Optional.of(new ScoredPortfolio(
                score,
                labels(matchedSkillIds, requirements.skillsById()),
                labels(matchedDomainIds, requirements.domainsById()),
                labels(matchedTechnologyIds, requirements.technologiesById())
        ));
    }

    Set<Integer> parseCatalogIds(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<Integer> ids = new LinkedHashSet<>();
        Arrays.stream(value.split("[^0-9]+"))
                .filter(token -> !token.isBlank() && token.length() <= 9)
                .forEach(token -> {
                    try {
                        ids.add(Integer.valueOf(token));
                    } catch (NumberFormatException ignored) {
                        // Invalid legacy token is intentionally ignored.
                    }
                });
        return ids;
    }

    private Set<Integer> intersection(Set<Integer> actual, Set<Integer> required) {
        Set<Integer> result = new LinkedHashSet<>(required);
        result.retainAll(actual);
        return result;
    }

    private List<String> labels(Set<Integer> ids, java.util.Map<Integer, String> labelsById) {
        return ids.stream().map(labelsById::get).filter(java.util.Objects::nonNull).toList();
    }

    private double coverage(int matched, int required) {
        return required == 0 ? 0.0 : (double) matched / required;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public record ScoredPortfolio(
            double score,
            List<String> matchedSkills,
            List<String> matchedDomains,
            List<String> matchedTechnologies
    ) {
    }
}
