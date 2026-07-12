/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/NotificationRepository.java
 * Đây là file gì: Repository truy cập dữ liệu thông báo trong bảng notifications.
 * Mục đích note: giải thích các hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    // Note: Hàm `findByReceiverAccountIdOrderByCreatedAtDesc` lấy thông báo của một tài khoản theo thời gian mới nhất.
    List<NotificationEntity> findByReceiverAccountIdOrderByCreatedAtDesc(Integer receiverAccountId);

    // Note: Hàm `countByReceiverAccountIdAndIsReadFalse` đếm số thông báo chưa đọc để hiển thị badge trên giao diện.
    long countByReceiverAccountIdAndIsReadFalse(Integer receiverAccountId);

    Optional<NotificationEntity> findByIdempotencyKey(String idempotencyKey);
}
