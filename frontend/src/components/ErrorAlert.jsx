import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import ErrorAlert from './ErrorAlert';

describe('ErrorAlert Component', () => {
  describe('Component Rendering', () => {
    test('should render ErrorAlert component with error message', () => {
      const error = {
        type: 'access_denied',
        message: 'You do not have permission to access this resource'
      };

      render(<ErrorAlert error={error} />);

      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(screen.getByText('You do not have permission to access this resource')).toBeInTheDocument();
    });

    test('should render error message as h3 heading', () => {
      const error = {
        type: 'network_error',
        message: 'Network connection failed'
      };

      render(<ErrorAlert error={error} />);

      const heading = screen.getByRole('heading', { level: 3 });
      expect(heading).toBeInTheDocument();
      expect(heading).toHaveTextContent('Network connection failed');
    });

    test('should render error icon', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const icon = screen.getByText('⚠');
      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass('error-icon');
    });
  });

  describe('Conditional Details Rendering', () => {
    test('should render error details when details prop is provided', () => {
      const error = {
        type: 'access_denied',
        message: 'You do not have permission to access this resource',
        details: 'Please contact your administrator for access'
      };

      render(<ErrorAlert error={error} />);

      expect(screen.getByText('Please contact your administrator for access')).toBeInTheDocument();
    });

    test('should NOT render details paragraph when details prop is not provided', () => {
      const error = {
        type: 'access_denied',
        message: 'You do not have permission to access this resource'
      };

      const { container } = render(<ErrorAlert error={error} />);

      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });

    test('should NOT render details paragraph when details is null', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied',
        details: null
      };

      const { container } = render(<ErrorAlert error={error} />);

      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });

    test('should NOT render details paragraph when details is undefined', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied',
        details: undefined
      };

      const { container } = render(<ErrorAlert error={error} />);

      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });

    test('should NOT render details paragraph when details is empty string', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied',
        details: ''
      };

      const { container } = render(<ErrorAlert error={error} />);

      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });
  });

  describe('Accessibility Attributes', () => {
    test('should have role="alert" attribute on outer div', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveAttribute('role', 'alert');
    });

    test('should have aria-live="polite" attribute for screen reader announcement', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveAttribute('aria-live', 'polite');
    });

    test('should have aria-label="Access denied error" for additional context', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveAttribute('aria-label', 'Access denied error');
    });

    test('should have aria-hidden="true" on error icon', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const icon = screen.getByText('⚠');
      expect(icon).toHaveAttribute('aria-hidden', 'true');
    });
  });

  describe('CSS Classes and Styling', () => {
    test('should apply error-alert CSS class to outer div', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveClass('error-alert');
    });

    test('should apply light red background color styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveStyle({ backgroundColor: '#fee' });
    });

    test('should apply red border styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveStyle({ border: '1px solid #c33' });
    });

    test('should apply padding styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveStyle({ padding: '16px' });
    });

    test('should apply border radius styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveStyle({ borderRadius: '4px' });
    });

    test('should apply margin bottom styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const alertDiv = screen.getByRole('alert');
      expect(alertDiv).toHaveStyle({ marginBottom: '16px' });
    });

    test('should apply error-icon CSS class to icon span', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const icon = screen.getByText('⚠');
      expect(icon).toHaveClass('error-icon');
    });

    test('should apply icon color styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const icon = screen.getByText('⚠');
      expect(icon).toHaveStyle({ color: '#c33' });
    });

    test('should apply icon font size styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const icon = screen.getByText('⚠');
      expect(icon).toHaveStyle({ fontSize: '24px' });
    });

    test('should apply icon font weight styling', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied'
      };

      render(<ErrorAlert error={error} />);

      const icon = screen.getByText('⚠');
      expect(icon).toHaveStyle({ fontWeight: 'bold' });
    });
  });

  describe('Security Considerations', () => {
    test('should NOT display stack traces in error message', () => {
      const error = {
        type: 'system_error',
        message: 'An error occurred',
        stack: 'Error: Something went wrong\n    at Object.<anonymous> (/app/index.js:10:15)'
      };

      const { container } = render(<ErrorAlert error={error} />);

      expect(container.textContent).not.toContain('Error: Something went wrong');
      expect(container.textContent).not.toContain('/app/index.js');
      expect(container.textContent).not.toContain('at Object.<anonymous>');
    });

    test('should NOT display internal error codes', () => {
      const error = {
        type: 'database_error',
        message: 'Unable to process request',
        code: 'ERR_DB_CONNECTION_FAILED_5432'
      };

      const { container } = render(<ErrorAlert error={error} />);

      expect(container.textContent).not.toContain('ERR_DB_CONNECTION_FAILED_5432');
      expect(container.textContent).not.toContain('5432');
    });

    test('should NOT reveal sensitive system information', () => {
      const error = {
        type: 'server_error',
        message: 'Service unavailable',
        systemInfo: 'PostgreSQL connection to 192.168.1.100:5432 failed'
      };

      const { container } = render(<ErrorAlert error={error} />);

      expect(container.textContent).not.toContain('PostgreSQL');
      expect(container.textContent).not.toContain('192.168.1.100');
      expect(container.textContent).not.toContain('5432');
    });

    test('should only display sanitized user-facing message', () => {
      const error = {
        type: 'access_denied',
        message: 'You do not have permission to access this resource',
        internalMessage: 'User ID 12345 lacks role ADMIN for resource /api/admin/users'
      };

      const { container } = render(<ErrorAlert error={error} />);

      expect(screen.getByText('You do not have permission to access this resource')).toBeInTheDocument();
      expect(container.textContent).not.toContain('User ID 12345');
      expect(container.textContent).not.toContain('role ADMIN');
      expect(container.textContent).not.toContain('/api/admin/users');
    });

    test('should only display sanitized user-facing details', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied',
        details: 'Please contact your administrator',
        internalDetails: 'JWT token expired at 2024-01-15T10:30:00Z'
      };

      const { container } = render(<ErrorAlert error={error} />);

      expect(screen.getByText('Please contact your administrator')).toBeInTheDocument();
      expect(container.textContent).not.toContain('JWT token');
      expect(container.textContent).not.toContain('2024-01-15T10:30:00Z');
    });
  });

  describe('Error Object Structure', () => {
    test('should handle error object with type property', () => {
      const error = {
        type: 'validation_error',
        message: 'Invalid input'
      };

      render(<ErrorAlert error={error} />);

      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    test('should handle error object with message property', () => {
      const error = {
        type: 'network_error',
        message: 'Connection timeout'
      };

      render(<ErrorAlert error={error} />);

      expect(screen.getByText('Connection timeout')).toBeInTheDocument();
    });

    test('should handle error object with optional details property', () => {
      const error = {
        type: 'access_denied',
        message: 'Access denied',
        details: 'Contact support'
      };

      render(<ErrorAlert error={error} />);

      expect(screen.getByText('Contact support')).toBeInTheDocument();
    });

    test('should handle different error types', () => {
      const errorTypes = [
        { type: 'access_denied', message: 'Access denied' },
        { type: 'network_error', message: 'Network error' },
        { type: 'validation_error', message: 'Validation error' },
        { type: 'server_error', message: 'Server error' }
      ];

      errorTypes.forEach(error => {
        const { unmount } = render(<ErrorAlert error={error} />);
        expect(screen.getByText(error.message)).toBeInTheDocument();
        unmount();
      });
    });
  });

  describe('Component Export', () => {
    test('should export ErrorAlert as default export', () => {
      expect(ErrorAlert).toBeDefined();
      expect(typeof ErrorAlert).toBe('function');
    });
  });

  describe('Integration with StaffList.jsx', () => {
    test('should be consumable by parent components', () => {
      const error = {
        type: 'access_denied',
        message: 'You do not have permission to view staff list',
        details: 'Please request access from your manager'
      };

      const { container } = render(<ErrorAlert error={error} />);

      expect(container.firstChild).toBeInTheDocument();
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    test('should render correctly when passed as prop from parent', () => {
      const ParentComponent = () => {
        const error = {
          type: 'access_denied',
          message: 'Access denied to staff list'
        };
        return <ErrorAlert error={error} />;
      };

      render(<ParentComponent />);

      expect(screen.getByText('Access denied to staff list')).toBeInTheDocument();
    });
  });
});