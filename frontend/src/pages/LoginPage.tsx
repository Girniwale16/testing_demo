import { render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../LoginPage';
import { useAuth } from '../../hooks/useAuth';
import { logger } from '../../utils/logger';

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});

vi.mock('../../hooks/useAuth');
vi.mock('../../utils/logger');
vi.mock('../../components/LoginForm', () => ({
  default: ({ onSubmit }: { onSubmit: (username: string, password: string) => void }) => (
    <div data-testid="login-form">
      <button onClick={() => onSubmit('testuser', 'testpass')}>Submit</button>
    </div>
  ),
}));
vi.mock('../../components/ErrorBanner', () => ({
  default: ({ message, onDismiss }: { message: string; onDismiss: () => void }) => (
    <div data-testid="error-banner">
      <span>{message}</span>
      <button onClick={onDismiss}>Dismiss</button>
    </div>
  ),
}));

describe('LoginPage', () => {
  const mockNavigate = vi.fn();
  const mockLogin = vi.fn();
  const mockClearError = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    const { useNavigate } = require('react-router-dom');
    useNavigate.mockReturnValue(mockNavigate);
    
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      error: null,
      login: mockLogin,
      clearError: mockClearError,
      logout: vi.fn(),
      loading: false,
    });

    vi.mocked(logger.info).mockImplementation(() => {});
    vi.mocked(logger.error).mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Component Rendering', () => {
    it('should render LoginPage with correct layout structure', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(screen.getByRole('heading', { name: /login/i })).toBeInTheDocument();
      expect(screen.getByTestId('login-form')).toBeInTheDocument();
    });

    it('should render LoginForm component', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(screen.getByTestId('login-form')).toBeInTheDocument();
    });

    it('should apply correct styling for page layout', () => {
      const { container } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const outerDiv = container.firstChild as HTMLElement;
      expect(outerDiv).toHaveStyle({
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f5f5f5',
        padding: '1rem',
      });
    });

    it('should apply correct styling for inner container', () => {
      const { container } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const innerDiv = container.querySelector('div > div') as HTMLElement;
      expect(innerDiv).toHaveStyle({
        width: '100%',
        maxWidth: '400px',
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '8px',
      });
    });

    it('should render heading with correct styling', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const heading = screen.getByRole('heading', { name: /login/i });
      expect(heading).toHaveStyle({
        marginBottom: '1.5rem',
        textAlign: 'center',
      });
    });
  });

  describe('Error Handling and Display', () => {
    it('should not render ErrorBanner when there is no error', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(screen.queryByTestId('error-banner')).not.toBeInTheDocument();
    });

    it('should render ErrorBanner when error exists', () => {
      vi.mocked(useAuth).mockReturnValue({
        user: null,
        error: 'Invalid credentials',
        login: mockLogin,
        clearError: mockClearError,
        logout: vi.fn(),
        loading: false,
      });

      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(screen.getByTestId('error-banner')).toBeInTheDocument();
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });

    it('should pass clearError function to ErrorBanner onDismiss', () => {
      vi.mocked(useAuth).mockReturnValue({
        user: null,
        error: 'Test error',
        login: mockLogin,
        clearError: mockClearError,
        logout: vi.fn(),
        loading: false,
      });

      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const dismissButton = screen.getByText('Dismiss');
      dismissButton.click();

      expect(mockClearError).toHaveBeenCalledTimes(1);
    });
  });

  describe('Logging Behavior', () => {
    it('should log page mount event on component mount', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(logger.info).toHaveBeenCalledWith('LoginPage mounted', {
        event: 'login_page_mount',
      });
    });

    it('should log page mount event only once', () => {
      const { rerender } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(logger.info).toHaveBeenCalledTimes(1);

      rerender(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(logger.info).toHaveBeenCalledTimes(1);
    });
  });

  describe('Navigation on Successful Login', () => {
    it('should navigate to default route when user is authenticated', async () => {
      const mockUser = { username: 'testuser', role: 'admin' };
      
      const { rerender } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      vi.mocked(useAuth).mockReturnValue({
        user: mockUser,
        error: null,
        login: mockLogin,
        clearError: mockClearError,
        logout: vi.fn(),
        loading: false,
      });

      rerender(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/');
      });
    });

    it('should log navigation event with user details', async () => {
      const mockUser = { username: 'testuser', role: 'staff' };
      
      const { rerender } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      vi.mocked(useAuth).mockReturnValue({
        user: mockUser,
        error: null,
        login: mockLogin,
        clearError: mockClearError,
        logout: vi.fn(),
        loading: false,
      });

      rerender(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(logger.info).toHaveBeenCalledWith(
          'Login successful, navigating to default route',
          {
            event: 'login_navigation',
            username: 'testuser',
            role: 'staff',
            target_route: '/',
          }
        );
      });
    });

    it('should not navigate when user is null', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  describe('Login Handler', () => {
    it('should call login function with correct credentials', async () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      submitButton.click();

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith('testuser', 'testpass');
      });
    });

    it('should handle successful login without errors', async () => {
      mockLogin.mockResolvedValue(undefined);

      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      submitButton.click();

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalled();
      });

      expect(logger.error).not.toHaveBeenCalled();
    });

    it('should log error when login fails with Error instance', async () => {
      const testError = new Error('Authentication failed');
      mockLogin.mockRejectedValue(testError);

      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      submitButton.click();

      await waitFor(() => {
        expect(logger.error).toHaveBeenCalledWith('Login failed in LoginPage', {
          event: 'login_page_error',
          error: 'Authentication failed',
        });
      });
    });

    it('should log error when login fails with non-Error instance', async () => {
      mockLogin.mockRejectedValue('String error');

      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      submitButton.click();

      await waitFor(() => {
        expect(logger.error).toHaveBeenCalledWith('Login failed in LoginPage', {
          event: 'login_page_error',
          error: 'Unknown error',
        });
      });
    });

    it('should handle login rejection gracefully', async () => {
      mockLogin.mockRejectedValue(new Error('Network error'));

      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      
      expect(() => submitButton.click()).not.toThrow();

      await waitFor(() => {
        expect(logger.error).toHaveBeenCalled();
      });
    });
  });

  describe('Integration Tests', () => {
    it('should handle complete login flow from form submission to navigation', async () => {
      const mockUser = { username: 'testuser', role: 'admin' };
      mockLogin.mockResolvedValue(undefined);

      const { rerender } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      submitButton.click();

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith('testuser', 'testpass');
      });

      vi.mocked(useAuth).mockReturnValue({
        user: mockUser,
        error: null,
        login: mockLogin,
        clearError: mockClearError,
        logout: vi.fn(),
        loading: false,
      });

      rerender(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/');
      });
    });

    it('should display error and allow dismissal after failed login', async () => {
      mockLogin.mockRejectedValue(new Error('Invalid credentials'));

      const { rerender } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const submitButton = screen.getByText('Submit');
      submitButton.click();

      await waitFor(() => {
        expect(logger.error).toHaveBeenCalled();
      });

      vi.mocked(useAuth).mockReturnValue({
        user: null,
        error: 'Invalid credentials',
        login: mockLogin,
        clearError: mockClearError,
        logout: vi.fn(),
        loading: false,
      });

      rerender(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      expect(screen.getByTestId('error-banner')).toBeInTheDocument();

      const dismissButton = screen.getByText('Dismiss');
      dismissButton.click();

      expect(mockClearError).toHaveBeenCalled();
    });
  });

  describe('Consistency with Application Design', () => {
    it('should maintain consistent branding with application', () => {
      render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const heading = screen.getByRole('heading', { name: /login/i });
      expect(heading).toBeInTheDocument();
      expect(heading.tagName).toBe('H1');
    });

    it('should use consistent color scheme', () => {
      const { container } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const outerDiv = container.firstChild as HTMLElement;
      expect(outerDiv).toHaveStyle({ backgroundColor: '#f5f5f5' });

      const innerDiv = container.querySelector('div > div') as HTMLElement;
      expect(innerDiv).toHaveStyle({ backgroundColor: 'white' });
    });

    it('should be responsive with proper padding', () => {
      const { container } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      );

      const outerDiv = container.firstChild as HTMLElement;
      expect(outerDiv).toHaveStyle({ padding: '1rem' });

      const innerDiv = container.querySelector('div > div') as HTMLElement;
      expect(innerDiv).toHaveStyle({ padding: '2rem' });
    });
  });
});