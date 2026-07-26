import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { HiUser, HiEnvelope, HiLockClosed, HiEye, HiEyeSlash, HiShieldCheck, HiArrowRight } from 'react-icons/hi2';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'CUSTOMER',
  });

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleChange = (e) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
    if (errorMessage) setErrorMessage('');
  };

  // Real-time password strength score (0 to 4)
  const getPasswordStrength = (pwd) => {
    let score = 0;
    if (pwd.length >= 8) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^A-Za-z0-9]/.test(pwd)) score++;
    return score;
  };

  const strength = getPasswordStrength(formData.password);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.name || !formData.email || !formData.password) {
      setErrorMessage('Please fill in all required fields.');
      return;
    }

    if (formData.password.length < 8) {
      setErrorMessage('Password must be at least 8 characters long.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      await register({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        role: formData.role,
      });

      // Redirect to login upon successful registration
      navigate('/login');
    } catch (err) {
      const backendMsg = err.response?.data?.message || 'Registration failed. Email may already be in use.';
      setErrorMessage(backendMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl shadow-black/50 transition-all duration-300">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-white tracking-tight">Create an Account</h2>
        <p className="text-gray-400 text-sm mt-1">Get real-time shipment visibility</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-danger-500/10 border border-danger-500/30 rounded-2xl text-danger-400 text-xs font-medium">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Role Selector Tabs */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">
            Account Type
          </label>
          <div className="grid grid-cols-2 gap-2 p-1 bg-surface-900/80 rounded-2xl border border-white/10">
            <button
              type="button"
              onClick={() => setFormData((prev) => ({ ...prev, role: 'CUSTOMER' }))}
              className={`py-2 px-3 text-xs font-semibold rounded-xl transition-all ${
                formData.role === 'CUSTOMER'
                  ? 'bg-primary-600 text-white shadow-md'
                  : 'text-gray-400 hover:text-gray-200'
              }`}
            >
              Customer
            </button>
            <button
              type="button"
              onClick={() => setFormData((prev) => ({ ...prev, role: 'ADMIN' }))}
              className={`py-2 px-3 text-xs font-semibold rounded-xl transition-all ${
                formData.role === 'ADMIN'
                  ? 'bg-primary-600 text-white shadow-md'
                  : 'text-gray-400 hover:text-gray-200'
              }`}
            >
              Admin
            </button>
          </div>
        </div>

        {/* Full Name */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
            Full Name
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiUser className="w-5 h-5" />
            </div>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="John Doe"
              required
              className="w-full pl-11 pr-4 py-2.5 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
          </div>
        </div>

        {/* Email Address */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
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
              placeholder="john@example.com"
              required
              className="w-full pl-11 pr-4 py-2.5 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
          </div>
        </div>

        {/* Password */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
            Password
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiLockClosed className="w-5 h-5" />
            </div>
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="At least 8 characters"
              required
              className="w-full pl-11 pr-11 py-2.5 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-400 hover:text-gray-200 transition-colors"
            >
              {showPassword ? <HiEyeSlash className="w-5 h-5" /> : <HiEye className="w-5 h-5" />}
            </button>
          </div>

          {/* Password Strength Meter */}
          {formData.password && (
            <div className="mt-2 flex items-center gap-1.5">
              {[1, 2, 3, 4].map((step) => (
                <div
                  key={step}
                  className={`h-1 flex-1 rounded-full transition-all duration-300 ${
                    strength >= step
                      ? strength === 4
                        ? 'bg-accent-500'
                        : strength >= 2
                        ? 'bg-warning-500'
                        : 'bg-danger-500'
                      : 'bg-white/10'
                  }`}
                />
              ))}
            </div>
          )}
        </div>

        {/* Confirm Password */}
        <div>
          <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
            Confirm Password
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <HiShieldCheck className="w-5 h-5" />
            </div>
            <input
              type={showPassword ? 'text' : 'password'}
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Re-enter password"
              required
              className="w-full pl-11 pr-4 py-2.5 bg-surface-900/80 border border-white/10 rounded-2xl text-white text-sm placeholder-gray-500 focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 transition-all"
            />
          </div>
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full mt-2 py-3.5 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 text-white font-semibold text-sm rounded-2xl shadow-lg shadow-primary-500/25 flex items-center justify-center gap-2 transition-all duration-200 disabled:opacity-50 hover:scale-[1.01]"
        >
          {isSubmitting ? (
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <>
              <span>Create Account</span>
              <HiArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </form>

      {/* Footer Link */}
      <div className="mt-6 text-center text-xs text-gray-400">
        Already have an account?{' '}
        <Link to="/login" className="text-primary-400 font-semibold hover:text-primary-300 transition-colors ml-1">
          Sign In
        </Link>
      </div>
    </div>
  );
}
