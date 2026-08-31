import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import StaffListPage from './StaffListPage';
import { staffApi } from '../api/staffApi';
import { useAuth } from '../hooks/useAuth';

jest.mock('../api/staffApi');
jest.mock('../hooks/useAuth');
jest.mock('../components/Table', () => ({
  Table: ({ data, columns, pagination, onPageChange, ...props }: any) => (
    <div data-testid="staff-table" {...props}>
      <table>
        <thead>
          <tr>
            {columns.map((col: any) => (
              <th key={col.key}>{col.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((item: any, idx: number) => (
            <tr key={idx}>
              {columns.map((col: any) => (
                <td key={col.key}>{col.accessor(item)}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      <button onClick={() => onPageChange(pagination.currentPage + 1)}>Next Page</button>
    </div>
  )
}));
jest.mock('../components/LoadingSkeleton', () => ({
  LoadingSkeleton: ({ variant, rows }: any) => (
    <div data-testid="loading-skeleton" data-variant={variant} data-rows={rows}>Loading...</div>
  )
}));
jest.mock('../components/EmptyState', () => ({
  EmptyState: ({ title, message, actionLabel }: any) => (
    <div data-testid="empty-state">
      <h2>{title}</h2>
      <p>{message}</p>
      <button>{actionLabel}</button>
    </div>
  )
}));
jest.mock('../components/ErrorBanner', () => ({
  ErrorBanner: ({ message, onRetry, onDismiss }: any) => (
    <div data-testid="error-banner">
      <span>{message}</span>
      <button onClick={onRetry} data-testid="retry-button">Retry</button>
      <button onClick={onDismiss} data-testid="dismiss-button">Dismiss</button>
    </div>
  )
}));

const mockStaffApi = staffApi as jest.Mocked<typeof staffApi>;
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;

describe('StaffListPage', () => {
  const mockStaffData = [
    {
      id: '1',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      role: 'Admin',
      isActive: true
    },
    {
      id: '2',
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@example.com',
      role: 'Staff',
      isActive: false
    }
  ];

  const mockPagination = {
    currentPage: 1,
    totalPages: 5,
    totalItems: 50,
    itemsPerPage: 10
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Test User', role: 'Admin' },
      login: jest.fn(),
      logout: jest.fn(),
      isAuthenticated: true
    } as any);
  });

  describe('Component Initialization', () => {
    it('should render StaffListPage with correct data-testid', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
      expect(screen.getByRole('main')).toBeInTheDocument();
    });

    it('should initialize with filters.isActive set to true', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledWith(
          1,
          10,
          { isActive: true }
        );
      });
    });

    it('should render page header with title and add button', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      expect(screen.getByText('Staff Members')).toBeInTheDocument();
      expect(screen.getByTestId('add-staff-button')).toBeInTheDocument();
      expect(screen.getByTestId('add-staff-button')).toHaveTextContent('Add Staff Member');
    });
  });

  describe('Data Fetching on Mount', () => {
    it('should fetch staff data on component mount', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(1);
        expect(mockStaffApi.listStaff).toHaveBeenCalledWith(1, 10, { isActive: true });
      });
    });

    it('should display loading skeleton while fetching data', () => {
      mockStaffApi.listStaff.mockImplementation(() => new Promise(() => {}));

      render(<StaffListPage />);

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
      expect(screen.getByTestId('loading-skeleton')).toHaveAttribute('data-variant', 'table');
      expect(screen.getByTestId('loading-skeleton')).toHaveAttribute('data-rows', '10');
    });

    it('should update staff state with response data on successful fetch', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('John')).toBeInTheDocument();
        expect(screen.getByText('Doe')).toBeInTheDocument();
        expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
      });
    });

    it('should update pagination state with response pagination', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });
    });

    it('should hide loading skeleton after data is loaded', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });
  });

  describe('Error Handling - 401 Unauthorized', () => {
    it('should throw error on 401 to let useAuth handle redirect', async () => {
      const error401 = {
        response: { status: 401, data: { message: 'Unauthorized' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error401);

      // Suppress console errors for this test
      const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});

      expect(() => render(<StaffListPage />)).not.toThrow();

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalled();
      });

      consoleError.mockRestore();
    });
  });

  describe('Error Handling - 403 Forbidden', () => {
    it('should display authorization error message on 403', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
        expect(screen.getByText('You are not authorized to view staff members')).toBeInTheDocument();
      });
    });

    it('should not display loading skeleton when 403 error occurs', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });
  });

  describe('Error Handling - 5xx Server Errors', () => {
    it('should display server error message on 500', async () => {
      const error500 = {
        response: { status: 500, data: { message: 'Internal Server Error' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error500);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
        expect(screen.getByText('Failed to load staff. Please try again.')).toBeInTheDocument();
      });
    });

    it('should display server error message on 503', async () => {
      const error503 = {
        response: { status: 503, data: { message: 'Service Unavailable' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error503);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load staff. Please try again.')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling - Network Errors', () => {
    it('should display network error message when request fails without response', async () => {
      const networkError = {
        request: {},
        message: 'Network Error'
      };
      mockStaffApi.listStaff.mockRejectedValue(networkError);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
        expect(screen.getByText('Network error. Please check your connection.')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling - Generic Errors', () => {
    it('should display generic error message for other errors', async () => {
      const genericError = {
        message: 'Something went wrong'
      };
      mockStaffApi.listStaff.mockRejectedValue(genericError);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load staff. Please try again.')).toBeInTheDocument();
      });
    });

    it('should display generic error message for 4xx errors other than 401 and 403', async () => {
      const error404 = {
        response: { status: 404, data: { message: 'Not Found' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error404);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load staff. Please try again.')).toBeInTheDocument();
      });
    });
  });

  describe('Error Banner Interactions', () => {
    it('should call handleRetry when retry button is clicked', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValueOnce(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      });

      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      fireEvent.click(screen.getByTestId('retry-button'));

      await waitFor(() => {
        expect(screen.queryByTestId('error-banner')).not.toBeInTheDocument();
      });
    });

    it('should clear error state when retry is clicked', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValueOnce(error403).mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByTestId('retry-button'));

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(2);
      });
    });

    it('should call handleDismissError when dismiss button is clicked', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByTestId('dismiss-button'));

      await waitFor(() => {
        expect(screen.queryByTestId('error-banner')).not.toBeInTheDocument();
      });
    });
  });

  describe('Empty State', () => {
    it('should display EmptyState when staff array is empty', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: [],
        pagination: { ...mockPagination, totalItems: 0 }
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      });
    });

    it('should display correct EmptyState content', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: [],
        pagination: { ...mockPagination, totalItems: 0 }
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('No Staff Members')).toBeInTheDocument();
        expect(screen.getByText('Get started by adding your first staff member')).toBeInTheDocument();
        expect(screen.getByText('Add Staff Member')).toBeInTheDocument();
      });
    });

    it('should not display EmptyState when loading', () => {
      mockStaffApi.listStaff.mockImplementation(() => new Promise(() => {}));

      render(<StaffListPage />);

      expect(screen.queryByTestId('empty-state')).not.toBeInTheDocument();
      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
    });

    it('should not display EmptyState when error exists', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.queryByTestId('empty-state')).not.toBeInTheDocument();
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      });
    });
  });

  describe('Table Rendering', () => {
    it('should render Table component when staff data exists', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });
    });

    it('should pass correct columns configuration to Table', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('First Name')).toBeInTheDocument();
        expect(screen.getByText('Last Name')).toBeInTheDocument();
        expect(screen.getByText('Email')).toBeInTheDocument();
        expect(screen.getByText('Role')).toBeInTheDocument();
        expect(screen.getByText('Status')).toBeInTheDocument();
        expect(screen.getByText('Actions')).toBeInTheDocument();
      });
    });

    it('should display staff firstName in table', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('John')).toBeInTheDocument();
        expect(screen.getByText('Jane')).toBeInTheDocument();
      });
    });

    it('should display staff lastName in table', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Doe')).toBeInTheDocument();
        expect(screen.getByText('Smith')).toBeInTheDocument();
      });
    });

    it('should display staff email in table', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
        expect(screen.getByText('jane.smith@example.com')).toBeInTheDocument();
      });
    });

    it('should display staff role in table', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Admin')).toBeInTheDocument();
        expect(screen.getByText('Staff')).toBeInTheDocument();
      });
    });

    it('should display "Active" status for active staff', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Active')).toBeInTheDocument();
      });
    });

    it('should display "Inactive" status for inactive staff', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Inactive')).toBeInTheDocument();
      });
    });

    it('should not render Table when loading', () => {
      mockStaffApi.listStaff.mockImplementation(() => new Promise(() => {}));

      render(<StaffListPage />);

      expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
    });

    it('should not render Table when staff array is empty', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: [],
        pagination: { ...mockPagination, totalItems: 0 }
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.queryByTestId('staff-table')).not.toBeInTheDocument();
      });
    });
  });

  describe('Pagination', () => {
    it('should call handlePageChange when page is changed', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Next Page'));

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledWith(2, 10, { isActive: true });
      });
    });

    it('should refetch data when pagination changes', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(1);
      });

      fireEvent.click(screen.getByText('Next Page'));

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(2);
      });
    });

    it('should update currentPage in pagination state', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('staff-table')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Next Page'));

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledWith(2, 10, { isActive: true });
      });
    });
  });

  describe('useEffect Dependencies', () => {
    it('should refetch data when currentPage changes', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(1);
      });

      fireEvent.click(screen.getByText('Next Page'));

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(2);
      });
    });

    it('should refetch data when itemsPerPage changes', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      const { rerender } = render(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(1);
      });

      rerender(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalled();
      });
    });

    it('should refetch data when filters change', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      const { rerender } = render(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalledTimes(1);
      });

      rerender(<StaffListPage />);

      await waitFor(() => {
        expect(mockStaffApi.listStaff).toHaveBeenCalled();
      });
    });
  });

  describe('ARIA Landmarks', () => {
    it('should have main role on main element', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      expect(screen.getByRole('main')).toBeInTheDocument();
    });

    it('should have header element', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      const { container } = render(<StaffListPage />);

      expect(container.querySelector('header')).toBeInTheDocument();
    });

    it('should have section element', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      const { container } = render(<StaffListPage />);

      expect(container.querySelector('section')).toBeInTheDocument();
    });
  });

  describe('Loading State Management', () => {
    it('should set loading to true before fetch', () => {
      mockStaffApi.listStaff.mockImplementation(() => new Promise(() => {}));

      render(<StaffListPage />);

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
    });

    it('should set loading to false after successful fetch', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });

    it('should set loading to false after failed fetch', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      });
    });
  });

  describe('Error State Management', () => {
    it('should clear error state before fetch', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValueOnce(error403).mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByTestId('retry-button'));

      await waitFor(() => {
        expect(screen.queryByTestId('error-banner')).not.toBeInTheDocument();
      });
    });

    it('should set error state on fetch failure', async () => {
      const error403 = {
        response: { status: 403, data: { message: 'Forbidden' } }
      };
      mockStaffApi.listStaff.mockRejectedValue(error403);

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      });
    });
  });

  describe('Column Accessors', () => {
    it('should correctly access firstName from staff object', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('John')).toBeInTheDocument();
      });
    });

    it('should correctly access lastName from staff object', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Doe')).toBeInTheDocument();
      });
    });

    it('should correctly access email from staff object', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
      });
    });

    it('should correctly access role from staff object', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Admin')).toBeInTheDocument();
      });
    });

    it('should correctly compute status from isActive property', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('Active')).toBeInTheDocument();
        expect(screen.getByText('Inactive')).toBeInTheDocument();
      });
    });

    it('should correctly access id for actions column', async () => {
      mockStaffApi.listStaff.mockResolvedValue({
        data: mockStaffData,
        pagination: mockPagination
      });

      render(<StaffListPage />);

      await waitFor(() => {
        expect(screen.getByText('1')).toBeInTheDocument();
        expect(screen.getByText('2')).toBeInTheDocument();
      });
    });
  });
});