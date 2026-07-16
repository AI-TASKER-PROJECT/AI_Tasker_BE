/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/filter/AuditRequestFilter.java
 * Đây là file gì: Filter ghi audit log dự phòng cho mọi request thay đổi dữ liệu của tài khoản đã đăng nhập.
 * Mục đích note: giải thích annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.filter;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.service.core.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Note: Annotation này cho Spring quản lý class như một component dùng chung.
@Component
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AuditRequestFilter extends OncePerRequestFilter {
    private static final List<String> MUTATING_METHODS = List.of("POST", "PATCH", "PUT", "DELETE");

    private final AuditLogService auditLogService;
    private final AccountRepository accountRepository;

    // Note: Hàm `doFilterInternal` ghi audit fallback sau khi request thành công nếu service chưa tự ghi log.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            recordFallbackAuditIfNeeded(request, response);
        }
    }

    // Note: Hàm `recordFallbackAuditIfNeeded` đảm bảo mọi role có log khi thực hiện thao tác thay đổi dữ liệu.
    private void recordFallbackAuditIfNeeded(HttpServletRequest request, HttpServletResponse response) {
        if (!MUTATING_METHODS.contains(request.getMethod())) return;
        if (response.getStatus() < 200 || response.getStatus() >= 300) return;
        if (Boolean.TRUE.equals(request.getAttribute(AuditLogService.REQUEST_ATTRIBUTE_LOGGED))) return;
        if (isPublicAuthRequest(request)) return;
        if (isPayOsSyncRequest(request)) return;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) return;

        accountRepository.findByEmailWithRole(authentication.getName())
                .map(AccountEntity::getAccountId)
                .ifPresent(accountId -> auditLogService.record(
                        resolveAction(request),
                        resolveEntityName(request),
                        resolveEntityId(request),
                        accountId
                ));
    }

    // Note: Hàm `isPublicAuthRequest` bỏ qua login/register/OTP vì các request này chưa đại diện cho một phiên role đã xác thực.
    private boolean isPublicAuthRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth/");
    }

    private boolean isPayOsSyncRequest(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && request.getRequestURI().matches("^/api/payments/payos/\\d+/sync$");
    }

    // Note: Hàm `resolveAction` chuyển request thành action tiếng Việt khi endpoint chưa tự khai báo action cụ thể.
    private String resolveAction(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if ("POST".equals(method)) return "Tạo hoặc gửi dữ liệu: " + uri;
        if ("PATCH".equals(method) || "PUT".equals(method)) return "Cập nhật dữ liệu: " + uri;
        if ("DELETE".equals(method)) return "Xóa hoặc vô hiệu hóa dữ liệu: " + uri;
        return "Thao tác dữ liệu: " + uri;
    }

    // Note: Hàm `resolveEntityName` lấy tên nhóm đối tượng từ URL để audit fallback vẫn đọc được ngữ cảnh.
    private String resolveEntityName(HttpServletRequest request) {
        String[] parts = request.getRequestURI().split("/");
        for (int index = 0; index < parts.length; index++) {
            if ("v1".equals(parts[index]) && index + 1 < parts.length) {
                return parts[index + 1];
            }
        }
        return request.getRequestURI();
    }

    // Note: Hàm `resolveEntityId` lấy id đầu tiên trong URL nếu có, giúp audit fallback gắn được mã đối tượng.
    private String resolveEntityId(HttpServletRequest request) {
        for (String part : request.getRequestURI().split("/")) {
            if (part.matches("\\d+")) return part;
        }
        return null;
    }
}
