package com.shiptrackpro.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * Tests token generation, parsing, and JTI extraction with real RSA keys.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");
    private static final String ROLE = "ADMIN";
    private static final String EMAIL = "admin@shiptrackpro.com";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // Set fields via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(jwtTokenProvider, "privateKeyResource",
                new ClassPathResource("keys/private.pem"));
        ReflectionTestUtils.setField(jwtTokenProvider, "publicKeyResource",
                new ClassPathResource("keys/public.pem"));
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiryMinutes", 15);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiryDays", 7);

        // Trigger @PostConstruct
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Should generate valid access token with correct claims")
    void generateAccessToken_shouldContainCorrectClaims() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);

        assertThat(token).isNotBlank();

        Claims claims = jwtTokenProvider.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
        assertThat(claims.get("role", String.class)).isEqualTo(ROLE);
        assertThat(claims.get("email", String.class)).isEqualTo(EMAIL);
        assertThat(claims.getId()).isNotBlank(); // JTI exists
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Should generate unique JTI for each token")
    void generateAccessToken_shouldHaveUniqueJti() {
        String token1 = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);
        String token2 = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);

        String jti1 = jwtTokenProvider.extractJti(token1);
        String jti2 = jwtTokenProvider.extractJti(token2);

        assertThat(jti1).isNotEqualTo(jti2);
    }

    @Test
    @DisplayName("Should generate cryptographically random refresh token")
    void generateRefreshToken_shouldBeRandomUUID() {
        String token1 = jwtTokenProvider.generateRefreshToken();
        String token2 = jwtTokenProvider.generateRefreshToken();

        assertThat(token1).isNotBlank();
        assertThat(token2).isNotBlank();
        assertThat(token1).isNotEqualTo(token2);
        // Should be valid UUID format
        assertThatCode(() -> UUID.fromString(token1)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should extract JTI from valid token")
    void extractJti_shouldReturnTokenId() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);
        String jti = jwtTokenProvider.extractJti(token);

        assertThat(jti).isNotBlank();
        assertThatCode(() -> UUID.fromString(jti)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return correct access token expiry in seconds")
    void getAccessTokenExpirySeconds_shouldReturn900() {
        long expirySeconds = jwtTokenProvider.getAccessTokenExpirySeconds();
        assertThat(expirySeconds).isEqualTo(900L); // 15 minutes
    }

    @Test
    @DisplayName("Should return correct refresh token expiry duration")
    void getRefreshTokenExpiry_shouldReturn7Days() {
        Duration expiry = jwtTokenProvider.getRefreshTokenExpiry();
        assertThat(expiry).isEqualTo(Duration.ofDays(7));
    }

    @Test
    @DisplayName("Should calculate positive remaining TTL for fresh token")
    void getRemainingTtl_forFreshToken_shouldBePositive() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);
        Duration remaining = jwtTokenProvider.getRemainingTtl(token);

        assertThat(remaining).isPositive();
        // Should be close to 15 minutes (within 1 second tolerance)
        assertThat(remaining.toSeconds()).isBetween(899L, 900L);
    }

    @Test
    @DisplayName("Should parse token and verify RS256 signature")
    void parseToken_withValidToken_shouldReturnClaims() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);
        Claims claims = jwtTokenProvider.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
    }

    @Test
    @DisplayName("Should throw when parsing tampered token")
    void parseToken_withTamperedToken_shouldThrow() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, ROLE, EMAIL);
        // Tamper with the payload (change a character)
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtTokenProvider.parseToken(tampered))
                .isInstanceOf(Exception.class);
    }
}
