import api from './api';

/**
 * Notification Service API Client.
 */
const notificationService = {
  getMyNotifications: (params) =>
    api.get('/notifications/my', { params }),

  getUnreadCount: () =>
    api.get('/notifications/unread-count'),

  markAsRead: (notificationId) =>
    api.put(`/notifications/${notificationId}/read`),

  markAllAsRead: () =>
    api.put('/notifications/mark-all-read'),
};

export default notificationService;
