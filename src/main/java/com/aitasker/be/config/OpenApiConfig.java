/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/OpenApiConfig.java
 * Day la file gi: File cau hinh bean/thu vien, giup Spring Boot khoi tao hanh vi dung chung.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Note: Annotation nay danh dau class cau hinh bean cho Spring.
@Configuration
// Note: Annotation nay cung cap metadata tong quan cho OpenAPI/Swagger.
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
// Note: Annotation nay khai bao scheme xac thuc de Swagger hien thi nut Authorize.
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Value("${app.backend-url:}")
    private String backendUrl;

    public static final String BEARER_AUTH = "bearerAuth";
    public static final String AUTH_FLOW = "Auth Flow";
    public static final String PROFILE_FLOW = "Profile Verification Flow";
    public static final String JOB_FLOW = "Job Draft & Publish Flow";
    public static final String PROPOSAL_FLOW = "Proposal Flow";
    public static final String WALLET_FLOW = "Wallet & Payment Flow";
    public static final String CONTRACT_FLOW = "Contract Execution Flow";
    public static final String NOTIFICATION_FLOW = "Notification Flow";
    public static final String CATALOG_FLOW = "Catalog & Reference Flow";
    public static final String AI_FLOW = "AI & Matching Flow";
    public static final String ADMIN_FLOW = "Admin & Governance Flow";
    public static final String SYSTEM_FLOW = "System & Test Flow";

    private static final List<Tag> ORDERED_TAGS = List.of(
            new Tag().name(AUTH_FLOW).description("Dang ky, dang nhap, OTP email va tra cuu ma so thue."),
            new Tag().name(PROFILE_FLOW).description("Tao, xem va duyet ho so Business/Expert va portfolio."),
            new Tag().name(JOB_FLOW).description("Tao draft, cap nhat, gan taxonomy, sinh SoW va publish job."),
            new Tag().name(PROPOSAL_FLOW).description("Submit proposal, review proposal, matching va de xuat expert."),
            new Tag().name(WALLET_FLOW).description("Wallet, top-up, membership, credits, quota va withdrawal."),
            new Tag().name(CONTRACT_FLOW).description("Contract, milestone, deliverable, dispute, termination va review."),
            new Tag().name(NOTIFICATION_FLOW).description("Thong bao trong he thong."),
            new Tag().name(CATALOG_FLOW).description("Danh muc domain, skill va technology."),
            new Tag().name(AI_FLOW).description("Chatbot va cac endpoint AI ho tro nghiep vu."),
            new Tag().name(ADMIN_FLOW).description("Quan tri account, settings, analytics, reviews va wallet system."),
            new Tag().name(SYSTEM_FLOW).description("Health check va endpoint test ky thuat.")
    );

    // Note: Annotation nay khai bao object duoc Spring quan ly va inject khi can.
    @Bean
    // Note: Ham `openAPI` khai bao bean hoac cau hinh dung chung cho ung dung.
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("AITASKER Backend API")
                        .version("v1")
                        .description("Interactive API documentation for the AITASKER backend."))
                .tags(ORDERED_TAGS);

        if (backendUrl != null && !backendUrl.isBlank()) {
            openAPI.setServers(List.of(new Server().url(backendUrl.trim())));
        }

        return openAPI;
    }

    // Note: Annotation nay khai bao object duoc Spring quan ly va inject khi can.
    @Bean
    // Note: Ham `flowTagCustomizer` khai bao bean hoac cau hinh dung chung cho ung dung.
    public OpenApiCustomizer flowTagCustomizer() {
        return openApi -> {
            openApi.setTags(ORDERED_TAGS);
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperations().forEach(operation ->
                            operation.setTags(List.of(resolveFlowTag(path))))
            );
        };
    }

    // Note: Ham `resolveFlowTag` khai bao bean hoac cau hinh dung chung cho ung dung.
    private String resolveFlowTag(String path) {
        if (path.startsWith("/api/auth")) {
            return AUTH_FLOW;
        }

        if (path.startsWith("/api/v1/profiles")) {
            return PROFILE_FLOW;
        }

        if (path.equals("/api/jobs/generate-sow")
                || path.equals("/api/v1/jobs")
                || path.equals("/api/v1/jobs/my")
                || path.equals("/api/v1/jobs/{jobId}")
                || path.equals("/api/v1/jobs/{jobId}/publish")
                || path.equals("/api/v1/jobs/{jobId}/status")
                || path.equals("/api/v1/jobs/{jobId}/milestones")
                || path.equals("/api/v1/jobs/{jobId}/milestones/{milestoneId}")
                || path.equals("/api/v1/jobs/{jobId}/domains")
                || path.equals("/api/v1/jobs/{jobId}/skills")
                || path.equals("/api/v1/jobs/{jobId}/technologies")) {
            return JOB_FLOW;
        }

        if (path.equals("/api/v1/jobs/{jobId}/proposals")
                || path.equals("/api/v1/proposals")
                || path.equals("/api/v1/proposals/{proposalId}")
                || path.equals("/api/v1/proposals/my")
                || path.equals("/api/v1/proposals/{proposalId}/status")
                || path.equals("/api/v1/proposals/file")
                || path.equals("/api/v1/jobs/{jobId}/matching")
                || path.equals("/api/jobs/{jobPostingId}/expert-candidates")
                || path.equals("/api/jobs/{jobPostingId}/expert-recommendations")
                || path.equals("/api/jobs/{jobPostingId}/expert-recommendations/{expertId}/select")) {
            return PROPOSAL_FLOW;
        }

        if (path.startsWith("/api/payments/payos")
                || path.startsWith("/api/credits")
                || path.startsWith("/api/membership")
                || path.startsWith("/api/wallet")
                || path.startsWith("/api/v1/wallet")
                || path.startsWith("/api/users/me/quota")
                || path.startsWith("/api/v1/withdrawal-requests")
                || path.startsWith("/api/v1/admin/withdrawal-requests")) {
            return WALLET_FLOW;
        }

        if (path.startsWith("/api/v1/contracts")
                || path.startsWith("/api/v1/milestones")
                || path.startsWith("/api/v1/disputes")
                || path.startsWith("/api/v1/termination-requests")
                || path.startsWith("/api/v1/case-attachments")
                || path.startsWith("/api/v1/admin/contracts")) {
            return CONTRACT_FLOW;
        }

        if (path.startsWith("/api/v1/notifications")) {
            return NOTIFICATION_FLOW;
        }

        if (path.equals("/api/v1/domains")
                || path.equals("/api/v1/skills")
                || path.equals("/api/v1/technologies")
                || path.startsWith("/api/v1/domains/{")
                || path.startsWith("/api/v1/skills/{")
                || path.startsWith("/api/v1/technologies/{")) {
            return CATALOG_FLOW;
        }

        if (path.startsWith("/api/chatbot")) {
            return AI_FLOW;
        }

        if (path.startsWith("/api/v1/admin")) {
            return ADMIN_FLOW;
        }

        if (path.startsWith("/api/health") || path.startsWith("/api/test")) {
            return SYSTEM_FLOW;
        }

        return SYSTEM_FLOW;
    }
}
