/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/config/SecurityConfig.java
 * Đây là file gì: File cấu hình Spring Security, mở public endpoint cần thiết và gắn JWT/audit filter.
 * Mục đích note: giải thích annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.config;

import com.aitasker.be.security.filter.AuditRequestFilter;
import com.aitasker.be.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

// Note: Annotation này đánh dấu class cấu hình bean cho Spring.
@Configuration
// Note: Annotation này bật Spring Security cho ứng dụng.
@EnableWebSecurity
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuditRequestFilter auditRequestFilter;

    // Note: Annotation này khai báo object được Spring quản lý và inject khi cần.
    @Bean
    // Note: Hàm `passwordEncoder` tạo bộ mã hóa mật khẩu dùng trong auth service.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Note: Annotation này khai báo object được Spring quản lý và inject khi cần.
    @Bean
    // Note: Hàm `auditRequestFilterRegistration` chỉ cho audit filter chạy trong Spring Security chain, tránh servlet container tự chạy trùng.
    public FilterRegistrationBean<AuditRequestFilter> auditRequestFilterRegistration(AuditRequestFilter filter) {
        FilterRegistrationBean<AuditRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    // Note: Annotation này khai báo object được Spring quản lý và inject khi cần.
    @Bean
    // Note: Hàm `securityFilterChain` cấu hình phân quyền, JWT filter và audit filter cho request đã xác thực.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/chatbot/**",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/payos/return").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/jobs",
                                "/api/v1/domains",
                                "/api/v1/skills"
                        ).permitAll()
                        .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/jobs/\\d+")).permitAll()
                        .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/jobs/\\d+/milestones")).permitAll()
                        .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/profiles/business/\\d+")).permitAll()
                        .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/profiles/business/by-job/\\d+")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditRequestFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
