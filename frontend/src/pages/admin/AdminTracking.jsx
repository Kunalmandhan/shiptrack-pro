import { useState, useEffect } from 'react';
import {
  HiSignal,
  HiTruck,
  HiUser,
  HiBolt,
  HiMapPin,
  HiArrowPath,
  HiCheckCircle,
} from 'react-icons/hi2';
import GoogleMapView from '../../components/maps/GoogleMapView';
import LocationPingModal from '../../components/tracking/LocationPingModal';
import useWebSocketTracking from '../../hooks/useWebSocketTracking';
import driverService from '../../services/driverService';
import trackingService from '../../services/trackingService';
import toast from 'react-hot-toast';

const MOCK_ACTIVE_FLEET = [
  {
    id: 'drv-101',
    driverName: 'Robert Fox',
    vehiclePlate: 'TRK-9081',
    assignedTrackingNumber: 'STP-881924',
    latitude: 37.7749,
    longitude: -122.4194,
    speedKmH: 68.4,
    heading: 45,
    lastPing: '2 mins ago',
    status: 'ONLINE',
  },
  {
    id: 'drv-102',
    driverName: 'Michael Scott',
    vehiclePlate: 'TRK-4420',
    assignedTrackingNumber: 'STP-774109',
    latitude: 39.5296,
    longitude: -119.8138,
    speedKmH: 72.0,
    heading: 120,
    lastPing: '1 min ago',
    status: 'ONLINE',
  },
  {
    id: 'drv-103',
    driverName: 'Sarah Connor',
    vehiclePlate: 'EV-1029',
    assignedTrackingNumber: 'STP-663812',
    latitude: 40.7608,
    longitude: -111.8910,
    speedKmH: 0.0,
    heading: 0,
    lastPing: 'Just now',
    status: 'STOPPED',
  },
];

export default function AdminTracking() {
  const [fleet, setFleet] = useState(MOCK_ACTIVE_FLEET);
  const [selectedDriver, setSelectedDriver] = useState(MOCK_ACTIVE_FLEET[0]);
  const [isPingModalOpen, setIsPingModalOpen] = useState(false);

  // Subscribe to driver telemetry stream
  const { isConnected, lastLocation } = useWebSocketTracking(null, selectedDriver?.id);

  useEffect(() => {
    if (!lastLocation) return;
    setFleet((prev) =>
      prev.map((drv) =>
        drv.id === lastLocation.driverId || drv.id === selectedDriver?.id
          ? {
              ...drv,
              latitude: lastLocation.latitude || drv.latitude,
              longitude: lastLocation.longitude || drv.longitude,
              speedKmH: lastLocation.speed || drv.speedKmH,
              lastPing: 'Just now',
            }
          : drv
      )
    );
    setSelectedDriver((prev) =>
      prev
        ? {
            ...prev,
            latitude: lastLocation.latitude || prev.latitude,
            longitude: lastLocation.longitude || prev.longitude,
            speedKmH: lastLocation.speed || prev.speedKmH,
          }
        : prev
    );
  }, [lastLocation]);

  const handlePingSuccess = (newPing) => {
    toast.success(`GPS coordinates updated for ${newPing.driverId}`);
    setFleet((prev) =>
      prev.map((drv) =>
        drv.id === newPing.driverId || drv.driverName.toLowerCase().includes('robert')
          ? {
              ...drv,
              latitude: newPing.latitude,
              longitude: newPing.longitude,
              speedKmH: newPing.speedKmH,
              lastPing: 'Just now',
            }
          : drv
      )
    );
    if (selectedDriver) {
      setSelectedDriver((prev) =>
        prev
          ? {
              ...prev,
              latitude: newPing.latitude,
              longitude: newPing.longitude,
              speedKmH: newPing.speedKmH,
            }
          : prev
      );
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-accent-400 animate-ping' : 'bg-amber-400'}`} />
            <span className="text-[10px] font-mono uppercase tracking-wider text-gray-300">
              {isConnected ? 'STOMP Telemetry Stream Connected' : 'Connecting to Telemetry Broker...'}
            </span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">Admin Fleet Monitoring</h1>
          <p className="text-gray-400 text-xs mt-0.5">
            Real-time GPS telemetry, driver tracking, and location ping simulation
          </p>
        </div>

        <button
          onClick={() => setIsPingModalOpen(true)}
          className="px-5 py-2.5 bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-500 hover:to-accent-400 text-white text-xs font-bold rounded-2xl shadow-lg shadow-accent-500/25 flex items-center justify-center gap-2 transition-all hover:scale-[1.02]"
        >
          <HiBolt className="w-4 h-4 text-warning-300" />
          <span>Push GPS Ping</span>
        </button>
      </div>

      {/* Fleet Live Map Visualizer */}
      <GoogleMapView
        origin="SF Central Depot"
        destination="Chicago Hub"
        status="IN_TRANSIT"
        distanceRemaining="24.8 km"
        eta="32 mins"
        driverPosition={{
          lat: selectedDriver?.latitude || 37.7749,
          lng: selectedDriver?.longitude || -122.4194,
          speed: selectedDriver?.speedKmH || 68,
        }}
      />

      {/* Active Fleet Telemetry Table */}
      <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl overflow-hidden shadow-2xl space-y-4">
        <div className="p-6 border-b border-white/10 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <HiTruck className="w-5 h-5 text-primary-400" />
            <h3 className="text-sm font-bold text-white">Active Courier Vehicles ({fleet.length})</h3>
          </div>
          <span className="text-xs text-accent-400 font-mono bg-accent-500/10 px-2.5 py-1 rounded-lg border border-accent-500/20">
            Telemetry Stream Active
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-900/60 border-b border-white/10 text-gray-400 uppercase font-semibold text-[10px] tracking-wider">
              <tr>
                <th className="px-6 py-4">Driver Name</th>
                <th className="px-6 py-4">Vehicle Plate</th>
                <th className="px-6 py-4">Assigned Shipment</th>
                <th className="px-6 py-4">GPS Coordinates</th>
                <th className="px-6 py-4">Speed</th>
                <th className="px-6 py-4">Last Ping</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {fleet.map((drv) => (
                <tr key={drv.id} className="hover:bg-white/[0.02] transition-colors">
                  <td className="px-6 py-4 font-bold text-white flex items-center gap-2">
                    <div className="w-7 h-7 bg-primary-500/20 text-primary-400 rounded-full flex items-center justify-center text-[10px] font-bold">
                      {drv.driverName.charAt(0)}
                    </div>
                    <span>{drv.driverName}</span>
                  </td>
                  <td className="px-6 py-4 font-mono text-gray-300">
                    {drv.vehiclePlate}
                  </td>
                  <td className="px-6 py-4 font-mono font-bold text-primary-400">
                    {drv.assignedTrackingNumber}
                  </td>
                  <td className="px-6 py-4 font-mono text-gray-400">
                    {drv.latitude.toFixed(4)}, {drv.longitude.toFixed(4)}
                  </td>
                  <td className="px-6 py-4 font-mono font-bold text-accent-400">
                    {drv.speedKmH} km/h
                  </td>
                  <td className="px-6 py-4 text-gray-400">
                    {drv.lastPing}
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button
                      onClick={() => {
                        setSelectedDriver(drv);
                        toast.success(`Focused map view on ${drv.driverName}`);
                      }}
                      className="px-3 py-1.5 bg-surface-700 hover:bg-surface-600 text-white text-[11px] font-semibold rounded-xl transition-colors"
                    >
                      Focus Map
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Location Simulation Ping Modal */}
      <LocationPingModal
        isOpen={isPingModalOpen}
        onClose={() => setIsPingModalOpen(false)}
        onPingSuccess={handlePingSuccess}
      />
    </div>
  );
}
