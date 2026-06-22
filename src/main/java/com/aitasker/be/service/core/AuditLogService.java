/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/AuditLogService.java
 * Đây là file gì: Service tập trung ghi và đọc audit log, chuẩn hóa action tiếng Việt và phân quyền xem log cho admin.
 * Mục đích note: giải thích annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.admin.AuditLogResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AuditLogService {
    public static final String GROUP_INTERNAL = "INTERNAL";
    public static final String GROUP_EXTERNAL = "EXTERNAL";
    public static final String ACTION_APPROVE_BUSINESS_PROFILE = "Duyệt hồ sơ doanh nghiệp";
    public static final String ACTION_REJECT_BUSINESS_PROFILE = "Từ chối hồ sơ doanh nghiệp";
    public static final String ACTION_APPROVE_EXPERT_PROFILE = "Duyệt hồ sơ chuyên gia";
    public static final String ACTION_REJECT_EXPERT_PROFILE = "Từ chối hồ sơ chuyên gia";
    public static final String ACTION_CREATE_ACCOUNT = "Tạo tài khoản";
    public static final String ACTION_UPDATE_ACCOUNT = "Cập nhật tài khoản";
    public static final String ACTION_CHANGE_ACCOUNT_STATUS = "Đổi trạng thái tài khoản";
    public static final String ACTION_CREATE_STAFF_PROFILE = "Tạo hồ sơ staff";
    public static final String ACTION_UPDATE_STAFF_PROFILE = "Cập nhật hồ sơ staff";
    public static final String ACTION_UPDATE_SYSTEM_SETTING = "Cập nhật cài đặt hệ thống";
    public static final String ACTION_UPSERT_BUSINESS_PROFILE = "Cập nhật hồ sơ doanh nghiệp";
    public static final String ACTION_UPSERT_EXPERT_PROFILE = "Cập nhật hồ sơ chuyên gia";
    public static final String ACTION_UPLOAD_BUSINESS_LICENSE = "Tải giấy phép kinh doanh";
    public static final String ACTION_UPLOAD_EXPERT_CERTIFICATE = "Tải chứng chỉ chuyên gia";
    public static final String ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE = "Tải file portfolio chuyên gia";
    public static final String ACTION_UPSERT_PORTFOLIO = "Cập nhật portfolio chuyên gia";
    public static final String ACTION_CREATE_JOB_DRAFT = "Tạo job nháp";
    public static final String ACTION_UPDATE_JOB_DRAFT = "Cập nhật job nháp";
    public static final String ACTION_CHANGE_JOB_STATUS = "Đổi trạng thái job";
    public static final String ACTION_SUBMIT_PROPOSAL = "Gửi proposal";
    public static final String ACTION_REVIEW_PROPOSAL = "Duyệt proposal";
    public static final String ACTION_CREATE_CONTRACT_DRAFT = "Tạo hợp đồng nháp";
    public static final String ACTION_REQUEST_CONTRACT_CHANGE = "Yêu cầu chỉnh sửa hợp đồng";
    public static final String ACTION_ACCEPT_CONTRACT = "Xác nhận hợp đồng";
    public static final String ACTION_SIGN_NDA = "Ký NDA";
    public static final String ACTION_REJECT_CONTRACT = "Từ chối hợp đồng";
    public static final String ACTION_ACTIVATE_CONTRACT = "Kích hoạt hợp đồng";
    public static final String ACTION_COMPLETE_CONTRACT = "Hoàn tất hợp đồng";
    public static final String ACTION_TERMINATE_CONTRACT = "Chấm dứt hợp đồng";
    public static final String ACTION_CREATE_MILESTONE = "Tạo milestone";
    public static final String ACTION_UPDATE_MILESTONE = "Cập nhật milestone";
    public static final String ACTION_COMPLETE_MILESTONE = "Hoàn tất milestone";
    public static final String ACTION_CREATE_ACCEPTANCE_CRITERIA = "Tạo tiêu chí nghiệm thu";
    public static final String ACTION_SUBMIT_DELIVERABLE = "Nộp sản phẩm bàn giao";
    public static final String ACTION_CREATE_TRANSACTION = "Tạo giao dịch";
    public static final String ACTION_UPDATE_TRANSACTION_STATUS = "Cập nhật trạng thái giao dịch";
    public static final String ACTION_CREATE_DISPUTE = "Tạo tranh chấp";
    public static final String ACTION_ASSIGN_DISPUTE = "Phân công tranh chấp";
    public static final String ACTION_RESOLVE_DISPUTE = "Xử lý tranh chấp";
    public static final String ACTION_RECORD_DEMO_TESTING = "Ghi nhận demo testing";
    public static final String ACTION_ISSUE_TECHNICAL_REPORT = "Gửi báo cáo kỹ thuật";
    public static final String ACTION_RUN_SLA_AUTO_APPROVE = "Chạy tự động duyệt SLA";
    public static final String ACTION_PROCESS_PAYMENT_WEBHOOK = "Xử lý webhook thanh toán";
    public static final String ACTION_CREATE_REVIEW = "Gửi đánh giá";

    private final AccessService accessService;
    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final MilestoneRepository milestoneRepository;
    private final DisputeRepository disputeRepository;
    private final TransactionRepository transactionRepository;
    private final StaffRepository staffRepository;
    private final PortfolioRepository portfolioRepository;
    private final ReviewRepository reviewRepository;

    public static final String REQUEST_ATTRIBUTE_LOGGED = "aitasker.audit.logged";

    // Note: Annotation này đảm bảo thao tác ghi audit log chạy trong transaction.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // Note: Hàm `record` ghi một sự kiện quan trọng vào audit log với action tiếng Việt và thông tin request nếu có.
    public void record(String action, String entityName, String entityId, Integer actorAccountId) {
        HttpServletRequest request = currentRequest();
        auditLogRepository.save(AuditLogEntity.builder()
                .actorAccountId(actorAccountId)
                .action(normalizeAction(action))
                .entityName(entityName)
                .entityId(entityId)
                .build());
        if (request != null) {
            request.setAttribute(REQUEST_ATTRIBUTE_LOGGED, Boolean.TRUE);
        }
    }

    // Note: Annotation này giúp dữ liệu đọc ổn định trong transaction và không ghi thay đổi ngoài ý muốn.
    @Transactional(readOnly = true)
    // Note: Hàm `listForAdmin` chỉ cho ADMIN đọc audit log và lọc theo nhóm role nội bộ hoặc bên ngoài.
    public List<AuditLogResponse> listForAdmin(String actorGroup) {
        accessService.requireRole("ADMIN");
        String normalizedGroup = normalizeActorGroup(actorGroup);
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .filter(log -> normalizedGroup == null || normalizedGroup.equalsIgnoreCase(log.getActorGroup()))
                .toList();
    }

    // Note: Hàm `toResponse` gắn thông tin account actor vào audit log để giao diện hiển thị người thực hiện.
    private AuditLogResponse toResponse(AuditLogEntity log) {
        AccountEntity actor = accountRepository.findById(log.getActorAccountId()).orElse(null);
        String actorGroup = resolveActorGroup(actor);
        return attachEntityInfo(AuditLogResponse.from(log, actor, actorGroup, translateLegacyAction(log.getAction())), log)
                .fallbackEntityOwner(actor);
    }

    // Note: Hàm `attachEntityInfo` tìm tên đối tượng và chủ sở hữu theo từng loại entity để audit log hiển thị dễ đọc.
    private AuditLogResponse attachEntityInfo(AuditLogResponse response, AuditLogEntity log) {
        Integer id = parseInteger(log.getEntityId());
        if (id == null) return response.attachEntityInfo(log.getEntityName(), null);
        return switch (log.getEntityName()) {
            case "account" -> accountRepository.findById(id)
                    .map(account -> response.attachEntityInfo(account.getFullName(), account))
                    .orElse(response.attachEntityInfo("Account #" + id, null));
            case "business_profiles" -> businessProfileRepository.findById(id)
                    .map(profile -> response.attachEntityInfo(profile.getCompanyName(), accountById(profile.getAccountId())))
                    .orElse(response.attachEntityInfo("Hồ sơ doanh nghiệp #" + id, null));
            case "expert_profiles" -> expertProfileRepository.findById(id)
                    .map(profile -> response.attachEntityInfo("Hồ sơ chuyên gia #" + id, accountById(profile.getAccountId())))
                    .orElse(response.attachEntityInfo("Hồ sơ chuyên gia #" + id, null));
            case "portfolios" -> response.attachEntityInfo("Portfolio #" + id, expertOwnerByPortfolioId(id));
            case "jobs" -> jobRepository.findById(id)
                    .map(job -> response.attachEntityInfo(job.getTitle(), businessOwnerByBusinessId(job.getBusinessId())))
                    .orElse(response.attachEntityInfo("Job #" + id, null));
            case "proposals" -> proposalRepository.findById(id)
                    .map(proposal -> response.attachEntityInfo("Proposal #" + id, expertOwnerByExpertId(proposal.getExpertId())))
                    .orElse(response.attachEntityInfo("Proposal #" + id, null));
            case "contracts" -> contractRepository.findById(id)
                    .map(contract -> response.attachEntityInfo("Hợp đồng #" + id, businessOwnerByBusinessId(contract.getBusinessId())))
                    .orElse(response.attachEntityInfo("Hợp đồng #" + id, null));
            case "milestones" -> milestoneRepository.findById(id)
                    .map(milestone -> response.attachEntityInfo("Milestone #" + id, jobRepository.findById(milestone.getJobId()).map(JobEntity::getBusinessId).map(this::businessOwnerByBusinessId).orElse(null)))
                    .orElse(response.attachEntityInfo("Milestone #" + id, null));
            case "transactions" -> transactionRepository.findById(Long.valueOf(id))
                    .map(transaction -> response.attachEntityInfo("Giao dịch #" + id, null))
                    .orElse(response.attachEntityInfo("Giao dịch #" + id, null));
            case "disputes" -> disputeRepository.findById(id)
                    .map(dispute -> response.attachEntityInfo("Tranh chấp #" + id, null))
                    .orElse(response.attachEntityInfo("Tranh chấp #" + id, null));
            case "staffs" -> staffRepository.findById(id)
                    .map(staff -> response.attachEntityInfo("Staff #" + id, accountById(staff.getAccountId())))
                    .orElse(response.attachEntityInfo("Staff #" + id, null));
            case "reviews" -> reviewRepository.findById(id)
                    .map(review -> response.attachEntityInfo("Đánh giá #" + id, accountById(review.getRevieweeId())))
                    .orElse(response.attachEntityInfo("Đánh giá #" + id, null));
            case "system_settings" -> response.attachEntityInfo(log.getEntityId(), null);
            default -> response.attachEntityInfo(log.getEntityName() + " #" + id, null);
        };
    }

    // Note: Hàm `normalizeAction` chuẩn hóa action ghi xuống database theo tiếng Việt, kể cả khi code cũ truyền action tiếng Anh.
    private String normalizeAction(String action) {
        String translated = translateLegacyAction(action);
        if (translated == null || translated.isBlank()) throw new AppException("AUDIT ACTION KHONG HOP LE");
        String normalized = translated.trim();
        return normalized.length() <= 100 ? normalized : normalized.substring(0, 100);
    }

    // Note: Hàm `translateLegacyAction` đổi các action cũ dạng mã kỹ thuật sang tiếng Việt để hiển thị thống nhất.
    private String translateLegacyAction(String action) {
        if (action == null) return null;
        return switch (action.trim()) {
            case "APPROVE_BUSINESS_PROFILE" -> ACTION_APPROVE_BUSINESS_PROFILE;
            case "REJECT_BUSINESS_PROFILE" -> ACTION_REJECT_BUSINESS_PROFILE;
            case "APPROVE_EXPERT_PROFILE" -> ACTION_APPROVE_EXPERT_PROFILE;
            case "REJECT_EXPERT_PROFILE" -> ACTION_REJECT_EXPERT_PROFILE;
            default -> action.trim();
        };
    }

    // Note: Hàm `normalizeActorGroup` kiểm tra giá trị tab lọc log để tránh truyền nhóm role không hợp lệ.
    private String normalizeActorGroup(String actorGroup) {
        if (actorGroup == null || actorGroup.isBlank()) return null;
        String normalized = actorGroup.trim().toUpperCase();
        if (GROUP_INTERNAL.equals(normalized) || GROUP_EXTERNAL.equals(normalized)) return normalized;
        throw new AppException("NHOM AUDIT LOG KHONG HOP LE");
    }

    // Note: Hàm `resolveActorGroup` phân loại actor nội bộ hoặc bên ngoài dựa trên role của tài khoản.
    private String resolveActorGroup(AccountEntity actor) {
        String roleName = actor == null || actor.getRole() == null ? null : actor.getRole().getRoleName();
        if (roleName != null && (roleName.equalsIgnoreCase("ADMIN") || roleName.equalsIgnoreCase("STAFF"))) {
            return GROUP_INTERNAL;
        }
        return GROUP_EXTERNAL;
    }

    // Note: Hàm `currentRequest` lấy request hiện tại để đánh dấu request đã được ghi audit log, tránh ghi trùng.
    // Note: Hàm `parseInteger` chuyển id dạng text trong audit log về số để tra cứu entity liên quan.
    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // Note: Hàm `accountById` lấy tài khoản theo id và trả null nếu đối tượng đã bị xóa hoặc không tồn tại.
    private AccountEntity accountById(Integer accountId) {
        return accountId == null ? null : accountRepository.findById(accountId).orElse(null);
    }

    // Note: Hàm `businessOwnerByBusinessId` tìm account doanh nghiệp sở hữu một hồ sơ/job/contract.
    private AccountEntity businessOwnerByBusinessId(Integer businessId) {
        return businessId == null ? null : businessProfileRepository.findById(businessId)
                .map(BusinessProfileEntity::getAccountId)
                .map(this::accountById)
                .orElse(null);
    }

    // Note: Hàm `expertOwnerByExpertId` tìm account chuyên gia sở hữu một proposal/contract.
    private AccountEntity expertOwnerByExpertId(Integer expertId) {
        return expertId == null ? null : expertProfileRepository.findById(expertId)
                .map(ExpertProfileEntity::getAccountId)
                .map(this::accountById)
                .orElse(null);
    }

    // Note: Hàm `expertOwnerByPortfolioId` tìm account chuyên gia sở hữu portfolio được ghi trong audit log.
    private AccountEntity expertOwnerByPortfolioId(Integer portfolioId) {
        return portfolioId == null ? null : portfolioRepository.findById(portfolioId)
                .map(PortfolioEntity::getExpertId)
                .map(this::expertOwnerByExpertId)
                .orElse(null);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

}
