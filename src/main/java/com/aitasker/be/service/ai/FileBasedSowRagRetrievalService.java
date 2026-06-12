package com.aitasker.be.service.ai;

import com.aitasker.be.dto.sow.GenerateSowRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileBasedSowRagRetrievalService implements RagRetrievalService {
    private static final String SOW_KNOWLEDGE_PATH = "knowledge/sow/";

    @Value("${openai.sow.rag.enabled:true}")
    private boolean ragEnabled = true;

    @Override
    public String retrieveContext(GenerateSowRequest request) {
        if (!ragEnabled) {
            return "";
        }

        try {
            String contextFile = resolveContextFile(request);
            return readMarkdown(contextFile);
        } catch (Exception ex) {
            log.warn("RAG retrieval failed for Generate SoW. Continue without RAG context.", ex);
            return "";
        }
    }

    private String resolveContextFile(GenerateSowRequest request) {
        String query = normalize(buildSearchQuery(request));

        if (containsAny(query, "chatbot", "customer support", "cham soc khach hang", "rag", "tra cuu don hang")) {
            return "customer-support-chatbot.md";
        }

        if (containsAny(query, "computer vision", "image", "camera", "detection", "nhan dien")) {
            return "computer-vision.md";
        }

        if (containsAny(query, "dashboard", "report", "analytics", "bao cao") || containsToken(query, "bi")) {
            return "bi-dashboard.md";
        }

        if (containsAny(query, "data pipeline", "etl", "data engineering", "warehouse", "lakehouse")) {
            return "data-pipeline.md";
        }

        if (containsAny(query, "api testing", "swagger", "postman", "automation test")) {
            return "api-testing.md";
        }

        return "generic-ai-project.md";
    }

    private String buildSearchQuery(GenerateSowRequest request) {
        return String.join(" ",
                safe(request.getProjectTitle()),
                safe(request.getRawRequirement()),
                String.valueOf(request.getSupportFields()),
                String.valueOf(request.getRequiredSkills())
        );
    }

    private String readMarkdown(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(SOW_KNOWLEDGE_PATH + fileName);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private boolean containsAny(String query, String... keywords) {
        for (String keyword : keywords) {
            if (query.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsToken(String query, String token) {
        return query.matches(".*\\b" + token.toLowerCase(Locale.ROOT) + "\\b.*");
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'd');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
