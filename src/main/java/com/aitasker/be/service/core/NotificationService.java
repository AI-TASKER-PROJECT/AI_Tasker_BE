/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/NotificationService.java
 * Đây là file gì: Service xử lý tạo, đọc, đánh dấu đã đọc và đẩy thông báo realtime qua WebSocket.
 * Mục đích note: giải thích các hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.notification.NotificationResponse;
import com.aitasker.be.dto.notification.UnreadNotificationCountResponse;
import com.aitasker.be.entity.NotificationEntity;
import com.aitasker.be.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class NotificationService {
    private final AccessService accessService;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Note: Hàm `listMine` lấy toàn bộ thông báo của tài khoản đang đăng nhập để giao diện hiển thị lịch sử thông báo.
    public List<NotificationResponse> listMine() {
        Integer accountId = accessService.currentAccount().getAccountId();
        return notificationRepository.findByReceiverAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Note: Hàm `countUnreadMine` đếm thông báo chưa đọc của tài khoản hiện tại để hiển thị badge.
    public UnreadNotificationCountResponse countUnreadMine() {
        Integer accountId = accessService.currentAccount().getAccountId();
        return new UnreadNotificationCountResponse(notificationRepository.countByReceiverAccountIdAndIsReadFalse(accountId));
    }

    // Note: Hàm `markAsRead` đánh dấu một thông báo là đã đọc, đồng thời chặn đọc thông báo của tài khoản khác.
    @Transactional
    public NotificationResponse markAsRead(Integer notificationId) {
        Integer accountId = accessService.currentAccount().getAccountId();
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY THONG BAO"));
        if (!accountId.equals(notification.getReceiverAccountId())) {
            throw new ForbiddenException("BAN KHONG CO QUYEN DOC THONG BAO NAY");
        }
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(Boolean.TRUE);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    // Note: Hàm `markAllAsRead` đánh dấu tất cả thông báo của tài khoản hiện tại là đã đọc.
    @Transactional
    public List<NotificationResponse> markAllAsRead() {
        Integer accountId = accessService.currentAccount().getAccountId();
        LocalDateTime now = LocalDateTime.now();
        List<NotificationEntity> notifications = notificationRepository.findByReceiverAccountIdOrderByCreatedAtDesc(accountId);
        notifications.stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
                .forEach(notification -> {
                    notification.setIsRead(Boolean.TRUE);
                    notification.setReadAt(now);
                });
        return notificationRepository.saveAll(notifications).stream()
                .map(this::toResponse)
                .toList();
    }

    // Note: Hàm `notifyProposalCreated` tạo thông báo tiếng Việt khi chuyên gia nộp proposal cho job của doanh nghiệp.
    public void notifyProposalCreated(Integer receiverAccountId, Integer actorAccountId, Integer jobId, String jobTitle) {
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "PROPOSAL_CREATED",
                "Có proposal mới",
                "Một chuyên gia vừa gửi proposal cho dự án \"" + safeText(jobTitle, "không tên") + "\".",
                "/business/jobs/" + jobId + "/proposals"
        );
    }

    // Note: Hàm `notifyProposalReviewed` tạo thông báo tiếng Việt khi doanh nghiệp chấp nhận hoặc từ chối proposal.
    public void notifyProposalReviewed(Integer receiverAccountId, Integer actorAccountId, Integer jobId, String jobTitle, String status) {
        String result = "Accepted".equalsIgnoreCase(status) ? "đã được chấp nhận" : "đã bị từ chối";
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "PROPOSAL_REVIEWED",
                "Kết quả proposal",
                "Proposal của bạn cho dự án \"" + safeText(jobTitle, "không tên") + "\" " + result + ".",
                "/expert/proposals"
        );
    }

    // Note: Hàm `notifyExpertSelectedForJob` báo cho expert khi doanh nghiệp chọn expert từ danh sách AI recommendation.
    public void notifyExpertSelectedForJob(Integer receiverAccountId, Integer actorAccountId, Integer jobId, String jobTitle) {
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "EXPERT_RECOMMENDATION_SELECTED",
                "Bạn được doanh nghiệp chọn",
                "Doanh nghiệp đã chọn bạn cho dự án \"" + safeText(jobTitle, "không tên") + "\". Hãy xem job và nộp proposal nếu phù hợp.",
                "/expert/jobs/" + jobId
        );
    }

    // Note: Hàm `notifyProfileReviewed` tạo thông báo tiếng Việt khi staff duyệt hoặc từ chối hồ sơ KYB/KYC.
    public void notifyProfileReviewed(Integer receiverAccountId, Integer actorAccountId, String profileType, String status) {
        boolean approved = "Approved".equalsIgnoreCase(status);
        String profileName = "BUSINESS".equalsIgnoreCase(profileType) ? "doanh nghiệp" : "chuyên gia";
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "PROFILE_REVIEWED",
                approved ? "Hồ sơ đã được duyệt" : "Hồ sơ bị từ chối",
                "Hồ sơ " + profileName + " của bạn " + (approved ? "đã được staff duyệt." : "đã bị staff từ chối."),
                "BUSINESS".equalsIgnoreCase(profileType) ? "/business/kyb" : "/expert/profile"
        );
    }

    // Note: Hàm `notifyDeliverableSubmitted` tạo thông báo tiếng Việt khi chuyên gia nộp sản phẩm bàn giao cho milestone.
    public void notifyDeliverableSubmitted(Integer receiverAccountId, Integer actorAccountId,
            Integer contractId, Integer milestoneId, Integer deliverableId, String milestoneName) {
        Map<String, Object> metadata = Map.of(
                "contractId", contractId,
                "milestoneId", milestoneId,
                "deliverableId", deliverableId
        );
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "DELIVERABLE_SUBMITTED",
                "Có sản phẩm bàn giao mới",
                "Chuyên gia vừa nộp sản phẩm bàn giao cho milestone \"" + safeText(milestoneName, "không tên") + "\".",
                "/contracts/" + contractId + "/workspace?milestoneId=" + milestoneId,
                metadata
        );
    }

    public void notifyContractEvent(Integer receiverAccountId, Integer actorAccountId, String type, String title, String message, Integer contractId) {
        createAndPush(
                receiverAccountId,
                actorAccountId,
                type,
                title,
                message,
                "/contracts/" + contractId
        );
    }

    // Note: Hàm `notifyDisputeCreated` tạo thông báo tiếng Việt khi có tranh chấp mới được gán cho staff.
    public void notifyDisputeCreated(Integer receiverAccountId, Integer actorAccountId, Integer disputeId) {
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "DISPUTE_CREATED",
                "Có tranh chấp mới",
                "Một tranh chấp mới đã được tạo và cần staff xử lý.",
                "/staff/disputes/" + disputeId
        );
    }

    // Note: Hàm `notifyDisputeAssigned` tạo thông báo tiếng Việt khi admin gán tranh chấp cho staff.
    public void notifyDisputeAssigned(Integer receiverAccountId, Integer actorAccountId, Integer disputeId) {
        createAndPush(
                receiverAccountId,
                actorAccountId,
                "DISPUTE_ASSIGNED",
                "Bạn được gán xử lý tranh chấp",
                "Admin vừa gán một tranh chấp cho bạn xử lý.",
                "/staff/disputes/" + disputeId
        );
    }

    // Note: Hàm `createAndPush` lưu thông báo vào database và đẩy realtime tới đúng tài khoản nhận qua WebSocket.
    @Transactional
    public NotificationResponse createAndPush(Integer receiverAccountId, Integer actorAccountId, String type, String title, String message, String targetUrl) {
        return createAndPush(receiverAccountId, actorAccountId, type, title, message, targetUrl, null);
    }

    @Transactional
    public NotificationResponse createAndPush(Integer receiverAccountId, Integer actorAccountId, String type, String title, String message, String targetUrl, Map<String, Object> metadata) {
        if (receiverAccountId == null) return null;
        String metadataJson = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (Exception ignored) {
            }
        }
        NotificationEntity saved = notificationRepository.save(NotificationEntity.builder()
                .receiverAccountId(receiverAccountId)
                .actorAccountId(actorAccountId)
                .type(type)
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .metadata(metadataJson)
                .isRead(Boolean.FALSE)
                .build());
        NotificationResponse response = toResponse(saved);
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverAccountId), "/queue/notifications", response);
        return response;
    }

    // Note: Hàm `toResponse` chuyển entity sang DTO để không trả trực tiếp dữ liệu database nội bộ.
    private NotificationResponse toResponse(NotificationEntity notification) {
        Object metadataObj = null;
        if (notification.getMetadata() != null && !notification.getMetadata().isBlank()) {
            try {
                metadataObj = objectMapper.readValue(notification.getMetadata(), Object.class);
            } catch (Exception ignored) {
            }
        }
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetUrl(notification.getTargetUrl())
                .metadata(metadataObj)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    // Note: Hàm `safeText` tránh trả message bị trống khi dữ liệu gốc chưa có tên rõ ràng.
    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
