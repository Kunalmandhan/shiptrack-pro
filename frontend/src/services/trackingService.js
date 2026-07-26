import api from './api';

/**
 * Tracking Service API calls.
 */
const trackingService = {
  getLiveLocation: (shipmentId) =>
    api.get(`/tracking/${shipmentId}/live`),

  getLocationHistory: (shipmentId) =>
    api.get(`/tracking/${shipmentId}/history`),

  getDriverLocation: (driverId) =>
    api.get(`/tracking/driver/${driverId}/current`),

  pushLocation: (data) =>
    api.post('/tracking/location', data),

  pushLocationBatch: (data) =>
    api.post('/tracking/location/batch', data),
};

export default trackingService;
