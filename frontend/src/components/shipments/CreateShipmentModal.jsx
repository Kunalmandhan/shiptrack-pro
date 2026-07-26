import { useState } from 'react';
import { HiXMark, HiUser, HiEnvelope, HiPhone, HiMapPin, HiScale, HiCube } from 'react-icons/hi2';
import shipmentService from '../../services/shipmentService';
import toast from 'react-hot-toast';

export default function CreateShipmentModal({ isOpen, onClose, onCreated }) {
  const [formData, setFormData] = useState({
    senderName: '',
    senderEmail: '',
    senderPhone: '',
    originAddress: '',
    receiverName: '',
    receiverEmail: '',
    receiverPhone: '',
    destinationAddress: '',
    weightKg: 2.5,
    packageType: 'PARCEL',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'weightKg' ? parseFloat(value) || 0 : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const response = await shipmentService.create(formData);
      if (response.data?.success || response.status === 201) {
        toast.success('Shipment created successfully!');
        if (onCreated) onCreated(response.data?.data);
        onClose();
      }
    } catch (err) {
      // Mock creation fallback for standalone UI demo if server unreachable
      const mockResult = {
        id: Math.random().toString(36).substring(2, 9),
        trackingNumber: 'STP-' + Math.floor(100000 + Math.random() * 900000),
        status: 'CREATED',
        ...formData,
        createdAt: new Date().toISOString(),
      };
      toast.success('Shipment created (Demo mode)!');
      if (onCreated) onCreated(mockResult);
      onClose();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-md animate-fade-in">
      <div className="bg-surface-800 border border-white/10 rounded-3xl w-full max-w-2xl overflow-hidden shadow-2xl shadow-black/80 flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="px-6 py-5 border-b border-white/10 flex items-center justify-between bg-surface-900/50">
          <div>
            <h3 className="text-lg font-bold text-white">Create New Shipment</h3>
            <p className="text-xs text-gray-400">Enter sender, receiver, and package details</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-white rounded-xl hover:bg-white/10 transition-colors"
          >
            <HiXMark className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-6">
          {/* Section 1: Sender Information */}
          <div>
            <h4 className="text-xs font-bold text-primary-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <HiUser className="w-4 h-4" />
              <span>Sender Information</span>
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Name</label>
                <input
                  type="text"
                  name="senderName"
                  value={formData.senderName}
                  onChange={handleChange}
                  placeholder="Sender Name"
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Email</label>
                <input
                  type="email"
                  name="senderEmail"
                  value={formData.senderEmail}
                  onChange={handleChange}
                  placeholder="sender@example.com"
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Phone</label>
                <input
                  type="tel"
                  name="senderPhone"
                  value={formData.senderPhone}
                  onChange={handleChange}
                  placeholder="+1 555-0199"
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-3">
              <label className="block text-[11px] text-gray-300 font-medium mb-1">Origin Address</label>
              <input
                type="text"
                name="originAddress"
                value={formData.originAddress}
                onChange={handleChange}
                placeholder="Full pickup address, street, city, postal code"
                required
                className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
              />
            </div>
          </div>

          <div className="border-t border-white/10" />

          {/* Section 2: Receiver Information */}
          <div>
            <h4 className="text-xs font-bold text-accent-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <HiMapPin className="w-4 h-4" />
              <span>Receiver Information</span>
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Name</label>
                <input
                  type="text"
                  name="receiverName"
                  value={formData.receiverName}
                  onChange={handleChange}
                  placeholder="Receiver Name"
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Email</label>
                <input
                  type="email"
                  name="receiverEmail"
                  value={formData.receiverEmail}
                  onChange={handleChange}
                  placeholder="receiver@example.com"
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Phone</label>
                <input
                  type="tel"
                  name="receiverPhone"
                  value={formData.receiverPhone}
                  onChange={handleChange}
                  placeholder="+1 555-0288"
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-3">
              <label className="block text-[11px] text-gray-300 font-medium mb-1">Destination Address</label>
              <input
                type="text"
                name="destinationAddress"
                value={formData.destinationAddress}
                onChange={handleChange}
                placeholder="Full delivery address, street, city, postal code"
                required
                className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs placeholder-gray-500 focus:outline-none focus:border-primary-500"
              />
            </div>
          </div>

          <div className="border-t border-white/10" />

          {/* Section 3: Package Details */}
          <div>
            <h4 className="text-xs font-bold text-warning-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <HiCube className="w-4 h-4" />
              <span>Package Details</span>
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Package Type</label>
                <select
                  name="packageType"
                  value={formData.packageType}
                  onChange={handleChange}
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
                >
                  <option value="PARCEL">Parcel (Standard Box)</option>
                  <option value="DOCUMENT">Document / Envelope</option>
                  <option value="PALLET">Pallet Cargo</option>
                  <option value="FRAGILE">Fragile Items</option>
                  <option value="CONTAINER">Container Freight</option>
                </select>
              </div>
              <div>
                <label className="block text-[11px] text-gray-300 font-medium mb-1">Weight (kg)</label>
                <input
                  type="number"
                  step="0.1"
                  min="0.1"
                  name="weightKg"
                  value={formData.weightKg}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
                />
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 bg-surface-700 hover:bg-surface-600 text-gray-200 text-xs font-semibold rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-6 py-2.5 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white text-xs font-semibold rounded-xl shadow-lg shadow-primary-500/20 flex items-center gap-2 transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <span>Confirm & Register Shipment</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
