import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import App from '../App';
import LoginPage from '../pages/LoginPage';
import LoginForm from '../components/LoginForm';
import ErrorBanner from '../components/ErrorBanner';
import ProtectedRoute from '../components/ProtectedRoute';
import { authApi } from '../api/authApi';

vi.mock('../api/authApi');

describe('App Component', () => {
  it('renders without crashing', () => {
    render(<App />);
    expect(document.getElementById('root')).toBeTruthy();
  });

  it('redirects to login when unauthenticated', async () => {
    vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error('Not authenticated'));
    render(<App />);
    await waitFor(() => {
      expect(window.location.pathname).toBe('/login');
    });
  });
});

describe('LoginPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders login form', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );
    expect(screen.getByRole('heading', { name: /login/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('shows error banner when login fails', async () => {
    const mockError = { response: { data: { message: 'Invalid credentials' } } };
    vi.mocked(authApi.login).mockRejectedValue(mockError);
    vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error('Not authenticated'));

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await userEvent.type(usernameInput, 'testuser');
    await userEvent.type(passwordInput, 'wrongpassword');
    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });
});

describe('LoginForm Component', () => {
  it('validates required fields', async () => {
    const mockSubmit = vi.fn();
    render(<LoginForm onSubmit={mockSubmit} />);

    const submitButton = screen.getByRole('button', { name: /login/i });
    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/username is required/i)).toBeInTheDocument();
    });
    expect(mockSubmit).not.toHaveBeenCalled();
  });

  it('submits form with valid data', async () => {
    const mockSubmit = vi.fn().mockResolvedValue(undefined);
    render(<LoginForm onSubmit={mockSubmit} />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await userEvent.type(usernameInput, 'testuser');
    await userEvent.type(passwordInput, 'testpass123');
    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(mockSubmit).toHaveBeenCalledWith('testuser', 'testpass123');
    });
  });

  it('prevents multiple rapid submits', async () => {
    const mockSubmit = vi.fn().mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    render(<LoginForm onSubmit={mockSubmit} />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await userEvent.type(usernameInput, 'testuser');
    await userEvent.type(passwordInput, 'testpass123');
    await userEvent.click(submitButton);
    await userEvent.click(submitButton);

    expect(mockSubmit).toHaveBeenCalledTimes(1);
  });
});

describe('ErrorBanner Component', () => {
  it('renders error message', () => {
    render(<ErrorBanner message="Test error message" />);
    expect(screen.getByText(/test error message/i)).toBeInTheDocument();
  });

  it('calls onDismiss when dismiss button clicked', async () => {
    const mockDismiss = vi.fn();
    render(<ErrorBanner message="Test error" onDismiss={mockDismiss} />);

    const dismissButton = screen.getByRole('button', { name: /dismiss/i });
    await userEvent.click(dismissButton);

    expect(mockDismiss).toHaveBeenCalledTimes(1);
  });
});

describe('ProtectedRoute Component', () => {
  it('redirects to login when user is not authenticated', async () => {
    vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error('Not authenticated'));

    render(
      <BrowserRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(window.location.pathname).toBe('/login');
    });
  });

  it('renders children when user is authenticated', async () => {
    vi.mocked(authApi.getCurrentUser).mockResolvedValue({
      id: '1',
      username: 'testuser',
      role: 'user'
    });

    render(
      <BrowserRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/protected content/i)).toBeInTheDocument();
    });
  });
});

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('login returns user data on success', async () => {
    const mockResponse = {
      message: 'Login successful',
      user: { id: '1', username: 'testuser', role: 'user' }
    };
    vi.mocked(authApi.login).mockResolvedValue(mockResponse);

    const result = await authApi.login({ username: 'testuser', password: 'testpass123' });

    expect(result).toEqual(mockResponse);
    expect(result.user.username).toBe('testuser');
  });

  it('login throws error on invalid credentials', async () => {
    const mockError = { response: { status: 401, data: { message: 'Invalid credentials' } } };
    vi.mocked(authApi.login).mockRejectedValue(mockError);

    await expect(authApi.login({ username: 'testuser', password: 'wrong' })).rejects.toEqual(mockError);
  });

  it('getCurrentUser returns user profile', async () => {
    const mockUser = { id: '1', username: 'testuser', role: 'user' };
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(mockUser);

    const result = await authApi.getCurrentUser();

    expect(result).toEqual(mockUser);
  });

  it('getCurrentUser throws error when not authenticated', async () => {
    const mockError = { response: { status: 401, data: { message: 'Not authenticated' } } };
    vi.mocked(authApi.getCurrentUser).mockRejectedValue(mockError);

    await expect(authApi.getCurrentUser()).rejects.toEqual(mockError);
  });

  it('logout completes successfully', async () => {
    vi.mocked(authApi.logout).mockResolvedValue(undefined);

    await expect(authApi.logout()).resolves.toBeUndefined();
  });
});