package com.aitasker.be.service.core;

import com.aitasker.be.config.OpenAiProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCompletionServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void generateAnswer_shouldUseDedicatedChatbotModel() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("test-key");
        properties.setModel("gpt-5.6-terra");

        when(restTemplate.exchange(
                eq(properties.getResponsesUrl()),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of("output_text", "Cau tra loi")));

        AiCompletionService service = new AiCompletionService(restTemplate, properties);

        assertEquals("Cau tra loi", service.generateAnswer("Cau hoi", Map.of("auth.md", "Noi dung")));

        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(properties.getResponsesUrl()),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(Map.class)
        );

        Map<String, Object> requestBody = requestCaptor.getValue().getBody();
        assertEquals("gpt-4o-mini", requestBody.get("model"));
        assertEquals("gpt-5.6-terra", properties.getModel());
    }
}
