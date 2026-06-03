package com.aitasker.be.security;

import com.aitasker.be.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    // LAY EMAIL DANG NHAP TU SECURITY CONTEXT DE TRUY XUAT DU LIEU DUNG CHU THE.
    public static String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("CHUA DANG NHAP");
        }
        return auth.getName();
    }

    // KIEM TRA QUYEN THEO ROLE TOKEN DA GAN VAO CONTEXT.
    public static boolean hasRole(String roleName) {
        String authority = "ROLE_" + roleName;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }
}
