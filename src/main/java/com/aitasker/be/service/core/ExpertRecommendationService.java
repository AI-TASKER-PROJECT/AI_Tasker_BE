package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.recommendation.AiExpertRankingResponseDto;
import com.aitasker.be.dto.recommendation.ExpertCandidateDto;
import com.aitasker.be.dto.recommendation.ExpertRecommendationItemDto;
import com.aitasker.be.dto.recommendation.ExpertRecommendationResponse;
import com.aitasker.be.dto.recommendation.RecommendExpertsRequest;
import com.aitasker.be.dto.recommendation.SowKeywordsDto;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.ExpertRecommendationEntity;
import com.aitasker.be.entity.JobDomainEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.JobSkillEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.entity.SkillEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ContractRepository;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.ExpertRecommendationRepository;
import com.aitasker.be.repository.JobDomainRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.JobSkillRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.ReviewRepository;
import com.aitasker.be.repository.SkillRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertRecommendationService {
    private static final int DEFAULT_CANDIDATE_LIMIT = 30;
    private static final int MAX_CANDIDATE_LIMIT = 50;
    private static final int TOP_RECOMMENDATION_LIMIT = 5;
    private static final Set<String> STOP_WORDS = Set.of(
            "and", "for", "the", "with", "from", "into", "onto", "this", "that",
            "project", "system", "solution", "build", "create", "develop", "design",
            "implementation", "application", "applications", "service", "services",
            "business", "company", "expert", "specialist", "support", "scope"
    );

    private final AccessService accessService;
    private final AiExpertRecommendationService aiExpertRecommendationService;
    private final JobRepository jobRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobDomainRepository jobDomainRepository;
    private final SkillRepository skillRepository;
    private final DomainRepository domainRepository;
    private final PortfolioRepository portfolioRepository;
    private final AccountRepository accountRepository;
    private final ContractRepository contractRepository;
    private final ReviewRepository reviewRepository;
    private final ExpertRecommendationRepository expertRecommendationRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExpertRecommendationResponse recommendExperts(Integer jobId, RecommendExpertsRequest request) {
        JobEntity job = requireBusinessOwnedJob(jobId);
        String sowSummary = buildSowSummary(job);
        SowKeywordsDto keywords = aiExpertRecommendationService.extractKeywords(sowSummary);

        List<JobSkillEntity> jobSkills = jobSkillRepository.findByIdJobId(jobId);
        List<JobDomainEntity> jobDomains = jobDomainRepository.findByIdJobId(jobId);
        Set<Integer> skillIds = resolveSkillIds(jobSkills, keywords);
        Set<Integer> domainIds = resolveDomainIds(jobDomains, keywords);
        List<String> textTerms = resolveTextFilterTerms(keywords);
        int minYears = resolveMinYears(jobSkills, keywords);
        int candidateLimit = resolveCandidateLimit(request);

        List<ExpertProfileEntity> expertCandidates = findCandidateExperts(skillIds, domainIds, textTerms, minYears, candidateLimit);
        if (expertCandidates.isEmpty() && minYears > 0) {
            expertCandidates = findCandidateExperts(skillIds, domainIds, textTerms, 0, candidateLimit);
        }

        if (expertCandidates.isEmpty()) {
            expertRecommendationRepository.deleteByJobId(jobId);
            return ExpertRecommendationResponse.builder()
                    .jobId(jobId)
                    .sowSummary(sowSummary)
                    .extractedKeywords(keywords)
                    .candidateCount(0)
                    .note("Khong co expert phu hop sau khi backend filter theo skill/domain/keyword. Can mo rong keyword hoac cap nhat portfolio expert.")
                    .recommendations(List.of())
                    .build();
        }

        List<ExpertCandidateDto> candidates = buildCandidateDtos(expertCandidates);
        AiExpertRankingResponseDto aiResponse = aiExpertRecommendationService.rankExperts(sowSummary, keywords, candidates);
        List<ExpertRecommendationItemDto> recommendations = sanitizeRecommendations(aiResponse, candidates);
        String note = buildNote(aiResponse.getNote(), candidates.size(), recommendations.size());

        saveLatestRecommendations(jobId, keywords, candidates.size(), note, recommendations);
        return ExpertRecommendationResponse.builder()
                .jobId(jobId)
                .sowSummary(sowSummary)
                .extractedKeywords(keywords)
                .candidateCount(candidates.size())
                .note(note)
                .recommendations(recommendations)
                .build();
    }

    private JobEntity requireBusinessOwnedJob(Integer jobId) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();

        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        AccountEntity actor = accessService.currentAccount();
        BusinessProfileEntity business = businessProfileRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!"Approved".equalsIgnoreCase(business.getKybStatus())) {
            throw new AppException("BUSINESS PROFILE CHUA DUOC KYB APPROVED");
        }
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new ForbiddenException("BAN KHONG CO QUYEN XEM RECOMMENDATION CUA JOB NAY");
        }
        return job;
    }

    private String buildSowSummary(JobEntity job) {
        String source = hasText(job.getStructuredSow()) ? job.getStructuredSow() : job.getRawRequirements();
        String sourceLabel = hasText(job.getStructuredSow()) ? "Structured SoW" : "Raw hiring request";
        return """
                Project title: %s
                Source type: %s
                Source content: %s
                Budget: %s VND
                Timeline: %s %s
                AI tag: %s
                """.formatted(
                nullToEmpty(job.getTitle()),
                sourceLabel,
                truncate(nullToEmpty(source), 8000),
                job.getBudget() == null ? "" : job.getBudget(),
                job.getPlannedDurationValue() == null ? "" : job.getPlannedDurationValue(),
                nullToEmpty(job.getPlannedDurationUnit()),
                nullToEmpty(job.getAiTag())
        );
    }

    private Set<Integer> resolveSkillIds(List<JobSkillEntity> jobSkills, SowKeywordsDto keywords) {
        Set<Integer> ids = jobSkills.stream()
                .map(JobSkillEntity::getId)
                .filter(Objects::nonNull)
                .map(id -> id.getSkillId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> terms = new ArrayList<>();
        terms.addAll(defaultList(keywords.getRequiredSkills()));
        terms.addAll(defaultList(keywords.getProjectScope()));
        terms.addAll(defaultList(keywords.getDeliverables()));
        terms.addAll(defaultList(keywords.getKeywords()));

        for (SkillEntity skill : skillRepository.findByIsActiveTrueOrderBySkillNameAsc()) {
            if (matchesCatalog(terms, skill.getSkillCode(), skill.getSkillName(), skill.getDescription())) {
                ids.add(skill.getSkillId());
            }
        }
        return ids;
    }

    private Set<Integer> resolveDomainIds(List<JobDomainEntity> jobDomains, SowKeywordsDto keywords) {
        Set<Integer> ids = jobDomains.stream()
                .map(JobDomainEntity::getId)
                .filter(Objects::nonNull)
                .map(id -> id.getDomainId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> terms = new ArrayList<>();
        terms.addAll(defaultList(keywords.getDomains()));
        terms.addAll(defaultList(keywords.getIndustries()));
        terms.addAll(defaultList(keywords.getProjectScope()));
        terms.addAll(defaultList(keywords.getKeywords()));

        for (DomainEntity domain : domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc()) {
            if (matchesCatalog(terms, domain.getDomainCode(), domain.getDomainName(), domain.getDescription())) {
                ids.add(domain.getDomainId());
            }
        }
        return ids;
    }

    private int resolveMinYears(List<JobSkillEntity> jobSkills, SowKeywordsDto keywords) {
        int minYearsFromJob = jobSkills.stream()
                .map(JobSkillEntity::getMinYearsExperience)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        String level = normalizeText(keywords.getExperienceLevel());
        int minYearsFromAi = 0;
        if (level.contains("lead") || level.contains("principal")) {
            minYearsFromAi = 8;
        } else if (level.contains("senior")) {
            minYearsFromAi = 5;
        } else if (level.contains("middle") || level.contains("mid")) {
            minYearsFromAi = 2;
        }

        return Math.max(minYearsFromJob, minYearsFromAi);
    }

    private List<String> resolveTextFilterTerms(SowKeywordsDto keywords) {
        List<String> terms = new ArrayList<>();
        terms.addAll(defaultList(keywords.getRequiredSkills()));
        terms.addAll(defaultList(keywords.getDomains()));
        terms.addAll(defaultList(keywords.getIndustries()));
        terms.addAll(defaultList(keywords.getProjectScope()));
        terms.addAll(defaultList(keywords.getDeliverables()));
        terms.addAll(defaultList(keywords.getKeywords()));

        return terms.stream()
                .map(this::normalizeText)
                .filter(term -> term.length() >= 3 && term.length() <= 80)
                .filter(term -> !STOP_WORDS.contains(term))
                .distinct()
                .limit(8)
                .toList();
    }

    private List<ExpertProfileEntity> findCandidateExperts(
            Set<Integer> skillIds,
            Set<Integer> domainIds,
            List<String> textTerms,
            int minYears,
            int limit
    ) {
        if (skillIds.isEmpty() && domainIds.isEmpty() && textTerms.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT e.*
                FROM expert_profiles e
                JOIN account a ON a.account_id = e.account_id
                JOIN portfolios p ON p.expert_id = e.expert_id
                WHERE LOWER(e.kyc_status) = 'approved'
                  AND LOWER(a.status) = 'approved'
                """);

        if (minYears > 0) {
            sql.append(" AND COALESCE(p.years_experience, e.years_of_experience, 0) >= :minYears");
        }

        List<String> filters = new ArrayList<>();
        if (!skillIds.isEmpty()) {
            filters.add("""
                    EXISTS (
                        SELECT 1
                        FROM regexp_split_to_table(p.skill_ids, ',') AS sid(value)
                        WHERE TRIM(sid.value) ~ '^[0-9]+$'
                          AND CAST(TRIM(sid.value) AS INTEGER) IN (:skillIds)
                    )
                    """);
        }
        if (!domainIds.isEmpty()) {
            filters.add("""
                    EXISTS (
                        SELECT 1
                        FROM regexp_split_to_table(p.domain_ids, ',') AS did(value)
                        WHERE TRIM(did.value) ~ '^[0-9]+$'
                          AND CAST(TRIM(did.value) AS INTEGER) IN (:domainIds)
                    )
                    """);
        }
        for (int i = 0; i < textTerms.size(); i++) {
            filters.add("(LOWER(p.self_description) LIKE :term" + i + " OR LOWER(COALESCE(p.certificates, '')) LIKE :term" + i + ")");
        }

        sql.append(" AND (").append(String.join(" OR ", filters)).append(")");
        sql.append(" ORDER BY COALESCE(e.years_of_experience, 0) DESC, e.created_at ASC");

        Query query = entityManager.createNativeQuery(sql.toString(), ExpertProfileEntity.class);
        if (minYears > 0) {
            query.setParameter("minYears", minYears);
        }
        if (!skillIds.isEmpty()) {
            query.setParameter("skillIds", skillIds);
        }
        if (!domainIds.isEmpty()) {
            query.setParameter("domainIds", domainIds);
        }
        for (int i = 0; i < textTerms.size(); i++) {
            query.setParameter("term" + i, "%" + textTerms.get(i) + "%");
        }
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<ExpertProfileEntity> results = query.getResultList();
        return results;
    }

    private List<ExpertCandidateDto> buildCandidateDtos(List<ExpertProfileEntity> experts) {
        Map<Integer, SkillEntity> skillsById = skillRepository.findAll().stream()
                .collect(Collectors.toMap(SkillEntity::getSkillId, skill -> skill, (left, right) -> left, LinkedHashMap::new));
        Map<Integer, DomainEntity> domainsById = domainRepository.findAll().stream()
                .collect(Collectors.toMap(DomainEntity::getDomainId, domain -> domain, (left, right) -> left, LinkedHashMap::new));

        return experts.stream()
                .map(expert -> buildCandidateDto(expert, skillsById, domainsById))
                .toList();
    }

    private ExpertCandidateDto buildCandidateDto(
            ExpertProfileEntity expert,
            Map<Integer, SkillEntity> skillsById,
            Map<Integer, DomainEntity> domainsById
    ) {
        Optional<PortfolioEntity> portfolio = portfolioRepository.findByExpertId(expert.getExpertId());
        AccountEntity account = accountRepository.findById(expert.getAccountId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT CUA EXPERT"));

        List<String> skillNames = portfolio
                .map(PortfolioEntity::getSkillIds)
                .map(ids -> mapSkillNames(ids, skillsById))
                .orElse(List.of());
        List<String> domainNames = portfolio
                .map(PortfolioEntity::getDomainIds)
                .map(ids -> mapDomainNames(ids, domainsById))
                .orElse(List.of());

        int yearsExperience = Math.max(
                expert.getYearsOfExperience() == null ? 0 : expert.getYearsOfExperience(),
                portfolio.map(PortfolioEntity::getYearsExperience).orElse(0)
        );
        Double averageRating = reviewRepository.findAverageRatingByRevieweeId(expert.getAccountId());
        BigDecimal rating = averageRating == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP);

        return ExpertCandidateDto.builder()
                .expertId(expert.getExpertId())
                .fullName(account.getFullName())
                .profile(portfolio.map(PortfolioEntity::getSelfDescription).orElse(""))
                .skills(skillNames)
                .domains(domainNames)
                .yearsExperience(yearsExperience)
                .rating(rating)
                .completedProjects((int) contractRepository.countByExpertIdAndStatus(expert.getExpertId(), "Completed"))
                .hourlyRate(expert.getHourlyRate())
                .availability(hasText(expert.getAvailability()) ? expert.getAvailability() : "AVAILABLE")
                .portfolioUrl(expert.getPortfolioUrl())
                .build();
    }

    private List<ExpertRecommendationItemDto> sanitizeRecommendations(
            AiExpertRankingResponseDto aiResponse,
            List<ExpertCandidateDto> candidates
    ) {
        Map<Integer, ExpertCandidateDto> candidatesById = candidates.stream()
                .collect(Collectors.toMap(ExpertCandidateDto::getExpertId, candidate -> candidate, (left, right) -> left));
        Set<Integer> seen = new HashSet<>();

        return defaultList(aiResponse.getRecommendations()).stream()
                .filter(item -> item != null && item.getExpertId() != null)
                .filter(item -> candidatesById.containsKey(item.getExpertId()))
                .filter(item -> seen.add(item.getExpertId()))
                .map(item -> sanitizeRecommendation(item, candidatesById.get(item.getExpertId())))
                .sorted(Comparator.comparing(ExpertRecommendationItemDto::getMatchScore).reversed())
                .limit(TOP_RECOMMENDATION_LIMIT)
                .toList();
    }

    private ExpertRecommendationItemDto sanitizeRecommendation(
            ExpertRecommendationItemDto item,
            ExpertCandidateDto candidate
    ) {
        item.setFullName(candidate.getFullName());
        item.setMatchScore(clampScore(item.getMatchScore()));
        item.setMatchedSkills(defaultList(item.getMatchedSkills()));
        if (!hasText(item.getReason())) {
            item.setReason("AI khong cung cap ly do chi tiet.");
        }
        if (!hasText(item.getRiskNotes())) {
            item.setRiskNotes("Chua co risk note noi bat.");
        }
        if (!hasText(item.getSuggestedRole())) {
            item.setSuggestedRole("Recommended expert");
        }
        return item;
    }

    private void saveLatestRecommendations(
            Integer jobId,
            SowKeywordsDto keywords,
            int candidateCount,
            String note,
            List<ExpertRecommendationItemDto> recommendations
    ) {
        expertRecommendationRepository.deleteByJobId(jobId);
        if (recommendations.isEmpty()) {
            return;
        }

        String keywordJson = toJson(keywords);
        List<ExpertRecommendationEntity> entities = recommendations.stream()
                .map(item -> ExpertRecommendationEntity.builder()
                        .jobId(jobId)
                        .expertId(item.getExpertId())
                        .matchScore(item.getMatchScore())
                        .matchedSkills(toJson(defaultList(item.getMatchedSkills())))
                        .reason(item.getReason())
                        .riskNotes(item.getRiskNotes())
                        .suggestedRole(item.getSuggestedRole())
                        .keywordJson(keywordJson)
                        .aiNote(note)
                        .candidateCount(candidateCount)
                        .build())
                .toList();
        expertRecommendationRepository.saveAll(entities);
    }

    private boolean matchesCatalog(List<String> terms, String code, String name, String description) {
        String catalogText = normalizeText(String.join(" ", nonNullStrings(code, name, description)));
        Set<String> catalogTokens = tokens(catalogText);
        for (String term : terms) {
            String normalizedTerm = normalizeText(term);
            if (normalizedTerm.length() >= 3 && catalogText.contains(normalizedTerm)) {
                return true;
            }
            Set<String> termTokens = tokens(normalizedTerm);
            termTokens.retainAll(catalogTokens);
            if (!termTokens.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private List<String> mapSkillNames(String skillIds, Map<Integer, SkillEntity> skillsById) {
        return parseCsvIds(skillIds).stream()
                .map(skillsById::get)
                .filter(Objects::nonNull)
                .map(SkillEntity::getSkillName)
                .toList();
    }

    private List<String> mapDomainNames(String domainIds, Map<Integer, DomainEntity> domainsById) {
        return parseCsvIds(domainIds).stream()
                .map(domainsById::get)
                .filter(Objects::nonNull)
                .map(DomainEntity::getDomainName)
                .toList();
    }

    private Set<Integer> parseCsvIds(String value) {
        if (!hasText(value)) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> token.matches("[0-9]+"))
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private BigDecimal clampScore(BigDecimal score) {
        if (score == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal clamped = score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        return clamped.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildNote(String aiNote, int candidateCount, int recommendationCount) {
        if (recommendationCount == 0) {
            return hasText(aiNote)
                    ? aiNote
                    : "AI khong tim thay expert du phu hop trong danh sach candidate da filter.";
        }
        if (recommendationCount < TOP_RECOMMENDATION_LIMIT) {
            String fallback = "Chi recommend " + recommendationCount + " expert vi backend chi tim thay "
                    + candidateCount + " candidate phu hop sau khi filter.";
            return hasText(aiNote) ? aiNote + " " + fallback : fallback;
        }
        return hasText(aiNote) ? aiNote : "Da recommend Top 5 expert phu hop nhat tu danh sach candidate da filter.";
    }

    private int resolveCandidateLimit(RecommendExpertsRequest request) {
        if (request == null || request.getMaxCandidates() == null) {
            return DEFAULT_CANDIDATE_LIMIT;
        }
        return Math.max(1, Math.min(MAX_CANDIDATE_LIMIT, request.getMaxCandidates()));
    }

    private Set<String> tokens(String value) {
        if (!hasText(value)) {
            return new HashSet<>();
        }
        return Arrays.stream(value.split("\\s+"))
                .map(String::trim)
                .filter(token -> token.length() >= 3)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String withoutAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccent
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new AppException("Khong serialize duoc recommendation");
        }
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<String> nonNullStrings(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .toList();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }
}
