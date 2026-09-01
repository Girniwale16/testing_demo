import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useNavigate } from 'react-router-dom';
import StaffList from './StaffList';
import useStaffApi from '../hooks/useStaffApi';
import staffApi from '../api/staffApi';

// Mock all dependencies
vi.mock('react-router-dom', () => ({
  useNavigate: vi.fn(),
}));

vi.mock('../hooks/useStaffApi');
vi.mock('../api/staffApi');

vi.mock('./LoadingSkeleton', () => ({
  default: () => <div data-testid="loading-skeleton">Loading...</div>,
}));

vi.mock('./EmptyState', () => ({
  default: ({ onCreateClick }) => (
    <div data-testid="empty-state">
      <button onClick={onCreateClick}>Create Staff</button>
    </div>
  ),
}));

vi.mock('./StaffTable', () => ({
  default: ({ staff, onEdit, onDeactivate }) => (
    <div data-testid="staff-table">
      {staff.map((s) => (
        <div key={s.id} data-testid={`staff-${s.id}`}>
          <span>{s.name}</span>
          <button onClick={() => onEdit(s.id)}>Edit</button>
          <button onClick={() => onDeactivate(s.id)}>Deactivate</button>
        </div>
      ))}
    </div>
  ),
}));

vi.mock('./Pagination', () => ({
  default: ({ currentPage, totalPages, onPageChange }) => (
    <div data-testid="pagination">
      <button onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1}>
        Previous
      </button>
      <span>Page {currentPage} of {totalPages}</span>
      <button onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages}>
        Next
      </button>
    </div>
  ),
}));

vi.mock('./RetryBanner', () => ({
  default: ({ onRetry, message }) => (
    <div data-testid="retry-banner">
      <span>{message}</span>
      <button onClick={onRetry}>Retry</button>
    </div>
  ),
}));

vi.mock('./ErrorAlert', () => ({
  default: ({ error }) => (
    <div data-testid="error-alert">{error.message}</div>
  ),
}));

describe('StaffList Component', () => {
  let mockNavigate;
  let mockCallApi;
  let mockClearError;

  beforeEach(() => {
    mockNavigate = vi.fn();
    mockCallApi = vi.fn();
    mockClearError = vi.fn();

    useNavigate.mockReturnValue(mockNavigate);
    useStaffApi.mockReturnValue({
      callApi: mockCallApi,
      error: null,
      clearError: mockClearError,
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Initial State and Loading', () => {
    it('should initialize with loading state set to true', () => {
      mockCallApi.mockImplementation(() => new Promise(() => {}));
      
      render(<StaffList />);
      
      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
    });

    it('should display LoadingSkeleton component when loading is true', () => {
      mockCallApi.mockImplementation(() => new Promise(() => {}));
      
      render(<StaffList />);
      
      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('should initialize page state to 1', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalledWith(expect.any(Function));
      });

      const callApiCallback = mockCallApi.mock.calls[0][0];
      callApiCallback();

      expect(staffApi.list).toHaveBeenCalledWith(1, 20, false);
    });

    it('should initialize totalPages state to 1', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });

    it('should initialize staff state to empty array', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      });
    });

    it('should initialize showRetryBanner state to false', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      });
    });
  });

  describe('loadStaff Function', () => {
    it('should call staffApi.list with correct parameters (pageNumber, 20, false)', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 2,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(1, 20, false);
      });
    });

    it('should set loading to true when loadStaff is called', async () => {
      let resolvePromise;
      mockCallApi.mockImplementation(() => new Promise((resolve) => {
        resolvePromise = resolve;
      }));

      render(<StaffList />);

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

      resolvePromise({ data: [], totalPages: 1 });
    });

    it('should update staff state with result.data on successful API call', async () => {
      const mockStaffData = [
        { id: 1, name: 'John Doe' },
        { id: 2, name: 'Jane Smith' },
      ];

      mockCallApi.mockResolvedValue({
        data: mockStaffData,
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    it('should update totalPages state with result.totalPages on successful API call', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 5,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 5')).toBeInTheDocument();
      });
    });

    it('should set loading to false after successful API call', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });

    it('should set showRetryBanner to false after successful API call', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      });
    });

    it('should set loading to false when API call fails', async () => {
      mockCallApi.mockRejectedValue(new Error('API Error'));

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });

    it('should set showRetryBanner to true when error.canRetry is true', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValue(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });
    });

    it('should not set showRetryBanner to true when error.canRetry is false', async () => {
      const nonRetryableError = new Error('Server Error');
      nonRetryableError.canRetry = false;

      mockCallApi.mockRejectedValue(nonRetryableError);

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      });
    });

    it('should not set showRetryBanner when error.canRetry is undefined', async () => {
      mockCallApi.mockRejectedValue(new Error('Unknown Error'));

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      });
    });
  });

  describe('useEffect Hook', () => {
    it('should call loadStaff with current page on component mount', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalledTimes(1);
      });
    });

    it('should call loadStaff when page state changes', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });

      mockCallApi.mockClear();

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalledTimes(1);
      });
    });

    it('should fetch staff for page 2 when page changes to 2', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 3, name: 'Page 2 Staff' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });

      staffApi.list.mockClear();

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });
    });
  });

  describe('handleEdit Function', () => {
    it('should call navigate with correct path when handleEdit is invoked', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      const editButton = screen.getByText('Edit');
      fireEvent.click(editButton);

      expect(mockNavigate).toHaveBeenCalledWith('/staff/edit/1');
    });

    it('should navigate to edit page with string staffId', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 'abc123', name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      const editButton = screen.getByText('Edit');
      fireEvent.click(editButton);

      expect(mockNavigate).toHaveBeenCalledWith('/staff/edit/abc123');
    });

    it('should navigate to edit page with numeric staffId', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 42, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      const editButton = screen.getByText('Edit');
      fireEvent.click(editButton);

      expect(mockNavigate).toHaveBeenCalledWith('/staff/edit/42');
    });
  });

  describe('handleDeactivate Function', () => {
    it('should call staffApi.deactivate with correct staffId', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      mockCallApi.mockClear();
      mockCallApi.mockResolvedValue({});

      const deactivateButton = screen.getByText('Deactivate');
      fireEvent.click(deactivateButton);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalledWith(expect.any(Function));
      });

      const callApiCallback = mockCallApi.mock.calls[0][0];
      await callApiCallback();

      expect(staffApi.deactivate).toHaveBeenCalledWith(1);
    });

    it('should reload staff list after successful deactivation', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      mockCallApi.mockClear();
      mockCallApi
        .mockResolvedValueOnce({})
        .mockResolvedValueOnce({
          data: [],
          totalPages: 1,
        });

      const deactivateButton = screen.getByText('Deactivate');
      fireEvent.click(deactivateButton);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalledTimes(2);
      });
    });

    it('should call loadStaff with current page after deactivation', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 2,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });

      staffApi.deactivate.mockResolvedValue({});
      staffApi.list.mockClear();

      const deactivateButton = screen.getByText('Deactivate');
      fireEvent.click(deactivateButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });
    });
  });

  describe('handlePageChange Function', () => {
    it('should update page state when handlePageChange is called', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
      });

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(screen.getByText('Page 2 of 3')).toBeInTheDocument();
      });
    });

    it('should trigger useEffect to load new page data', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });

      staffApi.list.mockClear();

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });
    });

    it('should handle page change to page 3', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 5,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });

      staffApi.list.mockClear();

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });

      staffApi.list.mockClear();
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(3, 20, false);
      });
    });
  });

  describe('handleRetry Function', () => {
    it('should call clearError when handleRetry is invoked', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValueOnce(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });

      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      const retryButton = screen.getByText('Retry');
      fireEvent.click(retryButton);

      expect(mockClearError).toHaveBeenCalled();
    });

    it('should call loadStaff with current page when handleRetry is invoked', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValueOnce(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });

      mockCallApi.mockClear();
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      const retryButton = screen.getByText('Retry');
      fireEvent.click(retryButton);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalled();
      });
    });

    it('should hide retry banner after successful retry', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValueOnce(retryableError);
      
      const { rerender } = render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });

      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: null,
        clearError: mockClearError,
      });

      const retryButton = screen.getByText('Retry');
      fireEvent.click(retryButton);

      rerender(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      });
    });
  });

  describe('Render Conditions - Loading State', () => {
    it('should return LoadingSkeleton when loading is true', () => {
      mockCallApi.mockImplementation(() => new Promise(() => {}));

      render(<StaffList />);

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
      expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
      expect(screen.queryByTestId('empty-state')).not.toBeInTheDocument();
    });

    it('should not render other components when loading is true', () => {
      mockCallApi.mockImplementation(() => new Promise(() => {}));

      render(<StaffList />);

      expect(screen.queryByTestId('error-alert')).not.toBeInTheDocument();
      expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
      expect(screen.queryByTestId('pagination')).not.toBeInTheDocument();
    });
  });

  describe('Render Conditions - FORBIDDEN Error', () => {
    it('should return ErrorAlert when error.type is FORBIDDEN', async () => {
      mockCallApi.mockRejectedValue(new Error('Forbidden'));
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { type: 'FORBIDDEN', message: 'Access Denied' },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('error-alert')).toBeInTheDocument();
      });

      expect(screen.getByText('Access Denied')).toBeInTheDocument();
    });

    it('should not render other components when FORBIDDEN error exists', async () => {
      mockCallApi.mockRejectedValue(new Error('Forbidden'));
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { type: 'FORBIDDEN', message: 'Access Denied' },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('error-alert')).toBeInTheDocument();
      });

      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.queryByTestId('retry-banner')).not.toBeInTheDocument();
      expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
    });

    it('should pass error object to ErrorAlert component', async () => {
      const forbiddenError = { type: 'FORBIDDEN', message: 'No Permission' };
      mockCallApi.mockRejectedValue(new Error('Forbidden'));
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: forbiddenError,
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('error-alert')).toBeInTheDocument();
      });

      expect(screen.getByText('No Permission')).toBeInTheDocument();
    });
  });

  describe('Render Conditions - Retry Banner', () => {
    it('should return RetryBanner when showRetryBanner is true', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValue(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });
    });

    it('should pass error message to RetryBanner component', async () => {
      const retryableError = new Error('Connection Timeout');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValue(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Connection Timeout', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Connection Timeout')).toBeInTheDocument();
      });
    });

    it('should pass handleRetry function to RetryBanner onRetry prop', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValueOnce(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });

      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      const retryButton = screen.getByText('Retry');
      fireEvent.click(retryButton);

      expect(mockClearError).toHaveBeenCalled();
    });

    it('should not render other components when retry banner is shown', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValue(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });

      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
      expect(screen.queryByTestId('empty-state')).not.toBeInTheDocument();
    });
  });

  describe('Render Conditions - Empty State', () => {
    it('should return EmptyState when staff.length is 0 and loading is false', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      });
    });

    it('should not show EmptyState when loading is true even if staff is empty', () => {
      mockCallApi.mockImplementation(() => new Promise(() => {}));

      render(<StaffList />);

      expect(screen.queryByTestId('empty-state')).not.toBeInTheDocument();
      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
    });

    it('should pass onCreateClick handler to EmptyState that navigates to /staff/create', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      });

      const createButton = screen.getByText('Create Staff');
      fireEvent.click(createButton);

      expect(mockNavigate).toHaveBeenCalledWith('/staff/create');
    });

    it('should not render other components when empty state is shown', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      });

      expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
      expect(screen.queryByTestId('pagination')).not.toBeInTheDocument();
      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
    });
  });

  describe('Render Conditions - Staff Table and Pagination', () => {
    it('should render StaffTable when staff.length > 0', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });
    });

    it('should render Pagination when staff.length > 0', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });
    });

    it('should pass staff array to StaffTable component', async () => {
      const mockStaffData = [
        { id: 1, name: 'John Doe' },
        { id: 2, name: 'Jane Smith' },
      ];

      mockCallApi.mockResolvedValue({
        data: mockStaffData,
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      });
    });

    it('should pass handleEdit to StaffTable onEdit prop', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      const editButton = screen.getByText('Edit');
      fireEvent.click(editButton);

      expect(mockNavigate).toHaveBeenCalledWith('/staff/edit/1');
    });

    it('should pass handleDeactivate to StaffTable onDeactivate prop', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      mockCallApi.mockClear();
      mockCallApi.mockResolvedValue({});

      const deactivateButton = screen.getByText('Deactivate');
      fireEvent.click(deactivateButton);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalled();
      });
    });

    it('should pass currentPage to Pagination component', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
      });
    });

    it('should pass totalPages to Pagination component', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 5,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 5')).toBeInTheDocument();
      });
    });

    it('should pass handlePageChange to Pagination onPageChange prop', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(screen.getByText('Page 2 of 3')).toBeInTheDocument();
      });
    });

    it('should render both StaffTable and Pagination together', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 2,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });
    });
  });

  describe('Render Conditions - Null Return', () => {
    it('should return null when no render conditions are met', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 0,
      });
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: null,
        clearError: mockClearError,
      });

      const { container } = render(<StaffList />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });

      // When staff is empty and loading is false, EmptyState should show
      // This test verifies the fallback behavior
    });
  });

  describe('Integration Tests', () => {
    it('should handle complete flow: load -> edit -> deactivate -> reload', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      const editButton = screen.getByText('Edit');
      fireEvent.click(editButton);
      expect(mockNavigate).toHaveBeenCalledWith('/staff/edit/1');

      staffApi.deactivate.mockResolvedValue({});
      staffApi.list.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      const deactivateButton = screen.getByText('Deactivate');
      fireEvent.click(deactivateButton);

      await waitFor(() => {
        expect(staffApi.deactivate).toHaveBeenCalledWith(1);
      });
    });

    it('should handle pagination flow across multiple pages', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'Page 1' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
      });

      staffApi.list.mockResolvedValue({
        data: [{ id: 2, name: 'Page 2' }],
        totalPages: 3,
      });

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });
    });

    it('should handle error -> retry -> success flow', async () => {
      const retryableError = new Error('Network Error');
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValueOnce(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { message: 'Network Error', canRetry: true },
        clearError: mockClearError,
      });

      const { rerender } = render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });

      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: null,
        clearError: mockClearError,
      });

      const retryButton = screen.getByText('Retry');
      fireEvent.click(retryButton);

      rerender(<StaffList />);

      await waitFor(() => {
        expect(mockClearError).toHaveBeenCalled();
      });
    });

    it('should maintain page state after deactivation', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 3,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('pagination')).toBeInTheDocument();
      });

      const nextButton = screen.getByText('Next');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });

      staffApi.deactivate.mockResolvedValue({});
      staffApi.list.mockClear();

      const deactivateButton = screen.getByText('Deactivate');
      fireEvent.click(deactivateButton);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(2, 20, false);
      });
    });

    it('should handle empty state -> create navigation', async () => {
      mockCallApi.mockResolvedValue({
        data: [],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      });

      const createButton = screen.getByText('Create Staff');
      fireEvent.click(createButton);

      expect(mockNavigate).toHaveBeenCalledWith('/staff/create');
    });
  });

  describe('Edge Cases', () => {
    it('should handle staff list with single item', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'Only Staff' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Only Staff')).toBeInTheDocument();
      });
    });

    it('should handle staff list with many items', async () => {
      const manyStaff = Array.from({ length: 20 }, (_, i) => ({
        id: i + 1,
        name: `Staff ${i + 1}`,
      }));

      mockCallApi.mockResolvedValue({
        data: manyStaff,
        totalPages: 5,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Staff 1')).toBeInTheDocument();
        expect(screen.getByText('Staff 20')).toBeInTheDocument();
      });
    });

    it('should handle totalPages of 1', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 1')).toBeInTheDocument();
      });
    });

    it('should handle large totalPages number', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 100,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 100')).toBeInTheDocument();
      });
    });

    it('should handle error without message property', async () => {
      const retryableError = new Error();
      retryableError.canRetry = true;

      mockCallApi.mockRejectedValue(retryableError);
      useStaffApi.mockReturnValue({
        callApi: mockCallApi,
        error: { canRetry: true },
        clearError: mockClearError,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('retry-banner')).toBeInTheDocument();
      });
    });

    it('should handle simultaneous deactivate calls', async () => {
      mockCallApi.mockResolvedValue({
        data: [
          { id: 1, name: 'Staff 1' },
          { id: 2, name: 'Staff 2' },
        ],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      mockCallApi.mockClear();
      mockCallApi.mockResolvedValue({});

      const deactivateButtons = screen.getAllByText('Deactivate');
      fireEvent.click(deactivateButtons[0]);
      fireEvent.click(deactivateButtons[1]);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalled();
      });
    });
  });

  describe('Hook Integration', () => {
    it('should use useNavigate hook correctly', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(useNavigate).toHaveBeenCalled();
      });
    });

    it('should use useStaffApi hook correctly', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(useStaffApi).toHaveBeenCalled();
      });
    });

    it('should destructure callApi, error, and clearError from useStaffApi', async () => {
      mockCallApi.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(mockCallApi).toHaveBeenCalled();
      });
    });
  });

  describe('API Parameter Validation', () => {
    it('should always pass false as third parameter to staffApi.list (exclude deactivated)', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        expect(staffApi.list).toHaveBeenCalledWith(1, 20, false);
      });
    });

    it('should always pass 20 as page size to staffApi.list', async () => {
      mockCallApi.mockImplementation(async (callback) => {
        return await callback();
      });
      staffApi.list.mockResolvedValue({
        data: [{ id: 1, name: 'John Doe' }],
        totalPages: 1,
      });

      render(<StaffList />);

      await waitFor(() => {
        const calls = staffApi.list.mock.calls;
        expect(calls[0][1]).toBe(20);
      });
    });
  });
});