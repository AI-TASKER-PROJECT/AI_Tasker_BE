/*
 * NOTE FILE: src/test/java/com/aitasker/be/integration/AdminFlowIntegrationTest.java
 * Đây là file gì: File test kiểm tra luồng hoặc nghiệp vụ để phát hiện lỗi hồi quy khi thay đổi code.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.integration;

import com.aitasker.be.service.auth.EmailOtpService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Note: Annotation này chạy test với Spring context đầy đủ.
@SpringBootTest(properties = {
        "spring.config.import=",
        "DB_HOST=127.0.0.1",
        "DB_PORT=5433",
        "DB_NAME=aitasker_db",
        "DB_USER=aitasker",
        "DB_PASSWORD=aitasker123",
        "APP_JWT_SECRET=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "app.jwt.secret=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh",
        "spring.datasource.username=aitasker",
        "spring.datasource.password=aitasker123"
})
// Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
@Transactional
class AdminFlowIntegrationTest {

    private MockMvc mockMvc;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Autowired private WebApplicationContext webApplicationContext;
    @MockitoBean private EmailOtpService emailOtpService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @BeforeEach
    // Note: Hàm `setupMockMvc` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(emailOtpService.isEmailVerified(anyString())).thenReturn(true);
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `adminEndpoints_shouldEnforceAuthorization` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void adminEndpoints_shouldEnforceAuthorization() throws Exception {
        // API ADMIN PHAI CHAN KHI KHONG CO JWT.
        mockMvc.perform(get("/api/v1/admin/settings"))
                .andExpect(status().isUnauthorized());

        // BUSINESS CO JWT NHUNG KHONG DUOC TRUY CAP SETTINGS ADMIN.
        String businessEmail = "biz_admin_case_" + System.currentTimeMillis() + "@mail.com";
        registerAccount(businessEmail, "BUSINESS");
        String businessToken = loginAndGetToken(businessEmail);

        mockMvc.perform(get("/api/v1/admin/settings")
                        .header("Authorization", "Bearer " + businessToken))
                .andExpect(status().isUnauthorized());
    }

    // Note: Hàm `registerAccount` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    private void registerAccount(String email, String role) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "12345678",
                  "fullName": "TEST USER",
                  "phone": "0900123456",
                  "role": "%s"
                }
                """.formatted(email, role);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    // Note: Hàm `loginAndGetToken` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    private String loginAndGetToken(String email) throws Exception {
        String loginBody = """
                {
                  "email": "%s",
                  "password": "12345678"
                }
                """.formatted(email);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
