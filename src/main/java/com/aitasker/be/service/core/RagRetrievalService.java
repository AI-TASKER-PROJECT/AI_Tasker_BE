package com.aitasker.be.service.core;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class RagRetrievalService {
    private static final String KNOWLEDGE_PATH = "knowledge/chatbot/";

    public Map<String, String> retrieveRelevantContext(String question) {
        String sourceFile = resolveSourceFile(question);
        Map<String, String> contexts = new LinkedHashMap<>();
        contexts.put(sourceFile, readMarkdown(sourceFile));
        return contexts;
    }

    private String resolveSourceFile(String question) {
        String normalizedQuestion = question == null ? "" : question.toLowerCase(Locale.ROOT);

        if (containsAny(normalizedQuestion, "đăng ký", "mã số thuế", "kyb", "kyc")) {
            return "registration.md";
        }

        if (containsAny(normalizedQuestion, "đăng nhập", "token", "jwt", "phân quyền")) {
            return "auth.md";
        }

        if (containsAny(normalizedQuestion, "job", "proposal", "đấu thầu", "chuyên gia")) {
            return "job.md";
        }

        if (containsAny(normalizedQuestion, "hợp đồng", "nda", "milestone", "nghiệm thu")) {
            return "contract.md";
        }

        if (containsAny(normalizedQuestion, "thanh toán", "ký quỹ", "vnpay", "escrow")) {
            return "payment.md";
        }

        if (containsAny(normalizedQuestion, "tranh chấp", "khiếu nại", "hoàn tiền", "dispute")) {
            return "dispute.md";
        }

        return "faq.md";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String readMarkdown(String fileName) {
        ClassPathResource resource = new ClassPathResource(KNOWLEDGE_PATH + fileName);
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Khong doc duoc tai lieu noi bo: " + fileName, ex);
        }
    }
}
