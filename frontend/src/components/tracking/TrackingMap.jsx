import { HiMapPin, HiTruck, HiSignal, HiClock, HiSparkles } from 'react-icons/hi2';

export default function TrackingMap({
  origin = 'Origin Pickup',
  destination = 'Destination Address',
  driverPosition = { lat: 37.7749, lng: -122.4194, speed: 65, heading: 45 },
  status = 'IN_TRANSIT',
  distanceRemaining = '14.2 km',
  eta = '28 mins',
}) {
  return (
    <div className="relative w-full h-[380px] bg-surface-900/90 backdrop-blur-xl border border-white/10 rounded-3xl overflow-hidden shadow-2xl flex flex-col justify-between p-6">
      {/* Decorative Dark Grid Pattern Background */}
      <div
        className="absolute inset-0 opacity-15 pointer-events-none"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, rgba(255, 255, 255, 0.2) 1px, transparent 0)`,
          backgroundSize: '24px 24px',
        }}
      />

      {/* Top Map Status Bar Overlay */}
      <div className="relative z-10 flex flex-wrap items-center justify-between gap-3 bg-surface-800/80 backdrop-blur-md p-3.5 rounded-2xl border border-white/10 shadow-lg">
        <div className="flex items-center gap-2">
          <span className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-accent-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-3 w-3 bg-accent-500" />
          </span>
          <span className="text-xs font-bold text-white tracking-wide">Live GPS Pipeline</span>
          <span className="text-[10px] text-accent-400 font-mono bg-accent-500/10 px-2 py-0.5 rounded-md border border-accent-500/20">
            GPS Active
          </span>
        </div>

        <div className="flex items-center gap-4 text-xs">
          <div className="flex items-center gap-1.5 text-gray-300 font-medium">
            <HiClock className="w-4 h-4 text-primary-400" />
            <span>ETA: <strong className="text-white">{eta}</strong></span>
          </div>
          <div className="flex items-center gap-1.5 text-gray-300 font-medium">
            <HiSparkles className="w-4 h-4 text-warning-400" />
            <span>Dist: <strong className="text-white">{distanceRemaining}</strong></span>
          </div>
        </div>
      </div>

      {/* Center Interactive Map Canvas Visualizer */}
      <div className="relative w-full h-full flex items-center justify-between px-8 py-6 my-auto">
        {/* SVG Route Line Connecting Points */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none z-0">
          <path
            d="M 120 180 Q 300 80, 500 180 T 800 180"
            fill="none"
            stroke="url(#route-gradient)"
            strokeWidth="4"
            strokeDasharray="8 6"
            className="animate-pulse"
          />
          <defs>
            <linearGradient id="route-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#10b981" />
              <stop offset="50%" stopColor="#6366f1" />
              <stop offset="100%" stopColor="#f59e0b" />
            </linearGradient>
          </defs>
        </svg>

        {/* 1. Origin Marker */}
        <div className="relative z-10 flex flex-col items-center group">
          <div className="p-3 bg-accent-500/20 text-accent-400 border border-accent-500/40 rounded-2xl shadow-lg shadow-accent-500/20 group-hover:scale-110 transition-transform">
            <HiMapPin className="w-6 h-6" />
          </div>
          <div className="mt-2 bg-surface-800/90 px-3 py-1 rounded-xl border border-white/10 text-center">
            <p className="text-[10px] uppercase font-bold text-accent-400">Origin</p>
            <p className="text-xs font-semibold text-white max-w-[100px] truncate">{origin}</p>
          </div>
        </div>

        {/* 2. Driver Vehicle Live Marker */}
        <div className="relative z-20 flex flex-col items-center">
          {/* Radar Ring Animation */}
          <div className="relative">
            <div className="absolute -inset-3 bg-primary-500/30 rounded-full animate-ping pointer-events-none" />
            <div className="p-4 bg-gradient-to-tr from-primary-600 to-primary-400 text-white rounded-2xl shadow-xl shadow-primary-500/50 ring-4 ring-primary-500/30">
              <HiTruck className="w-7 h-7 animate-bounce" />
            </div>
          </div>
          <div className="mt-2 bg-primary-900/90 px-3.5 py-1.5 rounded-xl border border-primary-500/40 text-center shadow-lg">
            <p className="text-[10px] uppercase font-bold text-primary-300 flex items-center justify-center gap-1">
              <HiSignal className="w-3 h-3 text-accent-400" />
              <span>Driver On Route</span>
            </p>
            <p className="text-xs font-mono font-bold text-white">{driverPosition.speed || 65} km/h</p>
          </div>
        </div>

        {/* 3. Destination Marker */}
        <div className="relative z-10 flex flex-col items-center group">
          <div className="p-3 bg-warning-500/20 text-warning-400 border border-warning-500/40 rounded-2xl shadow-lg shadow-warning-500/20 group-hover:scale-110 transition-transform">
            <HiMapPin className="w-6 h-6" />
          </div>
          <div className="mt-2 bg-surface-800/90 px-3 py-1 rounded-xl border border-white/10 text-center">
            <p className="text-[10px] uppercase font-bold text-warning-400">Destination</p>
            <p className="text-xs font-semibold text-white max-w-[100px] truncate">{destination}</p>
          </div>
        </div>
      </div>

      {/* Bottom Coordinates & Scale Bar */}
      <div className="relative z-10 flex items-center justify-between text-[11px] text-gray-400 bg-surface-800/60 px-4 py-2 rounded-xl border border-white/5 font-mono">
        <span>Lat: {driverPosition.lat.toFixed(4)} | Lng: {driverPosition.lng.toFixed(4)}</span>
        <span className="text-gray-500">Scale: 1 : 50,000</span>
      </div>
    </div>
  );
}
