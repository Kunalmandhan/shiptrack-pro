package com.shiptrackpro.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.user.config.SecurityConfig;
import com.shiptrackpro.user.dto.request.AdminUpdateUserRequest;
import com.shiptrackpro.user.dto.request.UpdateProfileRequest;
import com.shiptrackpro.user.dto.response.UserResponse;
import com.shiptrackpro.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private UserService userService;

    private final UUID testUserId = UUID.randomUUID();

    // ==================== /api/v1/users/me ====================

    @Test
    @DisplayName("GET /me — returns own profile")
    void getMyProfile_returnsProfile() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(testUserId).name("John Doe").email("john@example.com")
                .roles(List.of("CUSTOMER")).build();

        when(userService.getProfile(testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .header(AppConstants.HEADER_USER_ID, testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("PUT /me — updates profile")
    void updateMyProfile_updatesNameAndPhone() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Jane Doe").phone("+1234567890").build();
        UserResponse response = UserResponse.builder()
                .id(testUserId).name("Jane Doe").build();

        when(userService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me")
                        .header(AppConstants.HEADER_USER_ID, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("PUT /me/avatar — uploads avatar")
    void uploadAvatar_uploadsImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        UserResponse response = UserResponse.builder()
                .id(testUserId).avatarUrl("https://cloudinary.com/avatar.png").build();

        when(userService.uploadAvatar(eq(testUserId), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header(AppConstants.HEADER_USER_ID, testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatarUrl").value("https://cloudinary.com/avatar.png"));
    }

    // ==================== Admin Endpoints ====================

    @Test
    @DisplayName("GET /{id} — admin gets any user")
    void getUserById_adminAccess() throws Exception {
        UUID targetId = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(targetId).name("Target User").build();

        when(userService.getUserById(targetId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/{id}", targetId)
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Target User"));
    }

    @Test
    @DisplayName("GET /{id} — non-admin gets 403")
    void getUserById_nonAdminDenied() throws Exception {
        UUID targetId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/{id}", targetId)
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET / — admin lists all users paginated")
    void getAllUsers_adminAccess() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(testUserId).name("John Doe").build();
        Page<UserResponse> page = new PageImpl<>(List.of(user));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET / — non-admin gets 403")
    void getAllUsers_nonAdminDenied() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /{id} — admin updates user role/status")
    void adminUpdateUser_adminAccess() throws Exception {
        UUID targetId = UUID.randomUUID();
        AdminUpdateUserRequest request = AdminUpdateUserRequest.builder()
                .role("ADMIN").enabled(true).build();
        UserResponse response = UserResponse.builder()
                .id(targetId).roles(List.of("ADMIN")).build();

        when(userService.adminUpdateUser(eq(targetId), any(AdminUpdateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/users/{id}", targetId)
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /{id} — non-admin gets 403")
    void adminUpdateUser_nonAdminDenied() throws Exception {
        UUID targetId = UUID.randomUUID();
        AdminUpdateUserRequest request = AdminUpdateUserRequest.builder()
                .role("ADMIN").build();

        mockMvc.perform(put("/api/v1/users/{id}", targetId)
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /me — missing X-User-Id header returns 400")
    void getMyProfile_missingHeader() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isBadRequest());
    }
}
