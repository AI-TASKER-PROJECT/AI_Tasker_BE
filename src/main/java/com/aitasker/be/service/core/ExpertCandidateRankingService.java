/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ExpertCandidateRankingService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.candidate.ExpertCandidateResponse;
import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.entity.SkillEntity;
import com.aitasker.be.entity.SowEntity;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.SkillRepository;
import com.aitasker.be.repository.SowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class ExpertCandidateRankingService {
    private static final int MAX_CANDIDATES = 20;

    private final JobRepository jobRepository;
    private final SowRepository sowRepository;
    private final MilestoneRepository milestoneRepository;
    private final PortfolioRepository portfolioRepository;
    private final SkillRepository skillRepository;
    private final DomainRepository domainRepository;
    private final SowKeywordExtractionService sowKeywordExtractionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    // Note: Ham `findTopCandidatesByJobPostingId` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public ExpertCandidateSearchResponse findTopCandidatesByJobPostingId(Integer jobPostingId) {
        JobEntity job = jobRepository.findById(jobPostingId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));

        String sowPayload = buildSowPayload(job);
        SowKeywordExtractionResult keywords = sowKeywordExtractionService.extractKeywordsFromSow(sowPayload);
        CatalogIndex catalogIndex = loadCatalogIndex(keywords);

        List<ExpertCandidateResponse> candidates = findCandidatePortfolios(keywords, catalogIndex).stream()
                .map(portfolio -> rankPortfolio(portfolio, keywords, catalogIndex))
                .flatMap(Optional::stream)
                .sorted(this::compareCandidates)
                .limit(MAX_CANDIDATES)
                .toList();

        return ExpertCandidateSearchResponse.builder()
                .jobPostingId(jobPostingId)
                .keywords(keywords)
                .candidates(candidates)
                .build();
    }

    // Note: Ham `rankPortfolio` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Optional<ExpertCandidateResponse> rankPortfolio(
            PortfolioEntity portfolio,
            SowKeywordExtractionResult keywords,
            CatalogIndex catalogIndex
    ) {
        List<String> matchedSkills = matchedRequirements(
                keywords.getSkills(),
                portfolio.getSkillIds(),
                catalogIndex.skillTextById(),
                catalogIndex.skillIdsByRequirement()
        );
        List<String> matchedDomains = matchedRequirements(
                keywords.getDomains(),
                portfolio.getDomainIds(),
                catalogIndex.domainTextById(),
                catalogIndex.domainIdsByRequirement()
        );

        if (matchedSkills.isEmpty() && matchedDomains.isEmpty()) {
            return Optional.empty();
        }

        int yearsExperience = portfolio.getYearsExperience() == null ? 0 : portfolio.getYearsExperience();
        double skillScore = percent(matchedSkills.size(), sizeOf(keywords.getSkills()));
        double domainScore = percent(matchedDomains.size(), sizeOf(keywords.getDomains()));
        double experienceScore = experienceScore(yearsExperience);
        double certificateScore = hasAnyKeyword(portfolio.getCertificates(), keywords.getKeywords()) ? 100.0 : 0.0;
        double descriptionScore = descriptionScore(portfolio.getSelfDescription(), keywords.getKeywords());
        double matchScore = round2(
                skillScore * 0.5
                        + domainScore * 0.2
                        + experienceScore * 0.15
                        + certificateScore * 0.05
                        + descriptionScore * 0.1
        );

        return Optional.of(ExpertCandidateResponse.builder()
                .expertId(portfolio.getExpertId())
                .portfolioId(portfolio.getPortfolioId())
                .matchScore(matchScore)
                .matchedSkills(matchedSkills)
                .matchedDomains(matchedDomains)
                .yearsExperience(yearsExperience)
                .certificates(portfolio.getCertificates())
                .selfDescription(portfolio.getSelfDescription())
                .build());
    }

    // Note: Ham `findCandidatePortfolios` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<PortfolioEntity> findCandidatePortfolios(
            SowKeywordExtractionResult keywords,
            CatalogIndex catalogIndex
    ) {
        if (isEmpty(keywords.getSkills()) && isEmpty(keywords.getDomains())) {
            return List.of();
        }

        LinkedHashSet<String> queryTokens = new LinkedHashSet<>();
        addQueryTokens(queryTokens, keywords.getSkills());
        addQueryTokens(queryTokens, keywords.getDomains());
        addCatalogIdTokens(queryTokens, catalogIndex.skillIdsByRequirement().values());
        addCatalogIdTokens(queryTokens, catalogIndex.domainIdsByRequirement().values());

        if (queryTokens.isEmpty()) {
            return List.of();
        }

        Map<Integer, PortfolioEntity> portfoliosById = new LinkedHashMap<>();
        for (String token : queryTokens) {
            portfolioRepository.findCandidatesBySkillOrDomainKeyword(token).forEach(portfolio -> {
                if (portfolio.getPortfolioId() != null) {
                    portfoliosById.putIfAbsent(portfolio.getPortfolioId(), portfolio);
                }
            });
        }
        return new ArrayList<>(portfoliosById.values());
    }

    // Note: Ham `loadCatalogIndex` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private CatalogIndex loadCatalogIndex(SowKeywordExtractionResult keywords) {
        Map<Integer, String> skillTextById = skillRepository.findByIsActiveTrueOrderBySkillNameAsc().stream()
                .collect(Collectors.toMap(
                        SkillEntity::getSkillId,
                        this::catalogText,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        Map<Integer, String> domainTextById = domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc().stream()
                .collect(Collectors.toMap(
                        DomainEntity::getDomainId,
                        this::catalogText,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        return new CatalogIndex(
                skillTextById,
                domainTextById,
                resolveCatalogIds(keywords.getSkills(), skillTextById),
                resolveCatalogIds(keywords.getDomains(), domainTextById)
        );
    }

    // Note: Ham `resolveCatalogIds` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Map<String, Set<Integer>> resolveCatalogIds(List<String> requirements, Map<Integer, String> catalogTextById) {
        Map<String, Set<Integer>> idsByRequirement = new LinkedHashMap<>();
        if (requirements == null || requirements.isEmpty()) {
            return idsByRequirement;
        }

        for (String requirement : requirements) {
            Set<Integer> ids = catalogTextById.entrySet().stream()
                    .filter(entry -> textMatchesRequirement(entry.getValue(), requirement))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            idsByRequirement.put(requirement, ids);
        }
        return idsByRequirement;
    }

    // Note: Ham `matchedRequirements` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<String> matchedRequirements(
            List<String> requirements,
            String rawCatalogValue,
            Map<Integer, String> catalogTextById,
            Map<String, Set<Integer>> idsByRequirement
    ) {
        if (requirements == null || requirements.isEmpty()) {
            return List.of();
        }

        Set<Integer> portfolioCatalogIds = parseCatalogIds(rawCatalogValue);
        List<String> matches = new ArrayList<>();
        for (String requirement : requirements) {
            Set<Integer> matchingCatalogIds = idsByRequirement.getOrDefault(requirement, Set.of());
            boolean idMatched = portfolioCatalogIds.stream().anyMatch(matchingCatalogIds::contains);
            boolean rawTextMatched = textMatchesRequirement(rawCatalogValue, requirement);
            boolean resolvedTextMatched = portfolioCatalogIds.stream()
                    .map(catalogTextById::get)
                    .filter(Objects::nonNull)
                    .anyMatch(catalogText -> textMatchesRequirement(catalogText, requirement));

            if (idMatched || rawTextMatched || resolvedTextMatched) {
                matches.add(requirement);
            }
        }
        return matches;
    }

    // Note: Ham `buildSowPayload` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildSowPayload(JobEntity job) {
        List<MilestoneEntity> milestones = milestoneRepository.findByJobIdOrderByOrderIndexAsc(job.getJobId());
        Optional<SowEntity> sow = sowRepository.findByJobId(job.getJobId());
        if (sow.isPresent()) {
            return buildSowPayload(sow.get(), milestones);
        }

        if (job.getStructuredSow() != null && !job.getStructuredSow().isBlank()) {
            return buildLegacySowPayload(job.getStructuredSow(), milestones);
        }

        if (!milestones.isEmpty()) {
            ObjectNode root = objectMapper.createObjectNode();
            root.set("milestones", milestonesToJson(milestones));
            return root.toString();
        }
        return "{}";
    }

    // Note: Ham `buildSowPayload` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildSowPayload(SowEntity sow, List<MilestoneEntity> milestones) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode sowNode = root.putObject("sow");
        putText(sowNode, "title", sow.getTitle());
        putText(sowNode, "overview", sow.getOverview());
        putJsonText(sowNode, "objectives", sow.getObjectives());
        putJsonText(sowNode, "scopeOfWork", sow.getScopeOfWork());
        putJsonText(sowNode, "deliverables", sow.getDeliverable());
        root.set("milestones", milestonesToJson(milestones));
        return root.toString();
    }

    // Note: Ham `buildLegacySowPayload` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildLegacySowPayload(String structuredSow, List<MilestoneEntity> milestones) {
        try {
            JsonNode legacyNode = objectMapper.readTree(structuredSow);
            ObjectNode root = legacyNode.isObject()
                    ? (ObjectNode) legacyNode.deepCopy()
                    : objectMapper.createObjectNode().set("sow", legacyNode);
            if (!milestones.isEmpty()) {
                root.set("milestones", milestonesToJson(milestones));
            }
            return root.toString();
        } catch (JsonProcessingException ex) {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode sowNode = root.putObject("sow");
            sowNode.put("overview", structuredSow);
            if (!milestones.isEmpty()) {
                root.set("milestones", milestonesToJson(milestones));
            }
            return root.toString();
        }
    }

    // Note: Ham `milestonesToJson` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private ArrayNode milestonesToJson(List<MilestoneEntity> milestones) {
        ArrayNode milestonesNode = objectMapper.createArrayNode();
        for (MilestoneEntity milestone : milestones) {
            ObjectNode milestoneNode = objectMapper.createObjectNode();
            putText(milestoneNode, "name", milestone.getMilestoneName());
            putText(milestoneNode, "description", milestone.getDescription());
            milestonesNode.add(milestoneNode);
        }
        return milestonesNode;
    }

    // Note: Ham `putText` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void putText(ObjectNode node, String fieldName, String value) {
        if (value != null && !value.isBlank()) {
            node.put(fieldName, value);
        }
    }

    // Note: Ham `putJsonText` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void putJsonText(ObjectNode node, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        try {
            node.set(fieldName, objectMapper.readTree(value));
        } catch (JsonProcessingException ex) {
            node.put(fieldName, value);
        }
    }

    // Note: Ham `addQueryTokens` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void addQueryTokens(LinkedHashSet<String> queryTokens, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        for (String alias : sowKeywordExtractionService.aliasesForKeywords(keywords)) {
            addQueryToken(queryTokens, alias);
            addQueryToken(queryTokens, sowKeywordExtractionService.normalizeForMatching(alias));
        }
    }

    // Note: Ham `addCatalogIdTokens` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void addCatalogIdTokens(LinkedHashSet<String> queryTokens, Collection<Set<Integer>> catalogIdGroups) {
        for (Set<Integer> catalogIds : catalogIdGroups) {
            for (Integer catalogId : catalogIds) {
                if (catalogId != null) {
                    queryTokens.add(String.valueOf(catalogId));
                }
            }
        }
    }

    // Note: Ham `addQueryToken` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void addQueryToken(LinkedHashSet<String> queryTokens, String token) {
        if (token != null && !token.isBlank()) {
            queryTokens.add(token.trim());
        }
    }

    // Note: Ham `parseCatalogIds` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Set<Integer> parseCatalogIds(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(value.split("[,;\\s]+"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(this::parseInteger)
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Note: Ham `parseInteger` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Optional<Integer> parseInteger(String value) {
        try {
            return Optional.of(Integer.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    // Note: Ham `textMatchesRequirement` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean textMatchesRequirement(String text, String requirement) {
        if (text == null || text.isBlank() || requirement == null || requirement.isBlank()) {
            return false;
        }

        if (sowKeywordExtractionService.textContainsKeyword(text, requirement)) {
            return true;
        }

        String normalizedText = sowKeywordExtractionService.normalizeForMatching(text);
        return catalogRelatedTerms(requirement).stream()
                .map(sowKeywordExtractionService::normalizeForMatching)
                .filter(term -> !term.isBlank())
                .anyMatch(term -> containsNormalizedTerm(normalizedText, term));
    }

    // Note: Ham `catalogRelatedTerms` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<String> catalogRelatedTerms(String requirement) {
        LinkedHashSet<String> terms = new LinkedHashSet<>(sowKeywordExtractionService.aliasesForKeyword(requirement));
        switch (requirement) {
            case "AI" -> terms.addAll(List.of("generative ai", "ai product", "prompt engineering", "llm", "machine learning"));
            case "Chatbot" -> terms.addAll(List.of("generative ai", "llm", "agent", "rag", "natural language", "nlp"));
            case "RAG / Knowledge Base" -> terms.addAll(List.of("rag architecture", "retrieval", "semantic search", "knowledge base design"));
            case "API Integration" -> terms.addAll(List.of("cloud backend", "web platform", "rest api", "api testing", "payos integration"));
            case "Spring Boot" -> terms.addAll(List.of("java spring boot", "java backend"));
            case "NLP" -> terms.addAll(List.of("natural language processing", "semantic search", "text classification", "summarization"));
            case "Testing" -> terms.addAll(List.of("api testing", "model evaluation", "quality metrics", "regression"));
            case "Deployment" -> terms.addAll(List.of("mlops", "model operations", "docker", "devops", "ci cd", "deployment packaging"));
            case "Customer Support" -> terms.addAll(List.of("customer experience", "support workflow", "assistant"));
            case "E-commerce" -> terms.addAll(List.of("retail tech", "catalog", "recommendation", "customer experience"));
            case "CRM" -> terms.addAll(List.of("customer relationship", "customer experience"));
            case "Order Management" -> terms.addAll(List.of("order management", "transaction flow", "workflow automation"));
            default -> {
            }
        }
        return List.copyOf(terms);
    }

    // Note: Ham `containsNormalizedTerm` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean containsNormalizedTerm(String normalizedText, String normalizedTerm) {
        return (" " + normalizedText + " ").contains(" " + normalizedTerm + " ");
    }

    // Note: Ham `hasAnyKeyword` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean hasAnyKeyword(String text, List<String> keywords) {
        return keywords != null && keywords.stream()
                .anyMatch(keyword -> sowKeywordExtractionService.textContainsKeyword(text, keyword));
    }

    // Note: Ham `descriptionScore` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private double descriptionScore(String selfDescription, List<String> keywords) {
        int keywordCount = sizeOf(keywords);
        if (keywordCount == 0) {
            return 0.0;
        }
        long matchedCount = sowKeywordExtractionService.countKeywordsInText(selfDescription, keywords);
        return percent((int) matchedCount, keywordCount);
    }

    // Note: Ham `experienceScore` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private double experienceScore(int yearsExperience) {
        if (yearsExperience >= 5) {
            return 100.0;
        }
        if (yearsExperience >= 3) {
            return 80.0;
        }
        if (yearsExperience >= 1) {
            return 50.0;
        }
        return 20.0;
    }

    // Note: Ham `percent` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private double percent(int matchedCount, int totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return matchedCount * 100.0 / totalCount;
    }

    // Note: Ham `round2` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Note: Ham `compareCandidates` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int compareCandidates(ExpertCandidateResponse first, ExpertCandidateResponse second) {
        int scoreCompare = Double.compare(second.getMatchScore(), first.getMatchScore());
        if (scoreCompare != 0) {
            return scoreCompare;
        }

        int experienceCompare = Integer.compare(nullToZero(second.getYearsExperience()), nullToZero(first.getYearsExperience()));
        if (experienceCompare != 0) {
            return experienceCompare;
        }

        return Integer.compare(nullToMax(first.getPortfolioId()), nullToMax(second.getPortfolioId()));
    }

    // Note: Ham `nullToZero` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    // Note: Ham `nullToMax` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int nullToMax(Integer value) {
        return value == null ? Integer.MAX_VALUE : value;
    }

    // Note: Ham `sizeOf` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int sizeOf(Collection<?> values) {
        return values == null ? 0 : values.size();
    }

    // Note: Ham `isEmpty` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean isEmpty(Collection<?> values) {
        return values == null || values.isEmpty();
    }

    // Note: Ham `catalogText` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String catalogText(SkillEntity skill) {
        return joinText(skill.getSkillId(), skill.getSkillCode(), skill.getSkillName(), skill.getDescription());
    }

    // Note: Ham `catalogText` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String catalogText(DomainEntity domain) {
        return joinText(domain.getDomainId(), domain.getDomainCode(), domain.getDomainName(), domain.getDescription());
    }

    // Note: Ham `joinText` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String joinText(Object... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    private record CatalogIndex(
            Map<Integer, String> skillTextById,
            Map<Integer, String> domainTextById,
            Map<String, Set<Integer>> skillIdsByRequirement,
            Map<String, Set<Integer>> domainIdsByRequirement
    ) {
    }
}
