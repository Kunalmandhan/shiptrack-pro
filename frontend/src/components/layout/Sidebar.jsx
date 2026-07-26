import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  HiOutlineHome,
  HiOutlineCube,
  HiOutlineLocationMarker,
  HiOutlineChartBar,
  HiOutlineCog,
  HiOutlineLogout,
  HiOutlineUsers,
  HiOutlineX,
} from 'react-icons/hi';

const adminLinks = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: HiOutlineHome },
  { to: '/admin/shipments', label: 'Shipments', icon: HiOutlineCube },
  { to: '/admin/tracking', label: 'Tracking', icon: HiOutlineLocationMarker },
  { to: '/admin/users', label: 'Users', icon: HiOutlineUsers },
  { to: '/admin/analytics', label: 'Analytics', icon: HiOutlineChartBar },
];

const customerLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: HiOutlineHome },
  { to: '/shipments', label: 'My Shipments', icon: HiOutlineCube },
  { to: '/tracking', label: 'Track', icon: HiOutlineLocationMarker },
];

/**
 * Sidebar navigation — adapts links based on user role (Admin vs Customer).
 * Responsive: overlay on mobile, fixed on desktop.
 */
export default function Sidebar({ isOpen, onClose }) {
  const { isAdmin, logout } = useAuth();
  const links = isAdmin ? adminLinks : customerLinks;

  const handleLogout = async () => {
    await logout();
  };

  const navLinkClass = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
      isActive
        ? 'bg-primary-600/20 text-primary-400 border-l-2 border-primary-400'
        : 'text-gray-400 hover:text-white hover:bg-surface-700'
    }`;

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed lg:static inset-y-0 left-0 z-50 w-64 bg-surface-800 border-r border-white/5 
          flex flex-col transform transition-transform duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}
      >
        {/* Logo area */}
        <div className="flex items-center justify-between h-16 px-6 border-b border-white/5">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center">
              <HiOutlineCube className="w-5 h-5 text-white" />
            </div>
            <span className="text-lg font-bold text-white">ShipTrack</span>
          </div>
          <button onClick={onClose} className="lg:hidden p-1 rounded hover:bg-surface-700">
            <HiOutlineX className="w-5 h-5 text-gray-400" />
          </button>
        </div>

        {/* Navigation links */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={navLinkClass}
              onClick={onClose}
            >
              <link.icon className="w-5 h-5 flex-shrink-0" />
              {link.label}
            </NavLink>
          ))}
        </nav>

        {/* Bottom actions */}
        <div className="px-3 py-4 border-t border-white/5 space-y-1">
          <NavLink to="/settings" className={navLinkClass} onClick={onClose}>
            <HiOutlineCog className="w-5 h-5 flex-shrink-0" />
            Settings
          </NavLink>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-4 py-2.5 rounded-lg text-sm font-medium text-gray-400 hover:text-danger-400 hover:bg-danger-500/10 transition-all duration-200"
          >
            <HiOutlineLogout className="w-5 h-5 flex-shrink-0" />
            Logout
          </button>
        </div>
      </aside>
    </>
  );
}
