import { renderHook, act, waitFor } from '@testing-library/react';
import { useStaffDeactivation } from '../hooks/useStaffDeactivation';

// Mock fetch globally
global.fetch = jest.fn();

describe('useStaffDeactivation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (global.fetch as jest.Mock).mockClear();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('deactivate function makes POST request to correct endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      status: 204,
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(global.fetch).toHaveBeenCalledWith(
      '/api/staff/123/deactivate',
      expect.objectContaining({
        method: 'POST',
      })
    );
  });

  test('sets isLoading to true during API call', async () => {
    (global.fetch as jest.Mock).mockImplementationOnce(
      () =>
        new Promise((resolve) =>
          setTimeout(
            () =>
              resolve({
                ok: true,
                status: 204,
              }),
            100
          )
        )
    );

    const { result } = renderHook(() => useStaffDeactivation());

    act(() => {
      result.current.deactivate(123);
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
  });

  test('sets isLoading to false after successful API call', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      status: 204,
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(result.current.isLoading).toBe(false);
  });

  test('sets error to FORBIDDEN when HTTP 403 with Manager role required message', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 403,
      json: async () => ({ message: 'Manager role required' }),
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(result.current.error).toBe('FORBIDDEN: Manager role required');
  });

  test('sets error to NOT_FOUND when HTTP 404', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 404,
      json: async () => ({}),
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(result.current.error).toBe('NOT_FOUND: Staff member not found');
  });

  test('sets error to FACILITY_ACCESS_DENIED when HTTP 403 with No facility access message', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 403,
      json: async () => ({ message: 'No facility access' }),
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(result.current.error).toBe('FACILITY_ACCESS_DENIED: No facility access');
  });

  test('retries up to 3 times on HTTP 5xx errors', async () => {
    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: true,
        status: 204,
      });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(global.fetch).toHaveBeenCalledTimes(3);
  });

  test('sets error after 3 failed retries on HTTP 5xx', async () => {
    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({}),
      });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(result.current.error).toBe('Server error. Please try again later.');
    expect(global.fetch).toHaveBeenCalledTimes(3);
  });

  test('does not retry on HTTP 4xx errors', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 403,
      json: async () => ({ message: 'Manager role required' }),
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(global.fetch).toHaveBeenCalledTimes(1);
  });

  test('reset function clears error and retryCount', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 404,
      json: async () => ({}),
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(result.current.error).toBe('NOT_FOUND: Staff member not found');

    act(() => {
      result.current.reset();
    });

    expect(result.current.error).toBeNull();
    expect(result.current.retryCount).toBe(0);
  });

  test('includes authentication token in request headers', async () => {
    const mockToken = 'test-auth-token-12345';
    
    // Mock localStorage or auth context
    Storage.prototype.getItem = jest.fn(() => mockToken);

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      status: 204,
    });

    const { result } = renderHook(() => useStaffDeactivation());

    await act(async () => {
      await result.current.deactivate(123);
    });

    expect(global.fetch).toHaveBeenCalledWith(
      '/api/staff/123/deactivate',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          Authorization: expect.stringContaining(mockToken),
        }),
      })
    );
  });
});