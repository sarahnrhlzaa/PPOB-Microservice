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
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final MessagingService messagingService;

    @PostMapping("/pay")
    public ResponseEntity<PpobDto.ApiResponse<PpobDto.PaymentReplyMessage>> pay(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PpobDto.PaymentRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PpobDto.ApiResponse.error("Authorization header required. Format: Bearer <token>"));
        }

        String token = authHeader.substring(7);
        log.info("Payment request: product={}, customer={}", request.getProductType(), request.getCustomerNumber());

        try {
            PpobDto.PaymentReplyMessage reply = messagingService.sendPaymentRequest(
                    token, request.getProductType(),
                    request.getCustomerNumber(), request.getAmount());

            if (reply.isSuccess()) {
                return ResponseEntity.ok(PpobDto.ApiResponse.ok("Payment successful", reply));
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
