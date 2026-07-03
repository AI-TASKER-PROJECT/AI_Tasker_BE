/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/core/AcceptanceCriteriaRequest.java
 * Đây là file gì: Request DTO để Business thêm hoặc sửa một tiêu chí nghiệm thu của milestone.
 */
package com.aitasker.be.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceCriteriaRequest {
    private String description;
    private Integer sortOrder;
}
