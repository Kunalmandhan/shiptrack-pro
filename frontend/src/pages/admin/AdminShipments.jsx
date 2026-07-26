import { useState, useEffect } from 'react';
import {
  HiPlus,
  HiMagnifyingGlass,
  HiFunnel,
  HiCube,
  HiUserPlus,
  HiEye,
  HiTruck,
  HiArrowPath,
} from 'react-icons/hi2';
import StatusBadge from '../../components/shipments/StatusBadge';
import CreateShipmentModal from '../../components/shipments/CreateShipmentModal';
import ShipmentDetailModal from '../../components/shipments/ShipmentDetailModal';
import AssignDriverModal from '../../components/shipments/AssignDriverModal';
import shipmentService from '../../services/shipmentService';
import toast from 'react-hot-toast';

const MOCK_ADMIN_SHIPMENTS = [
  {
    id: 'shp-201',
    trackingNumber: 'STP-902184',
    status: 'IN_TRANSIT',
    senderName: 'Acme Corp',
    originAddress: '100 Industrial Parkway, Chicago, IL',
    receiverName: 'Apex Logistics',
    destinationAddress: '55 Fleet Street, New York, NY',
    weightKg: 25.0,
    packageType: 'PALLET',
    assignedDriverName: 'Robert Fox',
    assignedVehiclePlate: 'TRK-9081',
    createdAt: '2026-07-25T08:00:00Z',
  },
  {
    id: 'shp-202',
    trackingNumber: 'STP-881924',
    status: 'OUT_FOR_DELIVERY',
    senderName: 'John Doe',
    originAddress: '123 Tech Park, San Francisco, CA',
    receiverName: 'Sarah Jenkins',
    destinationAddress: '456 Oak Street, Seattle, WA',
    weightKg: 4.2,
    packageType: 'PARCEL',
    assignedDriverName: 'Michael Scott',
    assignedVehiclePlate: 'TRK-4420',
    createdAt: '2026-07-25T10:30:00Z',
  },
  {
    id: 'shp-203',
    trackingNumber: 'STP-774109',
    status: 'CREATED',
    senderName: 'TechGlobal Corp',
    originAddress: '12 Silicon Alley, Austin, TX',
    receiverName: 'Omega Retails',
    destinationAddress: '77 Market Square, Denver, CO',
    weightKg: 15.5,
    packageType: 'CONTAINER',
    assignedDriverName: null,
    assignedVehiclePlate: null,
    createdAt: '2026-07-26T09:15:00Z',
  },
  {
    id: 'shp-204',
    trackingNumber: 'STP-663812',
    status: 'DELAYED',
    senderName: 'BioPharm Inc',
    originAddress: '88 Lab Way, Boston, MA',
    receiverName: 'Central Hospital',
    destinationAddress: '200 Health Ave, Philadelphia, PA',
    weightKg: 3.0,
    packageType: 'FRAGILE',
    assignedDriverName: 'Sarah Connor',
    assignedVehiclePlate: 'EV-1029',
    createdAt: '2026-07-23T16:00:00Z',
  },
];

export default function AdminShipments() {
  const [shipments, setShipments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedShipment, setSelectedShipment] = useState(null);
  const [assignShipmentTarget, setAssignShipmentTarget] = useState(null);

  const fetchAllShipments = async () => {
    setIsLoading(true);
    try {
      const response = await shipmentService.list();
      if (response.data?.success && response.data?.data?.content) {
        setShipments(response.data.data.content);
      } else {
        setShipments(MOCK_ADMIN_SHIPMENTS);
      }
    } catch {
      setShipments(MOCK_ADMIN_SHIPMENTS);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAllShipments();
  }, []);

  const handleCreated = (newShipment) => {
    if (newShipment) {
      setShipments((prev) => [newShipment, ...prev]);
    }
    fetchAllShipments();
  };

  // Metrics calculation
  const totalCount = shipments.length;
  const inTransitCount = shipments.filter((s) => ['IN_TRANSIT', 'OUT_FOR_DELIVERY', 'PICKED_UP'].includes(s.status)).length;
  const deliveredCount = shipments.filter((s) => s.status === 'DELIVERED').length;
  const delayedCount = shipments.filter((s) => s.status === 'DELAYED').length;

  // Filtered List
  const filteredShipments = shipments.filter((shp) => {
    const matchesSearch =
      shp.trackingNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      shp.senderName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      shp.receiverName?.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;
    if (statusFilter !== 'ALL' && shp.status !== statusFilter) return false;
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight">Admin Shipment Management</h1>
          <p className="text-gray-400 text-xs mt-1">
            Platform-wide shipment tracking, asset assignment, and status control
          </p>
        </div>

        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="px-5 py-2.5 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white text-xs font-bold rounded-2xl shadow-lg shadow-primary-500/25 flex items-center justify-center gap-2 transition-all hover:scale-[1.02]"
        >
          <HiPlus className="w-4 h-4" />
          <span>Create Shipment</span>
        </button>
      </div>

      {/* KPI Stats Bar */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl flex items-center gap-3">
          <div className="p-3 bg-primary-500/10 text-primary-400 rounded-xl border border-primary-500/20">
            <HiCube className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] text-gray-400 font-semibold uppercase">Total Shipments</p>
            <p className="text-lg font-bold text-white">{totalCount}</p>
          </div>
        </div>

        <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl flex items-center gap-3">
          <div className="p-3 bg-blue-500/10 text-blue-400 rounded-xl border border-blue-500/20">
            <HiTruck className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] text-gray-400 font-semibold uppercase">In Pipeline</p>
            <p className="text-lg font-bold text-blue-400">{inTransitCount}</p>
          </div>
        </div>

        <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl flex items-center gap-3">
          <div className="p-3 bg-accent-500/10 text-accent-400 rounded-xl border border-accent-500/20">
            <HiCube className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] text-gray-400 font-semibold uppercase">Delivered</p>
            <p className="text-lg font-bold text-accent-400">{deliveredCount}</p>
          </div>
        </div>

        <div className="bg-surface-800/80 border border-white/10 p-4 rounded-2xl flex items-center gap-3">
          <div className="p-3 bg-amber-500/10 text-amber-400 rounded-xl border border-amber-500/20">
            <HiCube className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] text-gray-400 font-semibold uppercase">Delayed</p>
            <p className="text-lg font-bold text-amber-400">{delayedCount}</p>
          </div>
        </div>
      </div>

      {/* Filter & Toolbar */}
      <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-4 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-xl">
        <div className="flex items-center gap-3 w-full md:w-auto">
          {/* Status Dropdown Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3.5 py-2 bg-surface-900 border border-white/10 rounded-xl text-xs text-white focus:outline-none focus:border-primary-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="CREATED">Created</option>
            <option value="PICKED_UP">Picked Up</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
            <option value="DELIVERED">Delivered</option>
            <option value="DELAYED">Delayed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        {/* Search Bar */}
        <div className="relative w-full md:w-72">
          <HiMagnifyingGlass className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search tracking, sender, receiver..."
            className="w-full pl-10 pr-4 py-2 bg-surface-900 border border-white/10 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary-500"
          />
        </div>
      </div>

      {/* Data Table */}
      <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
        {isLoading ? (
          <div className="py-16 text-center text-gray-400">
            <div className="w-8 h-8 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs">Loading shipment platform data...</p>
          </div>
        ) : filteredShipments.length === 0 ? (
          <div className="py-12 text-center text-gray-400 text-xs">
            No shipments match the selected filters.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-900/60 border-b border-white/10 text-gray-400 uppercase font-semibold text-[10px] tracking-wider">
                <tr>
                  <th className="px-6 py-4">Tracking #</th>
                  <th className="px-6 py-4">Status</th>
                  <th className="px-6 py-4">Sender</th>
                  <th className="px-6 py-4">Receiver & Destination</th>
                  <th className="px-6 py-4">Driver / Vehicle</th>
                  <th className="px-6 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {filteredShipments.map((shp) => (
                  <tr key={shp.id} className="hover:bg-white/[0.02] transition-colors">
                    <td className="px-6 py-4 font-mono font-bold text-primary-400">
                      {shp.trackingNumber}
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge status={shp.status} size="small" />
                    </td>
                    <td className="px-6 py-4 font-medium text-white">
                      {shp.senderName || 'N/A'}
                    </td>
                    <td className="px-6 py-4">
                      <p className="font-semibold text-white">{shp.receiverName}</p>
                      <p className="text-[11px] text-gray-400 truncate max-w-xs">
                        {shp.destinationAddress}
                      </p>
                    </td>
                    <td className="px-6 py-4">
                      {shp.assignedDriverName ? (
                        <div>
                          <p className="font-semibold text-white">{shp.assignedDriverName}</p>
                          <p className="text-[10px] text-gray-400">{shp.assignedVehiclePlate || 'Assigned'}</p>
                        </div>
                      ) : (
                        <button
                          onClick={() => setAssignShipmentTarget(shp)}
                          className="px-2.5 py-1 bg-primary-500/10 hover:bg-primary-500/20 text-primary-400 border border-primary-500/20 text-[11px] font-semibold rounded-lg flex items-center gap-1 transition-colors"
                        >
                          <HiUserPlus className="w-3.5 h-3.5" />
                          <span>Assign</span>
                        </button>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setSelectedShipment(shp)}
                          className="p-2 bg-surface-700 hover:bg-surface-600 text-gray-200 rounded-xl transition-colors"
                          title="View Details"
                        >
                          <HiEye className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modals */}
      <CreateShipmentModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onCreated={handleCreated}
      />

      <ShipmentDetailModal
        isOpen={!!selectedShipment}
        onClose={() => setSelectedShipment(null)}
        shipment={selectedShipment}
        isAdmin={true}
        onAssignDriverClick={(shp) => {
          setSelectedShipment(null);
          setAssignShipmentTarget(shp);
        }}
        onStatusUpdated={fetchAllShipments}
      />

      <AssignDriverModal
        isOpen={!!assignShipmentTarget}
        onClose={() => setAssignShipmentTarget(null)}
        shipment={assignShipmentTarget}
        onAssigned={fetchAllShipments}
      />
    </div>
  );
}
