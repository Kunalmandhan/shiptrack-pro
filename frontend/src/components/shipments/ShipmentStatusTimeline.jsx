import { HiCheck, HiClock } from 'react-icons/hi2';

const STEPS = [
  { status: 'CREATED', label: 'Order Created' },
  { status: 'PICKED_UP', label: 'Picked Up' },
  { status: 'IN_TRANSIT', label: 'In Transit' },
  { status: 'OUT_FOR_DELIVERY', label: 'Out for Delivery' },
  { status: 'DELIVERED', label: 'Delivered' },
];

const STATUS_ORDER = {
  CREATED: 1,
  PROCESSING: 1,
  PICKED_UP: 2,
  IN_TRANSIT: 3,
  OUT_FOR_DELIVERY: 4,
  DELIVERED: 5,
};

export default function ShipmentStatusTimeline({ currentStatus, history = [] }) {
  const currentLevel = STATUS_ORDER[currentStatus] || 1;
  const isCancelled = currentStatus === 'CANCELLED';
  const isFailed = currentStatus === 'FAILED';

  if (isCancelled || isFailed) {
    return (
      <div className="p-4 bg-danger-500/10 border border-danger-500/30 rounded-2xl text-center">
        <p className="text-sm font-semibold text-danger-400">
          Shipment {isCancelled ? 'Cancelled' : 'Failed'}
        </p>
        <p className="text-xs text-gray-400 mt-1">
          This shipment is no longer active in the delivery pipeline.
        </p>
      </div>
    );
  }

  return (
    <div className="w-full py-4">
      <div className="flex items-center justify-between relative">
        {/* Background Connecting Line */}
        <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 h-0.5 bg-white/10 z-0" />

        {STEPS.map((step, idx) => {
          const stepLevel = idx + 1;
          const isCompleted = currentLevel > stepLevel;
          const isCurrent = currentLevel === stepLevel;

          return (
            <div key={step.status} className="flex flex-col items-center relative z-10">
              {/* Step Icon Indicator */}
              <div
                className={`w-9 h-9 rounded-full flex items-center justify-center font-bold text-xs transition-all duration-300 ${
                  isCompleted
                    ? 'bg-accent-500 text-white shadow-lg shadow-accent-500/30'
                    : isCurrent
                    ? 'bg-primary-500 text-white ring-4 ring-primary-500/30 animate-pulse'
                    : 'bg-surface-800 border border-white/20 text-gray-500'
                }`}
              >
                {isCompleted ? (
                  <HiCheck className="w-5 h-5" />
                ) : isCurrent ? (
                  <span className="w-2.5 h-2.5 bg-white rounded-full" />
                ) : (
                  <span>{stepLevel}</span>
                )}
              </div>

              {/* Step Label */}
              <p
                className={`text-[11px] font-medium mt-2 text-center max-w-[80px] ${
                  isCurrent
                    ? 'text-primary-400 font-semibold'
                    : isCompleted
                    ? 'text-gray-300'
                    : 'text-gray-500'
                }`}
              >
                {step.label}
              </p>
            </div>
          );
        })}
      </div>
    </div>
  );
}
