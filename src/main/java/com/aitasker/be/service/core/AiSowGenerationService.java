/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/AiSowGenerationService.java
 * Đây là file gì: File service xử lý luồng AI generate Statement of Work cho job.
 * Nhiệm vụ: Lấy ngữ cảnh RAG, tạo prompt, gọi OpenAI, parse JSON trả về và chuẩn hóa milestone/ngân sách.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.BadGatewayException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.sow.GenerateSowRequest;
import com.aitasker.be.dto.sow.GenerateSowResponse;
import com.aitasker.be.dto.sow.MilestoneDto;
import com.aitasker.be.service.ai.RagRetrievalService;
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
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// Note: Annotation này cho Spring quản lý class như một service nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AiSowGenerationService {
    private static final String SYSTEM_MESSAGE = "Ban la Senior AI Solution Architect. Bat buoc tra ve JSON hop le, khong markdown, khong giai thich ngoai JSON.";

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;
    private final RagRetrievalService ragRetrievalService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Note: Hàm chính của luồng generate SoW; kiểm tra cấu hình AI, lấy RAG context, gọi AI và chuẩn hóa kết quả.
    public GenerateSowResponse generateSow(GenerateSowRequest request) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new BadGatewayException("Chua cau hinh OPENAI_API_KEY");
        }

        String ragContext = ragRetrievalService.retrieveContext(request);
        String aiResponse = callAi(buildPrompt(request, ragContext));
        GenerateSowResponse response = parseAiResponse(aiResponse);

        if (Boolean.TRUE.equals(response.getNeedMoreInfo())) {
            response.setQuestions(defaultList(response.getQuestions()));
            response.setSow(null);
            response.setMilestones(new ArrayList<>());
            return response;
        }

        if (response.getSow() == null) {
            throw new AppException("AI response thieu thong tin sow");
        }
        if (response.getMilestones() == null || response.getMilestones().isEmpty()) {
            throw new AppException("AI response thieu milestones");
        }

        response.setNeedMoreInfo(false);
        response.setQuestions(defaultList(response.getQuestions()));
        normalizeMilestoneDuration(response, request.getDuration(), request.getDurationUnit());
        normalizeMilestoneBudget(response, request.getBudget());
        return response;
    }

    // Note: Hàm dựng prompt đầy đủ từ yêu cầu dự án và RAG context để AI trả về JSON đúng cấu trúc hệ thống.
    public String buildPrompt(GenerateSowRequest request, String ragContext) {
        return """
                Ban la Senior AI Solution Architect.

                Su dung RAG CONTEXT ben duoi de tao SoW dung nghiep vu he thong.
                Neu RAG CONTEXT khong lien quan, hay bo qua phan khong lien quan.
                Khong duoc copy may moc context, chi dung no lam quy tac tham khao.
                Tuyet doi khong liet ke milestones hoac milestone guidance (ten milestone,
                mo ta milestone, phan bo ngan sach %%) trong cac field sow.overview,
                sow.scopeOfWork, sow.deliverables. Cac field sow chi mo ta tong quan
                du an, pham vi cong viec va san pham ban giao. Milestones duoc liet ke
                doc lap trong array milestones.

                RAG CONTEXT:
                %s

                Nhiem vu:
                1. Phan tich yeu cau tho.
                2. Neu thieu thong tin hay dat cau hoi.
                3. Neu du thong tin:
                   - Viet Statement of Work chuyen nghiep.
                   - Chia milestone.
                   - Uoc luong thoi luong.
                   - Phan bo ngan sach theo milestone.

                Bat buoc tra ve JSON hop le, khong markdown, khong giai thich ngoai JSON.

                JSON schema bat buoc:
                {
                  "needMoreInfo": boolean,
                  "questions": ["string"],
                  "sow": {
                    "title": "string",
                    "overview": "string",
                    "objectives": ["string"],
                    "scopeOfWork": ["string"],
                    "deliverables": ["string"],
                    "assumptions": ["string"],
                    "outOfScope": ["string"]
                  },
                  "milestones": [
                    {
                      "name": "string",
                      "description": "string",
                      "duration": 1,
                      "durationUnit": "tuan",
                      "budget": 30000000
                    }
                  ]
                }

                Input:
                Project title: %s
                Raw requirement: %s
                Budget: %s
                Duration: %s %s
                Support fields: %s
                Required skills: %s
                """.formatted(
                ragContext == null ? "" : ragContext,
                request.getProjectTitle(),
                request.getRawRequirement(),
                request.getBudget(),
                request.getDuration(),
                request.getDurationUnit(),
                defaultList(request.getSupportFields()),
                defaultList(request.getRequiredSkills())
        );
    }

    // Note: Hàm parse nội dung AI trả về, làm sạch các field dễ sai kiểu rồi map sang response DTO.
    public GenerateSowResponse parseAiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BadGatewayException("AI khong tra ve noi dung");
        }

        String jsonPayload = extractJsonPayload(aiResponse);
        try {
            JsonNode responseNode = objectMapper.readTree(jsonPayload);
            normalizeStringListFields(responseNode);
            stripMilestoneGuidanceFromSow(responseNode);
            normalizeBudgetFields(responseNode);
            normalizeDurationFields(responseNode);
            return objectMapper.treeToValue(responseNode, GenerateSowResponse.class);
        } catch (JsonProcessingException ex) {
            throw new AppException("AI response khong phai JSON hop le: " + truncate(ex.getOriginalMessage(), 200));
        }
    }

    // Note: Hàm chuẩn hóa các field dạng danh sách để nếu AI trả chuỗi đơn thì hệ thống vẫn đọc được.
    private void normalizeStringListFields(JsonNode responseNode) {
        if (!(responseNode instanceof ObjectNode response)) {
            return;
        }

        normalizeArrayField(response, "questions");

        JsonNode sowNode = response.get("sow");
        if (sowNode instanceof ObjectNode sow) {
            normalizeArrayField(sow, "objectives");
            normalizeArrayField(sow, "scopeOfWork");
            normalizeArrayField(sow, "deliverables");
            normalizeArrayField(sow, "assumptions");
            normalizeArrayField(sow, "outOfScope");

        }
    }

    // Note: Hàm chuyển một field không phải array thành array một phần tử để đúng schema response.
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

    private void stripMilestoneGuidanceFromSow(JsonNode responseNode) {
        JsonNode sowNode = responseNode.get("sow");
        if (!(sowNode instanceof ObjectNode sow)) {
            return;
        }
        Pattern milestoneSectionStart = Pattern.compile(
                "(?i)(recommended|suggested|proposed)\\s+milestones?\\s*:"
                + "|milestone\\s+(recommendations?|breakdown|plan)\\s*:",
                Pattern.UNICODE_CHARACTER_CLASS);
        stripFromTextField(sow, "scopeOfWork", milestoneSectionStart);
        stripFromTextField(sow, "deliverables", milestoneSectionStart);
    }

    private void stripFromTextField(ObjectNode sow, String fieldName, Pattern sectionPattern) {
        JsonNode fieldNode = sow.get(fieldName);
        if (!(fieldNode instanceof ArrayNode items)) {
            return;
        }
        ArrayNode cleaned = objectMapper.createArrayNode();
        boolean inMilestoneSection = false;
        for (JsonNode item : items) {
            String text = item.isTextual() ? item.asText() : "";
            if (sectionPattern.matcher(text).find()) {
                inMilestoneSection = true;
                continue;
            }
            if (inMilestoneSection) {
                if (!text.isBlank() && !isMilestoneListItem(text)) {
                    inMilestoneSection = false;
                    cleaned.add(text);
                }
                continue;
            }
            cleaned.add(text);
        }
        sow.set(fieldName, cleaned);
    }

    private boolean isMilestoneListItem(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (trimmed.matches("^\\d+\\..*")) {
            return true;
        }
        if (trimmed.matches("^[-•*]\\s+.*")) {
            return true;
        }
        return false;
    }

    // Note: Hàm chuẩn hóa budget milestone khi AI trả tiền dạng text như "30 triệu" hoặc có ký tự phân cách.
    private void normalizeBudgetFields(JsonNode responseNode) {
        JsonNode milestonesNode = responseNode.get("milestones");
        if (!(milestonesNode instanceof ArrayNode milestones)) {
            return;
        }

        for (JsonNode milestoneNode : milestones) {
            if (!(milestoneNode instanceof ObjectNode milestone)) {
                continue;
            }

            JsonNode budgetNode = milestone.get("budget");
            if (budgetNode == null || !budgetNode.isTextual()) {
                continue;
            }

            String normalizedBudget = normalizeMoneyText(budgetNode.asText());
            if (normalizedBudget.isBlank()) {
                milestone.putNull("budget");
            } else {
                milestone.put("budget", new BigDecimal(normalizedBudget));
            }
        }
    }

    private void normalizeDurationFields(JsonNode responseNode) {
        JsonNode milestonesNode = responseNode.get("milestones");
        if (!(milestonesNode instanceof ArrayNode milestones)) {
            return;
        }

        for (JsonNode milestoneNode : milestones) {
            if (!(milestoneNode instanceof ObjectNode milestone)) {
                continue;
            }

            JsonNode durationNode = milestone.get("duration");
            if (durationNode != null && durationNode.isTextual()) {
                String normalizedDuration = normalizeMoneyText(durationNode.asText());
                if (normalizedDuration.isBlank()) {
                    milestone.putNull("duration");
                } else {
                    milestone.put("duration", Integer.parseInt(normalizedDuration));
                }
            }
        }
    }

    private String normalizeMoneyText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String digits = value.replaceAll("[^0-9-]", "");
        if (digits.equals("-")) {
            return "";
        }
        return digits;
    }

    // Note: Hàm tách phần JSON thật từ response AI, kể cả khi AI bọc trong markdown code block.
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

    // Note: Hàm điều chỉnh tổng ngân sách các milestone khớp với ngân sách doanh nghiệp nhập.
    public void normalizeMilestoneBudget(GenerateSowResponse response, BigDecimal totalBudget) {
        List<MilestoneDto> milestones = response.getMilestones();
        if (milestones == null || milestones.isEmpty()) {
            return;
        }

        boolean hasInvalidBudget = milestones.stream()
                .anyMatch(milestone -> milestone.getBudget() == null || milestone.getBudget().compareTo(BigDecimal.ZERO) < 0);

        if (hasInvalidBudget) {
            distributeEqually(milestones, totalBudget);
            return;
        }

        BigDecimal currentTotal = milestones.stream()
                .map(MilestoneDto::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentTotal.compareTo(BigDecimal.ZERO) <= 0) {
            distributeEqually(milestones, totalBudget);
            return;
        }

        if (currentTotal.compareTo(totalBudget) == 0) {
            return;
        }

        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < milestones.size(); i++) {
            MilestoneDto milestone = milestones.get(i);
            BigDecimal normalizedBudget;

            if (i == milestones.size() - 1) {
                normalizedBudget = totalBudget.subtract(allocated);
            } else {
                normalizedBudget = milestone.getBudget()
                        .multiply(totalBudget)
                        .divide(currentTotal, 0, RoundingMode.HALF_UP);
                allocated = allocated.add(normalizedBudget);
            }

            milestone.setBudget(normalizedBudget);
        }
    }

    public void normalizeMilestoneDuration(GenerateSowResponse response, Integer totalDuration, String durationUnit) {
        List<MilestoneDto> milestones = response.getMilestones();
        if (milestones == null || milestones.isEmpty() || totalDuration == null || totalDuration <= 0) {
            return;
        }

        boolean hasInvalidDuration = milestones.stream()
                .anyMatch(milestone -> milestone.getDuration() == null || milestone.getDuration() <= 0);

        boolean hasMismatchedUnit = milestones.stream()
                .map(MilestoneDto::getDurationUnit)
                .anyMatch(unit -> unit == null || unit.isBlank() || !isSameDurationUnit(unit, durationUnit));

        if (hasInvalidDuration || hasMismatchedUnit) {
            distributeDurationEqually(milestones, totalDuration, durationUnit);
            return;
        }

        int currentTotal = milestones.stream()
                .map(MilestoneDto::getDuration)
                .reduce(0, Integer::sum);

        if (currentTotal <= 0) {
            distributeDurationEqually(milestones, totalDuration, durationUnit);
            return;
        }

        if (currentTotal == totalDuration) {
            milestones.forEach(milestone -> milestone.setDurationUnit(durationUnit));
            return;
        }

        int allocated = 0;
        for (int i = 0; i < milestones.size(); i++) {
            MilestoneDto milestone = milestones.get(i);
            int normalizedDuration;

            if (i == milestones.size() - 1) {
                normalizedDuration = totalDuration - allocated;
            } else {
                normalizedDuration = BigDecimal.valueOf(milestone.getDuration())
                        .multiply(BigDecimal.valueOf(totalDuration))
                        .divide(BigDecimal.valueOf(currentTotal), 0, RoundingMode.HALF_UP)
                        .intValue();
                allocated += normalizedDuration;
            }

            milestone.setDuration(Math.max(normalizedDuration, 1));
            milestone.setDurationUnit(durationUnit);
        }

        rebalanceDurationTotal(milestones, totalDuration);
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

    // Note: Hàm gọi OpenAI có retry ngắn với lỗi 5xx để giảm lỗi tạm thời từ phía AI.
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

    // Note: Hàm xác định loại HTTP status nào nên thử gọi lại.
    private boolean shouldRetry(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError();
    }

    // Note: Hàm dựng body request theo Chat Completions API và ép AI trả JSON object.
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
        requestBody.put("temperature", 0.2);
        requestBody.put("response_format", responseFormat);
        return requestBody;
    }

    // Note: Hàm dựng header gọi AI, bao gồm Content-Type JSON và Bearer API key.
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());
        return headers;
    }

    // Note: Hàm lấy phần content text từ response Chat Completions của AI.
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

    // Note: Hàm chia đều ngân sách khi AI không trả budget hợp lệ cho milestone.
    private void distributeEqually(List<MilestoneDto> milestones, BigDecimal totalBudget) {
        BigDecimal baseBudget = totalBudget.divide(BigDecimal.valueOf(milestones.size()), 0, RoundingMode.DOWN);
        BigDecimal allocated = BigDecimal.ZERO;

        for (int i = 0; i < milestones.size(); i++) {
            BigDecimal milestoneBudget = i == milestones.size() - 1
                    ? totalBudget.subtract(allocated)
                    : baseBudget;
            milestones.get(i).setBudget(milestoneBudget);
            allocated = allocated.add(milestoneBudget);
        }
    }

    private void distributeDurationEqually(List<MilestoneDto> milestones, Integer totalDuration, String durationUnit) {
        int baseDuration = totalDuration / milestones.size();
        int allocated = 0;

        for (int i = 0; i < milestones.size(); i++) {
            int milestoneDuration = i == milestones.size() - 1
                    ? totalDuration - allocated
                    : baseDuration;
            milestones.get(i).setDuration(Math.max(milestoneDuration, 1));
            milestones.get(i).setDurationUnit(durationUnit);
            allocated += milestoneDuration;
        }

        rebalanceDurationTotal(milestones, totalDuration);
    }

    private void rebalanceDurationTotal(List<MilestoneDto> milestones, int totalDuration) {
        int diff = milestones.stream()
                .map(MilestoneDto::getDuration)
                .reduce(0, Integer::sum) - totalDuration;

        for (int i = milestones.size() - 1; diff > 0 && i >= 0; i--) {
            MilestoneDto milestone = milestones.get(i);
            while (diff > 0 && milestone.getDuration() > 1) {
                milestone.setDuration(milestone.getDuration() - 1);
                diff--;
            }
        }
    }

    private boolean isSameDurationUnit(String left, String right) {
        return normalizeDurationUnit(left).equals(normalizeDurationUnit(right));
    }

    private String normalizeDurationUnit(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? new ArrayList<>() : values;
    }

    // Note: Hàm rút gọn lỗi OpenAI để trả message đủ thông tin nhưng không quá dài.
    private String buildOpenAiErrorMessage(RestClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return "AI API loi: " + ex.getStatusCode();
        }

        return "AI API loi: " + ex.getStatusCode() + " - " + truncate(responseBody, 500);
    }

    // Note: Hàm cắt chuỗi dài, dùng cho thông báo lỗi từ AI hoặc JSON parser.
    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }
}
