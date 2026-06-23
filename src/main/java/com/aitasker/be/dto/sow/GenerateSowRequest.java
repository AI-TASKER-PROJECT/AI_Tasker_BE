/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/sow/GenerateSowRequest.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.sow;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
// Note: Annotation nay giup Lombok sinh constructor rong cho JPA hoac deserialize du lieu.
@NoArgsConstructor
// Note: Annotation nay giup Lombok sinh constructor nhan day du field.
@AllArgsConstructor
public class GenerateSowRequest {
    // Note: Annotation nay bat buoc field phai co gia tri text hop le.
    @NotBlank(message = "projectTitle khong duoc rong")
    private String projectTitle;

    // Note: Annotation nay bat buoc field phai co gia tri text hop le.
    @NotBlank(message = "rawRequirement khong duoc rong")
    private String rawRequirement;

    // Note: Annotation nay bat buoc field khong duoc null.
    @NotNull(message = "budget khong duoc rong")
    @DecimalMin(value = "0.0", inclusive = false, message = "budget phai lon hon 0")
    private BigDecimal budget;

    // Note: Annotation nay bat buoc field khong duoc null.
    @NotNull(message = "duration khong duoc rong")
    @Positive(message = "duration phai lon hon 0")
    private Integer duration;

    // Note: Annotation nay bat buoc field phai co gia tri text hop le.
    @NotBlank(message = "durationUnit khong duoc rong")
    private String durationUnit;

    private List<String> supportFields;
    private List<String> requiredSkills;
}
