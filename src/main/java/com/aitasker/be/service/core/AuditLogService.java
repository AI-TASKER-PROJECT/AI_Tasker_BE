/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/AuditLogService.java
 * Day la file gi: Service tap trung ghi va doc audit log cho admin.
 * Muc dich note: chuan hoa action/object de audit log hien thi nhu su kien nghiep vu, khong lo path/id ky thuat.
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

@Service
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

    public static final String REQUEST_ATTRIBUTE_LOGGED = "aitasker.audit.logged";

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
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final ContractDepositRepository contractDepositRepository;
    private final MembershipPurchaseRepository membershipPurchaseRepository;
    private final MembershipPackageRepository membershipPackageRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final QuotaUsageLogRepository quotaUsageLogRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final NotificationRepository notificationRepository;
    private final TechnologyRepository technologyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listForAdmin(String actorGroup) {
        accessService.requireRole("ADMIN");
        String normalizedGroup = normalizeActorGroup(actorGroup);
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .filter(log -> normalizedGroup == null || normalizedGroup.equalsIgnoreCase(log.getActorGroup()))
                .toList();
    }

    private AuditLogResponse toResponse(AuditLogEntity log) {
        AccountEntity actor = accountRepository.findById(log.getActorAccountId()).orElse(null);
        String actorGroup = resolveActorGroup(actor);
        return attachEntityInfo(AuditLogResponse.from(log, actor, actorGroup, translateLegacyAction(log.getAction())), log, actor)
                .fallbackEntityOwner(actor);
    }

    private AuditLogResponse attachEntityInfo(AuditLogResponse response, AuditLogEntity log, AccountEntity actor) {
        AuditLogResponse pathResponse = attachPathEntityInfo(response, log, actor);
        if (pathResponse != null) return pathResponse;

        Integer id = parseInteger(log.getEntityId());
        if (id == null) {
            String label = cleanEntityFallback(log.getEntityName());
            return response.attachEntityInfo(label, label, actor);
        }

        return switch (log.getEntityName()) {
            case "account" -> accountRepository.findById(id)
                    .map(account -> response.attachEntityInfo("Tài khoản của " + displayAccount(account), "Tài khoản", account))
                    .orElseGet(() -> response.attachEntityInfo("Tài khoản", "Tài khoản", null));
            case "business_profiles" -> businessProfileRepository.findById(id)
                    .map(profile -> response.attachEntityInfo("Hồ sơ doanh nghiệp của " + profile.getCompanyName(), "Hồ sơ doanh nghiệp", accountById(profile.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Hồ sơ doanh nghiệp", "Hồ sơ doanh nghiệp", null));
            case "expert_profiles" -> expertProfileRepository.findById(id)
                    .map(profile -> {
                        AccountEntity owner = accountById(profile.getAccountId());
                        return response.attachEntityInfo("Hồ sơ chuyên gia của " + displayAccount(owner), "Hồ sơ chuyên gia", owner);
                    })
                    .orElseGet(() -> response.attachEntityInfo("Hồ sơ chuyên gia", "Hồ sơ chuyên gia", null));
            case "portfolios" -> {
                AccountEntity owner = expertOwnerByPortfolioId(id);
                yield response.attachEntityInfo("Portfolio của " + displayAccount(owner), "Portfolio", owner);
            }
            case "jobs" -> jobRepository.findById(id)
                    .map(job -> response.attachEntityInfo("Job: " + job.getTitle(), "Job", businessOwnerByBusinessId(job.getBusinessId())))
                    .orElseGet(() -> response.attachEntityInfo("Job", "Job", null));
            case "proposals" -> proposalRepository.findById(id)
                    .map(proposal -> response.attachEntityInfo(proposalDisplay(proposal), "Proposal", expertOwnerByExpertId(proposal.getExpertId())))
                    .orElseGet(() -> response.attachEntityInfo("Proposal", "Proposal", null));
            case "contracts" -> contractRepository.findById(id)
                    .map(contract -> response.attachEntityInfo(contractDisplay(contract), "Hợp đồng", businessOwnerByBusinessId(contract.getBusinessId())))
                    .orElseGet(() -> response.attachEntityInfo("Hợp đồng", "Hợp đồng", null));
            case "milestones" -> milestoneRepository.findById(id)
                    .map(milestone -> response.attachEntityInfo("Cột mốc: " + milestone.getMilestoneName(), "Cột mốc", milestoneOwner(milestone)))
                    .orElseGet(() -> response.attachEntityInfo("Cột mốc", "Cột mốc", null));
            case "transactions" -> transactionRepository.findById(Long.valueOf(id))
                    .map(transaction -> response.attachEntityInfo(transactionDisplay(transaction), "Giao dịch", null))
                    .orElseGet(() -> response.attachEntityInfo("Giao dịch", "Giao dịch", null));
            case "disputes" -> disputeRepository.findById(id)
                    .map(dispute -> response.attachEntityInfo(disputeDisplay(dispute), "Tranh chấp", null))
                    .orElseGet(() -> response.attachEntityInfo("Tranh chấp", "Tranh chấp", null));
            case "staffs" -> staffRepository.findById(id)
                    .map(staff -> {
                        AccountEntity owner = accountById(staff.getAccountId());
                        return response.attachEntityInfo("Hồ sơ staff của " + displayAccount(owner), "Hồ sơ staff", owner);
                    })
                    .orElseGet(() -> response.attachEntityInfo("Hồ sơ staff", "Hồ sơ staff", null));
            case "reviews" -> reviewRepository.findById(id)
                    .map(review -> {
                        AccountEntity reviewee = accountById(review.getRevieweeId());
                        return response.attachEntityInfo("Đánh giá cho " + displayAccount(reviewee), "Đánh giá", reviewee);
                    })
                    .orElseGet(() -> response.attachEntityInfo("Đánh giá", "Đánh giá", null));
            case "system_settings" -> response.attachEntityInfo("Cài đặt hệ thống: " + log.getEntityId(), "Cài đặt hệ thống", null);
            case "acceptance_criteria" -> acceptanceCriteriaRepository.findById(id)
                    .map(criteria -> response.attachEntityInfo("Tiêu chí nghiệm thu: " + criteria.getDescription(), "Tiêu chí nghiệm thu", null))
                    .orElseGet(() -> response.attachEntityInfo("Tiêu chí nghiệm thu", "Tiêu chí nghiệm thu", null));
            case "contract_deposits" -> contractDepositRepository.findById(Long.valueOf(id))
                    .map(deposit -> contractRepository.findById(deposit.getContractId())
                            .map(contract -> response.attachEntityInfo("Ký quỹ cho " + lowerFirst(contractDisplay(contract)), "Ký quỹ hợp đồng", businessOwnerByBusinessId(contract.getBusinessId())))
                            .orElseGet(() -> response.attachEntityInfo("Ký quỹ hợp đồng", "Ký quỹ hợp đồng", null)))
                    .orElseGet(() -> response.attachEntityInfo("Ký quỹ hợp đồng", "Ký quỹ hợp đồng", null));
            case "membership_purchases" -> membershipPurchaseRepository.findById(Long.valueOf(id))
                    .map(purchase -> response.attachEntityInfo(membershipPurchaseDisplay(purchase), "Gói thành viên", accountById(purchase.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Gói thành viên", "Gói thành viên", null));
            case "quota_usage_logs" -> quotaUsageLogRepository.findById(Long.valueOf(id))
                    .map(usage -> response.attachEntityInfo("Lượt sử dụng quota của " + displayAccount(accountById(usage.getAccountId())), "Lượt sử dụng quota", accountById(usage.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Lượt sử dụng quota của " + displayAccount(actor), "Lượt sử dụng quota", actor));
            case "wallet_transactions" -> walletTransactionRepository.findById(Long.valueOf(id))
                    .map(transaction -> response.attachEntityInfo("Giao dịch ví của " + displayAccount(accountById(transaction.getAccountId())), "Giao dịch ví", accountById(transaction.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Giao dịch ví", "Giao dịch ví", null));
            case "withdrawal_requests" -> withdrawalRequestRepository.findById(Long.valueOf(id))
                    .map(withdrawal -> response.attachEntityInfo("Yêu cầu rút tiền của " + displayAccount(accountById(withdrawal.getAccountId())), "Yêu cầu rút tiền", accountById(withdrawal.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Yêu cầu rút tiền", "Yêu cầu rút tiền", null));
            case "notifications" -> notificationRepository.findById(id)
                    .map(notification -> response.attachEntityInfo("Thông báo của " + displayAccount(accountById(notification.getReceiverAccountId())), "Thông báo", accountById(notification.getReceiverAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Thông báo của " + displayAccount(actor), "Thông báo", actor));
            case "technologies" -> technologyRepository.findById(id)
                    .map(technology -> response.attachEntityInfo("Công nghệ: " + technology.getTechnologyName(), "Công nghệ", null))
                    .orElseGet(() -> response.attachEntityInfo("Công nghệ", "Công nghệ", null));
            case "profiles" -> response.attachEntityInfo("Hồ sơ người dùng", "Hồ sơ người dùng", actor);
            default -> {
                String label = cleanEntityFallback(log.getEntityName());
                yield response.attachEntityInfo(label, label, null);
            }
        };
    }

    private AuditLogResponse attachPathEntityInfo(AuditLogResponse response, AuditLogEntity log, AccountEntity actor) {
        String uri = extractUri(log.getAction());
        if (uri == null && log.getEntityName() != null && log.getEntityName().startsWith("/api/")) {
            uri = log.getEntityName();
        }
        if (uri == null) return null;

        if (uri.matches("^/api/jobs/\\d+/expert-recommendations/\\d+/select$")) {
            Integer jobId = firstNumber(uri);
            Integer expertId = secondNumber(uri);
            String expertName = expertName(expertId);
            String jobTitle = jobTitle(jobId);
            return response.attachEntityInfo(expertName + " được chọn cho job " + jobTitle, "Chuyên gia được chọn", businessOwnerByJobId(jobId));
        }
        if (uri.matches("^/api/jobs/\\d+/expert-recommendations$")) {
            Integer jobId = firstNumber(uri);
            return response.attachEntityInfo("Danh sách chuyên gia gợi ý cho job " + jobTitle(jobId), "Danh sách chuyên gia gợi ý", businessOwnerByJobId(jobId));
        }
        if (uri.matches("^/api/payments/payos/\\d+/sync$")) {
            Long orderCode = firstLong(uri);
            return paymentOrderRepository.findByProviderOrderCode(orderCode)
                    .map(order -> response.attachEntityInfo(paymentOrderDisplay(order), "Thanh toán", accountByLongId(order.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Thanh toán của " + displayAccount(actor), "Thanh toán", actor));
        }
        if ("/api/payments/payos/create".equals(uri)) {
            return response.attachEntityInfo("Thanh toán của " + displayAccount(actor), "Thanh toán", actor);
        }
        if ("/api/jobs/generate-sow".equals(uri)) {
            return response.attachEntityInfo("Bản mô tả công việc AI của " + displayAccount(actor), "Bản mô tả công việc AI", actor);
        }
        if ("/api/chatbot/ask".equals(uri)) {
            return response.attachEntityInfo("Cuộc trò chuyện chatbot của " + displayAccount(actor), "Cuộc trò chuyện chatbot", actor);
        }
        if ("/api/v1/profiles/business/license-file".equals(uri)) {
            return businessProfileRepository.findByAccountId(actor == null ? null : actor.getAccountId())
                    .map(profile -> response.attachEntityInfo("Hồ sơ doanh nghiệp của " + profile.getCompanyName(), "Hồ sơ doanh nghiệp", actor))
                    .orElseGet(() -> response.attachEntityInfo("Hồ sơ doanh nghiệp của " + displayAccount(actor), "Hồ sơ doanh nghiệp", actor));
        }
        if ("/api/v1/proposals/file".equals(uri)) {
            return response.attachEntityInfo("Proposal của " + displayAccount(actor), "Proposal", actor);
        }
        return null;
    }

    private String normalizeAction(String action) {
        String translated = translateLegacyAction(action);
        if (translated == null || translated.isBlank()) throw new AppException("AUDIT ACTION KHONG HOP LE");
        String normalized = translated.trim();
        return normalized.length() <= 100 ? normalized : normalized.substring(0, 100);
    }

    private String translateLegacyAction(String action) {
        if (action == null) return null;
        String trimmed = action.trim();
        String uri = extractUri(trimmed);
        if (uri != null) {
            String mapped = translateFallbackUriAction(uri);
            if (mapped != null) return mapped;
        }
        return switch (trimmed) {
            case "APPROVE_BUSINESS_PROFILE" -> ACTION_APPROVE_BUSINESS_PROFILE;
            case "REJECT_BUSINESS_PROFILE" -> ACTION_REJECT_BUSINESS_PROFILE;
            case "APPROVE_EXPERT_PROFILE" -> ACTION_APPROVE_EXPERT_PROFILE;
            case "REJECT_EXPERT_PROFILE" -> ACTION_REJECT_EXPERT_PROFILE;
            case "ASSIGN_DISPUTE" -> ACTION_ASSIGN_DISPUTE;
            case "Mua goi thanh vien" -> "Mua gói thành viên";
            case "Mua credit" -> "Mua lượt sử dụng";
            case "Su dung quota" -> "Sử dụng quota";
            case "Duyet yeu cau rut tien" -> "Duyệt yêu cầu rút tiền";
            case "Tao yeu cau rut tien" -> "Tạo yêu cầu rút tiền";
            case "Tra tien ky quy hop dong" -> "Trả tiền ký quỹ hợp đồng";
            case "Tu choi yeu cau rut tien" -> "Từ chối yêu cầu rút tiền";
            case "Xu ly hoan ky quy hop dong" -> "Xử lý hoàn ký quỹ hợp đồng";
            default -> trimmed;
        };
    }

    private String translateFallbackUriAction(String uri) {
        if ("/api/payments/payos/create".equals(uri)) return "Tạo yêu cầu thanh toán";
        if (uri.matches("^/api/payments/payos/\\d+/sync$")) return "Đồng bộ trạng thái thanh toán";
        if ("/api/jobs/generate-sow".equals(uri)) return "Tạo bản mô tả công việc bằng AI";
        if ("/api/chatbot/ask".equals(uri)) return "Gửi câu hỏi đến chatbot";
        if (uri.matches("^/api/jobs/\\d+/expert-recommendations$")) return "Tạo danh sách chuyên gia được AI gợi ý";
        if (uri.matches("^/api/jobs/\\d+/expert-recommendations/\\d+/select$")) return "Chọn chuyên gia được AI gợi ý";
        if (uri.matches("^/api/v1/notifications/\\d+/read$")) return "Đánh dấu thông báo đã đọc";
        if ("/api/v1/notifications/read-all".equals(uri)) return "Đánh dấu tất cả thông báo đã đọc";
        if (uri.matches("^/api/v1/jobs/\\d+/skills$")) return "Cập nhật kỹ năng cho job";
        if (uri.matches("^/api/v1/jobs/\\d+/domains$")) return "Cập nhật lĩnh vực cho job";
        if (uri.matches("^/api/v1/jobs/\\d+/technologies$")) return "Cập nhật công nghệ cho job";
        if ("/api/v1/profiles/business/license-file".equals(uri)) return ACTION_UPLOAD_BUSINESS_LICENSE;
        if ("/api/v1/proposals/file".equals(uri)) return "Tải tệp proposal";
        return null;
    }

    private String normalizeActorGroup(String actorGroup) {
        if (actorGroup == null || actorGroup.isBlank()) return null;
        String normalized = actorGroup.trim().toUpperCase();
        if (GROUP_INTERNAL.equals(normalized) || GROUP_EXTERNAL.equals(normalized)) return normalized;
        throw new AppException("NHOM AUDIT LOG KHONG HOP LE");
    }

    private String resolveActorGroup(AccountEntity actor) {
        String roleName = actor == null || actor.getRole() == null ? null : actor.getRole().getRoleName();
        if (roleName != null && (roleName.equalsIgnoreCase("ADMIN") || roleName.equalsIgnoreCase("STAFF"))) {
            return GROUP_INTERNAL;
        }
        return GROUP_EXTERNAL;
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private AccountEntity accountById(Integer accountId) {
        return accountId == null ? null : accountRepository.findById(accountId).orElse(null);
    }

    private AccountEntity accountByLongId(Long accountId) {
        return accountId == null ? null : accountById(accountId.intValue());
    }

    private AccountEntity businessOwnerByBusinessId(Integer businessId) {
        return businessId == null ? null : businessProfileRepository.findById(businessId)
                .map(BusinessProfileEntity::getAccountId)
                .map(this::accountById)
                .orElse(null);
    }

    private AccountEntity businessOwnerByJobId(Integer jobId) {
        return jobId == null ? null : jobRepository.findById(jobId)
                .map(JobEntity::getBusinessId)
                .map(this::businessOwnerByBusinessId)
                .orElse(null);
    }

    private AccountEntity expertOwnerByExpertId(Integer expertId) {
        return expertId == null ? null : expertProfileRepository.findById(expertId)
                .map(ExpertProfileEntity::getAccountId)
                .map(this::accountById)
                .orElse(null);
    }

    private AccountEntity expertOwnerByPortfolioId(Integer portfolioId) {
        return portfolioId == null ? null : portfolioRepository.findById(portfolioId)
                .map(PortfolioEntity::getExpertId)
                .map(this::expertOwnerByExpertId)
                .orElse(null);
    }

    private AccountEntity milestoneOwner(MilestoneEntity milestone) {
        return milestone == null ? null : jobRepository.findById(milestone.getJobId())
                .map(JobEntity::getBusinessId)
                .map(this::businessOwnerByBusinessId)
                .orElse(null);
    }

    private String proposalDisplay(ProposalEntity proposal) {
        String expert = expertName(proposal.getExpertId());
        String job = jobTitle(proposal.getJobId());
        return "Proposal của " + expert + " cho job " + job;
    }

    private String contractDisplay(ContractEntity contract) {
        return "Hợp đồng giữa " + businessName(contract.getBusinessId()) + " và " + expertName(contract.getExpertId());
    }

    private String transactionDisplay(TransactionEntity transaction) {
        return milestoneRepository.findById(transaction.getMilestoneId())
                .map(milestone -> "Giao dịch của cột mốc " + milestone.getMilestoneName())
                .orElse("Giao dịch");
    }

    private String disputeDisplay(DisputeEntity dispute) {
        return contractRepository.findById(dispute.getContractId())
                .map(contract -> "Tranh chấp của " + lowerFirst(contractDisplay(contract)))
                .orElse("Tranh chấp");
    }

    private String membershipPurchaseDisplay(MembershipPurchaseEntity purchase) {
        String packageName = membershipPackageRepository.findById(purchase.getPackageId())
                .map(MembershipPackageEntity::getPackageName)
                .orElse("thành viên");
        return "Gói " + packageName + " của " + displayAccount(accountById(purchase.getAccountId()));
    }

    private String paymentOrderDisplay(PaymentOrderEntity order) {
        AccountEntity owner = accountByLongId(order.getAccountId());
        if (order.getJobId() != null) {
            return "Thanh toán cho job " + jobTitle(order.getJobId().intValue());
        }
        return "Thanh toán của " + displayAccount(owner);
    }

    private String businessName(Integer businessId) {
        if (businessId == null) return "doanh nghiệp";
        return businessProfileRepository.findById(businessId)
                .map(BusinessProfileEntity::getCompanyName)
                .orElse("doanh nghiệp");
    }

    private String expertName(Integer expertId) {
        AccountEntity expert = expertOwnerByExpertId(expertId);
        return expert == null ? "chuyên gia" : displayAccount(expert);
    }

    private String jobTitle(Integer jobId) {
        if (jobId == null) return "không còn tồn tại";
        return jobRepository.findById(jobId)
                .map(JobEntity::getTitle)
                .orElse("không còn tồn tại");
    }

    private String displayAccount(AccountEntity account) {
        if (account == null) return "người dùng";
        if (account.getFullName() != null && !account.getFullName().isBlank()) return account.getFullName();
        return account.getEmail() == null || account.getEmail().isBlank() ? "người dùng" : account.getEmail();
    }

    private String cleanEntityFallback(String entityName) {
        if (entityName == null || entityName.isBlank()) return "Đối tượng audit";
        return switch (entityName) {
            case "membership_purchases" -> "Gói thành viên";
            case "quota_usage_logs" -> "Lượt sử dụng quota";
            case "wallet_transactions" -> "Giao dịch ví";
            case "withdrawal_requests" -> "Yêu cầu rút tiền";
            case "contract_deposits" -> "Ký quỹ hợp đồng";
            case "notifications" -> "Thông báo";
            case "profiles" -> "Hồ sơ người dùng";
            default -> entityName.startsWith("/api/") ? "Thao tác hệ thống" : entityName.replace('_', ' ');
        };
    }

    private String lowerFirst(String value) {
        if (value == null || value.isBlank()) return value;
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    private String extractUri(String action) {
        if (action == null) return null;
        int index = action.indexOf(": ");
        if (index < 0) return null;
        String uri = action.substring(index + 2).trim();
        return uri.startsWith("/api/") ? uri : null;
    }

    private Integer firstNumber(String value) {
        Long number = firstLong(value);
        return number == null ? null : number.intValue();
    }

    private Integer secondNumber(String value) {
        String[] parts = value.split("/");
        boolean seenFirst = false;
        for (String part : parts) {
            if (part.matches("\\d+")) {
                if (seenFirst) return Integer.parseInt(part);
                seenFirst = true;
            }
        }
        return null;
    }

    private Long firstLong(String value) {
        for (String part : value.split("/")) {
            if (part.matches("\\d+")) return Long.parseLong(part);
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
