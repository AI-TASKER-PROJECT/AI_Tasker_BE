/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/GlobalExceptionHandler.java
 * Đây là file gì: File exception chuẩn hóa lỗi nghiệp vụ và cách API trả lỗi cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.exception;

import com.aitasker.be.common.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// GOM TAT CA EXCEPTION THANH JSON RESPONSE THONG NHAT.
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(ResourceConflictException.class)
    // Note: Hàm `handleConflict` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleConflict(ResourceConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(UnauthorizedException.class)
    // Note: Hàm `handleUnauthorized` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

<<<<<<< HEAD
    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
=======
    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadGateway(BadGatewayException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(ex.getMessage()));
    }

>>>>>>> feat/week4-MST
    @ExceptionHandler(AppException.class)
    // Note: Hàm `handleAppException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(NotFoundException.class)
    // Note: Hàm `handleNotFound` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(ForbiddenException.class)
    // Note: Hàm `handleForbidden` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Note: Hàm `handleValidation` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }

    // TRA VE CHI TIET LOI RANG BUOC DB DE DE DEBUG TRONG LOCAL/POSTMAN.
    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(DataIntegrityViolationException.class)
    // Note: Hàm `handleDataIntegrity` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DATABASE CONSTRAINT ERROR: " + message));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(Exception.class)
    // Note: Hàm `handleException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }
}
