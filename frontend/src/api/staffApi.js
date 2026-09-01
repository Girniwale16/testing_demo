import staffApi from '../staffApi';
import axiosInstance from '../../api/axiosInstance.ts';

jest.mock('../../api/axiosInstance.ts');

describe('staffApi', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('list', () => {
    it('should successfully retrieve paginated staff list with data nested in response', async () => {
      const mockResponse = {
        data: {
          data: [
            { id: '1', firstName: 'John', lastName: 'Doe', role: 'Nurse', facilityName: 'Main Hospital' },
            { id: '2', firstName: 'Jane', lastName: 'Smith', role: 'Doctor', facilityName: 'Main Hospital' }
          ],
          totalPages: 5
        }
      };
      axiosInstance.get.mockResolvedValue(mockResponse);

      const result = await staffApi.list(0, 10, false);

      expect(axiosInstance.get).toHaveBeenCalledWith('/api/staff', {
        params: {
          page: 0,
          size: 10,
          includeDeactivated: false
        }
      });
      expect(result).toEqual({
        data: mockResponse.data.data,
        totalPages: 5
      });
    });

    it('should successfully retrieve paginated staff list with data at root level', async () => {
      const mockStaffArray = [
        { id: '1', firstName: 'John', lastName: 'Doe', role: 'Nurse', facilityName: 'Main Hospital' }
      ];
      const mockResponse = {
        data: mockStaffArray
      };
      axiosInstance.get.mockResolvedValue(mockResponse);

      const result = await staffApi.list(1, 20, true);

      expect(axiosInstance.get).toHaveBeenCalledWith('/api/staff', {
        params: {
          page: 1,
          size: 20,
          includeDeactivated: true
        }
      });
      expect(result.data).toEqual(mockStaffArray);
    });

    it('should throw standardized error with custom type from server response', async () => {
      const mockError = {
        response: {
          status: 400,
          data: {
            type: 'VALIDATION_ERROR',
            message: 'Invalid page number',
            details: 'Page must be non-negative'
          }
        }
      };
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(-1, 10, false)).rejects.toEqual({
        type: 'VALIDATION_ERROR',
        message: 'Invalid page number',
        details: 'Page must be non-negative',
        canRetry: false
      });
    });

    it('should throw standardized error with default type when server error type is missing', async () => {
      const mockError = {
        response: {
          status: 404,
          data: {
            message: 'Not found'
          }
        }
      };
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(0, 10, false)).rejects.toEqual({
        type: 'STAFF_LIST_ERROR',
        message: 'Not found',
        details: expect.any(String),
        canRetry: false
      });
    });

    it('should throw standardized error with canRetry true for 500 server errors', async () => {
      const mockError = {
        response: {
          status: 500,
          data: {
            message: 'Internal server error'
          }
        }
      };
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(0, 10, false)).rejects.toEqual({
        type: 'STAFF_LIST_ERROR',
        message: 'Internal server error',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with canRetry true for connection timeout', async () => {
      const mockError = {
        code: 'ETIMEDOUT',
        message: 'Connection timeout'
      };
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(0, 10, false)).rejects.toEqual({
        type: 'STAFF_LIST_ERROR',
        message: 'Connection timeout',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with canRetry true for connection aborted', async () => {
      const mockError = {
        code: 'ECONNABORTED',
        message: 'Connection aborted'
      };
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(0, 10, false)).rejects.toEqual({
        type: 'STAFF_LIST_ERROR',
        message: 'Connection aborted',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with default message when all error properties are missing', async () => {
      const mockError = new Error();
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(0, 10, false)).rejects.toEqual({
        type: 'STAFF_LIST_ERROR',
        message: 'Failed to retrieve staff list',
        details: expect.any(String),
        canRetry: false
      });
    });
  });

  describe('create', () => {
    it('should successfully create a new staff member', async () => {
      const staffData = {
        firstName: 'John',
        lastName: 'Doe',
        role: 'Nurse',
        facilityName: 'Main Hospital'
      };
      const mockResponse = {
        data: {
          id: '123',
          firstName: 'John',
          lastName: 'Doe',
          role: 'Nurse',
          facilityName: 'Main Hospital'
        }
      };
      axiosInstance.post.mockResolvedValue(mockResponse);

      const result = await staffApi.create(staffData);

      expect(axiosInstance.post).toHaveBeenCalledWith('/api/staff', {
        firstName: 'John',
        lastName: 'Doe',
        role: 'Nurse',
        facilityName: 'Main Hospital'
      });
      expect(result).toEqual({
        id: '123',
        firstName: 'John',
        lastName: 'Doe',
        role: 'Nurse',
        facilityName: 'Main Hospital'
      });
    });

    it('should map response data to StaffResponse DTO structure', async () => {
      const staffData = {
        firstName: 'Jane',
        lastName: 'Smith',
        role: 'Doctor',
        facilityName: 'East Clinic'
      };
      const mockResponse = {
        data: {
          id: '456',
          firstName: 'Jane',
          lastName: 'Smith',
          role: 'Doctor',
          facilityName: 'East Clinic',
          extraField: 'should not be included'
        }
      };
      axiosInstance.post.mockResolvedValue(mockResponse);

      const result = await staffApi.create(staffData);

      expect(result).toEqual({
        id: '456',
        firstName: 'Jane',
        lastName: 'Smith',
        role: 'Doctor',
        facilityName: 'East Clinic'
      });
      expect(result.extraField).toBeUndefined();
    });

    it('should throw standardized error with custom type from server response', async () => {
      const mockError = {
        response: {
          status: 400,
          data: {
            type: 'DUPLICATE_STAFF',
            message: 'Staff member already exists',
            details: 'A staff member with this name already exists'
          }
        }
      };
      axiosInstance.post.mockRejectedValue(mockError);

      await expect(staffApi.create({ firstName: 'John', lastName: 'Doe' })).rejects.toEqual({
        type: 'DUPLICATE_STAFF',
        message: 'Staff member already exists',
        details: 'A staff member with this name already exists',
        canRetry: false
      });
    });

    it('should throw standardized error with default type when server error type is missing', async () => {
      const mockError = {
        response: {
          status: 422,
          data: {
            message: 'Validation failed'
          }
        }
      };
      axiosInstance.post.mockRejectedValue(mockError);

      await expect(staffApi.create({ firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_CREATE_ERROR',
        message: 'Validation failed',
        details: expect.any(String),
        canRetry: false
      });
    });

    it('should throw standardized error with canRetry true for 503 service unavailable', async () => {
      const mockError = {
        response: {
          status: 503,
          data: {
            message: 'Service temporarily unavailable'
          }
        }
      };
      axiosInstance.post.mockRejectedValue(mockError);

      await expect(staffApi.create({ firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_CREATE_ERROR',
        message: 'Service temporarily unavailable',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with default message when all error properties are missing', async () => {
      const mockError = new Error();
      axiosInstance.post.mockRejectedValue(mockError);

      await expect(staffApi.create({ firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_CREATE_ERROR',
        message: 'Failed to create staff member',
        details: expect.any(String),
        canRetry: false
      });
    });
  });

  describe('update', () => {
    it('should successfully update an existing staff member', async () => {
      const staffData = {
        firstName: 'John',
        lastName: 'Doe',
        role: 'Senior Nurse',
        facilityName: 'Main Hospital'
      };
      const mockResponse = {
        data: {
          id: '123',
          firstName: 'John',
          lastName: 'Doe',
          role: 'Senior Nurse',
          facilityName: 'Main Hospital'
        }
      };
      axiosInstance.put.mockResolvedValue(mockResponse);

      const result = await staffApi.update('123', staffData);

      expect(axiosInstance.put).toHaveBeenCalledWith('/api/staff/123', {
        firstName: 'John',
        lastName: 'Doe',
        role: 'Senior Nurse',
        facilityName: 'Main Hospital'
      });
      expect(result).toEqual({
        id: '123',
        firstName: 'John',
        lastName: 'Doe',
        role: 'Senior Nurse',
        facilityName: 'Main Hospital'
      });
    });

    it('should map response data to StaffResponse DTO structure', async () => {
      const staffData = {
        firstName: 'Jane',
        lastName: 'Smith',
        role: 'Chief Doctor',
        facilityName: 'East Clinic'
      };
      const mockResponse = {
        data: {
          id: '456',
          firstName: 'Jane',
          lastName: 'Smith',
          role: 'Chief Doctor',
          facilityName: 'East Clinic',
          updatedAt: '2024-01-01',
          extraField: 'should not be included'
        }
      };
      axiosInstance.put.mockResolvedValue(mockResponse);

      const result = await staffApi.update('456', staffData);

      expect(result).toEqual({
        id: '456',
        firstName: 'Jane',
        lastName: 'Smith',
        role: 'Chief Doctor',
        facilityName: 'East Clinic'
      });
      expect(result.updatedAt).toBeUndefined();
      expect(result.extraField).toBeUndefined();
    });

    it('should construct correct URL with staff ID', async () => {
      const staffData = {
        firstName: 'Test',
        lastName: 'User',
        role: 'Admin',
        facilityName: 'Test Facility'
      };
      const mockResponse = {
        data: {
          id: 'abc-123',
          firstName: 'Test',
          lastName: 'User',
          role: 'Admin',
          facilityName: 'Test Facility'
        }
      };
      axiosInstance.put.mockResolvedValue(mockResponse);

      await staffApi.update('abc-123', staffData);

      expect(axiosInstance.put).toHaveBeenCalledWith('/api/staff/abc-123', expect.any(Object));
    });

    it('should throw standardized error with custom type from server response', async () => {
      const mockError = {
        response: {
          status: 404,
          data: {
            type: 'STAFF_NOT_FOUND',
            message: 'Staff member not found',
            details: 'No staff member exists with the provided ID'
          }
        }
      };
      axiosInstance.put.mockRejectedValue(mockError);

      await expect(staffApi.update('999', { firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_NOT_FOUND',
        message: 'Staff member not found',
        details: 'No staff member exists with the provided ID',
        canRetry: false
      });
    });

    it('should throw standardized error with default type when server error type is missing', async () => {
      const mockError = {
        response: {
          status: 400,
          data: {
            message: 'Invalid data'
          }
        }
      };
      axiosInstance.put.mockRejectedValue(mockError);

      await expect(staffApi.update('123', { firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_UPDATE_ERROR',
        message: 'Invalid data',
        details: expect.any(String),
        canRetry: false
      });
    });

    it('should throw standardized error with canRetry true for 502 bad gateway', async () => {
      const mockError = {
        response: {
          status: 502,
          data: {
            message: 'Bad gateway'
          }
        }
      };
      axiosInstance.put.mockRejectedValue(mockError);

      await expect(staffApi.update('123', { firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_UPDATE_ERROR',
        message: 'Bad gateway',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with canRetry true for ECONNABORTED', async () => {
      const mockError = {
        code: 'ECONNABORTED',
        message: 'Request aborted'
      };
      axiosInstance.put.mockRejectedValue(mockError);

      await expect(staffApi.update('123', { firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_UPDATE_ERROR',
        message: 'Request aborted',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with default message when all error properties are missing', async () => {
      const mockError = new Error();
      axiosInstance.put.mockRejectedValue(mockError);

      await expect(staffApi.update('123', { firstName: 'John' })).rejects.toEqual({
        type: 'STAFF_UPDATE_ERROR',
        message: 'Failed to update staff member',
        details: expect.any(String),
        canRetry: false
      });
    });
  });

  describe('deactivate', () => {
    it('should successfully deactivate a staff member', async () => {
      axiosInstance.delete.mockResolvedValue({});

      await staffApi.deactivate('123');

      expect(axiosInstance.delete).toHaveBeenCalledWith('/api/staff/123');
    });

    it('should return void on successful deactivation', async () => {
      axiosInstance.delete.mockResolvedValue({ data: { success: true } });

      const result = await staffApi.deactivate('456');

      expect(result).toBeUndefined();
    });

    it('should construct correct URL with staff ID', async () => {
      axiosInstance.delete.mockResolvedValue({});

      await staffApi.deactivate('abc-xyz-789');

      expect(axiosInstance.delete).toHaveBeenCalledWith('/api/staff/abc-xyz-789');
    });

    it('should throw standardized error with custom type from server response', async () => {
      const mockError = {
        response: {
          status: 404,
          data: {
            type: 'STAFF_NOT_FOUND',
            message: 'Staff member not found',
            details: 'Cannot deactivate non-existent staff member'
          }
        }
      };
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('999')).rejects.toEqual({
        type: 'STAFF_NOT_FOUND',
        message: 'Staff member not found',
        details: 'Cannot deactivate non-existent staff member',
        canRetry: false
      });
    });

    it('should throw standardized error with default type when server error type is missing', async () => {
      const mockError = {
        response: {
          status: 409,
          data: {
            message: 'Staff member already deactivated'
          }
        }
      };
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('123')).rejects.toEqual({
        type: 'STAFF_DEACTIVATE_ERROR',
        message: 'Staff member already deactivated',
        details: expect.any(String),
        canRetry: false
      });
    });

    it('should throw standardized error with canRetry true for 500 internal server error', async () => {
      const mockError = {
        response: {
          status: 500,
          data: {
            message: 'Database error'
          }
        }
      };
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('123')).rejects.toEqual({
        type: 'STAFF_DEACTIVATE_ERROR',
        message: 'Database error',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with canRetry true for ETIMEDOUT', async () => {
      const mockError = {
        code: 'ETIMEDOUT',
        message: 'Request timeout'
      };
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('123')).rejects.toEqual({
        type: 'STAFF_DEACTIVATE_ERROR',
        message: 'Request timeout',
        details: expect.any(String),
        canRetry: true
      });
    });

    it('should throw standardized error with canRetry false for 4xx client errors', async () => {
      const mockError = {
        response: {
          status: 403,
          data: {
            message: 'Forbidden'
          }
        }
      };
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('123')).rejects.toEqual({
        type: 'STAFF_DEACTIVATE_ERROR',
        message: 'Forbidden',
        details: expect.any(String),
        canRetry: false
      });
    });

    it('should throw standardized error with default message when all error properties are missing', async () => {
      const mockError = new Error();
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('123')).rejects.toEqual({
        type: 'STAFF_DEACTIVATE_ERROR',
        message: 'Failed to deactivate staff member',
        details: expect.any(String),
        canRetry: false
      });
    });
  });

  describe('error handling edge cases', () => {
    it('should handle error with message property but no response', async () => {
      const mockError = {
        message: 'Network error occurred'
      };
      axiosInstance.get.mockRejectedValue(mockError);

      await expect(staffApi.list(0, 10, false)).rejects.toEqual({
        type: 'STAFF_LIST_ERROR',
        message: 'Network error occurred',
        details: expect.any(String),
        canRetry: false
      });
    });

    it('should handle error with toString method', async () => {
      const mockError = {
        toString: () => 'Custom error string'
      };
      axiosInstance.post.mockRejectedValue(mockError);

      await expect(staffApi.create({ firstName: 'John' })).rejects.toMatchObject({
        details: 'Custom error string'
      });
    });

    it('should handle 504 gateway timeout as retryable', async () => {
      const mockError = {
        response: {
          status: 504,
          data: {
            message: 'Gateway timeout'
          }
        }
      };
      axiosInstance.put.mockRejectedValue(mockError);

      await expect(staffApi.update('123', { firstName: 'John' })).rejects.toMatchObject({
        canRetry: true
      });
    });

    it('should handle 501 not implemented as retryable', async () => {
      const mockError = {
        response: {
          status: 501,
          data: {
            message: 'Not implemented'
          }
        }
      };
      axiosInstance.delete.mockRejectedValue(mockError);

      await expect(staffApi.deactivate('123')).rejects.toMatchObject({
        canRetry: true
      });
    });
  });
});