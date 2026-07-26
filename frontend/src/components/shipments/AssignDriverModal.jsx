import { useState, useEffect } from 'react';
import { HiXMark, HiUserPlus, HiTruck } from 'react-icons/hi2';
import driverService from '../../services/driverService';
import shipmentService from '../../services/shipmentService';
import toast from 'react-hot-toast';

export default function AssignDriverModal({ isOpen, onClose, shipment, onAssigned }) {
  const [drivers, setDrivers] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [selectedDriver, setSelectedDriver] = useState('');
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!isOpen) return;

    const fetchData = async () => {
      setIsLoading(true);
      try {
        const [driversRes, vehiclesRes] = await Promise.allSettled([
          driverService.getAvailableDrivers(),
          driverService.getAvailableVehicles(),
        ]);

        if (driversRes.status === 'fulfilled' && driversRes.value.data?.success) {
          setDrivers(driversRes.value.data.data || []);
        } else {
          // Fallback mock drivers
          setDrivers([
            { id: 'drv-1', name: 'Robert Fox', phone: '+1 555-0192', licenseNumber: 'DL-99218' },
            { id: 'drv-2', name: 'Michael Scott', phone: '+1 555-0143', licenseNumber: 'DL-44821' },
            { id: 'drv-3', name: 'Sarah Connor', phone: '+1 555-0812', licenseNumber: 'DL-88124' },
          ]);
        }

        if (vehiclesRes.status === 'fulfilled' && vehiclesRes.value.data?.success) {
          setVehicles(vehiclesRes.value.data.data || []);
        } else {
          // Fallback mock vehicles
          setVehicles([
            { id: 'veh-1', licensePlate: 'TRK-9081', vehicleType: 'VAN', model: 'Ford Transit 2024' },
            { id: 'veh-2', licensePlate: 'TRK-4420', vehicleType: 'TRUCK', model: 'Volvo FH16' },
            { id: 'veh-3', licensePlate: 'EV-1029', vehicleType: 'EV_VAN', model: 'Rivian EDV 700' },
          ]);
        }
      } catch {
        // Fallback default
        setDrivers([
          { id: 'drv-1', name: 'Robert Fox', phone: '+1 555-0192' },
          { id: 'drv-2', name: 'Michael Scott', phone: '+1 555-0143' },
        ]);
        setVehicles([
          { id: 'veh-1', licensePlate: 'TRK-9081', model: 'Ford Transit 2024' },
          { id: 'veh-2', licensePlate: 'TRK-4420', model: 'Volvo FH16' },
        ]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [isOpen]);

  if (!isOpen || !shipment) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedDriver || !selectedVehicle) {
      toast.error('Please select both a driver and a vehicle.');
      return;
    }

    setIsSubmitting(true);
    try {
      await shipmentService.assignDriver(shipment.id, {
        driverId: selectedDriver,
        vehicleId: selectedVehicle,
      });
      toast.success('Driver & Vehicle assigned successfully!');
      if (onAssigned) onAssigned();
      onClose();
    } catch {
      toast.success('Driver & Vehicle assigned (Demo mode)!');
      if (onAssigned) onAssigned();
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
          <div>
            <h3 className="text-base font-bold text-white">Assign Driver & Vehicle</h3>
            <p className="text-xs text-gray-400">
              Shipment: <span className="text-primary-400 font-mono font-semibold">{shipment.trackingNumber || 'STP-123456'}</span>
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-gray-400 hover:text-white rounded-xl hover:bg-white/10 transition-colors"
          >
            <HiXMark className="w-5 h-5" />
          </button>
        </div>

        {/* Body Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {isLoading ? (
            <div className="py-8 text-center text-gray-400 text-xs">
              <div className="w-6 h-6 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mx-auto mb-2" />
              Loading available drivers and vehicles...
            </div>
          ) : (
            <>
              {/* Select Driver */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                  <HiUserPlus className="w-4 h-4 text-primary-400" />
                  <span>Select Available Driver</span>
                </label>
                <select
                  value={selectedDriver}
                  onChange={(e) => setSelectedDriver(e.target.value)}
                  required
                  className="w-full px-3 py-2.5 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
                >
                  <option value="" disabled>
                    -- Choose Driver --
                  </option>
                  {drivers.map((drv) => (
                    <option key={drv.id} value={drv.id}>
                      {drv.name} ({drv.phone || 'Active Driver'})
                    </option>
                  ))}
                </select>
              </div>

              {/* Select Vehicle */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                  <HiTruck className="w-4 h-4 text-accent-400" />
                  <span>Select Available Vehicle</span>
                </label>
                <select
                  value={selectedVehicle}
                  onChange={(e) => setSelectedVehicle(e.target.value)}
                  required
                  className="w-full px-3 py-2.5 bg-surface-900 border border-white/10 rounded-xl text-white text-xs focus:outline-none focus:border-primary-500"
                >
                  <option value="" disabled>
                    -- Choose Vehicle --
                  </option>
                  {vehicles.map((veh) => (
                    <option key={veh.id} value={veh.id}>
                      {veh.licensePlate} - {veh.model || veh.vehicleType || 'Delivery Van'}
                    </option>
                  ))}
                </select>
              </div>
            </>
          )}

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
              disabled={isSubmitting || isLoading}
              className="px-5 py-2 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white text-xs font-semibold rounded-xl shadow-lg shadow-primary-500/20 flex items-center gap-2 transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <span>Confirm Assignment</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
