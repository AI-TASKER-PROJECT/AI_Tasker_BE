package com.aitasker.be.service.core;

import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.JobSkillEntity;
import com.aitasker.be.entity.JobSkillId;
import com.aitasker.be.entity.SkillEntity;
import com.aitasker.be.entity.TechnologyEntity;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.JobDomainRepository;
import com.aitasker.be.repository.JobSkillRepository;
import com.aitasker.be.repository.JobTechnologyRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.SkillRepository;
import com.aitasker.be.repository.SowRepository;
import com.aitasker.be.repository.TechnologyRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRequirementResolverTest {
    @Mock private SowRepository sowRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobTechnologyRepository jobTechnologyRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private TechnologyRepository technologyRepository;

    private JobRequirementResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new JobRequirementResolver(
                sowRepository,
                milestoneRepository,
                jobSkillRepository,
                jobDomainRepository,
                jobTechnologyRepository,
                skillRepository,
                domainRepository,
                technologyRepository,
                new SowKeywordExtractionService(new ObjectMapper())
        );
    }

    @Test
    void resolve_shouldUseStructuredSkillMappingsBeforeTextFallback() {
        JobEntity job = JobEntity.builder().jobId(1).title("Computer Vision platform").build();
        stubCommon(job);
        when(skillRepository.findByIsActiveTrueOrderBySkillNameAsc()).thenReturn(List.of(
                skill(12, "DATA_PIPELINE", "Data Pipeline Design"),
                skill(15, "COMPUTER_VISION", "Computer Vision Modeling")
        ));
        when(jobSkillRepository.findByIdJobId(1)).thenReturn(List.of(JobSkillEntity.builder()
                .id(new JobSkillId(1, 12))
                .isMandatory(true)
                .build()));

        ResolvedJobRequirements result = resolver.resolve(job);

        assertEquals(List.of(12), result.skillIds());
        assertEquals(java.util.Set.of(12), result.mandatorySkillIds());
    }

    @Test
    void resolve_shouldFillMissingGroupsFromSowAndActiveCatalog() {
        JobEntity job = JobEntity.builder()
                .jobId(2)
                .title("BI Dashboard")
                .rawRequirements("Build KPI reporting with PostgreSQL data visualization")
                .build();
        stubCommon(job);
        when(skillRepository.findByIsActiveTrueOrderBySkillNameAsc()).thenReturn(List.of(
                skill(13, "BI_DASHBOARDING", "BI Dashboarding")
        ));
        when(domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc()).thenReturn(List.of(
                DomainEntity.builder()
                        .domainId(6)
                        .domainCode("DATA_ANALYTICS_BI")
                        .domainName("Data Analytics & BI")
                        .description("Business intelligence dashboard and KPI reporting")
                        .isActive(true)
                        .sortOrder(1)
                        .build()
        ));
        when(technologyRepository.findByIsActiveTrueOrderBySortOrderAscTechnologyNameAsc()).thenReturn(List.of(
                TechnologyEntity.builder()
                        .technologyId(7)
                        .technologyCode("POSTGRESQL")
                        .technologyName("PostgreSQL")
                        .isActive(true)
                        .sortOrder(1)
                        .build()
        ));

        ResolvedJobRequirements result = resolver.resolve(job);

        assertEquals(List.of(13), result.skillIds());
        assertEquals(List.of(6), result.domainIds());
        assertEquals(List.of(7), result.technologyIds());
        assertTrue(result.mandatorySkillIds().isEmpty());
    }

    @Test
    void resolve_shouldMapGenericAiKeywordToCatalogBackedDomain() {
        JobEntity job = JobEntity.builder()
                .jobId(3)
                .title("Artificial intelligence assistant")
                .build();
        stubCommon(job);
        when(skillRepository.findByIsActiveTrueOrderBySkillNameAsc()).thenReturn(List.of());
        when(domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc()).thenReturn(List.of(
                DomainEntity.builder()
                        .domainId(2)
                        .domainCode("GENERATIVE_AI")
                        .domainName("Generative AI Applications")
                        .description("AI solution delivery")
                        .isActive(true)
                        .sortOrder(1)
                        .build()
        ));

        ResolvedJobRequirements result = resolver.resolve(job);

        assertEquals(List.of(2), result.domainIds());
    }

    private void stubCommon(JobEntity job) {
        when(sowRepository.findByJobId(job.getJobId())).thenReturn(Optional.empty());
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(job.getJobId())).thenReturn(List.of());
    }

    private SkillEntity skill(int id, String code, String name) {
        return SkillEntity.builder()
                .skillId(id)
                .skillCode(code)
                .skillName(name)
                .description(name)
                .isActive(true)
                .build();
    }
}
