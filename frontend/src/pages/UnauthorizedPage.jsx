import { Link } from 'react-router-dom';
import { HiOutlineShieldExclamation } from 'react-icons/hi';

export default function UnauthorizedPage() {
  return (
    <div className="min-h-screen bg-surface-900 flex items-center justify-center p-4">
      <div className="text-center">
        <HiOutlineShieldExclamation className="w-16 h-16 text-danger-500 mx-auto mb-4" />
        <h1 className="text-4xl font-bold text-white mb-2">Access Denied</h1>
        <p className="text-gray-400 mb-6">You don't have permission to view this page.</p>
        <Link
          to="/"
          className="inline-flex items-center px-6 py-3 rounded-lg bg-primary-600 hover:bg-primary-700 text-white font-medium transition-colors"
        >
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
}
