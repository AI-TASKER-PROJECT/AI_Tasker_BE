package com.aitasker.be.service.core;

import com.aitasker.be.config.OpenAiProperties;
import com.aitasker.be.dto.candidate.ExpertCandidateResponse;
import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.dto.candidate.ExpertRecommendationListResponse;
import com.aitasker.be.dto.candidate.SowKeywordExtractionResult;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.ExpertRecommendationEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.SowEntity;
import com.aitasker.be.repository.ExpertRecommendationRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.SowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertRecommendationServiceTest {
    @Mock private ExpertCandidateRankingService expertCandidateRankingService;
    @Mock private ExpertRecommendationRepository expertRecommendationRepository;
    @Mock private JobRepository jobRepository;
    @Mock private SowRepository sowRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private AccessService accessService;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private NotificationService notificationService;

    private OpenAiProperties openAiProperties;
    private ExpertRecommendationService service;

    @BeforeEach
    void setUp() {
        openAiProperties = new OpenAiProperties();
        service = new ExpertRecommendationService(
                expertCandidateRankingService,
                expertRecommendationRepository,
                jobRepository,
                sowRepository,
                milestoneRepository,
                restTemplate,
                openAiProperties,
                paymentWalletService,
                accessService,
                expertProfileRepository,
                notificationService
        );
    }

    @Test
    void generateRecommendations_whenOpenAiKeyMissing_shouldFallbackAndPersistTopFive() {
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(expertCandidateRankingService.findTopCandidatesByJobPostingId(1))
                .thenReturn(candidateSearch(List.of(
                        candidate(1, 10, 95.0),
                        candidate(2, 20, 90.0),
                        candidate(3, 30, 85.0),
                        candidate(4, 40, 80.0),
                        candidate(5, 50, 75.0),
                        candidate(6, 60, 70.0)
                )));

        ExpertRecommendationListResponse response = service.generateRecommendations(1L);

        assertFalse(response.getGeneratedByAi());
        assertEquals("AI recommendation failed, fallback to rule-based ranking.", response.getMessage());
        assertEquals(5, response.getRecommendations().size());
        assertEquals(1L, response.getRecommendations().get(0).getExpertId());
        assertEquals(5, response.getRecommendations().get(4).getRankPosition());

        ArgumentCaptor<Iterable<ExpertRecommendationEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(expertRecommendationRepository).deleteByJobPostingId(1L);
        verify(expertRecommendationRepository).saveAll(captor.capture());
        verify(paymentWalletService).requirePremiumRecommendationAccess(1L);
        assertEquals(5, toList(captor.getValue()).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateRecommendations_whenAiReturnsJson_shouldKeepBackendIdentityScoreRankAndEvidence() {
        openAiProperties.setApiKey("test-key");
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(sowRepository.findByJobId(1)).thenReturn(Optional.of(SowEntity.builder()
                .title("AI chatbot support")
                .overview("Build customer support chatbot")
                .build()));
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(expertCandidateRankingService.findTopCandidatesByJobPostingId(1))
                .thenReturn(candidateSearch(List.of(
                        candidate(1, 10, 95.0),
                        candidate(2, 20, 90.0)
                )));
        when(restTemplate.exchange(
                eq(openAiProperties.getChatCompletionsUrl()),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(openAiResponse("""
                {
                  "recommendations": [
                    {
                      "expertId": 2,
                      "reason": "Phu hop voi chatbot AI"
                    }
                  ]
                }
                """)));

        ExpertRecommendationListResponse response = service.generateRecommendations(1L);

        assertTrue(response.getGeneratedByAi());
        assertEquals(2, response.getRecommendations().size());
        assertEquals(1L, response.getRecommendations().get(0).getExpertId());
        assertEquals(1, response.getRecommendations().get(0).getRankPosition());
        assertEquals(95.0, response.getRecommendations().get(0).getMatchScore());
        assertEquals(List.of("AI", "Chatbot"), response.getRecommendations().get(0).getMatchedSkills());
        assertEquals(2L, response.getRecommendations().get(1).getExpertId());
        assertEquals(90.0, response.getRecommendations().get(1).getMatchScore());
        assertEquals("Phu hop voi chatbot AI", response.getRecommendations().get(1).getReason());

        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(openAiProperties.getChatCompletionsUrl()),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(Map.class)
        );
        Map<String, Object> requestBody = requestCaptor.getValue().getBody();
        Map<String, Object> responseFormat = (Map<String, Object>) requestBody.get("response_format");
        assertEquals("json_schema", responseFormat.get("type"));
        Map<String, Object> jsonSchema = (Map<String, Object>) responseFormat.get("json_schema");
        assertEquals("expert_recommendations", jsonSchema.get("name"));
        assertEquals(Boolean.TRUE, jsonSchema.get("strict"));
        Map<String, Object> rootSchema = (Map<String, Object>) jsonSchema.get("schema");
        assertEquals(Boolean.FALSE, rootSchema.get("additionalProperties"));
        Map<String, Object> rootProperties = (Map<String, Object>) rootSchema.get("properties");
        Map<String, Object> recommendationsSchema = (Map<String, Object>) rootProperties.get("recommendations");
        assertEquals(1, recommendationsSchema.get("minItems"));
        assertEquals(5, recommendationsSchema.get("maxItems"));
        Map<String, Object> itemSchema = (Map<String, Object>) recommendationsSchema.get("items");
        assertEquals(Boolean.FALSE, itemSchema.get("additionalProperties"));
        Map<String, Object> itemProperties = (Map<String, Object>) itemSchema.get("properties");
        Map<String, Object> expertIdSchema = (Map<String, Object>) itemProperties.get("expertId");
        assertEquals("integer", expertIdSchema.get("type"));
        assertEquals(List.of(1L, 2L), expertIdSchema.get("enum"));
        Map<String, Object> reasonSchema = (Map<String, Object>) itemProperties.get("reason");
        assertEquals("string", reasonSchema.get("type"));
        assertEquals(1, reasonSchema.get("minLength"));
        assertEquals(800, reasonSchema.get("maxLength"));

        ArgumentCaptor<Iterable<ExpertRecommendationEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(expertRecommendationRepository).saveAll(captor.capture());
        verify(paymentWalletService).requirePremiumRecommendationAccess(1L);
        List<ExpertRecommendationEntity> saved = toList(captor.getValue());
        assertEquals(2, saved.size());
        assertEquals(1L, saved.get(0).getExpertId());
        assertEquals(95.0, saved.get(0).getMatchScore().doubleValue());
    }

    @Test
    void generateRecommendations_whenAiReturnsExpertIdAsString_shouldUseBackendFallback() {
        openAiProperties.setApiKey("test-key");
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(sowRepository.findByJobId(1)).thenReturn(Optional.empty());
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(expertCandidateRankingService.findTopCandidatesByJobPostingId(1))
                .thenReturn(candidateSearch(List.of(candidate(1, 10, 95.0))));
        when(restTemplate.exchange(
                eq(openAiProperties.getChatCompletionsUrl()),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(openAiResponse("""
                {"recommendations":[{"expertId":"1","reason":"Phu hop voi chatbot AI"}]}
                """)));

        ExpertRecommendationListResponse response = service.generateRecommendations(1L);

        assertFalse(response.getGeneratedByAi());
        assertEquals("AI recommendation failed, fallback to rule-based ranking.", response.getMessage());
        assertEquals(1L, response.getRecommendations().get(0).getExpertId());
        assertEquals(95.0, response.getRecommendations().get(0).getMatchScore());
    }

    @Test
    void generateRecommendations_whenAiReturnsBlankReason_shouldUseBackendFallback() {
        openAiProperties.setApiKey("test-key");
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(sowRepository.findByJobId(1)).thenReturn(Optional.empty());
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(expertCandidateRankingService.findTopCandidatesByJobPostingId(1))
                .thenReturn(candidateSearch(List.of(candidate(1, 10, 95.0))));
        when(restTemplate.exchange(
                eq(openAiProperties.getChatCompletionsUrl()),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(openAiResponse("""
                {"recommendations":[{"expertId":1,"reason":"   "}]}
                """)));

        ExpertRecommendationListResponse response = service.generateRecommendations(1L);

        assertFalse(response.getGeneratedByAi());
        assertEquals("AI recommendation failed, fallback to rule-based ranking.", response.getMessage());
        assertEquals(1L, response.getRecommendations().get(0).getExpertId());
        assertEquals(95.0, response.getRecommendations().get(0).getMatchScore());
    }

    @Test
    void generateRecommendations_shouldPinAndPreserveExistingBusinessSelection() {
        openAiProperties.setApiKey("");
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(expertRecommendationRepository.findByJobPostingIdOrderByRankPositionAsc(1L)).thenReturn(List.of(
                ExpertRecommendationEntity.builder()
                        .jobPostingId(1L)
                        .expertId(2L)
                        .portfolioId(20L)
                        .rankPosition(2)
                        .businessSelected(true)
                        .build()
        ));
        when(expertCandidateRankingService.findTopCandidatesByJobPostingId(1)).thenReturn(candidateSearch(List.of(
                candidate(1, 10, 95.0),
                candidate(2, 20, 90.0),
                candidate(3, 30, 85.0)
        )));

        ExpertRecommendationListResponse response = service.generateRecommendations(1L);

        assertEquals(2L, response.getRecommendations().get(0).getExpertId());
        assertTrue(response.getRecommendations().get(0).getBusinessSelected());
        ArgumentCaptor<Iterable<ExpertRecommendationEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(expertRecommendationRepository).saveAll(captor.capture());
        assertTrue(toList(captor.getValue()).get(0).getBusinessSelected());
    }

    @Test
    void generateRecommendations_whenAiOnlyInventsExpert_shouldUseBackendFallback() {
        openAiProperties.setApiKey("test-key");
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(sowRepository.findByJobId(1)).thenReturn(Optional.empty());
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(expertCandidateRankingService.findTopCandidatesByJobPostingId(1))
                .thenReturn(candidateSearch(List.of(candidate(1, 10, 95.0))));
        when(restTemplate.exchange(
                eq(openAiProperties.getChatCompletionsUrl()),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(openAiResponse("""
                {"recommendations":[{"expertId":999,"reason":"Invented"}]}
                """)));

        ExpertRecommendationListResponse response = service.generateRecommendations(1L);

        assertFalse(response.getGeneratedByAi());
        assertEquals(1L, response.getRecommendations().get(0).getExpertId());
        assertEquals(95.0, response.getRecommendations().get(0).getMatchScore());
    }

    @Test
    void selectRecommendedExpert_shouldMarkSelectedAndNotifyExpertOnce() {
        ExpertRecommendationEntity recommendation = ExpertRecommendationEntity.builder()
                .id(100L)
                .jobPostingId(1L)
                .expertId(2L)
                .portfolioId(20L)
                .rankPosition(1)
                .businessSelected(Boolean.FALSE)
                .build();
        when(expertRecommendationRepository.findByJobPostingIdAndExpertId(1L, 2L))
                .thenReturn(Optional.of(recommendation));
        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).title("AI chatbot").build()));
        when(expertRecommendationRepository.save(any(ExpertRecommendationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expertProfileRepository.findById(2)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(2).accountId(22).build()));
        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(11).build());

        var response = service.selectRecommendedExpert(1L, 2L);

        assertTrue(response.getBusinessSelected());
        verify(paymentWalletService).requirePremiumRecommendationAccess(1L);
        verify(notificationService).notifyExpertSelectedForJob(22, 11, 1, "AI chatbot");
    }

    private ExpertCandidateSearchResponse candidateSearch(List<ExpertCandidateResponse> candidates) {
        return ExpertCandidateSearchResponse.builder()
                .jobPostingId(1)
                .keywords(SowKeywordExtractionResult.builder()
                        .skills(List.of("AI", "Chatbot"))
                        .domains(List.of("Customer Support"))
                        .keywords(List.of("AI", "Chatbot", "Customer Support"))
                        .build())
                .candidates(candidates)
                .build();
    }

    private ExpertCandidateResponse candidate(Integer expertId, Integer portfolioId, Double score) {
        return ExpertCandidateResponse.builder()
                .expertId(expertId)
                .portfolioId(portfolioId)
                .matchScore(score)
                .matchedSkills(List.of("AI", "Chatbot"))
                .matchedDomains(List.of("Customer Support"))
                .yearsExperience(5)
                .certificates("AI certificate")
                .selfDescription("AI chatbot customer support")
                .build();
    }

    private Map<String, Object> openAiResponse(String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("content", content);

        Map<String, Object> choice = new LinkedHashMap<>();
        choice.put("message", message);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("choices", List.of(choice));
        return response;
    }

    private List<ExpertRecommendationEntity> toList(Iterable<ExpertRecommendationEntity> entities) {
        List<ExpertRecommendationEntity> result = new java.util.ArrayList<>();
        entities.forEach(result::add);
        return result;
    }
}
