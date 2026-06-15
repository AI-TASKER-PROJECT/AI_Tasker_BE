/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/catalog/TechnologyRequest.java
 * Đây là file gì: DTO nhận dữ liệu tạo/cập nhật danh mục công nghệ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.catalog;

import lombok.Data;

@Data
public class TechnologyRequest {
    private String technologyCode;
    private String technologyName;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
}
