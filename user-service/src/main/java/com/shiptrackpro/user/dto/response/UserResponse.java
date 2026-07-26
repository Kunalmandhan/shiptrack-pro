package com.shiptrackpro.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Public-facing user response DTO.
 * Returned by /api/v1/users/** endpoints.
 *
 * NEVER contains passwordHash — that field is internal only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private List<String> roles;
    private boolean enabled;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
