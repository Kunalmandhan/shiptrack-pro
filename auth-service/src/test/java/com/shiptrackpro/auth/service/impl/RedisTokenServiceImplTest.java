package com.shiptrackpro.auth.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisTokenServiceImpl.
 * All Redis interactions are mocked via StringRedisTemplate.
 */
@ExtendWith(MockitoExtension.class)
class RedisTokenServiceImplTest {

    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private SetOperations<String, String> setOps;

    private RedisTokenServiceImpl redisTokenService;

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440010";
    private static final String TOKEN = "d290f1ee-6c54-4b01-90e6-d701748f0851";

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
        lenient().when(redis.opsForSet()).thenReturn(setOps);
        redisTokenService = new RedisTokenServiceImpl(redis);
    }

    // ==================== Refresh Tokens ====================

    @Nested
    @DisplayName("Refresh Token Operations")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should save refresh token with TTL and track user tokens")
        void saveRefreshToken_shouldStoreInRedis() {
            redisTokenService.saveRefreshToken(TOKEN, USER_ID, Duration.ofDays(7));

            verify(valueOps).set("refresh:" + TOKEN, USER_ID, Duration.ofDays(7));
            verify(setOps).add("user_tokens:" + USER_ID, TOKEN);
            verify(redis).expire("user_tokens:" + USER_ID, Duration.ofDays(7));
        }

        @Test
        @DisplayName("Should return userId when refresh token is valid")
        void validateRefreshToken_withValidToken_shouldReturnUserId() {
            when(valueOps.get("refresh:" + TOKEN)).thenReturn(USER_ID);

            Optional<String> result = redisTokenService.validateRefreshToken(TOKEN);

            assertThat(result).isPresent().contains(USER_ID);
        }

        @Test
        @DisplayName("Should return empty when refresh token is invalid")
        void validateRefreshToken_withInvalidToken_shouldReturnEmpty() {
            when(valueOps.get("refresh:invalid")).thenReturn(null);

            Optional<String> result = redisTokenService.validateRefreshToken("invalid");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should delete refresh token and remove from user set")
        void deleteRefreshToken_shouldRemoveFromRedis() {
            when(valueOps.get("refresh:" + TOKEN)).thenReturn(USER_ID);

            redisTokenService.deleteRefreshToken(TOKEN);

            verify(redis).delete("refresh:" + TOKEN);
            verify(setOps).remove("user_tokens:" + USER_ID, TOKEN);
        }

        @Test
        @DisplayName("Should delete all refresh tokens for a user")
        void deleteAllRefreshTokensForUser_shouldClearAllTokens() {
            Set<String> tokens = Set.of("token1", "token2", "token3");
            when(setOps.members("user_tokens:" + USER_ID)).thenReturn(tokens);

            redisTokenService.deleteAllRefreshTokensForUser(USER_ID);

            verify(redis).delete("refresh:token1");
            verify(redis).delete("refresh:token2");
            verify(redis).delete("refresh:token3");
            verify(redis).delete("user_tokens:" + USER_ID);
        }
    }

    // ==================== Blacklist ====================

    @Nested
    @DisplayName("Access Token Blacklist")
    class BlacklistTests {

        @Test
        @DisplayName("Should blacklist access token JTI with TTL")
        void blacklistAccessToken_shouldSetInRedis() {
            redisTokenService.blacklistAccessToken("jti123", Duration.ofMinutes(10));

            verify(valueOps).set("blacklist:jti123", "true", Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("Should not store blacklist with zero/negative TTL")
        void blacklistAccessToken_withZeroTtl_shouldSkip() {
            redisTokenService.blacklistAccessToken("jti123", Duration.ZERO);

            verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("Should detect blacklisted token")
        void isBlacklisted_withBlacklistedJti_shouldReturnTrue() {
            when(redis.hasKey("blacklist:jti123")).thenReturn(true);

            assertThat(redisTokenService.isBlacklisted("jti123")).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-blacklisted token")
        void isBlacklisted_withValidJti_shouldReturnFalse() {
            when(redis.hasKey("blacklist:valid-jti")).thenReturn(false);

            assertThat(redisTokenService.isBlacklisted("valid-jti")).isFalse();
        }
    }

    // ==================== Email Verification ====================

    @Nested
    @DisplayName("Email Verification Token Operations")
    class VerificationTests {

        @Test
        @DisplayName("Should save verification token")
        void saveVerificationToken_shouldStoreInRedis() {
            redisTokenService.saveVerificationToken(TOKEN, USER_ID, Duration.ofHours(24));

            verify(valueOps).set("verify:" + TOKEN, USER_ID, Duration.ofHours(24));
        }

        @Test
        @DisplayName("Should validate verification token")
        void validateVerificationToken_withValid_shouldReturnUserId() {
            when(valueOps.get("verify:" + TOKEN)).thenReturn(USER_ID);

            assertThat(redisTokenService.validateVerificationToken(TOKEN))
                    .isPresent().contains(USER_ID);
        }

        @Test
        @DisplayName("Should delete verification token")
        void deleteVerificationToken_shouldRemove() {
            redisTokenService.deleteVerificationToken(TOKEN);

            verify(redis).delete("verify:" + TOKEN);
        }
    }

    // ==================== Password Reset ====================

    @Nested
    @DisplayName("Password Reset Token Operations")
    class PasswordResetTests {

        @Test
        @DisplayName("Should save password reset token")
        void savePasswordResetToken_shouldStoreInRedis() {
            redisTokenService.savePasswordResetToken(TOKEN, USER_ID, Duration.ofMinutes(60));

            verify(valueOps).set("reset:" + TOKEN, USER_ID, Duration.ofMinutes(60));
        }

        @Test
        @DisplayName("Should validate password reset token")
        void validatePasswordResetToken_withValid_shouldReturnUserId() {
            when(valueOps.get("reset:" + TOKEN)).thenReturn(USER_ID);

            assertThat(redisTokenService.validatePasswordResetToken(TOKEN))
                    .isPresent().contains(USER_ID);
        }

        @Test
        @DisplayName("Should delete password reset token")
        void deletePasswordResetToken_shouldRemove() {
            redisTokenService.deletePasswordResetToken(TOKEN);

            verify(redis).delete("reset:" + TOKEN);
        }
    }

    // ==================== Login Attempts ====================

    @Nested
    @DisplayName("Login Attempt Tracking")
    class LoginAttemptTests {

        @Test
        @DisplayName("Should increment login attempts and set TTL on first attempt")
        void incrementLoginAttempts_firstAttempt_shouldSetTtl() {
            when(valueOps.increment("login_attempts:test@test.com")).thenReturn(1L);

            long count = redisTokenService.incrementLoginAttempts("test@test.com");

            assertThat(count).isEqualTo(1L);
            verify(redis).expire("login_attempts:test@test.com", Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("Should increment without resetting TTL on subsequent attempts")
        void incrementLoginAttempts_subsequentAttempt_shouldNotResetTtl() {
            when(valueOps.increment("login_attempts:test@test.com")).thenReturn(3L);

            long count = redisTokenService.incrementLoginAttempts("test@test.com");

            assertThat(count).isEqualTo(3L);
            verify(redis, never()).expire(anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("Should reset login attempts by deleting the key")
        void resetLoginAttempts_shouldDeleteKey() {
            redisTokenService.resetLoginAttempts("test@test.com");

            verify(redis).delete("login_attempts:test@test.com");
        }

        @Test
        @DisplayName("Should return 0 when no login attempts recorded")
        void getLoginAttempts_withNoAttempts_shouldReturnZero() {
            when(valueOps.get("login_attempts:test@test.com")).thenReturn(null);

            assertThat(redisTokenService.getLoginAttempts("test@test.com")).isZero();
        }

        @Test
        @DisplayName("Should return stored attempt count")
        void getLoginAttempts_withAttempts_shouldReturnCount() {
            when(valueOps.get("login_attempts:test@test.com")).thenReturn("4");

            assertThat(redisTokenService.getLoginAttempts("test@test.com")).isEqualTo(4L);
        }
    }
}
