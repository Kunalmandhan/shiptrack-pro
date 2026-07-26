import api from './api';

/**
 * Proof of Delivery (POD) Service API client.
 */
const podService = {
  uploadPod: (shipmentId, formData) =>
    api.post(`/pod/${shipmentId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),

  getPod: (shipmentId) =>
    api.get(`/pod/${shipmentId}`),

  downloadPodPhoto: (shipmentId) =>
    api.get(`/pod/${shipmentId}/download`, { responseType: 'blob' }),
};

export default podService;
