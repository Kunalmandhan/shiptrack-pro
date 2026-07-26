package com.shiptrackpro.user.controller;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.user.dto.request.AdminUpdateUserRequest;
import com.shiptrackpro.user.dto.request.UpdateProfileRequest;
import com.shiptrackpro.user.dto.response.UserResponse;
import com.shiptrackpro.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Public user endpoints routed through API Gateway.
 *
 * Authentication is handled by the Gateway's JwtAuthFilter,
 * which injects X-User-Id, X-User-Role, and X-User-Email headers.
 *
 * Role-based authorization is checked in-controller using the
 * X-User-Role header rather than Spring Security roles, because
 * User Service has no JWT token to parse — it trusts the Gateway headers.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management and admin operations")
public class UserController {

    private final UserService userService;

    // ==================== Authenticated User Endpoints ====================

    @GetMapping("/me")
    @Operation(summary = "Get own profile", description = "Returns the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId) {
        UserResponse profile = userService.getProfile(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile, "/api/v1/users/me"));
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile", description = "Update name and/or phone number")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse profile = userService.updateProfile(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile, "/api/v1/users/me"));
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar", description = "Upload profile picture (max 5MB, image only)")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestParam("file") MultipartFile file) {
        UserResponse profile = userService.uploadAvatar(UUID.fromString(userId), file);
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded", profile, "/api/v1/users/me/avatar"));
    }

    // ==================== Admin Endpoints ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (Admin)", description = "Admin-only: Get any user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id) {
        requireAdmin(role);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User found", user, "/api/v1/users/" + id));
    }

    @GetMapping
    @Operation(summary = "List all users (Admin)", description = "Admin-only: Paginated list of all users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        requireAdmin(role);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users, "/api/v1/users"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user (Admin)", description = "Admin-only: Update role, enabled status, or lock state")
    public ResponseEntity<ApiResponse<UserResponse>> adminUpdateUser(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        requireAdmin(role);
        UserResponse user = userService.adminUpdateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated", user, "/api/v1/users/" + id));
    }

    // ==================== Authorization Helper ====================

    private void requireAdmin(String role) {
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new ShipTrackException(
                    "Admin access required",
                    "ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }
    }
}
