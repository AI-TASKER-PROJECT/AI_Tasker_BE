package com.aitasker.be.service.core;

import com.aitasker.be.dto.candidate.ExpertCandidateResponse;
import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.entity.SkillEntity;
import com.aitasker.be.entity.SowEntity;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.SkillRepository;
import com.aitasker.be.repository.SowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertCandidateRankingServiceTest {
    @Mock private JobRepository jobRepository;
    @Mock private SowRepository sowRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private DomainRepository domainRepository;

    private ExpertCandidateRankingService service;

    @BeforeEach
    void setUp() {
        service = new ExpertCandidateRankingService(
                jobRepository,
                sowRepository,
                milestoneRepository,
                portfolioRepository,
                skillRepository,
                domainRepository,
                new SowKeywordExtractionService(new ObjectMapper())
        );
    }

    @Test
    void findTopCandidatesByJobPostingId_whenPortfolioStoresCatalogIds_shouldResolveAndRank() {
        PortfolioEntity strongPortfolio = PortfolioEntity.builder()
                .portfolioId(10)
                .expertId(100)
                .skillIds("2,3,9,19")
                .domainIds("17")
                .yearsExperience(5)
                .certificates("API Testing certificate")
                .selfDescription("RAG chatbot API don hang deployment")
                .build();
        PortfolioEntity unrelatedPortfolio = PortfolioEntity.builder()
                .portfolioId(11)
                .expertId(101)
                .skillIds("8")
                .domainIds("5")
                .yearsExperience(1)
                .certificates("")
                .selfDescription("BI dashboard and data reporting")
                .build();

        when(jobRepository.findById(1)).thenReturn(Optional.of(JobEntity.builder().jobId(1).build()));
        when(sowRepository.findByJobId(1)).thenReturn(Optional.of(SowEntity.builder()
                .jobId(1)
                .title("AI chatbot")
                .overview("Tro ly ao dung RAG")
                .scopeOfWork("[\"Tich hop API don hang\", \"Kiem thu va trien khai\"]")
                .deliverable("[\"Deployment guide\"]")
                .build()));
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(1)).thenReturn(List.of(
                MilestoneEntity.builder()
                        .milestoneName("Testing")
                        .description("Verify chatbot flow")
                        .build()
        ));
        when(skillRepository.findByIsActiveTrueOrderBySkillNameAsc()).thenReturn(List.of(
                skill(2, "RAG_ARCHITECTURE", "RAG Architecture", "Retrieval and knowledge-base design"),
                skill(3, "LLM_TOOL_CALLING", "LLM Tool Calling", "Agent workflows and tool routing"),
                skill(9, "DOCKER_DEVOPS", "Docker & DevOps", "CI/CD and deployment packaging"),
                skill(19, "API_TESTING_SWAGGER", "API Testing & Swagger", "Contract documentation and regression checks")
        ));
        when(domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc()).thenReturn(List.of(
                domain(17, "ECOMMERCE_RETAIL", "E-commerce & Retail Tech", "Catalog, CRM, and customer experience systems")
        ));
        when(portfolioRepository.findCandidatesBySkillOrDomainKeyword(anyString()))
                .thenReturn(List.of(strongPortfolio, unrelatedPortfolio));

        ExpertCandidateSearchResponse response = service.findTopCandidatesByJobPostingId(1);

        assertEquals(1, response.getCandidates().size());
        ExpertCandidateResponse candidate = response.getCandidates().get(0);
        assertEquals(100, candidate.getExpertId());
        assertTrue(candidate.getMatchedSkills().contains("RAG / Knowledge Base"));
        assertTrue(candidate.getMatchedSkills().contains("API Integration"));
        assertTrue(candidate.getMatchedSkills().contains("Deployment"));
        assertTrue(candidate.getMatchScore() > 60);
    }

    private SkillEntity skill(Integer id, String code, String name, String description) {
        return SkillEntity.builder()
                .skillId(id)
                .skillCode(code)
                .skillName(name)
                .description(description)
                .isActive(true)
                .build();
    }

    private DomainEntity domain(Integer id, String code, String name, String description) {
        return DomainEntity.builder()
                .domainId(id)
                .domainCode(code)
                .domainName(name)
                .description(description)
                .isActive(true)
                .sortOrder(id)
                .build();
    }
}
