package com.ppob.servicea.controller;

import com.ppob.servicea.dto.PpobDto;
import com.ppob.servicea.messaging.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MessagingService messagingService;

    @PostMapping("/login")
    public ResponseEntity<PpobDto.ApiResponse<PpobDto.LoginResponse>> login(
            @Valid @RequestBody PpobDto.LoginRequest request) {
        log.info("Login attempt: {}", request.getUsername());
        try {
            PpobDto.AuthReplyMessage reply =
                    messagingService.sendAuthRequest(request.getUsername(), request.getPassword());
            if (reply.isSuccess()) {
                return ResponseEntity.ok(PpobDto.ApiResponse.ok("Login successful",
                        PpobDto.LoginResponse.builder()
                                .token(reply.getToken())
                                .username(reply.getUsername())
                                .fullName(reply.getFullName())
                                .build()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PpobDto.ApiResponse.error(reply.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PpobDto.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<PpobDto.ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PpobDto.ApiResponse.error("Authorization header required. Format: Bearer <token>"));
        }

        String token = authHeader.substring(7);
        log.info("Logout request received");

        try {
            PpobDto.LogoutReplyMessage reply = messagingService.sendLogoutRequest(token);
            if (reply.isSuccess()) {
                return ResponseEntity.ok(PpobDto.ApiResponse.ok("Logout successful", null));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PpobDto.ApiResponse.error(reply.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PpobDto.ApiResponse.error(e.getMessage()));
        }
    }
}
