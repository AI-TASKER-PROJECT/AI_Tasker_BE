/*
 * NOTE FILE: src/test/java/com/aitasker/be/AitaskerApplicationTests.java
 * Đây là file gì: File test kiểm tra luồng hoặc nghiệp vụ để phát hiện lỗi hồi quy khi thay đổi code.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Note: Annotation này chạy test với Spring context đầy đủ.
@SpringBootTest(properties = {
        "spring.config.import=",
        "DB_HOST=127.0.0.1",
        "DB_PORT=5433",
        "DB_NAME=${TEST_DB_NAME:aitasker_db}",
        "DB_USER=aitasker",
        "DB_PASSWORD=aitasker123",
        "APP_JWT_SECRET=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "app.jwt.secret=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh}",
        "spring.datasource.username=aitasker",
        "spring.datasource.password=aitasker123"
})
class AitaskerApplicationTests {

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `contextLoads` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void contextLoads() {
    }

}
