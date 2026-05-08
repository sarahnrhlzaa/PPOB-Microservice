package com.ppob.servicea.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PpobDto {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PaymentRequest {
        @NotBlank(message = "Product type is required")
        private String productType;
        @NotBlank(message = "Customer number is required")
        private String customerNumber;
        private BigDecimal amount;
    }

    // ── Auth ──────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthRequestMessage {
        private String correlationId;
        private String eventType;
        private String username;
        private String password;
        private LocalDateTime timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthReplyMessage {
        private String correlationId;
        private String eventType;
        private boolean success;
        private String token;
        private String username;
        private String fullName;
        private String message;
        private LocalDateTime timestamp;
    }

    // ── Logout ────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LogoutRequestMessage {
        private String correlationId;
        private String eventType;
        private String token;
        private LocalDateTime timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LogoutReplyMessage {
        private String correlationId;
        private String eventType;
        private boolean success;
        private String message;
        private LocalDateTime timestamp;
    }

    // ── Profile ───────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileRequestMessage {
        private String correlationId;
        private String eventType;
        private String token;
        private LocalDateTime timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProfileReplyMessage {
        private String correlationId;
        private String eventType;
        private boolean success;
        private String username;
        private String fullName;
        private BigDecimal balance;
        private String status;
        private String message;
        private LocalDateTime timestamp;
    }

    // ── Payment ───────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentRequestMessage {
        private String correlationId;
        private String eventType;
        private String token;
        private String productType;
        private String customerNumber;
        private BigDecimal amount;
        private LocalDateTime timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentReplyMessage {
        private String correlationId;
        private String eventType;
        private boolean success;
        private String transactionNumber;
        private String productType;
        private String customerNumber;
        private BigDecimal amount;
        private BigDecimal adminFee;
        private BigDecimal totalAmount;
        private String status;
        private String message;
        private LocalDateTime timestamp;
    }

    // ── Generic ───────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        public static <T> ApiResponse<T> ok(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true).message(message).data(data)
                    .timestamp(LocalDateTime.now()).build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false).message(message)
                    .timestamp(LocalDateTime.now()).build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String username;
        private String fullName;
    }
}
