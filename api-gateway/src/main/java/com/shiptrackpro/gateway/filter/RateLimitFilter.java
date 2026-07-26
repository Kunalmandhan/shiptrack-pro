package com.shiptrackpro.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * Rate limiting filter using Redis sliding window counter.
 *
 * Algorithm:
 *   - Key: rate:{clientIP}
 *   - On each request: INCR the key
 *   - If count == 1 (new key): set TTL to 60 seconds
 *   - If count > limit: reject with 429
 *
 * This is a simple fixed-window approach (resets every 60s).
 * For a true sliding window, we'd use Redis sorted sets — but this is
 * sufficient for our project and easier to debug.
 *
 * Order: 0 (runs before JWT filter)
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    private static final String RATE_KEY_PREFIX = "rate:";

    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress().getHostAddress();
        String key = RATE_KEY_PREFIX + clientIp;

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // First request in this window — set the 60s TTL
                        return redisTemplate.expire(key, Duration.ofSeconds(60))
                                .then(chain.filter(exchange));
                    }

                    if (count > requestsPerMinute) {
                        log.warn("Rate limit exceeded for IP: {} | Count: {}", clientIp, count);

                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                        exchange.getResponse().getHeaders().add("Retry-After", "60");

                        String body = String.format(
                                "{\"success\":false,\"message\":\"Rate limit exceeded. Try again later.\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"path\":\"%s\"}",
                                exchange.getRequest().getURI().getPath()
                        );

                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                        );
                    }

                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return 0; // Runs first — no point validating JWT if rate-limited
    }
}
