package com.shiptrackpro.auth.service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis operations contract for token management.
 *
 * Key patterns used:
 *   refresh:{token}          → userId        (TTL: 7 days)
 *   blacklist:{jti}          → "true"        (TTL: remaining access token life)
 *   verify:{token}           → userId        (TTL: 24 hours)
 *   reset:{token}            → userId        (TTL: 60 minutes)
 *   login_attempts:{email}   → count         (TTL: 30 minutes)
 */
public interface RedisTokenService {

    // Refresh token operations
    void saveRefreshToken(String token, String userId, Duration ttl);
    Optional<String> validateRefreshToken(String token);
    void deleteRefreshToken(String token);
    void deleteAllRefreshTokensForUser(String userId);

    // Access token blacklist
    void blacklistAccessToken(String jti, Duration ttl);
    boolean isBlacklisted(String jti);

    // Email verification
    void saveVerificationToken(String token, String userId, Duration ttl);
    Optional<String> validateVerificationToken(String token);
    void deleteVerificationToken(String token);

    // Password reset
    void savePasswordResetToken(String token, String userId, Duration ttl);
    Optional<String> validatePasswordResetToken(String token);
    void deletePasswordResetToken(String token);

    // Login attempt tracking
    long incrementLoginAttempts(String email);
    void resetLoginAttempts(String email);
    long getLoginAttempts(String email);
}
