import { renderHook, act, waitFor } from '@testing-library/react';
import { ReactNode } from 'react';
import { AuthProvider, useAuth, User } from './useAuth';
import { authApi } from '../api/authApi';
import { logger } from '../utils/logger';

jest.mock('../api/authApi');
jest.mock('../utils/logger');

const mockAuthApi = authApi as jest.Mocked<typeof authApi>;
const mockLogger = logger as jest.Mocked<typeof logger>;

const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

const mockUser: User = {
  userId: 1,
  username: 'testuser',
  role: 'admin',
  facilityId: 100,
  facilityName: 'Test Facility',
  isActive: true
};

describe('useAuth Hook - Enhanced Error Handling', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Checklist #1: Error handling in checkAuth function', () => {
    it('should handle successful session validation', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.error).toBeNull();
      expect(mockLogger.info).toHaveBeenCalledWith('Session validated successfully', {
        event: 'session_validation_success',
        userId: mockUser.userId
      });
    });

    it('should handle 401 error in checkAuth and set user to null', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { status: 401 }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockLogger.warn).toHaveBeenCalledWith('Session validation failed - user not authenticated', {
        event: 'session_validation_unauthorized',
        status: 401
      });
    });

    it('should handle 403 error in checkAuth with custom message', async () => {
      const customMessage = 'Custom permission denied message';
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: { message: customMessage }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe(customMessage);
      expect(mockLogger.error).toHaveBeenCalledWith('Permission denied during session validation', {
        event: 'session_validation_forbidden',
        status: 403,
        error: customMessage
      });
    });

    it('should handle 403 error in checkAuth with default message', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: {}
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe('You do not have permission to perform this action');
    });

    it('should handle 404 error in checkAuth with error field', async () => {
      const errorMessage = 'User not found';
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 404,
          data: { error: errorMessage }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe(errorMessage);
      expect(mockLogger.error).toHaveBeenCalledWith('Resource not found during session validation', {
        event: 'session_validation_not_found',
        status: 404,
        error: errorMessage
      });
    });

    it('should handle 404 error in checkAuth with default message', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 404,
          data: {}
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe('Resource not found');
    });

    it('should handle generic error in checkAuth', async () => {
      const genericError = new Error('Network error');
      mockAuthApi.getCurrentUser.mockRejectedValueOnce(genericError);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockLogger.warn).toHaveBeenCalledWith('Session validation failed - user not authenticated', {
        event: 'session_validation_failure',
        error: genericError.message
      });
    });
  });

  describe('Checklist #2: 401 errors trigger state updates for redirect', () => {
    it('should set user to null on 401 error to trigger redirect', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { status: 401 }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
    });

    it('should set user to null on 401 during login', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Invalid credentials');
    });
  });

  describe('Checklist #3: Authentication state propagation', () => {
    it('should propagate user state changes to consuming components', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.loading).toBe(false);
    });

    it('should propagate error state changes to consuming components', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: { message: 'Access denied' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.error).toBe('Access denied');
      });

      expect(result.current.error).toBe('Access denied');
      expect(result.current.user).toBeNull();
    });
  });

  describe('Checklist #4: Loading state management', () => {
    it('should set loading to true at start of checkAuth and false at end', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should set loading to true during login and false after completion', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockResolvedValueOnce(undefined);
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.login('testuser', 'password');
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should set loading to false even when login fails', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.loading).toBe(false);
    });

    it('should set loading to true during logout and false after completion', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.logout.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.logout();
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should set loading to false even when logout fails', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.logout.mockRejectedValueOnce(new Error('Logout failed'));

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.loading).toBe(false);
    });
  });

  describe('Checklist #5: Backward compatibility with AuthContextType', () => {
    it('should maintain all required properties in AuthContextType', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current).toHaveProperty('user');
      expect(result.current).toHaveProperty('loading');
      expect(result.current).toHaveProperty('error');
      expect(result.current).toHaveProperty('login');
      expect(result.current).toHaveProperty('logout');
      expect(result.current).toHaveProperty('clearError');
      expect(typeof result.current.login).toBe('function');
      expect(typeof result.current.logout).toBe('function');
      expect(typeof result.current.clearError).toBe('function');
    });
  });

  describe('Checklist #6: Authentication state structure compatibility', () => {
    it('should maintain user state structure', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.user).toHaveProperty('userId');
      expect(result.current.user).toHaveProperty('username');
      expect(result.current.user).toHaveProperty('role');
      expect(result.current.user).toHaveProperty('facilityId');
      expect(result.current.user).toHaveProperty('facilityName');
      expect(result.current.user).toHaveProperty('isActive');
    });

    it('should maintain error state as string or null', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: { message: 'Access denied' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.error).toBe('Access denied');
      });

      expect(typeof result.current.error).toBe('string');
    });

    it('should maintain loading state as boolean', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(typeof result.current.loading).toBe('boolean');

      await waitFor(() => {
        expect(typeof result.current.loading).toBe('boolean');
      });
    });
  });

  describe('Checklist #7: Error state handling for ErrorBanner pattern', () => {
    it('should set error message on 401 login failure', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: { message: 'Invalid username or password' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Invalid username or password');
    });

    it('should set default error message on 401 login failure without message', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: {}
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Invalid username or password');
    });

    it('should set error message on 403 login failure', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: { error: 'Account suspended' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Account suspended');
    });

    it('should set error message on 404 login failure', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 404,
          data: { message: 'Staff member not found' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Staff member not found');
    });

    it('should set generic error message on unknown login failure', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 500,
          data: { error: 'Internal server error' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Internal server error');
    });

    it('should clear error using clearError function', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Invalid credentials');

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });
  });

  describe('Login function - comprehensive error handling', () => {
    it('should successfully login and set user', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockResolvedValueOnce(undefined);
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.login('testuser', 'password');
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.error).toBeNull();
      expect(mockLogger.info).toHaveBeenCalledWith('Login successful', {
        event: 'login_success',
        username: 'testuser',
        userId: mockUser.userId
      });
    });

    it('should clear error before login attempt', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: { message: 'First error' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('First error');

      mockAuthApi.login.mockResolvedValueOnce(undefined);
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      await act(async () => {
        await result.current.login('testuser', 'correctpassword');
      });

      expect(result.current.error).toBeNull();
    });

    it('should throw error on login failure', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      const loginError = {
        response: { 
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      };
      mockAuthApi.login.mockRejectedValueOnce(loginError);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await expect(act(async () => {
        await result.current.login('testuser', 'wrongpassword');
      })).rejects.toEqual(loginError);
    });
  });

  describe('Logout function - error handling and dependency fix', () => {
    it('should successfully logout and clear user', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.logout.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockLogger.info).toHaveBeenCalledWith('Logout successful', {
        event: 'logout_success'
      });
    });

    it('should handle 403 error during logout', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.logout.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: { message: 'Forbidden' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockLogger.error).toHaveBeenCalledWith('Logout forbidden', {
        event: 'logout_forbidden',
        status: 403,
        error: 'Forbidden'
      });
    });

    it('should handle 404 error during logout', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.logout.mockRejectedValueOnce({
        response: { 
          status: 404,
          data: { error: 'Not found' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockLogger.error).toHaveBeenCalledWith('Logout resource not found', {
        event: 'logout_not_found',
        status: 404,
        error: 'Not found'
      });
    });

    it('should handle generic error during logout', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      const logoutError = new Error('Network error');
      mockAuthApi.logout.mockRejectedValueOnce(logoutError);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockLogger.error).toHaveBeenCalledWith('Logout failed', {
        event: 'logout_failure',
        error: logoutError.message
      });
    });

    it('should have empty dependency array for logout callback', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      const { result, rerender } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const logoutRef1 = result.current.logout;

      rerender();

      const logoutRef2 = result.current.logout;

      expect(logoutRef1).toBe(logoutRef2);
    });
  });

  describe('useAuth hook error handling', () => {
    it('should throw error when used outside AuthProvider', () => {
      const { result } = renderHook(() => useAuth());

      expect(result.error).toEqual(Error('useAuth must be used within an AuthProvider'));
    });
  });

  describe('clearError function', () => {
    it('should clear error state', async () => {
      mockAuthApi.getCurrentUser.mockRejectedValueOnce({
        response: { 
          status: 403,
          data: { message: 'Access denied' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.error).toBe('Access denied');
      });

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });
  });

  describe('Integration: Full authentication flow', () => {
    it('should handle complete login-logout cycle', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockResolvedValueOnce(undefined);
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.logout.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.login('testuser', 'password');
      });

      expect(result.current.user).toEqual(mockUser);

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
    });

    it('should handle failed login followed by successful login', async () => {
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockAuthApi.login.mockRejectedValueOnce({
        response: { 
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      });

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'wrongpassword');
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Invalid credentials');

      mockAuthApi.login.mockResolvedValueOnce(undefined);
      mockAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);

      await act(async () => {
        await result.current.login('testuser', 'correctpassword');
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.error).toBeNull();
    });
  });
});