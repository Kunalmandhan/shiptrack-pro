import api, { setAccessToken, clearAccessToken } from './api';

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
    }
    return response;
  },

  logout: async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      clearAccessToken();
    }
  },

  refreshToken: () =>
    api.post('/auth/refresh'),

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
