package com.ppob.serviceb.consumer;

import com.ppob.serviceb.dto.PpobDto;
import com.ppob.serviceb.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final AuthService authService;
    private final PaymentService paymentService;
    private final ProfileService profileService;
    private final LogoutService logoutService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${ppob.rabbitmq.exchange}")                          private String exchange;
    @Value("${ppob.rabbitmq.routing-key.auth-reply}")           private String authReplyRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.payment-reply}")        private String paymentReplyRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.profile-reply}")        private String profileReplyRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.logout-reply}")         private String logoutReplyRoutingKey;

    @RabbitListener(queues = "${ppob.rabbitmq.queue.auth-request}")
    public void handleAuthRequest(PpobDto.AuthRequestMessage request) {
        log.info("Received AUTH_REQUEST correlationId={}, username={}", request.getCorrelationId(), request.getUsername());
        PpobDto.AuthReplyMessage reply = authService.login(request);
        rabbitTemplate.convertAndSend(exchange, authReplyRoutingKey, reply);
        log.info("Sent AUTH_REPLY correlationId={}, success={}", reply.getCorrelationId(), reply.isSuccess());
    }

    @RabbitListener(queues = "${ppob.rabbitmq.queue.payment-request}")
    public void handlePaymentRequest(PpobDto.PaymentRequestMessage request) {
        log.info("Received PAYMENT_REQUEST correlationId={}, product={}", request.getCorrelationId(), request.getProductType());
        PpobDto.PaymentReplyMessage reply = paymentService.processPayment(request);
        rabbitTemplate.convertAndSend(exchange, paymentReplyRoutingKey, reply);
        log.info("Sent PAYMENT_REPLY correlationId={}, success={}", reply.getCorrelationId(), reply.isSuccess());
    }

    @RabbitListener(queues = "${ppob.rabbitmq.queue.profile-request}")
    public void handleProfileRequest(PpobDto.ProfileRequestMessage request) {
        log.info("Received PROFILE_REQUEST correlationId={}", request.getCorrelationId());
        PpobDto.ProfileReplyMessage reply = profileService.getProfile(request);
        rabbitTemplate.convertAndSend(exchange, profileReplyRoutingKey, reply);
        log.info("Sent PROFILE_REPLY correlationId={}, success={}", reply.getCorrelationId(), reply.isSuccess());
    }

    @RabbitListener(queues = "${ppob.rabbitmq.queue.logout-request}")
    public void handleLogoutRequest(PpobDto.LogoutRequestMessage request) {
        log.info("Received LOGOUT_REQUEST correlationId={}", request.getCorrelationId());
        PpobDto.LogoutReplyMessage reply = logoutService.logout(request);
        rabbitTemplate.convertAndSend(exchange, logoutReplyRoutingKey, reply);
        log.info("Sent LOGOUT_REPLY correlationId={}, success={}", reply.getCorrelationId(), reply.isSuccess());
    }
}
