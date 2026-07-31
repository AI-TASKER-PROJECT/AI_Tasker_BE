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
    public static final String ACTION_CREATE_STAFF_PROFILE = "Tạo hồ sơ nhân viên";
    public static final String ACTION_UPDATE_STAFF_PROFILE = "Cập nhật hồ sơ nhân viên";
    public static final String ACTION_UPDATE_SYSTEM_SETTING = "Cập nhật cài đặt hệ thống";
    public static final String ACTION_UPSERT_BUSINESS_PROFILE = "Cập nhật hồ sơ doanh nghiệp";
    public static final String ACTION_UPSERT_EXPERT_PROFILE = "Cập nhật hồ sơ chuyên gia";
    public static final String ACTION_UPLOAD_BUSINESS_LICENSE = "Tải giấy phép kinh doanh";
    public static final String ACTION_UPLOAD_EXPERT_CERTIFICATE = "Tải chứng chỉ chuyên gia";
    public static final String ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE = "Tải tệp hồ sơ năng lực chuyên gia";
    public static final String ACTION_UPSERT_PORTFOLIO = "Cập nhật hồ sơ năng lực chuyên gia";
    public static final String ACTION_CREATE_JOB_DRAFT = "Tạo dự án nháp";
    public static final String ACTION_UPDATE_JOB_DRAFT = "Cập nhật dự án nháp";
    public static final String ACTION_UPDATE_JOB_OPEN = "Cập nhật dự án đã đăng";
    public static final String ACTION_CHANGE_JOB_STATUS = "Đổi trạng thái dự án";
    public static final String ACTION_SUBMIT_PROPOSAL = "Gửi bản đề xuất";
    public static final String ACTION_UPDATE_PROPOSAL = "Cập nhật bản đề xuất";
    public static final String ACTION_REVIEW_PROPOSAL = "Duyệt bản đề xuất";
    public static final String ACTION_CREATE_CONTRACT_DRAFT = "Tạo hợp đồng nháp";
    public static final String ACTION_REQUEST_CONTRACT_CHANGE = "Yêu cầu chỉnh sửa hợp đồng";
    public static final String ACTION_ACCEPT_CONTRACT_CHANGE = "Chấp nhận yêu cầu chỉnh sửa hợp đồng";
    public static final String ACTION_REJECT_CONTRACT_CHANGE = "Từ chối yêu cầu chỉnh sửa hợp đồng";
    public static final String ACTION_ACCEPT_CONTRACT = "Xác nhận hợp đồng";
    public static final String ACTION_SIGN_NDA = "Ký thỏa thuận bảo mật (NDA)";
    public static final String ACTION_REJECT_CONTRACT = "Từ chối hợp đồng";
    public static final String ACTION_ACTIVATE_CONTRACT = "Kích hoạt hợp đồng";
    public static final String ACTION_COMPLETE_CONTRACT = "Hoàn tất hợp đồng";
    public static final String ACTION_TERMINATE_CONTRACT = "Chấm dứt hợp đồng";
    public static final String ACTION_CREATE_MILESTONE = "Tạo cột mốc";
    public static final String ACTION_UPDATE_MILESTONE = "Cập nhật cột mốc";
    public static final String ACTION_COMPLETE_MILESTONE = "Hoàn tất cột mốc";
    public static final String ACTION_CREATE_ACCEPTANCE_CRITERIA = "Tạo tiêu chí nghiệm thu";
    public static final String ACTION_UPDATE_ACCEPTANCE_CRITERIA = "Cập nhật tiêu chí nghiệm thu";
    public static final String ACTION_DELETE_ACCEPTANCE_CRITERIA = "Xóa tiêu chí nghiệm thu";
    public static final String ACTION_SUBMIT_DELIVERABLE = "Nộp sản phẩm bàn giao";
    public static final String ACTION_SUBMIT_PROGRESS_REPORT = "Nộp báo cáo tiến độ";
    public static final String ACTION_UPLOAD_MILESTONE_SOURCE_CODE = "Tải tệp mã nguồn cột mốc";
    public static final String ACTION_UPLOAD_MILESTONE_USER_GUIDE = "Tải tệp hướng dẫn sử dụng sản phẩm";
    public static final String ACTION_CREATE_TRANSACTION = "Tạo giao dịch";
    public static final String ACTION_UPDATE_TRANSACTION_STATUS = "Cập nhật trạng thái giao dịch";
    public static final String ACTION_CREATE_DISPUTE = "Tạo tranh chấp";
    public static final String ACTION_ASSIGN_DISPUTE = "Phân công tranh chấp";
    public static final String ACTION_RESOLVE_DISPUTE = "Xử lý tranh chấp";
    public static final String ACTION_RECORD_DEMO_TESTING = "Ghi nhận kiểm thử bản chạy thử";
    public static final String ACTION_ISSUE_TECHNICAL_REPORT = "Gửi báo cáo kỹ thuật";
    public static final String ACTION_RUN_SLA_AUTO_APPROVE = "Chạy tự động duyệt theo thời hạn";
    public static final String ACTION_PROCESS_PAYMENT_WEBHOOK = "Xử lý thông báo thanh toán";
    public static final String ACTION_CREATE_REVIEW = "Gửi đánh giá";
    // Note: Cac action tieng Viet cho luong milestone escrow / dispute / termination v2 (Flow 4 & 5).
    public static final String ACTION_DEPOSIT_MILESTONE_ESCROW = "Ký quỹ cột mốc";
    public static final String ACTION_START_MILESTONE = "Bắt đầu cột mốc";
    public static final String ACTION_APPROVE_MILESTONE = "Duyệt cột mốc";
    public static final String ACTION_REJECT_MILESTONE = "Từ chối sản phẩm cột mốc";
    public static final String ACTION_ESCALATE_DISPUTE = "Yêu cầu can thiệp tranh chấp";
    public static final String ACTION_STAFF_DECIDE_DISPUTE = "Ra quyết định tranh chấp";
    public static final String ACTION_EXECUTE_DISPUTE_SETTLEMENT = "Thực thi quyết toán tranh chấp";
    public static final String ACTION_CANCEL_DISPUTE = "Hủy tranh chấp";
    public static final String ACTION_REQUEST_TERMINATION = "Yêu cầu chấm dứt hợp đồng";
    public static final String ACTION_ASSIGN_TERMINATION_STAFF = "Phân công nhân viên xử lý chấm dứt";
    public static final String ACTION_REJECT_TERMINATION = "Từ chối chấm dứt hợp đồng";
    public static final String ACTION_APPROVE_TERMINATION = "Duyệt chấm dứt hợp đồng";
    public static final String ACTION_SUBMIT_PARTIAL_EVIDENCE = "Nộp bằng chứng công việc khi chấm dứt";
    public static final String ACTION_EXECUTE_TERMINATION_SETTLEMENT = "Thực thi quyết toán chấm dứt";
    public static final String ACTION_CANCEL_TERMINATION = "Hủy yêu cầu chấm dứt";
    public static final String ACTION_REFUND_TERMINATION_DEPOSIT = "Hoàn ký quỹ sau chấm dứt";
    public static final String ACTION_CREATE_CASE_ATTACHMENT = "Thêm tệp đính kèm hồ sơ";

    public static final String ACTION_WALLET_TOPUP_SUCCEEDED = "Nạp tiền vào ví thành công";
    public static final String ACTION_WALLET_TOPUP_FAILED = "Nạp tiền vào ví thất bại";
    public static final String ACTION_BUSINESS_CONTRACT_DEPOSIT_HELD = "Doanh nghiệp ký quỹ hợp đồng";
    public static final String ACTION_EXPERT_CONTRACT_DEPOSIT_HELD = "Chuyên gia ký quỹ hợp đồng";
    public static final String ACTION_REQUEST_PROGRESS_REPORT = "Yêu cầu báo cáo tiến độ";
    public static final String ACTION_ACKNOWLEDGE_PROGRESS_REPORT = "Xác nhận báo cáo tiến độ";
    public static final String ACTION_EXPIRE_PROGRESS_REPORT_REQUEST = "Yêu cầu báo cáo tiến độ hết hạn";
    public static final String ACTION_MARK_MILESTONE_OVERDUE = "Đánh dấu cột mốc quá hạn";
    public static final String ACTION_AUTO_APPROVE_MILESTONE_REVIEW_SLA =
            "Hệ thống tự động duyệt và giải ngân cột mốc khi hết hạn nghiệm thu";
    public static final String ACTION_AUTO_ASSIGN_DISPUTE = "Tự động phân công tranh chấp";
    public static final String ACTION_ESCALATE_DISPUTE_SLA = "Chuyển cấp tranh chấp quá hạn xử lý";
    public static final String ACTION_ACCEPT_TERMINATION_BY_EXPERT = "Chuyên gia chấp nhận chấm dứt";
    public static final String ACTION_DISPUTE_TERMINATION_BY_EXPERT = "Chuyên gia tranh chấp yêu cầu chấm dứt";
    public static final String ACTION_EXPIRE_TERMINATION_RESPONSE = "Hết hạn phản hồi yêu cầu chấm dứt";
    public static final String ACTION_SETTLE_IMMEDIATE_TERMINATION_PENALTY = "Quyết toán bồi thường chấm dứt ngay";
    public static final String ACTION_TERMINATE_AFTER_REJECTED_MILESTONE_CHANGES = "Hủy hợp đồng do yêu cầu thay đổi mốc bị từ chối";
    public static final String ACTION_REFUND_CONTRACT_DEPOSIT = "Hoàn ký quỹ hợp đồng";
    public static final String ACTION_CLOSE_CONTRACT = "Đóng hợp đồng";
    public static final String ACTION_RECORD_PROGRESS_REPORT_FEEDBACK = "Ghi nhận phản hồi báo cáo tiến độ";
    public static final String ACTION_PURCHASE_MEMBERSHIP = "Mua gói thành viên";
    public static final String ACTION_PURCHASE_CREDIT = "Mua lượt sử dụng";
    public static final String ACTION_CONSUME_QUOTA = "Sử dụng lượt đăng";
    public static final String ACTION_APPROVE_WITHDRAWAL = "Duyệt yêu cầu rút tiền";
    public static final String ACTION_CREATE_WITHDRAWAL = "Tạo yêu cầu rút tiền";
    public static final String ACTION_PAY_CONTRACT_DEPOSIT = "Trả tiền ký quỹ hợp đồng";
    public static final String ACTION_REJECT_WITHDRAWAL = "Từ chối yêu cầu rút tiền";
    public static final String ACTION_PROCESS_CONTRACT_DEPOSIT_REFUND = "Xử lý hoàn ký quỹ hợp đồng";
    public static final String ACTION_CREATE_MEMBERSHIP_PACKAGE = "Tạo gói thành viên";
    public static final String ACTION_UPDATE_MEMBERSHIP_PACKAGE = "Cập nhật gói thành viên";
    public static final String ACTION_DELETE_MEMBERSHIP_PACKAGE = "Xóa gói thành viên";
    public static final String ACTION_CANCEL_CONTRACT_DRAFT_BY_BUSINESS = "Doanh nghiệp hủy hợp đồng nháp";
    public static final String ACTION_CREATE_PAYOS_PAYMENT_REQUEST = "Tạo yêu cầu thanh toán";
    public static final String ACTION_SYNC_PAYOS_PAYMENT_STATUS = "Đồng bộ trạng thái thanh toán";
    public static final String ACTION_GENERATE_AI_SOW = "Tạo bản mô tả công việc bằng trí tuệ nhân tạo";
    public static final String ACTION_ASK_CHATBOT = "Gửi câu hỏi đến chatbot";
    public static final String ACTION_CREATE_AI_EXPERT_RECOMMENDATIONS = "Tạo danh sách chuyên gia được AI gợi ý";
    public static final String ACTION_SELECT_AI_EXPERT_RECOMMENDATION = "Chọn chuyên gia được AI gợi ý";
    public static final String ACTION_MARK_NOTIFICATION_READ = "Đánh dấu thông báo đã đọc";
    public static final String ACTION_MARK_ALL_NOTIFICATIONS_READ = "Đánh dấu tất cả thông báo đã đọc";
    public static final String ACTION_UPDATE_JOB_SKILLS = "Cập nhật kỹ năng cho dự án";
    public static final String ACTION_UPDATE_JOB_DOMAINS = "Cập nhật lĩnh vực cho dự án";
    public static final String ACTION_UPDATE_JOB_TECHNOLOGIES = "Cập nhật công nghệ cho dự án";
    public static final String ACTION_UPLOAD_PROPOSAL_FILE = "Tải tệp bản đề xuất";

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSystem(String action, String entityName, String entityId) {
        record(action, entityName, entityId, null);
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
        AccountEntity actor = accountById(log.getActorAccountId());
        boolean systemActor = log.getActorAccountId() == null;
        String actorGroup = systemActor ? GROUP_INTERNAL : resolveActorGroup(actor);
        AuditLogResponse response = AuditLogResponse.from(
                log, actor, actorGroup, translateLegacyAction(log.getAction()));
        if (systemActor) response.markAsSystemActor();
        return attachEntityInfo(response, log, actor)
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
                yield response.attachEntityInfo("Hồ sơ năng lực của " + displayAccount(owner), "Hồ sơ năng lực", owner);
            }
            case "jobs" -> jobRepository.findById(id)
                    .map(job -> response.attachEntityInfo("Dự án: " + job.getTitle(), "Dự án", businessOwnerByBusinessId(job.getBusinessId())))
                    .orElseGet(() -> response.attachEntityInfo("Dự án", "Dự án", null));
            case "proposals" -> proposalRepository.findById(id)
                    .map(proposal -> response.attachEntityInfo(proposalDisplay(proposal), "Bản đề xuất", expertOwnerByExpertId(proposal.getExpertId())))
                    .orElseGet(() -> response.attachEntityInfo("Bản đề xuất", "Bản đề xuất", null));
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
                    .map(dispute -> response.attachEntityInfo(disputeDisplay(dispute), "Tranh chấp", disputeOwner(dispute)))
                    .orElseGet(() -> response.attachEntityInfo("Tranh chấp", "Tranh chấp", null));
            case "payment_order" -> paymentOrderRepository.findById(Long.valueOf(id))
                    .map(order -> response.attachEntityInfo(paymentOrderDisplay(order), "Thanh toán", accountByLongId(order.getAccountId())))
                    .orElseGet(() -> response.attachEntityInfo("Thanh toán", "Thanh toán", null));
            case "staffs" -> staffRepository.findById(id)
                    .map(staff -> {
                        AccountEntity owner = accountById(staff.getAccountId());
                        return response.attachEntityInfo("Hồ sơ nhân viên của " + displayAccount(owner), "Hồ sơ nhân viên", owner);
                    })
                    .orElseGet(() -> response.attachEntityInfo("Hồ sơ nhân viên", "Hồ sơ nhân viên", null));
            case "reviews" -> reviewRepository.findById(id)
                    .map(review -> {
                        AccountEntity reviewee = accountById(review.getRevieweeId());
                        return response.attachEntityInfo("Đánh giá cho " + displayAccount(reviewee), "Đánh giá", reviewee);
                    })
                    .orElseGet(() -> response.attachEntityInfo("Đánh giá", "Đánh giá", null));
            case "system_settings" -> response.attachEntityInfo("Cài đặt hệ thống: " + log.getEntityId(), "Cài đặt hệ thống", null);
            case "milestone_progress_reports" -> response.attachEntityInfo("Báo cáo tiến độ cột mốc", "Báo cáo tiến độ", null);
            case "milestone_progress_report_requests" -> response.attachEntityInfo("Yêu cầu báo cáo tiến độ", "Yêu cầu báo cáo", null);
            case "case_attachments" -> response.attachEntityInfo("Tệp đính kèm hồ sơ", "Tệp đính kèm", null);
            case "membership_packages" -> membershipPackageRepository.findById(Long.valueOf(id))
                    .map(pkg -> response.attachEntityInfo("Gói thành viên: " + pkg.getPackageName(), "Gói thành viên", null))
                    .orElseGet(() -> response.attachEntityInfo("Gói thành viên", "Gói thành viên", null));
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
            return response.attachEntityInfo(expertName + " được chọn cho dự án " + jobTitle, "Chuyên gia được chọn", businessOwnerByJobId(jobId));
        }
        if (uri.matches("^/api/jobs/\\d+/expert-recommendations$")) {
            Integer jobId = firstNumber(uri);
            return response.attachEntityInfo("Danh sách chuyên gia gợi ý cho dự án " + jobTitle(jobId), "Danh sách chuyên gia gợi ý", businessOwnerByJobId(jobId));
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
            return response.attachEntityInfo("Bản đề xuất của " + displayAccount(actor), "Bản đề xuất", actor);
        }
        return null;
    }

    private String normalizeAction(String action) {
        String normalized = normalizeRawAction(action);
        if (normalized == null || normalized.isBlank()) throw new AppException("Thao tác nhật ký kiểm toán không hợp lệ");
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
        String rawAction = normalizeRawAction(trimmed);
        return switch (rawAction == null ? trimmed : rawAction) {
            case "APPROVE_BUSINESS_PROFILE" -> ACTION_APPROVE_BUSINESS_PROFILE;
            case "REJECT_BUSINESS_PROFILE" -> ACTION_REJECT_BUSINESS_PROFILE;
            case "APPROVE_EXPERT_PROFILE" -> ACTION_APPROVE_EXPERT_PROFILE;
            case "REJECT_EXPERT_PROFILE" -> ACTION_REJECT_EXPERT_PROFILE;
            case "CREATE_ACCOUNT" -> ACTION_CREATE_ACCOUNT;
            case "UPDATE_ACCOUNT" -> ACTION_UPDATE_ACCOUNT;
            case "CHANGE_ACCOUNT_STATUS" -> ACTION_CHANGE_ACCOUNT_STATUS;
            case "CREATE_STAFF_PROFILE" -> ACTION_CREATE_STAFF_PROFILE;
            case "UPDATE_STAFF_PROFILE" -> ACTION_UPDATE_STAFF_PROFILE;
            case "UPDATE_SYSTEM_SETTING" -> ACTION_UPDATE_SYSTEM_SETTING;
            case "UPSERT_BUSINESS_PROFILE" -> ACTION_UPSERT_BUSINESS_PROFILE;
            case "UPSERT_EXPERT_PROFILE" -> ACTION_UPSERT_EXPERT_PROFILE;
            case "UPLOAD_BUSINESS_LICENSE" -> ACTION_UPLOAD_BUSINESS_LICENSE;
            case "UPLOAD_EXPERT_CERTIFICATE" -> ACTION_UPLOAD_EXPERT_CERTIFICATE;
            case "UPLOAD_EXPERT_PORTFOLIO_FILE" -> ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE;
            case "UPSERT_PORTFOLIO" -> ACTION_UPSERT_PORTFOLIO;
            case "CREATE_JOB_DRAFT" -> ACTION_CREATE_JOB_DRAFT;
            case "UPDATE_JOB_DRAFT" -> ACTION_UPDATE_JOB_DRAFT;
            case "UPDATE_JOB_OPEN" -> ACTION_UPDATE_JOB_OPEN;
            case "CHANGE_JOB_STATUS" -> ACTION_CHANGE_JOB_STATUS;
            case "SUBMIT_PROPOSAL", "Gửi proposal" -> ACTION_SUBMIT_PROPOSAL;
            case "UPDATE_PROPOSAL" -> ACTION_UPDATE_PROPOSAL;
            case "REVIEW_PROPOSAL" -> ACTION_REVIEW_PROPOSAL;
            case "CREATE_CONTRACT_DRAFT" -> ACTION_CREATE_CONTRACT_DRAFT;
            case "REQUEST_CONTRACT_CHANGE" -> ACTION_REQUEST_CONTRACT_CHANGE;
            case "ACCEPT_CONTRACT_CHANGE" -> ACTION_ACCEPT_CONTRACT_CHANGE;
            case "REJECT_CONTRACT_CHANGE" -> ACTION_REJECT_CONTRACT_CHANGE;
            case "ACCEPT_CONTRACT" -> ACTION_ACCEPT_CONTRACT;
            case "SIGN_NDA" -> ACTION_SIGN_NDA;
            case "REJECT_CONTRACT" -> ACTION_REJECT_CONTRACT;
            case "ACTIVATE_CONTRACT" -> ACTION_ACTIVATE_CONTRACT;
            case "COMPLETE_CONTRACT" -> ACTION_COMPLETE_CONTRACT;
            case "TERMINATE_CONTRACT" -> ACTION_TERMINATE_CONTRACT;
            case "CREATE_MILESTONE" -> ACTION_CREATE_MILESTONE;
            case "UPDATE_MILESTONE" -> ACTION_UPDATE_MILESTONE;
            case "COMPLETE_MILESTONE" -> ACTION_COMPLETE_MILESTONE;
            case "CREATE_ACCEPTANCE_CRITERIA" -> ACTION_CREATE_ACCEPTANCE_CRITERIA;
            case "UPDATE_ACCEPTANCE_CRITERIA" -> ACTION_UPDATE_ACCEPTANCE_CRITERIA;
            case "DELETE_ACCEPTANCE_CRITERIA" -> ACTION_DELETE_ACCEPTANCE_CRITERIA;
            case "SUBMIT_DELIVERABLE" -> ACTION_SUBMIT_DELIVERABLE;
            case "SUBMIT_PROGRESS_REPORT" -> ACTION_SUBMIT_PROGRESS_REPORT;
            case "UPLOAD_MILESTONE_SOURCE_CODE" -> ACTION_UPLOAD_MILESTONE_SOURCE_CODE;
            case "UPLOAD_MILESTONE_USER_GUIDE" -> ACTION_UPLOAD_MILESTONE_USER_GUIDE;
            case "CREATE_TRANSACTION" -> ACTION_CREATE_TRANSACTION;
            case "UPDATE_TRANSACTION_STATUS" -> ACTION_UPDATE_TRANSACTION_STATUS;
            case "CREATE_DISPUTE" -> ACTION_CREATE_DISPUTE;
            case "ASSIGN_DISPUTE" -> ACTION_ASSIGN_DISPUTE;
            case "RESOLVE_DISPUTE" -> ACTION_RESOLVE_DISPUTE;
            case "RECORD_DEMO_TESTING" -> ACTION_RECORD_DEMO_TESTING;
            case "ISSUE_TECHNICAL_REPORT" -> ACTION_ISSUE_TECHNICAL_REPORT;
            case "RUN_SLA_AUTO_APPROVE" -> ACTION_RUN_SLA_AUTO_APPROVE;
            case "PROCESS_PAYMENT_WEBHOOK" -> ACTION_PROCESS_PAYMENT_WEBHOOK;
            // Milestone escrow / dispute / termination v2 (ma raw duoc chuan hoa sang tieng Viet khi hien thi).
            case "MILESTONE_ESCROW_DEPOSITED" -> ACTION_DEPOSIT_MILESTONE_ESCROW;
            case "BUSINESS_CONTRACT_DEPOSIT_HELD" -> ACTION_BUSINESS_CONTRACT_DEPOSIT_HELD;
            case "EXPERT_CONTRACT_DEPOSIT_HELD" -> ACTION_EXPERT_CONTRACT_DEPOSIT_HELD;
            case "CONTRACT_ACTIVATED_AFTER_DUAL_DEPOSIT" -> ACTION_ACTIVATE_CONTRACT;
            case "MILESTONE_STARTED" -> ACTION_START_MILESTONE;
            case "PROGRESS_REPORT_SUBMITTED" -> ACTION_SUBMIT_PROGRESS_REPORT;
            case "PROGRESS_REPORT_REQUESTED" -> ACTION_REQUEST_PROGRESS_REPORT;
            case "PROGRESS_REPORT_ACKNOWLEDGED" -> ACTION_ACKNOWLEDGE_PROGRESS_REPORT;
            case "PROGRESS_REPORT_REQUEST_EXPIRED" -> ACTION_EXPIRE_PROGRESS_REPORT_REQUEST;
            case "MILESTONE_MARKED_OVERDUE" -> ACTION_MARK_MILESTONE_OVERDUE;
            case "MILESTONE_REVIEW_SLA_AUTO_APPROVED" -> ACTION_AUTO_APPROVE_MILESTONE_REVIEW_SLA;
            case "DELIVERABLE_SUBMITTED" -> ACTION_SUBMIT_DELIVERABLE;
            case "MILESTONE_APPROVED" -> ACTION_APPROVE_MILESTONE;
            case "MILESTONE_REJECTED" -> ACTION_REJECT_MILESTONE;
            case "DISPUTE_CREATED" -> ACTION_CREATE_DISPUTE;
            case "DISPUTE_ESCALATION_REQUESTED" -> ACTION_ESCALATE_DISPUTE;
            case "DISPUTE_STAFF_ASSIGNED" -> ACTION_ASSIGN_DISPUTE;
            case "DISPUTE_STAFF_ROUTED" -> ACTION_ASSIGN_DISPUTE;
            case "DISPUTE_STAFF_AUTO_ASSIGNED" -> ACTION_AUTO_ASSIGN_DISPUTE;
            case "DISPUTE_STAFF_SLA_ESCALATED" -> ACTION_ESCALATE_DISPUTE_SLA;
            case "DISPUTE_STAFF_DECIDED" -> ACTION_STAFF_DECIDE_DISPUTE;
            case "DISPUTE_SETTLEMENT_EXECUTED" -> ACTION_EXECUTE_DISPUTE_SETTLEMENT;
            case "DISPUTE_CANCELLED" -> ACTION_CANCEL_DISPUTE;
            case "CONTRACT_COMPLETED" -> ACTION_COMPLETE_CONTRACT;
            case "TERMINATION_REQUESTED" -> ACTION_REQUEST_TERMINATION;
            case "TERMINATION_ACCEPTED_BY_EXPERT" -> ACTION_ACCEPT_TERMINATION_BY_EXPERT;
            case "TERMINATION_DISPUTED_BY_EXPERT" -> ACTION_DISPUTE_TERMINATION_BY_EXPERT;
            case "TERMINATION_RESPONSE_EXPIRED" -> ACTION_EXPIRE_TERMINATION_RESPONSE;
            case "CONTRACT_IMMEDIATE_TERMINATED" -> ACTION_TERMINATE_CONTRACT;
            case "IMMEDIATE_TERMINATION_PENALTY_SETTLED" -> ACTION_SETTLE_IMMEDIATE_TERMINATION_PENALTY;
            case "CONTRACT_TERMINATED_AFTER_REJECTED_MILESTONE_CHANGES" -> ACTION_TERMINATE_AFTER_REJECTED_MILESTONE_CHANGES;
            case "TERMINATION_STAFF_ASSIGNED" -> ACTION_ASSIGN_TERMINATION_STAFF;
            case "TERMINATION_STAFF_REJECTED" -> ACTION_REJECT_TERMINATION;
            case "TERMINATION_STAFF_APPROVED" -> ACTION_APPROVE_TERMINATION;
            case "TERMINATION_REJECTED" -> ACTION_REJECT_TERMINATION;
            case "TERMINATION_APPROVED" -> ACTION_APPROVE_TERMINATION;
            case "TERMINATION_PARTIAL_EVIDENCE_SUBMITTED" -> ACTION_SUBMIT_PARTIAL_EVIDENCE;
            case "TERMINATION_SETTLEMENT_EXECUTED" -> ACTION_EXECUTE_TERMINATION_SETTLEMENT;
            case "CONTRACT_DEPOSIT_REFUNDED" -> ACTION_REFUND_CONTRACT_DEPOSIT;
            case "PARTICIPANT_DEPOSITS_REFUNDED" -> ACTION_REFUND_TERMINATION_DEPOSIT;
            case "CONTRACT_CLOSED" -> ACTION_CLOSE_CONTRACT;
            case "REVIEW_CREATED" -> ACTION_CREATE_REVIEW;
            case "TERMINATION_CANCELLED" -> ACTION_CANCEL_TERMINATION;
            case "TERMINATION_DEPOSIT_REFUNDED" -> ACTION_REFUND_TERMINATION_DEPOSIT;
            case "CASE_ATTACHMENT_CREATED" -> ACTION_CREATE_CASE_ATTACHMENT;
            case "WALLET_TOPUP_SUCCEEDED" -> ACTION_WALLET_TOPUP_SUCCEEDED;
            case "WALLET_TOPUP_FAILED" -> ACTION_WALLET_TOPUP_FAILED;
            case "PROGRESS_REPORT_FEEDBACK_RECORDED" -> ACTION_RECORD_PROGRESS_REPORT_FEEDBACK;
            case "CREATE_PAYOS_PAYMENT_REQUEST" -> ACTION_CREATE_PAYOS_PAYMENT_REQUEST;
            case "SYNC_PAYOS_PAYMENT_STATUS" -> ACTION_SYNC_PAYOS_PAYMENT_STATUS;
            case "GENERATE_AI_SOW" -> ACTION_GENERATE_AI_SOW;
            case "ASK_CHATBOT" -> ACTION_ASK_CHATBOT;
            case "CREATE_AI_EXPERT_RECOMMENDATIONS" -> ACTION_CREATE_AI_EXPERT_RECOMMENDATIONS;
            case "SELECT_AI_EXPERT_RECOMMENDATION" -> ACTION_SELECT_AI_EXPERT_RECOMMENDATION;
            case "MARK_NOTIFICATION_READ" -> ACTION_MARK_NOTIFICATION_READ;
            case "MARK_ALL_NOTIFICATIONS_READ" -> ACTION_MARK_ALL_NOTIFICATIONS_READ;
            case "UPDATE_JOB_SKILLS" -> ACTION_UPDATE_JOB_SKILLS;
            case "UPDATE_JOB_DOMAINS" -> ACTION_UPDATE_JOB_DOMAINS;
            case "UPDATE_JOB_TECHNOLOGIES" -> ACTION_UPDATE_JOB_TECHNOLOGIES;
            case "UPLOAD_PROPOSAL_FILE" -> ACTION_UPLOAD_PROPOSAL_FILE;
            case "MEMBERSHIP_PURCHASED" -> ACTION_PURCHASE_MEMBERSHIP;
            case "CREDIT_PURCHASED" -> ACTION_PURCHASE_CREDIT;
            case "QUOTA_USED" -> ACTION_CONSUME_QUOTA;
            case "WITHDRAWAL_REQUEST_APPROVED" -> ACTION_APPROVE_WITHDRAWAL;
            case "WITHDRAWAL_REQUEST_CREATED" -> ACTION_CREATE_WITHDRAWAL;
            case "CONTRACT_DEPOSIT_PAID" -> ACTION_PAY_CONTRACT_DEPOSIT;
            case "WITHDRAWAL_REQUEST_REJECTED" -> ACTION_REJECT_WITHDRAWAL;
            case "CONTRACT_DEPOSIT_REFUND_PROCESSED" -> ACTION_PROCESS_CONTRACT_DEPOSIT_REFUND;
            case "MEMBERSHIP_PACKAGE_CREATED" -> ACTION_CREATE_MEMBERSHIP_PACKAGE;
            case "MEMBERSHIP_PACKAGE_UPDATED" -> ACTION_UPDATE_MEMBERSHIP_PACKAGE;
            case "MEMBERSHIP_PACKAGE_DELETED" -> ACTION_DELETE_MEMBERSHIP_PACKAGE;
            case "CONTRACT_DRAFT_CANCELLED_BY_BUSINESS" -> ACTION_CANCEL_CONTRACT_DRAFT_BY_BUSINESS;
            default -> trimmed;
        };
    }

    private String normalizeRawAction(String action) {
        if (action == null) return null;
        String trimmed = action.trim();
        String uri = extractUri(trimmed);
        if (uri != null) {
            String rawFallbackAction = normalizeFallbackUriAction(uri);
            if (rawFallbackAction != null) return rawFallbackAction;
        }
        String legacyVietnameseAction = normalizeLegacyVietnameseAction(trimmed);
        if (legacyVietnameseAction != null) return legacyVietnameseAction;
        return switch (trimmed) {
            case ACTION_APPROVE_BUSINESS_PROFILE -> "APPROVE_BUSINESS_PROFILE";
            case ACTION_REJECT_BUSINESS_PROFILE -> "REJECT_BUSINESS_PROFILE";
            case ACTION_APPROVE_EXPERT_PROFILE -> "APPROVE_EXPERT_PROFILE";
            case ACTION_REJECT_EXPERT_PROFILE -> "REJECT_EXPERT_PROFILE";
            case ACTION_CREATE_ACCOUNT -> "CREATE_ACCOUNT";
            case ACTION_UPDATE_ACCOUNT -> "UPDATE_ACCOUNT";
            case ACTION_CHANGE_ACCOUNT_STATUS -> "CHANGE_ACCOUNT_STATUS";
            case ACTION_CREATE_STAFF_PROFILE -> "CREATE_STAFF_PROFILE";
            case ACTION_UPDATE_STAFF_PROFILE -> "UPDATE_STAFF_PROFILE";
            case ACTION_UPDATE_SYSTEM_SETTING -> "UPDATE_SYSTEM_SETTING";
            case ACTION_UPSERT_BUSINESS_PROFILE -> "UPSERT_BUSINESS_PROFILE";
            case ACTION_UPSERT_EXPERT_PROFILE -> "UPSERT_EXPERT_PROFILE";
            case ACTION_UPLOAD_BUSINESS_LICENSE -> "UPLOAD_BUSINESS_LICENSE";
            case ACTION_UPLOAD_EXPERT_CERTIFICATE -> "UPLOAD_EXPERT_CERTIFICATE";
            case ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE -> "UPLOAD_EXPERT_PORTFOLIO_FILE";
            case ACTION_UPSERT_PORTFOLIO -> "UPSERT_PORTFOLIO";
            case ACTION_CREATE_JOB_DRAFT -> "CREATE_JOB_DRAFT";
            case ACTION_UPDATE_JOB_DRAFT -> "UPDATE_JOB_DRAFT";
            case ACTION_UPDATE_JOB_OPEN -> "UPDATE_JOB_OPEN";
            case ACTION_CHANGE_JOB_STATUS -> "CHANGE_JOB_STATUS";
            case ACTION_SUBMIT_PROPOSAL -> "SUBMIT_PROPOSAL";
            case ACTION_UPDATE_PROPOSAL -> "UPDATE_PROPOSAL";
            case ACTION_REVIEW_PROPOSAL -> "REVIEW_PROPOSAL";
            case ACTION_CREATE_CONTRACT_DRAFT -> "CREATE_CONTRACT_DRAFT";
            case ACTION_REQUEST_CONTRACT_CHANGE -> "REQUEST_CONTRACT_CHANGE";
            case ACTION_ACCEPT_CONTRACT_CHANGE -> "ACCEPT_CONTRACT_CHANGE";
            case ACTION_REJECT_CONTRACT_CHANGE -> "REJECT_CONTRACT_CHANGE";
            case ACTION_ACCEPT_CONTRACT -> "ACCEPT_CONTRACT";
            case ACTION_SIGN_NDA -> "SIGN_NDA";
            case ACTION_REJECT_CONTRACT -> "REJECT_CONTRACT";
            case ACTION_ACTIVATE_CONTRACT -> "ACTIVATE_CONTRACT";
            case ACTION_COMPLETE_CONTRACT -> "COMPLETE_CONTRACT";
            case ACTION_TERMINATE_CONTRACT -> "TERMINATE_CONTRACT";
            case ACTION_CREATE_MILESTONE -> "CREATE_MILESTONE";
            case ACTION_UPDATE_MILESTONE -> "UPDATE_MILESTONE";
            case ACTION_COMPLETE_MILESTONE -> "COMPLETE_MILESTONE";
            case ACTION_CREATE_ACCEPTANCE_CRITERIA -> "CREATE_ACCEPTANCE_CRITERIA";
            case ACTION_UPDATE_ACCEPTANCE_CRITERIA -> "UPDATE_ACCEPTANCE_CRITERIA";
            case ACTION_DELETE_ACCEPTANCE_CRITERIA -> "DELETE_ACCEPTANCE_CRITERIA";
            case ACTION_SUBMIT_DELIVERABLE -> "SUBMIT_DELIVERABLE";
            case ACTION_SUBMIT_PROGRESS_REPORT -> "SUBMIT_PROGRESS_REPORT";
            case ACTION_UPLOAD_MILESTONE_SOURCE_CODE -> "UPLOAD_MILESTONE_SOURCE_CODE";
            case ACTION_UPLOAD_MILESTONE_USER_GUIDE -> "UPLOAD_MILESTONE_USER_GUIDE";
            case ACTION_CREATE_TRANSACTION -> "CREATE_TRANSACTION";
            case ACTION_UPDATE_TRANSACTION_STATUS -> "UPDATE_TRANSACTION_STATUS";
            case ACTION_CREATE_DISPUTE -> "CREATE_DISPUTE";
            case ACTION_ASSIGN_DISPUTE -> "ASSIGN_DISPUTE";
            case ACTION_RESOLVE_DISPUTE -> "RESOLVE_DISPUTE";
            case ACTION_RECORD_DEMO_TESTING -> "RECORD_DEMO_TESTING";
            case ACTION_ISSUE_TECHNICAL_REPORT -> "ISSUE_TECHNICAL_REPORT";
            case ACTION_RUN_SLA_AUTO_APPROVE -> "RUN_SLA_AUTO_APPROVE";
            case ACTION_PROCESS_PAYMENT_WEBHOOK -> "PROCESS_PAYMENT_WEBHOOK";
            case ACTION_CREATE_REVIEW -> "REVIEW_CREATED";
            case ACTION_DEPOSIT_MILESTONE_ESCROW -> "MILESTONE_ESCROW_DEPOSITED";
            case ACTION_START_MILESTONE -> "MILESTONE_STARTED";
            case ACTION_APPROVE_MILESTONE -> "MILESTONE_APPROVED";
            case ACTION_REJECT_MILESTONE -> "MILESTONE_REJECTED";
            case ACTION_ESCALATE_DISPUTE -> "DISPUTE_ESCALATION_REQUESTED";
            case ACTION_STAFF_DECIDE_DISPUTE -> "DISPUTE_STAFF_DECIDED";
            case ACTION_EXECUTE_DISPUTE_SETTLEMENT -> "DISPUTE_SETTLEMENT_EXECUTED";
            case ACTION_CANCEL_DISPUTE -> "DISPUTE_CANCELLED";
            case ACTION_REQUEST_TERMINATION -> "TERMINATION_REQUESTED";
            case ACTION_ASSIGN_TERMINATION_STAFF -> "TERMINATION_STAFF_ASSIGNED";
            case ACTION_REJECT_TERMINATION -> "TERMINATION_STAFF_REJECTED";
            case ACTION_APPROVE_TERMINATION -> "TERMINATION_STAFF_APPROVED";
            case ACTION_SUBMIT_PARTIAL_EVIDENCE -> "TERMINATION_PARTIAL_EVIDENCE_SUBMITTED";
            case ACTION_EXECUTE_TERMINATION_SETTLEMENT -> "TERMINATION_SETTLEMENT_EXECUTED";
            case ACTION_CANCEL_TERMINATION -> "TERMINATION_CANCELLED";
            case ACTION_REFUND_TERMINATION_DEPOSIT -> "TERMINATION_DEPOSIT_REFUNDED";
            case ACTION_CREATE_CASE_ATTACHMENT -> "CASE_ATTACHMENT_CREATED";
            case ACTION_WALLET_TOPUP_SUCCEEDED -> "WALLET_TOPUP_SUCCEEDED";
            case ACTION_WALLET_TOPUP_FAILED -> "WALLET_TOPUP_FAILED";
            case ACTION_BUSINESS_CONTRACT_DEPOSIT_HELD -> "BUSINESS_CONTRACT_DEPOSIT_HELD";
            case ACTION_EXPERT_CONTRACT_DEPOSIT_HELD -> "EXPERT_CONTRACT_DEPOSIT_HELD";
            case ACTION_REQUEST_PROGRESS_REPORT -> "PROGRESS_REPORT_REQUESTED";
            case ACTION_ACKNOWLEDGE_PROGRESS_REPORT -> "PROGRESS_REPORT_ACKNOWLEDGED";
            case ACTION_EXPIRE_PROGRESS_REPORT_REQUEST -> "PROGRESS_REPORT_REQUEST_EXPIRED";
            case ACTION_MARK_MILESTONE_OVERDUE -> "MILESTONE_MARKED_OVERDUE";
            case ACTION_AUTO_APPROVE_MILESTONE_REVIEW_SLA -> "MILESTONE_REVIEW_SLA_AUTO_APPROVED";
            case ACTION_AUTO_ASSIGN_DISPUTE -> "DISPUTE_STAFF_AUTO_ASSIGNED";
            case ACTION_ESCALATE_DISPUTE_SLA -> "DISPUTE_STAFF_SLA_ESCALATED";
            case ACTION_ACCEPT_TERMINATION_BY_EXPERT -> "TERMINATION_ACCEPTED_BY_EXPERT";
            case ACTION_DISPUTE_TERMINATION_BY_EXPERT -> "TERMINATION_DISPUTED_BY_EXPERT";
            case ACTION_EXPIRE_TERMINATION_RESPONSE -> "TERMINATION_RESPONSE_EXPIRED";
            case ACTION_SETTLE_IMMEDIATE_TERMINATION_PENALTY -> "IMMEDIATE_TERMINATION_PENALTY_SETTLED";
            case ACTION_TERMINATE_AFTER_REJECTED_MILESTONE_CHANGES -> "CONTRACT_TERMINATED_AFTER_REJECTED_MILESTONE_CHANGES";
            case ACTION_REFUND_CONTRACT_DEPOSIT -> "CONTRACT_DEPOSIT_REFUNDED";
            case ACTION_CLOSE_CONTRACT -> "CONTRACT_CLOSED";
            case ACTION_RECORD_PROGRESS_REPORT_FEEDBACK -> "PROGRESS_REPORT_FEEDBACK_RECORDED";
            case ACTION_CREATE_PAYOS_PAYMENT_REQUEST -> "CREATE_PAYOS_PAYMENT_REQUEST";
            case ACTION_SYNC_PAYOS_PAYMENT_STATUS -> "SYNC_PAYOS_PAYMENT_STATUS";
            case ACTION_GENERATE_AI_SOW -> "GENERATE_AI_SOW";
            case ACTION_ASK_CHATBOT -> "ASK_CHATBOT";
            case ACTION_CREATE_AI_EXPERT_RECOMMENDATIONS -> "CREATE_AI_EXPERT_RECOMMENDATIONS";
            case ACTION_SELECT_AI_EXPERT_RECOMMENDATION -> "SELECT_AI_EXPERT_RECOMMENDATION";
            case ACTION_MARK_NOTIFICATION_READ -> "MARK_NOTIFICATION_READ";
            case ACTION_MARK_ALL_NOTIFICATIONS_READ -> "MARK_ALL_NOTIFICATIONS_READ";
            case ACTION_UPDATE_JOB_SKILLS -> "UPDATE_JOB_SKILLS";
            case ACTION_UPDATE_JOB_DOMAINS -> "UPDATE_JOB_DOMAINS";
            case ACTION_UPDATE_JOB_TECHNOLOGIES -> "UPDATE_JOB_TECHNOLOGIES";
            case ACTION_UPLOAD_PROPOSAL_FILE -> "UPLOAD_PROPOSAL_FILE";
            case ACTION_PURCHASE_MEMBERSHIP -> "MEMBERSHIP_PURCHASED";
            case ACTION_PURCHASE_CREDIT -> "CREDIT_PURCHASED";
            case ACTION_CONSUME_QUOTA -> "QUOTA_USED";
            case ACTION_APPROVE_WITHDRAWAL -> "WITHDRAWAL_REQUEST_APPROVED";
            case ACTION_CREATE_WITHDRAWAL -> "WITHDRAWAL_REQUEST_CREATED";
            case ACTION_PAY_CONTRACT_DEPOSIT -> "CONTRACT_DEPOSIT_PAID";
            case ACTION_REJECT_WITHDRAWAL -> "WITHDRAWAL_REQUEST_REJECTED";
            case ACTION_PROCESS_CONTRACT_DEPOSIT_REFUND -> "CONTRACT_DEPOSIT_REFUND_PROCESSED";
            case ACTION_CREATE_MEMBERSHIP_PACKAGE -> "MEMBERSHIP_PACKAGE_CREATED";
            case ACTION_UPDATE_MEMBERSHIP_PACKAGE -> "MEMBERSHIP_PACKAGE_UPDATED";
            case ACTION_DELETE_MEMBERSHIP_PACKAGE -> "MEMBERSHIP_PACKAGE_DELETED";
            case ACTION_CANCEL_CONTRACT_DRAFT_BY_BUSINESS -> "CONTRACT_DRAFT_CANCELLED_BY_BUSINESS";
            default -> trimmed;
        };
    }

    private String normalizeLegacyVietnameseAction(String action) {
        return switch (action) {
            case "Mua goi thanh vien" -> "MEMBERSHIP_PURCHASED";
            case "Mua credit" -> "CREDIT_PURCHASED";
            case "Su dung quota" -> "QUOTA_USED";
            case "Duyet yeu cau rut tien" -> "WITHDRAWAL_REQUEST_APPROVED";
            case "Tao yeu cau rut tien" -> "WITHDRAWAL_REQUEST_CREATED";
            case "Tra tien ky quy hop dong" -> "CONTRACT_DEPOSIT_PAID";
            case "Tu choi yeu cau rut tien" -> "WITHDRAWAL_REQUEST_REJECTED";
            case "Xu ly hoan ky quy hop dong" -> "CONTRACT_DEPOSIT_REFUND_PROCESSED";
            default -> null;
        };
    }

    private String translateFallbackUriAction(String uri) {
        String rawAction = normalizeFallbackUriAction(uri);
        return rawAction == null ? null : translateLegacyAction(rawAction);
    }

    private String normalizeFallbackUriAction(String uri) {
        if ("/api/payments/payos/create".equals(uri)) return "CREATE_PAYOS_PAYMENT_REQUEST";
        if (uri.matches("^/api/payments/payos/\\d+/sync$")) return "SYNC_PAYOS_PAYMENT_STATUS";
        if ("/api/jobs/generate-sow".equals(uri)) return "GENERATE_AI_SOW";
        if ("/api/chatbot/ask".equals(uri)) return "ASK_CHATBOT";
        if (uri.matches("^/api/jobs/\\d+/expert-recommendations$")) return "CREATE_AI_EXPERT_RECOMMENDATIONS";
        if (uri.matches("^/api/jobs/\\d+/expert-recommendations/\\d+/select$")) return "SELECT_AI_EXPERT_RECOMMENDATION";
        if (uri.matches("^/api/v1/notifications/\\d+/read$")) return "MARK_NOTIFICATION_READ";
        if ("/api/v1/notifications/read-all".equals(uri)) return "MARK_ALL_NOTIFICATIONS_READ";
        if (uri.matches("^/api/v1/jobs/\\d+/skills$")) return "UPDATE_JOB_SKILLS";
        if (uri.matches("^/api/v1/jobs/\\d+/domains$")) return "UPDATE_JOB_DOMAINS";
        if (uri.matches("^/api/v1/jobs/\\d+/technologies$")) return "UPDATE_JOB_TECHNOLOGIES";
        if ("/api/v1/profiles/business/license-file".equals(uri)) return "UPLOAD_BUSINESS_LICENSE";
        if ("/api/v1/proposals/file".equals(uri)) return "UPLOAD_PROPOSAL_FILE";
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

    private AccountEntity disputeOwner(DisputeEntity dispute) {
        return dispute == null ? null : contractRepository.findById(dispute.getContractId())
                .map(ContractEntity::getBusinessId)
                .map(this::businessOwnerByBusinessId)
                .orElse(null);
    }

    private String proposalDisplay(ProposalEntity proposal) {
        String expert = expertName(proposal.getExpertId());
        String job = jobTitle(proposal.getJobId());
        return "Bản đề xuất của " + expert + " cho dự án " + job;
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
        String display = contractRepository.findById(dispute.getContractId())
                .map(contract -> "Tranh chấp giữa " + businessName(contract.getBusinessId()) + " và " + expertName(contract.getExpertId()))
                .orElse("Tranh chấp");
        if (dispute.getAssignedStaffId() != null) {
            String staffName = staffRepository.findById(dispute.getAssignedStaffId())
                    .map(StaffEntity::getAccountId)
                    .map(this::accountById)
                    .map(this::displayAccount)
                    .orElse("nhân viên");
            return display + " - nhân viên phụ trách: " + staffName;
        }
        return display;
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
            return "Thanh toán cho dự án " + jobTitle(order.getJobId().intValue());
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
        if (entityName == null || entityName.isBlank()) return "Đối tượng kiểm toán";
        return switch (entityName) {
            case "membership_purchases" -> "Gói thành viên";
            case "quota_usage_logs" -> "Lượt sử dụng quota";
            case "wallet_transactions" -> "Giao dịch ví";
            case "withdrawal_requests" -> "Yêu cầu rút tiền";
            case "contract_deposits" -> "Ký quỹ hợp đồng";
            case "notifications" -> "Thông báo";
            case "profiles" -> "Hồ sơ người dùng";
            default -> "Đối tượng hệ thống";
        };
    }

    private String lowerFirst(String value) {
        if (value == null || value.isBlank()) return value;
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    private String extractUri(String action) {
        if (action == null) return null;
        int index = action.indexOf("/api/");
        if (index < 0) return null;
        String uri = action.substring(index).trim();
        int end = uri.indexOf(' ');
        return end < 0 ? uri : uri.substring(0, end);
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
