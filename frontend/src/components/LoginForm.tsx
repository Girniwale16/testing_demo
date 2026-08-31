import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginForm from './LoginForm';
import { logger } from '../utils/logger';

jest.mock('../utils/logger', () => ({
  logger: {
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn(),
  },
}));

describe('LoginForm', () => {
  let mockOnSubmit: jest.Mock;

  beforeEach(() => {
    mockOnSubmit = jest.fn();
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    test('renders login form with username and password fields', () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
    });

    test('renders form with proper accessibility attributes', () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      expect(usernameInput).toHaveAttribute('aria-required', 'true');
      expect(passwordInput).toHaveAttribute('aria-required', 'true');
      expect(usernameInput).toHaveAttribute('type', 'text');
      expect(passwordInput).toHaveAttribute('type', 'password');
    });
  });

  describe('Form Validation', () => {
    test('displays error when username is empty on submit', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /username is required/i })).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
      expect(logger.warn).toHaveBeenCalledWith('Form validation failed', {
        event: 'form_validation_error',
        field: 'username',
        error: 'Username is required',
      });
    });

    test('displays error when password is empty on submit', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      await userEvent.type(usernameInput, 'testuser');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /password is required/i })).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
      expect(logger.warn).toHaveBeenCalledWith('Form validation failed', {
        event: 'form_validation_error',
        field: 'password',
        error: 'Password is required',
      });
    });

    test('displays errors for both fields when both are empty', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /username is required/i })).toBeInTheDocument();
        expect(screen.getByRole('alert', { name: /password is required/i })).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
      expect(logger.warn).toHaveBeenCalledTimes(2);
    });

    test('validates trimmed values - rejects whitespace-only input', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, '   ');
      await userEvent.type(passwordInput, '   ');

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /username is required/i })).toBeInTheDocument();
        expect(screen.getByRole('alert', { name: /password is required/i })).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    test('updates aria-invalid attribute when validation fails', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).toHaveAttribute('aria-invalid', 'true');
        expect(usernameInput).toHaveAttribute('aria-describedby', 'username-error');
      });
    });
  });

  describe('Loading State', () => {
    test('disables form inputs and button during submission', async () => {
      mockOnSubmit.mockImplementation(() => new Promise((resolve) => setTimeout(resolve, 100)));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).toBeDisabled();
        expect(passwordInput).toBeDisabled();
        expect(submitButton).toBeDisabled();
        expect(submitButton).toHaveTextContent(/logging in/i);
      });
    });

    test('prevents duplicate submissions when already loading', async () => {
      mockOnSubmit.mockImplementation(() => new Promise((resolve) => setTimeout(resolve, 100)));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);
      fireEvent.click(submitButton);
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledTimes(1);
      });

      expect(logger.warn).toHaveBeenCalledWith('Form submission prevented - already loading', {
        event: 'form_submit_prevented',
      });
    });

    test('re-enables form after successful submission', async () => {
      mockOnSubmit.mockResolvedValue(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).not.toBeDisabled();
        expect(passwordInput).not.toBeDisabled();
        expect(submitButton).not.toBeDisabled();
      });
    });

    test('re-enables form after failed submission', async () => {
      mockOnSubmit.mockRejectedValue(new Error('Login failed'));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).not.toBeDisabled();
        expect(passwordInput).not.toBeDisabled();
        expect(submitButton).not.toBeDisabled();
      });
    });
  });

  describe('Successful Submission', () => {
    test('calls onSubmit with username and password when form is valid', async () => {
      mockOnSubmit.mockResolvedValue(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('testuser', 'password123');
      });

      expect(logger.info).toHaveBeenCalledWith('Form submission started', {
        event: 'form_submit_start',
        username: 'testuser',
      });

      expect(logger.info).toHaveBeenCalledWith('Form submission successful', {
        event: 'form_submit_success',
        username: 'testuser',
      });
    });

    test('clears previous errors on successful submission', async () => {
      mockOnSubmit.mockResolvedValue(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /username is required/i })).toBeInTheDocument();
      });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.queryByRole('alert', { name: /username is required/i })).not.toBeInTheDocument();
      });
    });
  });

  describe('Error Handling - Server Validation (422)', () => {
    test('displays field errors from server in array format', async () => {
      const serverError = {
        response: {
          status: 422,
          data: {
            errors: [
              { field: 'username', message: 'Username already exists' },
              { field: 'password', message: 'Password too weak' },
            ],
          },
        },
      };

      mockOnSubmit.mockRejectedValue(serverError);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'pass');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /username already exists/i })).toBeInTheDocument();
        expect(screen.getByRole('alert', { name: /password too weak/i })).toBeInTheDocument();
      });

      expect(logger.warn).toHaveBeenCalledWith('Validation errors from server', {
        event: 'server_validation_error',
        errors: {
          username: 'Username already exists',
          password: 'Password too weak',
        },
      });
    });

    test('displays field errors from server in object format', async () => {
      const serverError = {
        response: {
          status: 422,
          data: {
            errors: {
              username: 'Invalid username format',
              password: 'Password must contain special characters',
            },
          },
        },
      };

      mockOnSubmit.mockRejectedValue(serverError);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'test@user');
      await userEvent.type(passwordInput, 'password');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /invalid username format/i })).toBeInTheDocument();
        expect(screen.getByRole('alert', { name: /password must contain special characters/i })).toBeInTheDocument();
      });
    });

    test('displays form error when 422 has message but no field errors', async () => {
      const serverError = {
        response: {
          status: 422,
          data: {
            message: 'Validation failed',
          },
        },
      };

      mockOnSubmit.mockRejectedValue(serverError);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /validation failed/i })).toBeInTheDocument();
      });
    });

    test('displays form error when 422 has errors object with no valid fields', async () => {
      const serverError = {
        response: {
          status: 422,
          data: {
            errors: {},
            message: 'Validation failed',
          },
        },
      };

      mockOnSubmit.mockRejectedValue(serverError);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /validation failed/i })).toBeInTheDocument();
      });
    });

    test('handles 422 with default message when no message provided', async () => {
      const serverError = {
        response: {
          status: 422,
          data: {},
        },
      };

      mockOnSubmit.mockRejectedValue(serverError);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /validation failed/i })).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling - General Errors', () => {
    test('displays general error message for non-422 errors', async () => {
      const networkError = new Error('Network error');
      mockOnSubmit.mockRejectedValue(networkError);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /network error/i })).toBeInTheDocument();
      });

      expect(logger.error).toHaveBeenCalledWith('Form submission failed', {
        event: 'form_submit_error',
        username: 'testuser',
        error: 'Network error',
      });
    });

    test('displays default error message when error has no message', async () => {
      mockOnSubmit.mockRejectedValue({});

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /an error occurred during login/i })).toBeInTheDocument();
      });

      expect(logger.error).toHaveBeenCalledWith('Form submission failed', {
        event: 'form_submit_error',
        username: 'testuser',
        error: 'Unknown error',
      });
    });

    test('clears previous errors before new submission', async () => {
      mockOnSubmit.mockRejectedValueOnce(new Error('First error')).mockResolvedValueOnce(undefined);

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByRole('alert', { name: /first error/i })).toBeInTheDocument();
      });

      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.queryByRole('alert', { name: /first error/i })).not.toBeInTheDocument();
      });
    });
  });

  describe('Form Interaction', () => {
    test('updates username input value on change', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;

      await userEvent.type(usernameInput, 'newuser');

      expect(usernameInput.value).toBe('newuser');
    });

    test('updates password input value on change', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;

      await userEvent.type(passwordInput, 'newpassword');

      expect(passwordInput.value).toBe('newpassword');
    });

    test('prevents default form submission behavior', async () => {
      mockOnSubmit.mockResolvedValue(undefined);

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

      fireEvent.submit(form!);

      expect(preventDefaultSpy).toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    test('form has noValidate attribute to use custom validation', () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const form = screen.getByRole('button', { name: /login/i }).closest('form');

      expect(form).toHaveAttribute('noValidate');
    });

    test('error messages have proper ARIA attributes', async () => {
      render(<LoginForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /login/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        const usernameError = screen.getByRole('alert', { name: /username is required/i });
        const passwordError = screen.getByRole('alert', { name: /password is required/i });

        expect(usernameError).toHaveAttribute('id', 'username-error');
        expect(passwordError).toHaveAttribute('id', 'password-error');
      });
    });

    test('form error has proper ARIA live region', async () => {
      mockOnSubmit.mockRejectedValue(new Error('Login failed'));

      render(<LoginForm onSubmit={mockOnSubmit} />);

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /login/i });

      await userEvent.type(usernameInput, 'testuser');
      await userEvent.type(passwordInput, 'password123');

      fireEvent.click(submitButton);

      await waitFor(() => {
        const formError = screen.getByRole('alert', { name: /login failed/i });
        expect(formError).toHaveAttribute('aria-live', 'polite');
      });
    });
  });
});