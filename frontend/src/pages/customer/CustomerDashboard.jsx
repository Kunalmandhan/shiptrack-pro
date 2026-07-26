import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  HiCube,
  HiTruck,
  HiCheckCircle,
  HiClock,
  HiPlus,
  HiMagnifyingGlass,
  HiArrowRight,
  HiMapPin,
  HiSparkles,
} from 'react-icons/hi2';
import StatusBadge from '../../components/shipments/StatusBadge';
import ShipmentStatusTimeline from '../../components/shipments/ShipmentStatusTimeline';
import CreateShipmentModal from '../../components/shipments/CreateShipmentModal';
import ShipmentDetailModal from '../../components/shipments/ShipmentDetailModal';
import { useAuth } from '../../context/AuthContext';
import analyticsService from '../../services/analyticsService';
import shipmentService from '../../services/shipmentService';

const MOCK_CUSTOMER_SHIPMENTS = [
  {
    id: 'shp-101',
    trackingNumber: 'STP-881924',
    status: 'IN_TRANSIT',
    senderName: 'John Doe',
    originAddress: '123 Tech Park, San Francisco, CA',
    receiverName: 'Apex Logistics Inc',
    destinationAddress: '789 Commercial Blvd, Chicago, IL',
    weightKg: 4.2,
    packageType: 'PARCEL',
    assignedDriverName: 'Robert Fox',
    createdAt: '2026-07-25T10:30:00Z',
  },
  {
    id: 'shp-102',
    trackingNumber: 'STP-774109',
    status: 'OUT_FOR_DELIVERY',
    senderName: 'John Doe',
    originAddress: '123 Tech Park, San Francisco, CA',
    receiverName: 'Sarah Jenkins',
    destinationAddress: '456 Oak Street, Seattle, WA',
    weightKg: 1.5,
    packageType: 'DOCUMENT',
    assignedDriverName: 'Michael Scott',
    createdAt: '2026-07-24T14:15:00Z',
  },
  {
    id: 'shp-103',
    trackingNumber: 'STP-663812',
    status: 'DELIVERED',
    senderName: 'John Doe',
    originAddress: '123 Tech Park, San Francisco, CA',
    receiverName: 'David Miller',
    destinationAddress: '99 Pine Lane, Austin, TX',
    weightKg: 12.0,
    packageType: 'PALLET',
    assignedDriverName: 'Sarah Connor',
    createdAt: '2026-07-20T09:00:00Z',
  },
];

export default function CustomerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [metrics, setMetrics] = useState({
    totalShipments: 12,
    activeShipments: 2,
    deliveredShipments: 10,
    onTimeDeliveryRate: 100.0,
  });

  const [recentShipments, setRecentShipments] = useState(MOCK_CUSTOMER_SHIPMENTS);
  const [quickTrackingInput, setQuickTrackingInput] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedShipment, setSelectedShipment] = useState(null);

  const fetchDashboardData = async () => {
    setIsLoading(true);
    try {
      const [dashRes, myRes] = await Promise.allSettled([
        analyticsService.getCustomerDashboard(),
        shipmentService.myShipments({ size: 5 }),
      ]);

      if (dashRes.status === 'fulfilled' && dashRes.value.data?.success) {
        setMetrics(dashRes.value.data.data);
      }

      if (myRes.status === 'fulfilled' && myRes.value.data?.success?.content) {
        setRecentShipments(myRes.value.data.data.content);
      } else {
        setRecentShipments(MOCK_CUSTOMER_SHIPMENTS);
      }
    } catch {
      // Fallback defaults
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleQuickTrackSubmit = (e) => {
    e.preventDefault();
    if (!quickTrackingInput.trim()) return;
    navigate(`/tracking/${quickTrackingInput.trim()}`);
  };

  const activeHighlight = recentShipments.find(
    (s) => ['IN_TRANSIT', 'OUT_FOR_DELIVERY', 'PICKED_UP', 'CREATED'].includes(s.status)
  ) || recentShipments[0];

  return (
    <div className="space-y-6">
      {/* Welcome Banner Header */}
      <div className="bg-gradient-to-r from-primary-900/60 via-surface-800 to-surface-800 border border-white/10 p-6 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 bg-accent-500/20 text-accent-300 text-[10px] font-bold uppercase tracking-wider rounded-md border border-accent-500/30">
              Customer Portal
            </span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight mt-1">
            Welcome back, {user?.name || 'Customer'}!
          </h1>
          <p className="text-gray-400 text-xs mt-1">
            Track your outgoing packages and monitor live deliveries in real-time
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-5 py-2.5 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white text-xs font-bold rounded-2xl shadow-lg shadow-primary-500/25 flex items-center justify-center gap-2 transition-all hover:scale-[1.02]"
          >
            <HiPlus className="w-4 h-4" />
            <span>Book New Shipment</span>
          </button>
        </div>
      </div>

      {/* Top 4 KPI Summary Metric Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Sent */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Total Sent</span>
            <div className="p-2 bg-primary-500/10 text-primary-400 rounded-xl border border-primary-500/20">
              <HiCube className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-white">{metrics.totalShipments}</p>
          <p className="text-[11px] text-gray-400 font-medium">Packages registered</p>
        </div>

        {/* In Pipeline */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">In Pipeline</span>
            <div className="p-2 bg-blue-500/10 text-blue-400 rounded-xl border border-blue-500/20">
              <HiTruck className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-blue-400">{metrics.activeShipments}</p>
          <p className="text-[11px] text-blue-400 font-medium">Currently in transit</p>
        </div>

        {/* Delivered */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Delivered</span>
            <div className="p-2 bg-accent-500/10 text-accent-400 rounded-xl border border-accent-500/20">
              <HiCheckCircle className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-accent-400">{metrics.deliveredShipments}</p>
          <p className="text-[11px] text-accent-400 font-medium">Successfully completed</p>
        </div>

        {/* On-Time Rate */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">On-Time Rate</span>
            <div className="p-2 bg-warning-500/10 text-warning-400 rounded-xl border border-warning-500/20">
              <HiClock className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-warning-400">{metrics.onTimeDeliveryRate}%</p>
          <p className="text-[11px] text-gray-400 font-medium">Delivery performance</p>
        </div>
      </div>

      {/* Grid: Quick Track Search Widget + Active Delivery Highlight Card */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 1 Col: Quick Track Input Widget */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <HiMagnifyingGlass className="w-5 h-5 text-primary-400" />
              <h3 className="text-sm font-bold text-white">Quick Package Lookup</h3>
            </div>
            <p className="text-xs text-gray-400">
              Enter any tracking number to instantly view live location and ETA
            </p>

            <form onSubmit={handleQuickTrackSubmit} className="mt-4 space-y-3">
              <input
                type="text"
                value={quickTrackingInput}
                onChange={(e) => setQuickTrackingInput(e.target.value)}
                placeholder="e.g. STP-881924..."
                className="w-full px-4 py-3 bg-surface-900 border border-white/10 rounded-2xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary-500"
              />
              <button
                type="submit"
                className="w-full py-3 bg-primary-600 hover:bg-primary-500 text-white text-xs font-bold rounded-2xl shadow-lg shadow-primary-600/25 flex items-center justify-center gap-2 transition-all"
              >
                <span>Track Live Status</span>
                <HiArrowRight className="w-4 h-4" />
              </button>
            </form>
          </div>

          <div className="p-3 bg-surface-900/50 rounded-2xl border border-white/5 text-[11px] text-gray-400">
            💡 <strong className="text-white">Tip:</strong> Share your tracking number with recipients so they can track without logging in.
          </div>
        </div>

        {/* Right 2 Cols: Featured Active Shipment Highlight */}
        {activeHighlight && (
          <div className="lg:col-span-2 bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="text-xs font-mono font-bold text-primary-400 bg-primary-500/10 px-2.5 py-1 rounded-lg border border-primary-500/20">
                  {activeHighlight.trackingNumber}
                </span>
                <StatusBadge status={activeHighlight.status} size="small" />
              </div>
              <Link
                to={`/tracking/${activeHighlight.trackingNumber}`}
                className="text-xs font-bold text-primary-400 hover:text-primary-300 flex items-center gap-1"
              >
                <span>Live Map</span>
                <HiArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            {/* Stepper Progress Timeline */}
            <div className="bg-surface-900/40 p-4 rounded-2xl border border-white/5">
              <ShipmentStatusTimeline currentStatus={activeHighlight.status} />
            </div>

            {/* Destination summary */}
            <div className="flex items-center justify-between pt-1 text-xs">
              <div className="flex items-center gap-2 text-gray-300">
                <HiMapPin className="w-4 h-4 text-accent-400" />
                <span>To: <strong className="text-white">{activeHighlight.receiverName}</strong> ({activeHighlight.destinationAddress})</span>
              </div>
              <span className="text-gray-400 text-[11px]">Est. Delivery: Jul 28</span>
            </div>
          </div>
        )}
      </div>

      {/* Recent Orders List */}
      <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <HiSparkles className="w-4 h-4 text-warning-400" />
            <span>My Recent Shipments</span>
          </h3>
          <Link
            to="/shipments"
            className="text-xs text-primary-400 hover:text-primary-300 font-semibold flex items-center gap-1"
          >
            <span>View All Shipments</span>
            <HiArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {recentShipments.length === 0 ? (
          <div className="py-8 text-center text-gray-400 text-xs">
            No recent shipments found. Click "Book New Shipment" to get started.
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-3">
            {recentShipments.map((shp) => (
              <div
                key={shp.id}
                onClick={() => setSelectedShipment(shp)}
                className="bg-surface-900/60 hover:bg-surface-900 border border-white/5 hover:border-primary-500/30 p-4 rounded-2xl transition-all cursor-pointer flex flex-col md:flex-row md:items-center justify-between gap-3"
              >
                <div className="flex items-center gap-4">
                  <div className="p-2.5 bg-primary-500/10 text-primary-400 rounded-xl border border-primary-500/20">
                    <HiCube className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-3">
                      <span className="font-mono font-bold text-xs text-white">{shp.trackingNumber}</span>
                      <StatusBadge status={shp.status} size="small" />
                    </div>
                    <p className="text-xs text-gray-400 mt-1">
                      To: <span className="text-gray-200 font-medium">{shp.receiverName}</span> ({shp.destinationAddress})
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between md:justify-end gap-4 text-xs text-gray-400">
                  <span>{new Date(shp.createdAt || Date.now()).toLocaleDateString()}</span>
                  <button className="px-3 py-1.5 bg-surface-700 hover:bg-primary-600 text-gray-200 hover:text-white rounded-xl font-semibold transition-all">
                    View Details
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modals */}
      <CreateShipmentModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onCreated={fetchDashboardData}
      />

      <ShipmentDetailModal
        isOpen={!!selectedShipment}
        onClose={() => setSelectedShipment(null)}
        shipment={selectedShipment}
        isAdmin={false}
        onStatusUpdated={fetchDashboardData}
      />
    </div>
  );
}
