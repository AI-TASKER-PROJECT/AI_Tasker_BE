/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/candidate/ExpertRecommendationResponse.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
// Note: Annotation nay giup Lombok sinh constructor rong cho JPA hoac deserialize du lieu.
@NoArgsConstructor
// Note: Annotation nay giup Lombok sinh constructor nhan day du field.
@AllArgsConstructor
public class ExpertRecommendationResponse {
    private Long expertId;
    private Long portfolioId;
    private Integer rankPosition;
    private Double matchScore;
    private List<String> matchedSkills;
    private List<String> matchedDomains;
    private String reason;
    private Boolean businessSelected;
}
