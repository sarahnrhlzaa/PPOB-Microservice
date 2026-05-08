package com.ppob.serviceb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${ppob.token.expiry-seconds:86400}")
    private long expirySeconds;

    private static final String PREFIX = "token:";

    public String generateAndSave(String username) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, username, expirySeconds, TimeUnit.SECONDS);
        log.info("Token saved for user: {}", username);
        return token;
    }

    public String validateToken(String token) {
        String username = redisTemplate.opsForValue().get(PREFIX + token);
        if (username == null) {
            log.warn("Token not found or expired: {}", token);
        }
        return username;
    }

    public void deleteToken(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
