import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginForm from '../components/LoginForm';
import { logger } from '../utils/logger';

jest.mock('../utils/logger', () => ({
  logger: {
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn()
  }
}));

describe('LoginForm', () => {
  let mockOnSubmit: jest.Mock;

  beforeEach(() => {
    mockOnSubmit = jest.fn();
    jest.clearAllMocks();
  });

  describe('Form Rendering', () => {
    test('should render username and password fields', () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
    });

    test('should render form with proper accessibility attributes', () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      expect(usernameInput).toHaveAttribute('aria-required', 'true');
      expect(passwordInput).toHaveAttribute('aria-required', 'true');
      expect(usernameInput).toHaveAttribute('aria-invalid', 'false');
      expect(passwordInput).toHaveAttribute('aria-invalid', 'false');
    });
  });

  describe('Client-Side Validation - Username', () => {
    test('should display field-level error when username is empty', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Username is required')).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
      expect(logger.warn).toHaveBeenCalledWith('Form validation failed', {
        event: 'form_validation_error',
        field: 'username',
        error: 'Username is required'
      });
    });

    test('should display field-level error when username is only whitespace', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      await userEvent.type(usernameInput, '   ');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Username is required')).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    test('should set aria-invalid and aria-describedby when username validation fails', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        const usernameInput = screen.getByLabelText(/username/i);
        expect(usernameInput).toHaveAttribute('aria-invalid', 'true');
        expect(usernameInput).toHaveAttribute('aria-describedby', 'username-error');
        expect(screen.getByRole('alert', { name: /username is required/i })).toBeInTheDocument();
      });
    });
  });

  describe('Client-Side Validation - Password', () => {
    test('should display field-level error when password is empty', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      await userEvent.type(usernameInput, 'testuser');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Password is required')).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
      expect(logger.warn).toHaveBeenCalledWith('Form validation failed', {
        event: 'form_validation_error',
        field: 'password',
        error: 'Password is required'
      });
    });

    test('should display field-level error when password is only whitespace', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, '   ');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Password is required')).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    test('should set aria-invalid and aria-describedby when password validation fails', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      await userEvent.type(usernameInput, 'testuser');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        const passwordInput = screen.getByLabelText(/password/i);
        expect(passwordInput).toHaveAttribute('aria-invalid', 'true');
        expect(passwordInput).toHaveAttribute('aria-describedby', 'password-error');
        expect(screen.getByRole('alert', { name: /password is required/i })).toBeInTheDocument();
      });
    });
  });

  describe('Validation Error Clearing', () => {
    test('should clear previous field errors on new submission', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Username is required')).toBeInTheDocument();
      });

      const usernameInput = screen.getByLabelText(/username/i);
      await userEvent.type(usernameInput, 'testuser');
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.queryByText('Username is required')).not.toBeInTheDocument();
        expect(screen.getByText('Password is required')).toBeInTheDocument();
      });
    });

    test('should clear form-level validation error on new submission', async () => {
      mockOnSubmit.mockRejectedValueOnce(new Error('Network error'));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.queryByText('Network error')).not.toBeInTheDocument();
      });
    });
  });

  describe('Loading State Management', () => {
    test('should set loading state during submission', async () => {
      mockOnSubmit.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /logging in\.\.\./i })).toBeInTheDocument();
        expect(submitButton).toBeDisabled();
      });
    });

    test('should disable form inputs during loading', async () => {
      mockOnSubmit.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).toBeDisabled();
        expect(passwordInput).toBeDisabled();
      });
    });

    test('should prevent multiple submissions when already loading', async () => {
      mockOnSubmit.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);
      fireEvent.click(submitButton);
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledTimes(1);
        expect(logger.warn).toHaveBeenCalledWith('Form submission prevented - already loading', {
          event: 'form_submit_prevented'
        });
      });
    });

    test('should reset loading state after successful submission', async () => {
      mockOnSubmit.mockResolvedValueOnce(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /login/i })).not.toBeDisabled();
      });
    });

    test('should reset loading state after failed submission', async () => {
      mockOnSubmit.mockRejectedValueOnce(new Error('Login failed'));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /login/i })).not.toBeDisabled();
      });
    });
  });

  describe('Successful Form Submission', () => {
    test('should call onSubmit with username and password', async () => {
      mockOnSubmit.mockResolvedValueOnce(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('testuser', 'password123');
      });
    });

    test('should log form submission start', async () => {
      mockOnSubmit.mockResolvedValueOnce(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(logger.info).toHaveBeenCalledWith('Form submission started', {
          event: 'form_submit_start',
          username: 'testuser'
        });
      });
    });

    test('should log form submission success', async () => {
      mockOnSubmit.mockResolvedValueOnce(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(logger.info).toHaveBeenCalledWith('Form submission successful', {
          event: 'form_submit_success',
          username: 'testuser'
        });
      });
    });
  });

  describe('Server Validation Errors - 422 Response with Array Format', () => {
    test('should handle 422 response with field-specific errors in array format', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            errors: [
              { field: 'username', message: 'Username already exists' },
              { field: 'password', message: 'Password too weak' }
            ]
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'weak');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Username already exists')).toBeInTheDocument();
        expect(screen.getByText('Password too weak')).toBeInTheDocument();
      });

      expect(logger.warn).toHaveBeenCalledWith('Validation errors from server', {
        event: 'server_validation_error',
        errors: {
          username: 'Username already exists',
          password: 'Password too weak'
        }
      });
    });

    test('should set aria-invalid for fields with server validation errors', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            errors: [
              { field: 'username', message: 'Username already exists' }
            ]
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).toHaveAttribute('aria-invalid', 'true');
        expect(usernameInput).toHaveAttribute('aria-describedby', 'username-error');
      });
    });

    test('should skip errors without field or message in array format', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            errors: [
              { field: 'username', message: 'Username already exists' },
              { field: '', message: 'Invalid error' },
              { field: 'password', message: '' },
              { message: 'No field specified' }
            ]
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Username already exists')).toBeInTheDocument();
        expect(screen.queryByText('Invalid error')).not.toBeInTheDocument();
        expect(screen.queryByText('No field specified')).not.toBeInTheDocument();
      });
    });
  });

  describe('Server Validation Errors - 422 Response with Object Format', () => {
    test('should handle 422 response with field-specific errors in object format', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            errors: {
              username: 'Username is invalid',
              password: 'Password must be at least 8 characters'
            }
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'test');
      await userEvent.type(passwordInput, 'short');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Username is invalid')).toBeInTheDocument();
        expect(screen.getByText('Password must be at least 8 characters')).toBeInTheDocument();
      });

      expect(logger.warn).toHaveBeenCalledWith('Validation errors from server', {
        event: 'server_validation_error',
        errors: {
          username: 'Username is invalid',
          password: 'Password must be at least 8 characters'
        }
      });
    });
  });

  describe('Server Validation Errors - 422 Response with Message', () => {
    test('should display form-level error when 422 has message but no field errors', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            message: 'Validation failed'
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /validation failed/i })).toBeInTheDocument();
      });
    });

    test('should display default message when 422 has no message or errors', async () => {
      const error = {
        response: {
          status: 422,
          data: {}
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Validation failed')).toBeInTheDocument();
      });
    });
  });

  describe('General Error Handling', () => {
    test('should display form-level error for non-422 errors with message', async () => {
      const error = new Error('Network connection failed');
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Network connection failed')).toBeInTheDocument();
      });

      expect(logger.error).toHaveBeenCalledWith('Form submission failed', {
        event: 'form_submit_error',
        username: 'testuser',
        error: 'Network connection failed'
      });
    });

    test('should display default error message for errors without message', async () => {
      mockOnSubmit.mockRejectedValueOnce({});

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('An error occurred during login')).toBeInTheDocument();
      });
    });

    test('should log error with Unknown error for non-Error objects', async () => {
      mockOnSubmit.mockRejectedValueOnce('string error');

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(logger.error).toHaveBeenCalledWith('Form submission failed', {
          event: 'form_submit_error',
          username: 'testuser',
          error: 'Unknown error'
        });
      });
    });

    test('should render form-level error with proper accessibility attributes', async () => {
      mockOnSubmit.mockRejectedValueOnce(new Error('Server error'));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        const alert = screen.getByRole('alert', { name: /server error/i });
        expect(alert).toHaveAttribute('aria-live', 'polite');
      });
    });
  });

  describe('Form Interaction', () => {
    test('should update username state on input change', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
      await userEvent.type(usernameInput, 'newuser');

      expect(usernameInput.value).toBe('newuser');
    });

    test('should update password state on input change', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;
      await userEvent.type(passwordInput, 'newpassword');

      expect(passwordInput.value).toBe('newpassword');
    });

    test('should prevent default form submission', async () => {
      mockOnSubmit.mockResolvedValueOnce(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const form = screen.getByRole('button', { name: /login/i }).closest('form');
      const preventDefaultSpy = jest.fn();

      form?.addEventListener('submit', (e) => {
        preventDefaultSpy();
        e.preventDefault();
      });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });
    });
  });

  describe('Edge Cases', () => {
    test('should handle 422 response with empty errors array', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            errors: []
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Validation failed')).toBeInTheDocument();
      });
    });

    test('should handle 422 response with null errors', async () => {
      const error = {
        response: {
          status: 422,
          data: {
            errors: null
          }
        }
      };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Validation failed')).toBeInTheDocument();
      });
    });

    test('should handle error without response object', async () => {
      const error = { message: 'Request failed' };
      mockOnSubmit.mockRejectedValueOnce(error);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Request failed')).toBeInTheDocument();
      });
    });

    test('should trim whitespace from username before validation', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      await userEvent.type(usernameInput, '  testuser  ');

      const passwordInput = screen.getByLabelText(/password/i);
      await userEvent.type(passwordInput, 'password123');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('  testuser  ', 'password123');
      });
    });
  });
});
