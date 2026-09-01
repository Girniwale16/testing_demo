import { renderHook, act, waitFor } from '@testing-library/react';
import { useStaffDeactivation } from './useStaffDeactivation';

describe('useStaffDeactivation', () => {
  let mockFetch: jest.SpyInstance;
  let mockLocalStorage: { [key: string]: string };

  beforeEach(() => {
    mockLocalStorage = {};
    
    global.localStorage = {
      getItem: jest.fn((key: string) => mockLocalStorage[key] || null),
      setItem: jest.fn((key: string, value: string) => {
        mockLocalStorage[key] = value;
      }),
      removeItem: jest.fn((key: string) => {
        delete mockLocalStorage[key];
      }),
      clear: jest.fn(() => {
        mockLocalStorage = {};
      }),
      length: 0,
      key: jest.fn(),
    } as Storage;

    mockFetch = jest.spyOn(global, 'fetch');
    jest.useFakeTimers();
  });

  afterEach(() => {
    mockFetch.mockRestore();
    jest.clearAllTimers();
    jest.useRealTimers();
  });

  describe('Initial State', () => {
    it('should initialize with isLoading as false', () => {
      const { result } = renderHook(() => useStaffDeactivation());
      expect(result.current.isLoading).toBe(false);
    });

    it('should initialize with error as null', () => {
      const { result } = renderHook(() => useStaffDeactivation());
      expect(result.current.error).toBe(null);
    });

    it('should provide deactivate function', () => {
      const { result } = renderHook(() => useStaffDeactivation());
      expect(typeof result.current.deactivate).toBe('function');
    });

    it('should provide reset function', () => {
      const { result } = renderHook(() => useStaffDeactivation());
      expect(typeof result.current.reset).toBe('function');
    });
  });

  describe('Successful Deactivation (HTTP 204)', () => {
    it('should set isLoading to true during request', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      act(() => {
        result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(true);

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });
    });

    it('should make POST request to correct endpoint with staffId', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(456);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/staff/456/deactivate',
        expect.objectContaining({
          method: 'POST',
        })
      );
    });

    it('should include Authorization header with Bearer token', async () => {
      mockLocalStorage['authToken'] = 'my-auth-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(789);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer my-auth-token',
          }),
        })
      );
    });

    it('should include Content-Type header', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
    });

    it('should set isLoading to false after successful response', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });

    it('should not set error on successful response', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe(null);
    });

    it('should clear previous error on new request', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 404,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('NOT_FOUND: Staff member not found');

      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe(null);
    });
  });

  describe('HTTP 403 - Manager Role Required', () => {
    it('should set error to FORBIDDEN when Manager role required', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({ message: 'Manager role required' }),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('FORBIDDEN: Manager role required');
    });

    it('should set isLoading to false after 403 Manager role error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({ message: 'Manager role required' }),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('HTTP 403 - No Facility Access', () => {
    it('should set error to FACILITY_ACCESS_DENIED when No facility access', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({ message: 'No facility access' }),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('FACILITY_ACCESS_DENIED: No facility access');
    });

    it('should set isLoading to false after 403 facility access error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({ message: 'No facility access' }),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('HTTP 403 - Generic Forbidden', () => {
    it('should set generic forbidden error for other 403 messages', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({ message: 'Some other forbidden reason' }),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('FORBIDDEN: Access denied');
    });

    it('should handle 403 with empty message', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({ message: '' }),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('FORBIDDEN: Access denied');
    });

    it('should handle 403 with no message field', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 403,
        json: async () => ({}),
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('FORBIDDEN: Access denied');
    });
  });

  describe('HTTP 404 - Not Found', () => {
    it('should set error to NOT_FOUND when staff member not found', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 404,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('NOT_FOUND: Staff member not found');
    });

    it('should set isLoading to false after 404 error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 404,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('HTTP 5xx - Server Error with Retry Logic', () => {
    it('should retry on first 500 error with 1 second backoff', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 204,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await deactivatePromise;

      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should retry on second 500 error with 2 second backoff', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 204,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await deactivatePromise;

      expect(mockFetch).toHaveBeenCalledTimes(3);
    });

    it('should retry on third 500 error with 4 second backoff', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 204,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      await deactivatePromise;

      expect(mockFetch).toHaveBeenCalledTimes(4);
    });

    it('should stop retrying after 3 failed attempts and set error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      await deactivatePromise;

      expect(mockFetch).toHaveBeenCalledTimes(4);
      expect(result.current.error).toBe('Server error. Please try again later.');
    });

    it('should handle 502 Bad Gateway with retry logic', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 502,
        } as Response)
        .mockResolvedValueOnce({
          status: 204,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await deactivatePromise;

      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should handle 503 Service Unavailable with retry logic', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 503,
        } as Response)
        .mockResolvedValueOnce({
          status: 204,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await deactivatePromise;

      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should set isLoading to false after max retries', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValue({
        status: 500,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      await deactivatePromise;

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('Network Error', () => {
    it('should set network error when fetch throws', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockRejectedValueOnce(new Error('Network failure'));

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('Network error. Please check your connection.');
    });

    it('should set isLoading to false after network error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockRejectedValueOnce(new Error('Network failure'));

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });

    it('should handle TypeError for network errors', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockRejectedValueOnce(new TypeError('Failed to fetch'));

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('Network error. Please check your connection.');
    });
  });

  describe('Unexpected Status Codes', () => {
    it('should set unexpected error for 400 status', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 400,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('An unexpected error occurred.');
    });

    it('should set unexpected error for 401 status', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 401,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('An unexpected error occurred.');
    });

    it('should set isLoading to false after unexpected error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 400,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('Reset Function', () => {
    it('should reset error to null', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 404,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('NOT_FOUND: Staff member not found');

      act(() => {
        result.current.reset();
      });

      expect(result.current.error).toBe(null);
    });

    it('should reset retryCount to 0', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch
        .mockResolvedValueOnce({
          status: 500,
        } as Response)
        .mockResolvedValueOnce({
          status: 500,
        } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      const deactivatePromise = act(async () => {
        await result.current.deactivate(123);
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await deactivatePromise;

      act(() => {
        result.current.reset();
      });

      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe(null);
    });

    it('should not affect isLoading state', async () => {
      const { result } = renderHook(() => useStaffDeactivation());

      act(() => {
        result.current.reset();
      });

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('Authentication Token Handling', () => {
    it('should use empty string when no auth token in localStorage', async () => {
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer ',
          }),
        })
      );
    });

    it('should retrieve token from localStorage on each request', async () => {
      mockLocalStorage['authToken'] = 'token-1';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer token-1',
          }),
        })
      );

      mockLocalStorage['authToken'] = 'token-2';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      await act(async () => {
        await result.current.deactivate(456);
      });

      expect(mockFetch).toHaveBeenLastCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer token-2',
          }),
        })
      );
    });
  });

  describe('Finally Block Execution', () => {
    it('should set isLoading to false in finally block on success', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });

    it('should set isLoading to false in finally block on error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 404,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });

    it('should set isLoading to false in finally block on network error', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockRejectedValueOnce(new Error('Network failure'));

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('Multiple Sequential Calls', () => {
    it('should handle multiple sequential successful calls', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValue({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe(null);

      await act(async () => {
        await result.current.deactivate(456);
      });

      expect(result.current.error).toBe(null);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should clear error from previous call on new request', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 404,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(123);
      });

      expect(result.current.error).toBe('NOT_FOUND: Staff member not found');

      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      await act(async () => {
        await result.current.deactivate(456);
      });

      expect(result.current.error).toBe(null);
    });
  });

  describe('Edge Cases', () => {
    it('should handle staffId of 0', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(0);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/staff/0/deactivate',
        expect.any(Object)
      );
    });

    it('should handle negative staffId', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(-1);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/staff/-1/deactivate',
        expect.any(Object)
      );
    });

    it('should handle very large staffId', async () => {
      mockLocalStorage['authToken'] = 'test-token';
      mockFetch.mockResolvedValueOnce({
        status: 204,
      } as Response);

      const { result } = renderHook(() => useStaffDeactivation());

      await act(async () => {
        await result.current.deactivate(999999999);
      });

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/staff/999999999/deactivate',
        expect.any(Object)
      );
    });
  });
});