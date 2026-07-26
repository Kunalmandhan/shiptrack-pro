import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { HiEnvelope, HiLockClosed, HiEye, HiEyeSlash, HiArrowRight } from 'react-icons/hi2';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({
    email: localStorage.getItem('shiptrack_remember_email') || '',
    password: '',
  });

  const [rememberMe, setRememberMe] = useState(
    !!localStorage.getItem('shiptrack_remember_email')
  );
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const from = location.state?.from?.pathname || null;

  const handleChange = (e) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
    if (errorMessage) setErrorMessage('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.email || !formData.password) {
      setErrorMessage('Please enter both email and password.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      if (rememberMe) {
        localStorage.setItem('shiptrack_remember_email', formData.email);
      } else {
        localStorage.removeItem('shiptrack_remember_email');
      }

      await login(formData);

      // Determine redirect path
      const role = formData.email.toLowerCase().includes('admin') ? 'ADMIN' : 'CUSTOMER';
      const redirectTarget = from || (role === 'ADMIN' ? '/admin/dashboard' : '/dashboard');
      navigate(redirectTarget, { replace: true });
    } catch (err) {
      const backendMessage = err.response?.data?.message || 'Invalid email or password. Please try again.';
      setErrorMessage(backendMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl shadow-black/50 transition-all duration-300">
      <div className="text-center mb-8">
        <h2 className="text-2xl font-bold text-white tracking-tight">Welcome Back</h2>
        <p className="text-gray-400 text-sm mt-1">Sign in to your ShipTrack Pro account</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-danger-500/10 border border-danger-500/30 rounded-2xl text-danger-400 text-xs font-medium animate-fade-in">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Email Address */}
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
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="you@example.com"
              required
              className="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
          </div>
        </div>

        {/* Password */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider">
              Password
            </label>
            <Link
              to="/forgot-password"
              className="text-xs text-primary-400 hover:text-primary-300 font-medium transition-colors"
            >
              Forgot Password?
            </Link>
          </div>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiLockClosed className="w-5 h-5" />
            </div>
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="••••••••"
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

        {/* Remember Me */}
        <div className="flex items-center">
          <input
            type="checkbox"
            id="rememberMe"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
            className="w-4 h-4 rounded border-gray-600 bg-surface-900 text-primary-600 focus:ring-primary-500 focus:ring-offset-surface-800"
          />
          <label htmlFor="rememberMe" className="ml-2.5 text-xs text-gray-300 select-none cursor-pointer">
            Remember email on this device
          </label>
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3.5 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white font-semibold text-sm rounded-2xl shadow-lg shadow-primary-500/25 flex items-center justify-center gap-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed hover:scale-[1.01]"
        >
          {isSubmitting ? (
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <>
              <span>Sign In</span>
              <HiArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </form>

      {/* Footer Link */}
      <div className="mt-8 text-center text-xs text-gray-400">
        Don't have an account?{' '}
        <Link to="/register" className="text-primary-400 font-semibold hover:text-primary-300 transition-colors ml-1">
          Create Account
        </Link>
      </div>
    </div>
  );
}
