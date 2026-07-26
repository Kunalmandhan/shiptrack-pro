import { Outlet, Link } from 'react-router-dom';
import { HiTruck } from 'react-icons/hi2';

/**
 * Modern Glassmorphic Layout for Authentication Pages.
 * Includes ShipTrack Pro brand logo, ambient glow backdrops, and centered card outlet.
 */
export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-surface-900 text-white flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Glowing Ambient Backdrop Accents */}
      <div className="absolute -top-32 -left-32 w-96 h-96 bg-primary-600/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-32 -right-32 w-96 h-96 bg-accent-500/20 rounded-full blur-3xl pointer-events-none" />

      {/* Brand Header */}
      <div className="mb-6 text-center z-10">
        <Link to="/" className="inline-flex items-center gap-3 group">
          <div className="p-3 bg-gradient-to-tr from-primary-600 to-accent-500 rounded-2xl shadow-lg shadow-primary-500/20 group-hover:scale-105 transition-transform duration-300">
            <HiTruck className="w-8 h-8 text-white" />
          </div>
          <div className="text-left">
            <h1 className="text-2xl font-black tracking-tight text-white flex items-center gap-1">
              ShipTrack <span className="text-primary-400">Pro</span>
            </h1>
            <p className="text-xs font-medium text-gray-400 uppercase tracking-widest">Visibility Platform</p>
          </div>
        </Link>
      </div>

      {/* Auth Card Content */}
      <div className="w-full max-w-md z-10">
        <Outlet />
      </div>

      {/* Footer Branding */}
      <div className="mt-8 text-center text-xs text-gray-500 z-10">
        &copy; {new Date().getFullYear()} ShipTrack Pro. All rights reserved.
      </div>
    </div>
  );
}
