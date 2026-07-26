import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import websocketService from '../services/websocketService';

/**
 * Custom React Hook for live WebSocket tracking subscriptions.
 *
 * @param {string} shipmentId — Shipment UUID or Tracking Number
 * @param {string} driverId — Optional Driver ID
 * @returns {object} { isConnected, lastLocation }
 */
export function useWebSocketTracking(shipmentId, driverId) {
  const [isConnected, setIsConnected] = useState(false);
  const [lastLocation, setLastLocation] = useState(null);

  useEffect(() => {
    // Listen for WebSocket connection status
    const unsubscribeStatus = websocketService.addStatusListener(setIsConnected);

    // Auto connect
    websocketService.connect();

    let unsubscribeShipment = null;
    let unsubscribeDriver = null;

    if (shipmentId) {
      unsubscribeShipment = websocketService.subscribeShipment(shipmentId, (payload) => {
        setLastLocation(payload);
        toast.success(`Live GPS Ping: ${payload.speed || 65} km/h`, {
          id: `ping-${shipmentId}`,
          duration: 3000,
        });
      });
    }

    if (driverId) {
      unsubscribeDriver = websocketService.subscribeDriver(driverId, (payload) => {
        setLastLocation(payload);
      });
    }

    return () => {
      unsubscribeStatus();
      if (unsubscribeShipment) unsubscribeShipment();
      if (unsubscribeDriver) unsubscribeDriver();
    };
  }, [shipmentId, driverId]);

  return {
    isConnected,
    lastLocation,
  };
}

export default useWebSocketTracking;
