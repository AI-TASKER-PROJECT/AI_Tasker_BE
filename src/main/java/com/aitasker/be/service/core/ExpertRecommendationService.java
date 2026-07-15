/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ExpertRecommendationService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.candidate.ExpertCandidateResponse;
import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.dto.candidate.ExpertRecommendationListResponse;
import com.aitasker.be.dto.candidate.ExpertRecommendationResponse;
import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.ExpertRecommendationEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.SowEntity;
import com.aitasker.be.repository.ExpertRecommendationRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.SowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class ExpertRecommendationService {
    private static final int MAX_RECOMMENDATIONS = 5;
    private static final String FALLBACK_REASON = "Được đề xuất dựa trên điểm match từ kỹ năng, lĩnh vực, kinh nghiệm và mô tả portfolio.";
    private static final String AI_SYSTEM_MESSAGE = """
            Bạn là AI matching assistant cho nền tảng thuê chuyên gia. Nhiệm vụ của bạn là chọn Top 5 Expert phù hợp nhất cho Job Posting dựa trên SoW và danh sách candidate đã được backend lọc trước. Không được bịa expertId. Chỉ được chọn expertId có trong candidate list. Trả về JSON hợp lệ, không thêm markdown.
            """;

    private final ExpertCandidateRankingService expertCandidateRankingService;
    private final ExpertRecommendationRepository expertRecommendationRepository;
    private final JobRepository jobRepository;
    private final SowRepository sowRepository;
    private final MilestoneRepository milestoneRepository;
    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;
    private final PaymentWalletService paymentWalletService;
    private final AccessService accessService;
    private final ExpertProfileRepository expertProfileRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    // Note: Ham `generateRecommendations` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public ExpertRecommendationListResponse generateRecommendations(Long jobPostingId) {
        paymentWalletService.requirePremiumRecommendationAccess(jobPostingId);
        Integer jobId = toIntegerJobId(jobPostingId);
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));

        List<ExpertRecommendationEntity> existingRecommendations = expertRecommendationRepository
                .findByJobPostingIdOrderByRankPositionAsc(jobPostingId);
        Set<Long> previouslySelectedExpertIds = existingRecommendations.stream()
                .filter(item -> Boolean.TRUE.equals(item.getBusinessSelected()))
                .map(ExpertRecommendationEntity::getExpertId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        ExpertCandidateSearchResponse candidateSearch = expertCandidateRankingService.findTopCandidatesByJobPostingId(jobId);
        List<ExpertCandidateResponse> candidates = defaultList(candidateSearch.getCandidates());

        if (candidates.isEmpty()) {
            List<ExpertRecommendationResponse> selectedRecommendations = existingRecommendations.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getBusinessSelected()))
                    .map(this::toResponse)
                    .toList();
            return ExpertRecommendationListResponse.builder()
                    .jobPostingId(jobPostingId)
                    .recommendations(selectedRecommendations)
                    .generatedByAi(false)
                    .message(selectedRecommendations.isEmpty()
                            ? "No eligible expert candidates found."
                            : "No new eligible candidates found; existing Business selection was preserved.")
                    .build();
        }

        RecommendationGenerationResult generationResult = generateWithAi(job, candidateSearch)
                .orElseGet(() -> RecommendationGenerationResult.builder()
                        .recommendations(fallbackRecommendations(candidates))
                        .generatedByAi(false)
                        .message("AI recommendation failed, fallback to rule-based ranking.")
                        .build());

        List<ExpertRecommendationResponse> normalizedRecommendations = normalizeRecommendations(
                generationResult.recommendations(),
                candidates,
                previouslySelectedExpertIds
        );
        if (Boolean.TRUE.equals(generationResult.generatedByAi())
                && !hasUsableAiReason(generationResult.recommendations(), normalizedRecommendations)) {
            generationResult = RecommendationGenerationResult.builder()
                    .recommendations(generationResult.recommendations())
                    .generatedByAi(false)
                    .message("AI recommendation failed, fallback to rule-based ranking.")
                    .build();
        }

        expertRecommendationRepository.deleteByJobPostingId(jobPostingId);
        expertRecommendationRepository.flush();
        expertRecommendationRepository.saveAll(toEntities(jobPostingId, normalizedRecommendations));

        return ExpertRecommendationListResponse.builder()
                .jobPostingId(jobPostingId)
                .recommendations(normalizedRecommendations)
                .generatedByAi(generationResult.generatedByAi())
                .message(generationResult.message())
                .build();
    }

    @Transactional(readOnly = true)
    // Note: Ham `getRecommendations` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public ExpertRecommendationListResponse getRecommendations(Long jobPostingId) {
        paymentWalletService.requirePremiumRecommendationAccess(jobPostingId);
        List<ExpertRecommendationResponse> recommendations = expertRecommendationRepository
                .findByJobPostingIdOrderByRankPositionAsc(jobPostingId).stream()
                .map(this::toResponse)
                .toList();

        return ExpertRecommendationListResponse.builder()
                .jobPostingId(jobPostingId)
                .recommendations(recommendations)
                .generatedByAi(null)
                .message(recommendations.isEmpty() ? "No saved expert recommendations found." : "Saved expert recommendations loaded.")
                .build();
    }

    @Transactional
    // Note: Ham `selectRecommendedExpert` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public ExpertRecommendationResponse selectRecommendedExpert(Long jobPostingId, Long expertId) {
        paymentWalletService.requirePremiumRecommendationAccess(jobPostingId);
        ExpertRecommendationEntity recommendation = expertRecommendationRepository
                .findByJobPostingIdAndExpertId(jobPostingId, expertId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT RECOMMENDATION"));
        JobEntity job = jobRepository.findById(toIntegerJobId(jobPostingId))
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));

        boolean firstSelection = !Boolean.TRUE.equals(recommendation.getBusinessSelected());
        if (firstSelection) {
            recommendation.setBusinessSelected(Boolean.TRUE);
            recommendation = expertRecommendationRepository.save(recommendation);
            ExpertProfileEntity expert = expertProfileRepository.findById(toIntegerExpertId(expertId))
                    .orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
            notificationService.notifyExpertSelectedForJob(
                    expert.getAccountId(),
                    accessService.currentAccount().getAccountId(),
                    job.getJobId(),
                    job.getTitle()
            );
        }
        return toResponse(recommendation);
    }

    // Note: Ham `generateWithAi` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Optional<RecommendationGenerationResult> generateWithAi(
            JobEntity job,
            ExpertCandidateSearchResponse candidateSearch
    ) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            return Optional.empty();
        }

        try {
            String aiResponse = callOpenAi(buildPrompt(job, candidateSearch));
            List<ExpertRecommendationResponse> recommendations = parseAiRecommendations(aiResponse);
            if (recommendations.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(RecommendationGenerationResult.builder()
                    .recommendations(recommendations)
                    .generatedByAi(true)
                    .message("AI recommendations generated successfully.")
                    .build());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    // Note: Ham `callOpenAi` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String callOpenAi(String prompt) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(buildRequestBody(prompt), buildHeaders());
        RestClientResponseException lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        openAiProperties.getChatCompletionsUrl(),
                        HttpMethod.POST,
                        request,
                        Map.class
                );
                return extractContent(response.getBody());
            } catch (RestClientResponseException ex) {
                lastException = ex;
                if (!shouldRetry(ex.getStatusCode()) || attempt == 2) {
                    throw ex;
                }
            } catch (RestClientException ex) {
                throw ex;
            }
        }

        throw lastException;
    }

    // Note: Ham `buildRequestBody` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", AI_SYSTEM_MESSAGE);

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        Map<String, Object> responseFormat = new LinkedHashMap<>();
        responseFormat.put("type", "json_object");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("messages", List.of(systemMessage, userMessage));
        requestBody.put("temperature", 0.1);
        requestBody.put("response_format", responseFormat);
        return requestBody;
    }

    // Note: Ham `buildHeaders` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());
        return headers;
    }

    // Note: Ham `shouldRetry` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean shouldRetry(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError();
    }

    // Note: Ham `extractContent` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String extractContent(Map<?, ?> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new IllegalArgumentException("AI response body is empty");
        }

        Object choices = responseBody.get("choices");
        if (!(choices instanceof List<?> choiceItems) || choiceItems.isEmpty()) {
            throw new IllegalArgumentException("AI response choices are empty");
        }

        Object firstChoice = choiceItems.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            throw new IllegalArgumentException("AI response choice is invalid");
        }

        Object message = choiceMap.get("message");
        if (!(message instanceof Map<?, ?> messageMap)) {
            throw new IllegalArgumentException("AI response message is invalid");
        }

        Object content = messageMap.get("content");
        if (!(content instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException("AI response content is empty");
        }
        return text;
    }

    // Note: Ham `buildPrompt` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildPrompt(JobEntity job, ExpertCandidateSearchResponse candidateSearch) {
        SowKeywordExtractionResult keywords = candidateSearch.getKeywords();
        return """
                SoW summary:
                %s

                Required skills:
                %s

                Required domains:
                %s

                Candidate list Top 20:
                %s

                Không thay đổi thứ tự, điểm hoặc bằng chứng của backend. Chỉ viết lý do ngắn cho expertId có trong candidate list.
                JSON response bắt buộc:
                {
                  "recommendations": [
                    {
                      "expertId": 1,
                      "reason": "Expert phù hợp vì có kinh nghiệm xây dựng chatbot AI chăm sóc khách hàng và mô tả portfolio khớp với yêu cầu SoW."
                    }
                  ]
                }
                """.formatted(
                buildSowSummary(job),
                keywords == null ? List.of() : defaultList(keywords.getSkills()),
                keywords == null ? List.of() : defaultList(keywords.getDomains()),
                buildCandidatePromptList(candidateSearch.getCandidates())
        );
    }

    // Note: Ham `buildSowSummary` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildSowSummary(JobEntity job) {
        Integer jobId = job.getJobId();
        Optional<SowEntity> sow = sowRepository.findByJobId(jobId);
        List<MilestoneEntity> milestones = milestoneRepository.findByJobIdOrderByOrderIndexAsc(jobId);
        List<String> parts = new ArrayList<>();

        parts.add("Job title: " + nullToBlank(job.getTitle()));
        if (sow.isPresent()) {
            SowEntity item = sow.get();
            addPart(parts, "SoW title", item.getTitle());
            addPart(parts, "Overview", item.getOverview());
            addPart(parts, "Objectives", item.getObjectives());
            addPart(parts, "Scope of work", item.getScopeOfWork());
            addPart(parts, "Deliverables", item.getDeliverable());
        } else {
            addPart(parts, "Structured SoW", job.getStructuredSow());
        }

        if (!milestones.isEmpty()) {
            parts.add("Milestones: " + milestones.stream()
                    .map(milestone -> nullToBlank(milestone.getMilestoneName()) + " - " + nullToBlank(milestone.getDescription()))
                    .toList());
        }

        return truncate(String.join("\n", parts), 3000);
    }

    // Note: Ham `buildCandidatePromptList` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildCandidatePromptList(List<ExpertCandidateResponse> candidates) {
        List<Map<String, Object>> candidateItems = defaultList(candidates).stream()
                .limit(20)
                .map(candidate -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("expertId", candidate.getExpertId());
                    item.put("portfolioId", candidate.getPortfolioId());
                    item.put("backendMatchScore", candidate.getMatchScore());
                    item.put("matchedSkills", defaultList(candidate.getMatchedSkills()));
                    item.put("matchedDomains", defaultList(candidate.getMatchedDomains()));
                    item.put("yearsExperience", candidate.getYearsExperience());
                    item.put("certificates", truncate(candidate.getCertificates(), 500));
                    item.put("selfDescription", truncate(candidate.getSelfDescription(), 800));
                    return item;
                })
                .toList();
        try {
            return objectMapper.writeValueAsString(candidateItems);
        } catch (JsonProcessingException ex) {
            return candidateItems.toString();
        }
    }

    // Note: Ham `parseAiRecommendations` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<ExpertRecommendationResponse> parseAiRecommendations(String aiResponse) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(extractJsonPayload(aiResponse));
        JsonNode recommendationsNode = root.path("recommendations");
        if (!recommendationsNode.isArray()) {
            return List.of();
        }

        List<ExpertRecommendationResponse> recommendations = new ArrayList<>();
        for (JsonNode node : recommendationsNode) {
            recommendations.add(ExpertRecommendationResponse.builder()
                    .expertId(readLong(node, "expertId"))
                    .reason(readText(node, "reason"))
                    .build());
        }
        return recommendations;
    }

    // Note: Ham `normalizeRecommendations` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<ExpertRecommendationResponse> normalizeRecommendations(
            List<ExpertRecommendationResponse> rawRecommendations,
            List<ExpertCandidateResponse> candidates,
            Set<Long> previouslySelectedExpertIds
    ) {
        Map<Long, String> aiReasonsByExpertId = new LinkedHashMap<>();
        for (ExpertRecommendationResponse raw : defaultList(rawRecommendations)) {
            Long expertId = raw.getExpertId();
            String reason = sanitizeReason(raw.getReason());
            if (expertId != null && !reason.isBlank()) {
                aiReasonsByExpertId.putIfAbsent(expertId, reason);
            }
        }

        List<ExpertCandidateResponse> orderedCandidates = new ArrayList<>();
        candidates.stream()
                .filter(candidate -> previouslySelectedExpertIds.contains(toLong(candidate.getExpertId())))
                .forEach(orderedCandidates::add);
        candidates.stream()
                .filter(candidate -> !previouslySelectedExpertIds.contains(toLong(candidate.getExpertId())))
                .forEach(orderedCandidates::add);

        List<ExpertRecommendationResponse> normalized = new ArrayList<>();
        for (ExpertCandidateResponse candidate : orderedCandidates.stream().limit(MAX_RECOMMENDATIONS).toList()) {
            Long expertId = toLong(candidate.getExpertId());
            normalized.add(ExpertRecommendationResponse.builder()
                    .expertId(expertId)
                    .portfolioId(toLong(candidate.getPortfolioId()))
                    .rankPosition(normalized.size() + 1)
                    .matchScore(clampScore(candidate.getMatchScore()))
                    .matchedSkills(defaultList(candidate.getMatchedSkills()))
                    .matchedDomains(defaultList(candidate.getMatchedDomains()))
                    .reason(aiReasonsByExpertId.getOrDefault(expertId, FALLBACK_REASON))
                    .businessSelected(previouslySelectedExpertIds.contains(expertId))
                    .build());
        }
        return normalized;
    }

    private String sanitizeReason(String reason) {
        if (reason == null) {
            return "";
        }
        String sanitized = reason.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return truncate(sanitized, 800);
    }

    private boolean hasUsableAiReason(
            List<ExpertRecommendationResponse> aiRecommendations,
            List<ExpertRecommendationResponse> normalizedRecommendations
    ) {
        Set<Long> normalizedExpertIds = normalizedRecommendations.stream()
                .map(ExpertRecommendationResponse::getExpertId)
                .collect(java.util.stream.Collectors.toSet());
        return defaultList(aiRecommendations).stream().anyMatch(item ->
                item.getExpertId() != null
                        && normalizedExpertIds.contains(item.getExpertId())
                        && !sanitizeReason(item.getReason()).isBlank()
        );
    }

    // Note: Ham `fallbackRecommendations` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<ExpertRecommendationResponse> fallbackRecommendations(List<ExpertCandidateResponse> candidates) {
        List<ExpertRecommendationResponse> recommendations = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_RECOMMENDATIONS, candidates.size()); i++) {
            ExpertCandidateResponse candidate = candidates.get(i);
            recommendations.add(ExpertRecommendationResponse.builder()
                    .expertId(toLong(candidate.getExpertId()))
                    .portfolioId(toLong(candidate.getPortfolioId()))
                    .rankPosition(i + 1)
                    .matchScore(clampScore(candidate.getMatchScore()))
                    .matchedSkills(defaultList(candidate.getMatchedSkills()))
                    .matchedDomains(defaultList(candidate.getMatchedDomains()))
                    .reason(FALLBACK_REASON)
                    .businessSelected(Boolean.FALSE)
                    .build());
        }
        return recommendations;
    }

    // Note: Ham `toEntities` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<ExpertRecommendationEntity> toEntities(
            Long jobPostingId,
            List<ExpertRecommendationResponse> recommendations
    ) {
        return recommendations.stream()
                .map(response -> ExpertRecommendationEntity.builder()
                        .jobPostingId(jobPostingId)
                        .expertId(response.getExpertId())
                        .portfolioId(response.getPortfolioId())
                        .rankPosition(response.getRankPosition())
                        .matchScore(toBigDecimal(response.getMatchScore()))
                        .aiReason(response.getReason())
                        .matchedSkills(writeStringList(response.getMatchedSkills()))
                        .matchedDomains(writeStringList(response.getMatchedDomains()))
                        .businessSelected(Boolean.TRUE.equals(response.getBusinessSelected()))
                        .build())
                .toList();
    }

    // Note: Ham `toResponse` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private ExpertRecommendationResponse toResponse(ExpertRecommendationEntity entity) {
        return ExpertRecommendationResponse.builder()
                .expertId(entity.getExpertId())
                .portfolioId(entity.getPortfolioId())
                .rankPosition(entity.getRankPosition())
                .matchScore(entity.getMatchScore() == null ? null : entity.getMatchScore().doubleValue())
                .matchedSkills(readStoredStringList(entity.getMatchedSkills()))
                .matchedDomains(readStoredStringList(entity.getMatchedDomains()))
                .reason(entity.getAiReason())
                .businessSelected(Boolean.TRUE.equals(entity.getBusinessSelected()))
                .build();
    }

    // Note: Ham `extractJsonPayload` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String extractJsonPayload(String aiResponse) {
        String content = aiResponse == null ? "" : aiResponse.trim();
        if (content.startsWith("```")) {
            int firstLineBreak = content.indexOf('\n');
            int lastFence = content.lastIndexOf("```");
            if (firstLineBreak >= 0 && lastFence > firstLineBreak) {
                content = content.substring(firstLineBreak + 1, lastFence).trim();
            }
        }

        if (content.startsWith("{")) {
            return content;
        }

        int jsonStart = content.indexOf('{');
        int jsonEnd = content.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1).trim();
        }
        return content;
    }

    // Note: Ham `readLong` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Long readLong(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isNumber() ? value.asLong() : null;
    }

    // Note: Ham `readText` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isTextual() ? value.asText() : null;
    }

    // Note: Ham `writeStringList` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String writeStringList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(defaultList(values));
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    // Note: Ham `readStoredStringList` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<String> readStoredStringList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of(value);
        }
    }

    // Note: Ham `toBigDecimal` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    // Note: Ham `clampScore` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Double clampScore(Double value) {
        if (value == null) {
            return null;
        }
        return Math.max(0.0, Math.min(100.0, Math.round(value * 100.0) / 100.0));
    }

    // Note: Ham `toIntegerJobId` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Integer toIntegerJobId(Long jobPostingId) {
        try {
            return Math.toIntExact(jobPostingId);
        } catch (ArithmeticException ex) {
            throw new NotFoundException("KHONG TIM THAY JOB");
        }
    }

    // Note: Ham `toIntegerExpertId` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Integer toIntegerExpertId(Long expertId) {
        try {
            return Math.toIntExact(expertId);
        } catch (ArithmeticException ex) {
            throw new NotFoundException("KHONG TIM THAY EXPERT PROFILE");
        }
    }

    // Note: Ham `toLong` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Long toLong(Integer value) {
        return value == null ? null : Long.valueOf(value);
    }

    // Note: Ham `defaultList` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : values;
    }

    // Note: Ham `addPart` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void addPart(List<String> parts, String label, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(label + ": " + value);
        }
    }

    // Note: Ham `nullToBlank` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    // Note: Ham `truncate` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }

    private record RecommendationGenerationResult(
            List<ExpertRecommendationResponse> recommendations,
            Boolean generatedByAi,
            String message
    ) {
        static RecommendationGenerationResultBuilder builder() {
            return new RecommendationGenerationResultBuilder();
        }
    }

    private static class RecommendationGenerationResultBuilder {
        private List<ExpertRecommendationResponse> recommendations;
        private Boolean generatedByAi;
        private String message;

        RecommendationGenerationResultBuilder recommendations(List<ExpertRecommendationResponse> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        RecommendationGenerationResultBuilder generatedByAi(Boolean generatedByAi) {
            this.generatedByAi = generatedByAi;
            return this;
        }

        RecommendationGenerationResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        RecommendationGenerationResult build() {
            return new RecommendationGenerationResult(recommendations, generatedByAi, message);
        }
    }
}
