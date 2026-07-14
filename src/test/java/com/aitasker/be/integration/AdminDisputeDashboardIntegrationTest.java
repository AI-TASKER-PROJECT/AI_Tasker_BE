package com.aitasker.be.integration;

import com.aitasker.be.service.auth.EmailOtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.import=",
        "DB_HOST=127.0.0.1",
        "DB_PORT=5433",
        "DB_NAME=${TEST_DB_NAME:aitasker_db}",
        "DB_USER=aitasker",
        "DB_PASSWORD=aitasker123",
        "APP_JWT_SECRET=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "app.jwt.secret=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh}",
        "spring.datasource.username=aitasker",
        "spring.datasource.password=aitasker123"
})
@Transactional
class AdminDisputeDashboardIntegrationTest {

    private MockMvc mockMvc;
    @Autowired private WebApplicationContext webApplicationContext;
    @MockitoBean private EmailOtpService emailOtpService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(emailOtpService.isEmailVerified(anyString())).thenReturn(true);
    }

    @Test
    void adminDisputes_shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/disputes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDisputeDetail_shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/disputes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDisputes_shouldRejectBusinessRole() throws Exception {
        String businessEmail = "biz_dispute_dash_" + System.currentTimeMillis() + "@mail.com";
        registerAccount(businessEmail, "BUSINESS");
        String businessToken = loginAndGetToken(businessEmail);

        mockMvc.perform(get("/api/v1/admin/disputes")
                        .header("Authorization", "Bearer " + businessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDisputeDetail_shouldRejectExpertRole() throws Exception {
        String expertEmail = "exp_dispute_dash_" + System.currentTimeMillis() + "@mail.com";
        registerAccount(expertEmail, "EXPERT");
        String expertToken = loginAndGetToken(expertEmail);

        mockMvc.perform(get("/api/v1/admin/disputes/1")
                        .header("Authorization", "Bearer " + expertToken))
                .andExpect(status().isUnauthorized());
    }

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

    private String loginAndGetToken(String email) throws Exception {
        String loginBody = """
                {
                  "email": "%s",
                  "password": "12345678"
                }
                """.formatted(email);
        String responseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("data").get("accessToken").asText();
    }
}
