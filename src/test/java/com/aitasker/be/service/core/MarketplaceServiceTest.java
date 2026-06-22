/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/MarketplaceServiceTest.java
 * ÄÃ¢y lÃ  file gÃ¬: File service chá»©a nghiá»‡p vá»¥ chÃ­nh, Ä‘iá»u phá»‘i repository vÃ  kiá»ƒm tra luáº­t xá»­ lÃ½ cá»§a há»‡ thá»‘ng.
 * Má»¥c Ä‘Ã­ch note: giáº£i thÃ­ch cÃ¡c annotation vÃ  hÃ m chÃ­nh Ä‘á»ƒ Ä‘á»c hiá»ƒu chá»©c nÄƒng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.SowEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Note: Annotation nÃ y cung cáº¥p metadata Ä‘á»ƒ Spring, JPA, Lombok, validation hoáº·c test xá»­ lÃ½ tá»± Ä‘á»™ng.
@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    // Note: Annotation nÃ y cung cáº¥p metadata Ä‘á»ƒ Spring, JPA, Lombok, validation hoáº·c test xá»­ lÃ½ tá»± Ä‘á»™ng.
    @Mock private AccessService accessService;
    // Note: Annotation nÃ y cung cáº¥p metadata Ä‘á»ƒ Spring, JPA, Lombok, validation hoáº·c test xá»­ lÃ½ tá»± Ä‘á»™ng.
    @Mock private BusinessProfileRepository businessProfileRepository;
    // Note: Annotation nÃ y cung cáº¥p metadata Ä‘á»ƒ Spring, JPA, Lombok, validation hoáº·c test xá»­ lÃ½ tá»± Ä‘á»™ng.
    @Mock private ExpertProfileRepository expertProfileRepository;
    // Note: Annotation nÃ y cung cáº¥p metadata Ä‘á»ƒ Spring, JPA, Lombok, validation hoáº·c test xá»­ lÃ½ tá»± Ä‘á»™ng.
    @Mock private JobRepository jobRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private ProposalRepository proposalRepository;
    @Mock private SowRepository sowRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private TechnologyRepository technologyRepository;
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private JobTechnologyRepository jobTechnologyRepository;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private NotificationService notificationService;
    @Mock private FirebaseStorageService firebaseStorageService;
    @Mock private AcceptanceCriteriaRepository criteriaRepository;
    @Mock private MilestoneAcceptanceCriteriaRepository milestoneCriteriaRepository;
    @Mock private AuditLogService auditLogService;

    // Note: Annotation nÃ y cung cáº¥p metadata Ä‘á»ƒ Spring, JPA, Lombok, validation hoáº·c test xá»­ lÃ½ tá»± Ä‘á»™ng.
    @InjectMocks private MarketplaceService marketplaceService;

    // Note: Annotation nÃ y Ä‘Ã¡nh dáº¥u hÃ m test Ä‘á»ƒ JUnit thá»±c thi.
    @Test
    // Note: HÃ m `createJob_shouldThrowWhenBudgetInvalid` dÃ¹ng Ä‘á»ƒ kiá»ƒm thá»­ hÃ nh vi mong Ä‘á»£i, giÃºp phÃ¡t hiá»‡n lá»—i khi code thay Ä‘á»•i.
    void createJob_shouldThrowWhenBudgetInvalid() {
        JobEntity input = JobEntity.builder()
                .title("AI JOB")
                .rawRequirements("REQ")
                .budget(BigDecimal.ZERO)
                .build();

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.createJob(input));
        assertEquals("BUDGET PHAI LON HON 0", ex.getMessage());
    }

    // Note: Annotation nÃ y Ä‘Ã¡nh dáº¥u hÃ m test Ä‘á»ƒ JUnit thá»±c thi.
    @Test
    // Note: HÃ m `createJob_shouldForceDraftEvenWhenClientRequestsOpen` dÃ¹ng Ä‘á»ƒ kiá»ƒm thá»­ hÃ nh vi mong Ä‘á»£i, giÃºp phÃ¡t hiá»‡n lá»—i khi code thay Ä‘á»•i.
    void createJob_shouldForceDraftEvenWhenClientRequestsOpen() {
        JobEntity input = JobEntity.builder()
                .title("AI JOB")
                .rawRequirements("REQ")
                .budget(BigDecimal.TEN)
                .status("OPEN")
                .build();

        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder()
                .businessId(20)
                .accountId(10)
                .kybStatus("Approved")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobEntity saved = marketplaceService.createJob(input);

        assertEquals("DRAFT", saved.getStatus());
        assertNull(saved.getPublishedAt());
        assertEquals(20, saved.getBusinessId());
    }

    @Test
    void updateDraftJob_shouldUpsertSowAndReplaceMilestonesForDraftJob() {
        Integer jobId = 1;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(20).status("DRAFT")
                .title("Old").rawRequirements("Old req").budget(BigDecimal.TEN).build();
        JobEntity input = JobEntity.builder()
                .title("New title").rawRequirements("New req").budget(BigDecimal.valueOf(20))
                .sow(SowEntity.builder().title("New Sow").overview("ov").build())
                .milestones(List.of(MilestoneEntity.builder().milestoneName("M1").fundsAllocated(BigDecimal.valueOf(5)).orderIndex(1).build()))
                .build();
        SowEntity existingSow = SowEntity.builder().sowId(7).jobId(jobId).title("Old Sow").build();
        MilestoneEntity oldMilestone = MilestoneEntity.builder().milestoneId(100).jobId(jobId).milestoneName("old").build();

        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).kybStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(contractRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        when(sowRepository.findByJobId(jobId)).thenReturn(Optional.of(existingSow));
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(jobId)).thenReturn(List.of(oldMilestone));
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(inv -> {
            MilestoneEntity m = inv.getArgument(0);
            m.setMilestoneId(200);
            return m;
        });

        JobEntity result = marketplaceService.updateDraftJob(jobId, input);

        assertEquals("New title", result.getTitle());
        assertEquals("New Sow", existingSow.getTitle());
        verify(sowRepository).save(existingSow);
        verify(milestoneRepository).delete(oldMilestone);
        verify(milestoneRepository).save(any(MilestoneEntity.class));
        verify(auditLogService).record(AuditLogService.ACTION_UPDATE_JOB_DRAFT, "jobs", String.valueOf(jobId), 10);
    }

    @Test
    void updateDraftJob_shouldInsertSowWhenNoneExists() {
        Integer jobId = 2;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(20).status("DRAFT")
                .title("T").rawRequirements("R").budget(BigDecimal.TEN).build();
        JobEntity input = JobEntity.builder()
                .sow(SowEntity.builder().title("Inserted Sow").build())
                .build();
        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).kybStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(contractRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        when(sowRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(jobId)).thenReturn(List.of());
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        marketplaceService.updateDraftJob(jobId, input);

        verify(sowRepository).save(any(SowEntity.class));
    }

    @Test
    void updateDraftJob_shouldRejectNonOwner() {
        Integer jobId = 3;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(99).status("DRAFT").build();
        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).kybStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.updateDraftJob(jobId, JobEntity.builder().build()));
        assertEquals("BAN KHONG CO QUYEN THAO TAC JOB NAY", ex.getMessage());
        verify(sowRepository, never()).save(any(SowEntity.class));
    }

    @Test
    void updateDraftJob_shouldRejectNonDraftJob() {
        Integer jobId = 4;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(20).status("OPEN").build();
        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).kybStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.updateDraftJob(jobId, JobEntity.builder().build()));
        assertEquals("JOB KHONG O TRANG THAI DRAFT", ex.getMessage());
    }

    @Test
    void updateDraftJob_shouldRejectWhenContractExists() {
        Integer jobId = 5;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(20).status("DRAFT").build();
        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).kybStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(contractRepository.findByJobId(jobId)).thenReturn(Optional.of(ContractEntity.builder().build()));

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.updateDraftJob(jobId, JobEntity.builder().build()));
        assertEquals("JOB DA CO CONTRACT, KHONG DUOC CHINH MILESTONE", ex.getMessage());
    }

    @Test
    void updateDraftJob_shouldRejectBlankSowTitle() {
        Integer jobId = 6;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(20).status("DRAFT")
                .title("T").rawRequirements("R").budget(BigDecimal.TEN).build();
        JobEntity input = JobEntity.builder().sow(SowEntity.builder().title(" ").build()).build();
        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).kybStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(contractRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.updateDraftJob(jobId, input));
        assertEquals("SOW TITLE KHONG DUOC DE TRONG", ex.getMessage());
    }

    // Note: Annotation nÃ y Ä‘Ã¡nh dáº¥u hÃ m test Ä‘á»ƒ JUnit thá»±c thi.
    @Test
    // Note: HÃ m `updateJobStatus_shouldThrowWhenStatusInvalid` dÃ¹ng Ä‘á»ƒ kiá»ƒm thá»­ hÃ nh vi mong Ä‘á»£i, giÃºp phÃ¡t hiá»‡n lá»—i khi code thay Ä‘á»•i.
    void createJob_shouldThrowWhenJobDurationMissingUnit() {
        JobEntity input = JobEntity.builder()
                .title("AI JOB")
                .rawRequirements("REQ")
                .budget(BigDecimal.TEN)
                .plannedDurationValue(2)
                .build();

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.createJob(input));
        assertEquals("JOB DURATION VA DURATION UNIT PHAI CUNG CO HOAC CUNG KHONG CO", ex.getMessage());
    }

    @Test
    void createJob_shouldThrowWhenMilestoneDurationTotalExceedsJobDuration() {
        JobEntity input = JobEntity.builder()
                .title("AI JOB")
                .rawRequirements("REQ")
                .budget(BigDecimal.TEN)
                .plannedDurationValue(2)
                .plannedDurationUnit("week")
                .milestones(List.of(
                        MilestoneEntity.builder()
                                .milestoneName("M1")
                                .fundsAllocated(BigDecimal.ONE)
                                .orderIndex(1)
                                .duration(10)
                                .durationUnit("DAY")
                                .build(),
                        MilestoneEntity.builder()
                                .milestoneName("M2")
                                .fundsAllocated(BigDecimal.ONE)
                                .orderIndex(2)
                                .duration(1)
                                .durationUnit("WEEK")
                                .build()
                ))
                .build();

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.createJob(input));
        assertEquals("TONG DURATION CUA MILESTONE KHONG DUOC VUOT QUA DURATION CUA JOB", ex.getMessage());
    }

    @Test
    void createJob_shouldNormalizeAndSaveJobAndMilestoneDurations() {
        JobEntity input = JobEntity.builder()
                .title("AI JOB")
                .rawRequirements("REQ")
                .budget(BigDecimal.TEN)
                .plannedDurationValue(2)
                .plannedDurationUnit(" week ")
                .milestones(List.of(
                        MilestoneEntity.builder()
                                .milestoneName("M1")
                                .fundsAllocated(BigDecimal.ONE)
                                .orderIndex(1)
                                .duration(7)
                                .durationUnit(" day ")
                                .build(),
                        MilestoneEntity.builder()
                                .milestoneName("M2")
                                .fundsAllocated(BigDecimal.ONE)
                                .orderIndex(2)
                                .duration(1)
                                .durationUnit("week")
                                .build()
                ))
                .build();
        RoleEntity role = RoleEntity.builder().roleName("BUSINESS").build();
        AccountEntity account = AccountEntity.builder().accountId(10).status("Approved").role(role).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder()
                .businessId(20)
                .accountId(10)
                .kybStatus("Approved")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(invocation -> {
            JobEntity job = invocation.getArgument(0);
            job.setJobId(99);
            return job;
        });
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sowRepository.findByJobId(99)).thenReturn(Optional.empty());
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(99)).thenReturn(List.of());
        when(jobDomainRepository.findByIdJobId(99)).thenReturn(List.of());
        when(jobSkillRepository.findByIdJobId(99)).thenReturn(List.of());
        when(jobTechnologyRepository.findByIdJobId(99)).thenReturn(List.of());

        JobEntity saved = marketplaceService.createJob(input);

        assertEquals("WEEK", saved.getPlannedDurationUnit());
        ArgumentCaptor<MilestoneEntity> milestoneCaptor = ArgumentCaptor.forClass(MilestoneEntity.class);
        verify(milestoneRepository, times(2)).save(milestoneCaptor.capture());
        assertEquals("DAY", milestoneCaptor.getAllValues().get(0).getDurationUnit());
        assertEquals("WEEK", milestoneCaptor.getAllValues().get(1).getDurationUnit());
        assertEquals(99, milestoneCaptor.getAllValues().get(0).getJobId());
    }

    @Test
    void updateJobStatus_shouldThrowWhenStatusInvalid() {
        AppException ex = assertThrows(AppException.class, () -> marketplaceService.updateJobStatus(1, "INVALID"));
        assertEquals("STATUS JOB KHONG HOP LE", ex.getMessage());
    }

    // Note: Annotation nÃ y Ä‘Ã¡nh dáº¥u hÃ m test Ä‘á»ƒ JUnit thá»±c thi.
    @Test
    // Note: HÃ m `reviewProposal_shouldThrowWhenStatusInvalid` dÃ¹ng Ä‘á»ƒ kiá»ƒm thá»­ hÃ nh vi mong Ä‘á»£i, giÃºp phÃ¡t hiá»‡n lá»—i khi code thay Ä‘á»•i.
    void reviewProposal_shouldThrowWhenStatusInvalid() {
        AppException ex = assertThrows(AppException.class, () -> marketplaceService.reviewProposal(1, "INVALID"));
        assertEquals("STATUS PROPOSAL KHONG HOP LE", ex.getMessage());
    }
    @Test
    void createJobPayload_shouldAcceptGeneratedSowShapeWithSelectedCriteriaIds() throws Exception {
        String payload = """
                {
                  "title": "Xay dung tro ly AI cham soc khach hang da kenh",
                  "rawRequirements": "Can chatbot tra loi san pham",
                  "budget": 180000000,
                  "domainIds": [2, 3],
                  "skills": [
                    {
                      "skillId": 2,
                      "isMandatory": true
                    },
                    {
                      "skillId": 3,
                      "isMandatory": false
                    }
                  ],
                  "technologyIds": [1, 2, 3],
                  "sow": {
                    "title": "Xay dung tro ly AI cham soc khach hang da kenh",
                    "overview": "Tong quan du an",
                    "objectives": ["Phat trien chatbot"],
                    "scopeOfWork": ["Phan tich yeu cau"],
                    "deliverables": ["API chatbot"],
                    "assumptions": ["Co san du lieu san pham"],
                    "outOfScope": ["Khong bao gom mobile app"]
                  },
                  "milestones": [
                    {
                      "name": "Discovery & Solution Design",
                      "description": "Phan tich yeu cau va thiet ke giai phap",
                      "budget": 30000000,
                      "criteriaIds": [1, 3, 10]
                    }
                  ]
                }
                """;

        JobEntity request = new ObjectMapper().readValue(payload, JobEntity.class);
        MilestoneEntity milestone = request.getMilestones().get(0);

        assertEquals("[\"Phat trien chatbot\"]", request.getSow().getObjectives());
        assertEquals("[\"API chatbot\"]", request.getSow().getDeliverable());
        assertEquals(List.of(2, 3), request.getDomainIds());
        assertEquals(2, request.getSkills().size());
        assertEquals(2, request.getSkills().get(0).getSkillId());
        assertTrue(request.getSkills().get(0).getIsMandatory());
        assertEquals(3, request.getSkills().get(1).getSkillId());
        assertFalse(request.getSkills().get(1).getIsMandatory());
        assertEquals(List.of(1, 2, 3), request.getTechnologyIds());
        assertEquals("Discovery & Solution Design", milestone.getMilestoneName());
        assertEquals(new BigDecimal("30000000"), milestone.getFundsAllocated());
        assertEquals(List.of(1, 3, 10), milestone.getCriteriaIds());
    }
}
