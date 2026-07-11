package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.admin.StaffRequest;
import com.aitasker.be.dto.admin.StaffResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStaffSpecializationTest {

    @Mock private AccessService accessService;
    @Mock private ContractRepository contractRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private SystemSettingRepository systemSettingRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private NotificationService notificationService;
    @Mock private StaffDomainRepository staffDomainRepository;
    @Mock private StaffSkillRepository staffSkillRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private SkillRepository skillRepository;

    @InjectMocks private AdminService adminService;

    private AccountEntity adminAccount;
    private AccountEntity staffAccount;
    private StaffEntity staff;
    private DomainEntity domain1;
    private DomainEntity domain2;
    private SkillEntity skill1;

    @BeforeEach
    void setUp() {
        adminAccount = AccountEntity.builder()
                .accountId(1).fullName("Admin")
                .role(RoleEntity.builder().roleId(3).roleName("ADMIN").build())
                .status("Approved").build();

        staffAccount = AccountEntity.builder()
                .accountId(5).fullName("Staff Test").email("staff@test.com")
                .role(RoleEntity.builder().roleId(4).roleName("STAFF").build())
                .status("Approved").build();

        staff = StaffEntity.builder()
                .staffId(1).accountId(5)
                .specialization("AI & ML Specialist").build();

        domain1 = DomainEntity.builder().domainId(2).domainCode("GENERATIVE_AI")
                .domainName("Generative AI Applications").isActive(true).sortOrder(1).build();
        domain2 = DomainEntity.builder().domainId(3).domainCode("NLP")
                .domainName("Natural Language Processing").isActive(true).sortOrder(2).build();
        skill1 = SkillEntity.builder().skillId(1).skillCode("PROMPT_ENGINEERING")
                .skillName("Prompt Engineering").isActive(true).build();

        lenient().when(accessService.currentAccount()).thenReturn(adminAccount);
    }

    @Test
    void createStaff_withValidDomainsAndSkills_succeeds() {
        StaffRequest request = new StaffRequest();
        request.setAccountId(5);
        request.setSpecialization("AI Specialist");
        request.setDomainIds(List.of(2, 3));
        request.setSkillIds(List.of(1));

        when(accountRepository.findById(5)).thenReturn(Optional.of(staffAccount));
        when(staffRepository.findByAccountId(5)).thenReturn(Optional.empty());
        when(staffRepository.save(any(StaffEntity.class))).thenReturn(staff);
        when(domainRepository.findAllById(List.of(2, 3))).thenReturn(List.of(domain1, domain2));
        when(skillRepository.findAllById(List.of(1))).thenReturn(List.of(skill1));
        when(staffDomainRepository.findByIdStaffId(1)).thenReturn(List.of());
        when(staffSkillRepository.findByIdStaffId(1)).thenReturn(List.of());

        StaffResponse result = adminService.createStaff(request);

        assertNotNull(result);
        assertEquals(1, result.getStaffId());
        verify(staffDomainRepository, atLeastOnce()).save(any(StaffDomainEntity.class));
    }

    @Test
    void createStaff_emptyDomains_throwsException() {
        StaffRequest request = new StaffRequest();
        request.setAccountId(5);
        request.setDomainIds(List.of());

        when(accountRepository.findById(5)).thenReturn(Optional.of(staffAccount));

        assertThrows(AppException.class, () -> adminService.createStaff(request));
    }

    @Test
    void createStaff_unknownDomain_throwsException() {
        StaffRequest request = new StaffRequest();
        request.setAccountId(5);
        request.setDomainIds(List.of(999));

        when(accountRepository.findById(5)).thenReturn(Optional.of(staffAccount));
        when(domainRepository.findAllById(List.of(999))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> adminService.createStaff(request));
    }

    @Test
    void updateStaff_withNewDomains_succeeds() {
        StaffRequest request = new StaffRequest();
        request.setDomainIds(List.of(2));
        request.setSpecialization("Updated specialist");

        when(staffRepository.findById(1)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(StaffEntity.class))).thenReturn(staff);
        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domain1));
        when(staffDomainRepository.findByIdStaffId(1)).thenReturn(List.of());
        when(staffSkillRepository.findByIdStaffId(1)).thenReturn(List.of());

        StaffResponse result = adminService.updateStaff(1, request);

        assertNotNull(result);
        verify(staffDomainRepository).deleteByIdStaffId(1);
    }

    @Test
    void updateStaff_emptyDomains_throwsException() {
        StaffRequest request = new StaffRequest();
        request.setDomainIds(List.of());

        when(staffRepository.findById(1)).thenReturn(Optional.of(staff));

        assertThrows(AppException.class, () -> adminService.updateStaff(1, request));
    }
}
