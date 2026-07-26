import { useState, useEffect } from 'react';
import {
  HiPlus,
  HiMagnifyingGlass,
  HiFunnel,
  HiCube,
  HiMapPin,
  HiChevronRight,
  HiArrowPath,
} from 'react-icons/hi2';
import StatusBadge from '../../components/shipments/StatusBadge';
import CreateShipmentModal from '../../components/shipments/CreateShipmentModal';
import ShipmentDetailModal from '../../components/shipments/ShipmentDetailModal';
import shipmentService from '../../services/shipmentService';
import toast from 'react-hot-toast';

const MOCK_MY_SHIPMENTS = [
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
  {
    id: 'shp-104',
    trackingNumber: 'STP-552019',
    status: 'CREATED',
    senderName: 'John Doe',
    originAddress: '123 Tech Park, San Francisco, CA',
    receiverName: 'Global Freight Hub',
    destinationAddress: '12 Port Road, Miami, FL',
    weightKg: 8.5,
    packageType: 'CONTAINER',
    assignedDriverName: null,
    createdAt: '2026-07-26T11:00:00Z',
  },
];

export default function MyShipments() {
  const [shipments, setShipments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'DELIVERED' | 'CANCELLED'

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedShipment, setSelectedShipment] = useState(null);

  const fetchShipments = async () => {
    setIsLoading(true);
    try {
      const response = await shipmentService.myShipments();
      if (response.data?.success && response.data?.data?.content) {
        setShipments(response.data.data.content);
      } else {
        setShipments(MOCK_MY_SHIPMENTS);
      }
    } catch {
      setShipments(MOCK_MY_SHIPMENTS);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchShipments();
  }, []);

  const handleCreated = (newShipment) => {
    if (newShipment) {
      setShipments((prev) => [newShipment, ...prev]);
    }
    fetchShipments();
  };

  // Filtering Logic
  const filteredShipments = shipments.filter((shp) => {
    const matchesSearch =
      shp.trackingNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      shp.receiverName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      shp.destinationAddress?.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    if (activeTab === 'ACTIVE') {
      return ['CREATED', 'PROCESSING', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELAYED'].includes(
        shp.status
      );
    }
    if (activeTab === 'DELIVERED') return shp.status === 'DELIVERED';
    if (activeTab === 'CANCELLED') return shp.status === 'CANCELLED';

    return true;
  });

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight">My Shipments</h1>
          <p className="text-gray-400 text-xs mt-1">
            Manage and track all packages sent or scheduled for delivery
          </p>
        </div>

        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="px-5 py-2.5 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white text-xs font-bold rounded-2xl shadow-lg shadow-primary-500/25 flex items-center justify-center gap-2 transition-all hover:scale-[1.02]"
        >
          <HiPlus className="w-4 h-4" />
          <span>New Shipment</span>
        </button>
      </div>

      {/* Filter & Search Toolbar */}
      <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 p-4 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-xl">
        {/* Status Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0">
          {[
            { id: 'ALL', label: 'All Shipments' },
            { id: 'ACTIVE', label: 'Active Pipeline' },
            { id: 'DELIVERED', label: 'Delivered' },
            { id: 'CANCELLED', label: 'Cancelled' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                activeTab === tab.id
                  ? 'bg-primary-600 text-white shadow-md shadow-primary-600/30'
                  : 'text-gray-400 hover:text-gray-200 hover:bg-white/5'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <div className="relative w-full md:w-72">
          <HiMagnifyingGlass className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search tracking # or receiver..."
            className="w-full pl-10 pr-4 py-2 bg-surface-900 border border-white/10 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary-500"
          />
        </div>
      </div>

      {/* Shipments Data Table / Cards */}
      {isLoading ? (
        <div className="py-16 text-center text-gray-400">
          <div className="w-8 h-8 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mx-auto mb-3" />
          <p className="text-xs">Fetching your shipments...</p>
        </div>
      ) : filteredShipments.length === 0 ? (
        <div className="bg-surface-800/50 border border-white/5 rounded-3xl p-12 text-center space-y-3">
          <div className="w-12 h-12 bg-surface-700 text-gray-400 rounded-2xl flex items-center justify-center mx-auto">
            <HiCube className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-white">No Shipments Found</h3>
          <p className="text-xs text-gray-400 max-w-sm mx-auto">
            {searchQuery
              ? `No shipments matching "${searchQuery}"`
              : 'You have not registered any shipments in this category yet.'}
          </p>
          {!searchQuery && (
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="mt-2 px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white text-xs font-semibold rounded-xl inline-flex items-center gap-1.5"
            >
              <HiPlus className="w-4 h-4" />
              <span>Create First Shipment</span>
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-3">
          {filteredShipments.map((shp) => (
            <div
              key={shp.id || shp.trackingNumber}
              onClick={() => setSelectedShipment(shp)}
              className="bg-surface-800/80 hover:bg-surface-800 border border-white/10 hover:border-primary-500/30 p-4 rounded-2xl transition-all duration-200 cursor-pointer flex flex-col md:flex-row md:items-center justify-between gap-4 group shadow-lg"
            >
              {/* Left Info */}
              <div className="flex items-start gap-4">
                <div className="p-3 bg-primary-500/10 text-primary-400 rounded-xl border border-primary-500/20 group-hover:scale-105 transition-transform">
                  <HiCube className="w-6 h-6" />
                </div>

                <div>
                  <div className="flex items-center gap-3">
                    <span className="font-mono font-bold text-sm text-white group-hover:text-primary-400 transition-colors">
                      {shp.trackingNumber}
                    </span>
                    <StatusBadge status={shp.status} size="small" />
                  </div>

                  <div className="flex items-center gap-4 mt-2 text-xs text-gray-400 flex-wrap">
                    <span className="flex items-center gap-1">
                      <HiMapPin className="w-3.5 h-3.5 text-accent-400 shrink-0" />
                      <span>To: {shp.receiverName} ({shp.destinationAddress})</span>
                    </span>
                    <span>•</span>
                    <span>Weight: {shp.weightKg} kg</span>
                  </div>
                </div>
              </div>

              {/* Right Action Trigger */}
              <div className="flex items-center justify-between md:justify-end gap-4 border-t md:border-t-0 pt-3 md:pt-0 border-white/5">
                <div className="text-right text-[11px] text-gray-500">
                  <span>Created: {new Date(shp.createdAt || Date.now()).toLocaleDateString()}</span>
                </div>
                <div className="p-2 bg-surface-700 text-gray-300 group-hover:bg-primary-600 group-hover:text-white rounded-xl transition-all">
                  <HiChevronRight className="w-4 h-4" />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

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
        isAdmin={false}
        onStatusUpdated={fetchShipments}
      />
    </div>
  );
}
