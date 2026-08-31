/**
 * Staff API Module Unit Tests
 * 
 * Comprehensive test suite for staffApi.ts ensuring 100% coverage of all
 * business logic including success paths, error handling, authentication,
 * and edge cases.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { listStaff, createStaff, updateStaff, deactivateStaff } from './staffApi';
import type { Staff, StaffListResponse, StaffFilters, PaginationParams } from '../types/staff.types';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; }
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock fetch
global.fetch = vi.fn();

describe('staffApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('listStaff', () => {
    const mockPagination: PaginationParams = { page: 1, limit: 10 };
    const mockStaffListResponse: StaffListResponse = {
      data: [
        {
          id: '1',
          name: 'John Doe',
          email: 'john@example.com',
          role: 'admin',
          department: 'IT',
          status: 'active',
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z'
        }
      ],
      total: 1,
      page: 1,
      limit: 10,
      totalPages: 1
    };

    it('should successfully list staff with pagination only', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockStaffListResponse
      });

      const result = await listStaff(mockPagination);

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/staff?page=1&limit=10',
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token'
          }
        }
      );
      expect(result).toEqual(mockStaffListResponse);
    });

    it('should successfully list staff with pagination and all filters', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      const filters: StaffFilters = {
        name: 'John',
        role: 'admin',
        status: 'active',
        department: 'IT'
      };

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockStaffListResponse
      });

      const result = await listStaff(mockPagination, filters);

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/staff?page=1&limit=10&name=John&role=admin&status=active&department=IT',
        expect.any(Object)
      );
      expect(result).toEqual(mockStaffListResponse);
    });

    it('should successfully list staff with partial filters', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      const filters: StaffFilters = {
        name: 'John',
        role: 'admin'
      };

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockStaffListResponse
      });

      await listStaff(mockPagination, filters);

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/staff?page=1&limit=10&name=John&role=admin',
        expect.any(Object)
      );
    });

    it('should make request without Authorization header when no token exists', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockStaffListResponse
      });

      await listStaff(mockPagination);

      expect(global.fetch).toHaveBeenCalledWith(
        expect.any(String),
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
    });

    it('should throw UNAUTHORIZED_REDIRECT_TO_LOGIN error on 401 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ message: 'Unauthorized', statusCode: 401 })
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('UNAUTHORIZED_REDIRECT_TO_LOGIN');
    });

    it('should throw not authorized error on 403 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ message: 'Forbidden', statusCode: 403 })
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('You are not authorized to view staff members');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 500 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ message: 'Internal Server Error', statusCode: 500 })
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 503 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 503,
        json: async () => ({ message: 'Service Unavailable', statusCode: 503 })
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw custom error message from ErrorResponse on 400 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ message: 'Invalid pagination parameters', statusCode: 400 })
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('Invalid pagination parameters');
    });

    it('should throw default error message when ErrorResponse has no message', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ statusCode: 400 })
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('Failed to fetch staff list');
    });

    it('should handle json parsing error and use fallback error', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => { throw new Error('JSON parse error'); }
      });

      await expect(listStaff(mockPagination)).rejects.toThrow('Failed to fetch staff list');
    });

    it('should throw unexpected error message for non-Error exceptions', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockRejectedValueOnce('string error');

      await expect(listStaff(mockPagination)).rejects.toThrow('An unexpected error occurred while fetching staff list');
    });

    it('should rethrow Error instances', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      const customError = new Error('Network failure');
      (global.fetch as any).mockRejectedValueOnce(customError);

      await expect(listStaff(mockPagination)).rejects.toThrow('Network failure');
    });
  });

  describe('createStaff', () => {
    const mockStaffData = {
      name: 'Jane Smith',
      email: 'jane@example.com',
      role: 'manager',
      department: 'HR',
      status: 'active'
    };

    const mockCreatedStaff: Staff = {
      id: '2',
      ...mockStaffData,
      createdAt: '2024-01-02T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z'
    };

    it('should successfully create staff member', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockCreatedStaff
      });

      const result = await createStaff(mockStaffData);

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/staff',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token'
          },
          body: JSON.stringify(mockStaffData)
        }
      );
      expect(result).toEqual(mockCreatedStaff);
    });

    it('should make request without Authorization header when no token exists', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockCreatedStaff
      });

      await createStaff(mockStaffData);

      expect(global.fetch).toHaveBeenCalledWith(
        '/api/staff',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(mockStaffData)
        }
      );
    });

    it('should throw UNAUTHORIZED_REDIRECT_TO_LOGIN error on 401 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ message: 'Unauthorized', statusCode: 401 })
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('UNAUTHORIZED_REDIRECT_TO_LOGIN');
    });

    it('should throw not authorized error on 403 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ message: 'Forbidden', statusCode: 403 })
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('You are not authorized to create staff members');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 500 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ message: 'Internal Server Error', statusCode: 500 })
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 502 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 502,
        json: async () => ({ message: 'Bad Gateway', statusCode: 502 })
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw custom error message from ErrorResponse on 422 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 422,
        json: async () => ({ message: 'Email already exists', statusCode: 422 })
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('Email already exists');
    });

    it('should throw default error message when ErrorResponse has no message', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ statusCode: 400 })
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('Failed to create staff member');
    });

    it('should handle json parsing error and use fallback error', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => { throw new Error('JSON parse error'); }
      });

      await expect(createStaff(mockStaffData)).rejects.toThrow('Failed to create staff member');
    });

    it('should throw unexpected error message for non-Error exceptions', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockRejectedValueOnce(123);

      await expect(createStaff(mockStaffData)).rejects.toThrow('An unexpected error occurred while creating staff member');
    });

    it('should rethrow Error instances', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      const customError = new Error('Connection timeout');
      (global.fetch as any).mockRejectedValueOnce(customError);

      await expect(createStaff(mockStaffData)).rejects.toThrow('Connection timeout');
    });
  });

  describe('updateStaff', () => {
    const staffId = 'staff-123';
    const mockUpdateData = {
      name: 'John Updated',
      department: 'Engineering'
    };

    const mockUpdatedStaff: Staff = {
      id: staffId,
      name: 'John Updated',
      email: 'john@example.com',
      role: 'admin',
      department: 'Engineering',
      status: 'active',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-03T00:00:00Z'
    };

    it('should successfully update staff member', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockUpdatedStaff
      });

      const result = await updateStaff(staffId, mockUpdateData);

      expect(global.fetch).toHaveBeenCalledWith(
        `/api/staff/${staffId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token'
          },
          body: JSON.stringify(mockUpdateData)
        }
      );
      expect(result).toEqual(mockUpdatedStaff);
    });

    it('should make request without Authorization header when no token exists', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockUpdatedStaff
      });

      await updateStaff(staffId, mockUpdateData);

      expect(global.fetch).toHaveBeenCalledWith(
        `/api/staff/${staffId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(mockUpdateData)
        }
      );
    });

    it('should throw UNAUTHORIZED_REDIRECT_TO_LOGIN error on 401 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ message: 'Unauthorized', statusCode: 401 })
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('UNAUTHORIZED_REDIRECT_TO_LOGIN');
    });

    it('should throw not authorized error on 403 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ message: 'Forbidden', statusCode: 403 })
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('You are not authorized to update staff members');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 500 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ message: 'Internal Server Error', statusCode: 500 })
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 504 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 504,
        json: async () => ({ message: 'Gateway Timeout', statusCode: 504 })
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw custom error message from ErrorResponse on 404 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ message: 'Staff member not found', statusCode: 404 })
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('Staff member not found');
    });

    it('should throw default error message when ErrorResponse has no message', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ statusCode: 400 })
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('Failed to update staff member');
    });

    it('should handle json parsing error and use fallback error', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => { throw new Error('JSON parse error'); }
      });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('Failed to update staff member');
    });

    it('should throw unexpected error message for non-Error exceptions', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockRejectedValueOnce({ error: 'object error' });

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('An unexpected error occurred while updating staff member');
    });

    it('should rethrow Error instances', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      const customError = new Error('Database connection lost');
      (global.fetch as any).mockRejectedValueOnce(customError);

      await expect(updateStaff(staffId, mockUpdateData)).rejects.toThrow('Database connection lost');
    });
  });

  describe('deactivateStaff', () => {
    const staffId = 'staff-456';

    it('should successfully deactivate staff member', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({})
      });

      await deactivateStaff(staffId);

      expect(global.fetch).toHaveBeenCalledWith(
        `/api/staff/${staffId}/deactivate`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token'
          }
        }
      );
    });

    it('should make request without Authorization header when no token exists', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({})
      });

      await deactivateStaff(staffId);

      expect(global.fetch).toHaveBeenCalledWith(
        `/api/staff/${staffId}/deactivate`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
    });

    it('should throw UNAUTHORIZED_REDIRECT_TO_LOGIN error on 401 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ message: 'Unauthorized', statusCode: 401 })
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('UNAUTHORIZED_REDIRECT_TO_LOGIN');
    });

    it('should throw not authorized error on 403 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ message: 'Forbidden', statusCode: 403 })
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('You are not authorized to deactivate staff members');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 500 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ message: 'Internal Server Error', statusCode: 500 })
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw SERVER_ERROR_RETRY_BANNER error on 501 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 501,
        json: async () => ({ message: 'Not Implemented', statusCode: 501 })
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('SERVER_ERROR_RETRY_BANNER');
    });

    it('should throw custom error message from ErrorResponse on 409 response', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 409,
        json: async () => ({ message: 'Staff member already deactivated', statusCode: 409 })
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('Staff member already deactivated');
    });

    it('should throw default error message when ErrorResponse has no message', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ statusCode: 400 })
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('Failed to deactivate staff member');
    });

    it('should handle json parsing error and use fallback error', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => { throw new Error('JSON parse error'); }
      });

      await expect(deactivateStaff(staffId)).rejects.toThrow('Failed to deactivate staff member');
    });

    it('should throw unexpected error message for non-Error exceptions', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockRejectedValueOnce(null);

      await expect(deactivateStaff(staffId)).rejects.toThrow('An unexpected error occurred while deactivating staff member');
    });

    it('should rethrow Error instances', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      const customError = new Error('Request aborted');
      (global.fetch as any).mockRejectedValueOnce(customError);

      await expect(deactivateStaff(staffId)).rejects.toThrow('Request aborted');
    });

    it('should complete successfully without returning a value', async () => {
      localStorageMock.setItem('authToken', 'test-token');
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({})
      });

      const result = await deactivateStaff(staffId);

      expect(result).toBeUndefined();
    });
  });
});