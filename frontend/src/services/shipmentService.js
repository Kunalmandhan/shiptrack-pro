import api from './api';

/**
 * Shipment Service API calls.
 */
const shipmentService = {
  create: (data) =>
    api.post('/shipments', data),

  update: (id, data) =>
    api.put(`/shipments/${id}`, data),

  updateStatus: (id, status) =>
    api.put(`/shipments/${id}/status`, { status }),

  delete: (id) =>
    api.delete(`/shipments/${id}`),

  cancel: (id) =>
    api.put(`/shipments/${id}/cancel`),

  getById: (id) =>
    api.get(`/shipments/${id}`),

  list: (params) =>
    api.get('/shipments', { params }),

  myShipments: (params) =>
    api.get('/shipments/my', { params }),

  trackByNumber: (trackingNumber) =>
    api.get(`/shipments/track/${trackingNumber}`),

  getHistory: (id) =>
    api.get(`/shipments/${id}/history`),

  assignDriver: (id, data) =>
    api.put(`/shipments/${id}/assign`, data),

  // POD
  uploadPod: (shipmentId, formData) =>
    api.post(`/pod/${shipmentId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),

  getPod: (shipmentId) =>
    api.get(`/pod/${shipmentId}`),

  verifyPod: (shipmentId) =>
    api.put(`/pod/${shipmentId}/verify`),

  downloadPod: (shipmentId) =>
    api.get(`/pod/${shipmentId}/download`, { responseType: 'blob' }),
};

export default shipmentService;
