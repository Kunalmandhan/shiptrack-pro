package com.shiptrackpro.user.controller;

import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.user.dto.request.CreateUserRequest;
import com.shiptrackpro.user.dto.request.UpdatePasswordRequest;
import com.shiptrackpro.user.dto.response.InternalUserResponse;
import com.shiptrackpro.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal user endpoints called directly by Auth Service.
 * These endpoints are NOT routed through the API Gateway — they are
 * only reachable within the Docker network on port 8082.
 *
 * No Swagger documentation since these are internal-only.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    /**
     * Check if a user exists by email.
     * Called by: UserServiceClient.existsByEmail()
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Email check complete", exists, "/internal/users/exists"));
    }

    /**
     * Create a new user.
     * Called by: UserServiceClient.createUser()
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InternalUserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        InternalUserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user, "/internal/users"));
    }

    /**
     * Find user by email (includes passwordHash for login validation).
     * Called by: UserServiceClient.findByEmail()
     */
    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<InternalUserResponse>> findByEmail(@RequestParam String email) {
        InternalUserResponse user = userService.findByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User found", user, "/internal/users/by-email"));
    }

    /**
     * Find user by ID (includes passwordHash).
     * Called by: UserServiceClient.findById()
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> findById(@PathVariable UUID id) {
        InternalUserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("User found", user, "/internal/users/" + id));
    }

    /**
     * Enable user account (after email verification).
     * Called by: UserServiceClient.enableUser()
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable UUID id) {
        userService.enableUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Update user password hash.
     * Called by: UserServiceClient.updatePassword()
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(id, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Lock user account (after too many failed login attempts).
     * Called by: UserServiceClient.lockUser()
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable UUID id) {
        userService.lockUser(id);
        return ResponseEntity.ok().build();
    }
}
