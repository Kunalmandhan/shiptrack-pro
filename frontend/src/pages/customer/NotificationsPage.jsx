import { useState, useEffect } from 'react';
import {
  HiBell,
  HiCheckCircle,
  HiTruck,
  HiCube,
  HiCheck,
  HiTrash,
  HiSparkles,
} from 'react-icons/hi2';
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
  {
    id: 'notif-4',
    title: 'Shipment Registered',
    message: 'New shipment STP-552901 was registered and assigned to processing.',
    type: 'CREATED',
    read: true,
    createdAt: '2026-07-24T09:00:00Z',
  },
];

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState(MOCK_NOTIFICATIONS);
  const [filter, setFilter] = useState('ALL'); // ALL, UNREAD, DELIVERED
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchNotifications = async () => {
      setIsLoading(true);
      try {
        const res = await notificationService.getMyNotifications({ size: 20 });
        if (res.data?.success?.content) {
          setNotifications(res.data.data.content);
        }
      } catch {
        // Fallback default
      } finally {
        setIsLoading(false);
      }
    };

    fetchNotifications();
  }, []);

  const handleMarkAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
    toast.success('Marked as read');
  };

  const handleMarkAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    toast.success('All notifications marked as read');
  };

  const filteredNotifications = notifications.filter((n) => {
    if (filter === 'UNREAD') return !n.read;
    if (filter === 'DELIVERED') return n.type === 'DELIVERED';
    return true;
  });

  const getIcon = (type) => {
    switch (type) {
      case 'DELIVERED':
        return <HiCheckCircle className="w-5 h-5 text-accent-400" />;
      case 'DRIVER_ASSIGNED':
      case 'IN_TRANSIT':
        return <HiTruck className="w-5 h-5 text-primary-400" />;
      default:
        return <HiCube className="w-5 h-5 text-warning-400" />;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="bg-surface-800/90 backdrop-blur-xl border border-white/10 p-6 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <HiBell className="w-5 h-5 text-primary-400" />
            <h1 className="text-2xl font-black text-white tracking-tight">Notification Center</h1>
          </div>
          <p className="text-gray-400 text-xs mt-1">
            Real-time delivery status alerts, courier assignments, and system notifications
          </p>
        </div>

        <button
          onClick={handleMarkAllRead}
          className="px-4 py-2 bg-surface-700 hover:bg-surface-600 text-white text-xs font-bold rounded-2xl flex items-center justify-center gap-2 transition-all border border-white/5"
        >
          <HiCheck className="w-4 h-4 text-accent-400" />
          <span>Mark All as Read</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 border-b border-white/10 pb-3">
        {['ALL', 'UNREAD', 'DELIVERED'].map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`px-4 py-2 text-xs font-semibold rounded-2xl transition-all ${
              filter === f
                ? 'bg-primary-600 text-white shadow-lg shadow-primary-600/25'
                : 'bg-surface-800/60 text-gray-400 hover:text-white border border-white/5'
            }`}
          >
            {f === 'ALL' ? 'All Alerts' : f === 'UNREAD' ? 'Unread Only' : 'Delivered Only'}
          </button>
        ))}
      </div>

      {/* Notifications List */}
      <div className="bg-surface-800/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6 shadow-2xl space-y-3">
        {filteredNotifications.length === 0 ? (
          <div className="py-12 text-center text-gray-400 text-xs">
            No notifications match your current filter.
          </div>
        ) : (
          filteredNotifications.map((notif) => (
            <div
              key={notif.id}
              className={`p-4 rounded-2xl border transition-all flex items-start justify-between gap-4 ${
                !notif.read
                  ? 'bg-surface-900 border-primary-500/30'
                  : 'bg-surface-900/40 border-white/5 opacity-80'
              }`}
            >
              <div className="flex items-start gap-4">
                <div className="p-3 bg-surface-800 rounded-2xl border border-white/10 shrink-0">
                  {getIcon(notif.type)}
                </div>
                <div>
                  <div className="flex items-center gap-3">
                    <h3 className="text-sm font-bold text-white">{notif.title}</h3>
                    {!notif.read && (
                      <span className="text-[10px] font-bold text-primary-400 bg-primary-500/10 px-2 py-0.5 rounded-md border border-primary-500/20">
                        New
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-gray-300 mt-1">{notif.message}</p>
                  <p className="text-[10px] font-mono text-gray-500 mt-2">
                    {new Date(notif.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>

              {!notif.read && (
                <button
                  onClick={() => handleMarkAsRead(notif.id)}
                  className="px-3 py-1.5 bg-surface-700 hover:bg-surface-600 text-white text-[11px] font-semibold rounded-xl transition-colors"
                >
                  Mark Read
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
