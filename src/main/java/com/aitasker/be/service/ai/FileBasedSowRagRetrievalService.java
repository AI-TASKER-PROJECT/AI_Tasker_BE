/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/ai/FileBasedSowRagRetrievalService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
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

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
@Slf4j
public class FileBasedSowRagRetrievalService implements RagRetrievalService {
    private static final String SOW_KNOWLEDGE_PATH = "knowledge/sow/";

    // Note: Annotation nay inject gia tri cau hinh vao field hoac tham so.
    @Value("${openai.sow.rag.enabled:true}")
    private boolean ragEnabled = true;

    @Override
    // Note: Ham `retrieveContext` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
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

    // Note: Ham `resolveContextFile` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
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

    // Note: Ham `buildSearchQuery` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildSearchQuery(GenerateSowRequest request) {
        return String.join(" ",
                safe(request.getProjectTitle()),
                safe(request.getRawRequirement()),
                String.valueOf(request.getSupportFields()),
                String.valueOf(request.getRequiredSkills())
        );
    }

    // Note: Ham `readMarkdown` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String readMarkdown(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(SOW_KNOWLEDGE_PATH + fileName);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Note: Ham `containsAny` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean containsAny(String query, String... keywords) {
        for (String keyword : keywords) {
            if (query.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    // Note: Ham `containsToken` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean containsToken(String query, String token) {
        return query.matches(".*\\b" + token.toLowerCase(Locale.ROOT) + "\\b.*");
    }

    // Note: Ham `normalize` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String normalize(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'd');
        return normalized.toLowerCase(Locale.ROOT);
    }

    // Note: Ham `safe` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String safe(String value) {
        return value == null ? "" : value;
    }
}
