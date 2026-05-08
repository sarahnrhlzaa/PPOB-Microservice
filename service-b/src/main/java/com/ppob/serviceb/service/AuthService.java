package com.ppob.serviceb.service;

import com.ppob.serviceb.dto.PpobDto;
import com.ppob.serviceb.entity.User;
import com.ppob.serviceb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public PpobDto.AuthReplyMessage login(PpobDto.AuthRequestMessage request) {
        String correlationId = request.getCorrelationId();
        try {
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

            if (userOpt.isEmpty()) {
                return buildAuthReply(correlationId, false, null, null, null, "Invalid username or password");
            }

            User user = userOpt.get();

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return buildAuthReply(correlationId, false, null, null, null, "Invalid username or password");
            }

            String token = tokenService.generateAndSave(user.getUsername());
            return buildAuthReply(correlationId, true, token, user.getUsername(), user.getFullName(), "Login successful");

        } catch (Exception e) {
            log.error("Auth error: {}", e.getMessage());
            return buildAuthReply(correlationId, false, null, null, null, "Internal error during authentication");
        }
    }

    private PpobDto.AuthReplyMessage buildAuthReply(String correlationId, boolean success,
            String token, String username, String fullName, String message) {
        return PpobDto.AuthReplyMessage.builder()
                .correlationId(correlationId)
                .eventType("AUTH_REPLY")
                .success(success)
                .token(token)
                .username(username)
                .fullName(fullName)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
