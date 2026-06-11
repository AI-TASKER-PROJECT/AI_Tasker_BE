/*
 * NOTE FILE: src/main/java/com/aitasker/be/test_demo/HealthController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.test_demo;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@SecurityRequirements
public class HealthController {
    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/api/health")
    // Note: Hàm `health` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public String health() {
        return "Health Api Oke";
    }
}
