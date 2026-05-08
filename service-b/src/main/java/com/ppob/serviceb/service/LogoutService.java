package com.ppob.serviceb.service;

import com.ppob.serviceb.dto.PpobDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenService tokenService;

    public PpobDto.LogoutReplyMessage logout(PpobDto.LogoutRequestMessage request) {
        String correlationId = request.getCorrelationId();
        try {
            String username = tokenService.validateToken(request.getToken());
            if (username == null) {
                return build(correlationId, false, "Invalid or expired token");
            }
            tokenService.deleteToken(request.getToken());
            log.info("Logout successful for user: {}", username);
            return build(correlationId, true, "Logout successful");
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return build(correlationId, false, "Internal error during logout");
        }
    }

    private PpobDto.LogoutReplyMessage build(String correlationId, boolean success, String message) {
        return PpobDto.LogoutReplyMessage.builder()
                .correlationId(correlationId)
                .eventType("LOGOUT_REPLY")
                .success(success)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
