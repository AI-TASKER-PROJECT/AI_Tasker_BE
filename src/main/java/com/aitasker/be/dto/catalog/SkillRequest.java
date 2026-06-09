/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/catalog/SkillRequest.java
 * Đây là file gì: File DTO mô tả dữ liệu request/response, giúp tách dữ liệu API khỏi entity database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.catalog;

import lombok.Data;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho dữ liệu.
@Data
public class SkillRequest {
    private String skillCode;
    private String skillName;
    private String description;
    private Boolean isActive;
}
