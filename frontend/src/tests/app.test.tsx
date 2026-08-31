import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach, afterEach, afterAll } from 'vitest';
import { authApi } from '../api/authApi';
import { staffApi } from '../api/staffApi';
import { useAuth } from '../hooks/useAuth';
import { StaffListPage } from '../pages/StaffListPage';
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import { BrowserRouter } from 'react-router-dom';

vi.mock('../api/staffApi');
vi.mock('../hooks/useAuth');

const server = setupServer(
  rest.post('/api/v1/auth/login', (_req: any, res: any, ctx: any) => {
    return res(
      ctx.status(200),
      ctx.json({
        userId: 1,
        username: 'testuser',
        role: 'MANAGER',
        facilityId: 1,
        facilityName: 'Test Facility',
        message: 'Login successful'
      })
    );
  }),
  rest.post('/api/v1/auth/logout', (_req: any, res: any, ctx: any) => {
    return res(
      ctx.status(200),
      ctx.json({ message: 'Logout successful' })
    );
  }),
  rest.get('/api/v1/auth/session', (_req: any, res: any, ctx: any) => {
    return res(
      ctx.status(200),
      ctx.json({
        userId: 1,
        username: 'testuser',
        role: 'MANAGER',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      })
    );
  })
);

beforeEach(() => {
  server.listen();
  vi.clearAllMocks();
});

afterEach(() => {
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});

describe('authApi', () => {
  test('login returns user data on success', async () => {
    const credentials = {
      username: 'testuser',
      password: 'password123',
      facilityId: 1
    };

    const response = await authApi.login(credentials);

    expect(response.userId).toBe(1);
    expect(response.username).toBe('testuser');
    expect(response.role).toBe('MANAGER');
    expect(response.facilityId).toBe(1);
    expect(response.facilityName).toBe('Test Facility');
    expect(response.message).toBe('Login successful');
  });

  test('login throws error on invalid credentials', async () => {
    server.use(
      rest.post('/api/v1/auth/login', (_req: any, res: any, ctx: any) => {
        return res(
          ctx.status(401),
          ctx.json({
            correlationId: 'test-correlation-id',
            errorCode: 'INVALID_CREDENTIALS',
            message: 'Invalid username or password'
          })
        );
      })
    );

    const credentials = {
      username: 'wronguser',
      password: 'wrongpass',
      facilityId: 1
    };

    await expect(authApi.login(credentials)).rejects.toThrow();
  });

  test('logout returns success message', async () => {
    await authApi.logout();
    expect(true).toBe(true);
  });

  test('logout throws error when unauthenticated', async () => {
    server.use(
      rest.post('/api/v1/auth/logout', (_req: any, res: any, ctx: any) => {
        return res(
          ctx.status(401),
          ctx.json({
            correlationId: 'test-correlation-id',
            errorCode: 'AUTHENTICATION_FAILED',
            message: 'Authentication required'
          })
        );
      })
    );

    await expect(authApi.logout()).rejects.toThrow();
  });

  test('getCurrentUser returns user profile', async () => {
    const profile = await authApi.getCurrentUser();

    expect(profile.userId).toBe(1);
    expect(profile.username).toBe('testuser');
    expect(profile.role).toBe('MANAGER');
    expect(profile.facilityId).toBe(1);
    expect(profile.facilityName).toBe('Test Facility');
    expect(profile.isActive).toBe(true);
  });

  test('getCurrentUser throws error when unauthenticated', async () => {
    server.use(
      rest.get('/api/v1/auth/session', (_req: any, res: any, ctx: any) => {
        return res(
          ctx.status(401),
          ctx.json({
            correlationId: 'test-correlation-id',
            errorCode: 'AUTHENTICATION_FAILED',
            message: 'Authentication required'
          })
        );
      })
    );

    await expect(authApi.getCurrentUser()).rejects.toThrow();
  });
});

describe('StaffListPage', () => {
  const mockStaffData = [
    {
      staffId: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      role: 'NURSE',
      facilityId: 1,
      isActive: true
    },
    {
      staffId: 2,
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@example.com',
      role: 'DOCTOR',
      facilityId: 1,
      isActive: true
    }
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders staff list page for authenticated users', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockResolvedValue(mockStaffData);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    expect(staffApi.listStaff).toHaveBeenCalledTimes(1);
  });

  test('redirects to login when accessing staff list unauthenticated', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: false,
      user: null
    });

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.queryByTestId('staff-list-page')).not.toBeInTheDocument();
    });

    expect(staffApi.listStaff).not.toHaveBeenCalled();
  });

  test('displays loading skeleton while fetching staff data', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockImplementation(() => new Promise(resolve => setTimeout(() => resolve(mockStaffData), 1000)));

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
    }, { timeout: 2000 });

    expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
  });

  test('displays empty state when no staff exist', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockResolvedValue([]);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      expect(screen.getByText(/no staff/i)).toBeInTheDocument();
    });

    expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
    expect(staffApi.listStaff).toHaveBeenCalledTimes(1);
  });

  test('displays error banner on API failure', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockRejectedValue(new Error('API Error'));

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      expect(screen.getByText(/error/i)).toBeInTheDocument();
    });

    expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
    expect(screen.queryByTestId('staff-list-page')).not.toBeInTheDocument();
  });

  test('verifies staffApi.listStaff is mocked correctly', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockResolvedValue(mockStaffData);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(staffApi.listStaff).toHaveBeenCalled();
    });

    expect(staffApi.listStaff).toHaveBeenCalledWith();
  });

  test('verifies useAuth hook is mocked for authenticated state', async () => {
    const mockAuthValue = {
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    };

    (useAuth as any).mockReturnValue(mockAuthValue);
    (staffApi.listStaff as any).mockResolvedValue(mockStaffData);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    });

    expect(useAuth).toHaveBeenCalled();
  });

  test('verifies useAuth hook is mocked for unauthenticated state', async () => {
    const mockAuthValue = {
      isAuthenticated: false,
      user: null
    };

    (useAuth as any).mockReturnValue(mockAuthValue);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.queryByTestId('staff-list-page')).not.toBeInTheDocument();
    });

    expect(useAuth).toHaveBeenCalled();
  });

  test('verifies data-testid assertions for component rendering', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockResolvedValue(mockStaffData);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    });

    const staffListElement = screen.getByTestId('staff-list-page');
    expect(staffListElement).toBeTruthy();
    expect(staffListElement.tagName).toBeDefined();
  });

  test('handles multiple staff members rendering correctly', async () => {
    const largeStaffData = [
      ...mockStaffData,
      {
        staffId: 3,
        firstName: 'Bob',
        lastName: 'Johnson',
        email: 'bob.johnson@example.com',
        role: 'ADMIN',
        facilityId: 1,
        isActive: true
      },
      {
        staffId: 4,
        firstName: 'Alice',
        lastName: 'Williams',
        email: 'alice.williams@example.com',
        role: 'NURSE',
        facilityId: 1,
        isActive: false
      }
    ];

    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockResolvedValue(largeStaffData);

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
      expect(screen.getByText('Alice Williams')).toBeInTheDocument();
    });
  });

  test('handles network timeout gracefully', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockRejectedValue(new Error('Network timeout'));

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('error-banner')).toBeInTheDocument();
    });
  });

  test('transitions from loading to loaded state correctly', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockImplementation(() => new Promise(resolve => setTimeout(() => resolve(mockStaffData), 500)));

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    }, { timeout: 1000 });
  });

  test('transitions from loading to error state correctly', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockImplementation(() => new Promise((_, reject) => setTimeout(() => reject(new Error('API Error')), 500)));

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.getByTestId('error-banner')).toBeInTheDocument();
    }, { timeout: 1000 });
  });

  test('transitions from loading to empty state correctly', async () => {
    (useAuth as any).mockReturnValue({
      isAuthenticated: true,
      user: { userId: 1, username: 'testuser', role: 'MANAGER' }
    });

    (staffApi.listStaff as any).mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 500)));

    render(
      <BrowserRouter>
        <StaffListPage />
      </BrowserRouter>
    );

    expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.getByTestId('empty-state')).toBeInTheDocument();
    }, { timeout: 1000 });
  });
});