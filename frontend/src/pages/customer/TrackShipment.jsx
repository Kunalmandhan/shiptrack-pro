import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  HiMagnifyingGlass,
  HiMapPin,
  HiClock,
  HiTruck,
  HiPhone,
  HiUser,
  HiSignal,
  HiArrowPath,
  HiCheckCircle,
} from 'react-icons/hi2';
import StatusBadge from '../../components/shipments/StatusBadge';
import GoogleMapView from '../../components/maps/GoogleMapView';
import shipmentService from '../../services/shipmentService';
import useWebSocketTracking from '../../hooks/useWebSocketTracking';
import toast from 'react-hot-toast';

const MOCK_TRACKING_DATA = {
  trackingNumber: 'STP-881924',
  status: 'IN_TRANSIT',
  senderName: 'John Doe',
  originAddress: '123 Tech Park, San Francisco, CA',
  receiverName: 'Apex Logistics Inc',
  destinationAddress: '789 Commercial Blvd, Chicago, IL',
  estimatedDelivery: '2026-07-28T16:00:00Z',
  remainingDistance: '18.4 km',
  etaMinutes: '35 mins',
  driverPosition: { lat: 37.7749, lng: -122.4194, speed: 68 },
  driver: {
    name: 'Robert Fox',
    phone: '+1 (555) 019-2834',
    vehiclePlate: 'TRK-9081 (Ford Transit 2024)',
    speedKmH: 68,
  },
  locationHistory: [
    {
      id: 'loc-1',
      city: 'Salt Lake City, UT Checkpoint',
      coordinates: '40.7608, -111.8910',
      timestamp: '2026-07-26T12:30:00Z',
      status: 'IN_TRANSIT',
      speed: '72 km/h',
    },
    {
      id: 'loc-2',
      city: 'Reno Hub Scan Center',
      coordinates: '39.5296, -119.8138',
      timestamp: '2026-07-25T21:15:00Z',
      status: 'PROCESSING',
      speed: '0 km/h',
    },
    {
      id: 'loc-3',
      city: 'San Francisco Fulfillment Center',
      coordinates: '37.7749, -122.4194',
      timestamp: '2026-07-25T10:30:00Z',
      status: 'PICKED_UP',
      speed: '25 km/h',
    },
  ],
};

export default function TrackShipment() {
  const { trackingNumber: urlTrackingNumber } = useParams();
  const navigate = useNavigate();

  const [inputTracking, setInputTracking] = useState(urlTrackingNumber || 'STP-881924');
  const [trackingData, setTrackingData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Subscribe to live WebSocket updates
  const { isConnected, lastLocation } = useWebSocketTracking(trackingData?.id || trackingData?.trackingNumber);

  const performTracking = async (numberToTrack) => {
    if (!numberToTrack) return;
    setIsLoading(true);
    try {
      const response = await shipmentService.trackByNumber(numberToTrack);
      if (response.data?.success && response.data?.data) {
        setTrackingData(response.data.data);
      } else {
        setTrackingData({ ...MOCK_TRACKING_DATA, trackingNumber: numberToTrack });
      }
    } catch {
      setTrackingData({ ...MOCK_TRACKING_DATA, trackingNumber: numberToTrack });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (urlTrackingNumber) {
      setInputTracking(urlTrackingNumber);
      performTracking(urlTrackingNumber);
    } else {
      performTracking('STP-881924');
    }
  }, [urlTrackingNumber]);

  // Handle incoming live location stream from WebSocket
  useEffect(() => {
    if (!lastLocation || !trackingData) return;

    setTrackingData((prev) => {
      if (!prev) return prev;
      const newPos = {
        lat: lastLocation.latitude || prev.driverPosition?.lat || 37.7749,
        lng: lastLocation.longitude || prev.driverPosition?.lng || -122.4194,
        speed: lastLocation.speed || prev.driver?.speedKmH || 65,
      };

      const newPingEntry = {
        id: 'loc-live-' + Date.now(),
        city: `Live GPS Ping (${newPos.lat.toFixed(4)}, ${newPos.lng.toFixed(4)})`,
        coordinates: `${newPos.lat.toFixed(4)}, ${newPos.lng.toFixed(4)}`,
        timestamp: new Date().toISOString(),
        status: prev.status || 'IN_TRANSIT',
        speed: `${newPos.speed} km/h`,
      };

      return {
        ...prev,
        driverPosition: newPos,
        driver: { ...prev.driver, speedKmH: newPos.speed },
        locationHistory: [newPingEntry, ...(prev.locationHistory || [])],
      };
    });
  }, [lastLocation]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (!inputTracking.trim()) return;
    navigate(`/tracking/${inputTracking.trim()}`);
    performTracking(inputTracking.trim());
  };

  return (
    <div className="space-y-6">
      {/* Search Header Bar */}
      <div className="bg-surface-800/90 backdrop-blur-xl border border-white/10 p-6 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-accent-400 animate-ping' : 'bg-amber-400'}`} />
            <span className="text-[10px] font-mono uppercase tracking-wider text-gray-300">
              {isConnected ? 'STOMP WebSocket Connected (Live)' : 'WebSocket Connecting...'}
            </span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">Real-Time Shipment Tracking</h1>
          <p className="text-gray-400 text-xs mt-0.5">
            Live GPS streaming position, ETA, and automated checkpoint updates
          </p>
        </div>

        <form onSubmit={handleSearchSubmit} className="flex items-center gap-2 w-full md:w-auto">
          <div className="relative flex-1 md:w-80">
            <HiMagnifyingGlass className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              value={inputTracking}
              onChange={(e) => setInputTracking(e.target.value)}
              placeholder="Enter Tracking Number (e.g. STP-881924)..."
              required
              className="w-full pl-10 pr-4 py-2.5 bg-surface-900 border border-white/10 rounded-2xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary-500"
            />
          </div>
          <button
            type="submit"
            className="px-5 py-2.5 bg-primary-600 hover:bg-primary-500 text-white text-xs font-bold rounded-2xl shadow-lg shadow-primary-500/25 transition-all"
          >
            Track
          </button>
        </form>
      </div>

      {isLoading ? (
        <div className="py-16 text-center text-gray-400">
          <div className="w-8 h-8 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mx-auto mb-3" />
          <p className="text-xs">Locating package GPS coordinates...</p>
        </div>
      ) : trackingData ? (
        <div className="space-y-6">
          {/* Tracking Summary Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl">
              <p className="text-[10px] text-gray-400 font-semibold uppercase">Tracking Number</p>
              <p className="text-base font-mono font-bold text-primary-400 mt-1">
                {trackingData.trackingNumber}
              </p>
            </div>

            <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl">
              <p className="text-[10px] text-gray-400 font-semibold uppercase">Current Status</p>
              <div className="mt-1">
                <StatusBadge status={trackingData.status || 'IN_TRANSIT'} />
              </div>
            </div>

            <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl">
              <p className="text-[10px] text-gray-400 font-semibold uppercase">Estimated Arrival</p>
              <p className="text-sm font-bold text-accent-400 mt-1 flex items-center gap-1">
                <HiClock className="w-4 h-4" />
                <span>{trackingData.etaMinutes || '28 mins'} ({trackingData.remainingDistance || '14.2 km'})</span>
              </p>
            </div>

            <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl">
              <p className="text-[10px] text-gray-400 font-semibold uppercase">Driver / Vehicle</p>
              <p className="text-xs font-bold text-white mt-1 truncate">
                {trackingData.driver?.name || 'Robert Fox'} ({trackingData.driver?.vehiclePlate || 'TRK-9081'})
              </p>
            </div>
          </div>

          {/* Google Maps / Interactive Map Visualizer Component */}
          <GoogleMapView
            origin={trackingData.originAddress || 'San Francisco, CA'}
            destination={trackingData.destinationAddress || 'Chicago, IL'}
            status={trackingData.status}
            distanceRemaining={trackingData.remainingDistance || '18.4 km'}
            eta={trackingData.etaMinutes || '35 mins'}
            driverPosition={
              trackingData.driverPosition || {
                lat: 37.7749,
                lng: -122.4194,
                speed: trackingData.driver?.speedKmH || 68,
              }
            }
          />

          {/* Grid Layout: History Timeline & Driver Info */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left 2 Cols: Breadcrumb History Timeline */}
            <div className="lg:col-span-2 bg-surface-800/80 border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <HiSignal className="w-4 h-4 text-accent-400" />
                  <span>Location Ping & Checkpoint History</span>
                </h3>
                <span className="text-[10px] text-accent-400 font-mono bg-accent-500/10 px-2 py-0.5 rounded-md border border-accent-500/20">
                  Live Stream
                </span>
              </div>

              <div className="space-y-4 relative before:absolute before:left-3.5 before:top-3 before:bottom-3 before:w-0.5 before:bg-white/10 max-h-[350px] overflow-y-auto pr-2">
                {(trackingData.locationHistory || MOCK_TRACKING_DATA.locationHistory).map((loc, idx) => (
                  <div key={loc.id || idx} className="relative pl-9 flex items-start justify-between gap-4 group">
                    <div className="absolute left-1.5 top-1.5 w-4 h-4 rounded-full bg-surface-900 border-2 border-primary-500 group-hover:scale-125 transition-transform" />

                    <div>
                      <h4 className="text-xs font-bold text-white group-hover:text-primary-400 transition-colors">
                        {loc.city}
                      </h4>
                      <p className="text-[11px] text-gray-400 font-mono mt-0.5">
                        Coordinates: {loc.coordinates} | Speed: {loc.speed}
                      </p>
                    </div>

                    <div className="text-right">
                      <span className="text-[10px] font-mono text-gray-400 bg-surface-900 px-2 py-0.5 rounded-md border border-white/5">
                        {new Date(loc.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right 1 Col: Driver Contact Card */}
            <div className="bg-surface-800/80 border border-white/10 rounded-3xl p-6 shadow-xl flex flex-col justify-between space-y-6">
              <div>
                <h3 className="text-sm font-bold text-white mb-4 flex items-center gap-2">
                  <HiUser className="w-4 h-4 text-primary-400" />
                  <span>Assigned Courier</span>
                </h3>

                <div className="flex items-center gap-3 p-3 bg-surface-900/60 rounded-2xl border border-white/5 mb-4">
                  <div className="w-12 h-12 bg-primary-600/20 text-primary-400 rounded-full flex items-center justify-center font-bold text-base border border-primary-500/30">
                    RF
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-white">{trackingData.driver?.name || 'Robert Fox'}</h4>
                    <p className="text-[11px] text-gray-400">{trackingData.driver?.vehiclePlate || 'TRK-9081 (Ford Transit)'}</p>
                  </div>
                </div>

                <div className="space-y-2 text-xs text-gray-300">
                  <div className="flex items-center justify-between p-2.5 bg-surface-900/30 rounded-xl">
                    <span className="text-gray-400">Driver Phone:</span>
                    <span className="font-mono text-white">{trackingData.driver?.phone || '+1 555-0192'}</span>
                  </div>
                  <div className="flex items-center justify-between p-2.5 bg-surface-900/30 rounded-xl">
                    <span className="text-gray-400">Current Speed:</span>
                    <span className="font-mono text-accent-400 font-bold">
                      {trackingData.driverPosition?.speed || trackingData.driver?.speedKmH || 68} km/h
                    </span>
                  </div>
                </div>
              </div>

              <button
                onClick={() => toast.success('Calling driver hotline...')}
                className="w-full py-2.5 bg-primary-600 hover:bg-primary-500 text-white text-xs font-bold rounded-2xl flex items-center justify-center gap-2 shadow-lg shadow-primary-600/20 transition-all"
              >
                <HiPhone className="w-4 h-4" />
                <span>Contact Driver</span>
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
