/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/FirebaseStorageService.java
 * Đây là file gì: File service xử lý upload file lên Firebase Storage và trả về đường dẫn lưu trong database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
public class FirebaseStorageService {
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_SOURCE_CODE_ARCHIVE_SIZE_BYTES = 50L * 1024 * 1024;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // Note: Hàm `upload` kiểm tra file hợp lệ, upload lên Firebase Storage và trả về storage path để lưu trong database.
    public String upload(MultipartFile file, String folder) {
        validateFirebaseReady();
        validateFile(file);

        return store(file, folder);
    }

    public String uploadSourceCodeArchive(MultipartFile file, String folder) {
        validateFirebaseReady();
        validateSourceCodeArchive(file);

        return store(file, folder);
    }

    private String store(MultipartFile file, String folder) {
        String objectName = folder + "/" + UUID.randomUUID() + "-" + sanitizeFileName(file.getOriginalFilename());
        try {
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.create(objectName, file.getBytes(), file.getContentType());
            return objectName;
        } catch (IOException ex) {
            throw new AppException("KHONG DOC DUOC FILE CAN UPLOAD");
        } catch (RuntimeException ex) {
            throw new AppException("UPLOAD FIREBASE STORAGE THAT BAI");
        }
    }

    void validateSourceCodeArchive(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("FILE SOURCE CODE KHONG HOP LE");
        }
        if (file.getSize() > MAX_SOURCE_CODE_ARCHIVE_SIZE_BYTES) {
            throw new AppException("FILE SOURCE CODE KHONG DUOC VUOT QUA 50MB");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(java.util.Locale.ROOT).endsWith(".zip")) {
            throw new AppException("FILE SOURCE CODE PHAI CO DINH DANG ZIP");
        }
        String contentType = file.getContentType();
        if (contentType == null || !List.of(
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream"
        ).contains(contentType)) {
            throw new AppException("DINH DANG FILE SOURCE CODE KHONG DUOC HO TRO");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(4);
            boolean validZipSignature = signature.length == 4
                    && signature[0] == 0x50
                    && signature[1] == 0x4B
                    && ((signature[2] == 0x03 && signature[3] == 0x04)
                    || (signature[2] == 0x05 && signature[3] == 0x06)
                    || (signature[2] == 0x07 && signature[3] == 0x08));
            if (!validZipSignature) {
                throw new AppException("FILE SOURCE CODE KHONG PHAI ZIP HOP LE");
            }
        } catch (IOException ex) {
            throw new AppException("KHONG DOC DUOC FILE SOURCE CODE");
        }
    }

    // Note: Hàm `validateFirebaseReady` bảo đảm Firebase đã được cấu hình trước khi cho upload file.
    // Note: Hàm `createReadUrl` tạo signed URL tạm thời để người dùng đã đăng nhập có thể bấm xem file Firebase.
    public String createReadUrl(String objectName) {
        validateFirebaseReady();
        if (objectName == null || objectName.isBlank()) {
            throw new AppException("FILE PATH KHONG HOP LE");
        }
        Blob blob = StorageClient.getInstance().bucket().get(objectName);
        if (blob == null) {
            throw new NotFoundException("KHONG TIM THAY FILE FIREBASE");
        }
        return blob.signUrl(15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature()).toString();
    }

    private void validateFirebaseReady() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new AppException("CHUA CAU HINH FIREBASE STORAGE");
        }
    }

    // Note: Hàm `validateFile` giới hạn file upload theo dung lượng và định dạng để tránh lưu dữ liệu không hợp lệ.
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("FILE KHONG HOP LE");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AppException("FILE KHONG DUOC VUOT QUA 10MB");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new AppException("DINH DANG FILE KHONG DUOC HO TRO");
        }
    }

    // Note: Hàm `sanitizeFileName` chuẩn hóa tên file để storage path gọn và tránh ký tự gây lỗi đường dẫn.
    private String sanitizeFileName(String originalName) {
        String fallback = "file";
        if (originalName == null || originalName.isBlank()) {
            return fallback;
        }
        String normalized = Normalizer.normalize(originalName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? fallback : normalized;
    }
}
