import api from './api';

/**
 * Driver & Vehicle Service API calls.
 */
const driverService = {
  getDrivers: (params) =>
    api.get('/drivers', { params }),

  getAvailableDrivers: () =>
    api.get('/drivers/available'),

  getVehicles: (params) =>
    api.get('/vehicles', { params }),

  getAvailableVehicles: () =>
    api.get('/vehicles/available'),

  createDriver: (data) =>
    api.post('/drivers', data),

  createVehicle: (data) =>
    api.post('/vehicles', data),
};

export default driverService;
