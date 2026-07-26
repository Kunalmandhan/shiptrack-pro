package com.shiptrackpro.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating a user's password hash via Auth Service internal call.
 * Matches the body sent by UserServiceClient.updatePassword().
 *
 * The hash is already BCrypt-encoded by Auth Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank(message = "Password hash is required")
    private String passwordHash;
}
