import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  HiCube,
  HiTruck,
  HiClock,
  HiCheckCircle,
  HiExclamationTriangle,
  HiArrowRight,
  HiArrowTrendingUp,
  HiUserGroup,
  HiSparkles,
} from 'react-icons/hi2';
import ShipmentVolumeChart from '../../components/analytics/ShipmentVolumeChart';
import StatusDistributionChart from '../../components/analytics/StatusDistributionChart';
import StatusBadge from '../../components/shipments/StatusBadge';
import analyticsService from '../../services/analyticsService';
import shipmentService from '../../services/shipmentService';

const MOCK_RECENT_ACTIVITY = [
  {
    id: 'shp-201',
    trackingNumber: 'STP-902184',
    senderName: 'Acme Corp',
    receiverName: 'Apex Logistics',
    status: 'IN_TRANSIT',
    assignedDriverName: 'Robert Fox',
    createdAt: '2026-07-26T10:15:00Z',
  },
  {
    id: 'shp-202',
    trackingNumber: 'STP-881924',
    senderName: 'John Doe',
    receiverName: 'Sarah Jenkins',
    status: 'OUT_FOR_DELIVERY',
    assignedDriverName: 'Michael Scott',
    createdAt: '2026-07-26T09:40:00Z',
  },
  {
    id: 'shp-203',
    trackingNumber: 'STP-774109',
    senderName: 'TechGlobal Corp',
    receiverName: 'Omega Retails',
    status: 'CREATED',
    assignedDriverName: 'Unassigned',
    createdAt: '2026-07-26T09:15:00Z',
  },
  {
    id: 'shp-204',
    trackingNumber: 'STP-663812',
    senderName: 'BioPharm Inc',
    receiverName: 'Central Hospital',
    status: 'DELAYED',
    assignedDriverName: 'Sarah Connor',
    createdAt: '2026-07-25T16:00:00Z',
  },
];

export default function AdminDashboard() {
  const [metrics, setMetrics] = useState({
    totalShipments: 128,
    activeShipments: 42,
    deliveredShipments: 76,
    delayedShipments: 10,
    onTimeDeliveryRate: 92.4,
    avgDeliveryHours: 24.5,
  });

  const [volumeSeries, setVolumeSeries] = useState([]);
  const [distribution, setDistribution] = useState(null);
  const [recentShipments, setRecentShipments] = useState(MOCK_RECENT_ACTIVITY);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      setIsLoading(true);
      try {
        const [dashRes, volRes, distRes, shipRes] = await Promise.allSettled([
          analyticsService.getAdminDashboard(),
          analyticsService.getAdminVolumeSeries('14DAYS'),
          analyticsService.getStatusDistribution(),
          shipmentService.list({ size: 5 }),
        ]);

        if (dashRes.status === 'fulfilled' && dashRes.value.data?.success) {
          setMetrics(dashRes.value.data.data);
        }
        if (volRes.status === 'fulfilled' && volRes.value.data?.success) {
          setVolumeSeries(volRes.value.data.data);
        }
        if (distRes.status === 'fulfilled' && distRes.value.data?.success) {
          setDistribution(distRes.value.data.data.distribution);
        }
        if (shipRes.status === 'fulfilled' && shipRes.value.data?.success?.content) {
          setRecentShipments(shipRes.value.data.data.content.slice(0, 5));
        }
      } catch {
        // Fallback default
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Welcome Banner Header */}
      <div className="bg-gradient-to-r from-primary-900/60 via-surface-800 to-surface-800 border border-white/10 p-6 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 bg-primary-500/20 text-primary-300 text-[10px] font-bold uppercase tracking-wider rounded-md border border-primary-500/30">
              Admin Platform Control
            </span>
            <span className="text-xs text-gray-400 font-mono">System Status: Optimal</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight mt-1">
            Operations & Analytics Control Center
          </h1>
          <p className="text-gray-400 text-xs mt-1">
            Real-time logistics monitoring, delivery KPIs, and active fleet intelligence
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/admin/shipments"
            className="px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white text-xs font-bold rounded-2xl shadow-lg shadow-primary-500/25 flex items-center gap-2 transition-all"
          >
            <span>Manage Shipments</span>
            <HiArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>

      {/* Top 4 KPI Summary Metric Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Shipments */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Total Volume</span>
            <div className="p-2 bg-primary-500/10 text-primary-400 rounded-xl border border-primary-500/20">
              <HiCube className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-white">{metrics.totalShipments}</p>
          <p className="text-[11px] text-accent-400 font-medium flex items-center gap-1">
            <HiArrowTrendingUp className="w-3.5 h-3.5" />
            <span>+12.4% vs last week</span>
          </p>
        </div>

        {/* Active Pipeline */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Active Pipeline</span>
            <div className="p-2 bg-blue-500/10 text-blue-400 rounded-xl border border-blue-500/20">
              <HiTruck className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-blue-400">{metrics.activeShipments}</p>
          <p className="text-[11px] text-gray-400 font-medium">In transit & pending delivery</p>
        </div>

        {/* On-Time Delivery Rate */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">On-Time Rate</span>
            <div className="p-2 bg-accent-500/10 text-accent-400 rounded-xl border border-accent-500/20">
              <HiCheckCircle className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-accent-400">{metrics.onTimeDeliveryRate}%</p>
          <p className="text-[11px] text-accent-400 font-medium">SLA Target Exceeded</p>
        </div>

        {/* Avg Delivery Duration */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-5 rounded-3xl shadow-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Avg Transit</span>
            <div className="p-2 bg-warning-500/10 text-warning-400 rounded-xl border border-warning-500/20">
              <HiClock className="w-5 h-5" />
            </div>
          </div>
          <p className="text-2xl font-black text-warning-400">{metrics.avgDeliveryHours} hrs</p>
          <p className="text-[11px] text-gray-400 font-medium">Order to delivery average</p>
        </div>
      </div>

      {/* Charts Grid (Volume Series + Status Distribution) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Shipment Volume Trend Line Chart */}
        <div className="lg:col-span-2 bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-white">Shipment Creation & Delivery Volume</h3>
              <p className="text-xs text-gray-400">Daily throughput trends over the last 14 days</p>
            </div>
            <span className="text-[10px] font-mono text-gray-400 bg-surface-900 px-2.5 py-1 rounded-lg border border-white/5">
              14-Day View
            </span>
          </div>
          <div className="h-64 pt-2">
            <ShipmentVolumeChart dataSeries={volumeSeries.length > 0 ? volumeSeries : undefined} />
          </div>
        </div>

        {/* Right 1 Col: Status Distribution Doughnut Chart */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
          <div>
            <h3 className="text-sm font-bold text-white">Status Breakdown</h3>
            <p className="text-xs text-gray-400">Pipeline distribution percentage</p>
          </div>
          <div className="h-64 pt-2">
            <StatusDistributionChart distribution={distribution || undefined} />
          </div>
        </div>
      </div>

      {/* Grid Layout: Fleet Quick Stats + Recent Activity Log */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 1 Col: Fleet Assets Overview */}
        <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <HiUserGroup className="w-4 h-4 text-primary-400" />
            <span>Active Fleet Status</span>
          </h3>

          <div className="space-y-3">
            <div className="p-3.5 bg-surface-900/60 rounded-2xl border border-white/5 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-accent-500/20 text-accent-400 rounded-xl flex items-center justify-center font-bold">
                  8
                </div>
                <div>
                  <p className="text-xs font-bold text-white">Active Couriers</p>
                  <p className="text-[10px] text-gray-400">Currently on delivery routes</p>
                </div>
              </div>
              <span className="text-[10px] font-bold text-accent-400 bg-accent-500/10 px-2 py-0.5 rounded-md border border-accent-500/20">
                100% Duty
              </span>
            </div>

            <div className="p-3.5 bg-surface-900/60 rounded-2xl border border-white/5 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-primary-500/20 text-primary-400 rounded-xl flex items-center justify-center font-bold">
                  12
                </div>
                <div>
                  <p className="text-xs font-bold text-white">Fleet Vehicles</p>
                  <p className="text-[10px] text-gray-400">Vans & Cargo trucks deployed</p>
                </div>
              </div>
              <span className="text-[10px] font-bold text-primary-400 bg-primary-500/10 px-2 py-0.5 rounded-md border border-primary-500/20">
                Active
              </span>
            </div>

            <div className="p-3.5 bg-surface-900/60 rounded-2xl border border-white/5 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-amber-500/20 text-amber-400 rounded-xl flex items-center justify-center font-bold">
                  2
                </div>
                <div>
                  <p className="text-xs font-bold text-white">Delayed Alerts</p>
                  <p className="text-[10px] text-gray-400">Shipments flagged for delay</p>
                </div>
              </div>
              <Link
                to="/admin/shipments"
                className="text-[10px] font-bold text-amber-400 hover:underline"
              >
                Inspect
              </Link>
            </div>
          </div>
        </div>

        {/* Right 2 Cols: Recent Activity Log Table */}
        <div className="lg:col-span-2 bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <HiSparkles className="w-4 h-4 text-warning-400" />
              <span>Recent Activity Stream</span>
            </h3>
            <Link
              to="/admin/shipments"
              className="text-xs text-primary-400 hover:text-primary-300 font-semibold flex items-center gap-1"
            >
              <span>View All</span>
              <HiArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-900/60 border-b border-white/10 text-gray-400 uppercase font-semibold text-[10px] tracking-wider">
                <tr>
                  <th className="px-4 py-3">Tracking #</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Receiver</th>
                  <th className="px-4 py-3">Courier</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {recentShipments.map((shp) => (
                  <tr key={shp.id} className="hover:bg-white/[0.02]">
                    <td className="px-4 py-3 font-mono font-bold text-primary-400">
                      {shp.trackingNumber}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={shp.status} size="small" />
                    </td>
                    <td className="px-4 py-3 font-medium text-white">
                      {shp.receiverName}
                    </td>
                    <td className="px-4 py-3 text-gray-300">
                      {shp.assignedDriverName || 'Unassigned'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
