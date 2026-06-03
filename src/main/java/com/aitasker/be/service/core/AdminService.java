package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AccessService accessService;
    private final ContractRepository contractRepository;
    private final DisputeRepository disputeRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final StaffRepository staffRepository;
    private final ReviewRepository reviewRepository;
    private final SystemSettingRepository systemSettingRepository;

    @Transactional
    public ReviewEntity createReview(ReviewEntity input) {
        // CHI CHO BUSINESS/EXPERT TAO DANH GIA SAU KHI HOP DONG DA KET THUC.
        accessService.requireRole("BUSINESS", "EXPERT");
        if (input.getRating() == null || input.getRating().doubleValue() < 1.0 || input.getRating().doubleValue() > 5.0) {
            throw new AppException("RATING PHAI NAM TRONG KHOANG 1 DEN 5");
        }
        ContractEntity contract = contractRepository.findById(input.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if (!List.of("Completed", "Terminated", "Cancelled").contains(contract.getStatus())) {
            throw new AppException("CHI DUOC DANH GIA KHI CONTRACT DA KET THUC");
        }
        AccountEntity actor = accessService.currentAccount();
        Integer actorBusinessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer actorExpertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isBusinessActor = actorBusinessId != null && actorBusinessId.equals(contract.getBusinessId());
        boolean isExpertActor = actorExpertId != null && actorExpertId.equals(contract.getExpertId());
        if (!isBusinessActor && !isExpertActor) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (reviewRepository.existsByContractIdAndReviewerId(input.getContractId(), actor.getAccountId())) {
            throw new AppException("BAN DA GUI DANH GIA CHO CONTRACT NAY");
        }
        input.setReviewId(null);
        input.setReviewerId(actor.getAccountId());
        if (isBusinessActor) {
            Integer targetAccountId = expertProfileRepository.findById(contract.getExpertId())
                    .map(ExpertProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN EXPERT"));
            input.setRevieweeId(targetAccountId);
        } else {
            Integer targetAccountId = businessProfileRepository.findById(contract.getBusinessId())
                    .map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN BUSINESS"));
            input.setRevieweeId(targetAccountId);
        }
        return reviewRepository.save(input);
    }

    public List<ReviewEntity> listReviewsByContract(Integer contractId) {
        accessService.requireRole("ADMIN", "STAFF", "BUSINESS", "EXPERT");
        return reviewRepository.findByContractId(contractId);
    }

    public List<SystemSettingEntity> listSettings() {
        accessService.requireRole("ADMIN", "STAFF");
        return systemSettingRepository.findAll();
    }

    @Transactional
    public SystemSettingEntity updateSetting(String key, String value, Boolean isActive) {
        accessService.requireRole("ADMIN");
        SystemSettingEntity setting = systemSettingRepository.findById(key).orElseThrow(() -> new NotFoundException("KHONG TIM THAY SYSTEM SETTING"));
        // CHI CHO PHEP CAP NHAT GIA TRI/CO HIEU LUC, KHONG CHO DOI VALUE_TYPE TRANH VO HOP DONG DU LIEU.
        if (value != null && !value.isBlank()) setting.setSettingValue(value);
        if (isActive != null) setting.setIsActive(isActive);
        setting.setUpdatedBy(accessService.currentAccount().getAccountId());
        return systemSettingRepository.save(setting);
    }

    public List<StaffEntity> listStaffs() {
        accessService.requireRole("ADMIN", "STAFF");
        return staffRepository.findAll();
    }

    @Transactional
    public StaffEntity createStaff(StaffEntity input) {
        accessService.requireRole("ADMIN");
        // KIEM TRA ACCOUNT TON TAI VA CHUA DUOC GAN HO SO STAFF.
        if (input.getAccountId() == null) throw new AppException("ACCOUNT ID KHONG DUOC DE TRONG");
        accountRepository.findById(input.getAccountId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT"));
        if (staffRepository.findByAccountId(input.getAccountId()).isPresent()) {
            throw new AppException("ACCOUNT NAY DA CO HO SO STAFF");
        }
        input.setStaffId(null);
        return staffRepository.save(input);
    }

    public Map<String, Object> analyticsOverview() {
        accessService.requireRole("ADMIN", "STAFF");
        // TONG HOP CHI SO CO BAN DE HO TRO DASHBOARD QUAN TRI MVP.
        long totalContracts = contractRepository.count();
        long completedContracts = contractRepository.findAll().stream().filter(c -> "Completed".equals(c.getStatus())).count();
        long terminatedContracts = contractRepository.findAll().stream().filter(c -> "Terminated".equals(c.getStatus()) || "Cancelled".equals(c.getStatus())).count();
        long totalDisputes = disputeRepository.count();
        long openDisputes = disputeRepository.findAll().stream().filter(d -> "Open".equals(d.getStatus()) || "UnderReview".equals(d.getStatus())).count();
        long totalTransactions = transactionRepository.count();
        BigDecimal totalVolume = transactionRepository.findAll().stream()
                .map(TransactionEntity::getAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal successRate = BigDecimal.ZERO;
        if (totalContracts > 0) {
            successRate = BigDecimal.valueOf(completedContracts * 100.0 / totalContracts).setScale(2, RoundingMode.HALF_UP);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalContracts", totalContracts);
        result.put("completedContracts", completedContracts);
        result.put("terminatedContracts", terminatedContracts);
        result.put("contractSuccessRatePercent", successRate);
        result.put("totalDisputes", totalDisputes);
        result.put("openDisputes", openDisputes);
        result.put("totalTransactions", totalTransactions);
        result.put("transactionVolume", totalVolume);
        return result;
    }
}
