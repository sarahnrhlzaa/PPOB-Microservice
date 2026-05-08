package com.ppob.servicea.controller;

import com.ppob.servicea.dto.PpobDto;
import com.ppob.servicea.messaging.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final MessagingService messagingService;

    @GetMapping
    public ResponseEntity<PpobDto.ApiResponse<PpobDto.ProfileReplyMessage>> getProfile(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PpobDto.ApiResponse.error("Authorization header required. Format: Bearer <token>"));
        }

        String token = authHeader.substring(7);
        log.info("Profile request received");

        try {
            PpobDto.ProfileReplyMessage reply = messagingService.sendProfileRequest(token);
            if (reply.isSuccess()) {
                return ResponseEntity.ok(PpobDto.ApiResponse.ok("Profile retrieved", reply));
            }
            HttpStatus status = "UNAUTHORIZED".equals(reply.getStatus())
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(PpobDto.ApiResponse.error(reply.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PpobDto.ApiResponse.error(e.getMessage()));
        }
    }
}
