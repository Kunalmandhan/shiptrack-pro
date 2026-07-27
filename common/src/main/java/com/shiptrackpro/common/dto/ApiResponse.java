package com.shiptrackpro.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API response wrapper used by ALL endpoints across ALL services.
 * Guarantees a consistent JSON structure for clients:
 *
 * Success: { success: true, message: "...", data: {...}, timestamp, path }
 * Error:   { success: false, message: "...", errors: [...], errorCode: "...", timestamp, path }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private List<FieldError> errors;
    private String path;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // --- Factory Methods ---

    public static <T> ApiResponse<T> success(String message, T data, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode,
                                           List<FieldError> errors, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .path(path)
                .build();
    }

    // --- Convenience overloads for service-layer usage (no path/status) ---

    /** Success with data only — used in service layer where path is not available */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /** Success with message and data */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Success with message only — used for simple confirmation responses */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Success with message only — HTTP status should be set via ResponseEntity.
     * @deprecated Use {@link #success(String)} instead. The status parameter was unused.
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static <T> ApiResponse<T> success(String message, HttpStatus status) {
        return success(message);
    }

    /**
     * Represents a single field validation error.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
