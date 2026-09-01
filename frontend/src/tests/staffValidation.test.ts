// frontend/src/utils/staffValidation.test.ts

import {
  validateStaffForm,
  validateRequiredFields,
  validateDateRange,
  validateContactFormat,
  StaffFormData,
  ValidationErrors
} from '../utils/staffValidation';

describe('staffValidation', () => {
  describe('validateRequiredFields', () => {
    it('should return empty errors object when all required fields are provided', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors).toEqual({});
    });

    it('should return error when firstName is missing', () => {
      const formData: StaffFormData = {
        firstName: '',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.firstName).toBe('First name is required');
    });

    it('should return error when firstName is only whitespace', () => {
      const formData: StaffFormData = {
        firstName: '   ',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.firstName).toBe('First name is required');
    });

    it('should return error when lastName is missing', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: '',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.lastName).toBe('Last name is required');
    });

    it('should return error when lastName is only whitespace', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: '   ',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.lastName).toBe('Last name is required');
    });

    it('should return error when contact is missing', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.contact).toBe('Contact is required');
    });

    it('should return error when contact is only whitespace', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '   ',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.contact).toBe('Contact is required');
    });

    it('should return error when role is missing', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: '',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.role).toBe('Role is required');
    });

    it('should return error when role is only whitespace', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: '   ',
        employmentStatus: 'Full-time'
      };

      const errors = validateRequiredFields(formData);

      expect(errors.role).toBe('Role is required');
    });

    it('should return error when employmentStatus is missing', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: ''
      };

      const errors = validateRequiredFields(formData);

      expect(errors.employmentStatus).toBe('Employment status is required');
    });

    it('should return error when employmentStatus is only whitespace', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: '   '
      };

      const errors = validateRequiredFields(formData);

      expect(errors.employmentStatus).toBe('Employment status is required');
    });

    it('should return multiple errors when multiple fields are missing', () => {
      const formData: StaffFormData = {
        firstName: '',
        lastName: '',
        contact: '',
        role: '',
        employmentStatus: ''
      };

      const errors = validateRequiredFields(formData);

      expect(errors.firstName).toBe('First name is required');
      expect(errors.lastName).toBe('Last name is required');
      expect(errors.contact).toBe('Contact is required');
      expect(errors.role).toBe('Role is required');
      expect(errors.employmentStatus).toBe('Employment status is required');
      expect(Object.keys(errors).length).toBe(5);
    });
  });

  describe('validateDateRange', () => {
    it('should return null when both dates are not provided', () => {
      const result = validateDateRange();

      expect(result).toBeNull();
    });

    it('should return null when only startDate is provided', () => {
      const result = validateDateRange('2024-01-01');

      expect(result).toBeNull();
    });

    it('should return null when only endDate is provided', () => {
      const result = validateDateRange(undefined, '2024-12-31');

      expect(result).toBeNull();
    });

    it('should return null when startDate is before endDate', () => {
      const result = validateDateRange('2024-01-01', '2024-12-31');

      expect(result).toBeNull();
    });

    it('should return error when startDate is equal to endDate', () => {
      const result = validateDateRange('2024-06-15', '2024-06-15');

      expect(result).toBe('Start date must be before end date');
    });

    it('should return error when startDate is after endDate', () => {
      const result = validateDateRange('2024-12-31', '2024-01-01');

      expect(result).toBe('Start date must be before end date');
    });

    it('should handle date strings with time components', () => {
      const result = validateDateRange('2024-06-15T10:00:00', '2024-06-15T09:00:00');

      expect(result).toBe('Start date must be before end date');
    });

    it('should validate correctly with different date formats', () => {
      const result = validateDateRange('2024-01-15', '2024-02-15');

      expect(result).toBeNull();
    });
  });

  describe('validateContactFormat', () => {
    it('should return null for valid email format', () => {
      const result = validateContactFormat('john.doe@example.com');

      expect(result).toBeNull();
    });

    it('should return null for valid email with subdomain', () => {
      const result = validateContactFormat('user@mail.example.com');

      expect(result).toBeNull();
    });

    it('should return null for valid email with plus sign', () => {
      const result = validateContactFormat('user+tag@example.com');

      expect(result).toBeNull();
    });

    it('should return null for valid phone number without formatting', () => {
      const result = validateContactFormat('1234567890');

      expect(result).toBeNull();
    });

    it('should return null for valid phone number with dashes', () => {
      const result = validateContactFormat('123-456-7890');

      expect(result).toBeNull();
    });

    it('should return null for valid phone number with parentheses', () => {
      const result = validateContactFormat('(123) 456-7890');

      expect(result).toBeNull();
    });

    it('should return null for valid phone number with country code', () => {
      const result = validateContactFormat('+1234567890');

      expect(result).toBeNull();
    });

    it('should return null for valid phone number with dots', () => {
      const result = validateContactFormat('123.456.7890');

      expect(result).toBeNull();
    });

    it('should return null for empty string', () => {
      const result = validateContactFormat('');

      expect(result).toBeNull();
    });

    it('should return null for whitespace-only string', () => {
      const result = validateContactFormat('   ');

      expect(result).toBeNull();
    });

    it('should return error for invalid email format - missing @', () => {
      const result = validateContactFormat('invalidemail.com');

      expect(result).toBe('Invalid contact format');
    });

    it('should return error for invalid email format - missing domain', () => {
      const result = validateContactFormat('user@');

      expect(result).toBe('Invalid contact format');
    });

    it('should return error for invalid email format - missing TLD', () => {
      const result = validateContactFormat('user@domain');

      expect(result).toBe('Invalid contact format');
    });

    it('should return error for invalid phone format - contains letters', () => {
      const result = validateContactFormat('123-abc-7890');

      expect(result).toBe('Invalid contact format');
    });

    it('should return error for invalid phone format - too short', () => {
      const result = validateContactFormat('123');

      expect(result).toBe('Invalid contact format');
    });

    it('should return error for random text', () => {
      const result = validateContactFormat('not a valid contact');

      expect(result).toBe('Invalid contact format');
    });

    it('should return error for special characters only', () => {
      const result = validateContactFormat('!@#$%^&*()');

      expect(result).toBe('Invalid contact format');
    });

    it('should handle contact with leading/trailing whitespace for valid email', () => {
      const result = validateContactFormat('  user@example.com  ');

      expect(result).toBeNull();
    });

    it('should handle contact with leading/trailing whitespace for valid phone', () => {
      const result = validateContactFormat('  123-456-7890  ');

      expect(result).toBeNull();
    });
  });

  describe('validateStaffForm', () => {
    it('should return empty errors object for completely valid form data', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time',
        startDate: '2024-01-01',
        endDate: '2024-12-31'
      };

      const errors = validateStaffForm(formData);

      expect(errors).toEqual({});
    });

    it('should return empty errors object for valid form without optional dates', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '123-456-7890',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateStaffForm(formData);

      expect(errors).toEqual({});
    });

    it('should merge required field errors into errors object', () => {
      const formData: StaffFormData = {
        firstName: '',
        lastName: '',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateStaffForm(formData);

      expect(errors.firstName).toBe('First name is required');
      expect(errors.lastName).toBe('Last name is required');
    });

    it('should add dateRange error when date validation fails', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time',
        startDate: '2024-12-31',
        endDate: '2024-01-01'
      };

      const errors = validateStaffForm(formData);

      expect(errors.dateRange).toBe('Start date must be before end date');
    });

    it('should add contact error when contact format validation fails', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'invalid-contact',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateStaffForm(formData);

      expect(errors.contact).toBe('Invalid contact format');
    });

    it('should return multiple errors when multiple validations fail', () => {
      const formData: StaffFormData = {
        firstName: '',
        lastName: '',
        contact: 'invalid',
        role: '',
        employmentStatus: '',
        startDate: '2024-12-31',
        endDate: '2024-01-01'
      };

      const errors = validateStaffForm(formData);

      expect(errors.firstName).toBe('First name is required');
      expect(errors.lastName).toBe('Last name is required');
      expect(errors.contact).toBe('Invalid contact format');
      expect(errors.role).toBe('Role is required');
      expect(errors.employmentStatus).toBe('Employment status is required');
      expect(errors.dateRange).toBe('Start date must be before end date');
      expect(Object.keys(errors).length).toBe(6);
    });

    it('should not add dateRange error when dates are valid', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time',
        startDate: '2024-01-01',
        endDate: '2024-12-31'
      };

      const errors = validateStaffForm(formData);

      expect(errors.dateRange).toBeUndefined();
    });

    it('should not add contact format error when contact is valid email', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john.doe@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateStaffForm(formData);

      expect(errors.contact).toBeUndefined();
    });

    it('should not add contact format error when contact is valid phone', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '+1-234-567-8900',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateStaffForm(formData);

      expect(errors.contact).toBeUndefined();
    });

    it('should handle form with only startDate provided', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time',
        startDate: '2024-01-01'
      };

      const errors = validateStaffForm(formData);

      expect(errors).toEqual({});
    });

    it('should handle form with only endDate provided', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: 'john@example.com',
        role: 'Developer',
        employmentStatus: 'Full-time',
        endDate: '2024-12-31'
      };

      const errors = validateStaffForm(formData);

      expect(errors).toEqual({});
    });

    it('should prioritize required field error over format error for contact', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '',
        role: 'Developer',
        employmentStatus: 'Full-time'
      };

      const errors = validateStaffForm(formData);

      expect(errors.contact).toBe('Contact is required');
    });

    it('should handle whitespace-only fields correctly', () => {
      const formData: StaffFormData = {
        firstName: '   ',
        lastName: '   ',
        contact: '   ',
        role: '   ',
        employmentStatus: '   '
      };

      const errors = validateStaffForm(formData);

      expect(errors.firstName).toBe('First name is required');
      expect(errors.lastName).toBe('Last name is required');
      expect(errors.contact).toBe('Contact is required');
      expect(errors.role).toBe('Role is required');
      expect(errors.employmentStatus).toBe('Employment status is required');
    });

    it('should validate form with all edge cases combined', () => {
      const formData: StaffFormData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '  user@example.com  ',
        role: 'Developer',
        employmentStatus: 'Full-time',
        startDate: '2024-01-01',
        endDate: '2024-12-31'
      };

      const errors = validateStaffForm(formData);

      expect(errors).toEqual({});
    });
  });
});