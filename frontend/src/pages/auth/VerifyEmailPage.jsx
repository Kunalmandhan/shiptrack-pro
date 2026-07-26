import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import authService from '../../services/authService';
import { HiCheckCircle, HiExclamationTriangle } from 'react-icons/hi2';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';

  const [status, setStatus] = useState('loading'); // 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Invalid or missing email verification token.');
      return;
    }

    const verify = async () => {
      try {
        const res = await authService.verifyEmail(token);
        setStatus('success');
        setMessage(res.data?.message || 'Email verified successfully!');
      } catch (err) {
        setStatus('error');
        setMessage(err.response?.data?.message || 'Verification link is invalid or has expired.');
      }
    };

    verify();
  }, [token]);

  return (
    <div className="bg-surface-800/90 backdrop-blur-xl rounded-3xl p-8 border border-white/10 shadow-2xl text-center">
      {status === 'loading' && (
        <div className="py-8">
          <div className="w-12 h-12 border-4 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mx-auto mb-4" />
          <h2 className="text-xl font-bold text-white mb-1">Verifying Your Email</h2>
          <p className="text-gray-400 text-xs">Please wait while we confirm your verification link...</p>
        </div>
      )}

      {status === 'success' && (
        <div className="py-4 animate-fade-in">
          <div className="w-16 h-16 bg-accent-500/20 text-accent-400 rounded-full flex items-center justify-center mx-auto mb-4 border border-accent-500/30">
            <HiCheckCircle className="w-10 h-10" />
          </div>
          <h2 className="text-2xl font-bold text-white mb-2">Email Verified!</h2>
          <p className="text-gray-300 text-sm mb-6">{message}</p>
          <Link
            to="/login"
            className="inline-block w-full py-3.5 px-4 bg-primary-600 hover:bg-primary-500 text-white font-semibold text-sm rounded-2xl shadow-lg shadow-primary-500/25 transition-all"
          >
            Sign In to Your Account
          </Link>
        </div>
      )}

      {status === 'error' && (
        <div className="py-4 animate-fade-in">
          <div className="w-16 h-16 bg-danger-500/20 text-danger-400 rounded-full flex items-center justify-center mx-auto mb-4 border border-danger-500/30">
            <HiExclamationTriangle className="w-10 h-10" />
          </div>
          <h2 className="text-2xl font-bold text-white mb-2">Verification Failed</h2>
          <p className="text-gray-300 text-sm mb-6">{message}</p>
          <Link
            to="/login"
            className="inline-block w-full py-3.5 px-4 bg-surface-700 hover:bg-surface-600 text-white font-semibold text-sm rounded-2xl transition-all"
          >
            Return to Sign In
          </Link>
        </div>
      )}
    </div>
  );
}
