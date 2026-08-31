/**
 * Unit Tests for Staff Management Type Definitions
 * 
 * This test suite validates the structure and type safety of all interfaces
 * defined in staff.types.ts, ensuring proper TypeScript compilation and
 * interface contract adherence.
 */

import {
  Staff,
  StaffListResponse,
  PaginationMetadata,
  StaffFilters,
  PaginationParams,
  CreateStaffRequest,
  UpdateStaffRequest,
} from './staff.types';

describe('Staff Types', () => {
  describe('Staff Interface', () => {
    it('should accept a valid Staff object with all required fields', () => {
      const validStaff: Staff = {
        id: '123e4567-e89b-12d3-a456-426614174000',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        role: 'Administrator',
        isActive: true,
        createdAt: '2024-01-15T10:30:00Z',
        updatedAt: '2024-01-15T10:30:00Z',
      };

      expect(validStaff.id).toBe('123e4567-e89b-12d3-a456-426614174000');
      expect(validStaff.firstName).toBe('John');
      expect(validStaff.lastName).toBe('Doe');
      expect(validStaff.email).toBe('john.doe@example.com');
      expect(validStaff.role).toBe('Administrator');
      expect(validStaff.isActive).toBe(true);
      expect(validStaff.createdAt).toBe('2024-01-15T10:30:00Z');
      expect(validStaff.updatedAt).toBe('2024-01-15T10:30:00Z');
    });

    it('should accept Staff object with isActive set to false', () => {
      const inactiveStaff: Staff = {
        id: '123e4567-e89b-12d3-a456-426614174001',
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane.smith@example.com',
        role: 'Manager',
        isActive: false,
        createdAt: '2024-01-10T08:00:00Z',
        updatedAt: '2024-01-20T14:30:00Z',
      };

      expect(inactiveStaff.isActive).toBe(false);
    });

    it('should accept Staff object with different role values', () => {
      const roles = ['Administrator', 'Manager', 'Staff', 'Supervisor', 'Intern'];
      
      roles.forEach((role) => {
        const staff: Staff = {
          id: `id-${role}`,
          firstName: 'Test',
          lastName: 'User',
          email: `test.${role}@example.com`,
          role: role,
          isActive: true,
          createdAt: '2024-01-15T10:30:00Z',
          updatedAt: '2024-01-15T10:30:00Z',
        };

        expect(staff.role).toBe(role);
      });
    });

    it('should accept Staff object with ISO 8601 formatted timestamps', () => {
      const staff: Staff = {
        id: '123',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'Staff',
        isActive: true,
        createdAt: '2024-01-15T10:30:00.000Z',
        updatedAt: '2024-01-20T15:45:30.123Z',
      };

      expect(staff.createdAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/);
      expect(staff.updatedAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/);
    });
  });

  describe('PaginationMetadata Interface', () => {
    it('should accept valid PaginationMetadata with all required fields', () => {
      const pagination: PaginationMetadata = {
        currentPage: 1,
        totalPages: 10,
        totalItems: 100,
        itemsPerPage: 10,
      };

      expect(pagination.currentPage).toBe(1);
      expect(pagination.totalPages).toBe(10);
      expect(pagination.totalItems).toBe(100);
      expect(pagination.itemsPerPage).toBe(10);
    });

    it('should accept PaginationMetadata with single page', () => {
      const pagination: PaginationMetadata = {
        currentPage: 1,
        totalPages: 1,
        totalItems: 5,
        itemsPerPage: 10,
      };

      expect(pagination.currentPage).toBe(1);
      expect(pagination.totalPages).toBe(1);
      expect(pagination.totalItems).toBe(5);
    });

    it('should accept PaginationMetadata with zero items', () => {
      const pagination: PaginationMetadata = {
        currentPage: 1,
        totalPages: 0,
        totalItems: 0,
        itemsPerPage: 10,
      };

      expect(pagination.totalItems).toBe(0);
      expect(pagination.totalPages).toBe(0);
    });

    it('should accept PaginationMetadata with large page numbers', () => {
      const pagination: PaginationMetadata = {
        currentPage: 50,
        totalPages: 100,
        totalItems: 1000,
        itemsPerPage: 10,
      };

      expect(pagination.currentPage).toBe(50);
      expect(pagination.totalPages).toBe(100);
    });

    it('should accept PaginationMetadata with different itemsPerPage values', () => {
      const itemsPerPageValues = [5, 10, 20, 50, 100];

      itemsPerPageValues.forEach((itemsPerPage) => {
        const pagination: PaginationMetadata = {
          currentPage: 1,
          totalPages: 10,
          totalItems: 100,
          itemsPerPage: itemsPerPage,
        };

        expect(pagination.itemsPerPage).toBe(itemsPerPage);
      });
    });
  });

  describe('StaffListResponse Interface', () => {
    it('should accept valid StaffListResponse with data and pagination', () => {
      const response: StaffListResponse = {
        data: [
          {
            id: '1',
            firstName: 'John',
            lastName: 'Doe',
            email: 'john@example.com',
            role: 'Admin',
            isActive: true,
            createdAt: '2024-01-15T10:30:00Z',
            updatedAt: '2024-01-15T10:30:00Z',
          },
        ],
        pagination: {
          currentPage: 1,
          totalPages: 1,
          totalItems: 1,
          itemsPerPage: 10,
        },
      };

      expect(response.data).toHaveLength(1);
      expect(response.data[0].firstName).toBe('John');
      expect(response.pagination.totalItems).toBe(1);
    });

    it('should accept StaffListResponse with empty data array', () => {
      const response: StaffListResponse = {
        data: [],
        pagination: {
          currentPage: 1,
          totalPages: 0,
          totalItems: 0,
          itemsPerPage: 10,
        },
      };

      expect(response.data).toHaveLength(0);
      expect(response.pagination.totalItems).toBe(0);
    });

    it('should accept StaffListResponse with multiple staff members', () => {
      const response: StaffListResponse = {
        data: [
          {
            id: '1',
            firstName: 'John',
            lastName: 'Doe',
            email: 'john@example.com',
            role: 'Admin',
            isActive: true,
            createdAt: '2024-01-15T10:30:00Z',
            updatedAt: '2024-01-15T10:30:00Z',
          },
          {
            id: '2',
            firstName: 'Jane',
            lastName: 'Smith',
            email: 'jane@example.com',
            role: 'Manager',
            isActive: false,
            createdAt: '2024-01-10T08:00:00Z',
            updatedAt: '2024-01-20T14:30:00Z',
          },
          {
            id: '3',
            firstName: 'Bob',
            lastName: 'Johnson',
            email: 'bob@example.com',
            role: 'Staff',
            isActive: true,
            createdAt: '2024-01-12T09:15:00Z',
            updatedAt: '2024-01-12T09:15:00Z',
          },
        ],
        pagination: {
          currentPage: 1,
          totalPages: 1,
          totalItems: 3,
          itemsPerPage: 10,
        },
      };

      expect(response.data).toHaveLength(3);
      expect(response.pagination.totalItems).toBe(3);
    });

    it('should accept StaffListResponse with paginated data', () => {
      const response: StaffListResponse = {
        data: Array.from({ length: 10 }, (_, i) => ({
          id: `${i + 1}`,
          firstName: `FirstName${i + 1}`,
          lastName: `LastName${i + 1}`,
          email: `user${i + 1}@example.com`,
          role: 'Staff',
          isActive: true,
          createdAt: '2024-01-15T10:30:00Z',
          updatedAt: '2024-01-15T10:30:00Z',
        })),
        pagination: {
          currentPage: 2,
          totalPages: 5,
          totalItems: 50,
          itemsPerPage: 10,
        },
      };

      expect(response.data).toHaveLength(10);
      expect(response.pagination.currentPage).toBe(2);
      expect(response.pagination.totalPages).toBe(5);
    });
  });

  describe('StaffFilters Interface', () => {
    it('should accept StaffFilters with all optional fields', () => {
      const filters: StaffFilters = {
        isActive: true,
        role: 'Administrator',
        searchQuery: 'john',
      };

      expect(filters.isActive).toBe(true);
      expect(filters.role).toBe('Administrator');
      expect(filters.searchQuery).toBe('john');
    });

    it('should accept StaffFilters with only isActive field', () => {
      const filters: StaffFilters = {
        isActive: false,
      };

      expect(filters.isActive).toBe(false);
      expect(filters.role).toBeUndefined();
      expect(filters.searchQuery).toBeUndefined();
    });

    it('should accept StaffFilters with only role field', () => {
      const filters: StaffFilters = {
        role: 'Manager',
      };

      expect(filters.role).toBe('Manager');
      expect(filters.isActive).toBeUndefined();
      expect(filters.searchQuery).toBeUndefined();
    });

    it('should accept StaffFilters with only searchQuery field', () => {
      const filters: StaffFilters = {
        searchQuery: 'jane.smith@example.com',
      };

      expect(filters.searchQuery).toBe('jane.smith@example.com');
      expect(filters.isActive).toBeUndefined();
      expect(filters.role).toBeUndefined();
    });

    it('should accept empty StaffFilters object', () => {
      const filters: StaffFilters = {};

      expect(filters.isActive).toBeUndefined();
      expect(filters.role).toBeUndefined();
      expect(filters.searchQuery).toBeUndefined();
    });

    it('should accept StaffFilters with isActive true and role combination', () => {
      const filters: StaffFilters = {
        isActive: true,
        role: 'Staff',
      };

      expect(filters.isActive).toBe(true);
      expect(filters.role).toBe('Staff');
    });

    it('should accept StaffFilters with isActive false and searchQuery combination', () => {
      const filters: StaffFilters = {
        isActive: false,
        searchQuery: 'inactive user',
      };

      expect(filters.isActive).toBe(false);
      expect(filters.searchQuery).toBe('inactive user');
    });

    it('should accept StaffFilters with role and searchQuery combination', () => {
      const filters: StaffFilters = {
        role: 'Administrator',
        searchQuery: 'admin',
      };

      expect(filters.role).toBe('Administrator');
      expect(filters.searchQuery).toBe('admin');
    });
  });

  describe('PaginationParams Interface', () => {
    it('should accept valid PaginationParams with page and limit', () => {
      const params: PaginationParams = {
        page: 1,
        limit: 10,
      };

      expect(params.page).toBe(1);
      expect(params.limit).toBe(10);
    });

    it('should accept PaginationParams with first page', () => {
      const params: PaginationParams = {
        page: 1,
        limit: 20,
      };

      expect(params.page).toBe(1);
      expect(params.limit).toBe(20);
    });

    it('should accept PaginationParams with large page number', () => {
      const params: PaginationParams = {
        page: 100,
        limit: 10,
      };

      expect(params.page).toBe(100);
    });

    it('should accept PaginationParams with different limit values', () => {
      const limitValues = [5, 10, 20, 50, 100];

      limitValues.forEach((limit) => {
        const params: PaginationParams = {
          page: 1,
          limit: limit,
        };

        expect(params.limit).toBe(limit);
      });
    });

    it('should accept PaginationParams with various page and limit combinations', () => {
      const combinations = [
        { page: 1, limit: 10 },
        { page: 5, limit: 20 },
        { page: 10, limit: 50 },
        { page: 20, limit: 100 },
      ];

      combinations.forEach(({ page, limit }) => {
        const params: PaginationParams = { page, limit };

        expect(params.page).toBe(page);
        expect(params.limit).toBe(limit);
      });
    });
  });

  describe('CreateStaffRequest Interface', () => {
    it('should accept valid CreateStaffRequest with all required fields', () => {
      const request: CreateStaffRequest = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        role: 'Administrator',
      };

      expect(request.firstName).toBe('John');
      expect(request.lastName).toBe('Doe');
      expect(request.email).toBe('john.doe@example.com');
      expect(request.role).toBe('Administrator');
    });

    it('should accept CreateStaffRequest with different role values', () => {
      const roles = ['Administrator', 'Manager', 'Staff', 'Supervisor'];

      roles.forEach((role) => {
        const request: CreateStaffRequest = {
          firstName: 'Test',
          lastName: 'User',
          email: `test.${role}@example.com`,
          role: role,
        };

        expect(request.role).toBe(role);
      });
    });

    it('should accept CreateStaffRequest with various email formats', () => {
      const emails = [
        'simple@example.com',
        'first.last@example.com',
        'user+tag@example.co.uk',
        'user_name@sub.example.com',
      ];

      emails.forEach((email) => {
        const request: CreateStaffRequest = {
          firstName: 'Test',
          lastName: 'User',
          email: email,
          role: 'Staff',
        };

        expect(request.email).toBe(email);
      });
    });

    it('should accept CreateStaffRequest with names containing special characters', () => {
      const request: CreateStaffRequest = {
        firstName: "O'Brien",
        lastName: 'Smith-Jones',
        email: 'obrien.smith@example.com',
        role: 'Manager',
      };

      expect(request.firstName).toBe("O'Brien");
      expect(request.lastName).toBe('Smith-Jones');
    });

    it('should accept CreateStaffRequest with single character names', () => {
      const request: CreateStaffRequest = {
        firstName: 'A',
        lastName: 'B',
        email: 'a.b@example.com',
        role: 'Staff',
      };

      expect(request.firstName).toBe('A');
      expect(request.lastName).toBe('B');
    });
  });

  describe('UpdateStaffRequest Interface', () => {
    it('should accept UpdateStaffRequest with all optional fields', () => {
      const request: UpdateStaffRequest = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        role: 'Administrator',
      };

      expect(request.firstName).toBe('John');
      expect(request.lastName).toBe('Doe');
      expect(request.email).toBe('john.doe@example.com');
      expect(request.role).toBe('Administrator');
    });

    it('should accept UpdateStaffRequest with only firstName', () => {
      const request: UpdateStaffRequest = {
        firstName: 'Jane',
      };

      expect(request.firstName).toBe('Jane');
      expect(request.lastName).toBeUndefined();
      expect(request.email).toBeUndefined();
      expect(request.role).toBeUndefined();
    });

    it('should accept UpdateStaffRequest with only lastName', () => {
      const request: UpdateStaffRequest = {
        lastName: 'Smith',
      };

      expect(request.lastName).toBe('Smith');
      expect(request.firstName).toBeUndefined();
      expect(request.email).toBeUndefined();
      expect(request.role).toBeUndefined();
    });

    it('should accept UpdateStaffRequest with only email', () => {
      const request: UpdateStaffRequest = {
        email: 'newemail@example.com',
      };

      expect(request.email).toBe('newemail@example.com');
      expect(request.firstName).toBeUndefined();
      expect(request.lastName).toBeUndefined();
      expect(request.role).toBeUndefined();
    });

    it('should accept UpdateStaffRequest with only role', () => {
      const request: UpdateStaffRequest = {
        role: 'Manager',
      };

      expect(request.role).toBe('Manager');
      expect(request.firstName).toBeUndefined();
      expect(request.lastName).toBeUndefined();
      expect(request.email).toBeUndefined();
    });

    it('should accept empty UpdateStaffRequest object', () => {
      const request: UpdateStaffRequest = {};

      expect(request.firstName).toBeUndefined();
      expect(request.lastName).toBeUndefined();
      expect(request.email).toBeUndefined();
      expect(request.role).toBeUndefined();
    });

    it('should accept UpdateStaffRequest with firstName and lastName combination', () => {
      const request: UpdateStaffRequest = {
        firstName: 'John',
        lastName: 'Doe',
      };

      expect(request.firstName).toBe('John');
      expect(request.lastName).toBe('Doe');
      expect(request.email).toBeUndefined();
      expect(request.role).toBeUndefined();
    });

    it('should accept UpdateStaffRequest with email and role combination', () => {
      const request: UpdateStaffRequest = {
        email: 'updated@example.com',
        role: 'Supervisor',
      };

      expect(request.email).toBe('updated@example.com');
      expect(request.role).toBe('Supervisor');
      expect(request.firstName).toBeUndefined();
      expect(request.lastName).toBeUndefined();
    });

    it('should accept UpdateStaffRequest with firstName, lastName, and email combination', () => {
      const request: UpdateStaffRequest = {
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane.smith@example.com',
      };

      expect(request.firstName).toBe('Jane');
      expect(request.lastName).toBe('Smith');
      expect(request.email).toBe('jane.smith@example.com');
      expect(request.role).toBeUndefined();
    });
  });

  describe('Type Safety and Interface Contracts', () => {
    it('should ensure Staff interface has all required properties', () => {
      const staff: Staff = {
        id: '123',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        role: 'Admin',
        isActive: true,
        createdAt: '2024-01-15T10:30:00Z',
        updatedAt: '2024-01-15T10:30:00Z',
      };

      const requiredKeys: (keyof Staff)[] = [
        'id',
        'firstName',
        'lastName',
        'email',
        'role',
        'isActive',
        'createdAt',
        'updatedAt',
      ];

      requiredKeys.forEach((key) => {
        expect(staff[key]).toBeDefined();
      });
    });

    it('should ensure PaginationMetadata interface has all required properties', () => {
      const pagination: PaginationMetadata = {
        currentPage: 1,
        totalPages: 10,
        totalItems: 100,
        itemsPerPage: 10,
      };

      const requiredKeys: (keyof PaginationMetadata)[] = [
        'currentPage',
        'totalPages',
        'totalItems',
        'itemsPerPage',
      ];

      requiredKeys.forEach((key) => {
        expect(pagination[key]).toBeDefined();
      });
    });

    it('should ensure StaffListResponse interface has all required properties', () => {
      const response: StaffListResponse = {
        data: [],
        pagination: {
          currentPage: 1,
          totalPages: 0,
          totalItems: 0,
          itemsPerPage: 10,
        },
      };

      expect(response.data).toBeDefined();
      expect(response.pagination).toBeDefined();
    });

    it('should ensure CreateStaffRequest interface has all required properties', () => {
      const request: CreateStaffRequest = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        role: 'Admin',
      };

      const requiredKeys: (keyof CreateStaffRequest)[] = [
        'firstName',
        'lastName',
        'email',
        'role',
      ];

      requiredKeys.forEach((key) => {
        expect(request[key]).toBeDefined();
      });
    });

    it('should verify UpdateStaffRequest allows partial updates', () => {
      const partialUpdates: UpdateStaffRequest[] = [
        { firstName: 'John' },
        { lastName: 'Doe' },
        { email: 'john@example.com' },
        { role: 'Admin' },
        { firstName: 'John', lastName: 'Doe' },
        { email: 'john@example.com', role: 'Admin' },
        {},
      ];

      partialUpdates.forEach((update) => {
        expect(update).toBeDefined();
      });
    });
  });
});