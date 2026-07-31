/*
 * NOTE FILE: src/main/java/com/aitasker/be/AitaskerApplication.java
 * Đây là file gì: File khởi động chính của Spring Boot, dùng để bootstrap toàn bộ back-end AITASKER.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Note: Annotation này đánh dấu class khởi động Spring Boot và bật tự cấu hình/component scan.
@SpringBootApplication
@EnableScheduling
public class AitaskerApplication {

    // Note: Hàm main dùng để khởi động ứng dụng Spring Boot và chạy server back-end.
    public static void main(String[] args) {
        SpringApplication.run(AitaskerApplication.class, args);
    }

}
