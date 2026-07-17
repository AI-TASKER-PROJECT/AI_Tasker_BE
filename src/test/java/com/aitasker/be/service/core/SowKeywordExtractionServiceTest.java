package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SowKeywordExtractionServiceTest {
    private SowKeywordExtractionService service;

    @BeforeEach
    void setUp() {
        service = new SowKeywordExtractionService(new ObjectMapper());
    }

    @Test
    void extractKeywordsFromSow_whenJsonHasSowAndMilestones_shouldGroupKeywords() {
        SowKeywordExtractionResult result = service.extractKeywordsFromSow("""
                {
                  "sow": {
                    "title": "Tro ly AI cham soc khach hang",
                    "overview": "Chatbot su dung RAG va knowledge base",
                    "objectives": ["Tu dong tra loi customer support"],
                    "scopeOfWork": ["Tich hop API don hang", "Kiem thu va trien khai"],
                    "deliverables": ["Spring Boot API"]
                  },
                  "milestones": [
                    {
                      "name": "Deployment",
                      "description": "NLP chatbot rollout"
                    }
                  ]
                }
                """);

        assertEquals(List.of(
                "AI",
                "Chatbot",
                "RAG / Knowledge Base",
                "API Integration",
                "Spring Boot",
                "NLP",
                "Testing",
                "Deployment"
        ), result.getSkills());
        assertEquals(List.of("Customer Support", "Order Management"), result.getDomains());
        assertTrue(result.getKeywords().containsAll(result.getSkills()));
        assertTrue(result.getKeywords().containsAll(result.getDomains()));
    }

    @Test
    void extractKeywordsFromSow_whenFieldsAreMissing_shouldNotCrash() {
        SowKeywordExtractionResult result = service.extractKeywordsFromSow("""
                {
                  "sow": {
                    "overview": "CRM chatbot"
                  }
                }
                """);

        assertEquals(List.of("Chatbot"), result.getSkills());
        assertEquals(List.of("CRM"), result.getDomains());
    }

    @Test
    void extractKeywordsFromSow_whenPayloadIsLegacyText_shouldExtractKeywords() {
        SowKeywordExtractionResult result = service.extractKeywordsFromSow(
                "Can chatbot tra cuu don hang bang knowledge base, tich hop API va trien khai production."
        );

        assertTrue(result.getSkills().contains("Chatbot"));
        assertTrue(result.getSkills().contains("RAG / Knowledge Base"));
        assertTrue(result.getSkills().contains("API Integration"));
        assertTrue(result.getSkills().contains("Deployment"));
        assertEquals(List.of("Order Management"), result.getDomains());
    }

    @ParameterizedTest
    @MethodSource("benchmarkFixtures")
    void extractKeywordsFromSow_shouldCoverRecommendationBenchmarkGroups(
            String sow,
            String expectedSkill,
            String expectedDomain
    ) {
        SowKeywordExtractionResult result = service.extractKeywordsFromSow(sow);

        assertTrue(result.getSkills().contains(expectedSkill), () -> "Missing skill for: " + sow);
        if (expectedDomain != null) {
            assertTrue(result.getDomains().contains(expectedDomain), () -> "Missing domain for: " + sow);
        }
    }

    private static Stream<Arguments> benchmarkFixtures() {
        return Stream.of(
                Arguments.of("Swagger and Postman API testing with regression testing", "API Testing", null),
                Arguments.of("BI dashboard for KPI reporting and business intelligence", "BI Dashboard", "Business Intelligence"),
                Arguments.of("Computer vision object detection and OCR pipeline", "Computer Vision", "Computer Vision"),
                Arguments.of("Data pipeline ETL into a data warehouse", "Data Pipeline", "Data Engineering"),
                Arguments.of("Build an artificial intelligence assistant", "AI", null),
                Arguments.of("Customer support chatbot using RAG knowledge base", "Chatbot", "Customer Support")
        );
    }
}
