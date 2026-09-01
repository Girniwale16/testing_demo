import { useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { UserProfile } from '../types/auth.types';
import { logger } from '../utils/logger';

export function useAuth() {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const validateSession = async () => {
      try {
        logger.info('Validating session on mount', {
          event: 'session_validation_start'
        });

        const currentUser = await authApi.getCurrentUser();
        setUser(currentUser);

        logger.info('Session validation successful', {
          event: 'session_validation_success',
          username: currentUser.username,
          role: currentUser.role
        });
      } catch (err) {
        logger.warn('Session validation failed - user not authenticated', {
          event: 'session_validation_failed',
          error: err instanceof Error ? err.message : 'Unknown error'
        });
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    validateSession();
  }, []);

  const login = async (username: string, password: string) => {
    try {
      setError(null);
      setLoading(true);

      logger.info('Login attempt started', {
        event: 'login_attempt',
        username
      });
      const response = await authApi.login({ username, password, facilityId: 1 });
      const userProfile: UserProfile = {
        userId: response.userId,
        username: response.username,
        role: response.role,
        facilityId: response.facilityId,
        facilityName: response.facilityName,
        isActive: true
      };
      setUser(userProfile);

      logger.info('Login successful', {
        event: 'login_success',
        username: userProfile.username,
        role: userProfile.role
      });
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Login failed. Please try again.';
      setError(errorMessage);

      logger.error('Login failed', {
        event: 'login_failure',
        username,
        error: errorMessage,
        status: err.response?.status
      });

      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      logger.info('Logout attempt started', {
        event: 'logout_attempt',
        username: user?.username
      });

      await authApi.logout();
      setUser(null);
      setError(null);

      logger.info('Logout successful', {
        event: 'logout_success'
      });
    } catch (err) {
      logger.error('Logout failed', {
        event: 'logout_failure',
        error: err instanceof Error ? err.message : 'Unknown error'
      });
      
      setUser(null);
      setError(null);
    }
  };

  const clearError = () => {
    setError(null);
  };

  return {
    user,
    loading,
    error,
    login,
    logout,
    clearError
  };
}