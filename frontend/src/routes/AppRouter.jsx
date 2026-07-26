import { createBrowserRouter, Navigate } from 'react-router-dom';
import { ProtectedRoute, PublicRoute } from './ProtectedRoute';
import { ROLES } from '../utils/constants';

// Layouts
import AppLayout from '../components/layout/AppLayout';
import AuthLayout from '../components/layout/AuthLayout';

// Auth Pages
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '../pages/auth/ResetPasswordPage';
import VerifyEmailPage from '../pages/auth/VerifyEmailPage';

// Shared Pages
import NotFoundPage from '../pages/NotFoundPage';
import UnauthorizedPage from '../pages/UnauthorizedPage';

// Customer Pages
import CustomerDashboard from '../pages/customer/CustomerDashboard';
import MyShipments from '../pages/customer/MyShipments';
import TrackShipment from '../pages/customer/TrackShipment';
import NotificationsPage from '../pages/customer/NotificationsPage';

// Admin Pages
import AdminDashboard from '../pages/admin/AdminDashboard';
import AdminShipments from '../pages/admin/AdminShipments';
import AdminTracking from '../pages/admin/AdminTracking';
import AdminUsers from '../pages/admin/AdminUsers';
import AdminAnalytics from '../pages/admin/AdminAnalytics';

/**
 * Application router configuration.
 */
const router = createBrowserRouter([
  // Root redirect
  { path: '/', element: <Navigate to="/dashboard" replace /> },

  // Public auth routes (centered auth layout)
  {
    element: <PublicRoute><AuthLayout /></PublicRoute>,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      { path: '/forgot-password', element: <ForgotPasswordPage /> },
      { path: '/reset-password', element: <ResetPasswordPage /> },
      { path: '/verify-email', element: <VerifyEmailPage /> },
    ],
  },

  // Customer protected routes (with app navbar/sidebar)
  {
    element: (
      <ProtectedRoute roles={[ROLES.CUSTOMER, ROLES.ADMIN]}>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/dashboard', element: <CustomerDashboard /> },
      { path: '/shipments', element: <MyShipments /> },
      { path: '/tracking', element: <TrackShipment /> },
      { path: '/tracking/:trackingNumber', element: <TrackShipment /> },
      { path: '/notifications', element: <NotificationsPage /> },
    ],
  },

  // Admin-only protected routes
  {
    element: (
      <ProtectedRoute roles={[ROLES.ADMIN]}>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/admin/dashboard', element: <AdminDashboard /> },
      { path: '/admin/shipments', element: <AdminShipments /> },
      { path: '/admin/tracking', element: <AdminTracking /> },
      { path: '/admin/users', element: <AdminUsers /> },
      { path: '/admin/analytics', element: <AdminAnalytics /> },
    ],
  },

  // Error pages
  { path: '/unauthorized', element: <UnauthorizedPage /> },
  { path: '*', element: <NotFoundPage /> },
]);

export default router;
