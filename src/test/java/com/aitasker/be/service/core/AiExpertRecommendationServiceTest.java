package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.recommendation.AiExpertRankingResponseDto;
import com.aitasker.be.dto.recommendation.ExpertCandidateDto;
import com.aitasker.be.dto.recommendation.SowKeywordsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiExpertRecommendationServiceTest {
    private AiExpertRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new AiExpertRecommendationService(new RestTemplate(), new OpenAiProperties());
    }

    @Test
    void parseKeywordResponse_whenAliasesAndStringFields_shouldNormalize() {
        SowKeywordsDto keywords = service.parseKeywordResponse("""
                ```json
                {
                  "requiredSkills": "RAG Architecture",
                  "domain": "Generative AI Applications",
                  "industry": "E-commerce",
                  "projectScope": ["Build chatbot"],
                  "budgetRange": "120,000,000 VND",
                  "timeline": "",
                  "experienceLevel": "",
                  "deliverables": "Chatbot API",
                  "keywords": "LLM"
                }
                ```
                """);

        assertEquals("RAG Architecture", keywords.getRequiredSkills().get(0));
        assertEquals("Generative AI Applications", keywords.getDomains().get(0));
        assertEquals("E-commerce", keywords.getIndustries().get(0));
        assertEquals(BigDecimal.valueOf(120000000), keywords.getBudgetRange().getMax());
        assertEquals("unspecified", keywords.getTimeline());
        assertEquals("unspecified", keywords.getExperienceLevel());
        assertEquals("Chatbot API", keywords.getDeliverables().get(0));
    }

    @Test
    void parseRankingResponse_whenScoreAndSkillsAreStrings_shouldNormalize() {
        AiExpertRankingResponseDto response = service.parseRankingResponse("""
                {
                  "recommendations": [
                    {
                      "expertId": 1,
                      "fullName": "Tran Hoang Nam",
                      "matchScore": "94%",
                      "matchedSkills": "RAG Architecture",
                      "reason": "Strong RAG delivery",
                      "riskNotes": "Check availability",
                      "suggestedRole": "Lead AI Engineer"
                    }
                  ],
                  "note": "Only one strong candidate"
                }
                """);

        assertEquals(1, response.getRecommendations().size());
        assertEquals(BigDecimal.valueOf(94), response.getRecommendations().get(0).getMatchScore());
        assertEquals("RAG Architecture", response.getRecommendations().get(0).getMatchedSkills().get(0));
        assertEquals("Only one strong candidate", response.getNote());
    }

    @Test
    void parseRankingResponse_whenInvalidJson_shouldThrowAppException() {
        assertThrows(AppException.class, () -> service.parseRankingResponse("not json"));
    }

    @Test
    void buildRankingPrompt_shouldIncludeOnlyCandidatePayloadAndFixedSchema() {
        String prompt = service.buildRankingPrompt(
                "Build RAG chatbot",
                SowKeywordsDto.builder().requiredSkills(List.of("RAG Architecture")).build(),
                List.of(ExpertCandidateDto.builder()
                        .expertId(1)
                        .fullName("Tran Hoang Nam")
                        .skills(List.of("RAG Architecture"))
                        .build())
        );

        assertTrue(prompt.contains("\"expertId\" : 1"));
        assertTrue(prompt.contains("\"recommendations\""));
        assertTrue(prompt.contains("Khong duoc de xuat expert ngoai danh sach candidateExperts"));
    }
}
