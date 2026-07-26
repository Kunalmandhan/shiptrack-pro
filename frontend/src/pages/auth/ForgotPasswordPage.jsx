import { useState } from 'react';
import { Link } from 'react-router-dom';
import authService from '../../services/authService';
import { HiEnvelope, HiCheckCircle, HiArrowLeft } from 'react-icons/hi2';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) {
      setErrorMessage('Please enter your email address.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      await authService.forgotPassword(email);
      setIsSuccess(true);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to send reset link. Please try again.';
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl shadow-black/50 text-center animate-fade-in">
        <div className="w-16 h-16 bg-accent-500/20 text-accent-400 rounded-full flex items-center justify-center mx-auto mb-4 border border-accent-500/30">
          <HiCheckCircle className="w-10 h-10" />
        </div>
        <h2 className="text-2xl font-bold text-white mb-2">Check Your Inbox</h2>
        <p className="text-gray-300 text-sm mb-6">
          If an account exists for <span className="text-primary-400 font-semibold">{email}</span>, you will receive a password reset link shortly.
        </p>
        <Link
          to="/login"
          className="inline-flex items-center gap-2 text-sm font-semibold text-primary-400 hover:text-primary-300 transition-colors"
        >
          <HiArrowLeft className="w-4 h-4" />
          <span>Back to Sign In</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl shadow-black/50">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-white tracking-tight">Reset Password</h2>
        <p className="text-gray-400 text-sm mt-1">Enter your email to receive a password reset link</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-danger-500/10 border border-danger-500/30 rounded-2xl text-danger-400 text-xs font-medium">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">
            Email Address
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiEnvelope className="w-5 h-5" />
            </div>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
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
            <span>Send Reset Link</span>
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
