package com.shiptrackpro.auth.service.impl;

import com.shiptrackpro.auth.client.UserServiceClient;
import com.shiptrackpro.auth.dto.request.*;
import com.shiptrackpro.auth.dto.response.AuthResponse;
import com.shiptrackpro.auth.dto.response.UserDTO;
import com.shiptrackpro.auth.entity.AuditLog;
import com.shiptrackpro.auth.enums.AuditAction;
import com.shiptrackpro.auth.repository.AuditLogRepository;
import com.shiptrackpro.auth.security.JwtTokenProvider;
import com.shiptrackpro.auth.service.AuthService;
import com.shiptrackpro.auth.service.EmailService;
import com.shiptrackpro.auth.service.RedisTokenService;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final EmailService emailService;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.email-verification-expiry-hours:24}")
    private int emailVerificationExpiryHours;

    @Value("${app.password-reset-expiry-minutes:60}")
    private int passwordResetExpiryMinutes;

    // ==================== REGISTER ====================

    @Override
    public ApiResponse<String> register(RegisterRequest request) {
        // Check if email already exists
        if (userServiceClient.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Default role to CUSTOMER
        String role = request.getRole() != null ? request.getRole() : "CUSTOMER";

        // Create user via User Service
        UserDTO user = userServiceClient.createUser(
                request.getName(),
                request.getEmail(),
                passwordHash,
                role
        );

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        redisTokenService.saveVerificationToken(
                verificationToken,
                user.getId().toString(),
                Duration.ofHours(emailVerificationExpiryHours)
        );

        // Send verification email (async)
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);

        // Audit
        audit(user.getId(), AuditAction.REGISTER, null);

        log.info("User registered: {} | Role: {}", user.getEmail(), role);

        return ApiResponse.success(
                "Registration successful. Please check your email to verify your account.",
                HttpStatus.CREATED
        );
    }

    // ==================== LOGIN ====================

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        // Check login attempts
        long attempts = redisTokenService.getLoginAttempts(request.getEmail());
        if (attempts >= maxLoginAttempts) {
            throw new ShipTrackException(
                    "Account locked due to too many failed attempts. Try again after 30 minutes.",
                    "ACCOUNT_LOCKED",
                    HttpStatus.LOCKED
            );
        }

        // Find user
        UserDTO user = userServiceClient.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    redisTokenService.incrementLoginAttempts(request.getEmail());
                    return new ShipTrackException("Invalid email or password", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
                });

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            long newAttempts = redisTokenService.incrementLoginAttempts(request.getEmail());

            // Lock account if max attempts reached
            if (newAttempts >= maxLoginAttempts) {
                userServiceClient.lockUser(user.getId().toString());
                audit(user.getId(), AuditAction.ACCOUNT_LOCKED, null);
            }

            audit(user.getId(), AuditAction.LOGIN_FAILED,
                    Map.of("attempt", newAttempts, "max", maxLoginAttempts));

            throw new ShipTrackException("Invalid email or password", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new ShipTrackException(
                    "Please verify your email before logging in",
                    "EMAIL_NOT_VERIFIED",
                    HttpStatus.FORBIDDEN
            );
        }

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new ShipTrackException(
                    "Account is locked. Contact support.",
                    "ACCOUNT_LOCKED",
                    HttpStatus.LOCKED
            );
        }

        // Check if account is enabled
        if (!user.isEnabled()) {
            throw new ShipTrackException(
                    "Account is disabled. Contact support.",
                    "ACCOUNT_DISABLED",
                    HttpStatus.FORBIDDEN
            );
        }

        // Reset login attempts on success
        redisTokenService.resetLoginAttempts(request.getEmail());

        // Generate tokens
        AuthResponse authResponse = generateTokenPair(user);

        // Audit
        audit(user.getId(), AuditAction.LOGIN, null);

        log.info("User logged in: {}", user.getEmail());

        return ApiResponse.success(authResponse);
    }

    // ==================== REFRESH TOKEN ====================

    @Override
    public ApiResponse<AuthResponse> refreshToken(RefreshTokenRequest request) {
        // Validate refresh token exists in Redis
        String userId = redisTokenService.validateRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new ShipTrackException(
                        "Invalid or expired refresh token",
                        "INVALID_REFRESH_TOKEN",
                        HttpStatus.UNAUTHORIZED
                ));

        // Fetch user data
        UserDTO user = userServiceClient.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Delete old refresh token (rotation — each refresh token is single-use)
        redisTokenService.deleteRefreshToken(request.getRefreshToken());

        // Generate new token pair
        AuthResponse authResponse = generateTokenPair(user);

        // Audit
        audit(user.getId(), AuditAction.TOKEN_REFRESH, null);

        return ApiResponse.success(authResponse);
    }

    // ==================== LOGOUT ====================

    @Override
    public ApiResponse<String> logout(String accessToken, LogoutRequest request, String userId) {
        // Blacklist the access token (for remaining TTL)
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);
            try {
                String jti = jwtTokenProvider.extractJti(token);
                Duration remainingTtl = jwtTokenProvider.getRemainingTtl(token);
                redisTokenService.blacklistAccessToken(jti, remainingTtl);
            } catch (Exception e) {
                log.warn("Could not blacklist access token: {}", e.getMessage());
            }
        }

        // Delete refresh token
        if (request != null && request.getRefreshToken() != null) {
            redisTokenService.deleteRefreshToken(request.getRefreshToken());
        }

        // Audit
        if (userId != null) {
            audit(UUID.fromString(userId), AuditAction.LOGOUT, null);
        }

        log.info("User logged out: {}", userId);

        return ApiResponse.success("Logged out successfully");
    }

    // ==================== VERIFY EMAIL ====================

    @Override
    public ApiResponse<String> verifyEmail(String token) {
        String userId = redisTokenService.validateVerificationToken(token)
                .orElseThrow(() -> new ShipTrackException(
                        "Invalid or expired verification link",
                        "INVALID_VERIFICATION_TOKEN",
                        HttpStatus.BAD_REQUEST
                ));

        // Enable user account
        userServiceClient.enableUser(userId);

        // Delete the token (one-time use)
        redisTokenService.deleteVerificationToken(token);

        // Audit
        audit(UUID.fromString(userId), AuditAction.EMAIL_VERIFIED, null);

        log.info("Email verified for user: {}", userId);

        return ApiResponse.success("Email verified successfully. You can now log in.");
    }

    // ==================== FORGOT PASSWORD ====================

    @Override
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        // Always return success to prevent email enumeration
        userServiceClient.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            redisTokenService.savePasswordResetToken(
                    resetToken,
                    user.getId().toString(),
                    Duration.ofMinutes(passwordResetExpiryMinutes)
            );
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetToken);
            audit(user.getId(), AuditAction.PASSWORD_RESET_REQUEST, null);
        });

        return ApiResponse.success(
                "If an account exists with that email, a password reset link has been sent."
        );
    }

    // ==================== RESET PASSWORD ====================

    @Override
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        String userId = redisTokenService.validatePasswordResetToken(request.getToken())
                .orElseThrow(() -> new ShipTrackException(
                        "Invalid or expired reset link",
                        "INVALID_RESET_TOKEN",
                        HttpStatus.BAD_REQUEST
                ));

        // Hash new password
        String passwordHash = passwordEncoder.encode(request.getNewPassword());

        // Update via User Service
        userServiceClient.updatePassword(userId, passwordHash);

        // Delete the token (one-time use)
        redisTokenService.deletePasswordResetToken(request.getToken());

        // Invalidate all existing sessions
        redisTokenService.deleteAllRefreshTokensForUser(userId);

        // Audit
        audit(UUID.fromString(userId), AuditAction.PASSWORD_RESET, null);

        log.info("Password reset for user: {}", userId);

        return ApiResponse.success("Password reset successfully. Please log in with your new password.");
    }

    // ==================== GOOGLE OAUTH2 ====================

    @Override
    public ApiResponse<AuthResponse> googleOAuth(GoogleOAuthRequest request) {
        // TODO: Implement Google OAuth2 token exchange in Step 19
        // 1. Exchange authorization code for Google access token
        // 2. Fetch user info from Google
        // 3. Check if user exists → login
        // 4. If not → auto-create as CUSTOMER (skip email verification)
        // 5. Generate token pair
        throw new ShipTrackException(
                "Google OAuth2 will be implemented in Step 19",
                "NOT_IMPLEMENTED",
                HttpStatus.NOT_IMPLEMENTED
        );
    }

    // ==================== HELPERS ====================

    private AuthResponse generateTokenPair(UserDTO user) {
        String primaryRole = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().get(0)
                : "CUSTOMER";

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), primaryRole, user.getEmail()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken();
        redisTokenService.saveRefreshToken(
                refreshToken,
                user.getId().toString(),
                jwtTokenProvider.getRefreshTokenExpiry()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .build();
    }

    private void audit(UUID userId, AuditAction action, Map<String, Object> newValue) {
        try {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType("AUTH")
                    .entityId(userId)
                    .newValue(newValue)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Audit failures should not break auth flow
            AuthServiceImpl.log.warn("Failed to save audit log: {}", e.getMessage());
        }
    }
}
