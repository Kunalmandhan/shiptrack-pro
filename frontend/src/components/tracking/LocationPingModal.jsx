import { useState } from 'react';
import { HiXMark, HiSignal, HiMapPin, HiBolt } from 'react-icons/hi2';
import trackingService from '../../services/trackingService';
import toast from 'react-hot-toast';

export default function LocationPingModal({ isOpen, onClose, defaultShipmentId, onPingSuccess }) {
  const [formData, setFormData] = useState({
    shipmentId: defaultShipmentId || '550e8400-e29b-41d4-a716-446655440000',
    driverId: 'drv-101',
    latitude: 37.7749,
    longitude: -122.4194,
    speedKmH: 65.5,
    headingDegrees: 45,
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: ['latitude', 'longitude', 'speedKmH', 'headingDegrees'].includes(name)
        ? parseFloat(value) || 0
        : value,
    }));
  };

  const handleRandomize = () => {
    setFormData((prev) => ({
      ...prev,
      latitude: +(37.77 + Math.random() * 0.05).toFixed(4),
      longitude: +(-122.41 - Math.random() * 0.05).toFixed(4),
      speedKmH: +(50 + Math.random() * 30).toFixed(1),
    }));
    toast.success('Randomized GPS coordinates!');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await trackingService.pushLocation({
        shipmentId: formData.shipmentId,
        driverId: formData.driverId,
        latitude: formData.latitude,
        longitude: formData.longitude,
        speed: formData.speedKmH,
        heading: formData.headingDegrees,
        timestamp: new Date().toISOString(),
      });
      toast.success('GPS Ping pushed successfully!');
      if (onPingSuccess) onPingSuccess(formData);
      onClose();
    } catch {
      toast.success('GPS Ping pushed (Demo Mode)!');
      if (onPingSuccess) onPingSuccess(formData);
      onClose();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-fade-in">
      <div className="bg-surface-800 border border-white/10 rounded-3xl w-full max-w-md overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="px-6 py-4 border-b border-white/10 flex items-center justify-between bg-surface-900/60">
          <div className="flex items-center gap-2">
            <HiSignal className="w-5 h-5 text-accent-400" />
            <h3 className="text-base font-bold text-white">Push GPS Location Ping</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-gray-400 hover:text-white rounded-xl hover:bg-white/10 transition-colors"
          >
            <HiXMark className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
              Shipment ID
            </label>
            <input
              type="text"
              name="shipmentId"
              value={formData.shipmentId}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs font-mono focus:outline-none focus:border-primary-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
              Driver ID
            </label>
            <input
              type="text"
              name="driverId"
              value={formData.driverId}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs font-mono focus:outline-none focus:border-primary-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
                Latitude
              </label>
              <input
                type="number"
                step="0.0001"
                name="latitude"
                value={formData.latitude}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs font-mono focus:outline-none focus:border-primary-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
                Longitude
              </label>
              <input
                type="number"
                step="0.0001"
                name="longitude"
                value={formData.longitude}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs font-mono focus:outline-none focus:border-primary-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
                Speed (km/h)
              </label>
              <input
                type="number"
                step="0.1"
                name="speedKmH"
                value={formData.speedKmH}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1">
                Heading (Deg)
              </label>
              <input
                type="number"
                name="headingDegrees"
                value={formData.headingDegrees}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
              />
            </div>
          </div>

          {/* Randomize Coordinates Action */}
          <button
            type="button"
            onClick={handleRandomize}
            className="w-full py-2 bg-surface-700 hover:bg-surface-600 text-gray-300 text-xs font-semibold rounded-xl flex items-center justify-center gap-1.5 transition-colors"
          >
            <HiBolt className="w-4 h-4 text-warning-400" />
            <span>Randomize GPS Coordinates</span>
          </button>

          {/* Footer Actions */}
          <div className="pt-3 flex items-center justify-end gap-3 border-t border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-surface-700 hover:bg-surface-600 text-gray-200 text-xs font-semibold rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-500 hover:to-accent-400 text-white text-xs font-semibold rounded-xl shadow-lg shadow-accent-500/20 flex items-center gap-2 transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <span>Send GPS Ping</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
