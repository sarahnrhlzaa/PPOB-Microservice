package com.ppob.serviceb.service;

import com.ppob.serviceb.dto.PpobDto;
import com.ppob.serviceb.entity.User;
import com.ppob.serviceb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public PpobDto.ProfileReplyMessage getProfile(PpobDto.ProfileRequestMessage request) {
        String correlationId = request.getCorrelationId();
        try {
            String username = tokenService.validateToken(request.getToken());
            if (username == null) {
                return buildReply(correlationId, false, null, "UNAUTHORIZED", "Invalid or expired token");
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return buildReply(correlationId, false, null, "NOT_FOUND", "User not found");
            }

            User user = userOpt.get();
            return PpobDto.ProfileReplyMessage.builder()
                    .correlationId(correlationId)
                    .eventType("PROFILE_REPLY")
                    .success(true)
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .balance(user.getBalance())
                    .status("SUCCESS")
                    .message("Profile retrieved successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Profile error: {}", e.getMessage());
            return buildReply(correlationId, false, null, "ERROR", "Internal error retrieving profile");
        }
    }

    private PpobDto.ProfileReplyMessage buildReply(String correlationId, boolean success,
            String username, String status, String message) {
        return PpobDto.ProfileReplyMessage.builder()
                .correlationId(correlationId)
                .eventType("PROFILE_REPLY")
                .success(success)
                .username(username)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
