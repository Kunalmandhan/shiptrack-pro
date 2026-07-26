package com.shiptrackpro.auth.service.impl;

import com.shiptrackpro.auth.client.UserServiceClient;
import com.shiptrackpro.auth.dto.request.*;
import com.shiptrackpro.auth.dto.response.AuthResponse;
import com.shiptrackpro.auth.dto.response.UserDTO;
import com.shiptrackpro.auth.repository.AuditLogRepository;
import com.shiptrackpro.auth.security.JwtTokenProvider;
import com.shiptrackpro.auth.service.EmailService;
import com.shiptrackpro.auth.service.RedisTokenService;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Covers all 8 auth service methods with mocked dependencies.
 *
 * Test naming: methodName_givenCondition_expectedResult
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RedisTokenService redisTokenService;
    @Mock private EmailService emailService;
    @Mock private UserServiceClient userServiceClient;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");
    private static final String EMAIL = "test@shiptrackpro.com";
    private static final String PASSWORD = "StrongPass1!";
    private static final String PASSWORD_HASH = "$2a$12$hashedpassword";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.access";
    private static final String REFRESH_TOKEN = "d290f1ee-6c54-4b01-90e6-d701748f0851";
    private static final String JTI = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authService, "emailVerificationExpiryHours", 24);
        ReflectionTestUtils.setField(authService, "passwordResetExpiryMinutes", 60);
    }

    private UserDTO buildUser(boolean emailVerified, boolean enabled, boolean accountNonLocked) {
        return UserDTO.builder()
                .id(USER_ID)
                .name("Test User")
                .email(EMAIL)
                .passwordHash(PASSWORD_HASH)
                .roles(List.of("CUSTOMER"))
                .emailVerified(emailVerified)
                .enabled(enabled)
                .accountNonLocked(accountNonLocked)
                .build();
    }

    // ==================== REGISTER ====================

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void register_withValidRequest_shouldReturnCreated() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User").email(EMAIL).password(PASSWORD).build();

            UserDTO user = buildUser(false, false, true);

            when(userServiceClient.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
            when(userServiceClient.createUser("Test User", EMAIL, PASSWORD_HASH, "CUSTOMER"))
                    .thenReturn(user);

            ApiResponse<String> response = authService.register(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("verify your email");
            verify(redisTokenService).saveVerificationToken(anyString(), eq(USER_ID.toString()), eq(Duration.ofHours(24)));
            verify(emailService).sendVerificationEmail(eq(EMAIL), eq("Test User"), anyString());
            verify(auditLogRepository).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email exists")
        void register_withExistingEmail_shouldThrowDuplicate() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User").email(EMAIL).password(PASSWORD).build();

            when(userServiceClient.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            verify(userServiceClient, never()).createUser(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should default role to CUSTOMER when not provided")
        void register_withNoRole_shouldDefaultToCustomer() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User").email(EMAIL).password(PASSWORD).build();

            UserDTO user = buildUser(false, false, true);
            when(userServiceClient.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
            when(userServiceClient.createUser("Test User", EMAIL, PASSWORD_HASH, "CUSTOMER"))
                    .thenReturn(user);

            authService.register(request);

            verify(userServiceClient).createUser("Test User", EMAIL, PASSWORD_HASH, "CUSTOMER");
        }
    }

    // ==================== LOGIN ====================

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_withValidCredentials_shouldReturnTokens() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();
            UserDTO user = buildUser(true, true, true);

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(0L);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(USER_ID, "CUSTOMER", EMAIL)).thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.generateRefreshToken()).thenReturn(REFRESH_TOKEN);
            when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(Duration.ofDays(7));
            when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);

            ApiResponse<AuthResponse> response = authService.login(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getData().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.getData().getExpiresIn()).isEqualTo(900L);
            verify(redisTokenService).resetLoginAttempts(EMAIL);
        }

        @Test
        @DisplayName("Should throw LOCKED when too many failed attempts")
        void login_withLockedAccount_shouldThrowLocked() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(5L);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.LOCKED);
        }

        @Test
        @DisplayName("Should increment attempts and throw on wrong password")
        void login_withWrongPassword_shouldIncrementAndThrow() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password("wrong").build();
            UserDTO user = buildUser(true, true, true);

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(0L);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", PASSWORD_HASH)).thenReturn(false);
            when(redisTokenService.incrementLoginAttempts(EMAIL)).thenReturn(1L);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);

            verify(redisTokenService).incrementLoginAttempts(EMAIL);
        }

        @Test
        @DisplayName("Should lock account after 5th failed attempt")
        void login_withFifthFailedAttempt_shouldLockAccount() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password("wrong").build();
            UserDTO user = buildUser(true, true, true);

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(4L);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", PASSWORD_HASH)).thenReturn(false);
            when(redisTokenService.incrementLoginAttempts(EMAIL)).thenReturn(5L);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ShipTrackException.class);

            verify(userServiceClient).lockUser(USER_ID.toString());
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when email not verified")
        void login_withUnverifiedEmail_shouldThrowForbidden() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();
            UserDTO user = buildUser(false, true, true);

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(0L);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "EMAIL_NOT_VERIFIED")
                    .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should throw when account is disabled")
        void login_withDisabledAccount_shouldThrowForbidden() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();
            UserDTO user = buildUser(true, false, true);

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(0L);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "ACCOUNT_DISABLED");
        }

        @Test
        @DisplayName("Should increment attempts when user not found")
        void login_withNonExistentUser_shouldIncrementAndThrow() {
            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();

            when(redisTokenService.getLoginAttempts(EMAIL)).thenReturn(0L);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);

            verify(redisTokenService).incrementLoginAttempts(EMAIL);
        }
    }

    // ==================== REFRESH TOKEN ====================

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should rotate refresh token and return new pair")
        void refreshToken_withValidToken_shouldReturnNewPair() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(REFRESH_TOKEN).build();
            UserDTO user = buildUser(true, true, true);

            when(redisTokenService.validateRefreshToken(REFRESH_TOKEN))
                    .thenReturn(Optional.of(USER_ID.toString()));
            when(userServiceClient.findById(USER_ID.toString())).thenReturn(Optional.of(user));
            when(jwtTokenProvider.generateAccessToken(USER_ID, "CUSTOMER", EMAIL)).thenReturn("new-access");
            when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh");
            when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(Duration.ofDays(7));
            when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);

            ApiResponse<AuthResponse> response = authService.refreshToken(request);

            assertThat(response.getData().getAccessToken()).isEqualTo("new-access");
            assertThat(response.getData().getRefreshToken()).isEqualTo("new-refresh");
            verify(redisTokenService).deleteRefreshToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("Should throw when refresh token is invalid")
        void refreshToken_withInvalidToken_shouldThrow() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid-token").build();

            when(redisTokenService.validateRefreshToken("invalid-token"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should throw when user not found after valid refresh token")
        void refreshToken_withDeletedUser_shouldThrow() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(REFRESH_TOKEN).build();

            when(redisTokenService.validateRefreshToken(REFRESH_TOKEN))
                    .thenReturn(Optional.of(USER_ID.toString()));
            when(userServiceClient.findById(USER_ID.toString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== LOGOUT ====================

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("Should blacklist access token and delete refresh token")
        void logout_withBothTokens_shouldBlacklistAndDelete() {
            LogoutRequest request = LogoutRequest.builder().refreshToken(REFRESH_TOKEN).build();

            when(jwtTokenProvider.extractJti(ACCESS_TOKEN)).thenReturn(JTI);
            when(jwtTokenProvider.getRemainingTtl(ACCESS_TOKEN)).thenReturn(Duration.ofMinutes(10));

            ApiResponse<String> response = authService.logout(
                    "Bearer " + ACCESS_TOKEN, request, USER_ID.toString());

            assertThat(response.isSuccess()).isTrue();
            verify(redisTokenService).blacklistAccessToken(JTI, Duration.ofMinutes(10));
            verify(redisTokenService).deleteRefreshToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("Should handle null tokens gracefully (idempotent)")
        void logout_withNullTokens_shouldSucceed() {
            ApiResponse<String> response = authService.logout(null, null, null);

            assertThat(response.isSuccess()).isTrue();
            verify(redisTokenService, never()).blacklistAccessToken(any(), any());
            verify(redisTokenService, never()).deleteRefreshToken(any());
        }
    }

    // ==================== VERIFY EMAIL ====================

    @Nested
    @DisplayName("Verify Email")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email and enable user account")
        void verifyEmail_withValidToken_shouldEnableUser() {
            when(redisTokenService.validateVerificationToken("verify-token"))
                    .thenReturn(Optional.of(USER_ID.toString()));

            ApiResponse<String> response = authService.verifyEmail("verify-token");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("verified");
            verify(userServiceClient).enableUser(USER_ID.toString());
            verify(redisTokenService).deleteVerificationToken("verify-token");
        }

        @Test
        @DisplayName("Should throw when verification token is expired/invalid")
        void verifyEmail_withInvalidToken_shouldThrow() {
            when(redisTokenService.validateVerificationToken("expired"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verifyEmail("expired"))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_VERIFICATION_TOKEN")
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        }
    }

    // ==================== FORGOT PASSWORD ====================

    @Nested
    @DisplayName("Forgot Password")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should send reset email when user exists")
        void forgotPassword_withExistingUser_shouldSendEmail() {
            UserDTO user = buildUser(true, true, true);
            when(userServiceClient.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            ForgotPasswordRequest request = ForgotPasswordRequest.builder().email(EMAIL).build();
            ApiResponse<String> response = authService.forgotPassword(request);

            assertThat(response.isSuccess()).isTrue();
            verify(redisTokenService).savePasswordResetToken(anyString(), eq(USER_ID.toString()), eq(Duration.ofMinutes(60)));
            verify(emailService).sendPasswordResetEmail(eq(EMAIL), eq("Test User"), anyString());
        }

        @Test
        @DisplayName("Should return success even when user does not exist (prevent enumeration)")
        void forgotPassword_withNonExistentUser_shouldStillReturnSuccess() {
            when(userServiceClient.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("unknown@test.com").build();
            ApiResponse<String> response = authService.forgotPassword(request);

            assertThat(response.isSuccess()).isTrue();
            verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
        }
    }

    // ==================== RESET PASSWORD ====================

    @Nested
    @DisplayName("Reset Password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password and invalidate all sessions")
        void resetPassword_withValidToken_shouldUpdateAndInvalidate() {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("reset-token").newPassword("NewPass1!").build();

            when(redisTokenService.validatePasswordResetToken("reset-token"))
                    .thenReturn(Optional.of(USER_ID.toString()));
            when(passwordEncoder.encode("NewPass1!")).thenReturn("new-hash");

            ApiResponse<String> response = authService.resetPassword(request);

            assertThat(response.isSuccess()).isTrue();
            verify(userServiceClient).updatePassword(USER_ID.toString(), "new-hash");
            verify(redisTokenService).deletePasswordResetToken("reset-token");
            verify(redisTokenService).deleteAllRefreshTokensForUser(USER_ID.toString());
        }

        @Test
        @DisplayName("Should throw when reset token is expired/invalid")
        void resetPassword_withInvalidToken_shouldThrow() {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("invalid").newPassword("NewPass1!").build();

            when(redisTokenService.validatePasswordResetToken("invalid"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_RESET_TOKEN");
        }
    }

    // ==================== GOOGLE OAUTH ====================

    @Nested
    @DisplayName("Google OAuth")
    class GoogleOAuthTests {

        @Test
        @DisplayName("Should throw NOT_IMPLEMENTED (deferred to Step 19)")
        void googleOAuth_shouldThrowNotImplemented() {
            GoogleOAuthRequest request = GoogleOAuthRequest.builder().code("google-code").build();

            assertThatThrownBy(() -> authService.googleOAuth(request))
                    .isInstanceOf(ShipTrackException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_IMPLEMENTED);
        }
    }
}
