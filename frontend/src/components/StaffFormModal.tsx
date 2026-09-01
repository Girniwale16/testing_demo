import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import StaffFormModal from '../StaffFormModal';
import { useStaffForm } from '../../hooks/useStaffForm';

jest.mock('../../hooks/useStaffForm');
jest.mock('../ErrorBanner', () => ({
  __esModule: true,
  default: ({ message, fieldErrors, onDismiss }: any) => (
    <div data-testid="error-banner">
      <div>{message}</div>
      {Object.keys(fieldErrors).map(key => (
        <div key={key}>{fieldErrors[key]}</div>
      ))}
      <button onClick={onDismiss}>Dismiss</button>
    </div>
  )
}));
jest.mock('../Toast', () => ({
  __esModule: true,
  default: ({ message, type, onDismiss }: any) => (
    <div data-testid="toast" data-type={type}>
      <div>{message}</div>
      <button onClick={onDismiss}>Dismiss Toast</button>
    </div>
  )
}));
jest.mock('../../utils/logger', () => ({
  __esModule: true,
  default: {
    info: jest.fn(),
    error: jest.fn(),
    warn: jest.fn()
  }
}));

const mockUseStaffForm = useStaffForm as jest.MockedFunction<typeof useStaffForm>;

describe('StaffFormModal', () => {
  const mockOnClose = jest.fn();
  const mockOnSuccess = jest.fn();
  const mockResetForm = jest.fn();
  const mockHandleSubmit = jest.fn();
  const mockSetFormData = jest.fn();

  const defaultFormData = {
    firstName: '',
    lastName: '',
    contact: '',
    role: '',
    employmentStatus: '',
    startDate: '',
    endDate: ''
  };

  const defaultHookReturn = {
    formData: defaultFormData,
    setFormData: mockSetFormData,
    submitting: false,
    success: false,
    error: null,
    fieldErrors: {},
    handleSubmit: mockHandleSubmit,
    resetForm: mockResetForm
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseStaffForm.mockReturnValue(defaultHookReturn);
  });

  describe('Modal Visibility', () => {
    it('should not render when isOpen is false', () => {
      const { container } = render(
        <StaffFormModal
          isOpen={false}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );
      expect(container.firstChild).toBeNull();
    });

    it('should render when isOpen is true', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
  });

  describe('Modal Title and Mode', () => {
    it('should display "Add Staff" title in create mode', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );
      expect(screen.getByText('Add Staff')).toBeInTheDocument();
    });

    it('should display "Edit Staff" title in edit mode', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="edit"
          staffId={123}
        />
      );
      expect(screen.getByText('Edit Staff')).toBeInTheDocument();
    });
  });

  describe('Focus Management', () => {
    it('should focus first input field when modal opens', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );
      const firstNameInput = screen.getByLabelText('First Name *');
      expect(firstNameInput).toHaveFocus();
    });

    it('should trap focus within modal on Tab key', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const submitButton = screen.getByRole('button', { name: /Add Staff/i });
      submitButton.focus();

      fireEvent.keyDown(document, { key: 'Tab' });

      await waitFor(() => {
        const firstNameInput = screen.getByLabelText('First Name *');
        expect(firstNameInput).toHaveFocus();
      });
    });

    it('should trap focus backwards on Shift+Tab', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const firstNameInput = screen.getByLabelText('First Name *');
      firstNameInput.focus();

      fireEvent.keyDown(document, { key: 'Tab', shiftKey: true });

      await waitFor(() => {
        const submitButton = screen.getByRole('button', { name: /Add Staff/i });
        expect(submitButton).toHaveFocus();
      });
    });
  });

  describe('Form Field Rendering', () => {
    it('should render all required form fields', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByLabelText('First Name *')).toBeInTheDocument();
      expect(screen.getByLabelText('Last Name *')).toBeInTheDocument();
      expect(screen.getByLabelText('Contact *')).toBeInTheDocument();
      expect(screen.getByLabelText('Role *')).toBeInTheDocument();
      expect(screen.getByLabelText('Employment Status *')).toBeInTheDocument();
      expect(screen.getByLabelText('Start Date')).toBeInTheDocument();
      expect(screen.getByLabelText('End Date')).toBeInTheDocument();
    });

    it('should render role dropdown with correct options', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const roleSelect = screen.getByLabelText('Role *') as HTMLSelectElement;
      expect(roleSelect).toBeInTheDocument();
      expect(within(roleSelect).getByText('Nurse')).toBeInTheDocument();
      expect(within(roleSelect).getByText('Doctor')).toBeInTheDocument();
      expect(within(roleSelect).getByText('Administrator')).toBeInTheDocument();
      expect(within(roleSelect).getByText('Support Staff')).toBeInTheDocument();
    });

    it('should render employment status dropdown with correct options', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const statusSelect = screen.getByLabelText('Employment Status *') as HTMLSelectElement;
      expect(statusSelect).toBeInTheDocument();
      expect(within(statusSelect).getByText('ACTIVE')).toBeInTheDocument();
      expect(within(statusSelect).getByText('PENDING')).toBeInTheDocument();
      expect(within(statusSelect).getByText('ON_LEAVE')).toBeInTheDocument();
      expect(within(statusSelect).getByText('TERMINATED')).toBeInTheDocument();
    });
  });

  describe('Form Field Changes', () => {
    it('should call setFormData when firstName changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const firstNameInput = screen.getByLabelText('First Name *');
      await userEvent.type(firstNameInput, 'John');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should call setFormData when lastName changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const lastNameInput = screen.getByLabelText('Last Name *');
      await userEvent.type(lastNameInput, 'Doe');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should call setFormData when contact changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const contactInput = screen.getByLabelText('Contact *');
      await userEvent.type(contactInput, '555-1234');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should call setFormData when role changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const roleSelect = screen.getByLabelText('Role *');
      await userEvent.selectOptions(roleSelect, 'Nurse');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should call setFormData when employment status changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const statusSelect = screen.getByLabelText('Employment Status *');
      await userEvent.selectOptions(statusSelect, 'ACTIVE');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should call setFormData when startDate changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const startDateInput = screen.getByLabelText('Start Date');
      await userEvent.type(startDateInput, '2024-01-01');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should call setFormData when endDate changes', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const endDateInput = screen.getByLabelText('End Date');
      await userEvent.type(endDateInput, '2024-12-31');

      expect(mockSetFormData).toHaveBeenCalled();
    });

    it('should update formData with correct field and value', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const firstNameInput = screen.getByLabelText('First Name *');
      fireEvent.change(firstNameInput, { target: { value: 'Jane' } });

      expect(mockSetFormData).toHaveBeenCalledWith({
        ...defaultFormData,
        firstName: 'Jane'
      });
    });
  });

  describe('Form Submission', () => {
    it('should call handleSubmit when form is submitted', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const form = screen.getByRole('dialog').querySelector('form');
      fireEvent.submit(form!);

      await waitFor(() => {
        expect(mockHandleSubmit).toHaveBeenCalled();
      });
    });

    it('should prevent default form submission', async () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const form = screen.getByRole('dialog').querySelector('form');
      const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
      const preventDefaultSpy = jest.spyOn(submitEvent, 'preventDefault');
      
      form!.dispatchEvent(submitEvent);

      expect(preventDefaultSpy).toHaveBeenCalled();
    });

    it('should disable submit button when submitting', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        submitting: true
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const submitButton = screen.getByRole('button', { name: /Saving.../i });
      expect(submitButton).toBeDisabled();
    });

    it('should show "Saving..." text when submitting in create mode', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        submitting: true
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByText('Saving...')).toBeInTheDocument();
    });

    it('should show "Add Staff" text when not submitting in create mode', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByRole('button', { name: /Add Staff/i })).toBeInTheDocument();
    });

    it('should show "Save Changes" text when not submitting in edit mode', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="edit"
          staffId={123}
        />
      );

      expect(screen.getByRole('button', { name: /Save Changes/i })).toBeInTheDocument();
    });
  });

  describe('Success Handling', () => {
    it('should call onSuccess callback when success is true', async () => {
      const { rerender } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
          onSuccess={mockOnSuccess}
        />
      );

      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        success: true,
        formData: {
          ...defaultFormData,
          firstName: 'John',
          lastName: 'Doe'
        }
      });

      rerender(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(mockOnSuccess).toHaveBeenCalled();
      });
    });

    it('should display success toast in create mode', async () => {
      const { rerender } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        success: true
      });

      rerender(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Staff member added successfully')).toBeInTheDocument();
      });
    });

    it('should display success toast in edit mode', async () => {
      const { rerender } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="edit"
          staffId={123}
        />
      );

      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        success: true
      });

      rerender(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="edit"
          staffId={123}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Staff member updated successfully')).toBeInTheDocument();
      });
    });

    it('should close modal after 2 seconds on success', async () => {
      jest.useFakeTimers();

      const { rerender } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        success: true
      });

      rerender(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      jest.advanceTimersByTime(2000);

      await waitFor(() => {
        expect(mockOnClose).toHaveBeenCalled();
        expect(mockResetForm).toHaveBeenCalled();
      });

      jest.useRealTimers();
    });

    it('should clear timeout on unmount', async () => {
      jest.useFakeTimers();

      const { rerender, unmount } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        success: true
      });

      rerender(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      unmount();

      jest.advanceTimersByTime(2000);

      expect(mockOnClose).not.toHaveBeenCalled();

      jest.useRealTimers();
    });
  });

  describe('Error Handling', () => {
    it('should display ErrorBanner when error exists', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        error: 'Failed to save staff member'
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      expect(screen.getByText('Failed to save staff member')).toBeInTheDocument();
    });

    it('should display field errors in ErrorBanner', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        error: 'Validation failed',
        fieldErrors: {
          firstName: 'First name is required',
          contact: 'Invalid contact format'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByText('First name is required')).toBeInTheDocument();
      expect(screen.getByText('Invalid contact format')).toBeInTheDocument();
    });

    it('should display inline field error for firstName', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        fieldErrors: {
          firstName: 'First name is required'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const errorSpan = screen.getByRole('alert', { name: /First name is required/i });
      expect(errorSpan).toBeInTheDocument();
      expect(errorSpan).toHaveAttribute('id', 'firstName-error');
    });

    it('should set aria-invalid on firstName input when error exists', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        fieldErrors: {
          firstName: 'First name is required'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const firstNameInput = screen.getByLabelText('First Name *');
      expect(firstNameInput).toHaveAttribute('aria-invalid', 'true');
      expect(firstNameInput).toHaveAttribute('aria-describedby', 'firstName-error');
    });

    it('should display inline field error for lastName', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        fieldErrors: {
          lastName: 'Last name is required'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByText('Last name is required')).toBeInTheDocument();
    });

    it('should display inline field error for contact', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        fieldErrors: {
          contact: 'Invalid contact format'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByText('Invalid contact format')).toBeInTheDocument();
    });

    it('should display inline field error for startDate', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        fieldErrors: {
          startDate: 'Invalid date format'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByText('Invalid date format')).toBeInTheDocument();
    });

    it('should display inline field error for endDate', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        fieldErrors: {
          endDate: 'End date must be after start date'
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByText('End date must be after start date')).toBeInTheDocument();
    });
  });

  describe('Modal Close Behavior', () => {
    it('should call onClose when close button is clicked', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const closeButton = screen.getByLabelText('Close modal');
      fireEvent.click(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
      expect(mockResetForm).toHaveBeenCalled();
    });

    it('should call onClose when cancel button is clicked', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      fireEvent.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalled();
      expect(mockResetForm).toHaveBeenCalled();
    });

    it('should call onClose when backdrop is clicked', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const backdrop = screen.getByRole('dialog').parentElement;
      fireEvent.click(backdrop!);

      expect(mockOnClose).toHaveBeenCalled();
      expect(mockResetForm).toHaveBeenCalled();
    });

    it('should not close when modal content is clicked', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const modalContent = screen.getByRole('dialog');
      fireEvent.click(modalContent);

      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it('should reset form when closing', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const closeButton = screen.getByLabelText('Close modal');
      fireEvent.click(closeButton);

      expect(mockResetForm).toHaveBeenCalled();
    });

    it('should disable cancel button when submitting', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        submitting: true
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      expect(cancelButton).toBeDisabled();
    });
  });

  describe('Accessibility', () => {
    it('should have role="dialog" on modal', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('should have aria-modal="true" on modal', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByRole('dialog')).toHaveAttribute('aria-modal', 'true');
    });

    it('should have aria-labelledby pointing to modal title', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-labelledby', 'modal-title');
      expect(screen.getByText('Add Staff')).toHaveAttribute('id', 'modal-title');
    });

    it('should have aria-required="true" on required fields', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByLabelText('First Name *')).toHaveAttribute('aria-required', 'true');
      expect(screen.getByLabelText('Last Name *')).toHaveAttribute('aria-required', 'true');
      expect(screen.getByLabelText('Contact *')).toHaveAttribute('aria-required', 'true');
      expect(screen.getByLabelText('Role *')).toHaveAttribute('aria-required', 'true');
      expect(screen.getByLabelText('Employment Status *')).toHaveAttribute('aria-required', 'true');
    });

    it('should have aria-live="polite" on form actions', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      const formActions = screen.getByRole('button', { name: /Cancel/i }).parentElement;
      expect(formActions).toHaveAttribute('aria-live', 'polite');
    });

    it('should have proper label associations for all inputs', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByLabelText('First Name *')).toHaveAttribute('id', 'firstName');
      expect(screen.getByLabelText('Last Name *')).toHaveAttribute('id', 'lastName');
      expect(screen.getByLabelText('Contact *')).toHaveAttribute('id', 'contact');
      expect(screen.getByLabelText('Role *')).toHaveAttribute('id', 'role');
      expect(screen.getByLabelText('Employment Status *')).toHaveAttribute('id', 'employmentStatus');
      expect(screen.getByLabelText('Start Date')).toHaveAttribute('id', 'startDate');
      expect(screen.getByLabelText('End Date')).toHaveAttribute('id', 'endDate');
    });
  });

  describe('Form Data Population', () => {
    it('should populate form fields with existing data in edit mode', () => {
      const existingData = {
        firstName: 'John',
        lastName: 'Doe',
        contact: '555-1234',
        role: 'Nurse',
        employmentStatus: 'ACTIVE',
        startDate: '2024-01-01',
        endDate: '2024-12-31'
      };

      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        formData: existingData
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="edit"
          staffId={123}
        />
      );

      expect(screen.getByLabelText('First Name *')).toHaveValue('John');
      expect(screen.getByLabelText('Last Name *')).toHaveValue('Doe');
      expect(screen.getByLabelText('Contact *')).toHaveValue('555-1234');
      expect(screen.getByLabelText('Role *')).toHaveValue('Nurse');
      expect(screen.getByLabelText('Employment Status *')).toHaveValue('ACTIVE');
      expect(screen.getByLabelText('Start Date')).toHaveValue('2024-01-01');
      expect(screen.getByLabelText('End Date')).toHaveValue('2024-12-31');
    });

    it('should handle empty formData values gracefully', () => {
      mockUseStaffForm.mockReturnValue({
        ...defaultHookReturn,
        formData: {
          firstName: '',
          lastName: '',
          contact: '',
          role: '',
          employmentStatus: '',
          startDate: '',
          endDate: ''
        }
      });

      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(screen.getByLabelText('First Name *')).toHaveValue('');
      expect(screen.getByLabelText('Last Name *')).toHaveValue('');
      expect(screen.getByLabelText('Contact *')).toHaveValue('');
    });
  });

  describe('useStaffForm Hook Integration', () => {
    it('should call useStaffForm with staffId and facilityId', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={42}
          mode="edit"
          staffId={123}
        />
      );

      expect(mockUseStaffForm).toHaveBeenCalledWith(123, 42);
    });

    it('should call useStaffForm with undefined staffId in create mode', () => {
      render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={42}
          mode="create"
        />
      );

      expect(mockUseStaffForm).toHaveBeenCalledWith(undefined, 42);
    });
  });

  describe('Event Listener Cleanup', () => {
    it('should remove keydown listener when modal closes', () => {
      const removeEventListenerSpy = jest.spyOn(document, 'removeEventListener');

      const { rerender } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      rerender(
        <StaffFormModal
          isOpen={false}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      expect(removeEventListenerSpy).toHaveBeenCalledWith('keydown', expect.any(Function));

      removeEventListenerSpy.mockRestore();
    });

    it('should remove keydown listener on unmount', () => {
      const removeEventListenerSpy = jest.spyOn(document, 'removeEventListener');

      const { unmount } = render(
        <StaffFormModal
          isOpen={true}
          onClose={mockOnClose}
          facilityId={1}
          mode="create"
        />
      );

      unmount();

      expect(removeEventListenerSpy).toHaveBeenCalledWith('keydown', expect.any(Function));

      removeEventListenerSpy.mockRestore();
    });
  });
});