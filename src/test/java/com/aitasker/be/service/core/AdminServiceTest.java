package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.ReviewEntity;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

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

    @InjectMocks private AdminService adminService;

    @Test
    void createReview_shouldThrowWhenRatingOutOfRange() {
        ReviewEntity input = ReviewEntity.builder()
                .contractId(1)
                .rating(new BigDecimal("5.5"))
                .build();
        AppException ex = assertThrows(AppException.class, () -> adminService.createReview(input));
        assertEquals("RATING PHAI NAM TRONG KHOANG 1 DEN 5", ex.getMessage());
    }
}
