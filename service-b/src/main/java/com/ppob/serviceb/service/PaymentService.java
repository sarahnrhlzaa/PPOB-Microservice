package com.ppob.serviceb.service;

import com.ppob.serviceb.dto.PpobDto;
import com.ppob.serviceb.entity.Transaction;
import com.ppob.serviceb.entity.User;
import com.ppob.serviceb.repository.TransactionRepository;
import com.ppob.serviceb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TokenService tokenService;

    private static final BigDecimal ADMIN_FEE = new BigDecimal("2500");

    @Transactional
    public PpobDto.PaymentReplyMessage processPayment(PpobDto.PaymentRequestMessage request) {
        String correlationId = request.getCorrelationId();

        // 1. Validasi token di Redis
        String username = tokenService.validateToken(request.getToken());
        if (username == null) {
            return buildPaymentReply(correlationId, false, null, request, null, null, "UNAUTHORIZED", "Invalid or expired token");
        }

        // 2. Cari user di DB
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return buildPaymentReply(correlationId, false, null, request, null, null, "UNAUTHORIZED", "User not found");
        }

        User user = userOpt.get();
        BigDecimal totalAmount = request.getAmount().add(ADMIN_FEE);

        // 3. Cek saldo
        if (user.getBalance().compareTo(totalAmount) < 0) {
            return buildPaymentReply(correlationId, false, null, request, null, null, "INSUFFICIENT_BALANCE", "Insufficient balance");
        }

        // 4. Kurangi saldo dan simpan transaksi
        user.setBalance(user.getBalance().subtract(totalAmount));
        userRepository.save(user);

        Transaction tx = new Transaction();
        tx.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tx.setUser(user);
        tx.setProductType(request.getProductType());
        tx.setCustomerNumber(request.getCustomerNumber());
        tx.setAmount(request.getAmount());
        tx.setAdminFee(ADMIN_FEE);
        tx.setTotalAmount(totalAmount);
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        return buildPaymentReply(correlationId, true, tx.getTransactionNumber(), request, ADMIN_FEE, totalAmount, "SUCCESS", "Payment successful");
    }

    private PpobDto.PaymentReplyMessage buildPaymentReply(
            String correlationId, boolean success, String txNumber,
            PpobDto.PaymentRequestMessage request, BigDecimal adminFee,
            BigDecimal totalAmount, String status, String message) {
        return PpobDto.PaymentReplyMessage.builder()
                .correlationId(correlationId)
                .eventType("PAYMENT_REPLY")
                .success(success)
                .transactionNumber(txNumber)
                .productType(request.getProductType())
                .customerNumber(request.getCustomerNumber())
                .amount(request.getAmount())
                .adminFee(adminFee)
                .totalAmount(totalAmount)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
