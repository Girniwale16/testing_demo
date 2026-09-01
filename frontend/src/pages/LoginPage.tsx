import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import LoginPage from '../LoginPage';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
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
    <form data-testid="login-form" onSubmit={(e) => {
      e.preventDefault();
      onSubmit('testuser', 'testpass');
    }}>
      <button type="submit">Submit</button>
    </form>
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
    (useNavigate as any).mockReturnValue(mockNavigate);
    (useAuth as any).mockReturnValue({
      user: null,
      error: null,
      login: mockLogin,
      clearError: mockClearError,
    });
    (logger.info as any).mockImplementation(() => {});
    (logger.error as any).mockImplementation(() => {});
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render LoginForm component', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    expect(screen.getByTestId('login-form')).toBeInTheDocument();
  });

  it('should log info when LoginPage mounts', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    expect(logger.info).toHaveBeenCalledWith('LoginPage mounted', { event: 'login_page_mount' });
  });

  it('should navigate to /dashboard on successful authentication', async () => {
    const mockUser = { username: 'testuser', role: 'admin' };
    (useAuth as any).mockReturnValue({
      user: mockUser,
      error: null,
      login: mockLogin,
      clearError: mockClearError,
    });

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });

    expect(logger.info).toHaveBeenCalledWith('Login successful, navigating to dashboard', {
      event: 'login_navigation',
      username: 'testuser',
      role: 'admin',
      target_route: '/dashboard'
    });
  });

  it('should display ErrorBanner when authentication fails', () => {
    const errorMessage = 'Invalid credentials';
    (useAuth as any).mockReturnValue({
      user: null,
      error: errorMessage,
      login: mockLogin,
      clearError: mockClearError,
    });

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    expect(screen.getByTestId('error-banner')).toBeInTheDocument();
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('should not display ErrorBanner when there is no error', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    expect(screen.queryByTestId('error-banner')).not.toBeInTheDocument();
  });

  it('should call clearError when ErrorBanner dismiss is clicked', () => {
    const errorMessage = 'Invalid credentials';
    (useAuth as any).mockReturnValue({
      user: null,
      error: errorMessage,
      login: mockLogin,
      clearError: mockClearError,
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

  it('should call login with username and password when form is submitted', async () => {
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

  it('should log error when login fails with Error instance', async () => {
    const errorMessage = 'Network error';
    mockLogin.mockRejectedValueOnce(new Error(errorMessage));

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
        error: errorMessage
      });
    });
  });

  it('should log error when login fails with non-Error instance', async () => {
    mockLogin.mockRejectedValueOnce('Unknown error');

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
        error: 'Unknown error'
      });
    });
  });

  it('should render page with correct styling structure', () => {
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
      padding: '1rem'
    });
  });

  it('should render Login heading', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
  });

  it('should not navigate when user is null', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('should handle successful login flow end-to-end', async () => {
    const mockUser = { username: 'john.doe', role: 'user' };
    
    (useAuth as any).mockReturnValueOnce({
      user: null,
      error: null,
      login: mockLogin,
      clearError: mockClearError,
    });

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

    (useAuth as any).mockReturnValue({
      user: mockUser,
      error: null,
      login: mockLogin,
      clearError: mockClearError,
    });

    rerender(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });
});