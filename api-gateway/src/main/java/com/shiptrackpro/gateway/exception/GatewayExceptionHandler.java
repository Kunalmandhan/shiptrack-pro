package com.shiptrackpro.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the API Gateway (WebFlux-based).
 *
 * Handles gateway-level errors that occur BEFORE reaching a downstream service:
 *   - Service unreachable (ConnectException)
 *   - Route not found (ResponseStatusException)
 *   - Any unhandled exception
 *
 * Returns standard JSON error responses consistent with our API contract.
 *
 * Note: This is NOT @ControllerAdvice (that's WebMVC).
 * For WebFlux, we implement ErrorWebExceptionHandler.
 */
@Slf4j
@Component
@Order(-2) // Must be before the default Spring error handler (-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getURI().getPath();

        HttpStatus status;
        String message;
        String errorCode;

        if (ex instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable. Please try again later.";
            errorCode = "SERVICE_UNAVAILABLE";
            log.error("Service unreachable for path: {} | Error: {}", path, ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : "Request could not be processed";
            errorCode = "GATEWAY_ERROR";
            log.warn("ResponseStatusException for path: {} | Status: {} | Reason: {}",
                    path, status.value(), message);

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected gateway error occurred";
            errorCode = "GATEWAY_INTERNAL_ERROR";
            log.error("Unhandled gateway exception for path: {}", path, ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("errorCode", errorCode);
        body.put("path", path);
        body.put("timestamp", LocalDateTime.now().toString());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{\"success\":false,\"message\":\"Gateway error\"}".getBytes();
        }

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}
