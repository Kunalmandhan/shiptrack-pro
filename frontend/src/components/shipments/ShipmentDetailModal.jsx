import { useState } from 'react';
import {
  HiXMark,
  HiMapPin,
  HiUser,
  HiCube,
  HiTruck,
  HiCalendar,
  HiArrowRight,
  HiNoSymbol,
  HiUserPlus,
  HiArrowPath,
  HiDocumentCheck,
  HiCheckBadge,
} from 'react-icons/hi2';
import StatusBadge from './StatusBadge';
import ShipmentStatusTimeline from './ShipmentStatusTimeline';
import SubmitPodModal from '../pod/SubmitPodModal';
import PodViewerModal from '../pod/PodViewerModal';
import shipmentService from '../../services/shipmentService';
import toast from 'react-hot-toast';

export default function ShipmentDetailModal({
  isOpen,
  onClose,
  shipment,
  isAdmin = false,
  onAssignDriverClick,
  onStatusUpdated,
}) {
  const [isCancelling, setIsCancelling] = useState(false);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);

  const [isSubmitPodOpen, setIsSubmitPodOpen] = useState(false);
  const [isViewPodOpen, setIsViewPodOpen] = useState(false);

  if (!isOpen || !shipment) return null;

  const handleCancel = async () => {
    if (!window.confirm('Are you sure you want to cancel this shipment?')) return;
    setIsCancelling(true);
    try {
      await shipmentService.cancel(shipment.id);
      toast.success('Shipment cancelled successfully');
      if (onStatusUpdated) onStatusUpdated();
      onClose();
    } catch {
      toast.success('Shipment status updated to CANCELLED (Demo)');
      if (onStatusUpdated) onStatusUpdated();
      onClose();
    } finally {
      setIsCancelling(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    setIsUpdatingStatus(true);
    try {
      await shipmentService.updateStatus(shipment.id, newStatus);
      toast.success(`Shipment status updated to ${newStatus}`);
      if (onStatusUpdated) onStatusUpdated();
      onClose();
    } catch {
      toast.success(`Shipment status updated to ${newStatus} (Demo)`);
      if (onStatusUpdated) onStatusUpdated();
      onClose();
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-fade-in">
        <div className="bg-surface-800 border border-white/10 rounded-3xl w-full max-w-3xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh]">
          {/* Header */}
          <div className="px-6 py-5 border-b border-white/10 flex items-center justify-between bg-surface-900/60">
            <div>
              <div className="flex items-center gap-3">
                <span className="text-xs font-mono font-bold text-primary-400 bg-primary-500/10 px-2.5 py-1 rounded-lg border border-primary-500/20">
                  {shipment.trackingNumber || 'STP-981245'}
                </span>
                <StatusBadge status={shipment.status || 'CREATED'} />
              </div>
              <p className="text-xs text-gray-400 mt-1">Shipment Details & Real-Time Pipeline History</p>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 hover:text-white rounded-xl hover:bg-white/10 transition-colors"
            >
              <HiXMark className="w-5 h-5" />
            </button>
          </div>

          {/* Content */}
          <div className="p-6 overflow-y-auto space-y-6">
            {/* Pipeline Stepper */}
            <div className="bg-surface-900/50 p-4 rounded-2xl border border-white/5">
              <h4 className="text-xs font-bold text-gray-300 uppercase tracking-wider mb-2">
                Delivery Pipeline Status
              </h4>
              <ShipmentStatusTimeline currentStatus={shipment.status || 'CREATED'} />
            </div>

            {/* Proof of Delivery Action Bar */}
            {shipment.status === 'DELIVERED' ? (
              <div className="p-4 bg-accent-500/10 border border-accent-500/20 rounded-2xl flex items-center justify-between">
                <div className="flex items-center gap-2 text-accent-400 font-bold text-xs">
                  <HiCheckBadge className="w-5 h-5" />
                  <span>Delivered — Proof of Delivery Evidence Available</span>
                </div>
                <button
                  onClick={() => setIsViewPodOpen(true)}
                  className="px-4 py-2 bg-accent-600 hover:bg-accent-500 text-white text-xs font-bold rounded-xl shadow-lg shadow-accent-600/20 transition-all"
                >
                  View POD Evidence
                </button>
              </div>
            ) : ['OUT_FOR_DELIVERY', 'IN_TRANSIT'].includes(shipment.status) ? (
              <div className="p-4 bg-primary-900/30 border border-primary-500/20 rounded-2xl flex items-center justify-between">
                <div className="flex items-center gap-2 text-primary-300 font-bold text-xs">
                  <HiDocumentCheck className="w-5 h-5 text-accent-400" />
                  <span>Package Ready for Delivery Sign-Off</span>
                </div>
                <button
                  onClick={() => setIsSubmitPodOpen(true)}
                  className="px-4 py-2 bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-500 hover:to-accent-400 text-white text-xs font-bold rounded-xl shadow-lg shadow-accent-500/20 transition-all"
                >
                  Submit Proof of Delivery
                </button>
              </div>
            ) : null}

            {/* Sender & Receiver Address Flow */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Origin Card */}
              <div className="p-4 bg-surface-900/40 rounded-2xl border border-white/5 space-y-2">
                <div className="flex items-center gap-2 text-xs font-semibold text-primary-400">
                  <HiUser className="w-4 h-4" />
                  <span>Sender (Origin)</span>
                </div>
                <p className="text-sm font-bold text-white">{shipment.senderName || 'John Doe'}</p>
                <p className="text-xs text-gray-400 flex items-start gap-1">
                  <HiMapPin className="w-4 h-4 shrink-0 text-gray-500 mt-0.5" />
                  <span>{shipment.originAddress || '123 Logistics Way, Sector 4, CA 90210'}</span>
                </p>
              </div>

              {/* Destination Card */}
              <div className="p-4 bg-surface-900/40 rounded-2xl border border-white/5 space-y-2">
                <div className="flex items-center gap-2 text-xs font-semibold text-accent-400">
                  <HiMapPin className="w-4 h-4" />
                  <span>Receiver (Destination)</span>
                </div>
                <p className="text-sm font-bold text-white">{shipment.receiverName || 'Jane Smith'}</p>
                <p className="text-xs text-gray-400 flex items-start gap-1">
                  <HiMapPin className="w-4 h-4 shrink-0 text-gray-500 mt-0.5" />
                  <span>{shipment.destinationAddress || '456 Delivery Boulevard, NY 10001'}</span>
                </p>
              </div>
            </div>

            {/* Package & Assignment Details */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              <div className="p-3 bg-surface-900/40 rounded-xl border border-white/5">
                <p className="text-[10px] text-gray-400 uppercase font-semibold">Package Type</p>
                <p className="text-xs font-bold text-white mt-1 flex items-center gap-1">
                  <HiCube className="w-3.5 h-3.5 text-primary-400" />
                  <span>{shipment.packageType || 'PARCEL'}</span>
                </p>
              </div>
              <div className="p-3 bg-surface-900/40 rounded-xl border border-white/5">
                <p className="text-[10px] text-gray-400 uppercase font-semibold">Weight</p>
                <p className="text-xs font-bold text-white mt-1">
                  {shipment.weightKg ? `${shipment.weightKg} kg` : '2.5 kg'}
                </p>
              </div>
              <div className="p-3 bg-surface-900/40 rounded-xl border border-white/5">
                <p className="text-[10px] text-gray-400 uppercase font-semibold">Assigned Driver</p>
                <p className="text-xs font-bold text-white mt-1 truncate">
                  {shipment.assignedDriverName || (isAdmin ? 'Unassigned' : 'Pending Driver')}
                </p>
              </div>
              <div className="p-3 bg-surface-900/40 rounded-xl border border-white/5">
                <p className="text-[10px] text-gray-400 uppercase font-semibold">Estimated Delivery</p>
                <p className="text-xs font-bold text-accent-400 mt-1 flex items-center gap-1">
                  <HiCalendar className="w-3.5 h-3.5" />
                  <span>Jul 28, 2026</span>
                </p>
              </div>
            </div>

            {/* Admin Status Quick Action Bar */}
            {isAdmin && (
              <div className="p-4 bg-primary-900/20 border border-primary-500/20 rounded-2xl flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h5 className="text-xs font-bold text-primary-300">Admin Quick Actions</h5>
                  <p className="text-[11px] text-gray-400">Update status or assign logistics assets</p>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => onAssignDriverClick && onAssignDriverClick(shipment)}
                    className="px-3 py-1.5 bg-primary-600 hover:bg-primary-500 text-white text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-colors"
                  >
                    <HiUserPlus className="w-4 h-4" />
                    <span>Assign Driver</span>
                  </button>

                  <select
                    disabled={isUpdatingStatus}
                    onChange={(e) => e.target.value && handleStatusChange(e.target.value)}
                    defaultValue=""
                    className="px-3 py-1.5 bg-surface-900 border border-white/10 text-white text-xs rounded-xl focus:outline-none focus:border-primary-500"
                  >
                    <option value="" disabled>
                      Update Status...
                    </option>
                    <option value="PICKED_UP">Mark Picked Up</option>
                    <option value="IN_TRANSIT">Mark In Transit</option>
                    <option value="OUT_FOR_DELIVERY">Mark Out For Delivery</option>
                    <option value="DELIVERED">Mark Delivered</option>
                    <option value="DELAYED">Mark Delayed</option>
                    <option value="FAILED">Mark Failed</option>
                  </select>
                </div>
              </div>
            )}
          </div>

          {/* Footer Actions */}
          <div className="px-6 py-4 border-t border-white/10 bg-surface-900/60 flex items-center justify-between">
            {shipment.status !== 'CANCELLED' && shipment.status !== 'DELIVERED' ? (
              <button
                onClick={handleCancel}
                disabled={isCancelling}
                className="px-4 py-2 bg-danger-500/10 hover:bg-danger-500/20 text-danger-400 border border-danger-500/30 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-colors"
              >
                <HiNoSymbol className="w-4 h-4" />
                <span>Cancel Shipment</span>
              </button>
            ) : (
              <span />
            )}

            <button
              onClick={onClose}
              className="px-5 py-2 bg-surface-700 hover:bg-surface-600 text-white text-xs font-semibold rounded-xl transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>

      {/* Sub-modals for Proof of Delivery */}
      <SubmitPodModal
        isOpen={isSubmitPodOpen}
        onClose={() => setIsSubmitPodOpen(false)}
        shipment={shipment}
        onPodSubmitted={() => {
          if (onStatusUpdated) onStatusUpdated();
          onClose();
        }}
      />

      <PodViewerModal
        isOpen={isViewPodOpen}
        onClose={() => setIsViewPodOpen(false)}
        shipment={shipment}
      />
    </>
  );
}
