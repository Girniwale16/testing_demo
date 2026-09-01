import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios, { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import axiosInstance from '../api/axiosInstance';
import { logger } from '../utils/logger';
import { v4 as uuidv4 } from 'uuid';

vi.mock('uuid');
vi.mock('../utils/logger');

describe('axiosInstance', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  describe('generateCorrelationId', () => {
    it('should generate unique UUID v4 correlation IDs', () => {
      const mockUuid1 = '123e4567-e89b-12d3-a456-426614174000';
      const mockUuid2 = '987e6543-e21b-98d7-a654-426614174999';
      
      vi.mocked(uuidv4).mockReturnValueOnce(mockUuid1).mockReturnValueOnce(mockUuid2);

      const id1 = uuidv4();
      const id2 = uuidv4();

      expect(id1).toBe(mockUuid1);
      expect(id2).toBe(mockUuid2);
      expect(id1).not.toBe(id2);
      expect(uuidv4).toHaveBeenCalledTimes(2);
    });
  });

  describe('Request Interceptor - Correlation ID', () => {
    it('should attach X-Correlation-ID header to outgoing requests when not present', async () => {
      const mockCorrelationId = 'test-correlation-id-123';
      vi.mocked(uuidv4).mockReturnValue(mockCorrelationId);

      const requestInterceptor = (axiosInstance.interceptors.request as any).handlers[0];
      
      const config: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/test-endpoint'
      } as InternalAxiosRequestConfig;

      const result = await requestInterceptor.fulfilled(config);

      expect(result.headers['X-Correlation-ID']).toBe(mockCorrelationId);
      expect(logger.info).toHaveBeenCalledWith('Generated correlation ID for request', {
        event: 'correlation_id_generated',
        correlation_id: mockCorrelationId,
        path: '/test-endpoint'
      });
    });

    it('should not overwrite existing X-Correlation-ID header', async () => {
      const existingCorrelationId = 'existing-correlation-id';
      
      const requestInterceptor = (axiosInstance.interceptors.request as any).handlers[0];
      
      const config: InternalAxiosRequestConfig = {
        headers: {
          'X-Correlation-ID': existingCorrelationId
        } as any,
        url: '/test-endpoint'
      } as InternalAxiosRequestConfig;

      const result = await requestInterceptor.fulfilled(config);

      expect(result.headers['X-Correlation-ID']).toBe(existingCorrelationId);
      expect(uuidv4).not.toHaveBeenCalled();
      expect(logger.info).not.toHaveBeenCalled();
    });

    it('should handle request interceptor errors', async () => {
      const requestInterceptor = (axiosInstance.interceptors.request as any).handlers[0];
      const error = new Error('Request interceptor error');

      await expect(requestInterceptor.rejected(error)).rejects.toThrow(error);
      
      expect(logger.error).toHaveBeenCalledWith('Request interceptor error', {
        event: 'request_interceptor_error',
        error: 'Request interceptor error'
      });
    });
  });

  describe('Response Error Interceptor - Correlation ID Extraction', () => {
    it('should extract correlation ID from error responses and include in logged error objects', async () => {
      const correlationId = 'error-correlation-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test'
        } as InternalAxiosRequestConfig,
        response: {
          status: 500,
          data: { message: 'Internal server error' }
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Request failed',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.error).toHaveBeenCalledWith('HTTP error intercepted', {
        event: 'interceptor_error',
        correlation_id: correlationId,
        path: '/api/test',
        status: 500,
        message: 'Internal server error'
      });
    });
  });

  describe('Retry Logic - Network Failures', () => {
    it('should retry GET requests on network failure with exponential backoff', async () => {
      const correlationId = 'retry-correlation-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const mockAxiosInstance = vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        response: undefined,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', {
        event: 'request_retry',
        correlation_id: correlationId,
        path: '/api/test',
        retry_count: 1,
        delay_ms: 1000
      });

      expect(mockAxiosInstance).toHaveBeenCalled();
    });

    it('should retry with exponential backoff: 1s, 2s, 4s delays', async () => {
      const correlationId = 'backoff-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      let attemptCount = 0;
      vi.spyOn(axiosInstance, 'request').mockImplementation(() => {
        attemptCount++;
        if (attemptCount < 4) {
          return Promise.reject({
            config: {
              headers: { 'X-Correlation-ID': correlationId },
              url: '/api/test',
              method: 'GET',
              retryCount: attemptCount - 1
            },
            code: 'ERR_NETWORK',
            message: 'Network Error'
          });
        }
        return Promise.resolve({ data: 'success' } as AxiosResponse);
      });

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      await vi.advanceTimersByTimeAsync(2000);
      await vi.advanceTimersByTimeAsync(4000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({ delay_ms: 1000 }));
      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({ delay_ms: 2000 }));
      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({ delay_ms: 4000 }));
    });

    it('should stop retrying after max 3 retries', async () => {
      const correlationId = 'max-retry-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockRejectedValue({
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        },
        code: 'ERR_NETWORK',
        message: 'Network Error'
      });

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      await vi.advanceTimersByTimeAsync(2000);
      await vi.advanceTimersByTimeAsync(4000);
      
      await expect(retryPromise).rejects.toThrow();

      expect(logger.error).toHaveBeenCalledWith('Max retries reached', {
        event: 'max_retries_reached',
        correlation_id: correlationId,
        path: '/api/test',
        retry_count: 3
      });
    });

    it('should retry on status code 0', async () => {
      const correlationId = 'status-0-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        response: {
          status: 0
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry',
        correlation_id: correlationId
      }));
    });

    it('should retry on ECONNABORTED error code', async () => {
      const correlationId = 'econnaborted-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ECONNABORTED',
        isAxiosError: true,
        message: 'timeout of 30000ms exceeded',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry',
        correlation_id: correlationId
      }));
    });
  });

  describe('Idempotency Check', () => {
    it('should retry GET requests without idempotency key', async () => {
      const correlationId = 'get-no-key-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry'
      }));
    });

    it('should retry POST requests with X-Idempotency-Key header', async () => {
      const correlationId = 'post-with-key-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 
            'X-Correlation-ID': correlationId,
            'X-Idempotency-Key': 'idempotency-key-123'
          },
          url: '/api/test',
          method: 'POST'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry'
      }));
    });

    it('should NOT retry POST requests without X-Idempotency-Key header', async () => {
      const correlationId = 'post-no-key-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'POST'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.info).not.toHaveBeenCalledWith('Retrying request', expect.anything());
    });

    it('should retry PUT requests with X-Idempotency-Key header', async () => {
      const correlationId = 'put-with-key-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 
            'X-Correlation-ID': correlationId,
            'X-Idempotency-Key': 'idempotency-key-456'
          },
          url: '/api/test',
          method: 'PUT'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry'
      }));
    });

    it('should retry PATCH requests with X-Idempotency-Key header', async () => {
      const correlationId = 'patch-with-key-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 
            'X-Correlation-ID': correlationId,
            'X-Idempotency-Key': 'idempotency-key-789'
          },
          url: '/api/test',
          method: 'PATCH'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry'
      }));
    });

    it('should NOT retry DELETE requests even with X-Idempotency-Key header', async () => {
      const correlationId = 'delete-with-key-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 
            'X-Correlation-ID': correlationId,
            'X-Idempotency-Key': 'idempotency-key-delete'
          },
          url: '/api/test',
          method: 'DELETE'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.info).not.toHaveBeenCalledWith('Retrying request', expect.anything());
    });
  });

  describe('isRetry Flag - Prevent Infinite Loops', () => {
    it('should set isRetry flag to true when retrying', async () => {
      const correlationId = 'retry-flag-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      let capturedConfig: any;
      vi.spyOn(axiosInstance, 'request').mockImplementation((config) => {
        capturedConfig = config;
        return Promise.resolve({ data: 'success' } as AxiosResponse);
      });

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(capturedConfig.isRetry).toBe(true);
      expect(capturedConfig.retryCount).toBe(1);
    });

    it('should NOT retry if isRetry flag is already true', async () => {
      const correlationId = 'no-infinite-loop-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET',
          isRetry: true
        } as InternalAxiosRequestConfig & { isRetry: boolean },
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.info).not.toHaveBeenCalledWith('Retrying request', expect.anything());
    });
  });

  describe('Existing Interceptor Behavior - 401 Authentication', () => {
    it('should redirect to /login on 401 error', async () => {
      const correlationId = '401-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const originalLocation = window.location.href;
      delete (window as any).location;
      window.location = { href: '/dashboard', pathname: '/dashboard' } as any;

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test'
        } as InternalAxiosRequestConfig,
        response: {
          status: 401
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Unauthorized',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.warn).toHaveBeenCalledWith('401 Unauthorized - redirecting to login', {
        event: 'interceptor_401',
        correlation_id: correlationId,
        path: '/api/test',
        status: 401
      });

      expect(window.location.href).toBe('/login');
    });

    it('should NOT redirect if already on /login page', async () => {
      const correlationId = '401-already-login-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      delete (window as any).location;
      window.location = { href: '/login', pathname: '/login' } as any;

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test'
        } as InternalAxiosRequestConfig,
        response: {
          status: 401
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Unauthorized',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(window.location.href).toBe('/login');
    });

    it('should NOT retry on 401 error', async () => {
      const correlationId = '401-no-retry-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      delete (window as any).location;
      window.location = { href: '/dashboard', pathname: '/dashboard' } as any;

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        response: {
          status: 401
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Unauthorized',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.info).not.toHaveBeenCalledWith('Retrying request', expect.anything());
    });
  });

  describe('Existing Interceptor Behavior - Error Logging', () => {
    it('should log 403 errors with correlation ID', async () => {
      const correlationId = '403-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test'
        } as InternalAxiosRequestConfig,
        response: {
          status: 403,
          data: { message: 'Access denied' }
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Forbidden',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.error).toHaveBeenCalledWith('403 Forbidden - authorization error', {
        event: 'interceptor_403',
        correlation_id: correlationId,
        path: '/api/test',
        status: 403,
        message: 'Access denied'
      });
    });

    it('should log 500 errors with correlation ID', async () => {
      const correlationId = '500-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test'
        } as InternalAxiosRequestConfig,
        response: {
          status: 500,
          data: { message: 'Internal server error' }
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Server error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.error).toHaveBeenCalledWith('HTTP error intercepted', {
        event: 'interceptor_error',
        correlation_id: correlationId,
        path: '/api/test',
        status: 500,
        message: 'Internal server error'
      });
    });

    it('should log timeout errors with correlation ID', async () => {
      const correlationId = 'timeout-test-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test'
        } as InternalAxiosRequestConfig,
        code: 'ECONNABORTED',
        isAxiosError: true,
        message: 'timeout of 30000ms exceeded',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.error).toHaveBeenCalledWith('Request timeout', {
        event: 'interceptor_timeout',
        correlation_id: correlationId,
        path: '/api/test'
      });
    });

    it('should NOT retry on 4xx errors other than network failures', async () => {
      const correlationId = '404-no-retry-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        response: {
          status: 404,
          data: { message: 'Not found' }
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Not found',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.info).not.toHaveBeenCalledWith('Retrying request', expect.anything());
    });

    it('should NOT retry on 5xx errors other than network failures', async () => {
      const correlationId = '500-no-retry-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        response: {
          status: 500,
          data: { message: 'Internal server error' }
        } as AxiosResponse,
        isAxiosError: true,
        message: 'Server error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();

      expect(logger.info).not.toHaveBeenCalledWith('Retrying request', expect.anything());
    });
  });

  describe('Network Error Detection', () => {
    it('should detect network error when response is undefined', async () => {
      const correlationId = 'no-response-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        response: undefined,
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.error).toHaveBeenCalledWith('Network error', expect.objectContaining({
        event: 'interceptor_network_error',
        correlation_id: correlationId
      }));
    });

    it('should detect network error with ERR_NETWORK code', async () => {
      const correlationId = 'err-network-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.error).toHaveBeenCalledWith('Network error', expect.objectContaining({
        event: 'interceptor_network_error',
        correlation_id: correlationId
      }));
    });
  });

  describe('Edge Cases', () => {
    it('should handle missing config gracefully', async () => {
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: undefined,
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();
    });

    it('should handle missing headers gracefully', async () => {
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      const error: AxiosError = {
        config: {
          url: '/api/test',
          method: 'GET'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      await expect(responseInterceptor.rejected(error)).rejects.toThrow();
    });

    it('should handle case-insensitive HTTP methods', async () => {
      const correlationId = 'case-insensitive-id';
      const responseInterceptor = (axiosInstance.interceptors.response as any).handlers[0];

      vi.spyOn(axiosInstance, 'request').mockResolvedValue({ data: 'success' } as AxiosResponse);

      const error: AxiosError = {
        config: {
          headers: { 'X-Correlation-ID': correlationId },
          url: '/api/test',
          method: 'get'
        } as InternalAxiosRequestConfig,
        code: 'ERR_NETWORK',
        isAxiosError: true,
        message: 'Network Error',
        name: 'AxiosError',
        toJSON: () => ({})
      };

      const retryPromise = responseInterceptor.rejected(error);
      
      await vi.advanceTimersByTimeAsync(1000);
      
      await retryPromise;

      expect(logger.info).toHaveBeenCalledWith('Retrying request', expect.objectContaining({
        event: 'request_retry'
      }));
    });
  });
});