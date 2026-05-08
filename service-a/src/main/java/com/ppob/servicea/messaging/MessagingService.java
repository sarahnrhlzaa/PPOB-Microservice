package com.ppob.servicea.messaging;

import com.ppob.servicea.dto.PpobDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${ppob.rabbitmq.exchange}")                          private String exchange;
    @Value("${ppob.rabbitmq.routing-key.auth-request}")         private String authRequestRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.payment-request}")      private String paymentRequestRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.profile-request}")      private String profileRequestRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.logout-request}")       private String logoutRequestRoutingKey;
    @Value("${ppob.messaging.reply-timeout-ms:10000}")          private long replyTimeoutMs;

    private final ConcurrentHashMap<String, CompletableFuture<PpobDto.AuthReplyMessage>>
            authPendingReplies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<PpobDto.PaymentReplyMessage>>
            paymentPendingReplies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<PpobDto.ProfileReplyMessage>>
            profilePendingReplies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<PpobDto.LogoutReplyMessage>>
            logoutPendingReplies = new ConcurrentHashMap<>();

    // ── Auth ──────────────────────────────────────────────────────────────
    public PpobDto.AuthReplyMessage sendAuthRequest(String username, String password) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        PpobDto.AuthRequestMessage message = PpobDto.AuthRequestMessage.builder()
                .correlationId(correlationId).eventType("AUTH_REQUEST")
                .username(username).password(password)
                .timestamp(LocalDateTime.now()).build();

        CompletableFuture<PpobDto.AuthReplyMessage> future = new CompletableFuture<>();
        authPendingReplies.put(correlationId, future);
        log.info("Publishing AUTH_REQUEST correlationId={}", correlationId);
        rabbitTemplate.convertAndSend(exchange, authRequestRoutingKey, message);
        try {
            return future.get(replyTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Authentication service timeout. Please try again.");
        } finally {
            authPendingReplies.remove(correlationId);
        }
    }

    public void handleAuthReply(PpobDto.AuthReplyMessage reply) {
        CompletableFuture<PpobDto.AuthReplyMessage> future = authPendingReplies.get(reply.getCorrelationId());
        if (future != null) future.complete(reply);
        else log.warn("No pending auth request for correlationId={}", reply.getCorrelationId());
    }

    // ── Logout ────────────────────────────────────────────────────────────
    public PpobDto.LogoutReplyMessage sendLogoutRequest(String token) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        PpobDto.LogoutRequestMessage message = PpobDto.LogoutRequestMessage.builder()
                .correlationId(correlationId).eventType("LOGOUT_REQUEST")
                .token(token).timestamp(LocalDateTime.now()).build();

        CompletableFuture<PpobDto.LogoutReplyMessage> future = new CompletableFuture<>();
        logoutPendingReplies.put(correlationId, future);
        log.info("Publishing LOGOUT_REQUEST correlationId={}", correlationId);
        rabbitTemplate.convertAndSend(exchange, logoutRequestRoutingKey, message);
        try {
            return future.get(replyTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Logout service timeout. Please try again.");
        } finally {
            logoutPendingReplies.remove(correlationId);
        }
    }

    public void handleLogoutReply(PpobDto.LogoutReplyMessage reply) {
        CompletableFuture<PpobDto.LogoutReplyMessage> future = logoutPendingReplies.get(reply.getCorrelationId());
        if (future != null) future.complete(reply);
        else log.warn("No pending logout request for correlationId={}", reply.getCorrelationId());
    }

    // ── Profile ───────────────────────────────────────────────────────────
    public PpobDto.ProfileReplyMessage sendProfileRequest(String token) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        PpobDto.ProfileRequestMessage message = PpobDto.ProfileRequestMessage.builder()
                .correlationId(correlationId).eventType("PROFILE_REQUEST")
                .token(token).timestamp(LocalDateTime.now()).build();

        CompletableFuture<PpobDto.ProfileReplyMessage> future = new CompletableFuture<>();
        profilePendingReplies.put(correlationId, future);
        log.info("Publishing PROFILE_REQUEST correlationId={}", correlationId);
        rabbitTemplate.convertAndSend(exchange, profileRequestRoutingKey, message);
        try {
            return future.get(replyTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Profile service timeout. Please try again.");
        } finally {
            profilePendingReplies.remove(correlationId);
        }
    }

    public void handleProfileReply(PpobDto.ProfileReplyMessage reply) {
        CompletableFuture<PpobDto.ProfileReplyMessage> future = profilePendingReplies.get(reply.getCorrelationId());
        if (future != null) future.complete(reply);
        else log.warn("No pending profile request for correlationId={}", reply.getCorrelationId());
    }

    // ── Payment ───────────────────────────────────────────────────────────
    public PpobDto.PaymentReplyMessage sendPaymentRequest(
            String token, String productType, String customerNumber, BigDecimal amount) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        PpobDto.PaymentRequestMessage message = PpobDto.PaymentRequestMessage.builder()
                .correlationId(correlationId).eventType("PAYMENT_REQUEST")
                .token(token).productType(productType)
                .customerNumber(customerNumber).amount(amount)
                .timestamp(LocalDateTime.now()).build();

        CompletableFuture<PpobDto.PaymentReplyMessage> future = new CompletableFuture<>();
        paymentPendingReplies.put(correlationId, future);
        log.info("Publishing PAYMENT_REQUEST correlationId={}", correlationId);
        rabbitTemplate.convertAndSend(exchange, paymentRequestRoutingKey, message);
        try {
            return future.get(replyTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Payment service timeout. Please try again.");
        } finally {
            paymentPendingReplies.remove(correlationId);
        }
    }

    public void handlePaymentReply(PpobDto.PaymentReplyMessage reply) {
        CompletableFuture<PpobDto.PaymentReplyMessage> future = paymentPendingReplies.get(reply.getCorrelationId());
        if (future != null) future.complete(reply);
        else log.warn("No pending payment request for correlationId={}", reply.getCorrelationId());
    }
}
