import api, { setAccessToken, clearAccessToken, setRefreshToken, getRefreshToken, clearRefreshToken } from './api';

/**
 * Auth Service API calls.
 * Handles registration, login, logout, token refresh, OAuth2, password management.
 */
const authService = {
  register: (data) =>
    api.post('/auth/register', data),

  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    if (response.data.success) {
      setAccessToken(response.data.data.accessToken);
      if (response.data.data.refreshToken) {
        setRefreshToken(response.data.data.refreshToken);
      }
    }
    return response;
  },

  logout: async () => {
    try {
      const refreshTokenValue = getRefreshToken();
      await api.post('/auth/logout', refreshTokenValue ? { refreshToken: refreshTokenValue } : {});
    } finally {
      clearAccessToken();
      clearRefreshToken();
    }
  },

  refreshToken: () => {
    const refreshTokenValue = getRefreshToken();
    return api.post('/auth/refresh', { refreshToken: refreshTokenValue });
  },

  verifyEmail: (token) =>
    api.get(`/auth/verify-email?token=${token}`),

  forgotPassword: (email) =>
    api.post('/auth/forgot-password', { email }),

  resetPassword: (data) =>
    api.post('/auth/reset-password', data),

  googleLogin: (code) =>
    api.post('/auth/oauth2/google', { code }),
};

export default authService;

