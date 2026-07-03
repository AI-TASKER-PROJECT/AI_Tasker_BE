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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    void buildPrompt_shouldForbidMilestoneGuidanceInSowFields() {
        String prompt = service.buildPrompt(buildRequest(), "RAG with Recommended milestones");

        assertTrue(prompt.contains("Tuyet doi khong liet ke milestones"));
        assertTrue(prompt.contains("sow.overview"));
        assertTrue(prompt.contains("sow.scopeOfWork"));
        assertTrue(prompt.contains("sow.deliverables"));
        assertTrue(prompt.contains("array milestones"));
    }

    @Test
    void buildPrompt_shouldAlwaysRequireDraftAndAllowAtMostThreeOptionalQuestions() {
        String prompt = service.buildPrompt(buildRequest(), "RAG context");

        // Luon sinh draft + milestones khong rong
        assertTrue(prompt.contains("Luon sinh draft SoW day du"));
        assertTrue(prompt.contains("milestones khong"));
        // Bat buoc suy luan gia dinh vao sow.assumptions
        assertTrue(prompt.contains("sow.assumptions"));
        assertTrue(prompt.contains("SUY LUAN gia dinh"));
        // Cho phep toi da 3 cau hoi optional, advisory, khong chặn
        assertTrue(prompt.contains("TOI DA 3 cau hoi"));
        assertTrue(prompt.contains("advisory"));
        // needMoreInfo=true chi khi questions khong rong
        assertTrue(prompt.contains("needMoreInfo=true Chi khi questions khong rong"));
        // Khong bo sot sow/milestones vi co questions
        assertTrue(prompt.contains("Khong bao gio bo sot sow hay milestones vi co questions"));
        assertTrue(prompt.contains("acceptanceCriteria"));
        assertTrue(prompt.contains("Khong dung catalog"));
    }

    @Test
    void buildRecoveryPrompt_shouldForceCompleteDraftAndNotRepeatQuestionOnlyResponse() {
        String prompt = service.buildRecoveryPrompt(buildRequest(), "RAG context");

        assertTrue(prompt.contains("BUOC PHUC HOI NOI BO"));
        assertTrue(prompt.contains("bat buoc sinh ngay SoW day du"));
        assertTrue(prompt.contains("sow.assumptions"));
        assertTrue(prompt.contains("toi da 3 cau hoi optional"));
    }

    @Test
    void parseAiResponse_shouldStripRecommendedMilestonesBlockFromAllSowFields() {
        GenerateSowResponse response = service.parseAiResponse("""
                {
                  "needMoreInfo": false,
                  "questions": [],
                  "sow": {
                    "title": "AI Chatbot",
                    "overview": "Build AI chatbot for customer support. Recommended milestones: 1. Discovery 2. Build 3. Deploy. The project will use RAG technology.",
                    "objectives": ["Answer customer questions"],
                    "scopeOfWork": ["Design system", "Recommended milestones:", "1. Architecture", "2. Development", "3. Testing", "Scalability planning"],
                    "deliverables": ["Chatbot API", "Recommended milestones:", "1. MVP delivery", "2. Final product", "Documentation"],
                    "assumptions": ["API available"],
                    "outOfScope": ["CRM rebuild"]
                  },
                  "milestones": [
                    {
                      "name": "Discovery",
                      "description": "Analyze requirements",
                      "duration": 2,
                      "durationUnit": "tuan",
                      "budget": 30
                    },
                    {
                      "name": "Build",
                      "description": "Develop chatbot",
                      "duration": 4,
                      "durationUnit": "tuan",
                      "budget": 50
                    }
                  ]
                }
                """);

        assertFalse(response.getNeedMoreInfo());
        assertEquals("Build AI chatbot for customer support. The project will use RAG technology.",
                response.getSow().getOverview());
        assertEquals(List.of("Design system", "Scalability planning"),
                response.getSow().getScopeOfWork());
        assertEquals(List.of("Chatbot API", "Documentation"),
                response.getSow().getDeliverables());
        assertEquals(2, response.getMilestones().size());
        assertEquals("Discovery", response.getMilestones().get(0).getName());
    }

    @Test
    void parseAiResponse_shouldKeepSowFieldsWhenNoMilestoneDuplication() {
        GenerateSowResponse response = service.parseAiResponse("""
                {
                  "needMoreInfo": false,
                  "questions": [],
                  "sow": {
                    "title": "AI support bot",
                    "overview": "Build bot for Q&A automation",
                    "objectives": [],
                    "scopeOfWork": ["Design", "Develop"],
                    "deliverables": ["Bot API"],
                    "assumptions": [],
                    "outOfScope": []
                  },
                  "milestones": [
                    {
                      "name": "Build",
                      "description": "Develop bot",
                      "duration": 1,
                      "durationUnit": "tuan",
                      "budget": 90000000
                    }
                  ]
                }
                """);

        assertEquals("Build bot for Q&A automation", response.getSow().getOverview());
        assertEquals(List.of("Design", "Develop"), response.getSow().getScopeOfWork());
        assertEquals(List.of("Bot API"), response.getSow().getDeliverables());
    }

    @Test
    void parseAiResponse_shouldStripExtendedMilestoneBlockHeaders() {
        GenerateSowResponse response = service.parseAiResponse("""
                {
                  "needMoreInfo": false,
                  "questions": [],
                  "sow": {
                    "title": "OCR project",
                    "overview": "OCR pipeline with document parsing. Suggested milestones: 1. Setup 2. Train. Backend integration follows.",
                    "objectives": [],
                    "scopeOfWork": [
                        "Document analysis",
                        "Proposed milestones:",
                        "1. Preprocessing",
                        "2. Model training",
                        "3. Validation",
                        "API integration"
                    ],
                    "deliverables": [
                        "OCR service",
                        "Milestone breakdown:",
                        "1. MVP",
                        "2. Production",
                        "User guide"
                    ],
                    "assumptions": [],
                    "outOfScope": []
                  },
                  "milestones": [
                    {
                      "name": "Setup",
                      "description": "Initial setup",
                      "duration": 2,
                      "durationUnit": "tuan",
                      "budget": 40
                    }
                  ]
                }
                """);

        assertEquals("OCR pipeline with document parsing. Backend integration follows.",
                response.getSow().getOverview());
        assertEquals(List.of("Document analysis", "API integration"),
                response.getSow().getScopeOfWork());
        assertEquals(List.of("OCR service", "User guide"),
                response.getSow().getDeliverables());
    }

    @Test
    void parseAiResponse_shouldStripGenericMilestonesBlockFromOverview() {
        GenerateSowResponse response = service.parseAiResponse("""
                {
                  "needMoreInfo": false,
                  "questions": [],
                  "sow": {
                    "title": "AI assistant",
                    "overview": "Build internal AI assistant for support operations.\\n\\nMilestones:\\n- Moc 1: Discovery & Solution Design (Thoi gian: 3 WEEK)\\n- Moc 2: Knowledge Base & Data Preparation (Thoi gian: 3 WEEK)\\n- Moc 3: AI Assistant Development (Thoi gian: 3 WEEK)\\n- Moc 4: Integration & Testing (Thoi gian: 3 WEEK)\\n- Moc 5: Deployment & Handover (Thoi gian: 3 WEEK)",
                    "objectives": [],
                    "scopeOfWork": ["Design", "Develop"],
                    "deliverables": ["Assistant API"],
                    "assumptions": [],
                    "outOfScope": []
                  },
                  "milestones": [
                    {
                      "name": "Discovery",
                      "description": "Analyze requirements",
                      "duration": 3,
                      "durationUnit": "week",
                      "budget": 20
                    }
                  ]
                }
                """);

        assertEquals("Build internal AI assistant for support operations.",
                response.getSow().getOverview());
        assertEquals(List.of("Design", "Develop"), response.getSow().getScopeOfWork());
        assertEquals(List.of("Assistant API"), response.getSow().getDeliverables());
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
                              "acceptanceCriteria": ["Bot tra loi dung du lieu"],
                              "tasks": []
                            },
                            {
                              "name": "Deploy",
                              "description": "Deploy bot",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 50,
                              "acceptanceCriteria": ["Bot duoc trien khai thanh cong"],
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
    void normalizeMilestoneDuration_whenTotalIsDifferent_shouldScaleAndMatchInputDuration() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .milestones(List.of(
                        MilestoneDto.builder().duration(2).durationUnit("tuan").build(),
                        MilestoneDto.builder().duration(3).durationUnit("tuần").build()
                ))
                .build();

        service.normalizeMilestoneDuration(response, 10, "tuần");

        assertEquals(4, response.getMilestones().get(0).getDuration());
        assertEquals(6, response.getMilestones().get(1).getDuration());
        assertEquals("tuần", response.getMilestones().get(0).getDurationUnit());
        assertEquals("tuần", response.getMilestones().get(1).getDurationUnit());
    }

    @Test
    void normalizeMilestoneDuration_whenDurationIsMissing_shouldDistributeEqually() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .milestones(List.of(
                        MilestoneDto.builder().duration(null).build(),
                        MilestoneDto.builder().duration(3).durationUnit("week").build(),
                        MilestoneDto.builder().duration(3).durationUnit("week").build()
                ))
                .build();

        service.normalizeMilestoneDuration(response, 10, "tuần");

        assertEquals(3, response.getMilestones().get(0).getDuration());
        assertEquals(3, response.getMilestones().get(1).getDuration());
        assertEquals(4, response.getMilestones().get(2).getDuration());
        assertEquals("tuần", response.getMilestones().get(2).getDurationUnit());
    }

    @Test
    void generateSow_whenNeedMoreInfoTrue_shouldKeepSowMilestonesAndQuestionBatch() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": true,
                          "questions": ["Can bo sung API don hang?"],
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Answer customer questions"],
                            "scopeOfWork": ["Design", "Develop"],
                            "deliverables": ["Bot API"],
                            "assumptions": ["API don hang da co san"],
                            "outOfScope": ["CRM rebuild"]
                          },
                          "milestones": [
                            {
                              "name": "Build",
                              "description": "Develop bot",
                              "duration": 3,
                              "durationUnit": "tuan",
                              "budget": 100,
                              "acceptanceCriteria": ["Bot API hoat dong dung contract"]
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        // needMoreInfo=true advisory nhung van giu nguyen sow + milestones + question batch
        assertTrue(response.getNeedMoreInfo());
        assertEquals(1, response.getQuestions().size());
        assertEquals(1, response.getMilestones().size());
        assertEquals("AI support bot", response.getSow().getTitle());
        assertEquals(List.of("API don hang da co san"), response.getSow().getAssumptions());
        assertEquals(List.of("Bot API hoat dong dung contract"),
                response.getMilestones().get(0).getAcceptanceCriteria());
    }

    @Test
    void generateSow_whenSowAssumptionsMissing_shouldNormalizeToEmptyList() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": false,
                          "questions": [],
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Answer customer questions"],
                            "scopeOfWork": ["Design"],
                            "deliverables": ["Bot API"],
                            "outOfScope": []
                          },
                          "milestones": [
                            {
                              "name": "Build",
                              "description": "Develop bot",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 100,
                              "acceptanceCriteria": ["Bot tra loi dung knowledge base"]
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        assertFalse(response.getNeedMoreInfo());
        assertNotNull(response.getSow().getAssumptions());
        assertTrue(response.getSow().getAssumptions().isEmpty());
    }

    @Test
    void generateSow_shouldPreserveDomainSpecificAssumptionsFromModel() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": false,
                          "questions": [],
                          "sow": {
                            "title": "Data pipeline",
                            "overview": "ETL pipeline",
                            "objectives": ["Ingest daily"],
                            "scopeOfWork": ["Design schema"],
                            "deliverables": ["ETL service"],
                            "assumptions": [
                              "Du lieu nguon cap nhat hang ngay",
                              "Storage dung PostgreSQL",
                              "Monitoring dung log co ban"
                            ],
                            "outOfScope": []
                          },
                          "milestones": [
                            {
                              "name": "Pipeline",
                              "description": "Build ETL",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 100,
                              "acceptanceCriteria": ["Pipeline xu ly du lieu hang ngay"]
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        assertEquals(List.of(
                "Du lieu nguon cap nhat hang ngay",
                "Storage dung PostgreSQL",
                "Monitoring dung log co ban"
        ), response.getSow().getAssumptions());
    }

    @Test
    void generateSow_whenModelReturnsMoreThanThreeQuestions_shouldLimitToThree() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": true,
                          "questions": ["Q1?", "Q2?", "Q3?", "Q4?", "Q5?"],
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Answer"],
                            "scopeOfWork": ["Design"],
                            "deliverables": ["Bot API"],
                            "assumptions": [],
                            "outOfScope": []
                          },
                          "milestones": [
                            {
                              "name": "Build",
                              "description": "Develop bot",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 100,
                              "acceptanceCriteria": ["Bot API hoat dong dung contract"]
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        assertTrue(response.getNeedMoreInfo());
        assertEquals(3, response.getQuestions().size());
        assertEquals(List.of("Q1?", "Q2?", "Q3?"), response.getQuestions());
    }

    @Test
    void generateSow_shouldDropNullBlankAndDuplicateQuestionsBeforeLimitingToThree() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": true,
                          "questions": [
                            null,
                            "  ",
                            "Can bo sung API don hang?",
                            "Can bo sung API don hang?",
                            "CAN BO SUNG API DON HANG?",
                            "He thong ho tro bao nhieu ngon ngu?",
                            "Q4?",
                            "Q5?"
                          ],
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Answer"],
                            "scopeOfWork": ["Design"],
                            "deliverables": ["Bot API"],
                            "assumptions": [],
                            "outOfScope": []
                          },
                          "milestones": [
                            {
                              "name": "Build",
                              "description": "Develop bot",
                              "duration": 1,
                              "durationUnit": "tuan",
                              "budget": 100,
                              "acceptanceCriteria": ["Bot API hoat dong dung contract"]
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        // Bo null, rong/blank, va trung nhau (khong phan biet hoa/khoang trang).
        // Con lai 3 duy nhat: 1 cau API don hang + 1 cau ngon ngu + Q4, Q5 bi gioi han.
        assertTrue(response.getNeedMoreInfo());
        assertEquals(3, response.getQuestions().size());
        assertEquals("Can bo sung API don hang?", response.getQuestions().get(0));
        assertEquals("He thong ho tro bao nhieu ngon ngu?", response.getQuestions().get(1));
        assertEquals("Q4?", response.getQuestions().get(2));
    }

    @Test
    void generateSow_whenQuestionOnlyFirstResponse_shouldRecoverOnSingleInternalRetry() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        String questionOnly = """
                {
                  "needMoreInfo": true,
                  "questions": ["Can bo sung API don hang?"],
                  "sow": null,
                  "milestones": []
                }
                """;
        String recoveredDraft = """
                {
                  "needMoreInfo": false,
                  "questions": [],
                  "sow": {
                    "title": "AI support bot",
                    "overview": "Build bot",
                    "objectives": ["Answer customer questions"],
                    "scopeOfWork": ["Design", "Develop"],
                    "deliverables": ["Bot API"],
                    "assumptions": ["API don hang da co san"],
                    "outOfScope": []
                  },
                  "milestones": [
                    {
                      "name": "Build",
                      "description": "Develop bot",
                      "duration": 1,
                      "durationUnit": "tuan",
                      "budget": 100,
                      "acceptanceCriteria": ["Bot API hoat dong dung contract"]
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse(questionOnly)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse(recoveredDraft)));

        GenerateSowResponse response = localService.generateSow(buildRequest());

        // Dung 2 lan goi AI (1 ban dau + 1 recovery), khong loop
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
        assertFalse(response.getNeedMoreInfo());
        assertEquals("AI support bot", response.getSow().getTitle());
        assertEquals(1, response.getMilestones().size());
        assertEquals(List.of("API don hang da co san"), response.getSow().getAssumptions());
    }

    @Test
    void generateSow_whenRecoveryRetryStillFails_shouldThrowAndNotLoop() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);

        String questionOnly = """
                {
                  "needMoreInfo": true,
                  "questions": ["Can bo sung API don hang?"],
                  "sow": null,
                  "milestones": []
                }
                """;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse(questionOnly)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse(questionOnly)));

        AppException ex = assertThrows(AppException.class, () -> localService.generateSow(buildRequest()));

        // Dung 2 lan goi AI roi dung, khong retry vo tan
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
        assertTrue(ex.getMessage().contains("AI response thieu thong tin sow hoac milestones sau recovery"));
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
