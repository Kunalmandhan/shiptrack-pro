package com.shiptrackpro.gateway.filter;

import com.shiptrackpro.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global filter that validates JWT on every request (except public endpoints).
 *
 * Flow:
 *   1. Check if endpoint is public → skip validation
 *   2. Extract Bearer token from Authorization header
 *   3. Validate JWT signature + expiration (RS256 public key)
 *   4. Check Redis blacklist for revoked tokens
 *   5. Inject X-User-Id, X-User-Role, X-User-Email headers
 *   6. Forward to downstream service
 *
 * Order: 1 (runs after rate limit filter)
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /** Endpoints that bypass JWT validation entirely */
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/oauth2",
            "/api/v1/shipments/track/",
            "/ws/",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Validate JWT
        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT for path: {}", path);
            return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            log.warn("Invalid JWT for path: {} | Reason: {}", path, e.getMessage());
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Check token blacklist in Redis
        String jti = claims.getId();
        if (jti == null) {
            log.warn("JWT missing JTI claim for path: {}", path);
            return onError(exchange, "Invalid token: missing identifier", HttpStatus.UNAUTHORIZED);
        }

        return redisTemplate.hasKey("blacklist:" + jti)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("Blacklisted token used for path: {} | JTI: {}", path, jti);
                        return onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
                    }

                    // Inject trusted headers for downstream services
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", jwtUtil.extractUserId(claims))
                            .header("X-User-Role", jwtUtil.extractRole(claims))
                            .header("X-User-Email", jwtUtil.extractEmail(claims))
                            .build();

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        return 1; // After rate limiting (0), before forwarding
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"success\":false,\"message\":\"%s\",\"errorCode\":\"AUTHENTICATION_FAILED\",\"path\":\"%s\"}",
                message, exchange.getRequest().getURI().getPath()
        );

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
}
