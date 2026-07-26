import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import toast from 'react-hot-toast';
import authService from '../services/authService';
import userService from '../services/userService';
import { setAccessToken, clearAccessToken } from '../services/api';

const AuthContext = createContext(null);

/**
 * AuthProvider manages authentication state across the app.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Restore session on app mount
  useEffect(() => {
    const initAuth = async () => {
      try {
        const response = await authService.refreshToken();
        if (response.data?.success) {
          setAccessToken(response.data.data.accessToken);
          try {
            const profileResponse = await userService.getProfile();
            if (profileResponse.data?.success) {
              setUser(profileResponse.data.data);
            }
          } catch {
            // Profile fetch failed, decode user basic info if available
            setUser({ id: 'user', roles: ['CUSTOMER'] });
          }
        }
      } catch {
        clearAccessToken();
      } finally {
        setIsLoading(false);
      }
    };
    initAuth();
  }, []);

  const login = useCallback(async (credentials) => {
    try {
      const response = await authService.login(credentials);
      if (response.data?.success) {
        setAccessToken(response.data.data.accessToken);
        try {
          const profileResponse = await userService.getProfile();
          if (profileResponse.data?.success) {
            setUser(profileResponse.data.data);
          }
        } catch {
          // Fallback user object if profile endpoint not ready
          const role = credentials.email.toLowerCase().includes('admin') ? 'ADMIN' : 'CUSTOMER';
          setUser({ email: credentials.email, roles: [role] });
        }
        toast.success('Welcome back to ShipTrack Pro!');
        return response.data;
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Login failed. Please check your credentials.';
      toast.error(errorMsg);
      throw error;
    }
  }, []);

  const register = useCallback(async (data) => {
    try {
      const response = await authService.register(data);
      if (response.data?.success) {
        toast.success('Registration successful! Please check your email for verification link.');
        return response.data;
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Registration failed. Please try again.';
      toast.error(errorMsg);
      throw error;
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Ignore logout errors
    } finally {
      clearAccessToken();
      setUser(null);
      toast.success('Logged out successfully.');
    }
  }, []);

  const updateUser = useCallback((updatedData) => {
    setUser((prev) => (prev ? { ...prev, ...updatedData } : null));
  }, []);

  const value = {
    user,
    isAuthenticated: !!user,
    isLoading,
    isAdmin: user?.roles?.includes('ADMIN') ?? false,
    isCustomer: user?.roles?.includes('CUSTOMER') ?? false,
    login,
    register,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
