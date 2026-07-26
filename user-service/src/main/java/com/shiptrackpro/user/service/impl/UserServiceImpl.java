package com.shiptrackpro.user.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.user.dto.request.AdminUpdateUserRequest;
import com.shiptrackpro.user.dto.request.CreateUserRequest;
import com.shiptrackpro.user.dto.request.UpdatePasswordRequest;
import com.shiptrackpro.user.dto.request.UpdateProfileRequest;
import com.shiptrackpro.user.dto.response.InternalUserResponse;
import com.shiptrackpro.user.dto.response.UserResponse;
import com.shiptrackpro.user.entity.Role;
import com.shiptrackpro.user.entity.User;
import com.shiptrackpro.user.mapper.UserMapper;
import com.shiptrackpro.user.repository.RoleRepository;
import com.shiptrackpro.user.repository.UserRepository;
import com.shiptrackpro.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final Cloudinary cloudinary;

    // ==================== Internal (Auth Service Contracts) ====================

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public InternalUserResponse createUser(CreateUserRequest request) {
        // Double-check for race conditions (Auth Service also checks)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Resolve role — default to CUSTOMER
        String roleName = request.getRole() != null ? request.getRole().toUpperCase() : "CUSTOMER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(request.getPasswordHash())
                .authProvider("LOCAL")
                .enabled(false)
                .emailVerified(false)
                .accountNonLocked(true)
                .roles(Set.of(role))
                .build();

        User saved = userRepository.save(user);
        log.info("Created user: {} | Role: {}", saved.getEmail(), roleName);

        return userMapper.toInternalUserResponse(saved);
    }

    @Override
    public InternalUserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toInternalUserResponse(user);
    }

    @Override
    public InternalUserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toInternalUserResponse(user);
    }

    @Override
    @Transactional
    public void enableUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setEnabled(true);
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Enabled user: {}", id);
    }

    @Override
    @Transactional
    public void updatePassword(UUID id, UpdatePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setPasswordHash(request.getPasswordHash());
        userRepository.save(user);
        log.info("Password updated for user: {}", id);
    }

    @Override
    @Transactional
    public void lockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setAccountNonLocked(false);
        userRepository.save(user);
        log.warn("Locked user account: {}", id);
    }

    // ==================== Public (API Gateway) ====================

    @Override
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ShipTrackException(
                    "Only image files are allowed",
                    "INVALID_FILE_TYPE",
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "shiptrack-pro/avatars",
                            "public_id", userId.toString(),
                            "overwrite", true,
                            "resource_type", "image",
                            "transformation", "w_200,h_200,c_fill,g_face"
                    )
            );

            String avatarUrl = (String) uploadResult.get("secure_url");
            user.setAvatarUrl(avatarUrl);
            User saved = userRepository.save(user);

            log.info("Avatar uploaded for user: {} | URL: {}", userId, avatarUrl);
            return userMapper.toUserResponse(saved);
        } catch (IOException e) {
            log.error("Failed to upload avatar for user: {}", userId, e);
            throw new ShipTrackException(
                    "Failed to upload avatar. Please try again.",
                    "UPLOAD_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e
            );
        }
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toUserResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUser(UUID id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRole()));
            user.setRoles(Set.of(role));
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
        }

        User saved = userRepository.save(user);
        log.info("Admin updated user: {} | Changes: role={}, enabled={}, locked={}",
                id, request.getRole(), request.getEnabled(), request.getAccountNonLocked());
        return userMapper.toUserResponse(saved);
    }
}
