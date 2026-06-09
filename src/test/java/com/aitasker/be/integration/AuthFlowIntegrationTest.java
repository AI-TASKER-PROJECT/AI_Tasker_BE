/*
 * NOTE FILE: src/test/java/com/aitasker/be/integration/AuthFlowIntegrationTest.java
 * Đây là file gì: File test kiểm tra luồng hoặc nghiệp vụ để phát hiện lỗi hồi quy khi thay đổi code.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
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
class AuthFlowIntegrationTest {

    private MockMvc mockMvc;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Autowired private WebApplicationContext webApplicationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @BeforeEach
    // Note: Hàm `setupMockMvc` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `registerAndLoginBusiness_shouldReturnTokenAndRole` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void registerAndLoginBusiness_shouldReturnTokenAndRole() throws Exception {
        // TAO EMAIL UNIQUE DE TRANH XUNG DOT DU LIEU GIUA CAC LAN CHAY TEST.
        String email = "biz_" + System.currentTimeMillis() + "@mail.com";

        String registerBody = """
                {
                  "email": "%s",
                  "password": "12345678",
                  "fullName": "BUSINESS TEST",
                  "phone": "0900000000",
                  "role": "BUSINESS"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

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
        assertThat(root.path("data").path("accessToken").asText()).isNotBlank();
        assertThat(root.path("data").path("role").asText()).isEqualTo("BUSINESS");
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `register_shouldNormalizeEmailBeforeLogin` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void register_shouldNormalizeEmailBeforeLogin() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());
        String storedEmail = "mixed_" + suffix + "@mail.com";
        String registerBody = """
                {
                  "email": "Mixed_%s@Mail.Com",
                  "password": "12345678",
                  "fullName": "MIXED EMAIL TEST",
                  "phone": "0900000001",
                  "role": "BUSINESS"
                }
                """.formatted(suffix);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode registerRoot = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        assertThat(registerRoot.path("data").path("email").asText()).isEqualTo(storedEmail);

        String loginBody = """
                {
                  "email": "%s",
                  "password": "12345678"
                }
                """.formatted(storedEmail);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `secureEndpoint_withValidToken_shouldReturnOk` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void secureEndpoint_withValidToken_shouldReturnOk() throws Exception {
        // KIEM CHUNG JWT DA DUOC CAP VA DUNG DE GOI ENDPOINT PRIVATE.
        String email = "secure_" + System.currentTimeMillis() + "@mail.com";
        String registerBody = """
                {
                  "email": "%s",
                  "password": "12345678",
                  "fullName": "SECURE TEST",
                  "phone": "0900111111",
                  "role": "EXPERT"
                }
                """.formatted(email);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "email": "%s",
                  "password": "12345678"
                }
                """.formatted(email);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/test/secure").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
