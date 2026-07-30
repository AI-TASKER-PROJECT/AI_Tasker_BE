package com.aitasker.be.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void appException_shouldTranslateLegacyMachineCodeToVietnamese() {
        var response = handler.handleAppException(new AppException("NO_MATCHING_STAFF_FOR_JOB_DOMAIN"));

        assertNotNull(response.getBody());
        assertEquals("Chưa có nhân viên phù hợp với lĩnh vực của dự án.",
                response.getBody().getMessage());
    }

    @Test
    void unexpectedException_shouldNotExposeInternalEnglishMessage() {
        var response = handler.handleException(new IllegalStateException("database password leaked"));

        assertNotNull(response.getBody());
        assertEquals("Máy chủ gặp lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.",
                response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("password"));
    }
}
