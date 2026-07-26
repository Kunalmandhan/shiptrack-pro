package com.shiptrackpro.user.service;

import com.shiptrackpro.user.dto.request.AdminUpdateUserRequest;
import com.shiptrackpro.user.dto.request.CreateUserRequest;
import com.shiptrackpro.user.dto.request.UpdatePasswordRequest;
import com.shiptrackpro.user.dto.request.UpdateProfileRequest;
import com.shiptrackpro.user.dto.response.InternalUserResponse;
import com.shiptrackpro.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

    // ==================== Internal (called by Auth Service) ====================

    boolean existsByEmail(String email);

    InternalUserResponse createUser(CreateUserRequest request);

    InternalUserResponse findByEmail(String email);

    InternalUserResponse findById(UUID id);

    void enableUser(UUID id);

    void updatePassword(UUID id, UpdatePasswordRequest request);

    void lockUser(UUID id);

    // ==================== Public (called via API Gateway) ====================

    UserResponse getProfile(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);

    UserResponse uploadAvatar(UUID userId, MultipartFile file);

    UserResponse getUserById(UUID id);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse adminUpdateUser(UUID id, AdminUpdateUserRequest request);
}
