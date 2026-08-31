import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from './App';
import { logger } from './utils/logger';

jest.mock('./pages/LoginPage', () => {
  return function MockLoginPage() {
    return <div data-testid="login-page">Login Page</div>;
  };
});

jest.mock('./pages/StaffListPage', () => {
  return function MockStaffListPage() {
    return <div data-testid="staff-list-page">Staff List Page</div>;
  };
});

jest.mock('./components/ProtectedRoute', () => {
  return function MockProtectedRoute({ children }: { children: React.ReactNode }) {
    return <div data-testid="protected-route">{children}</div>;
  };
});

jest.mock('./utils/logger', () => ({
  logger: {
    info: jest.fn(),
  },
}));

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Route Configuration', () => {
    it('should render LoginPage at /login route without ProtectedRoute wrapper', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();
      expect(screen.queryByTestId('protected-route')).not.toBeInTheDocument();
    });

    it('should render StaffListPage at /staff route', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    });

    it('should wrap StaffListPage with ProtectedRoute component at /staff route', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      const protectedRoute = screen.getByTestId('protected-route');
      expect(protectedRoute).toBeInTheDocument();
      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    });

    it('should render protected content for catch-all route', () => {
      render(
        <MemoryRouter initialEntries={['/any-other-route']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.getByText('You are authenticated. Protected routes will be added here.')).toBeInTheDocument();
    });

    it('should wrap catch-all route with ProtectedRoute component', () => {
      render(
        <MemoryRouter initialEntries={['/any-other-route']}>
          <App />
        </MemoryRouter>
      );

      const protectedRoute = screen.getByTestId('protected-route');
      expect(protectedRoute).toBeInTheDocument();
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });
  });

  describe('Route Hierarchy and Positioning', () => {
    it('should have /staff route positioned before catch-all route', () => {
      const { container } = render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('should maintain existing route structure with LoginPage accessible', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();
    });

    it('should not break existing routes when staff route is added', () => {
      const { rerender } = render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();

      rerender(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
    });
  });

  describe('Authentication Context and Provider', () => {
    it('should ensure BrowserRouter wraps all routes including staff route', () => {
      const { container } = render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(container.querySelector('[data-testid="staff-list-page"]')).toBeInTheDocument();
    });

    it('should ensure authentication check is triggered for staff route via ProtectedRoute', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
      const protectedRoute = screen.getByTestId('protected-route');
      expect(protectedRoute).toContainElement(screen.getByTestId('staff-list-page'));
    });

    it('should ensure authentication check is triggered for catch-all route via ProtectedRoute', () => {
      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
    });
  });

  describe('App Lifecycle and Logging', () => {
    it('should log app mount event on component mount', async () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(logger.info).toHaveBeenCalledWith('App mounted', { event: 'app_mount' });
      });
    });

    it('should log app mount event only once', async () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(logger.info).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('Navigation Between Routes', () => {
    it('should navigate from login to staff route', () => {
      const { rerender } = render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();

      rerender(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
      expect(screen.queryByTestId('login-page')).not.toBeInTheDocument();
    });

    it('should navigate from staff route to catch-all route', () => {
      const { rerender } = render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();

      rerender(
        <MemoryRouter initialEntries={['/other']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.queryByTestId('staff-list-page')).not.toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle root path with catch-all route', () => {
      render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
    });

    it('should handle undefined routes with catch-all route', () => {
      render(
        <MemoryRouter initialEntries={['/undefined-route']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
    });

    it('should render correct content for /staff path exactly', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list-page')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('should not match /staff/list as separate route', () => {
      render(
        <MemoryRouter initialEntries={['/staff/list']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.queryByTestId('staff-list-page')).not.toBeInTheDocument();
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });
  });
});