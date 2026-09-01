import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ErrorBanner from '../components/ErrorBanner';
import { logger } from '../utils/logger';

jest.mock('../utils/logger', () => ({
  logger: {
    error: jest.fn(),
    info: jest.fn()
  }
}));

describe('ErrorBanner', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('message prop - string', () => {
    it('should render a single string message', () => {
      render(<ErrorBanner message="Test error message" />);
      expect(screen.getByText('Test error message')).toBeInTheDocument();
    });

    it('should log error when displaying single string message', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-123" />);
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: 'Test error',
        correlation_id: 'corr-123'
      });
    });
  });

  describe('message prop - string array', () => {
    it('should render multiple error messages as a list', () => {
      const messages = ['Error 1', 'Error 2', 'Error 3'];
      render(<ErrorBanner message={messages} />);
      
      const listItems = screen.getAllByRole('listitem');
      expect(listItems).toHaveLength(3);
      expect(listItems[0]).toHaveTextContent('Error 1');
      expect(listItems[1]).toHaveTextContent('Error 2');
      expect(listItems[2]).toHaveTextContent('Error 3');
    });

    it('should render messages in an unordered list', () => {
      const messages = ['Error 1', 'Error 2'];
      const { container } = render(<ErrorBanner message={messages} />);
      
      const ul = container.querySelector('ul');
      expect(ul).toBeInTheDocument();
      expect(ul).toHaveStyle({ margin: '0', paddingLeft: '1.5rem' });
    });

    it('should log error when displaying array of messages', () => {
      const messages = ['Error 1', 'Error 2'];
      render(<ErrorBanner message={messages} correlationId="corr-456" />);
      
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: messages,
        correlation_id: 'corr-456'
      });
    });
  });

  describe('fieldErrors prop', () => {
    it('should not render field errors when fieldErrors prop is not provided', () => {
      render(<ErrorBanner message="Test error" />);
      expect(screen.queryByText(/:/)).not.toBeInTheDocument();
    });

    it('should not render field errors when fieldErrors is an empty object', () => {
      render(<ErrorBanner message="Test error" fieldErrors={{}} />);
      const container = screen.getByRole('alert');
      expect(container.textContent).not.toMatch(/:/);
    });

    it('should render single field error with field name prefix', () => {
      const fieldErrors = { Name: 'Field is required' };
      render(<ErrorBanner message="Validation failed" fieldErrors={fieldErrors} />);
      
      expect(screen.getByText('Name:', { exact: false })).toBeInTheDocument();
      expect(screen.getByText(/Field is required/)).toBeInTheDocument();
    });

    it('should render multiple field errors as separate lines', () => {
      const fieldErrors = {
        Name: 'Field is required',
        Email: 'Invalid email format',
        Password: 'Password must be at least 8 characters'
      };
      render(<ErrorBanner message="Validation failed" fieldErrors={fieldErrors} />);
      
      expect(screen.getByText('Name:', { exact: false })).toBeInTheDocument();
      expect(screen.getByText(/Field is required/)).toBeInTheDocument();
      expect(screen.getByText('Email:', { exact: false })).toBeInTheDocument();
      expect(screen.getByText(/Invalid email format/)).toBeInTheDocument();
      expect(screen.getByText('Password:', { exact: false })).toBeInTheDocument();
      expect(screen.getByText(/Password must be at least 8 characters/)).toBeInTheDocument();
    });

    it('should render field errors with proper formatting', () => {
      const fieldErrors = { Username: 'Already taken' };
      const { container } = render(<ErrorBanner message="Error" fieldErrors={fieldErrors} />);
      
      const fieldErrorDiv = container.querySelector('div[style*="marginTop: 0.5rem"]');
      expect(fieldErrorDiv).toBeInTheDocument();
      
      const strong = fieldErrorDiv?.querySelector('strong');
      expect(strong).toHaveTextContent('Username:');
    });

    it('should render field errors alongside message prop', () => {
      const fieldErrors = { Name: 'Required' };
      render(<ErrorBanner message="Form validation failed" fieldErrors={fieldErrors} />);
      
      expect(screen.getByText('Form validation failed')).toBeInTheDocument();
      expect(screen.getByText('Name:', { exact: false })).toBeInTheDocument();
      expect(screen.getByText(/Required/)).toBeInTheDocument();
    });

    it('should render field errors alongside array message prop', () => {
      const messages = ['Error 1', 'Error 2'];
      const fieldErrors = { Email: 'Invalid' };
      render(<ErrorBanner message={messages} fieldErrors={fieldErrors} />);
      
      expect(screen.getByText('Error 1')).toBeInTheDocument();
      expect(screen.getByText('Error 2')).toBeInTheDocument();
      expect(screen.getByText('Email:', { exact: false })).toBeInTheDocument();
      expect(screen.getByText(/Invalid/)).toBeInTheDocument();
    });
  });

  describe('correlationId prop - collapsed/expandable section', () => {
    it('should not render technical details section when correlationId is not provided', () => {
      render(<ErrorBanner message="Test error" />);
      expect(screen.queryByText(/Technical Details/)).not.toBeInTheDocument();
    });

    it('should render collapsed technical details button when correlationId is provided', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-789" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      expect(button).toBeInTheDocument();
      expect(button).toHaveAttribute('aria-expanded', 'false');
    });

    it('should not display correlationId prominently by default', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-789" />);
      
      expect(screen.queryByText(/Correlation ID: corr-789/)).not.toBeInTheDocument();
    });

    it('should expand technical details section when button is clicked', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-789" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      fireEvent.click(button);
      
      expect(button).toHaveAttribute('aria-expanded', 'true');
      expect(screen.getByText(/Correlation ID: corr-789/)).toBeInTheDocument();
    });

    it('should collapse technical details section when button is clicked again', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-789" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      fireEvent.click(button);
      expect(screen.getByText(/Correlation ID: corr-789/)).toBeInTheDocument();
      
      fireEvent.click(button);
      expect(screen.queryByText(/Correlation ID: corr-789/)).not.toBeInTheDocument();
    });

    it('should toggle button icon when expanding/collapsing', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-789" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      expect(button.textContent).toContain('▶');
      
      fireEvent.click(button);
      expect(button.textContent).toContain('▼');
      
      fireEvent.click(button);
      expect(button.textContent).toContain('▶');
    });

    it('should render correlationId in monospace font when expanded', () => {
      const { container } = render(<ErrorBanner message="Test error" correlationId="corr-789" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      fireEvent.click(button);
      
      const correlationDiv = container.querySelector('div[style*="fontFamily: monospace"]');
      expect(correlationDiv).toBeInTheDocument();
      expect(correlationDiv).toHaveTextContent('Correlation ID: corr-789');
    });
  });

  describe('onDismiss callback', () => {
    it('should not render dismiss button when onDismiss is not provided', () => {
      render(<ErrorBanner message="Test error" />);
      expect(screen.queryByRole('button', { name: /Dismiss error/ })).not.toBeInTheDocument();
    });

    it('should render dismiss button when onDismiss is provided', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      expect(screen.getByRole('button', { name: /Dismiss error/ })).toBeInTheDocument();
    });

    it('should call onDismiss callback when dismiss button is clicked', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      const dismissButton = screen.getByRole('button', { name: /Dismiss error/ });
      fireEvent.click(dismissButton);
      
      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should log dismissal event when dismiss button is clicked', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} correlationId="corr-999" />);
      
      const dismissButton = screen.getByRole('button', { name: /Dismiss error/ });
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner dismissed', {
        event: 'error_banner_dismiss',
        correlation_id: 'corr-999'
      });
    });

    it('should log dismissal event with undefined correlationId when not provided', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      const dismissButton = screen.getByRole('button', { name: /Dismiss error/ });
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner dismissed', {
        event: 'error_banner_dismiss',
        correlation_id: undefined
      });
    });

    it('should call onDismiss before logging', () => {
      const callOrder: string[] = [];
      const onDismiss = jest.fn(() => callOrder.push('onDismiss'));
      (logger.info as jest.Mock).mockImplementation(() => callOrder.push('logger'));
      
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      const dismissButton = screen.getByRole('button', { name: /Dismiss error/ });
      fireEvent.click(dismissButton);
      
      expect(callOrder).toEqual(['logger', 'onDismiss']);
    });
  });

  describe('accessibility - ARIA attributes', () => {
    it('should have role="alert" attribute on banner container', () => {
      render(<ErrorBanner message="Test error" />);
      
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    it('should have aria-live="polite" attribute on banner container', () => {
      render(<ErrorBanner message="Test error" />);
      
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('aria-live', 'polite');
    });

    it('should have tabIndex=-1 for focus management', () => {
      render(<ErrorBanner message="Test error" />);
      
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('tabIndex', '-1');
    });

    it('should focus banner on mount', async () => {
      render(<ErrorBanner message="Test error" />);
      
      const banner = screen.getByRole('alert');
      await waitFor(() => {
        expect(banner).toHaveFocus();
      });
    });

    it('should have aria-label on dismiss button', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      const dismissButton = screen.getByRole('button', { name: 'Dismiss error' });
      expect(dismissButton).toHaveAttribute('aria-label', 'Dismiss error');
    });

    it('should have aria-expanded on technical details button', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-123" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      expect(button).toHaveAttribute('aria-expanded');
    });
  });

  describe('backward compatibility - existing prop interface', () => {
    it('should work with only message prop (LoginPage compatibility)', () => {
      render(<ErrorBanner message="Login failed" />);
      expect(screen.getByText('Login failed')).toBeInTheDocument();
    });

    it('should work with message and onDismiss props (LoginPage compatibility)', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Login failed" onDismiss={onDismiss} />);
      
      expect(screen.getByText('Login failed')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Dismiss error/ })).toBeInTheDocument();
    });

    it('should work with message, onDismiss, and correlationId props (LoginPage compatibility)', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Login failed" onDismiss={onDismiss} correlationId="login-123" />);
      
      expect(screen.getByText('Login failed')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Dismiss error/ })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Technical Details/ })).toBeInTheDocument();
    });

    it('should not require fieldErrors prop (optional)', () => {
      expect(() => {
        render(<ErrorBanner message="Test error" />);
      }).not.toThrow();
    });

    it('should work with all props including new fieldErrors prop', () => {
      const onDismiss = jest.fn();
      const fieldErrors = { username: 'Required' };
      
      render(
        <ErrorBanner 
          message="Validation failed" 
          onDismiss={onDismiss} 
          correlationId="val-123"
          fieldErrors={fieldErrors}
        />
      );
      
      expect(screen.getByText('Validation failed')).toBeInTheDocument();
      expect(screen.getByText('username:', { exact: false })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Dismiss error/ })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Technical Details/ })).toBeInTheDocument();
    });
  });

  describe('component styling and structure', () => {
    it('should render with error styling', () => {
      const { container } = render(<ErrorBanner message="Test error" />);
      
      const banner = container.querySelector('div[role="alert"]');
      expect(banner).toHaveStyle({
        backgroundColor: '#fee',
        color: '#c33',
        borderRadius: '4px',
        border: '1px solid #fcc'
      });
    });

    it('should render with proper layout structure', () => {
      const onDismiss = jest.fn();
      const { container } = render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      const flexContainer = container.querySelector('div[style*="display: flex"]');
      expect(flexContainer).toBeInTheDocument();
      expect(flexContainer).toHaveStyle({
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'flex-start'
      });
    });
  });

  describe('edge cases', () => {
    it('should handle empty string message', () => {
      render(<ErrorBanner message="" />);
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    it('should handle empty array message', () => {
      render(<ErrorBanner message={[]} />);
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    it('should handle very long error messages', () => {
      const longMessage = 'A'.repeat(1000);
      render(<ErrorBanner message={longMessage} />);
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    it('should handle special characters in field names', () => {
      const fieldErrors = { 'user.email': 'Invalid format' };
      render(<ErrorBanner message="Error" fieldErrors={fieldErrors} />);
      expect(screen.getByText('user.email:', { exact: false })).toBeInTheDocument();
    });

    it('should handle multiple clicks on technical details button', () => {
      render(<ErrorBanner message="Test error" correlationId="corr-123" />);
      
      const button = screen.getByRole('button', { name: /Technical Details/ });
      
      fireEvent.click(button);
      fireEvent.click(button);
      fireEvent.click(button);
      
      expect(button).toHaveAttribute('aria-expanded', 'true');
      expect(screen.getByText(/Correlation ID: corr-123/)).toBeInTheDocument();
    });

    it('should handle multiple clicks on dismiss button', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      
      const dismissButton = screen.getByRole('button', { name: /Dismiss error/ });
      
      fireEvent.click(dismissButton);
      fireEvent.click(dismissButton);
      fireEvent.click(dismissButton);
      
      expect(onDismiss).toHaveBeenCalledTimes(3);
    });
  });

  describe('useEffect dependencies', () => {
    it('should re-log error when message changes', () => {
      const { rerender } = render(<ErrorBanner message="Error 1" correlationId="corr-1" />);
      
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Error 2" correlationId="corr-1" />);
      
      expect(logger.error).toHaveBeenCalledTimes(2);
      expect(logger.error).toHaveBeenLastCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: 'Error 2',
        correlation_id: 'corr-1'
      });
    });

    it('should re-log error when correlationId changes', () => {
      const { rerender } = render(<ErrorBanner message="Error" correlationId="corr-1" />);
      
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Error" correlationId="corr-2" />);
      
      expect(logger.error).toHaveBeenCalledTimes(2);
      expect(logger.error).toHaveBeenLastCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: 'Error',
        correlation_id: 'corr-2'
      });
    });

    it('should not re-log error when fieldErrors changes', () => {
      const { rerender } = render(<ErrorBanner message="Error" fieldErrors={{ name: 'Required' }} />);
      
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Error" fieldErrors={{ email: 'Invalid' }} />);
      
      expect(logger.error).toHaveBeenCalledTimes(1);
    });
  });
});