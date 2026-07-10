/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/notification/UnreadNotificationCountResponse.java
 * Đây là file gì: DTO trả về số lượng thông báo chưa đọc của tài khoản hiện tại.
 * Mục đích note: giải thích dữ liệu response để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

// Note: Annotation này giúp Lombok sinh getter, setter và constructor có đủ tham số.
@Data
@AllArgsConstructor
public class UnreadNotificationCountResponse {
    private long unreadCount;
}
