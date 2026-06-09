/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/OpenApiConfig.java
 * Đây là file gì: File cấu hình bean/thư viện, giúp Spring Boot khởi tạo hành vi dùng chung.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

// Note: Annotation này đánh dấu class cấu hình bean cho Spring.
@Configuration
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@OpenAPIDefinition(
        info = @Info(
                title = "AITASKER Backend API",
                version = "v1",
                description = "Interactive API documentation for the AITASKER backend."
        ),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
)
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
