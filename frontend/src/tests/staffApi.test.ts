import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axiosInstance from '../api/axiosInstance';
import {
  createStaff,
  updateStaff,
  getStaff,
  listStaff,
  CreateStaffRequest,
  UpdateStaffRequest,
  StaffResponse,
} from '../api/staffApi';

vi.mock('../api/axiosInstance');

describe('staffApi', () => {
  const mockStaffResponse: StaffResponse = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    contact: 'john.doe@example.com',
    role: 'Nurse',
    employmentStatus: 'ACTIVE',
    startDate: '2024-01-01',
    endDate: undefined,
    facilityId: 100,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };

  const mockCreateStaffRequest: CreateStaffRequest = {
    firstName: 'John',
    lastName: 'Doe',
    contact: 'john.doe@example.com',
    role: 'Nurse',
    employmentStatus: 'ACTIVE',
    startDate: '2024-01-01',
  };

  const mockUpdateStaffRequest: UpdateStaffRequest = {
    firstName: 'Jane',
    role: 'Senior Nurse',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    global.crypto = {
      randomUUID: vi.fn(() => 'test-uuid-1234'),
    } as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('createStaff', () => {
    it('should successfully create staff with all required fields', async () => {
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

      const result = await createStaff(100, mockCreateStaffRequest);

      expect(axiosInstance.post).toHaveBeenCalledWith(
        '/api/staff',
        { ...mockCreateStaffRequest, facilityId: 100 },
        {
          headers: {
            'X-Idempotency-Key': 'test-uuid-1234',
          },
        }
      );
      expect(result).toEqual(mockStaffResponse);
    });

    it('should generate unique idempotency key for each request', async () => {
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

      await createStaff(100, mockCreateStaffRequest);

      expect(crypto.randomUUID).toHaveBeenCalledTimes(1);
    });

    it('should include optional startDate and endDate when provided', async () => {
      const staffDataWithDates: CreateStaffRequest = {
        ...mockCreateStaffRequest,
        startDate: '2024-01-01',
        endDate: '2024-12-31',
      };
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

      await createStaff(100, staffDataWithDates);

      expect(axiosInstance.post).toHaveBeenCalledWith(
        '/api/staff',
        { ...staffDataWithDates, facilityId: 100 },
        expect.any(Object)
      );
    });

    it('should attach correlation ID to error when present in response headers', async () => {
      const mockError = {
        response: {
          headers: {
            'x-correlation-id': 'corr-123',
          },
        },
      };
      vi.mocked(axiosInstance.post).mockRejectedValue(mockError);

      await expect(createStaff(100, mockCreateStaffRequest)).rejects.toMatchObject({
        correlationId: 'corr-123',
      });
    });

    it('should rethrow error without correlation ID when not present in headers', async () => {
      const mockError = new Error('Network error');
      vi.mocked(axiosInstance.post).mockRejectedValue(mockError);

      await expect(createStaff(100, mockCreateStaffRequest)).rejects.toThrow('Network error');
    });

    it('should handle error when response headers are undefined', async () => {
      const mockError = {
        response: {},
      };
      vi.mocked(axiosInstance.post).mockRejectedValue(mockError);

      await expect(createStaff(100, mockCreateStaffRequest)).rejects.toEqual(mockError);
    });

    it('should handle error when response is undefined', async () => {
      const mockError = new Error('Request failed');
      vi.mocked(axiosInstance.post).mockRejectedValue(mockError);

      await expect(createStaff(100, mockCreateStaffRequest)).rejects.toThrow('Request failed');
    });
  });

  describe('updateStaff', () => {
    it('should successfully update staff with partial data', async () => {
      const mockResponse = { data: { ...mockStaffResponse, firstName: 'Jane', role: 'Senior Nurse' } };
      vi.mocked(axiosInstance.put).mockResolvedValue(mockResponse);

      const result = await updateStaff(1, mockUpdateStaffRequest);

      expect(axiosInstance.put).toHaveBeenCalledWith(
        '/api/staff/1',
        mockUpdateStaffRequest,
        {
          headers: {
            'X-Idempotency-Key': 'test-uuid-1234',
          },
        }
      );
      expect(result).toEqual(mockResponse.data);
    });

    it('should generate unique idempotency key for update request', async () => {
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.put).mockResolvedValue(mockResponse);

      await updateStaff(1, mockUpdateStaffRequest);

      expect(crypto.randomUUID).toHaveBeenCalledTimes(1);
    });

    it('should update staff with all optional fields', async () => {
      const fullUpdateRequest: UpdateStaffRequest = {
        firstName: 'Jane',
        lastName: 'Smith',
        contact: 'jane.smith@example.com',
        role: 'Senior Nurse',
        employmentStatus: 'INACTIVE',
        startDate: '2024-02-01',
        endDate: '2024-12-31',
      };
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.put).mockResolvedValue(mockResponse);

      await updateStaff(1, fullUpdateRequest);

      expect(axiosInstance.put).toHaveBeenCalledWith(
        '/api/staff/1',
        fullUpdateRequest,
        expect.any(Object)
      );
    });

    it('should attach correlation ID to error when present in response headers', async () => {
      const mockError = {
        response: {
          headers: {
            'x-correlation-id': 'corr-456',
          },
        },
      };
      vi.mocked(axiosInstance.put).mockRejectedValue(mockError);

      await expect(updateStaff(1, mockUpdateStaffRequest)).rejects.toMatchObject({
        correlationId: 'corr-456',
      });
    });

    it('should rethrow error without correlation ID when not present', async () => {
      const mockError = new Error('Update failed');
      vi.mocked(axiosInstance.put).mockRejectedValue(mockError);

      await expect(updateStaff(1, mockUpdateStaffRequest)).rejects.toThrow('Update failed');
    });

    it('should handle different staff IDs correctly', async () => {
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.put).mockResolvedValue(mockResponse);

      await updateStaff(999, mockUpdateStaffRequest);

      expect(axiosInstance.put).toHaveBeenCalledWith(
        '/api/staff/999',
        mockUpdateStaffRequest,
        expect.any(Object)
      );
    });
  });

  describe('getStaff', () => {
    it('should successfully retrieve staff by ID', async () => {
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.get).mockResolvedValue(mockResponse);

      const result = await getStaff(1);

      expect(axiosInstance.get).toHaveBeenCalledWith('/api/staff/1');
      expect(result).toEqual(mockStaffResponse);
    });

    it('should retrieve staff with different IDs', async () => {
      const mockResponse = { data: mockStaffResponse };
      vi.mocked(axiosInstance.get).mockResolvedValue(mockResponse);

      await getStaff(42);

      expect(axiosInstance.get).toHaveBeenCalledWith('/api/staff/42');
    });

    it('should attach correlation ID to error when present in response headers', async () => {
      const mockError = {
        response: {
          headers: {
            'x-correlation-id': 'corr-789',
          },
        },
      };
      vi.mocked(axiosInstance.get).mockRejectedValue(mockError);

      await expect(getStaff(1)).rejects.toMatchObject({
        correlationId: 'corr-789',
      });
    });

    it('should rethrow error without correlation ID when not present', async () => {
      const mockError = new Error('Staff not found');
      vi.mocked(axiosInstance.get).mockRejectedValue(mockError);

      await expect(getStaff(1)).rejects.toThrow('Staff not found');
    });

    it('should handle 404 errors correctly', async () => {
      const mockError = {
        response: {
          status: 404,
          headers: {
            'x-correlation-id': 'corr-404',
          },
        },
      };
      vi.mocked(axiosInstance.get).mockRejectedValue(mockError);

      await expect(getStaff(999)).rejects.toMatchObject({
        correlationId: 'corr-404',
        response: { status: 404 },
      });
    });
  });

  describe('listStaff', () => {
    const mockStaffList: StaffResponse[] = [
      mockStaffResponse,
      {
        ...mockStaffResponse,
        id: 2,
        firstName: 'Jane',
        lastName: 'Smith',
      },
    ];

    it('should successfully retrieve list of staff for a facility', async () => {
      const mockResponse = { data: mockStaffList };
      vi.mocked(axiosInstance.get).mockResolvedValue(mockResponse);

      const result = await listStaff(100);

      expect(axiosInstance.get).toHaveBeenCalledWith('/api/facilities/100/staff');
      expect(result).toEqual(mockStaffList);
    });

    it('should retrieve staff list for different facility IDs', async () => {
      const mockResponse = { data: mockStaffList };
      vi.mocked(axiosInstance.get).mockResolvedValue(mockResponse);

      await listStaff(200);

      expect(axiosInstance.get).toHaveBeenCalledWith('/api/facilities/200/staff');
    });

    it('should return empty array when facility has no staff', async () => {
      const mockResponse = { data: [] };
      vi.mocked(axiosInstance.get).mockResolvedValue(mockResponse);

      const result = await listStaff(100);

      expect(result).toEqual([]);
    });

    it('should attach correlation ID to error when present in response headers', async () => {
      const mockError = {
        response: {
          headers: {
            'x-correlation-id': 'corr-list-123',
          },
        },
      };
      vi.mocked(axiosInstance.get).mockRejectedValue(mockError);

      await expect(listStaff(100)).rejects.toMatchObject({
        correlationId: 'corr-list-123',
      });
    });

    it('should rethrow error without correlation ID when not present', async () => {
      const mockError = new Error('Failed to fetch staff list');
      vi.mocked(axiosInstance.get).mockRejectedValue(mockError);

      await expect(listStaff(100)).rejects.toThrow('Failed to fetch staff list');
    });

    it('should handle facility not found errors', async () => {
      const mockError = {
        response: {
          status: 404,
          headers: {
            'x-correlation-id': 'corr-facility-404',
          },
        },
      };
      vi.mocked(axiosInstance.get).mockRejectedValue(mockError);

      await expect(listStaff(999)).rejects.toMatchObject({
        correlationId: 'corr-facility-404',
        response: { status: 404 },
      });
    });

    it('should handle large staff lists', async () => {
      const largeStaffList = Array.from({ length: 100 }, (_, i) => ({
        ...mockStaffResponse,
        id: i + 1,
      }));
      const mockResponse = { data: largeStaffList };
      vi.mocked(axiosInstance.get).mockResolvedValue(mockResponse);

      const result = await listStaff(100);

      expect(result).toHaveLength(100);
      expect(result).toEqual(largeStaffList);
    });
  });

  describe('Type Definitions', () => {
    it('should enforce CreateStaffRequest required fields', () => {
      const validRequest: CreateStaffRequest = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Nurse',
        employmentStatus: 'ACTIVE',
      };

      expect(validRequest).toBeDefined();
    });

    it('should allow optional fields in CreateStaffRequest', () => {
      const requestWithOptional: CreateStaffRequest = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Nurse',
        employmentStatus: 'ACTIVE',
        startDate: '2024-01-01',
        endDate: '2024-12-31',
      };

      expect(requestWithOptional.startDate).toBe('2024-01-01');
      expect(requestWithOptional.endDate).toBe('2024-12-31');
    });

    it('should allow all optional fields in UpdateStaffRequest', () => {
      const minimalUpdate: UpdateStaffRequest = {
        firstName: 'Jane',
      };

      expect(minimalUpdate).toBeDefined();
    });

    it('should enforce StaffResponse structure', () => {
      const response: StaffResponse = mockStaffResponse;

      expect(response.id).toBeDefined();
      expect(response.firstName).toBeDefined();
      expect(response.lastName).toBeDefined();
      expect(response.contact).toBeDefined();
      expect(response.role).toBeDefined();
      expect(response.employmentStatus).toBeDefined();
      expect(response.facilityId).toBeDefined();
      expect(response.createdAt).toBeDefined();
      expect(response.updatedAt).toBeDefined();
    });
  });
});