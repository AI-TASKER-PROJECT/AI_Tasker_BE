/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/NotificationController.java
 * Đây là file gì: Controller cung cấp API đọc và cập nhật trạng thái thông báo của tài khoản hiện tại.
 * Mục đích note: giải thích các endpoint chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.notification.NotificationResponse;
import com.aitasker.be.dto.notification.UnreadNotificationCountResponse;
import com.aitasker.be.service.core.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Note: Annotation này khai báo class là REST controller để nhận request thông báo.
@RestController
@RequestMapping("/api/v1/notifications")
// Note: Annotation này giúp Lombok sinh constructor cho service dependency.
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // Note: Hàm `listMine` trả về danh sách thông báo của tài khoản đang đăng nhập.
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listMine() {
        return ResponseEntity.ok(ApiResponse.success("LAY DANH SACH THONG BAO THANH CONG", notificationService.listMine()));
    }

    // Note: Hàm `countUnreadMine` trả về số thông báo chưa đọc của tài khoản đang đăng nhập.
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> countUnreadMine() {
        return ResponseEntity.ok(ApiResponse.success("DEM THONG BAO CHUA DOC THANH CONG", notificationService.countUnreadMine()));
    }

    // Note: Hàm `markAsRead` đánh dấu một thông báo cụ thể là đã đọc.
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(ApiResponse.success("DANH DAU THONG BAO DA DOC THANH CONG", notificationService.markAsRead(notificationId)));
    }

    // Note: Hàm `markAllAsRead` đánh dấu toàn bộ thông báo của tài khoản hiện tại là đã đọc.
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> markAllAsRead() {
        return ResponseEntity.ok(ApiResponse.success("DANH DAU TAT CA THONG BAO DA DOC THANH CONG", notificationService.markAllAsRead()));
    }
}
