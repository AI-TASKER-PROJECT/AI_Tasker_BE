package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseStorageServiceTest {

    private final FirebaseStorageService service = new FirebaseStorageService();

    @Test
    void validateSourceCodeArchive_shouldAcceptZipContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "source.zip", "application/zip", new byte[] {0x50, 0x4B, 0x03, 0x04});

        assertDoesNotThrow(() -> service.validateSourceCodeArchive(file));
    }

    @Test
    void validateSourceCodeArchive_shouldRejectNonZipFileName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "source.jar", "application/zip", new byte[] {0x50, 0x4B, 0x03, 0x04});

        AppException ex = assertThrows(AppException.class,
                () -> service.validateSourceCodeArchive(file));

        assertEquals("FILE SOURCE CODE PHAI CO DINH DANG ZIP", ex.getMessage());
    }

    @Test
    void validateSourceCodeArchive_shouldRejectUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "source.zip", "application/pdf", new byte[] {1});

        AppException ex = assertThrows(AppException.class,
                () -> service.validateSourceCodeArchive(file));

        assertEquals("DINH DANG FILE SOURCE CODE KHONG DUOC HO TRO", ex.getMessage());
    }

    @Test
    void validateSourceCodeArchive_shouldRejectSpoofedZipWithoutZipSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "source.zip", "application/zip", new byte[] {1, 2, 3, 4});

        AppException ex = assertThrows(AppException.class,
                () -> service.validateSourceCodeArchive(file));

        assertEquals("FILE SOURCE CODE KHONG PHAI ZIP HOP LE", ex.getMessage());
    }

    @Test
    void validateSourceCodeArchive_shouldRejectArchiveOverFiftyMegabytes() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(50L * 1024 * 1024 + 1);

        AppException ex = assertThrows(AppException.class,
                () -> service.validateSourceCodeArchive(file));

        assertEquals("FILE SOURCE CODE KHONG DUOC VUOT QUA 50MB", ex.getMessage());
    }

    @Test
    void validateUserGuide_shouldAcceptPdfSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "huong-dan.pdf", "application/pdf", new byte[] {0x25, 0x50, 0x44, 0x46});

        assertDoesNotThrow(() -> service.validateUserGuide(file));
    }

    @Test
    void validateUserGuide_shouldAcceptDocxSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "huong-dan.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[] {0x50, 0x4B, 0x03, 0x04});

        assertDoesNotThrow(() -> service.validateUserGuide(file));
    }

    @Test
    void validateUserGuide_shouldRejectUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "huong-dan.doc", "application/msword", new byte[] {1, 2, 3, 4});

        AppException ex = assertThrows(AppException.class, () -> service.validateUserGuide(file));

        assertEquals("Tệp hướng dẫn sử dụng phải có định dạng PDF hoặc DOCX", ex.getMessage());
    }

    @Test
    void validateUserGuide_shouldRejectSpoofedPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "huong-dan.pdf", "application/pdf", new byte[] {1, 2, 3, 4});

        AppException ex = assertThrows(AppException.class, () -> service.validateUserGuide(file));

        assertEquals("Nội dung tệp hướng dẫn không khớp với phần mở rộng", ex.getMessage());
    }
}
