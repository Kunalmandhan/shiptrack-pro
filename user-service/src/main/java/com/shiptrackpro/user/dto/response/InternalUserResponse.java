package com.shiptrackpro.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Internal user response DTO returned by /internal/users/** endpoints.
 * Includes passwordHash for Auth Service login validation.
 *
 * Shape matches Auth Service's UserDTO exactly:
 * - id, name, email, passwordHash, roles, enabled, emailVerified,
 *   accountNonLocked, authProvider, providerId
 *
 * This DTO is NEVER exposed through the API Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserResponse {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private List<String> roles;
    private boolean enabled;
    private boolean emailVerified;
    private boolean accountNonLocked;
    private String authProvider;
    private String providerId;
}
