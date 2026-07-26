package com.shiptrackpro.auth.service;

import com.shiptrackpro.auth.dto.request.*;
import com.shiptrackpro.auth.dto.response.AuthResponse;
import com.shiptrackpro.common.dto.ApiResponse;

/**
 * Core authentication service contract.
 */
public interface AuthService {

    ApiResponse<String> register(RegisterRequest request);

    ApiResponse<AuthResponse> login(LoginRequest request);

    ApiResponse<AuthResponse> refreshToken(RefreshTokenRequest request);

    ApiResponse<String> logout(String accessToken, LogoutRequest request, String userId);

    ApiResponse<String> verifyEmail(String token);

    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);

    ApiResponse<String> resetPassword(ResetPasswordRequest request);

    ApiResponse<AuthResponse> googleOAuth(GoogleOAuthRequest request);
}
