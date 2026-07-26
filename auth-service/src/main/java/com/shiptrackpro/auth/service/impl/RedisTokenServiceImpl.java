package com.shiptrackpro.auth.service.impl;

import com.shiptrackpro.auth.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    private final StringRedisTemplate redis;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String VERIFY_PREFIX = "verify:";
    private static final String RESET_PREFIX = "reset:";
    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";

    // ========== Refresh Token ==========

    @Override
    public void saveRefreshToken(String token, String userId, Duration ttl) {
        redis.opsForValue().set(REFRESH_PREFIX + token, userId, ttl);
        // Track which tokens belong to this user (for "logout all" scenarios)
        redis.opsForSet().add(USER_TOKENS_PREFIX + userId, token);
        redis.expire(USER_TOKENS_PREFIX + userId, ttl);
        log.debug("Saved refresh token for user: {}", userId);
    }

    @Override
    public Optional<String> validateRefreshToken(String token) {
        String userId = redis.opsForValue().get(REFRESH_PREFIX + token);
        return Optional.ofNullable(userId);
    }

    @Override
    public void deleteRefreshToken(String token) {
        String userId = redis.opsForValue().get(REFRESH_PREFIX + token);
        redis.delete(REFRESH_PREFIX + token);
        if (userId != null) {
            redis.opsForSet().remove(USER_TOKENS_PREFIX + userId, token);
        }
    }

    @Override
    public void deleteAllRefreshTokensForUser(String userId) {
        Set<String> tokens = redis.opsForSet().members(USER_TOKENS_PREFIX + userId);
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(token -> redis.delete(REFRESH_PREFIX + token));
            redis.delete(USER_TOKENS_PREFIX + userId);
            log.info("Deleted all refresh tokens for user: {} | Count: {}", userId, tokens.size());
        }
    }

    // ========== Access Token Blacklist ==========

    @Override
    public void blacklistAccessToken(String jti, Duration ttl) {
        if (ttl.isPositive()) {
            redis.opsForValue().set(BLACKLIST_PREFIX + jti, "true", ttl);
            log.debug("Blacklisted access token JTI: {} | TTL: {}s", jti, ttl.toSeconds());
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + jti));
    }

    // ========== Email Verification ==========

    @Override
    public void saveVerificationToken(String token, String userId, Duration ttl) {
        redis.opsForValue().set(VERIFY_PREFIX + token, userId, ttl);
    }

    @Override
    public Optional<String> validateVerificationToken(String token) {
        return Optional.ofNullable(redis.opsForValue().get(VERIFY_PREFIX + token));
    }

    @Override
    public void deleteVerificationToken(String token) {
        redis.delete(VERIFY_PREFIX + token);
    }

    // ========== Password Reset ==========

    @Override
    public void savePasswordResetToken(String token, String userId, Duration ttl) {
        redis.opsForValue().set(RESET_PREFIX + token, userId, ttl);
    }

    @Override
    public Optional<String> validatePasswordResetToken(String token) {
        return Optional.ofNullable(redis.opsForValue().get(RESET_PREFIX + token));
    }

    @Override
    public void deletePasswordResetToken(String token) {
        redis.delete(RESET_PREFIX + token);
    }

    // ========== Login Attempts ==========

    @Override
    public long incrementLoginAttempts(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(30));
        }
        return count != null ? count : 0;
    }

    @Override
    public void resetLoginAttempts(String email) {
        redis.delete(LOGIN_ATTEMPTS_PREFIX + email);
    }

    @Override
    public long getLoginAttempts(String email) {
        String val = redis.opsForValue().get(LOGIN_ATTEMPTS_PREFIX + email);
        return val != null ? Long.parseLong(val) : 0;
    }
}
