import { renderHook, act, waitFor } from '@testing-library/react';
import { useAuth } from '../hooks/useAuth';
import { authApi } from '../api/authApi';
import { logger } from '../utils/logger';

jest.mock('../api/authApi');
jest.mock('../utils/logger');

const mockedAuthApi = authApi as jest.Mocked<typeof authApi>;
const mockedLogger = logger as jest.Mocked<typeof logger>;

describe('useAuth', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Session Validation on Mount', () => {
    it('should handle 403 Forbidden error during session validation with custom message', async () => {
      const customErrorMessage = 'Custom permission denied message';
      const error = {
        response: {
          status: 403,
          data: {
            message: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Permission denied during session validation',
        {
          event: 'session_validation_forbidden',
          status: 403,
          error: customErrorMessage
        }
      );
    });

    it('should handle 403 Forbidden error during session validation with error field', async () => {
      const customErrorMessage = 'Error field permission denied';
      const error = {
        response: {
          status: 403,
          data: {
            error: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Permission denied during session validation',
        {
          event: 'session_validation_forbidden',
          status: 403,
          error: customErrorMessage
        }
      );
    });

    it('should handle 403 Forbidden error during session validation with default message', async () => {
      const error = {
        response: {
          status: 403,
          data: {}
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe('You do not have permission to perform this action');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Permission denied during session validation',
        {
          event: 'session_validation_forbidden',
          status: 403,
          error: 'You do not have permission to perform this action'
        }
      );
    });

    it('should handle 404 Not Found error during session validation with custom message', async () => {
      const customErrorMessage = 'Custom not found message';
      const error = {
        response: {
          status: 404,
          data: {
            message: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Resource not found during session validation',
        {
          event: 'session_validation_not_found',
          status: 404,
          error: customErrorMessage
        }
      );
    });

    it('should handle 404 Not Found error during session validation with error field', async () => {
      const customErrorMessage = 'Error field not found';
      const error = {
        response: {
          status: 404,
          data: {
            error: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Resource not found during session validation',
        {
          event: 'session_validation_not_found',
          status: 404,
          error: customErrorMessage
        }
      );
    });

    it('should handle 404 Not Found error during session validation with default message', async () => {
      const error = {
        response: {
          status: 404,
          data: {}
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBe('Resource not found');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Resource not found during session validation',
        {
          event: 'session_validation_not_found',
          status: 404,
          error: 'Resource not found'
        }
      );
    });

    it('should not set error for non-403/404 errors during session validation', async () => {
      const error = {
        response: {
          status: 500,
          data: {
            message: 'Internal server error'
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.warn).toHaveBeenCalledWith(
        'Session validation failed - user not authenticated',
        expect.any(Object)
      );
    });
  });

  describe('Login Error Handling', () => {
    it('should handle 403 Forbidden error during login with custom message', async () => {
      const customErrorMessage = 'Custom login permission denied';
      const error = {
        response: {
          status: 403,
          data: {
            message: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login forbidden',
        {
          event: 'login_forbidden',
          username: 'testuser',
          status: 403,
          error: customErrorMessage
        }
      );
    });

    it('should handle 403 Forbidden error during login with error field', async () => {
      const customErrorMessage = 'Error field login permission denied';
      const error = {
        response: {
          status: 403,
          data: {
            error: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login forbidden',
        {
          event: 'login_forbidden',
          username: 'testuser',
          status: 403,
          error: customErrorMessage
        }
      );
    });

    it('should handle 403 Forbidden error during login with default message', async () => {
      const error = {
        response: {
          status: 403,
          data: {}
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('You do not have permission to perform this action');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login forbidden',
        {
          event: 'login_forbidden',
          username: 'testuser',
          status: 403,
          error: 'You do not have permission to perform this action'
        }
      );
    });

    it('should handle 404 Not Found error during login with custom message', async () => {
      const customErrorMessage = 'Custom staff not found message';
      const error = {
        response: {
          status: 404,
          data: {
            message: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login resource not found',
        {
          event: 'login_not_found',
          username: 'testuser',
          status: 404,
          error: customErrorMessage
        }
      );
    });

    it('should handle 404 Not Found error during login with error field', async () => {
      const customErrorMessage = 'Error field staff not found';
      const error = {
        response: {
          status: 404,
          data: {
            error: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login resource not found',
        {
          event: 'login_not_found',
          username: 'testuser',
          status: 404,
          error: customErrorMessage
        }
      );
    });

    it('should handle 404 Not Found error during login with default message', async () => {
      const error = {
        response: {
          status: 404,
          data: {}
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Staff member not found');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login resource not found',
        {
          event: 'login_not_found',
          username: 'testuser',
          status: 404,
          error: 'Staff member not found'
        }
      );
    });

    it('should handle other errors during login with custom message', async () => {
      const customErrorMessage = 'Custom server error';
      const error = {
        response: {
          status: 500,
          data: {
            message: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login failed',
        {
          event: 'login_failure',
          username: 'testuser',
          error: customErrorMessage,
          status: 500
        }
      );
    });

    it('should handle other errors during login with error field', async () => {
      const customErrorMessage = 'Error field server error';
      const error = {
        response: {
          status: 500,
          data: {
            error: customErrorMessage
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe(customErrorMessage);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login failed',
        {
          event: 'login_failure',
          username: 'testuser',
          error: customErrorMessage,
          status: 500
        }
      );
    });

    it('should handle other errors during login with default message', async () => {
      const error = {
        response: {
          status: 500,
          data: {}
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Login failed. Please try again.');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Login failed',
        {
          event: 'login_failure',
          username: 'testuser',
          error: 'Login failed. Please try again.',
          status: 500
        }
      );
    });
  });

  describe('Logout Error Handling', () => {
    it('should handle 403 Forbidden error during logout with custom message', async () => {
      const customErrorMessage = 'Custom logout permission denied';
      const error = {
        response: {
          status: 403,
          data: {
            message: customErrorMessage
          }
        }
      };

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout forbidden',
        {
          event: 'logout_forbidden',
          status: 403,
          error: customErrorMessage
        }
      );
    });

    it('should handle 403 Forbidden error during logout with error field', async () => {
      const customErrorMessage = 'Error field logout permission denied';
      const error = {
        response: {
          status: 403,
          data: {
            error: customErrorMessage
          }
        }
      };

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout forbidden',
        {
          event: 'logout_forbidden',
          status: 403,
          error: customErrorMessage
        }
      );
    });

    it('should handle 403 Forbidden error during logout with default message', async () => {
      const error = {
        response: {
          status: 403,
          data: {}
        }
      };

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout forbidden',
        {
          event: 'logout_forbidden',
          status: 403,
          error: 'You do not have permission to perform this action'
        }
      );
    });

    it('should handle 404 Not Found error during logout with custom message', async () => {
      const customErrorMessage = 'Custom logout not found message';
      const error = {
        response: {
          status: 404,
          data: {
            message: customErrorMessage
          }
        }
      };

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout resource not found',
        {
          event: 'logout_not_found',
          status: 404,
          error: customErrorMessage
        }
      );
    });

    it('should handle 404 Not Found error during logout with error field', async () => {
      const customErrorMessage = 'Error field logout not found';
      const error = {
        response: {
          status: 404,
          data: {
            error: customErrorMessage
          }
        }
      };

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout resource not found',
        {
          event: 'logout_not_found',
          status: 404,
          error: customErrorMessage
        }
      );
    });

    it('should handle 404 Not Found error during logout with default message', async () => {
      const error = {
        response: {
          status: 404,
          data: {}
        }
      };

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout resource not found',
        {
          event: 'logout_not_found',
          status: 404,
          error: 'Resource not found'
        }
      );
    });

    it('should handle other errors during logout', async () => {
      const error = new Error('Network error');

      const mockUser = {
        userId: 1,
        username: 'testuser',
        role: 'admin',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(mockUser);
      mockedAuthApi.logout.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.error).toBeNull();
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Logout failed',
        {
          event: 'logout_failure',
          error: 'Network error'
        }
      );
    });
  });

  describe('Error Message Extraction Pattern', () => {
    it('should prioritize response.data.message over response.data.error for 403 errors', async () => {
      const error = {
        response: {
          status: 403,
          data: {
            message: 'Message field value',
            error: 'Error field value'
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Message field value');
    });

    it('should prioritize response.data.message over response.data.error for 404 errors', async () => {
      const error = {
        response: {
          status: 404,
          data: {
            message: 'Message field value',
            error: 'Error field value'
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Message field value');
    });

    it('should use response.data.error when response.data.message is not available', async () => {
      const error = {
        response: {
          status: 403,
          data: {
            error: 'Error field value'
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Error field value');
    });

    it('should use default message when neither message nor error fields are available', async () => {
      const error = {
        response: {
          status: 403,
          data: {}
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('You do not have permission to perform this action');
    });
  });

  describe('clearError functionality', () => {
    it('should clear error state when clearError is called', async () => {
      const error = {
        response: {
          status: 403,
          data: {
            message: 'Permission denied'
          }
        }
      };

      mockedAuthApi.getCurrentUser.mockResolvedValueOnce(null as any);
      mockedAuthApi.login.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        try {
          await result.current.login('testuser', 'password');
        } catch (e) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBe('Permission denied');

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });
  });
});