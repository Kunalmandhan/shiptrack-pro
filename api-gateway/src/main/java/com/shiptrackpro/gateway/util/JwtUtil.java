package com.shiptrackpro.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * JWT utility for the API Gateway.
 * Only loads the PUBLIC key — the gateway verifies tokens but never signs them.
 * Signing is exclusively done by the Auth Service using the private key.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try (InputStream is = publicKeyResource.getInputStream()) {
            String keyContent = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(spec);
            log.info("RSA public key loaded successfully for JWT validation");
        } catch (Exception e) {
            log.error("Failed to load RSA public key: {}", e.getMessage());
            throw new RuntimeException("Cannot start gateway without JWT public key", e);
        }
    }

    /**
     * Parse and validate the JWT token.
     * Returns claims if valid, throws JwtException variants if not.
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the JTI (JWT ID) for blacklist checking.
     * Uses a lenient parse that doesn't validate expiration —
     * we need the JTI even for expired tokens to check blacklist.
     */
    public String extractJti(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getId();
        } catch (ExpiredJwtException e) {
            // Even expired tokens have a JTI we can extract
            return e.getClaims().getId();
        }
    }

    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
