package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.sow.GenerateSowRequest;
import com.aitasker.be.dto.sow.GenerateSowResponse;
import com.aitasker.be.dto.sow.MilestoneDto;
import com.aitasker.be.service.ai.FileBasedSowRagRetrievalService;
import com.aitasker.be.service.ai.RagRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSowGenerationServiceTest {

    private RagRetrievalService ragRetrievalService;
    private AiSowGenerationService service;

    @BeforeEach
    void setUp() {
        ragRetrievalService = mock(RagRetrievalService.class);
        when(ragRetrievalService.retrieveContext(any())).thenReturn("Mock RAG context");
        service = new AiSowGenerationService(new RestTemplate(), new OpenAiProperties(), ragRetrievalService);
    }

    @Test
    void parseAiResponse_whenInvalidJson_shouldThrowAppException() {
        assertThrows(AppException.class, () -> service.parseAiResponse("not json"));
    }

    @Test
    void parseAiResponse_whenJsonIsWrappedInMarkdownFence_shouldParseSuccessfully() {
        GenerateSowResponse response = service.parseAiResponse("""
                ```json
                {
                  "needMoreInfo": true,
                  "questions": ["Can ban cung cap API don hang?"],
                  "sow": null,
                  "milestones": []
                }
                ```
                """);

        assertTrue(response.getNeedMoreInfo());
        assertEquals("Can ban cung cap API don hang?", response.getQuestions().get(0));
    }

    @Test
    void parseAiResponse_whenJsonHasExtraFieldsAndFormattedBudget_shouldParseSuccessfully() {
        GenerateSowResponse response = service.parseAiResponse("""
                {
                  "needMoreInfo": false,
                  "questions": [],
                  "unexpectedField": "ignored",
                  "sow": {
                    "title": "AI support bot",
                    "overview": "Build bot",
                    "objectives": [],
                    "scopeOfWork": [],
                    "deliverables": [],
                    "assumptions": [],
                    "outOfScope": [],
                    "acceptanceCriteria": [],
                    "extraSowField": "ignored"
                  },
                  "milestones": [
                    {
                      "name": "Build",
                      "description": "Develop bot",
                      "duration": 1,
                      "durationUnit": "tuan",
                      "budget": "90,000,000 VND",
                      "tasks": [],
                      "extraMilestoneField": "ignored"
                    }
                  ]
                }
                """);

        assertFalse(response.getNeedMoreInfo());
        assertEquals(BigDecimal.valueOf(90000000), response.getMilestones().get(0).getBudget());
    }

    @Test
    void parseAiResponse_whenStringListFieldsAreReturnedAsText_shouldWrapIntoLists() {
        GenerateSowResponse response = service.parseAiResponse("""
                {
                  "needMoreInfo": false,
                  "questions": "Can bo sung API don hang?",
                  "sow": {
                    "title": "AI support bot",
                    "overview": "Build bot",
                    "objectives": "Tu dong tra loi khach hang",
                    "scopeOfWork": "Xay chatbot",
                    "deliverables": "Chatbot API",
                    "assumptions": "Du lieu san pham co san",
                    "outOfScope": "Khong xay CRM",
                    "acceptanceCriteria": "Bot tra loi dung theo knowledge base"
                  },
                  "milestones": [
                    {
                      "name": "Build",
                      "description": "Develop bot",
                      "duration": 1,
                      "durationUnit": "tuan",
                      "budget": 90000000,
                      "tasks": []
                    }
                  ]
                }
                """);

        assertEquals("Can bo sung API don hang?", response.getQuestions().get(0));
        assertEquals("Tu dong tra loi khach hang", response.getSow().getObjectives().get(0));
        assertEquals("Du lieu san pham co san", response.getSow().getAssumptions().get(0));
    }

    @Test
    void buildPrompt_shouldIncludeRagContext() {
        String prompt = service.buildPrompt(buildRequest(), "RAG policy context");

        assertTrue(prompt.contains("RAG CONTEXT:"));
        assertTrue(prompt.contains("RAG policy context"));
        assertTrue(prompt.contains("Project title: AI support bot"));
    }

    @Test
    void generateSow_shouldContinueWhenRagContextEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        RagRetrievalService emptyRagRetrievalService = mock(RagRetrievalService.class);
        when(emptyRagRetrievalService.retrieveContext(any())).thenReturn("");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, emptyRagRetrievalService);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": false,
                          "questions": [],
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Answer customer questions"],
                            "scopeOfWork": ["Design", "Develop"],
                            "deliverables": ["Bot API"],
                            "assumptions": ["API available"],
                            "outOfScope": ["CRM rebuild"],
                            "acceptanceCriteria": ["Bot answers from data"]
                          },
                          "milestones": [
                            {
                              "name": "Build",
                              "description": "Develop bot",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 50,
                              "tasks": []
                            },
                            {
                              "name": "Deploy",
                              "description": "Deploy bot",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 50,
                              "tasks": []
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        assertFalse(response.getNeedMoreInfo());
        assertEquals(BigDecimal.valueOf(90), response.getMilestones().get(0).getBudget());
        assertEquals(BigDecimal.valueOf(90), response.getMilestones().get(1).getBudget());
    }

    @Test
    void normalizeMilestoneBudget_whenTotalIsDifferent_shouldScaleAndMatchInputBudget() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .milestones(List.of(
                        MilestoneDto.builder().budget(BigDecimal.valueOf(20)).build(),
                        MilestoneDto.builder().budget(BigDecimal.valueOf(30)).build()
                ))
                .build();

        service.normalizeMilestoneBudget(response, BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(40), response.getMilestones().get(0).getBudget());
        assertEquals(BigDecimal.valueOf(60), response.getMilestones().get(1).getBudget());
    }

    @Test
    void normalizeMilestoneBudget_whenBudgetIsMissing_shouldDistributeEqually() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .milestones(List.of(
                        MilestoneDto.builder().budget(null).build(),
                        MilestoneDto.builder().budget(BigDecimal.valueOf(30)).build(),
                        MilestoneDto.builder().budget(BigDecimal.valueOf(30)).build()
                ))
                .build();

        service.normalizeMilestoneBudget(response, BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(33), response.getMilestones().get(0).getBudget());
        assertEquals(BigDecimal.valueOf(33), response.getMilestones().get(1).getBudget());
        assertEquals(BigDecimal.valueOf(34), response.getMilestones().get(2).getBudget());
    }

    @Test
    void ragRetrieval_shouldReturnCustomerSupportContext() {
        FileBasedSowRagRetrievalService ragService = new FileBasedSowRagRetrievalService();

        String context = ragService.retrieveContext(buildRequest());

        assertTrue(context.contains("For RAG customer support chatbot projects"));
    }

    private GenerateSowRequest buildRequest() {
        return GenerateSowRequest.builder()
                .projectTitle("AI support bot")
                .rawRequirement("Can chatbot tra loi san pham va tra cuu don hang")
                .budget(BigDecimal.valueOf(180))
                .duration(10)
                .durationUnit("tuan")
                .supportFields(List.of("Generative AI Applications"))
                .requiredSkills(List.of("API Testing & Swagger"))
                .build();
    }

    private Map<String, Object> buildOpenAiResponse(String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("content", content);

        Map<String, Object> choice = new LinkedHashMap<>();
        choice.put("message", message);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("choices", List.of(choice));
        return response;
    }
}
