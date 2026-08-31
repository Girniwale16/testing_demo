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
vi.mock('./LoadingSkeleton', () => ({
  default: () => <div data-testid="loading-skeleton">Loading...</div>,
}));

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Loading State', () => {
    it('should display LoadingSkeleton when loading is true', () => {
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

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('should not log any events when in loading state', () => {
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

      expect(logger.warn).not.toHaveBeenCalled();
      expect(logger.info).not.toHaveBeenCalled();
    });
  });

  describe('Unauthenticated Access', () => {
    it('should redirect to login when user is not authenticated', () => {
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
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Login Page')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('should pass location state with return URL when redirecting to login', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        loading: false,
      });

      let capturedState: any = null;

      render(
        <MemoryRouter initialEntries={['/protected/resource']}>
          <Routes>
            <Route
              path="/protected/resource"
              element={
                <ProtectedRoute>
                  <div>Protected Content</div>
                </ProtectedRoute>
              }
            />
            <Route
              path="/login"
              element={
                <div>
                  {(() => {
                    const location = window.location;
                    capturedState = (window.history.state as any)?.usr;
                    return 'Login Page';
                  })()}
                </div>
              }
            />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });

    it('should log warning with correct event data when unauthenticated user attempts access', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        loading: false,
      });

      render(
        <MemoryRouter initialEntries={['/protected/dashboard']}>
          <Routes>
            <Route
              path="/protected/dashboard"
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

      expect(logger.warn).toHaveBeenCalledWith(
        'Unauthenticated access attempt to protected route',
        {
          event: 'protected_route_redirect',
          target_route: '/protected/dashboard',
          authenticated: false,
        }
      );
      expect(logger.warn).toHaveBeenCalledTimes(1);
    });

    it('should use replace navigation to prevent back button issues', () => {
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

  describe('Authenticated Access', () => {
    it('should render children when user is authenticated', () => {
      mockUseAuth.mockReturnValue({
        user: { username: 'testuser', id: '123' },
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
      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
    });

    it('should log info with correct event data when authenticated user accesses route', () => {
      const mockUser = { username: 'testuser', id: '123' };
      mockUseAuth.mockReturnValue({
        user: mockUser,
        loading: false,
      });

      render(
        <MemoryRouter initialEntries={['/protected/profile']}>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(logger.info).toHaveBeenCalledWith(
        'Authenticated access to protected route',
        {
          event: 'protected_route_access',
          target_route: '/protected/profile',
          authenticated: true,
          username: 'testuser',
        }
      );
      expect(logger.info).toHaveBeenCalledTimes(1);
    });

    it('should not redirect when user is authenticated', () => {
      mockUseAuth.mockReturnValue({
        user: { username: 'testuser', id: '123' },
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

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
    });

    it('should render complex children components when authenticated', () => {
      mockUseAuth.mockReturnValue({
        user: { username: 'testuser', id: '123' },
        loading: false,
      });

      const ComplexChild = () => (
        <div>
          <h1>Staff List Page</h1>
          <ul>
            <li>Staff 1</li>
            <li>Staff 2</li>
          </ul>
        </div>
      );

      render(
        <MemoryRouter initialEntries={['/staff']}>
          <ProtectedRoute>
            <ComplexChild />
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(screen.getByText('Staff List Page')).toBeInTheDocument();
      expect(screen.getByText('Staff 1')).toBeInTheDocument();
      expect(screen.getByText('Staff 2')).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle multiple children elements', () => {
      mockUseAuth.mockReturnValue({
        user: { username: 'testuser', id: '123' },
        loading: false,
      });

      render(
        <MemoryRouter initialEntries={['/protected']}>
          <ProtectedRoute>
            <div>First Child</div>
            <div>Second Child</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(screen.getByText('First Child')).toBeInTheDocument();
      expect(screen.getByText('Second Child')).toBeInTheDocument();
    });

    it('should handle root path correctly', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        loading: false,
      });

      render(
        <MemoryRouter initialEntries={['/']}>
          <Routes>
            <Route
              path="/"
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

      expect(logger.warn).toHaveBeenCalledWith(
        'Unauthenticated access attempt to protected route',
        expect.objectContaining({
          target_route: '/',
        })
      );
    });

    it('should handle nested routes correctly', () => {
      mockUseAuth.mockReturnValue({
        user: { username: 'testuser', id: '123' },
        loading: false,
      });

      render(
        <MemoryRouter initialEntries={['/protected/nested/deep']}>
          <ProtectedRoute>
            <div>Deeply Nested Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(logger.info).toHaveBeenCalledWith(
        'Authenticated access to protected route',
        expect.objectContaining({
          target_route: '/protected/nested/deep',
        })
      );
    });

    it('should transition from loading to authenticated state correctly', () => {
      const { rerender } = render(
        <MemoryRouter initialEntries={['/protected']}>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      mockUseAuth.mockReturnValue({
        user: null,
        loading: true,
      });

      rerender(
        <MemoryRouter initialEntries={['/protected']}>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

      mockUseAuth.mockReturnValue({
        user: { username: 'testuser', id: '123' },
        loading: false,
      });

      rerender(
        <MemoryRouter initialEntries={['/protected']}>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });

    it('should transition from loading to unauthenticated state correctly', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        loading: true,
      });

      const { rerender } = render(
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

      expect(screen.getByTestId('loading-skeleton')).toBeInTheDocument();

      mockUseAuth.mockReturnValue({
        user: null,
        loading: false,
      });

      rerender(
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

      expect(screen.queryByTestId('loading-skeleton')).not.toBeInTheDocument();
      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  describe('Integration with StaffListPage', () => {
    it('should properly wrap StaffListPage and enforce authentication', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        loading: false,
      });

      const StaffListPage = () => <div>Staff List Page</div>;

      render(
        <MemoryRouter initialEntries={['/staff']}>
          <Routes>
            <Route
              path="/staff"
              element={
                <ProtectedRoute>
                  <StaffListPage />
                </ProtectedRoute>
              }
            />
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Login Page')).toBeInTheDocument();
      expect(screen.queryByText('Staff List Page')).not.toBeInTheDocument();
    });

    it('should allow authenticated access to StaffListPage', () => {
      mockUseAuth.mockReturnValue({
        user: { username: 'admin', id: '456' },
        loading: false,
      });

      const StaffListPage = () => <div>Staff List Page</div>;

      render(
        <MemoryRouter initialEntries={['/staff']}>
          <ProtectedRoute>
            <StaffListPage />
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(screen.getByText('Staff List Page')).toBeInTheDocument();
    });
  });
});