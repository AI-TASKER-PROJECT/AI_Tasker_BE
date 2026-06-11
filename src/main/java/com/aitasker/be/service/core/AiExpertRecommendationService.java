package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.BadGatewayException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.recommendation.AiExpertRankingResponseDto;
import com.aitasker.be.dto.recommendation.BudgetRangeDto;
import com.aitasker.be.dto.recommendation.ExpertCandidateDto;
import com.aitasker.be.dto.recommendation.SowKeywordsDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiExpertRecommendationService {
    private static final String SYSTEM_MESSAGE = "Ban la AI talent matching assistant. Bat buoc tra ve JSON hop le, khong markdown, khong giai thich ngoai JSON.";

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SowKeywordsDto extractKeywords(String sowSummary) {
        ensureConfigured();
        String aiResponse = callAi(buildKeywordPrompt(sowSummary));
        return parseKeywordResponse(aiResponse);
    }

    public AiExpertRankingResponseDto rankExperts(
            String sowSummary,
            SowKeywordsDto keywords,
            List<ExpertCandidateDto> candidates
    ) {
        ensureConfigured();
        String aiResponse = callAi(buildRankingPrompt(sowSummary, keywords, candidates));
        return parseRankingResponse(aiResponse);
    }

    public String buildKeywordPrompt(String sowSummary) {
        return """
                Phan tich SoW hoac hiring request ben duoi va extract cac keyword dung de filter expert trong database.

                Chi tra ve JSON object dung schema. Neu khong co thong tin, dung mang rong hoac "unspecified".

                JSON schema bat buoc:
                {
                  "requiredSkills": ["string"],
                  "domains": ["string"],
                  "industries": ["string"],
                  "projectScope": ["string"],
                  "budgetRange": {
                    "min": 0,
                    "max": 0,
                    "currency": "VND"
                  },
                  "timeline": "string",
                  "experienceLevel": "junior|middle|senior|lead|unspecified",
                  "deliverables": ["string"],
                  "keywords": ["string"]
                }

                SoW summary:
                %s
                """.formatted(sowSummary == null ? "" : sowSummary);
    }

    public String buildRankingPrompt(
            String sowSummary,
            SowKeywordsDto keywords,
            List<ExpertCandidateDto> candidates
    ) {
        return """
                Danh gia candidate experts da duoc backend filter truoc. Khong duoc de xuat expert ngoai danh sach candidateExperts.
                Muc tieu la recommend toi da Top 5 expert phu hop nhat cho business xem, khong tu dong chon/accept expert.
                Neu khong du 5 expert phu hop, tra ve it hon 5 va ghi ro ly do trong note.

                Tieu chi cham diem:
                - Skill/domain match voi SoW va extracted keywords.
                - Kinh nghiem, completedProjects, rating.
                - Budget/hourlyRate va availability.
                - Rui ro ve thieu skill, thieu kinh nghiem, availability, hoac portfolio qua mong.

                Bat buoc tra ve JSON object dung schema:
                {
                  "recommendations": [
                    {
                      "expertId": 1,
                      "fullName": "string",
                      "matchScore": 92,
                      "matchedSkills": ["string"],
                      "reason": "string",
                      "riskNotes": "string",
                      "suggestedRole": "string"
                    }
                  ],
                  "note": "string"
                }

                SoW summary:
                %s

                extractedKeywords:
                %s

                candidateExperts:
                %s
                """.formatted(
                sowSummary == null ? "" : sowSummary,
                toJson(keywords),
                toJson(candidates == null ? List.of() : candidates)
        );
    }

    public SowKeywordsDto parseKeywordResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BadGatewayException("AI khong tra ve noi dung keyword");
        }

        try {
            JsonNode responseNode = objectMapper.readTree(extractJsonPayload(aiResponse));
            if (!(responseNode instanceof ObjectNode objectNode)) {
                throw new AppException("AI keyword response phai la JSON object");
            }

            copyAliasArray(objectNode, "domain", "domains");
            copyAliasArray(objectNode, "industry", "industries");
            normalizeArrayField(objectNode, "requiredSkills");
            normalizeArrayField(objectNode, "domains");
            normalizeArrayField(objectNode, "industries");
            normalizeArrayField(objectNode, "projectScope");
            normalizeArrayField(objectNode, "deliverables");
            normalizeArrayField(objectNode, "keywords");
            normalizeBudgetRange(objectNode);

            SowKeywordsDto keywords = objectMapper.treeToValue(objectNode, SowKeywordsDto.class);
            applyKeywordDefaults(keywords);
            return keywords;
        } catch (JsonProcessingException ex) {
            throw new AppException("AI keyword response khong phai JSON hop le: " + truncate(ex.getOriginalMessage(), 200));
        }
    }

    public AiExpertRankingResponseDto parseRankingResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BadGatewayException("AI khong tra ve noi dung recommendation");
        }

        try {
            JsonNode responseNode = objectMapper.readTree(extractJsonPayload(aiResponse));
            if (!(responseNode instanceof ObjectNode objectNode)) {
                throw new AppException("AI recommendation response phai la JSON object");
            }

            normalizeRecommendations(objectNode);
            AiExpertRankingResponseDto response = objectMapper.treeToValue(objectNode, AiExpertRankingResponseDto.class);
            if (response.getRecommendations() == null) {
                response.setRecommendations(new ArrayList<>());
            }
            return response;
        } catch (JsonProcessingException ex) {
            throw new AppException("AI recommendation response khong phai JSON hop le: " + truncate(ex.getOriginalMessage(), 200));
        }
    }

    private void normalizeBudgetRange(ObjectNode objectNode) {
        JsonNode budgetRange = objectNode.get("budgetRange");
        if (budgetRange == null || budgetRange.isNull() || budgetRange.isObject()) {
            return;
        }

        ObjectNode normalized = objectMapper.createObjectNode();
        normalized.put("min", 0);
        normalized.put("max", normalizeMoneyText(budgetRange.asText()).isBlank() ? 0 : Long.parseLong(normalizeMoneyText(budgetRange.asText())));
        normalized.put("currency", "VND");
        objectNode.set("budgetRange", normalized);
    }

    private void normalizeRecommendations(ObjectNode responseNode) {
        normalizeArrayField(responseNode, "recommendations");
        JsonNode recommendationsNode = responseNode.get("recommendations");
        if (!(recommendationsNode instanceof ArrayNode recommendations)) {
            return;
        }

        for (JsonNode itemNode : recommendations) {
            if (!(itemNode instanceof ObjectNode item)) {
                continue;
            }
            normalizeArrayField(item, "matchedSkills");
            normalizeMatchScore(item);
        }
    }

    private void normalizeMatchScore(ObjectNode item) {
        JsonNode scoreNode = item.get("matchScore");
        if (scoreNode == null || scoreNode.isNull() || scoreNode.isNumber()) {
            return;
        }

        String normalized = scoreNode.isTextual()
                ? scoreNode.asText().replaceAll("[^0-9.\\-]", "")
                : scoreNode.toString().replaceAll("[^0-9.\\-]", "");
        if (normalized.isBlank() || "-".equals(normalized)) {
            item.put("matchScore", 0);
            return;
        }
        item.put("matchScore", new BigDecimal(normalized));
    }

    private void copyAliasArray(ObjectNode objectNode, String sourceField, String targetField) {
        if (objectNode.has(targetField) || !objectNode.has(sourceField)) {
            return;
        }
        objectNode.set(targetField, objectNode.get(sourceField));
    }

    private void normalizeArrayField(ObjectNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.isArray()) {
            return;
        }

        ArrayNode values = objectMapper.createArrayNode();
        if (field.isTextual()) {
            values.add(field.asText());
        } else {
            values.add(field.toString());
        }
        node.set(fieldName, values);
    }

    private void applyKeywordDefaults(SowKeywordsDto keywords) {
        keywords.setRequiredSkills(defaultList(keywords.getRequiredSkills()));
        keywords.setDomains(defaultList(keywords.getDomains()));
        keywords.setIndustries(defaultList(keywords.getIndustries()));
        keywords.setProjectScope(defaultList(keywords.getProjectScope()));
        keywords.setDeliverables(defaultList(keywords.getDeliverables()));
        keywords.setKeywords(defaultList(keywords.getKeywords()));
        if (keywords.getBudgetRange() == null) {
            keywords.setBudgetRange(BudgetRangeDto.builder().min(BigDecimal.ZERO).max(BigDecimal.ZERO).currency("VND").build());
        }
        if (keywords.getTimeline() == null || keywords.getTimeline().isBlank()) {
            keywords.setTimeline("unspecified");
        }
        if (keywords.getExperienceLevel() == null || keywords.getExperienceLevel().isBlank()) {
            keywords.setExperienceLevel("unspecified");
        }
    }

    private String callAi(String prompt) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(buildRequestBody(prompt), buildHeaders());

        try {
            ResponseEntity<Map> response = callOpenAi(request);
            return extractContent(response.getBody());
        } catch (RestClientResponseException ex) {
            throw new BadGatewayException(buildOpenAiErrorMessage(ex));
        } catch (RestClientException ex) {
            throw new BadGatewayException("Khong goi duoc AI API");
        }
    }

    private ResponseEntity<Map> callOpenAi(HttpEntity<Map<String, Object>> request) {
        RestClientResponseException lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return restTemplate.exchange(openAiProperties.getChatCompletionsUrl(), HttpMethod.POST, request, Map.class);
            } catch (RestClientResponseException ex) {
                lastException = ex;
                if (!shouldRetry(ex.getStatusCode()) || attempt == 2) {
                    throw ex;
                }
            }
        }

        throw lastException;
    }

    private boolean shouldRetry(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError();
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_MESSAGE);

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

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());
        return headers;
    }

    private String extractContent(Map<?, ?> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new BadGatewayException("AI khong tra ve response hop le");
        }

        Object choices = responseBody.get("choices");
        if (!(choices instanceof List<?> choiceItems) || choiceItems.isEmpty()) {
            throw new BadGatewayException("AI khong tra ve choices hop le");
        }

        Object firstChoice = choiceItems.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            throw new BadGatewayException("AI khong tra ve choice hop le");
        }

        Object message = choiceMap.get("message");
        if (!(message instanceof Map<?, ?> messageMap)) {
            throw new BadGatewayException("AI khong tra ve message hop le");
        }

        Object content = messageMap.get("content");
        if (!(content instanceof String text) || text.isBlank()) {
            throw new BadGatewayException("AI khong tra ve noi dung hop le");
        }

        return text;
    }

    private String extractJsonPayload(String aiResponse) {
        String content = aiResponse.trim();

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

    private void ensureConfigured() {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new BadGatewayException("Chua cau hinh OPENAI_API_KEY");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new AppException("Khong serialize duoc prompt recommendation");
        }
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private String normalizeMoneyText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String digits = value.replaceAll("[^0-9]", "");
        return digits;
    }

    private String buildOpenAiErrorMessage(RestClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return "AI API loi: " + ex.getStatusCode();
        }

        return "AI API loi: " + ex.getStatusCode() + " - " + truncate(responseBody, 500);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }
}
