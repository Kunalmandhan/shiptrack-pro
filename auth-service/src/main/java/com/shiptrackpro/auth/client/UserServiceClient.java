package com.shiptrackpro.auth.client;

import com.shiptrackpro.auth.dto.response.UserDTO;
import com.shiptrackpro.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST client for User Service internal APIs.
 * Auth Service never owns the users table — it delegates to User Service.
 *
 * Calls are made directly (localhost:8082), NOT through the Gateway,
 * since /internal/** endpoints are blocked at the Gateway level.
 */
@Slf4j
@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${services.user-service-url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    /**
     * Check if a user exists by email.
     */
    public boolean existsByEmail(String email) {
        try {
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    userServiceUrl + "/internal/users/exists?email={email}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    email
            );
            return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getData());
        } catch (Exception e) {
            log.error("Failed to check user existence: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable", e);
        }
    }

    /**
     * Create a new user via User Service.
     */
    public UserDTO createUser(String name, String email, String passwordHash, String role) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("passwordHash", passwordHash);
        body.put("role", role);

        try {
            ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.exchange(
                    userServiceUrl + "/internal/users",
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody().getData() : null;
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable", e);
        }
    }

    /**
     * Find user by email (includes passwordHash for login validation).
     */
    public Optional<UserDTO> findByEmail(String email) {
        try {
            ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.exchange(
                    userServiceUrl + "/internal/users/by-email?email={email}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    email
            );
            return Optional.ofNullable(response.getBody())
                    .map(ApiResponse::getData);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to find user by email: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable", e);
        }
    }

    /**
     * Find user by ID.
     */
    public Optional<UserDTO> findById(String userId) {
        try {
            ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.exchange(
                    userServiceUrl + "/internal/users/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    userId
            );
            return Optional.ofNullable(response.getBody())
                    .map(ApiResponse::getData);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to find user by ID: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable", e);
        }
    }

    /**
     * Enable user account (after email verification).
     */
    public void enableUser(String userId) {
        try {
            restTemplate.put(
                    userServiceUrl + "/internal/users/{id}/enable",
                    null,
                    userId
            );
            log.info("Enabled user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to enable user: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable", e);
        }
    }

    /**
     * Update user password hash.
     */
    public void updatePassword(String userId, String newPasswordHash) {
        Map<String, String> body = Map.of("passwordHash", newPasswordHash);
        try {
            restTemplate.put(
                    userServiceUrl + "/internal/users/{id}/password",
                    body,
                    userId
            );
        } catch (Exception e) {
            log.error("Failed to update password for user: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable", e);
        }
    }

    /**
     * Lock user account (after too many failed login attempts).
     */
    public void lockUser(String userId) {
        try {
            restTemplate.put(
                    userServiceUrl + "/internal/users/{id}/lock",
                    null,
                    userId
            );
            log.warn("Locked user account: {}", userId);
        } catch (Exception e) {
            log.error("Failed to lock user: {}", e.getMessage());
        }
    }
}
