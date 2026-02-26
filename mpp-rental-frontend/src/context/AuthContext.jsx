import { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';

// Create Auth Context - EXPORT IT
export const AuthContext = createContext(null);

/**
 * AuthProvider - Provides authentication state to entire app
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check if user is logged in on app load
  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      setUser(currentUser);
    }
    setLoading(false);
  }, []);

  /**
   * Login function
   */
  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials);
      if (response.success) {
        setUser(response.data);
        return { success: true };
      }
    } catch (error) {
      return { success: false, error };
    }
  };

  /**
   * Register function
   */
  const register = async (userData) => {
    try {
      const response = await authService.register(userData);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error };
    }
  };

  /**
   * Logout function
   */
  const logout = () => {
    authService.logout();
    setUser(null);
  };

  /**
   * Update user data in state
   */
  const updateUser = (userData) => {
    setUser((prev) => ({ ...prev, ...userData }));
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    updateUser,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Custom hook to use Auth Context
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};