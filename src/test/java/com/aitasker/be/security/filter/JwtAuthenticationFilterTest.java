package com.aitasker.be.security.filter;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentTokenVersion_shouldAuthenticate() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, accountRepository);
        AccountEntity account = account(3);

        when(jwtService.extractUsername("token")).thenReturn("test@mail.com");
        when(accountRepository.findByEmailWithRole("test@mail.com")).thenReturn(Optional.of(account));
        when(jwtService.isTokenValid("token", "test@mail.com")).thenReturn(true);
        when(jwtService.extractTokenVersion("token")).thenReturn(3);

        MockHttpServletResponse response = runFilter(filter, "/api/v1/jobs/my");

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void staleTokenVersion_shouldReturnUnauthorized() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, accountRepository);
        AccountEntity account = account(3);

        when(jwtService.extractUsername("token")).thenReturn("test@mail.com");
        when(accountRepository.findByEmailWithRole("test@mail.com")).thenReturn(Optional.of(account));
        when(jwtService.isTokenValid("token", "test@mail.com")).thenReturn(true);
        when(jwtService.extractTokenVersion("token")).thenReturn(2);

        MockHttpServletResponse response = runFilter(filter, "/api/v1/jobs/my");

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void staleTokenVersion_onAuthEndpoint_shouldContinueAnonymously() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, accountRepository);
        AccountEntity account = account(3);

        when(jwtService.extractUsername("token")).thenReturn("test@mail.com");
        when(accountRepository.findByEmailWithRole("test@mail.com")).thenReturn(Optional.of(account));
        when(jwtService.isTokenValid("token", "test@mail.com")).thenReturn(true);
        when(jwtService.extractTokenVersion("token")).thenReturn(2);

        MockHttpServletResponse response = runFilter(filter, "/api/auth/refresh");

        assertEquals(200, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletResponse runFilter(JwtAuthenticationFilter filter, String path)
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private AccountEntity account(int activeTokenVersion) {
        RoleEntity role = new RoleEntity();
        role.setRoleName("BUSINESS");
        return AccountEntity.builder()
                .accountId(1)
                .email("test@mail.com")
                .fullName("Test User")
                .status("Approved")
                .role(role)
                .activeTokenVersion(activeTokenVersion)
                .build();
    }
}
