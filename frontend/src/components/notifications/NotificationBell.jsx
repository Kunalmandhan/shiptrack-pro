import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { HiBell, HiCheckCircle, HiTruck, HiCube, HiExclamationCircle } from 'react-icons/hi2';
import notificationService from '../../services/notificationService';
import toast from 'react-hot-toast';

const MOCK_NOTIFICATIONS = [
  {
    id: 'notif-1',
    title: 'Shipment Delivered!',
    message: 'Package STP-881924 was successfully delivered to Apex Logistics.',
    type: 'DELIVERED',
    read: false,
    createdAt: '2026-07-26T12:30:00Z',
  },
  {
    id: 'notif-2',
    title: 'Courier Assigned',
    message: 'Robert Fox has been assigned to driver your shipment STP-774109.',
    type: 'DRIVER_ASSIGNED',
    read: false,
    createdAt: '2026-07-26T10:15:00Z',
  },
  {
    id: 'notif-3',
    title: 'Out for Delivery',
    message: 'Shipment STP-663812 is out for delivery in Seattle, WA.',
    type: 'IN_TRANSIT',
    read: true,
    createdAt: '2026-07-25T16:00:00Z',
  },
];

export default function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState(MOCK_NOTIFICATIONS);
  const [unreadCount, setUnreadCount] = useState(2);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const [listRes, countRes] = await Promise.allSettled([
          notificationService.getMyNotifications({ size: 5 }),
          notificationService.getUnreadCount(),
        ]);

        if (listRes.status === 'fulfilled' && listRes.value.data?.success?.content) {
          setNotifications(listRes.value.data.data.content);
        }
        if (countRes.status === 'fulfilled' && countRes.value.data?.success) {
          setUnreadCount(countRes.value.data.data.unreadCount || 0);
        }
      } catch {
        // Fallback default mock
      }
    };

    fetchNotifications();
  }, []);

  // Close dropdown on click outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleMarkAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
    setUnreadCount((prev) => Math.max(0, prev - 1));
    toast.success('Notification marked as read');
  };

  const handleMarkAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    setUnreadCount(0);
    toast.success('All notifications marked as read');
  };

  const getIcon = (type) => {
    switch (type) {
      case 'DELIVERED':
        return <HiCheckCircle className="w-4 h-4 text-accent-400" />;
      case 'DRIVER_ASSIGNED':
      case 'IN_TRANSIT':
        return <HiTruck className="w-4 h-4 text-primary-400" />;
      default:
        return <HiCube className="w-4 h-4 text-warning-400" />;
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell Button */}
      <button
        onClick={() => setIsOpen((prev) => !prev)}
        className="relative p-2.5 bg-surface-800 hover:bg-surface-700 text-gray-300 hover:text-white rounded-2xl border border-white/10 transition-colors"
      >
        <HiBell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-danger-500 text-[10px] font-bold text-white shadow-lg ring-2 ring-surface-900 animate-pulse">
            {unreadCount}
          </span>
        )}
      </button>

      {/* Floating Dropdown */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-surface-800 border border-white/10 rounded-3xl shadow-2xl overflow-hidden z-50 animate-fade-in">
          {/* Header */}
          <div className="px-4 py-3 border-b border-white/10 flex items-center justify-between bg-surface-900/80">
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-white">Notifications</span>
              {unreadCount > 0 && (
                <span className="text-[10px] font-bold text-danger-400 bg-danger-500/10 px-2 py-0.5 rounded-full border border-danger-500/20">
                  {unreadCount} unread
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllRead}
                className="text-[11px] font-semibold text-primary-400 hover:underline"
              >
                Mark all read
              </button>
            )}
          </div>

          {/* List */}
          <div className="divide-y divide-white/5 max-h-80 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="p-6 text-center text-xs text-gray-400">
                No notifications right now.
              </div>
            ) : (
              notifications.map((notif) => (
                <div
                  key={notif.id}
                  className={`p-3.5 flex items-start gap-3 hover:bg-white/[0.02] transition-colors ${
                    !notif.read ? 'bg-primary-500/5' : ''
                  }`}
                >
                  <div className="p-2 bg-surface-900 rounded-xl border border-white/5 shrink-0 mt-0.5">
                    {getIcon(notif.type)}
                  </div>

                  <div className="flex-1 space-y-0.5">
                    <div className="flex items-center justify-between">
                      <h4 className="text-xs font-bold text-white">{notif.title}</h4>
                      {!notif.read && (
                        <span className="w-2 h-2 rounded-full bg-primary-500 shrink-0" />
                      )}
                    </div>
                    <p className="text-[11px] text-gray-400 leading-relaxed">{notif.message}</p>
                    <p className="text-[10px] font-mono text-gray-500 pt-1">
                      {new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>

                  {!notif.read && (
                    <button
                      onClick={() => handleMarkAsRead(notif.id)}
                      title="Mark as read"
                      className="text-gray-500 hover:text-white p-1"
                    >
                      ✓
                    </button>
                  )}
                </div>
              ))
            )}
          </div>

          {/* Footer Link */}
          <div className="p-3 border-t border-white/10 bg-surface-900/60 text-center">
            <Link
              to="/notifications"
              onClick={() => setIsOpen(false)}
              className="text-xs font-bold text-primary-400 hover:text-primary-300"
            >
              View Notification Center →
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
