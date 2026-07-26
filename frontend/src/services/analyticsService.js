import api from './api';

/**
 * Analytics Service API calls.
 */
const analyticsService = {
  getAdminDashboard: () =>
    api.get('/analytics/admin/dashboard'),

  getCustomerDashboard: () =>
    api.get('/analytics/customer/dashboard'),

  getAdminVolumeSeries: (period = '14DAYS') =>
    api.get('/analytics/admin/shipments/volume', { params: { period } }),

  getCustomerVolumeSeries: (period = '14DAYS') =>
    api.get('/analytics/customer/volume', { params: { period } }),

  getStatusDistribution: () =>
    api.get('/analytics/admin/shipments/status-distribution'),

  getAdminDelays: () =>
    api.get('/analytics/admin/delays'),

  generateReport: (data) =>
    api.post('/analytics/reports/generate', data),

  getMyReports: () =>
    api.get('/analytics/reports/my'),
};

export default analyticsService;
