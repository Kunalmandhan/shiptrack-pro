import { HiOutlineMenu } from 'react-icons/hi';
import { useAuth } from '../../context/AuthContext';
import { getInitials } from '../../utils/helpers';
import NotificationBell from '../notifications/NotificationBell';

/**
 * Top navigation bar — shows menu toggle, app name, NotificationBell, and user avatar.
 */
export default function Navbar({ onMenuClick }) {
  const { user } = useAuth();

  return (
    <header className="flex items-center justify-between h-16 px-6 bg-surface-800/80 backdrop-blur-md border-b border-white/5">
      {/* Left: Menu + Title */}
      <div className="flex items-center gap-4">
        <button
          onClick={onMenuClick}
          className="lg:hidden p-2 rounded-lg hover:bg-surface-700 transition-colors"
        >
          <HiOutlineMenu className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-bold bg-gradient-to-r from-primary-400 to-accent-400 bg-clip-text text-transparent tracking-tight">
          ShipTrack Pro
        </h1>
      </div>

      {/* Right: Notifications + Profile */}
      <div className="flex items-center gap-4">
        <NotificationBell />

        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center text-xs font-bold text-white shadow-md">
            {getInitials(user?.name || 'User')}
          </div>
          <div className="hidden sm:block">
            <p className="text-xs font-bold text-gray-200">{user?.name || 'Account User'}</p>
            <p className="text-[10px] font-mono text-gray-400 uppercase tracking-wider">{user?.roles?.[0] || 'CUSTOMER'}</p>
          </div>
        </div>
      </div>
    </header>
  );
}
