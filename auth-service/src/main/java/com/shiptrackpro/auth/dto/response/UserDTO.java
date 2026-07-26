package com.shiptrackpro.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * User data returned from the User Service via internal API.
 * Used by Auth Service for login validation and token generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

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
