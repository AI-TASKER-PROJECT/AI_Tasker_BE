package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.sow.BudgetAssessmentDto;
import com.aitasker.be.dto.sow.GenerateSowRequest;
import com.aitasker.be.dto.sow.GenerateSowResponse;
import com.aitasker.be.dto.sow.MilestoneBudgetReferenceDto;
import com.aitasker.be.dto.sow.MilestoneDto;
import com.aitasker.be.dto.sow.ReallocateSowBudgetRequest;
import com.aitasker.be.dto.sow.ReallocateSowBudgetResponse;
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
import static org.mockito.Mockito.verifyNoInteractions;
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
                  "budgetAssessment": {
                    "currency": "VND",
                    "estimatedMin": "70,000,000 VND",
                    "recommendedBudget": "90,000,000 VND",
                    "estimatedMax": "120,000,000 VND",
                    "confidence": "MEDIUM",
                    "factors": "Can tich hop API"
                  },
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
        assertEquals(BigDecimal.valueOf(70000000), response.getBudgetAssessment().getEstimatedMin());
        assertEquals(BigDecimal.valueOf(90000000), response.getBudgetAssessment().getRecommendedBudget());
        assertEquals(List.of("Can tich hop API"), response.getBudgetAssessment().getFactors());
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
        assertTrue(prompt.contains("budgetAssessment"));
        assertTrue(prompt.contains("DOC LAP"));
        assertTrue(prompt.contains("Business proposed budget"));
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
    void buildPrompt_whenClarificationAlreadyAsked_shouldForbidMoreQuestions() {
        GenerateSowRequest request = buildRequest();
        request.setClarificationAlreadyAsked(true);

        String prompt = service.buildPrompt(request, "RAG context");

        assertTrue(prompt.contains("KHONG duoc tra them cau hoi nao nua"));
        assertTrue(prompt.contains("needMoreInfo=false va questions=[]"));
        assertTrue(prompt.contains("sow.assumptions"));
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
    void normalizeBudgetAssessment_whenProviderEstimateIsMissing_shouldUseMilestoneFallback() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .milestones(List.of(
                        MilestoneDto.builder().budget(BigDecimal.valueOf(20)).build(),
                        MilestoneDto.builder().budget(BigDecimal.valueOf(30)).build()
                ))
                .build();

        service.normalizeBudgetAssessment(response, BigDecimal.valueOf(100));

        BudgetAssessmentDto assessment = response.getBudgetAssessment();
        assertEquals(BigDecimal.valueOf(100), assessment.getBusinessBudget());
        assertEquals(BigDecimal.valueOf(40), assessment.getEstimatedMin());
        assertEquals(BigDecimal.valueOf(50), assessment.getRecommendedBudget());
        assertEquals(BigDecimal.valueOf(60), assessment.getEstimatedMax());
        assertEquals("HIGH", assessment.getStatus());
        assertEquals("LOW", assessment.getConfidence());
        assertEquals("AI_MILESTONE_FALLBACK", assessment.getSource());
        assertTrue(assessment.getRequiresBusinessConfirmation());
    }

    @Test
    void normalizeBudgetAssessment_whenProviderAndMilestonesAreInvalid_shouldUseBusinessFallback() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .milestones(List.of(MilestoneDto.builder().budget(BigDecimal.ZERO).build()))
                .build();

        service.normalizeBudgetAssessment(response, BigDecimal.valueOf(100));

        BudgetAssessmentDto assessment = response.getBudgetAssessment();
        assertEquals(BigDecimal.valueOf(80), assessment.getEstimatedMin());
        assertEquals(BigDecimal.valueOf(100), assessment.getRecommendedBudget());
        assertEquals(BigDecimal.valueOf(120), assessment.getEstimatedMax());
        assertEquals("SUITABLE", assessment.getStatus());
        assertEquals("LOW", assessment.getConfidence());
        assertEquals("BUSINESS_BUDGET_FALLBACK", assessment.getSource());
    }

    @Test
    void reallocateSowBudget_shouldSortAndReturnExactProportionalWholeVndAllocations() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiSowGenerationService localService = new AiSowGenerationService(
                restTemplate, new OpenAiProperties(), ragRetrievalService);
        ReallocateSowBudgetRequest request = ReallocateSowBudgetRequest.builder()
                .selectedBudget(BigDecimal.valueOf(110_000_000))
                .milestones(List.of(
                        MilestoneBudgetReferenceDto.builder()
                                .milestoneIndex(1)
                                .referenceBudget(BigDecimal.valueOf(100_000_000))
                                .build(),
                        MilestoneBudgetReferenceDto.builder()
                                .milestoneIndex(0)
                                .referenceBudget(BigDecimal.valueOf(40_000_000))
                                .build()
                ))
                .build();

        ReallocateSowBudgetResponse response = localService.reallocateSowBudget(request);

        assertEquals("VND", response.getCurrency());
        assertEquals(BigDecimal.valueOf(110_000_000), response.getSelectedBudget());
        assertEquals(response.getSelectedBudget(), response.getAllocationTotal());
        assertEquals(0, response.getAllocations().get(0).getMilestoneIndex());
        assertEquals(BigDecimal.valueOf(31_428_571), response.getAllocations().get(0).getFundsAllocated());
        assertEquals(1, response.getAllocations().get(1).getMilestoneIndex());
        assertEquals(BigDecimal.valueOf(78_571_429), response.getAllocations().get(1).getFundsAllocated());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void reallocateSowBudget_whenSelectedBudgetIsSmallerThanMilestoneCount_shouldStayNonNegativeAndExact() {
        ReallocateSowBudgetRequest request = ReallocateSowBudgetRequest.builder()
                .selectedBudget(BigDecimal.valueOf(2))
                .milestones(List.of(
                        reference(0, 1),
                        reference(1, 1),
                        reference(2, 1),
                        reference(3, 1)
                ))
                .build();

        ReallocateSowBudgetResponse response = service.reallocateSowBudget(request);

        assertEquals(BigDecimal.valueOf(2), response.getAllocationTotal());
        assertTrue(response.getAllocations().stream()
                .allMatch(item -> item.getFundsAllocated().compareTo(BigDecimal.ZERO) >= 0));
        assertEquals(List.of(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2)),
                response.getAllocations().stream()
                        .map(item -> item.getFundsAllocated())
                        .toList());
    }

    @Test
    void reallocateSowBudget_whenMilestoneIndexIsDuplicated_shouldRejectRequest() {
        ReallocateSowBudgetRequest request = ReallocateSowBudgetRequest.builder()
                .selectedBudget(BigDecimal.valueOf(100))
                .milestones(List.of(reference(0, 40), reference(0, 60)))
                .build();

        AppException exception = assertThrows(
                AppException.class, () -> service.reallocateSowBudget(request));

        assertEquals("milestoneIndex bi trung: 0", exception.getMessage());
    }

    @Test
    void reallocateSowBudget_whenAmountHasFractionalVnd_shouldRejectRequest() {
        ReallocateSowBudgetRequest request = ReallocateSowBudgetRequest.builder()
                .selectedBudget(new BigDecimal("100.5"))
                .milestones(List.of(reference(0, 100)))
                .build();

        AppException exception = assertThrows(
                AppException.class, () -> service.reallocateSowBudget(request));

        assertEquals("selectedBudget phai la so VND nguyen lon hon 0", exception.getMessage());
    }

    @Test
    void normalizeBudgetAssessment_whenProviderRangeIsContradictory_shouldRepairAndLowerConfidence() {
        GenerateSowResponse response = GenerateSowResponse.builder()
                .budgetAssessment(BudgetAssessmentDto.builder()
                        .estimatedMin(BigDecimal.valueOf(120))
                        .recommendedBudget(BigDecimal.valueOf(100))
                        .estimatedMax(BigDecimal.valueOf(80))
                        .confidence("HIGH")
                        .factors(List.of(" API integration ", "api integration", "Production deployment"))
                        .build())
                .milestones(List.of(MilestoneDto.builder().budget(BigDecimal.valueOf(100)).build()))
                .build();

        service.normalizeBudgetAssessment(response, BigDecimal.valueOf(50));

        BudgetAssessmentDto assessment = response.getBudgetAssessment();
        assertEquals(BigDecimal.valueOf(80), assessment.getEstimatedMin());
        assertEquals(BigDecimal.valueOf(100), assessment.getRecommendedBudget());
        assertEquals(BigDecimal.valueOf(120), assessment.getEstimatedMax());
        assertEquals("TOO_LOW", assessment.getStatus());
        assertEquals(BigDecimal.valueOf(30), assessment.getGapToMinimum());
        assertEquals("LOW", assessment.getConfidence());
        assertEquals(List.of("API integration", "Production deployment"), assessment.getFactors());
    }

    @Test
    void normalizeBudgetAssessment_shouldClassifyAllBusinessBudgetBands() {
        List<BigDecimal> businessBudgets = List.of(
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(130)
        );
        List<String> expectedStatuses = List.of("TOO_LOW", "LOW", "SUITABLE", "HIGH");

        for (int i = 0; i < businessBudgets.size(); i++) {
            GenerateSowResponse response = GenerateSowResponse.builder()
                    .budgetAssessment(BudgetAssessmentDto.builder()
                            .estimatedMin(BigDecimal.valueOf(80))
                            .recommendedBudget(BigDecimal.valueOf(100))
                            .estimatedMax(BigDecimal.valueOf(120))
                            .confidence("MEDIUM")
                            .build())
                    .milestones(List.of(MilestoneDto.builder().budget(BigDecimal.valueOf(100)).build()))
                    .build();

            service.normalizeBudgetAssessment(response, businessBudgets.get(i));

            assertEquals(expectedStatuses.get(i), response.getBudgetAssessment().getStatus());
        }
    }

    @Test
    void generateSow_whenBusinessBudgetIsTooLow_shouldReturnBusinessAndRecommendedMilestoneAllocations() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);
        GenerateSowRequest request = buildRequest();
        request.setBudget(BigDecimal.valueOf(50));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": false,
                          "questions": [],
                          "budgetAssessment": {
                            "currency": "VND",
                            "estimatedMin": 120,
                            "recommendedBudget": 140,
                            "estimatedMax": 160,
                            "confidence": "MEDIUM",
                            "factors": ["Hai he thong tich hop", "Trien khai production"]
                          },
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Tu dong tra loi"],
                            "scopeOfWork": ["RAG", "Order API"],
                            "deliverables": ["Bot API"],
                            "assumptions": [],
                            "outOfScope": []
                          },
                          "milestones": [
                            {
                              "name": "Discovery",
                              "description": "Analyze",
                              "duration": 4,
                              "durationUnit": "tuan",
                              "budget": 40,
                              "acceptanceCriteria": ["Thiet ke duoc duyet"]
                            },
                            {
                              "name": "Build",
                              "description": "Implement",
                              "duration": 6,
                              "durationUnit": "tuan",
                              "budget": 100,
                              "acceptanceCriteria": ["Bot hoat dong"]
                            }
                          ]
                        }
                        """)));

        GenerateSowResponse response = localService.generateSow(request);

        BudgetAssessmentDto assessment = response.getBudgetAssessment();
        assertEquals(BigDecimal.valueOf(50), assessment.getBusinessBudget());
        assertEquals(BigDecimal.valueOf(140), assessment.getRecommendedBudget());
        assertEquals("TOO_LOW", assessment.getStatus());
        assertEquals(BigDecimal.valueOf(70), assessment.getGapToMinimum());
        assertEquals("AI_ADVISORY", assessment.getSource());
        assertEquals(BigDecimal.valueOf(50), response.getMilestones().stream()
                .map(MilestoneDto::getBudget).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(BigDecimal.valueOf(140), response.getMilestones().stream()
                .map(MilestoneDto::getRecommendedBudget).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(BigDecimal.valueOf(40), response.getMilestones().get(0).getRecommendedBudget());
        assertEquals(BigDecimal.valueOf(100), response.getMilestones().get(1).getRecommendedBudget());
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
    void generateSow_whenClarificationAlreadyAsked_shouldSuppressFurtherQuestions() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OpenAiProperties openAiProperties = new OpenAiProperties();
        openAiProperties.setApiKey("test-key");
        AiSowGenerationService localService = new AiSowGenerationService(restTemplate, openAiProperties, ragRetrievalService);
        GenerateSowRequest request = buildRequest();
        request.setClarificationAlreadyAsked(true);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(buildOpenAiResponse("""
                        {
                          "needMoreInfo": true,
                          "questions": ["Can bo sung API don hang?", "Can bo sung ngon ngu ho tro?"],
                          "sow": {
                            "title": "AI support bot",
                            "overview": "Build bot",
                            "objectives": ["Answer"],
                            "scopeOfWork": ["Design"],
                            "deliverables": ["Bot API"],
                            "assumptions": ["API don hang duoc suy luan theo REST"],
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

        GenerateSowResponse response = localService.generateSow(request);

        assertFalse(response.getNeedMoreInfo());
        assertTrue(response.getQuestions().isEmpty());
        assertEquals(List.of("API don hang duoc suy luan theo REST"), response.getSow().getAssumptions());
        assertEquals(1, response.getMilestones().size());
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

    private MilestoneBudgetReferenceDto reference(int milestoneIndex, long referenceBudget) {
        return MilestoneBudgetReferenceDto.builder()
                .milestoneIndex(milestoneIndex)
                .referenceBudget(BigDecimal.valueOf(referenceBudget))
                .build();
    }
}
