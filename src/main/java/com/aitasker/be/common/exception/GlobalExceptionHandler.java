/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/GlobalExceptionHandler.java
 * Đây là file gì: File exception chuẩn hóa lỗi nghiệp vụ và cách API trả lỗi cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.exception;

import com.aitasker.be.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GlobalExceptionHandler {

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(ResourceConflictException.class)
    // Note: Hàm `handleConflict` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleConflict(ResourceConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(localizedMessage(ex.getMessage(), "Dữ liệu đã tồn tại hoặc xung đột với trạng thái hiện tại.")));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(UnauthorizedException.class)
    // Note: Hàm `handleUnauthorized` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(localizedMessage(ex.getMessage(), "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.")));
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadGateway(BadGatewayException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(localizedMessage(ex.getMessage(), "Không thể kết nối dịch vụ bên ngoài. Vui lòng thử lại sau.")));
    }

    @ExceptionHandler(AppException.class)
    // Note: Hàm `handleAppException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(localizedMessage(ex.getMessage(), "Không thể thực hiện yêu cầu với dữ liệu hoặc trạng thái hiện tại.")));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(NotFoundException.class)
    // Note: Hàm `handleNotFound` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(localizedMessage(ex.getMessage(), "Không tìm thấy dữ liệu được yêu cầu.")));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(ForbiddenException.class)
    // Note: Hàm `handleForbidden` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(localizedMessage(ex.getMessage(), "Tài khoản hiện tại không có quyền thực hiện thao tác này.")));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Note: Hàm `handleValidation` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> localizedMessage(e.getDefaultMessage(), "Thông tin gửi lên không hợp lệ."))
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }

    // TRA VE CHI TIET LOI RANG BUOC DB DE DE DEBUG TRONG LOCAL/POSTMAN.
    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(DataIntegrityViolationException.class)
    // Note: Hàm `handleDataIntegrity` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Yêu cầu vi phạm ràng buộc dữ liệu", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Dữ liệu bị trùng hoặc không đáp ứng ràng buộc của hệ thống."));
    }

    // Note: Annotation này khai báo hàm xử lý một loại exception cụ thể.
    @ExceptionHandler(Exception.class)
    // Note: Hàm `handleException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        log.error("Lỗi máy chủ chưa được xử lý", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Máy chủ gặp lỗi khi xử lý yêu cầu. Vui lòng thử lại sau."));
    }

    private String localizedMessage(String message, String fallback) {
        if (message == null || message.isBlank()) return fallback;
        String trimmed = message.trim();
        String normalized = trimmed.toUpperCase();
        String translated = switch (normalized) {
            case "PREMIUM_REQUIRED" -> "Tài khoản cần có gói cao cấp còn hiệu lực để sử dụng chức năng này.";
            case "INSUFFICIENT_BALANCE" -> "Số dư khả dụng trong ví không đủ để thực hiện giao dịch này.";
            case "CONTRACT_INVALID_STATUS" -> "Hợp đồng chưa ở trạng thái cho phép thực hiện thao tác này.";
            case "CONTRACT_NOT_FOUND" -> "Không tìm thấy hợp đồng.";
            case "DEPOSIT_ALREADY_HELD" -> "Khoản ký quỹ này đã được ghi nhận trước đó.";
            case "PROGRESS_REPORT_ACK_PENDING" -> "Báo cáo tiến độ mới nhất đang chờ doanh nghiệp xác nhận.";
            case "PROGRESS_REPORT_ACK_NOT_ALLOWED" -> "Báo cáo này không còn đủ điều kiện để xác nhận.";
            case "PROGRESS_REPORT_REQUEST_ALREADY_PENDING" -> "Đã có một yêu cầu báo cáo tiến độ đang chờ xử lý.";
            case "CONTRACT_DRAFT_CANCELLATION_NOT_ALLOWED" -> "Chỉ có thể hủy hợp đồng nháp chưa được ký hoặc xác thực.";
            case "MILESTONE_DA_QUA_HAN_NOP_SAN_PHAM" -> "Cột mốc đã quá hạn nộp sản phẩm.";
            case "NO_MATCHING_STAFF_FOR_JOB_DOMAIN" -> "Chưa có nhân viên phù hợp với lĩnh vực của dự án.";
            case "NO_AVAILABLE_STAFF_CAPACITY" -> "Các nhân viên phù hợp hiện đều đã đạt giới hạn xử lý.";
            case "STAFF_DA_DAT_GIOI_HAN_DISPUTE_DANG_XU_LY" -> "Nhân viên đã đạt giới hạn tranh chấp đang xử lý.";
            case "REJECTION_FEEDBACK_REQUIRED" -> "Vui lòng nhập phản hồi khi từ chối sản phẩm.";
            case "DISPUTE_ALREADY_ACTIVE" -> "Cột mốc đã có tranh chấp đang được xử lý.";
            case "REJECTED_CRITERIA_REQUIRED" -> "Vui lòng chọn ít nhất một tiêu chí chưa đạt.";
            case "REJECTED_CRITERIA_REASON_REQUIRED" -> "Vui lòng nhập lý do cho từng tiêu chí chưa đạt.";
            case "ONLY_DISPUTE_INITIATOR_CAN_WITHDRAW" -> "Chỉ bên tạo tranh chấp mới có thể rút yêu cầu.";
            case "DISPUTE_WITHDRAWAL_NOT_ALLOWED_AFTER_STAFF_ROUTING" -> "Không thể rút tranh chấp sau khi đã phân công nhân viên xử lý.";
            default -> null;
        };
        if (translated != null) return translated;
        if (trimmed.matches(".*[À-ỹĐđ].*")) return trimmed;
        if (normalized.contains("KHONG TIM THAY") || normalized.contains("NOT_FOUND")) {
            return "Không tìm thấy dữ liệu được yêu cầu.";
        }
        if (normalized.contains("KHONG CO QUYEN") || normalized.contains("FORBIDDEN")
                || normalized.startsWith("ONLY_")) {
            return "Tài khoản hiện tại không có quyền thực hiện thao tác này.";
        }
        if (normalized.contains("KHONG HOP LE") || normalized.contains("INVALID")) {
            return "Dữ liệu gửi lên không hợp lệ.";
        }
        if (normalized.contains("REQUIRED") || normalized.contains("DE TRONG")
                || normalized.contains("THIEU")) {
            return "Vui lòng cung cấp đầy đủ thông tin bắt buộc.";
        }
        return fallback;
    }
}
