/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/MarketplaceServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.RoleEntity;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccessService accessService;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private BusinessProfileRepository businessProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ExpertProfileRepository expertProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private JobRepository jobRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private TechnologyRepository technologyRepository;
    @Mock private JobTechnologyRepository jobTechnologyRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private ProposalRepository proposalRepository;
    @Mock private SowRepository sowRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private AcceptanceCriteriaRepository criteriaRepository;
    @Mock private MilestoneAcceptanceCriteriaRepository milestoneCriteriaRepository;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private FirebaseStorageService firebaseStorageService;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private MarketplaceService marketplaceService;

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `createJob_shouldThrowWhenBudgetInvalid` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void createJob_shouldThrowWhenBudgetInvalid() {
        JobEntity input = JobEntity.builder()
                .title("AI JOB")
                .rawRequirements("REQ")
                .budget(BigDecimal.ZERO)
                .build();

        AppException ex = assertThrows(AppException.class, () -> marketplaceService.createJob(input));
        assertEquals("BUDGET PHAI LON HON 0", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `createJob_shouldForceDraftEvenWhenClientRequestsOpen` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
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

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `updateJobStatus_shouldThrowWhenStatusInvalid` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
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

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `reviewProposal_shouldThrowWhenStatusInvalid` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
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
