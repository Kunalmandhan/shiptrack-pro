/**
 * Shared constants for the frontend application.
 */

export const ROLES = {
  ADMIN: 'ADMIN',
  CUSTOMER: 'CUSTOMER',
};

export const SHIPMENT_STATUS = {
  CREATED: 'CREATED',
  PROCESSING: 'PROCESSING',
  PICKED_UP: 'PICKED_UP',
  IN_TRANSIT: 'IN_TRANSIT',
  OUT_FOR_DELIVERY: 'OUT_FOR_DELIVERY',
  DELIVERED: 'DELIVERED',
  DELAYED: 'DELAYED',
  FAILED_DELIVERY: 'FAILED_DELIVERY',
  CANCELLED: 'CANCELLED',
  RETURNED: 'RETURNED',
};

export const STATUS_COLORS = {
  CREATED: 'bg-blue-500',
  PROCESSING: 'bg-indigo-500',
  PICKED_UP: 'bg-cyan-500',
  IN_TRANSIT: 'bg-primary-500',
  OUT_FOR_DELIVERY: 'bg-amber-500',
  DELIVERED: 'bg-accent-500',
  DELAYED: 'bg-warning-500',
  FAILED_DELIVERY: 'bg-danger-500',
  CANCELLED: 'bg-gray-500',
  RETURNED: 'bg-orange-500',
};

export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 20,
};
