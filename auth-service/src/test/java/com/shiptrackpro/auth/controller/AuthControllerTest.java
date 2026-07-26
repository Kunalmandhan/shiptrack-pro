package com.shiptrackpro.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.auth.dto.request.*;
import com.shiptrackpro.auth.dto.response.AuthResponse;
import com.shiptrackpro.auth.service.AuthService;
import com.shiptrackpro.common.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests for AuthController using MockMvc.
 * Tests HTTP layer (status codes, content type, request validation)
 * without starting the full application context.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters for unit tests
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;

    private static final String BASE_URL = "/api/v1/auth";

    // ==================== REGISTER ====================

    @Nested
    @DisplayName("POST /register")
    class RegisterEndpoint {

        @Test
        @DisplayName("Should return 201 Created on successful registration")
        void register_withValidRequest_shouldReturn201() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User")
                    .email("test@test.com")
                    .password("StrongPass1!")
                    .build();

            when(authService.register(any())).thenReturn(
                    ApiResponse.success("Registration successful. Please check your email."));

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful. Please check your email."));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void register_withBlankName_shouldReturn400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("").email("test@test.com").password("StrongPass1!").build();

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void register_withInvalidEmail_shouldReturn400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test").email("not-an-email").password("StrongPass1!").build();

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is too weak")
        void register_withWeakPassword_shouldReturn400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test").email("test@test.com").password("weak").build();

            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== LOGIN ====================

    @Nested
    @DisplayName("POST /login")
    class LoginEndpoint {

        @Test
        @DisplayName("Should return 200 with token pair on successful login")
        void login_withValidCredentials_shouldReturn200() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("test@test.com").password("StrongPass1!").build();

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken("eyJ...").refreshToken("ref-token").expiresIn(900L).build();

            when(authService.login(any())).thenReturn(ApiResponse.success(authResponse));

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("eyJ..."))
                    .andExpect(jsonPath("$.data.refreshToken").value("ref-token"))
                    .andExpect(jsonPath("$.data.expiresIn").value(900));
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void login_withBlankEmail_shouldReturn400() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("").password("pass").build();

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== REFRESH ====================

    @Nested
    @DisplayName("POST /refresh")
    class RefreshEndpoint {

        @Test
        @DisplayName("Should return 200 with new token pair")
        void refreshToken_withValidToken_shouldReturn200() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("old-refresh-token").build();

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken("new-access").refreshToken("new-refresh").expiresIn(900L).build();

            when(authService.refreshToken(any())).thenReturn(ApiResponse.success(authResponse));

            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("new-access"));
        }

        @Test
        @DisplayName("Should return 400 when refresh token is blank")
        void refreshToken_withBlankToken_shouldReturn400() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("").build();

            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== LOGOUT ====================

    @Nested
    @DisplayName("POST /logout")
    class LogoutEndpoint {

        @Test
        @DisplayName("Should return 200 on successful logout")
        void logout_shouldReturn200() throws Exception {
            when(authService.logout(any(), any(), any()))
                    .thenReturn(ApiResponse.success("Logged out successfully"));

            mockMvc.perform(post(BASE_URL + "/logout")
                            .header("Authorization", "Bearer some-token")
                            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440010")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"ref-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ==================== VERIFY EMAIL ====================

    @Nested
    @DisplayName("GET /verify-email")
    class VerifyEmailEndpoint {

        @Test
        @DisplayName("Should return 200 on valid verification token")
        void verifyEmail_withValidToken_shouldReturn200() throws Exception {
            when(authService.verifyEmail("abc123"))
                    .thenReturn(ApiResponse.success("Email verified successfully"));

            mockMvc.perform(get(BASE_URL + "/verify-email")
                            .param("token", "abc123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email verified successfully"));
        }
    }

    // ==================== FORGOT PASSWORD ====================

    @Nested
    @DisplayName("POST /forgot-password")
    class ForgotPasswordEndpoint {

        @Test
        @DisplayName("Should return 200 regardless of whether user exists")
        void forgotPassword_shouldReturn200() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("test@test.com").build();

            when(authService.forgotPassword(any())).thenReturn(
                    ApiResponse.success("If an account exists, a reset link has been sent."));

            mockMvc.perform(post(BASE_URL + "/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ==================== RESET PASSWORD ====================

    @Nested
    @DisplayName("POST /reset-password")
    class ResetPasswordEndpoint {

        @Test
        @DisplayName("Should return 200 on successful password reset")
        void resetPassword_withValidData_shouldReturn200() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("reset-token").newPassword("NewPass1!").build();

            when(authService.resetPassword(any()))
                    .thenReturn(ApiResponse.success("Password reset successfully"));

            mockMvc.perform(post(BASE_URL + "/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 when reset token is blank")
        void resetPassword_withBlankToken_shouldReturn400() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("").newPassword("NewPass1!").build();

            mockMvc.perform(post(BASE_URL + "/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== GOOGLE OAUTH ====================

    @Nested
    @DisplayName("POST /oauth2/google")
    class GoogleOAuthEndpoint {

        @Test
        @DisplayName("Should return 200 with token pair on successful OAuth")
        void googleOAuth_shouldReturn200() throws Exception {
            GoogleOAuthRequest request = GoogleOAuthRequest.builder()
                    .code("google-auth-code").build();

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken("oauth-access").refreshToken("oauth-refresh").expiresIn(900L).build();

            when(authService.googleOAuth(any())).thenReturn(ApiResponse.success(authResponse));

            mockMvc.perform(post(BASE_URL + "/oauth2/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("oauth-access"));
        }

        @Test
        @DisplayName("Should return 400 when code is blank")
        void googleOAuth_withBlankCode_shouldReturn400() throws Exception {
            GoogleOAuthRequest request = GoogleOAuthRequest.builder().code("").build();

            mockMvc.perform(post(BASE_URL + "/oauth2/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
