/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/EmbeddingService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class EmbeddingService {
    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;

    // Note: Ham `embed` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public List<Double> embed(String text) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new BadGatewayException("Chua cau hinh OPENAI_API_KEY");
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openAiProperties.getEmbeddingModel());
        requestBody.put("input", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, buildHeaders());

        try {
            ResponseEntity<Map> response = callOpenAi(request);
            return extractEmbedding(response.getBody());
        } catch (RestClientResponseException ex) {
            throw new BadGatewayException(buildOpenAiErrorMessage(ex));
        } catch (RestClientException ex) {
            throw new BadGatewayException("Khong goi duoc OpenAI Embedding API");
        }
    }

    // Note: Ham `toPgVector` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public static String toPgVector(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new BadGatewayException("OpenAI khong tra ve embedding hop le");
        }

        return embedding.stream()
                .map(EmbeddingService::formatVectorNumber)
                .collect(Collectors.joining(",", "[", "]"));
    }

    // Note: Ham `callOpenAi` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private ResponseEntity<Map> callOpenAi(HttpEntity<Map<String, Object>> request) {
        RestClientResponseException lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return restTemplate.exchange(
                        openAiProperties.getEmbeddingsUrl(),
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

    // Note: Ham `shouldRetry` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean shouldRetry(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError();
    }

    // Note: Ham `buildHeaders` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getApiKey());
        return headers;
    }

    // Note: Ham `extractEmbedding` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private List<Double> extractEmbedding(Map<?, ?> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new BadGatewayException("OpenAI khong tra ve embedding hop le");
        }

        Object data = responseBody.get("data");
        if (!(data instanceof List<?> dataItems) || dataItems.isEmpty()) {
            throw new BadGatewayException("OpenAI khong tra ve embedding hop le");
        }

        Object firstItem = dataItems.get(0);
        if (!(firstItem instanceof Map<?, ?> item)) {
            throw new BadGatewayException("OpenAI khong tra ve embedding hop le");
        }

        Object embedding = item.get("embedding");
        if (!(embedding instanceof List<?> values) || values.isEmpty()) {
            throw new BadGatewayException("OpenAI khong tra ve embedding hop le");
        }

        return values.stream()
                .map(this::toDouble)
                .toList();
    }

    // Note: Ham `toDouble` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Double toDouble(Object value) {
        if (!(value instanceof Number number)) {
            throw new BadGatewayException("OpenAI tra ve embedding khong dung dinh dang");
        }

        double doubleValue = number.doubleValue();
        if (!Double.isFinite(doubleValue)) {
            throw new BadGatewayException("OpenAI tra ve embedding khong dung dinh dang");
        }
        return doubleValue;
    }

    // Note: Ham `formatVectorNumber` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private static String formatVectorNumber(Double value) {
        if (value == null || !Double.isFinite(value)) {
            throw new BadGatewayException("Embedding khong dung dinh dang pgvector");
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    // Note: Ham `buildOpenAiErrorMessage` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildOpenAiErrorMessage(RestClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return "OpenAI Embedding API loi: " + ex.getStatusCode();
        }

        return "OpenAI Embedding API loi: " + ex.getStatusCode() + " - " + truncate(responseBody, 500);
    }

    // Note: Ham `truncate` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }
}
