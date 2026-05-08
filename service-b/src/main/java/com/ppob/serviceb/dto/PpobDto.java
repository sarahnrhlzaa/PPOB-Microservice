package com.ppob.serviceb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PpobDto {

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
}
