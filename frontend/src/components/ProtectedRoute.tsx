import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ProtectedRoute from './ProtectedRoute';
import { useAuth } from '../hooks/useAuth';
import { logger } from '../utils/logger';

vi.mock('../hooks/useAuth');
vi.mock('../utils/logger', () => ({
  logger: {
    warn: vi.fn(),
    info: vi.fn(),
  },
}));

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading state while authentication is being verified', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: true,
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should redirect to /login if user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route
            path="/protected"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should log warning when unauthenticated user attempts to access protected route', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/staff']}>
        <Routes>
          <Route
            path="/staff"
            element={
              <ProtectedRoute>
                <div>Staff Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(logger.warn).toHaveBeenCalledWith(
      'Unauthenticated access attempt to protected route',
      {
        event: 'protected_route_redirect',
        target_route: '/staff',
        authenticated: false,
      }
    );
  });

  it('should render children prop if user is authenticated', () => {
    const mockUser = {
      username: 'testuser',
      id: '123',
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('should log info when authenticated user accesses protected route', () => {
    const mockUser = {
      username: 'testuser',
      id: '123',
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/staff']}>
        <ProtectedRoute>
          <div>Staff Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(logger.info).toHaveBeenCalledWith(
      'Authenticated access to protected route',
      {
        event: 'protected_route_access',
        target_route: '/staff',
        authenticated: true,
        username: 'testuser',
      }
    );
  });

  it('should pass location state when redirecting to login', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: false,
    });

    const { container } = render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route
            path="/protected"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
          <Route
            path="/login"
            element={
              <div data-testid="login-page">Login Page</div>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });

  it('should protect /staff route without modifications', () => {
    const mockUser = {
      username: 'staffuser',
      id: '456',
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/staff']}>
        <Routes>
          <Route
            path="/staff"
            element={
              <ProtectedRoute>
                <div>Staff Dashboard</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Staff Dashboard')).toBeInTheDocument();
  });

  it('should render loading state with correct styles', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: true,
    });

    const { container } = render(
      <MemoryRouter initialEntries={['/protected']}>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    const loadingDiv = container.querySelector('div');
    expect(loadingDiv).toHaveStyle({
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
    });
  });

  it('should handle multiple children elements when authenticated', () => {
    const mockUser = {
      username: 'testuser',
      id: '123',
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <ProtectedRoute>
          <div>First Child</div>
          <div>Second Child</div>
          <div>Third Child</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByText('First Child')).toBeInTheDocument();
    expect(screen.getByText('Second Child')).toBeInTheDocument();
    expect(screen.getByText('Third Child')).toBeInTheDocument();
  });

  it('should use replace prop on Navigate to prevent back navigation to protected route', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route
            path="/protected"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });
});