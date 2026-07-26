package com.shiptrackpro.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.user.config.SecurityConfig;
import com.shiptrackpro.user.dto.request.CreateUserRequest;
import com.shiptrackpro.user.dto.request.UpdatePasswordRequest;
import com.shiptrackpro.user.dto.response.InternalUserResponse;
import com.shiptrackpro.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalUserController.class)
@Import(SecurityConfig.class)
class InternalUserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private UserService userService;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    @DisplayName("GET /internal/users/exists — returns true when email exists")
    void existsByEmail_returnsTrue() throws Exception {
        when(userService.existsByEmail("john@example.com")).thenReturn(true);

        mockMvc.perform(get("/internal/users/exists")
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("GET /internal/users/exists — returns false when email doesn't exist")
    void existsByEmail_returnsFalse() throws Exception {
        when(userService.existsByEmail("unknown@example.com")).thenReturn(false);

        mockMvc.perform(get("/internal/users/exists")
                        .param("email", "unknown@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("POST /internal/users — creates user and returns 201")
    void createUser_returns201() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("$2a$12$hashedpassword")
                .role("CUSTOMER")
                .build();

        InternalUserResponse response = InternalUserResponse.builder()
                .id(testUserId)
                .name("John Doe")
                .email("john@example.com")
                .roles(List.of("CUSTOMER"))
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /internal/users — validation error on missing name")
    void createUser_validationError() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("john@example.com")
                .passwordHash("hash")
                .build();

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /internal/users/by-email — returns user with passwordHash")
    void findByEmail_returnsUser() throws Exception {
        InternalUserResponse response = InternalUserResponse.builder()
                .id(testUserId)
                .email("john@example.com")
                .passwordHash("$2a$12$hash")
                .roles(List.of("CUSTOMER"))
                .build();

        when(userService.findByEmail("john@example.com")).thenReturn(response);

        mockMvc.perform(get("/internal/users/by-email")
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.passwordHash").value("$2a$12$hash"));
    }

    @Test
    @DisplayName("GET /internal/users/{id} — returns user by ID")
    void findById_returnsUser() throws Exception {
        InternalUserResponse response = InternalUserResponse.builder()
                .id(testUserId).name("John Doe").build();

        when(userService.findById(testUserId)).thenReturn(response);

        mockMvc.perform(get("/internal/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    @DisplayName("PUT /internal/users/{id}/enable — returns 200")
    void enableUser_returns200() throws Exception {
        doNothing().when(userService).enableUser(testUserId);

        mockMvc.perform(put("/internal/users/{id}/enable", testUserId))
                .andExpect(status().isOk());

        verify(userService).enableUser(testUserId);
    }

    @Test
    @DisplayName("PUT /internal/users/{id}/password — updates password hash")
    void updatePassword_returns200() throws Exception {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .passwordHash("$2a$12$newhash").build();

        doNothing().when(userService).updatePassword(eq(testUserId), any(UpdatePasswordRequest.class));

        mockMvc.perform(put("/internal/users/{id}/password", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updatePassword(eq(testUserId), any(UpdatePasswordRequest.class));
    }

    @Test
    @DisplayName("PUT /internal/users/{id}/lock — locks user account")
    void lockUser_returns200() throws Exception {
        doNothing().when(userService).lockUser(testUserId);

        mockMvc.perform(put("/internal/users/{id}/lock", testUserId))
                .andExpect(status().isOk());

        verify(userService).lockUser(testUserId);
    }
}
