import { renderHook, act, waitFor } from '@testing-library/react';
import { useNavigate } from 'react-router-dom';
import useStaffApi from '../useStaffApi';
import staffApi from '../../api/staffApi';

jest.mock('react-router-dom', () => ({
  useNavigate: jest.fn()
}));

jest.mock('../../api/staffApi');

describe('useStaffApi', () => {
  let mockNavigate;

  beforeEach(() => {
    mockNavigate = jest.fn();
    useNavigate.mockReturnValue(mockNavigate);
    jest.clearAllMocks();
  });

  describe('Initial State', () => {
    test('should initialize with null error state', () => {
      const { result } = renderHook(() => useStaffApi());

      expect(result.current.error).toBeNull();
    });

    test('should return callApi, error, and clearError functions', () => {
      const { result } = renderHook(() => useStaffApi());

      expect(result.current).toHaveProperty('callApi');
      expect(result.current).toHaveProperty('error');
      expect(result.current).toHaveProperty('clearError');
      expect(typeof result.current.callApi).toBe('function');
      expect(typeof result.current.clearError).toBe('function');
    });
  });

  describe('callApi - Successful API Calls', () => {
    test('should successfully execute API function and return result', async () => {
      const { result } = renderHook(() => useStaffApi());
      const mockData = { data: [{ id: 1, name: 'John Doe' }] };
      const mockApiFunction = jest.fn().mockResolvedValue(mockData);

      let apiResult;
      await act(async () => {
        apiResult = await result.current.callApi(mockApiFunction);
      });

      expect(mockApiFunction).toHaveBeenCalledTimes(1);
      expect(apiResult).toEqual(mockData);
      expect(result.current.error).toBeNull();
    });

    test('should not set error state on successful API call', async () => {
      const { result } = renderHook(() => useStaffApi());
      const mockApiFunction = jest.fn().mockResolvedValue({ success: true });

      await act(async () => {
        await result.current.callApi(mockApiFunction);
      });

      expect(result.current.error).toBeNull();
    });
  });

  describe('handleError - 401 Unauthorized', () => {
    test('should navigate to /login on 401 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error401 = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error401);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(mockNavigate).toHaveBeenCalledWith('/login');
      expect(mockNavigate).toHaveBeenCalledTimes(1);
    });

    test('should not set error state on 401 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error401 = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error401);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toBeNull();
    });

    test('should re-throw error after handling 401', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error401 = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error401);

      await expect(
        act(async () => {
          await result.current.callApi(mockApiFunction);
        })
      ).rejects.toEqual(error401);
    });
  });

  describe('handleError - 403 Forbidden', () => {
    test('should set FORBIDDEN error state on 403 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error403 = {
        response: {
          status: 403,
          data: {
            message: 'Forbidden',
            details: 'User does not have manager role'
          }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error403);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'FORBIDDEN',
        message: 'Access denied - Manager role required',
        details: 'User does not have manager role',
        canRetry: false
      });
    });

    test('should not navigate on 403 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error403 = {
        response: {
          status: 403,
          data: { details: 'Forbidden access' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error403);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('should re-throw error after handling 403', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error403 = {
        response: {
          status: 403,
          data: { details: 'Forbidden' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error403);

      await expect(
        act(async () => {
          await result.current.callApi(mockApiFunction);
        })
      ).rejects.toEqual(error403);
    });
  });

  describe('handleError - 500 Server Error', () => {
    test('should set SERVER_ERROR state on 500 error with custom message', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error500 = {
        response: {
          status: 500,
          data: {
            message: 'Database connection failed',
            details: 'Connection timeout after 30s'
          }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error500);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'SERVER_ERROR',
        message: 'Database connection failed',
        details: 'Connection timeout after 30s',
        canRetry: true
      });
    });

    test('should set SERVER_ERROR state on 500 error with default message', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error500 = {
        response: {
          status: 500,
          data: {
            details: 'Internal server error'
          }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error500);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'SERVER_ERROR',
        message: 'Server error occurred',
        details: 'Internal server error',
        canRetry: true
      });
    });

    test('should set SERVER_ERROR state on 502 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error502 = {
        response: {
          status: 502,
          data: {
            message: 'Bad Gateway',
            details: 'Upstream server error'
          }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error502);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'SERVER_ERROR',
        message: 'Bad Gateway',
        details: 'Upstream server error',
        canRetry: true
      });
    });

    test('should set SERVER_ERROR state on 503 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error503 = {
        response: {
          status: 503,
          data: {
            message: 'Service Unavailable',
            details: 'Server is temporarily unavailable'
          }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error503);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'SERVER_ERROR',
        message: 'Service Unavailable',
        details: 'Server is temporarily unavailable',
        canRetry: true
      });
    });

    test('should not navigate on server error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error500 = {
        response: {
          status: 500,
          data: { message: 'Server error', details: 'Error details' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error500);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  describe('handleError - Other HTTP Errors', () => {
    test('should set UNKNOWN error state on 400 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error400 = {
        response: {
          status: 400,
          data: { message: 'Bad Request' }
        },
        message: 'Request failed with status code 400'
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error400);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: 'Request failed with status code 400',
        canRetry: false
      });
    });

    test('should set UNKNOWN error state on 404 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error404 = {
        response: {
          status: 404,
          data: { message: 'Not Found' }
        },
        message: 'Request failed with status code 404'
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error404);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: 'Request failed with status code 404',
        canRetry: false
      });
    });

    test('should set UNKNOWN error state on 422 error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error422 = {
        response: {
          status: 422,
          data: { message: 'Unprocessable Entity' }
        },
        message: 'Validation failed'
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error422);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: 'Validation failed',
        canRetry: false
      });
    });
  });

  describe('handleError - Network Errors', () => {
    test('should set UNKNOWN error state on network error without response', async () => {
      const { result } = renderHook(() => useStaffApi());
      const networkError = {
        message: 'Network Error',
        request: {}
      };
      const mockApiFunction = jest.fn().mockRejectedValue(networkError);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: 'Network Error',
        canRetry: false
      });
    });

    test('should set UNKNOWN error state on timeout error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const timeoutError = {
        message: 'timeout of 5000ms exceeded',
        code: 'ECONNABORTED'
      };
      const mockApiFunction = jest.fn().mockRejectedValue(timeoutError);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: 'timeout of 5000ms exceeded',
        canRetry: false
      });
    });

    test('should set UNKNOWN error state on generic error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const genericError = new Error('Something went wrong');
      const mockApiFunction = jest.fn().mockRejectedValue(genericError);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: 'Something went wrong',
        canRetry: false
      });
    });
  });

  describe('clearError', () => {
    test('should clear error state when clearError is called', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error403 = {
        response: {
          status: 403,
          data: { details: 'Forbidden' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error403);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).not.toBeNull();

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });

    test('should handle clearError when error is already null', () => {
      const { result } = renderHook(() => useStaffApi());

      expect(result.current.error).toBeNull();

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });

    test('should clear error state multiple times', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error500 = {
        response: {
          status: 500,
          data: { message: 'Server error', details: 'Error' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error500);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).not.toBeNull();

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).not.toBeNull();

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });
  });

  describe('Error Re-throwing', () => {
    test('should re-throw error after handling for all error types', async () => {
      const { result } = renderHook(() => useStaffApi());
      const testError = {
        response: {
          status: 500,
          data: { message: 'Test error', details: 'Details' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(testError);

      await expect(
        act(async () => {
          await result.current.callApi(mockApiFunction);
        })
      ).rejects.toEqual(testError);
    });

    test('should allow caller to catch re-thrown error', async () => {
      const { result } = renderHook(() => useStaffApi());
      const testError = {
        response: {
          status: 403,
          data: { details: 'Forbidden' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(testError);

      let caughtError;
      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          caughtError = err;
        }
      });

      expect(caughtError).toEqual(testError);
    });
  });

  describe('Integration Tests', () => {
    test('should handle multiple sequential API calls with different outcomes', async () => {
      const { result } = renderHook(() => useStaffApi());
      
      const successFunction = jest.fn().mockResolvedValue({ data: 'success' });
      const errorFunction = jest.fn().mockRejectedValue({
        response: {
          status: 500,
          data: { message: 'Error', details: 'Server error' }
        }
      });

      await act(async () => {
        await result.current.callApi(successFunction);
      });

      expect(result.current.error).toBeNull();

      await act(async () => {
        try {
          await result.current.callApi(errorFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).not.toBeNull();
      expect(result.current.error.type).toBe('SERVER_ERROR');
    });

    test('should maintain error state across multiple renders', async () => {
      const { result, rerender } = renderHook(() => useStaffApi());
      const error403 = {
        response: {
          status: 403,
          data: { details: 'Access denied' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error403);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      const errorBeforeRerender = result.current.error;

      rerender();

      expect(result.current.error).toEqual(errorBeforeRerender);
    });
  });

  describe('Edge Cases', () => {
    test('should handle error with missing response data', async () => {
      const { result } = renderHook(() => useStaffApi());
      const errorWithoutData = {
        response: {
          status: 500
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(errorWithoutData);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'SERVER_ERROR',
        message: 'Server error occurred',
        details: undefined,
        canRetry: true
      });
    });

    test('should handle error with undefined message', async () => {
      const { result } = renderHook(() => useStaffApi());
      const errorWithUndefinedMessage = {
        message: undefined
      };
      const mockApiFunction = jest.fn().mockRejectedValue(errorWithUndefinedMessage);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred',
        details: undefined,
        canRetry: false
      });
    });

    test('should handle API function that returns undefined', async () => {
      const { result } = renderHook(() => useStaffApi());
      const mockApiFunction = jest.fn().mockResolvedValue(undefined);

      let apiResult;
      await act(async () => {
        apiResult = await result.current.callApi(mockApiFunction);
      });

      expect(apiResult).toBeUndefined();
      expect(result.current.error).toBeNull();
    });

    test('should handle API function that returns null', async () => {
      const { result } = renderHook(() => useStaffApi());
      const mockApiFunction = jest.fn().mockResolvedValue(null);

      let apiResult;
      await act(async () => {
        apiResult = await result.current.callApi(mockApiFunction);
      });

      expect(apiResult).toBeNull();
      expect(result.current.error).toBeNull();
    });
  });

  describe('Error State Structure Validation', () => {
    test('should ensure FORBIDDEN error has correct structure', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error403 = {
        response: {
          status: 403,
          data: { details: 'Manager role required' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error403);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toHaveProperty('type', 'FORBIDDEN');
      expect(result.current.error).toHaveProperty('message', 'Access denied - Manager role required');
      expect(result.current.error).toHaveProperty('details');
      expect(result.current.error).toHaveProperty('canRetry', false);
    });

    test('should ensure SERVER_ERROR has correct structure', async () => {
      const { result } = renderHook(() => useStaffApi());
      const error500 = {
        response: {
          status: 500,
          data: { message: 'Internal error', details: 'Stack trace' }
        }
      };
      const mockApiFunction = jest.fn().mockRejectedValue(error500);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toHaveProperty('type', 'SERVER_ERROR');
      expect(result.current.error).toHaveProperty('message');
      expect(result.current.error).toHaveProperty('details');
      expect(result.current.error).toHaveProperty('canRetry', true);
    });

    test('should ensure UNKNOWN error has correct structure', async () => {
      const { result } = renderHook(() => useStaffApi());
      const unknownError = new Error('Unknown error');
      const mockApiFunction = jest.fn().mockRejectedValue(unknownError);

      await act(async () => {
        try {
          await result.current.callApi(mockApiFunction);
        } catch (err) {
          // Expected to throw
        }
      });

      expect(result.current.error).toHaveProperty('type', 'UNKNOWN');
      expect(result.current.error).toHaveProperty('message', 'An unexpected error occurred');
      expect(result.current.error).toHaveProperty('details');
      expect(result.current.error).toHaveProperty('canRetry', false);
    });
  });
});