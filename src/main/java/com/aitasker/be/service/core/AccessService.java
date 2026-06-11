/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/AccessService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AccessService {
    private final AccountRepository accountRepository;

    // LAY THONG TIN TAI KHOAN DANG NHAP DE GAN QUYEN NGHIEP VU.
    // Note: Hàm `currentAccount` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public AccountEntity currentAccount() {
        String email = SecurityUtils.getCurrentEmail();
        return accountRepository.findByEmailWithRole(email).orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN"));
    }

    // KIEM TRA ROLE THEO NGHIEP VU DE CHAN GOI API CHEO THAM QUYEN.
    // Note: Hàm `requireRole` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public void requireRole(String... roles) {
        String role = currentAccount().getRole().getRoleName();
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new ForbiddenException("BAN KHONG CO QUYEN THUC HIEN CHUC NANG NAY");
    }

    // Chan cac chuc nang nghiep vu cua BUSINESS/EXPERT cho toi khi ho so KYB/KYC duoc duyet.
    // Note: Hàm `requireApprovedAccount` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public void requireApprovedAccount() {
        AccountEntity account = currentAccount();
        if (!"Approved".equalsIgnoreCase(account.getStatus())) {
            throw new ForbiddenException("TAI KHOAN CHUA DUOC DUYET HO SO KYB/KYC");
        }
    }
}
