/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/FirebaseConfig.java
 * Đây là file gì: File cấu hình Firebase Admin SDK để back-end có thể upload file lên Firebase Storage.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

// Note: Annotation này cho Spring quản lý class như một file cấu hình ứng dụng.
@Configuration
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final nếu có.
@RequiredArgsConstructor
public class FirebaseConfig {

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${firebase.storage-bucket:}")
    private String storageBucket;

    // Note: Annotation này yêu cầu Spring chạy hàm sau khi khởi tạo bean để chuẩn bị Firebase trước khi nhận request.
    @PostConstruct
    // Note: Hàm `initializeFirebase` khởi tạo Firebase Admin SDK bằng file service account và bucket đã cấu hình trong .env.
    public void initializeFirebase() throws IOException {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()
                || storageBucket == null || storageBucket.isBlank()
                || !FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(storageBucket)
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}
