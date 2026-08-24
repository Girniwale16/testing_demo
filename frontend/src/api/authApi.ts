import axiosInstance from './axiosInstance';
import { LoginRequest, LoginResponse, UserProfile } from '../types/auth.types';
import { logger } from '../utils/logger';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
      logger.info('API call: POST /api/v1/auth/login', {
        event: 'api_call',
        method: 'POST',
        path: '/api/v1/auth/login',
        username: credentials.username
      });

      const response = await axiosInstance.post<LoginResponse>(
        '/api/v1/auth/login',
        credentials
      );

      logger.info('API call successful: POST /api/v1/auth/login', {
        event: 'api_success',
        method: 'POST',
        path: '/api/v1/auth/login',
        status: response.status,
        username: response.data.username
      });

      return response.data;
    } catch (error) {
      logger.error('API call failed: POST /api/v1/auth/login', {
        event: 'api_error',
        method: 'POST',
        path: '/api/v1/auth/login',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
      throw error;
    }
  },

  logout: async (): Promise<void> => {
    try {
      logger.info('API call: POST /api/v1/auth/logout', {
        event: 'api_call',
        method: 'POST',
        path: '/api/v1/auth/logout'
      });

      const response = await axiosInstance.post('/api/v1/auth/logout');

      logger.info('API call successful: POST /api/v1/auth/logout', {
        event: 'api_success',
        method: 'POST',
        path: '/api/v1/auth/logout',
        status: response.status
      });
    } catch (error) {
      logger.error('API call failed: POST /api/v1/auth/logout', {
        event: 'api_error',
        method: 'POST',
        path: '/api/v1/auth/logout',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
      throw error;
    }
  },

  getCurrentUser: async (): Promise<UserProfile> => {
    try {
      logger.info('API call: GET /api/v1/auth/session', {
        event: 'api_call',
        method: 'GET',
        path: '/api/v1/auth/session'
      });

      const response = await axiosInstance.get<UserProfile>('/api/v1/auth/session');

      logger.info('API call successful: GET /api/v1/auth/session', {
        event: 'api_success',
        method: 'GET',
        path: '/api/v1/auth/session',
        status: response.status,
        username: response.data.username
      });

      return response.data;
    } catch (error) {
      logger.error('API call failed: GET /api/v1/auth/session', {
        event: 'api_error',
        method: 'GET',
        path: '/api/v1/auth/session',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
      throw error;
    }
  }
};