/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/config/SecurityConfig.java
 * Đây là file gì: File security cấu hình hoặc xử lý xác thực, phân quyền và JWT cho các API.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.config;

import com.aitasker.be.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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

// Note: Annotation này đánh dấu class cấu hình bean cho Spring.
@Configuration
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@EnableWebSecurity
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
// BAT SPRING SECURITY, MO PUBLIC AUTH API, CHAN API CON LAI, GAN JWT FILTER.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Note: Annotation này khai báo object được Spring quản lý và inject khi cần.
    @Bean
    // Note: Hàm `passwordEncoder` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Note: Annotation này khai báo object được Spring quản lý và inject khi cần.
    @Bean
    // Note: Hàm `securityFilterChain` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
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
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/jobs", "/api/v1/jobs/*", "/api/v1/domains", "/api/v1/skills").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
