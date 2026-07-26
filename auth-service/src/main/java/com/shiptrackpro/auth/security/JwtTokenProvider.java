package com.shiptrackpro.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider — signs and validates tokens using RS256 key pair.
 *
 * This is the ONLY place in the entire system that creates JWTs.
 * The Gateway only validates (public key), but this class SIGNS (private key).
 *
 * Token structure:
 *   sub  = userId (UUID)
 *   role = user's primary role
 *   email = user's email
 *   jti  = unique token identifier (for blacklisting)
 *   iat  = issued at
 *   exp  = expiration
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiry-minutes:15}")
    private int accessTokenExpiryMinutes;

    @Value("${jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
        log.info("JWT key pair loaded successfully | Access token TTL: {}min | Refresh token TTL: {}d",
                accessTokenExpiryMinutes, refreshTokenExpiryDays);
    }

    /**
     * Generate a signed JWT access token.
     */
    public String generateAccessToken(UUID userId, String role, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofMinutes(accessTokenExpiryMinutes).toMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("email", email)
                .id(UUID.randomUUID().toString()) // JTI for blacklisting
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    /**
     * Generate a cryptographically secure refresh token.
     * This is NOT a JWT — just a random UUID stored in Redis.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Parse and validate an access token. Returns claims if valid.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the JTI (token ID) from a token for blacklisting.
     */
    public String extractJti(String token) {
        return parseToken(token).getId();
    }

    /**
     * Get access token expiry duration in seconds (for response).
     */
    public long getAccessTokenExpirySeconds() {
        return Duration.ofMinutes(accessTokenExpiryMinutes).toSeconds();
    }

    /**
     * Get refresh token expiry duration (for Redis TTL).
     */
    public Duration getRefreshTokenExpiry() {
        return Duration.ofDays(refreshTokenExpiryDays);
    }

    /**
     * Get remaining TTL of an access token (for blacklist TTL after logout).
     */
    public Duration getRemainingTtl(String token) {
        Date expiry = parseToken(token).getExpiration();
        long remaining = expiry.getTime() - System.currentTimeMillis();
        return remaining > 0 ? Duration.ofMillis(remaining) : Duration.ZERO;
    }

    private PrivateKey loadPrivateKey() {
        try (InputStream is = privateKeyResource.getInputStream()) {
            String keyContent = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA private key", e);
        }
    }

    private PublicKey loadPublicKey() {
        try (InputStream is = publicKeyResource.getInputStream()) {
            String keyContent = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA public key", e);
        }
    }
}
