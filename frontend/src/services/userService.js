import api from './api';

/**
 * User Service API calls.
 */
const userService = {
  getProfile: () =>
    api.get('/users/me'),

  updateProfile: (data) =>
    api.put('/users/me', data),

  uploadAvatar: (file) => {
    const formData = new FormData();
    formData.append('avatar', file);
    return api.post('/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // Admin endpoints
  listUsers: (params) =>
    api.get('/users', { params }),

  getUserById: (id) =>
    api.get(`/users/${id}`),

  deactivateUser: (id) =>
    api.put(`/users/${id}/deactivate`),

  approveRole: (id, data) =>
    api.put(`/users/${id}/approve-role`, data),
};

export default userService;
