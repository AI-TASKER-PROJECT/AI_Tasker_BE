package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.BadGatewayException;
import com.aitasker.be.config.OpenAiProperties;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiCompletionService {
    private static final String SYSTEM_INSTRUCTIONS = """
            Bạn là chatbot nội bộ của AITasker.
            Chỉ trả lời dựa trên tài liệu nội bộ được cung cấp trong context.
            Nếu context không đủ thông tin, hãy nói: Hiện tại tôi chưa có đủ thông tin để trả lời câu hỏi này.
            Trả lời ngắn gọn, rõ ràng, bằng tiếng Việt.
            """;

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;

    public String generateAnswer(String question, Map<String, String> contexts) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new BadGatewayException("Chua cau hinh OPENAI_API_KEY");
        }

        Map<String, Object> requestBody = buildRequestBody(question, contexts);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, buildHeaders());

        try {
            ResponseEntity<Map> response = callOpenAi(request);
            return extractAnswer(response.getBody());
        } catch (RestClientResponseException ex) {
            throw new BadGatewayException(buildOpenAiErrorMessage(ex));
        } catch (RestClientException ex) {
            throw new BadGatewayException("Khong goi duoc OpenAI API");
        }
    }

    private ResponseEntity<Map> callOpenAi(HttpEntity<Map<String, Object>> request) {
        RestClientResponseException lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return restTemplate.exchange(
                        openAiProperties.getResponsesUrl(),
                        HttpMethod.POST,
                        request,
                        Map.class
                );
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

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());
        return headers;
    }

    private Map<String, Object> buildRequestBody(String question, Map<String, String> contexts) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("instructions", SYSTEM_INSTRUCTIONS);
        requestBody.put("input", buildPrompt(question, contexts));
        requestBody.put("max_output_tokens", openAiProperties.getMaxOutputTokens());
        return requestBody;
    }

    private String buildPrompt(String question, Map<String, String> contexts) {
        String contextText = contexts.entrySet().stream()
                .map(entry -> "Nguon: " + entry.getKey() + "\n" + entry.getValue())
                .collect(Collectors.joining("\n\n"));

        return """
                Context:
                %s

                Cau hoi:
                %s
                """.formatted(contextText, question);
    }

    private String extractAnswer(Map<?, ?> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new BadGatewayException("OpenAI khong tra ve response hop le");
        }

        String outputText = getString(responseBody, "output_text");
        if (outputText != null) {
            return outputText;
        }

        Object output = responseBody.get("output");
        if (!(output instanceof List<?> outputItems)) {
            throw new BadGatewayException("OpenAI khong tra ve cau tra loi hop le");
        }

        List<String> texts = new ArrayList<>();
        for (Object outputItem : outputItems) {
            collectTextFromOutputItem(outputItem, texts);
        }

        String answer = texts.stream()
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n"))
                .trim();

        if (answer.isBlank()) {
            throw new BadGatewayException("OpenAI khong tra ve noi dung cau tra loi");
        }

        return answer;
    }

    private void collectTextFromOutputItem(Object outputItem, List<String> texts) {
        if (!(outputItem instanceof Map<?, ?> item)) {
            return;
        }

        Object content = item.get("content");
        if (!(content instanceof List<?> contentItems)) {
            return;
        }

        for (Object contentItem : contentItems) {
            if (contentItem instanceof Map<?, ?> contentMap) {
                String text = getString(contentMap, "text");
                if (text != null) {
                    texts.add(text);
                }
            }
        }
    }

    private String getString(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private String buildOpenAiErrorMessage(RestClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return "OpenAI API loi: " + ex.getStatusCode();
        }

        return "OpenAI API loi: " + ex.getStatusCode() + " - " + truncate(responseBody, 500);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }
}
