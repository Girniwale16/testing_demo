import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import axiosInstance from './axiosInstance';
import { logger } from '../utils/logger';

vi.mock('../utils/logger', () => ({
  logger: {
    info: vi.fn(),
    warn: vi.fn(),
    error: vi.fn()
  }
}));

describe('axiosInstance', () => {
  let localStorageMock: { [key: string]: string };
  let originalLocation: Location;

  beforeEach(() => {
    localStorageMock = {};
    
    global.localStorage = {
      getItem: vi.fn((key: string) => localStorageMock[key] || null),
      setItem: vi.fn((key: string, value: string) => {
        localStorageMock[key] = value;
      }),
      removeItem: vi.fn((key: string) => {
        delete localStorageMock[key];
      }),
      clear: vi.fn(() => {
        localStorageMock = {};
      }),
      length: 0,
      key: vi.fn()
    } as Storage;

    originalLocation = window.location;
    delete (window as any).location;
    window.location = {
      ...originalLocation,
      pathname: '/dashboard',
      href: ''
    } as Location;

    vi.clearAllMocks();
  });

  afterEach(() => {
    window.location = originalLocation;
  });

  describe('Axios Instance Configuration', () => {
    it('should create axios instance with correct baseURL from environment variable', () => {
      expect(axiosInstance.defaults.baseURL).toBe(import.meta.env.VITE_API_URL || '');
    });

    it('should have timeout set to 30000ms', () => {
      expect(axiosInstance.defaults.timeout).toBe(30000);
    });

    it('should have withCredentials set to true', () => {
      expect(axiosInstance.defaults.withCredentials).toBe(true);
    });

    it('should have Content-Type header set to application/json', () => {
      expect(axiosInstance.defaults.headers['Content-Type']).toBe('application/json');
    });
  });

  describe('Request Interceptor - Bearer Token Attachment', () => {
    it('should attach Bearer token from localStorage when token exists', async () => {
      localStorageMock['token'] = 'test-token-123';
      
      const config: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/test'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result = await interceptor(config);

      expect(result.headers['Authorization']).toBe('Bearer test-token-123');
    });

    it('should not attach Bearer token when token does not exist in localStorage', async () => {
      const config: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/test'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result = await interceptor(config);

      expect(result.headers['Authorization']).toBeUndefined();
    });

    it('should not override existing Authorization header', async () => {
      localStorageMock['token'] = 'test-token-123';
      
      const config: InternalAxiosRequestConfig = {
        headers: {
          'Authorization': 'Bearer existing-token'
        } as any,
        url: '/api/test'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result = await interceptor(config);

      expect(result.headers['Authorization']).toBe('Bearer existing-token');
    });
  });

  describe('Request Interceptor - Correlation ID Generation', () => {
    it('should generate and attach correlation ID when not present', async () => {
      const config: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/test'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result = await interceptor(config);

      expect(result.headers['X-Correlation-ID']).toBeDefined();
      expect(result.headers['X-Correlation-ID']).toMatch(/^fe-\d+-\d+-[a-z0-9]+$/);
    });

    it('should log correlation ID generation with correct event data', async () => {
      const config: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/test'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      await interceptor(config);

      expect(logger.info).toHaveBeenCalledWith(
        'Generated correlation ID for request',
        expect.objectContaining({
          event: 'correlation_id_generated',
          correlation_id: expect.stringMatching(/^fe-\d+-\d+-[a-z0-9]+$/),
          path: '/api/test'
        })
      );
    });

    it('should not override existing correlation ID', async () => {
      const existingCorrelationId = 'existing-correlation-id';
      const config: InternalAxiosRequestConfig = {
        headers: {
          'X-Correlation-ID': existingCorrelationId
        } as any,
        url: '/api/test'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result = await interceptor(config);

      expect(result.headers['X-Correlation-ID']).toBe(existingCorrelationId);
      expect(logger.info).not.toHaveBeenCalled();
    });

    it('should generate unique correlation IDs for multiple requests', async () => {
      const config1: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/test1'
      } as InternalAxiosRequestConfig;

      const config2: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/test2'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result1 = await interceptor(config1);
      const result2 = await interceptor(config2);

      expect(result1.headers['X-Correlation-ID']).not.toBe(result2.headers['X-Correlation-ID']);
    });
  });

  describe('Request Interceptor - Error Handling', () => {
    it('should log error and reject promise on request interceptor error', async () => {
      const error = new Error('Request setup failed');
      const interceptor = axiosInstance.interceptors.request['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toThrow('Request setup failed');

      expect(logger.error).toHaveBeenCalledWith(
        'Request interceptor error',
        {
          event: 'request_interceptor_error',
          error: 'Request setup failed'
        }
      );
    });

    it('should handle non-Error objects in request interceptor', async () => {
      const error = 'String error';
      const interceptor = axiosInstance.interceptors.request['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toBe('String error');

      expect(logger.error).toHaveBeenCalledWith(
        'Request interceptor error',
        {
          event: 'request_interceptor_error',
          error: 'Unknown error'
        }
      );
    });
  });

  describe('Response Interceptor - 401 Unauthorized Handling', () => {
    it('should clear localStorage and redirect to /login on 401 error', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/protected'
        } as any,
        response: {
          status: 401,
          data: {},
          statusText: 'Unauthorized',
          headers: {},
          config: {} as any
        }
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(localStorage.clear).toHaveBeenCalled();
      expect(window.location.href).toBe('/login');
      expect(logger.warn).toHaveBeenCalledWith(
        '401 Unauthorized - redirecting to login',
        {
          event: 'interceptor_401',
          correlation_id: 'test-correlation-id',
          path: '/api/protected',
          status: 401
        }
      );
    });

    it('should not redirect if already on /login page', async () => {
      window.location.pathname = '/login';

      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/auth/login'
        } as any,
        response: {
          status: 401,
          data: {},
          statusText: 'Unauthorized',
          headers: {},
          config: {} as any
        }
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(localStorage.clear).toHaveBeenCalled();
      expect(window.location.href).toBe('');
    });
  });

  describe('Response Interceptor - 403 Forbidden Handling', () => {
    it('should log 403 error with custom message from response', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/admin'
        } as any,
        response: {
          status: 403,
          data: { message: 'Insufficient permissions' },
          statusText: 'Forbidden',
          headers: {},
          config: {} as any
        }
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        '403 Forbidden - authorization error',
        {
          event: 'interceptor_403',
          correlation_id: 'test-correlation-id',
          path: '/api/admin',
          status: 403,
          message: 'Insufficient permissions'
        }
      );
    });

    it('should use default message for 403 error when no message in response', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/admin'
        } as any,
        response: {
          status: 403,
          data: {},
          statusText: 'Forbidden',
          headers: {},
          config: {} as any
        }
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        '403 Forbidden - authorization error',
        expect.objectContaining({
          message: 'Access forbidden'
        })
      );
    });
  });

  describe('Response Interceptor - HTTP Error Handling (4xx/5xx)', () => {
    it('should log 400 error with correlation ID', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/data'
        } as any,
        response: {
          status: 400,
          data: { message: 'Bad request' },
          statusText: 'Bad Request',
          headers: {},
          config: {} as any
        },
        message: 'Request failed with status code 400'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'HTTP error intercepted',
        {
          event: 'interceptor_error',
          correlation_id: 'test-correlation-id',
          path: '/api/data',
          status: 400,
          message: 'Bad request'
        }
      );
    });

    it('should log 500 error with correlation ID', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/data'
        } as any,
        response: {
          status: 500,
          data: { message: 'Internal server error' },
          statusText: 'Internal Server Error',
          headers: {},
          config: {} as any
        },
        message: 'Request failed with status code 500'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'HTTP error intercepted',
        {
          event: 'interceptor_error',
          correlation_id: 'test-correlation-id',
          path: '/api/data',
          status: 500,
          message: 'Internal server error'
        }
      );
    });

    it('should use error.message when response data has no message', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/data'
        } as any,
        response: {
          status: 404,
          data: {},
          statusText: 'Not Found',
          headers: {},
          config: {} as any
        },
        message: 'Resource not found'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'HTTP error intercepted',
        expect.objectContaining({
          message: 'Resource not found'
        })
      );
    });

    it('should use default message when no message available', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/data'
        } as any,
        response: {
          status: 422,
          data: {},
          statusText: 'Unprocessable Entity',
          headers: {},
          config: {} as any
        },
        message: ''
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'HTTP error intercepted',
        expect.objectContaining({
          message: 'Request failed'
        })
      );
    });
  });

  describe('Response Interceptor - Timeout Handling', () => {
    it('should log timeout error when error code is ECONNABORTED', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/slow'
        } as any,
        code: 'ECONNABORTED',
        message: 'timeout of 30000ms exceeded'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'Request timeout',
        {
          event: 'interceptor_timeout',
          correlation_id: 'test-correlation-id',
          path: '/api/slow'
        }
      );
    });

    it('should log timeout error when message includes "timeout"', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/slow'
        } as any,
        message: 'Request timeout occurred'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'Request timeout',
        {
          event: 'interceptor_timeout',
          correlation_id: 'test-correlation-id',
          path: '/api/slow'
        }
      );
    });
  });

  describe('Response Interceptor - Network Error Handling', () => {
    it('should log network error when no response status', async () => {
      const error: Partial<AxiosError> = {
        config: {
          headers: { 'X-Correlation-ID': 'test-correlation-id' },
          url: '/api/data'
        } as any,
        message: 'Network Error'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'Network error',
        {
          event: 'interceptor_network_error',
          correlation_id: 'test-correlation-id',
          path: '/api/data',
          error: 'Network Error'
        }
      );
    });

    it('should handle missing config in error', async () => {
      const error: Partial<AxiosError> = {
        message: 'Network Error'
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].rejected;

      await expect(interceptor(error)).rejects.toEqual(error);

      expect(logger.error).toHaveBeenCalledWith(
        'Network error',
        {
          event: 'interceptor_network_error',
          correlation_id: undefined,
          path: 'unknown',
          error: 'Network Error'
        }
      );
    });
  });

  describe('Response Interceptor - Successful Response', () => {
    it('should return response unchanged for successful requests', async () => {
      const response = {
        data: { success: true },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any
      };

      const interceptor = axiosInstance.interceptors.response['handlers'][0].fulfilled;
      const result = await interceptor(response);

      expect(result).toEqual(response);
      expect(logger.info).not.toHaveBeenCalled();
      expect(logger.error).not.toHaveBeenCalled();
      expect(logger.warn).not.toHaveBeenCalled();
    });
  });

  describe('Backward Compatibility', () => {
    it('should maintain existing authentication flow patterns', async () => {
      localStorageMock['token'] = 'auth-token';
      
      const config: InternalAxiosRequestConfig = {
        headers: {} as any,
        url: '/api/auth/profile'
      } as InternalAxiosRequestConfig;

      const interceptor = axiosInstance.interceptors.request['handlers'][0].fulfilled;
      const result = await interceptor(config);

      expect(result.headers['Authorization']).toBe('Bearer auth-token');
      expect(result.headers['X-Correlation-ID']).toBeDefined();
    });

    it('should be ready for staffApi.js consumption without breaking authApi.ts', () => {
      expect(axiosInstance).toBeDefined();
      expect(axiosInstance.defaults.baseURL).toBeDefined();
      expect(axiosInstance.interceptors.request['handlers'].length).toBeGreaterThan(0);
      expect(axiosInstance.interceptors.response['handlers'].length).toBeGreaterThan(0);
    });
  });
});