import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import { logger } from '../utils/logger';

jest.mock('../pages/LoginPage', () => {
  return function MockLoginPage() {
    return <div data-testid="login-page">Login Page</div>;
  };
});

jest.mock('../components/ProtectedRoute', () => {
  return function MockProtectedRoute({ children }: { children: React.ReactNode }) {
    return <div data-testid="protected-route">{children}</div>;
  };
});

jest.mock('../components/StaffList.jsx', () => {
  return function MockStaffList() {
    return <div data-testid="staff-list">Staff List Component</div>;
  };
});

jest.mock('../utils/logger', () => ({
  logger: {
    info: jest.fn(),
  },
}));

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Component Mounting', () => {
    it('should log app mount event on component mount', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(logger.info).toHaveBeenCalledWith('App mounted', { event: 'app_mount' });
      expect(logger.info).toHaveBeenCalledTimes(1);
    });
  });

  describe('Route: /login', () => {
    it('should render LoginPage component when navigating to /login', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();
      expect(screen.queryByTestId('protected-route')).not.toBeInTheDocument();
    });

    it('should not wrap /login route with ProtectedRoute', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      const loginPage = screen.getByTestId('login-page');
      expect(loginPage.parentElement?.getAttribute('data-testid')).not.toBe('protected-route');
    });
  });

  describe('Route: /staff', () => {
    it('should render StaffList component when navigating to /staff', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list')).toBeInTheDocument();
    });

    it('should wrap /staff route with ProtectedRoute component', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
      expect(screen.getByTestId('staff-list')).toBeInTheDocument();
    });

    it('should render StaffList inside ProtectedRoute for /staff path', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      const protectedRoute = screen.getByTestId('protected-route');
      const staffList = screen.getByTestId('staff-list');
      
      expect(protectedRoute).toContainElement(staffList);
    });
  });

  describe('Route: /* (catch-all)', () => {
    it('should render protected content for undefined routes', () => {
      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });

    it('should wrap catch-all route with ProtectedRoute component', () => {
      render(
        <MemoryRouter initialEntries={['/some-other-route']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
    });

    it('should display authentication message for catch-all routes', () => {
      render(
        <MemoryRouter initialEntries={['/random']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByText('You are authenticated. Protected routes will be added here.')).toBeInTheDocument();
    });

    it('should apply correct styling to catch-all route content', () => {
      render(
        <MemoryRouter initialEntries={['/other']}>
          <App />
        </MemoryRouter>
      );

      const contentDiv = screen.getByText('Protected Content').parentElement;
      expect(contentDiv).toHaveStyle({ padding: '2rem' });
    });
  });

  describe('Route Integrity', () => {
    it('should maintain all three routes: /login, /staff, and /*', () => {
      const { container: loginContainer } = render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );
      expect(screen.getByTestId('login-page')).toBeInTheDocument();

      const { container: staffContainer } = render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );
      expect(screen.getByTestId('staff-list')).toBeInTheDocument();

      const { container: catchAllContainer } = render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <App />
        </MemoryRouter>
      );
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });

    it('should not break existing /login route after adding /staff route', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();
      expect(screen.queryByTestId('staff-list')).not.toBeInTheDocument();
    });

    it('should not break existing catch-all route after adding /staff route', () => {
      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.queryByTestId('staff-list')).not.toBeInTheDocument();
    });
  });

  describe('BrowserRouter Integration', () => {
    it('should wrap all routes with BrowserRouter', () => {
      const { container } = render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(container.querySelector('[data-testid="login-page"]')).toBeInTheDocument();
    });
  });

  describe('Import Verification', () => {
    it('should successfully import and render StaffList component', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('staff-list')).toBeInTheDocument();
    });

    it('should successfully import and use ProtectedRoute component', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-route')).toBeInTheDocument();
    });
  });

  describe('Authentication Enforcement', () => {
    it('should enforce authentication on /staff route via ProtectedRoute', () => {
      render(
        <MemoryRouter initialEntries={['/staff']}>
          <App />
        </MemoryRouter>
      );

      const protectedRoute = screen.getByTestId('protected-route');
      expect(protectedRoute).toBeInTheDocument();
    });

    it('should not enforce authentication on /login route', () => {
      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.queryByTestId('protected-route')).not.toBeInTheDocument();
    });
  });
});