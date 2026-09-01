import { renderHook, act, waitFor } from '@testing-library/react';
import { useStaffForm } from '../useStaffForm';
import { createStaff, updateStaff, getStaff } from '../../api/staffApi';
import { validateStaffForm } from '../../utils/staffValidation';
import logger from '../../utils/logger';

jest.mock('../../api/staffApi');
jest.mock('../../utils/staffValidation');
jest.mock('../../utils/logger');

const mockedCreateStaff = createStaff as jest.MockedFunction<typeof createStaff>;
const mockedUpdateStaff = updateStaff as jest.MockedFunction<typeof updateStaff>;
const mockedGetStaff = getStaff as jest.MockedFunction<typeof getStaff>;
const mockedValidateStaffForm = validateStaffForm as jest.MockedFunction<typeof validateStaffForm>;
const mockedLogger = logger as jest.Mocked<typeof logger>;

describe('useStaffForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('Initialization', () => {
    it('should initialize with default form data and state values', () => {
      const { result } = renderHook(() => useStaffForm());

      expect(result.current.formData).toEqual({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        role: '',
        department: '',
        hireDate: '',
        status: 'active'
      });
      expect(result.current.submitting).toBe(false);
      expect(result.current.success).toBe(false);
      expect(result.current.error).toBe(null);
      expect(result.current.fieldErrors).toEqual({});
    });

    it('should initialize without loading staff data when staffId is not provided', () => {
      renderHook(() => useStaffForm());

      expect(mockedGetStaff).not.toHaveBeenCalled();
    });
  });

  describe('useEffect - Load existing staff data', () => {
    it('should load staff data when staffId is provided', async () => {
      const mockStaffData = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phone: '1234567890',
        role: 'Nurse',
        department: 'Emergency',
        hireDate: '2023-01-01',
        status: 'active'
      };

      mockedGetStaff.mockResolvedValue({ data: mockStaffData });

      const { result } = renderHook(() => useStaffForm(123));

      await waitFor(() => {
        expect(result.current.formData).toEqual(mockStaffData);
      });

      expect(mockedGetStaff).toHaveBeenCalledWith(123);
      expect(mockedLogger.info).toHaveBeenCalledWith(
        'Loading staff data for ID: 123',
        expect.objectContaining({ correlationId: expect.any(String) })
      );
      expect(mockedLogger.info).toHaveBeenCalledWith(
        'Successfully loaded staff data for ID: 123',
        expect.objectContaining({ correlationId: expect.any(String) })
      );
    });

    it('should set error state when loading staff data fails', async () => {
      const mockError = new Error('Failed to fetch staff');
      mockedGetStaff.mockRejectedValue(mockError);

      const { result } = renderHook(() => useStaffForm(123));

      await waitFor(() => {
        expect(result.current.error).toBe('Failed to fetch staff');
      });

      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Error loading staff data: Failed to fetch staff',
        expect.objectContaining({
          correlationId: expect.any(String),
          staffId: 123,
          error: mockError
        })
      );
    });

    it('should set default error message when error has no message', async () => {
      mockedGetStaff.mockRejectedValue({});

      const { result } = renderHook(() => useStaffForm(123));

      await waitFor(() => {
        expect(result.current.error).toBe('Failed to load staff data');
      });
    });
  });

  describe('handleSubmit - Validation', () => {
    it('should validate form data before submission', async () => {
      const validationErrors = { firstName: 'First name is required' };
      mockedValidateStaffForm.mockReturnValue(validationErrors);

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(mockedValidateStaffForm).toHaveBeenCalledWith(result.current.formData);
      expect(result.current.fieldErrors).toEqual(validationErrors);
      expect(mockedCreateStaff).not.toHaveBeenCalled();
      expect(mockedUpdateStaff).not.toHaveBeenCalled();
    });

    it('should return early if validation errors exist', async () => {
      mockedValidateStaffForm.mockReturnValue({ email: 'Invalid email' });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.submitting).toBe(false);
      expect(mockedCreateStaff).not.toHaveBeenCalled();
    });

    it('should proceed with submission when validation passes', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(mockedCreateStaff).toHaveBeenCalled();
    });
  });

  describe('handleSubmit - Create mode', () => {
    it('should call createStaff when staffId is not provided', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      act(() => {
        result.current.setFormData({
          firstName: 'Jane',
          lastName: 'Smith',
          email: 'jane@example.com',
          phone: '9876543210',
          role: 'Doctor',
          department: 'Cardiology',
          hireDate: '2023-06-01',
          status: 'active'
        });
      });

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(mockedCreateStaff).toHaveBeenCalledWith(1, result.current.formData);
      expect(result.current.success).toBe(true);
      expect(mockedLogger.info).toHaveBeenCalledWith(
        'Creating new staff for facility ID: 1',
        expect.objectContaining({ correlationId: expect.any(String) })
      );
      expect(mockedLogger.info).toHaveBeenCalledWith(
        'Successfully created new staff for facility ID: 1',
        expect.objectContaining({ correlationId: expect.any(String) })
      );
    });

    it('should throw error when facilityId is not provided in create mode', async () => {
      mockedValidateStaffForm.mockReturnValue({});

      const { result } = renderHook(() => useStaffForm());

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.error).toBe('Facility ID is required for creating new staff');
      expect(mockedCreateStaff).not.toHaveBeenCalled();
    });

    it('should set submitting state during create operation', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      let resolveCreate: any;
      mockedCreateStaff.mockReturnValue(
        new Promise((resolve) => {
          resolveCreate = resolve;
        })
      );

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      act(() => {
        result.current.handleSubmit();
      });

      await waitFor(() => {
        expect(result.current.submitting).toBe(true);
      });

      await act(async () => {
        resolveCreate({ data: {} });
      });

      await waitFor(() => {
        expect(result.current.submitting).toBe(false);
      });
    });
  });

  describe('handleSubmit - Update mode', () => {
    it('should call updateStaff when staffId is provided', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedUpdateStaff.mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useStaffForm(123, 1));

      act(() => {
        result.current.setFormData({
          firstName: 'John',
          lastName: 'Updated',
          email: 'john.updated@example.com',
          phone: '1111111111',
          role: 'Senior Nurse',
          department: 'ICU',
          hireDate: '2023-01-01',
          status: 'active'
        });
      });

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(mockedUpdateStaff).toHaveBeenCalledWith(123, result.current.formData);
      expect(result.current.success).toBe(true);
      expect(mockedLogger.info).toHaveBeenCalledWith(
        'Updating staff ID: 123',
        expect.objectContaining({ correlationId: expect.any(String) })
      );
      expect(mockedLogger.info).toHaveBeenCalledWith(
        'Successfully updated staff ID: 123',
        expect.objectContaining({ correlationId: expect.any(String) })
      );
    });

    it('should set submitting state during update operation', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      let resolveUpdate: any;
      mockedUpdateStaff.mockReturnValue(
        new Promise((resolve) => {
          resolveUpdate = resolve;
        })
      );

      const { result } = renderHook(() => useStaffForm(123, 1));

      act(() => {
        result.current.handleSubmit();
      });

      await waitFor(() => {
        expect(result.current.submitting).toBe(true);
      });

      await act(async () => {
        resolveUpdate({ data: {} });
      });

      await waitFor(() => {
        expect(result.current.submitting).toBe(false);
      });
    });
  });

  describe('handleSubmit - State management', () => {
    it('should reset error, fieldErrors, and success states before submission', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      act(() => {
        result.current.setFieldError('email', 'Previous error');
      });

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.error).toBe(null);
      expect(result.current.fieldErrors).toEqual({});
    });

    it('should set success to true on successful submission', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.success).toBe(true);
    });

    it('should always set submitting to false in finally block', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockRejectedValue(new Error('Submission failed'));

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.submitting).toBe(false);
    });
  });

  describe('handleSubmit - 422 Validation errors', () => {
    it('should handle 422 validation errors from server', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      const serverErrors = {
        email: 'Email already exists',
        phone: 'Invalid phone format'
      };
      mockedCreateStaff.mockRejectedValue({
        response: {
          status: 422,
          data: { errors: serverErrors }
        }
      });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.fieldErrors).toEqual(serverErrors);
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Validation errors from server',
        expect.objectContaining({
          correlationId: expect.any(String),
          errors: serverErrors
        })
      );
    });

    it('should handle 422 error with empty errors object', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockRejectedValue({
        response: {
          status: 422,
          data: {}
        }
      });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.fieldErrors).toEqual({});
    });

    it('should handle 422 error without data property', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockRejectedValue({
        response: {
          status: 422
        }
      });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.fieldErrors).toEqual({});
    });
  });

  describe('handleSubmit - Network error retry logic', () => {
    it('should retry on ECONNABORTED error up to 3 times', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff
        .mockRejectedValueOnce({ code: 'ECONNABORTED', message: 'Connection aborted' })
        .mockRejectedValueOnce({ code: 'ECONNABORTED', message: 'Connection aborted' })
        .mockResolvedValueOnce({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      const submitPromise = act(async () => {
        await result.current.handleSubmit();
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await submitPromise;

      expect(mockedCreateStaff).toHaveBeenCalledTimes(3);
      expect(result.current.success).toBe(true);
      expect(mockedLogger.warn).toHaveBeenCalledTimes(2);
    });

    it('should retry on Network Error message up to 3 times', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff
        .mockRejectedValueOnce({ message: 'Network Error occurred' })
        .mockResolvedValueOnce({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      const submitPromise = act(async () => {
        await result.current.handleSubmit();
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await submitPromise;

      expect(mockedCreateStaff).toHaveBeenCalledTimes(2);
      expect(result.current.success).toBe(true);
    });

    it('should use exponential backoff delays (1s, 2s, 4s)', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff
        .mockRejectedValueOnce({ code: 'ECONNABORTED' })
        .mockRejectedValueOnce({ code: 'ECONNABORTED' })
        .mockRejectedValueOnce({ code: 'ECONNABORTED' })
        .mockResolvedValueOnce({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      const submitPromise = act(async () => {
        await result.current.handleSubmit();
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      expect(mockedLogger.warn).toHaveBeenCalledWith(
        expect.stringContaining('retrying in 1000ms'),
        expect.any(Object)
      );

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      expect(mockedLogger.warn).toHaveBeenCalledWith(
        expect.stringContaining('retrying in 2000ms'),
        expect.any(Object)
      );

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      await submitPromise;

      expect(mockedCreateStaff).toHaveBeenCalledTimes(4);
    });

    it('should set network error message after all retries fail', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockRejectedValue({ code: 'ECONNABORTED', message: 'Connection aborted' });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      const submitPromise = act(async () => {
        await result.current.handleSubmit();
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await act(async () => {
        jest.advanceTimersByTime(2000);
      });

      await act(async () => {
        jest.advanceTimersByTime(4000);
      });

      await submitPromise;

      expect(mockedCreateStaff).toHaveBeenCalledTimes(3);
      expect(result.current.error).toBe('Network error. Please check your connection and try again.');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Network error after 3 retries',
        expect.objectContaining({
          correlationId: expect.any(String)
        })
      );
    });

    it('should log retry attempts with correlation ID', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff
        .mockRejectedValueOnce({ code: 'ECONNABORTED', message: 'Connection aborted' })
        .mockResolvedValueOnce({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      const submitPromise = act(async () => {
        await result.current.handleSubmit();
      });

      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      await submitPromise;

      expect(mockedLogger.warn).toHaveBeenCalledWith(
        'Network error, retrying in 1000ms (attempt 1/3)',
        expect.objectContaining({
          correlationId: expect.any(String),
          error: 'Connection aborted'
        })
      );
    });
  });

  describe('handleSubmit - General error handling', () => {
    it('should set error message for non-422, non-network errors', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockRejectedValue(new Error('Server error'));

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.error).toBe('Server error');
      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Error submitting staff form: Server error',
        expect.objectContaining({
          correlationId: expect.any(String)
        })
      );
    });

    it('should use default error message when error has no message', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockRejectedValue({});

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.error).toBe('An unexpected error occurred');
    });

    it('should log all errors with correlation ID', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      const mockError = new Error('Test error');
      mockedCreateStaff.mockRejectedValue(mockError);

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(mockedLogger.error).toHaveBeenCalledWith(
        'Error submitting staff form: Test error',
        expect.objectContaining({
          correlationId: expect.any(String),
          error: mockError
        })
      );
    });
  });

  describe('resetForm', () => {
    it('should reset formData to initial values', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFormData({
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@example.com',
          phone: '1234567890',
          role: 'Nurse',
          department: 'Emergency',
          hireDate: '2023-01-01',
          status: 'active'
        });
      });

      act(() => {
        result.current.resetForm();
      });

      expect(result.current.formData).toEqual({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        role: '',
        department: '',
        hireDate: '',
        status: 'active'
      });
    });

    it('should reset submitting to false', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.resetForm();
      });

      expect(result.current.submitting).toBe(false);
    });

    it('should reset success to false', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.resetForm();
      });

      expect(result.current.success).toBe(false);
    });

    it('should reset error to null', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.resetForm();
      });

      expect(result.current.error).toBe(null);
    });

    it('should reset fieldErrors to empty object', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFieldError('email', 'Invalid email');
      });

      act(() => {
        result.current.resetForm();
      });

      expect(result.current.fieldErrors).toEqual({});
    });

    it('should reset all state values together', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFormData({
          firstName: 'Test',
          lastName: 'User',
          email: 'test@example.com',
          phone: '9999999999',
          role: 'Admin',
          department: 'IT',
          hireDate: '2023-05-01',
          status: 'inactive'
        });
        result.current.setFieldError('firstName', 'Error');
      });

      act(() => {
        result.current.resetForm();
      });

      expect(result.current.formData).toEqual({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        role: '',
        department: '',
        hireDate: '',
        status: 'active'
      });
      expect(result.current.submitting).toBe(false);
      expect(result.current.success).toBe(false);
      expect(result.current.error).toBe(null);
      expect(result.current.fieldErrors).toEqual({});
    });
  });

  describe('setFieldError', () => {
    it('should set a single field error', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFieldError('email', 'Email is required');
      });

      expect(result.current.fieldErrors).toEqual({
        email: 'Email is required'
      });
    });

    it('should add field error without removing existing errors', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFieldError('email', 'Email is required');
      });

      act(() => {
        result.current.setFieldError('phone', 'Phone is required');
      });

      expect(result.current.fieldErrors).toEqual({
        email: 'Email is required',
        phone: 'Phone is required'
      });
    });

    it('should update existing field error', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFieldError('email', 'Email is required');
      });

      act(() => {
        result.current.setFieldError('email', 'Invalid email format');
      });

      expect(result.current.fieldErrors).toEqual({
        email: 'Invalid email format'
      });
    });

    it('should handle multiple field errors', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFieldError('firstName', 'First name is required');
        result.current.setFieldError('lastName', 'Last name is required');
        result.current.setFieldError('email', 'Email is required');
      });

      expect(result.current.fieldErrors).toEqual({
        firstName: 'First name is required',
        lastName: 'Last name is required',
        email: 'Email is required'
      });
    });
  });

  describe('setFormData', () => {
    it('should update form data', () => {
      const { result } = renderHook(() => useStaffForm());

      const newFormData = {
        firstName: 'Alice',
        lastName: 'Johnson',
        email: 'alice@example.com',
        phone: '5555555555',
        role: 'Technician',
        department: 'Lab',
        hireDate: '2023-03-15',
        status: 'active'
      };

      act(() => {
        result.current.setFormData(newFormData);
      });

      expect(result.current.formData).toEqual(newFormData);
    });

    it('should allow partial updates to form data', () => {
      const { result } = renderHook(() => useStaffForm());

      act(() => {
        result.current.setFormData({
          ...result.current.formData,
          firstName: 'Bob'
        });
      });

      expect(result.current.formData.firstName).toBe('Bob');
      expect(result.current.formData.lastName).toBe('');
    });
  });

  describe('Return object', () => {
    it('should return all required properties', () => {
      const { result } = renderHook(() => useStaffForm());

      expect(result.current).toHaveProperty('formData');
      expect(result.current).toHaveProperty('setFormData');
      expect(result.current).toHaveProperty('submitting');
      expect(result.current).toHaveProperty('success');
      expect(result.current).toHaveProperty('error');
      expect(result.current).toHaveProperty('fieldErrors');
      expect(result.current).toHaveProperty('handleSubmit');
      expect(result.current).toHaveProperty('resetForm');
      expect(result.current).toHaveProperty('setFieldError');
    });

    it('should return functions that are stable across renders', () => {
      const { result, rerender } = renderHook(() => useStaffForm());

      const initialHandleSubmit = result.current.handleSubmit;
      const initialResetForm = result.current.resetForm;
      const initialSetFieldError = result.current.setFieldError;

      rerender();

      expect(result.current.handleSubmit).toBe(initialHandleSubmit);
      expect(result.current.resetForm).toBe(initialResetForm);
      expect(result.current.setFieldError).toBe(initialSetFieldError);
    });
  });

  describe('Integration scenarios', () => {
    it('should handle complete create workflow', async () => {
      mockedValidateStaffForm.mockReturnValue({});
      mockedCreateStaff.mockResolvedValue({ data: { id: 1 } });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      act(() => {
        result.current.setFormData({
          firstName: 'New',
          lastName: 'Staff',
          email: 'new.staff@example.com',
          phone: '1231231234',
          role: 'Nurse',
          department: 'Pediatrics',
          hireDate: '2023-07-01',
          status: 'active'
        });
      });

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.success).toBe(true);
      expect(result.current.error).toBe(null);
      expect(result.current.submitting).toBe(false);
    });

    it('should handle complete update workflow', async () => {
      const mockStaffData = {
        firstName: 'Existing',
        lastName: 'Staff',
        email: 'existing@example.com',
        phone: '9999999999',
        role: 'Doctor',
        department: 'Surgery',
        hireDate: '2022-01-01',
        status: 'active'
      };

      mockedGetStaff.mockResolvedValue({ data: mockStaffData });
      mockedValidateStaffForm.mockReturnValue({});
      mockedUpdateStaff.mockResolvedValue({ data: mockStaffData });

      const { result } = renderHook(() => useStaffForm(123, 1));

      await waitFor(() => {
        expect(result.current.formData).toEqual(mockStaffData);
      });

      act(() => {
        result.current.setFormData({
          ...result.current.formData,
          department: 'Cardiology'
        });
      });

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.success).toBe(true);
      expect(mockedUpdateStaff).toHaveBeenCalledWith(123, expect.objectContaining({
        department: 'Cardiology'
      }));
    });

    it('should handle validation error, correction, and successful resubmission', async () => {
      mockedValidateStaffForm
        .mockReturnValueOnce({ email: 'Invalid email' })
        .mockReturnValueOnce({});
      mockedCreateStaff.mockResolvedValue({ data: {} });

      const { result } = renderHook(() => useStaffForm(undefined, 1));

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.fieldErrors).toEqual({ email: 'Invalid email' });
      expect(result.current.success).toBe(false);

      await act(async () => {
        await result.current.handleSubmit();
      });

      expect(result.current.fieldErrors).toEqual({});
      expect(result.current.success).toBe(true);
    });
  });
});