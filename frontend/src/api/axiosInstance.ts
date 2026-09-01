import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { logger } from '../utils/logger';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

let correlationIdCounter = 0;

function generateCorrelationId(): string {
  const timestamp = Date.now();
  const counter = ++correlationIdCounter;
  const random = Math.random().toString(36).substring(2, 9);
  return `fe-${timestamp}-${counter}-${random}`;
}

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (!config.headers['X-Correlation-ID']) {
      const correlationId = generateCorrelationId();
      config.headers['X-Correlation-ID'] = correlationId;
      logger.info('Generated correlation ID for request', {
        event: 'correlation_id_generated',
        correlation_id: correlationId,
        path: config.url
      });
    }
    return config;
  },
  (error) => {
    logger.error('Request interceptor error', {
      event: 'request_interceptor_error',
      error: error instanceof Error ? error.message : 'Unknown error'
    });
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    const correlationId = error.config?.headers?.['X-Correlation-ID'] as string;
    const path = error.config?.url || 'unknown';
    const status = error.response?.status;

    if (status === 401) {
      logger.warn('401 Unauthorized - redirecting to login', {
        event: 'interceptor_401',
        correlation_id: correlationId,
        path,
        status
      });
      
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    } else if (status === 403) {
      const errorMessage = (error.response?.data as any)?.message || 'Access forbidden';
      logger.error('403 Forbidden - authorization error', {
        event: 'interceptor_403',
        correlation_id: correlationId,
        path,
        status,
        message: errorMessage
      });
    } else if (status && status >= 400) {
      const errorMessage = (error.response?.data as any)?.message || error.message || 'Request failed';
      logger.error('HTTP error intercepted', {
        event: 'interceptor_error',
        correlation_id: correlationId,
        path,
        status,
        message: errorMessage
      });
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      logger.error('Request timeout', {
        event: 'interceptor_timeout',
        correlation_id: correlationId,
        path
      });
    } else {
      logger.error('Network error', {
        event: 'interceptor_network_error',
        correlation_id: correlationId,
        path,
        error: error.message
      });
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;