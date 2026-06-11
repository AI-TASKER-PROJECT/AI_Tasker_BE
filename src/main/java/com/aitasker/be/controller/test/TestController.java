/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/test/TestController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/test")
// CHỈ DÀNH ĐỂ TEST TOKEN JWT
public class TestController {
    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/secure")
    // Note: Hàm `secure` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public String secure() {
        return "secure ok";
    }
}
