import { useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import authService from '../../services/authService';
import toast from 'react-hot-toast';
import { HiLockClosed, HiShieldCheck, HiEye, HiEyeSlash, HiCheckCircle, HiArrowLeft } from 'react-icons/hi2';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      setErrorMessage('Invalid or missing password reset token.');
      return;
    }

    if (newPassword.length < 8) {
      setErrorMessage('Password must be at least 8 characters.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      await authService.resetPassword({ token, newPassword });
      setIsSuccess(true);
      toast.success('Password reset successfully!');
    } catch (err) {
      const msg = err.response?.data?.message || 'Password reset failed. The link may have expired.';
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl text-center">
        <div className="w-16 h-16 bg-accent-500/20 text-accent-400 rounded-full flex items-center justify-center mx-auto mb-4 border border-accent-500/30">
          <HiCheckCircle className="w-10 h-10" />
        </div>
        <h2 className="text-2xl font-bold text-white mb-2">Password Changed!</h2>
        <p className="text-gray-300 text-sm mb-6">
          Your password has been reset successfully. You can now sign in with your new password.
        </p>
        <button
          onClick={() => navigate('/login')}
          className="w-full py-3 px-4 bg-primary-600 hover:bg-primary-500 text-white font-semibold text-sm rounded-2xl transition-all"
        >
          Proceed to Sign In
        </button>
      </div>
    );
  }

  return (
    <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-white tracking-tight">Set New Password</h2>
        <p className="text-gray-400 text-sm mt-1">Please enter your new password below</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-danger-500/10 border border-danger-500/30 rounded-2xl text-danger-400 text-xs font-medium">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* New Password */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">
            New Password
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiLockClosed className="w-5 h-5" />
            </div>
            <input
              type={showPassword ? 'text' : 'password'}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="At least 8 characters"
              required
              className="w-full pl-11 pr-11 py-3 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-400 hover:text-gray-200 transition-colors"
            >
              {showPassword ? <HiEyeSlash className="w-5 h-5" /> : <HiEye className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Confirm New Password */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">
            Confirm New Password
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiShieldCheck className="w-5 h-5" />
            </div>
            <input
              type={showPassword ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter new password"
              required
              className="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3.5 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white font-semibold text-sm rounded-2xl shadow-lg shadow-primary-500/25 flex items-center justify-center gap-2 transition-all duration-200 disabled:opacity-50 hover:scale-[1.01]"
        >
          {isSubmitting ? (
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <span>Update Password</span>
          )}
        </button>
      </form>

      <div className="mt-8 text-center">
        <Link
          to="/login"
          className="inline-flex items-center gap-2 text-xs text-gray-400 hover:text-gray-200 transition-colors"
        >
          <HiArrowLeft className="w-4 h-4" />
          <span>Back to Sign In</span>
        </Link>
      </div>
    </div>
  );
}
