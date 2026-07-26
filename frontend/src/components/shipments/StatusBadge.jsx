import {
  HiPlusCircle,
  HiCog6Tooth,
  HiCube,
  HiTruck,
  HiPaperAirplane,
  HiCheckCircle,
  HiClock,
  HiXCircle,
  HiExclamationCircle,
} from 'react-icons/hi2';

const STATUS_CONFIG = {
  CREATED: {
    label: 'Created',
    bg: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
    icon: HiPlusCircle,
  },
  PROCESSING: {
    label: 'Processing',
    bg: 'bg-primary-500/10 text-primary-400 border-primary-500/20',
    icon: HiCog6Tooth,
  },
  PICKED_UP: {
    label: 'Picked Up',
    bg: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
    icon: HiCube,
  },
  IN_TRANSIT: {
    label: 'In Transit',
    bg: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
    icon: HiTruck,
  },
  OUT_FOR_DELIVERY: {
    label: 'Out for Delivery',
    bg: 'bg-warning-500/10 text-warning-400 border-warning-500/20',
    icon: HiPaperAirplane,
  },
  DELIVERED: {
    label: 'Delivered',
    bg: 'bg-accent-500/10 text-accent-400 border-accent-500/20',
    icon: HiCheckCircle,
  },
  DELAYED: {
    label: 'Delayed',
    bg: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
    icon: HiClock,
  },
  CANCELLED: {
    label: 'Cancelled',
    bg: 'bg-gray-500/10 text-gray-400 border-gray-500/20',
    icon: HiXCircle,
  },
  FAILED: {
    label: 'Failed',
    bg: 'bg-danger-500/10 text-danger-400 border-danger-500/20',
    icon: HiExclamationCircle,
  },
};

export default function StatusBadge({ status, size = 'normal' }) {
  const config = STATUS_CONFIG[status] || {
    label: status || 'Unknown',
    bg: 'bg-gray-500/10 text-gray-400 border-gray-500/20',
    icon: HiCube,
  };

  const Icon = config.icon;
  const sizeClasses =
    size === 'small'
      ? 'px-2 py-0.5 text-[10px] gap-1'
      : size === 'large'
      ? 'px-3.5 py-1.5 text-xs gap-2'
      : 'px-2.5 py-1 text-xs gap-1.5';

  return (
    <span
      className={`inline-flex items-center font-semibold rounded-full border ${config.bg} ${sizeClasses}`}
    >
      <Icon className={size === 'small' ? 'w-3 h-3' : 'w-3.5 h-3.5'} />
      <span>{config.label}</span>
    </span>
  );
}
