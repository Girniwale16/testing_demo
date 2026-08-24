import axiosInstance from './axiosInstance';
import { LoginRequest, LoginResponse, UserProfile } from '../types/auth.types';
import { logger } from '../utils/logger';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
      logger.info('API call: POST /api/auth/login', {
        event: 'api_call',
        method: 'POST',
        path: '/api/auth/login',
        username: credentials.username
      });

      const response = await axiosInstance.post<LoginResponse>(
        '/api/auth/login',
        credentials
      );

      logger.info('API call successful: POST /api/auth/login', {
        event: 'api_success',
        method: 'POST',
        path: '/api/auth/login',
        status: response.status,
        username: response.data.user.username
      });

      return response.data;
    } catch (error) {
      logger.error('API call failed: POST /api/auth/login', {
        event: 'api_error',
        method: 'POST',
        path: '/api/auth/login',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
      throw error;
    }
  },

  logout: async (): Promise<void> => {
    try {
      logger.info('API call: POST /api/auth/logout', {
        event: 'api_call',
        method: 'POST',
        path: '/api/auth/logout'
      });

      const response = await axiosInstance.post('/api/auth/logout');

      logger.info('API call successful: POST /api/auth/logout', {
        event: 'api_success',
        method: 'POST',
        path: '/api/auth/logout',
        status: response.status
      });
    } catch (error) {
      logger.error('API call failed: POST /api/auth/logout', {
        event: 'api_error',
        method: 'POST',
        path: '/api/auth/logout',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
      throw error;
    }
  },

  getCurrentUser: async (): Promise<UserProfile> => {
    try {
      logger.info('API call: GET /api/auth/me', {
        event: 'api_call',
        method: 'GET',
        path: '/api/auth/me'
      });

      const response = await axiosInstance.get<UserProfile>('/api/auth/me');

      logger.info('API call successful: GET /api/auth/me', {
        event: 'api_success',
        method: 'GET',
        path: '/api/auth/me',
        status: response.status,
        username: response.data.username
      });

      return response.data;
    } catch (error) {
      logger.error('API call failed: GET /api/auth/me', {
        event: 'api_error',
        method: 'GET',
        path: '/api/auth/me',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
      throw error;
    }
  }
};