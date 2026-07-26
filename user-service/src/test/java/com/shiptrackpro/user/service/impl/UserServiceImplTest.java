package com.shiptrackpro.user.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    @InjectMocks
    private UserServiceImpl userService;

    private Role customerRole;
    private Role adminRole;
    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        customerRole = Role.builder().name("CUSTOMER").build();
        adminRole = Role.builder().name("ADMIN").build();

        testUser = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("$2a$12$hashedpassword")
                .authProvider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .accountNonLocked(true)
                .roles(Set.of(customerRole))
                .build();
        // BaseEntity fields set via reflection in real usage; mock mapper handles it
    }

    // ==================== existsByEmail ====================

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("returns true when email exists")
        void returnsTrueWhenExists() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
            assertThat(userService.existsByEmail("john@example.com")).isTrue();
        }

        @Test
        @DisplayName("returns false when email does not exist")
        void returnsFalseWhenNotExists() {
            when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);
            assertThat(userService.existsByEmail("unknown@example.com")).isFalse();
        }
    }

    // ==================== createUser ====================

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("creates user with CUSTOMER role by default")
        void createsWithDefaultRole() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .passwordHash("$2a$12$hash")
                    .build();

            InternalUserResponse expectedResponse = InternalUserResponse.builder()
                    .name("John Doe").email("john@example.com").build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toInternalUserResponse(any(User.class))).thenReturn(expectedResponse);

            InternalUserResponse result = userService.createUser(request);

            assertThat(result.getName()).isEqualTo("John Doe");
            verify(roleRepository).findByName("CUSTOMER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("creates user with specified ADMIN role")
        void createsWithSpecifiedRole() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .passwordHash("$2a$12$hash")
                    .role("ADMIN")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toInternalUserResponse(any(User.class)))
                    .thenReturn(InternalUserResponse.builder().name("Admin User").build());

            userService.createUser(request);
            verify(roleRepository).findByName("ADMIN");
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email exists")
        void throwsOnDuplicateEmail() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John").email("john@example.com").passwordHash("hash").build();

            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown role")
        void throwsOnUnknownRole() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John").email("john@example.com").passwordHash("hash").role("DRIVER").build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByName("DRIVER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void normalizesEmail() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John").email("JOHN@Example.COM").passwordHash("hash").build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toInternalUserResponse(any(User.class)))
                    .thenReturn(InternalUserResponse.builder().build());

            userService.createUser(request);

            verify(userRepository).save(argThat(user ->
                    user.getEmail().equals("john@example.com")));
        }
    }

    // ==================== findByEmail ====================

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            InternalUserResponse expected = InternalUserResponse.builder()
                    .email("john@example.com").build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
            when(userMapper.toInternalUserResponse(testUser)).thenReturn(expected);

            InternalUserResponse result = userService.findByEmail("john@example.com");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByEmail("unknown@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== findById ====================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            InternalUserResponse expected = InternalUserResponse.builder()
                    .id(testUserId).build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toInternalUserResponse(testUser)).thenReturn(expected);

            InternalUserResponse result = userService.findById(testUserId);
            assertThat(result.getId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(testUserId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== enableUser ====================

    @Nested
    @DisplayName("enableUser")
    class EnableUser {

        @Test
        @DisplayName("sets enabled and emailVerified to true")
        void enablesUser() {
            testUser.setEnabled(false);
            testUser.setEmailVerified(false);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.enableUser(testUserId);

            assertThat(testUser.isEnabled()).isTrue();
            assertThat(testUser.isEmailVerified()).isTrue();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenNotFound() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.enableUser(testUserId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== updatePassword ====================

    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("updates password hash")
        void updatesPasswordHash() {
            UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                    .passwordHash("$2a$12$newhash").build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.updatePassword(testUserId, request);

            assertThat(testUser.getPasswordHash()).isEqualTo("$2a$12$newhash");
            verify(userRepository).save(testUser);
        }
    }

    // ==================== lockUser ====================

    @Nested
    @DisplayName("lockUser")
    class LockUser {

        @Test
        @DisplayName("sets accountNonLocked to false")
        void locksUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.lockUser(testUserId);

            assertThat(testUser.isAccountNonLocked()).isFalse();
            verify(userRepository).save(testUser);
        }
    }

    // ==================== getProfile ====================

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("returns user profile")
        void returnsProfile() {
            UserResponse expected = UserResponse.builder()
                    .name("John Doe").email("john@example.com").build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(expected);

            UserResponse result = userService.getProfile(testUserId);
            assertThat(result.getName()).isEqualTo("John Doe");
        }
    }

    // ==================== updateProfile ====================

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates name and phone")
        void updatesNameAndPhone() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("Jane Doe").phone("+1234567890").build();
            UserResponse expected = UserResponse.builder()
                    .name("Jane Doe").build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

            UserResponse result = userService.updateProfile(testUserId, request);

            assertThat(testUser.getName()).isEqualTo("Jane Doe");
            assertThat(testUser.getPhone()).isEqualTo("+1234567890");
        }

        @Test
        @DisplayName("updates only provided fields")
        void updatesOnlyProvidedFields() {
            testUser.setName("Original Name");
            testUser.setPhone("+1111111111");
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("New Name").build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class)))
                    .thenReturn(UserResponse.builder().build());

            userService.updateProfile(testUserId, request);

            assertThat(testUser.getName()).isEqualTo("New Name");
            assertThat(testUser.getPhone()).isEqualTo("+1111111111");
        }
    }

    // ==================== uploadAvatar ====================

    @Nested
    @DisplayName("uploadAvatar")
    class UploadAvatar {

        @Test
        @DisplayName("rejects non-image file")
        void rejectsNonImage() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("application/pdf");
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.uploadAvatar(testUserId, file))
                    .isInstanceOf(ShipTrackException.class)
                    .hasMessageContaining("Only image files are allowed");
        }

        @Test
        @DisplayName("uploads image and updates avatar URL")
        void uploadsImage() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("image/png");
            when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

            Map<String, Object> uploadResult = Map.of("secure_url", "https://res.cloudinary.com/avatar.png");
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class)))
                    .thenReturn(UserResponse.builder().avatarUrl("https://res.cloudinary.com/avatar.png").build());

            UserResponse result = userService.uploadAvatar(testUserId, file);

            assertThat(testUser.getAvatarUrl()).isEqualTo("https://res.cloudinary.com/avatar.png");
            assertThat(result.getAvatarUrl()).isEqualTo("https://res.cloudinary.com/avatar.png");
        }
    }

    // ==================== getAllUsers ====================

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("returns paginated users")
        void returnsPaginatedUsers() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
            UserResponse response = UserResponse.builder().name("John Doe").build();

            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.toUserResponse(any(User.class))).thenReturn(response);

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("John Doe");
        }
    }

    // ==================== adminUpdateUser ====================

    @Nested
    @DisplayName("adminUpdateUser")
    class AdminUpdateUser {

        @Test
        @DisplayName("updates role")
        void updatesRole() {
            AdminUpdateUserRequest request = AdminUpdateUserRequest.builder()
                    .role("ADMIN").build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class)))
                    .thenReturn(UserResponse.builder().build());

            userService.adminUpdateUser(testUserId, request);

            assertThat(testUser.getRoles()).containsExactly(adminRole);
        }

        @Test
        @DisplayName("updates enabled and lock status")
        void updatesEnabledAndLock() {
            AdminUpdateUserRequest request = AdminUpdateUserRequest.builder()
                    .enabled(false).accountNonLocked(false).build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class)))
                    .thenReturn(UserResponse.builder().build());

            userService.adminUpdateUser(testUserId, request);

            assertThat(testUser.isEnabled()).isFalse();
            assertThat(testUser.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("throws for unknown role")
        void throwsForUnknownRole() {
            AdminUpdateUserRequest request = AdminUpdateUserRequest.builder()
                    .role("DRIVER").build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("DRIVER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.adminUpdateUser(testUserId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
