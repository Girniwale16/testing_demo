import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ErrorBanner from './ErrorBanner';
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

  describe('Component Rendering', () => {
    it('should render with required message prop', () => {
      render(<ErrorBanner message="Test error message" />);
      expect(screen.getByText('Test error message')).toBeInTheDocument();
    });

    it('should render with message, onRetry, and onDismiss props', () => {
      const onRetry = jest.fn();
      const onDismiss = jest.fn();
      render(
        <ErrorBanner 
          message="Server error occurred" 
          onRetry={onRetry} 
          onDismiss={onDismiss} 
        />
      );
      expect(screen.getByText('Server error occurred')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Retry action' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Dismiss error' })).toBeInTheDocument();
    });

    it('should not render retry button when onRetry is not provided', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} />);
      expect(screen.queryByRole('button', { name: 'Retry action' })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Dismiss error' })).toBeInTheDocument();
    });

    it('should not render dismiss button when onDismiss is not provided', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} />);
      expect(screen.getByRole('button', { name: 'Retry action' })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Dismiss error' })).not.toBeInTheDocument();
    });

    it('should render without any buttons when neither onRetry nor onDismiss provided', () => {
      render(<ErrorBanner message="Error" />);
      expect(screen.queryByRole('button', { name: 'Retry action' })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Dismiss error' })).not.toBeInTheDocument();
    });
  });

  describe('5xx Error Message Support', () => {
    it('should display 5xx error message with retry action', () => {
      const onRetry = jest.fn();
      render(
        <ErrorBanner 
          message="500 Internal Server Error - Please try again" 
          onRetry={onRetry} 
        />
      );
      expect(screen.getByText('500 Internal Server Error - Please try again')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Retry action' })).toBeInTheDocument();
    });

    it('should display 503 Service Unavailable error with retry', () => {
      const onRetry = jest.fn();
      render(
        <ErrorBanner 
          message="503 Service Unavailable" 
          onRetry={onRetry} 
        />
      );
      expect(screen.getByText('503 Service Unavailable')).toBeInTheDocument();
    });
  });

  describe('Error Severity Levels', () => {
    it('should render with default error severity', () => {
      const { container } = render(<ErrorBanner message="Error message" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ backgroundColor: '#fee', color: '#c33' });
    });

    it('should render with error severity explicitly set', () => {
      const { container } = render(<ErrorBanner message="Error message" severity="error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ backgroundColor: '#fee', color: '#c33' });
    });

    it('should render with warning severity', () => {
      const { container } = render(<ErrorBanner message="Warning message" severity="warning" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ backgroundColor: '#fff3cd', color: '#856404' });
    });

    it('should render with info severity', () => {
      const { container } = render(<ErrorBanner message="Info message" severity="info" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ backgroundColor: '#d1ecf1', color: '#0c5460' });
    });

    it('should apply correct border color for error severity', () => {
      const { container } = render(<ErrorBanner message="Error" severity="error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ border: '1px solid #fcc' });
    });

    it('should apply correct border color for warning severity', () => {
      const { container } = render(<ErrorBanner message="Warning" severity="warning" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ border: '1px solid #ffeaa7' });
    });

    it('should apply correct border color for info severity', () => {
      const { container } = render(<ErrorBanner message="Info" severity="info" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ border: '1px solid #bee5eb' });
    });
  });

  describe('Button Interactions', () => {
    it('should call onRetry when retry button is clicked', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      fireEvent.click(retryButton);
      expect(onRetry).toHaveBeenCalledTimes(1);
    });

    it('should call onDismiss when dismiss button is clicked', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss error' });
      fireEvent.click(dismissButton);
      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should call both handlers independently', () => {
      const onRetry = jest.fn();
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} onDismiss={onDismiss} />);
      
      fireEvent.click(screen.getByRole('button', { name: 'Retry action' }));
      expect(onRetry).toHaveBeenCalledTimes(1);
      expect(onDismiss).not.toHaveBeenCalled();

      fireEvent.click(screen.getByRole('button', { name: 'Dismiss error' }));
      expect(onDismiss).toHaveBeenCalledTimes(1);
      expect(onRetry).toHaveBeenCalledTimes(1);
    });
  });

  describe('Accessibility - WCAG 2.1 AA Compliance', () => {
    it('should have role="alert" for screen readers', () => {
      render(<ErrorBanner message="Error" />);
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    it('should have aria-live="assertive" for immediate announcement', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveAttribute('aria-live', 'assertive');
    });

    it('should have aria-atomic="true" for complete message reading', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveAttribute('aria-atomic', 'true');
    });

    it('should have tabIndex=-1 for programmatic focus', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveAttribute('tabIndex', '-1');
    });

    it('should focus banner on mount', async () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      await waitFor(() => {
        expect(document.activeElement).toBe(banner);
      });
    });

    it('should have aria-label on retry button', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      expect(retryButton).toHaveAttribute('aria-label', 'Retry action');
    });

    it('should have aria-label on dismiss button', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss error' });
      expect(dismissButton).toHaveAttribute('aria-label', 'Dismiss error');
    });

    it('should meet minimum touch target size of 44x44px for banner', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ minHeight: '44px' });
    });

    it('should meet minimum touch target size of 44x44px for retry button', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      expect(retryButton).toHaveStyle({ minWidth: '44px', minHeight: '44px' });
    });

    it('should meet minimum touch target size of 44x44px for dismiss button', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss error' });
      expect(dismissButton).toHaveStyle({ minWidth: '44px', minHeight: '44px' });
    });
  });

  describe('Logging Functionality', () => {
    it('should log error when banner is displayed', () => {
      render(<ErrorBanner message="Test error" />);
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: 'Test error',
        correlation_id: undefined,
        severity: 'error'
      });
    });

    it('should log error with correlationId when provided', () => {
      render(<ErrorBanner message="Test error" correlationId="abc-123" />);
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: 'Test error',
        correlation_id: 'abc-123',
        severity: 'error'
      });
    });

    it('should log error with severity when provided', () => {
      render(<ErrorBanner message="Test warning" severity="warning" />);
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message: 'Test warning',
        correlation_id: undefined,
        severity: 'warning'
      });
    });

    it('should log info when retry button is clicked', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} correlationId="xyz-789" />);
      fireEvent.click(screen.getByRole('button', { name: 'Retry action' }));
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner retry clicked', {
        event: 'error_banner_retry',
        correlation_id: 'xyz-789'
      });
    });

    it('should log info when dismiss button is clicked', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} correlationId="xyz-789" />);
      fireEvent.click(screen.getByRole('button', { name: 'Dismiss error' }));
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner dismissed', {
        event: 'error_banner_dismiss',
        correlation_id: 'xyz-789'
      });
    });

    it('should log retry without correlationId when not provided', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} />);
      fireEvent.click(screen.getByRole('button', { name: 'Retry action' }));
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner retry clicked', {
        event: 'error_banner_retry',
        correlation_id: undefined
      });
    });

    it('should log dismiss without correlationId when not provided', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} />);
      fireEvent.click(screen.getByRole('button', { name: 'Dismiss error' }));
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner dismissed', {
        event: 'error_banner_dismiss',
        correlation_id: undefined
      });
    });
  });

  describe('Backward Compatibility', () => {
    it('should work with legacy props (message only)', () => {
      render(<ErrorBanner message="Legacy error" />);
      expect(screen.getByText('Legacy error')).toBeInTheDocument();
    });

    it('should work with legacy props (message and onRetry)', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Legacy error" onRetry={onRetry} />);
      expect(screen.getByText('Legacy error')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Retry action' })).toBeInTheDocument();
    });

    it('should work with legacy props (message and onDismiss)', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Legacy error" onDismiss={onDismiss} />);
      expect(screen.getByText('Legacy error')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Dismiss error' })).toBeInTheDocument();
    });

    it('should default to error severity when not specified', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ backgroundColor: '#fee' });
    });

    it('should work without correlationId', () => {
      render(<ErrorBanner message="Error without correlation" />);
      expect(screen.getByText('Error without correlation')).toBeInTheDocument();
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', expect.objectContaining({
        correlation_id: undefined
      }));
    });
  });

  describe('Visual Styling', () => {
    it('should apply correct padding and margin', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ padding: '1rem', marginBottom: '1rem' });
    });

    it('should apply correct border radius', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ borderRadius: '4px' });
    });

    it('should use flexbox layout', () => {
      const { container } = render(<ErrorBanner message="Error" />);
      const banner = container.firstChild as HTMLElement;
      expect(banner).toHaveStyle({ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center' 
      });
    });

    it('should apply correct button styling for retry button', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} severity="error" />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      expect(retryButton).toHaveStyle({
        padding: '0.5rem 1rem',
        backgroundColor: '#c33',
        border: 'none',
        borderRadius: '4px',
        color: '#fff',
        cursor: 'pointer',
        fontSize: '0.875rem',
        fontWeight: '500'
      });
    });

    it('should apply correct button styling for dismiss button', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} severity="error" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss error' });
      expect(dismissButton).toHaveStyle({
        padding: '0.5rem 1rem',
        backgroundColor: 'transparent',
        border: '1px solid #c33',
        borderRadius: '4px',
        color: '#c33',
        cursor: 'pointer',
        fontSize: '0.875rem',
        fontWeight: '500'
      });
    });

    it('should apply correct button colors for warning severity', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Warning" onRetry={onRetry} severity="warning" />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      expect(retryButton).toHaveStyle({ backgroundColor: '#856404' });
    });

    it('should apply correct button colors for info severity', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Info" onRetry={onRetry} severity="info" />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      expect(retryButton).toHaveStyle({ backgroundColor: '#0c5460' });
    });
  });

  describe('Component Re-rendering', () => {
    it('should re-log when message changes', () => {
      const { rerender } = render(<ErrorBanner message="First error" />);
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Second error" />);
      expect(logger.error).toHaveBeenCalledTimes(2);
      expect(logger.error).toHaveBeenLastCalledWith('ErrorBanner displayed', expect.objectContaining({
        message: 'Second error'
      }));
    });

    it('should re-log when correlationId changes', () => {
      const { rerender } = render(<ErrorBanner message="Error" correlationId="id-1" />);
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Error" correlationId="id-2" />);
      expect(logger.error).toHaveBeenCalledTimes(2);
      expect(logger.error).toHaveBeenLastCalledWith('ErrorBanner displayed', expect.objectContaining({
        correlation_id: 'id-2'
      }));
    });

    it('should re-log when severity changes', () => {
      const { rerender } = render(<ErrorBanner message="Error" severity="error" />);
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Error" severity="warning" />);
      expect(logger.error).toHaveBeenCalledTimes(2);
      expect(logger.error).toHaveBeenLastCalledWith('ErrorBanner displayed', expect.objectContaining({
        severity: 'warning'
      }));
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty message string', () => {
      render(<ErrorBanner message="" />);
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    it('should handle very long error messages', () => {
      const longMessage = 'A'.repeat(500);
      render(<ErrorBanner message={longMessage} />);
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    it('should handle special characters in message', () => {
      const specialMessage = '<script>alert("xss")</script>';
      render(<ErrorBanner message={specialMessage} />);
      expect(screen.getByText(specialMessage)).toBeInTheDocument();
    });

    it('should handle undefined correlationId gracefully', () => {
      render(<ErrorBanner message="Error" correlationId={undefined} />);
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', expect.objectContaining({
        correlation_id: undefined
      }));
    });

    it('should handle multiple rapid clicks on retry button', () => {
      const onRetry = jest.fn();
      render(<ErrorBanner message="Error" onRetry={onRetry} />);
      const retryButton = screen.getByRole('button', { name: 'Retry action' });
      
      fireEvent.click(retryButton);
      fireEvent.click(retryButton);
      fireEvent.click(retryButton);
      
      expect(onRetry).toHaveBeenCalledTimes(3);
    });

    it('should handle multiple rapid clicks on dismiss button', () => {
      const onDismiss = jest.fn();
      render(<ErrorBanner message="Error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss error' });
      
      fireEvent.click(dismissButton);
      fireEvent.click(dismissButton);
      fireEvent.click(dismissButton);
      
      expect(onDismiss).toHaveBeenCalledTimes(3);
    });
  });
});