package com.ppob.servicea.messaging;

import com.ppob.servicea.dto.PpobDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyListener {

    private final MessagingService messagingService;

    @RabbitListener(queues = "${ppob.rabbitmq.reply-queue.auth-reply}")
    public void onAuthReply(PpobDto.AuthReplyMessage reply) {
        log.info("Received AUTH_REPLY correlationId={}", reply.getCorrelationId());
        messagingService.handleAuthReply(reply);
    }

    @RabbitListener(queues = "${ppob.rabbitmq.reply-queue.payment-reply}")
    public void onPaymentReply(PpobDto.PaymentReplyMessage reply) {
        log.info("Received PAYMENT_REPLY correlationId={}", reply.getCorrelationId());
        messagingService.handlePaymentReply(reply);
    }

    @RabbitListener(queues = "${ppob.rabbitmq.reply-queue.profile-reply}")
    public void onProfileReply(PpobDto.ProfileReplyMessage reply) {
        log.info("Received PROFILE_REPLY correlationId={}", reply.getCorrelationId());
        messagingService.handleProfileReply(reply);
    }

    @RabbitListener(queues = "${ppob.rabbitmq.reply-queue.logout-reply}")
    public void onLogoutReply(PpobDto.LogoutReplyMessage reply) {
        log.info("Received LOGOUT_REPLY correlationId={}", reply.getCorrelationId());
        messagingService.handleLogoutReply(reply);
    }
}
