import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import ErrorBanner from './ErrorBanner';
import { logger } from '../utils/logger';

vi.mock('../utils/logger', () => ({
  logger: {
    error: vi.fn(),
    info: vi.fn()
  }
}));

describe('ErrorBanner', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Accessibility - role and aria attributes', () => {
    it('should have role="alert" attribute on container div', () => {
      render(<ErrorBanner message="Test error message" />);
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    it('should have aria-live="polite" attribute on container div', () => {
      render(<ErrorBanner message="Test error message" />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('aria-live', 'polite');
    });

    it('should have tabIndex=-1 for programmatic focus', () => {
      render(<ErrorBanner message="Test error message" />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('tabIndex', '-1');
    });

    it('should have aria-label on dismiss button', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      expect(dismissButton).toHaveAttribute('aria-label', 'Dismiss error message');
    });
  });

  describe('Accessibility - Touch target size compliance', () => {
    it('should have minimum 44x44px touch target size on dismiss button', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      const styles = window.getComputedStyle(dismissButton);
      
      expect(dismissButton).toHaveStyle({ minWidth: '44px' });
      expect(dismissButton).toHaveStyle({ minHeight: '44px' });
    });
  });

  describe('Accessibility - Color contrast', () => {
    it('should render error message text with proper color for contrast', () => {
      render(<ErrorBanner message="Test error message" />);
      const messageSpan = screen.getByText('Test error message');
      expect(messageSpan).toHaveStyle({ color: '#c33' });
    });

    it('should render container with appropriate background color for contrast', () => {
      render(<ErrorBanner message="Test error message" />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ backgroundColor: '#fee' });
      expect(banner).toHaveStyle({ color: '#c33' });
    });
  });

  describe('Error message display', () => {
    it('should render error message text', () => {
      const message = 'Something went wrong';
      render(<ErrorBanner message={message} />);
      expect(screen.getByText(message)).toBeInTheDocument();
    });

    it('should render error message wrapped in accessible span element', () => {
      const message = 'Test error';
      render(<ErrorBanner message={message} />);
      const messageElement = screen.getByText(message);
      expect(messageElement.tagName).toBe('SPAN');
    });

    it('should update when message prop changes', () => {
      const { rerender } = render(<ErrorBanner message="First error" />);
      expect(screen.getByText('First error')).toBeInTheDocument();
      
      rerender(<ErrorBanner message="Second error" />);
      expect(screen.getByText('Second error')).toBeInTheDocument();
      expect(screen.queryByText('First error')).not.toBeInTheDocument();
    });
  });

  describe('Dismiss functionality', () => {
    it('should render dismiss button when onDismiss prop is provided', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      expect(screen.getByRole('button', { name: /dismiss error message/i })).toBeInTheDocument();
    });

    it('should not render dismiss button when onDismiss prop is not provided', () => {
      render(<ErrorBanner message="Test error" />);
      expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });

    it('should call onDismiss callback when dismiss button is clicked', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      
      fireEvent.click(dismissButton);
      
      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should log dismiss event when dismiss button is clicked', () => {
      const onDismiss = vi.fn();
      const correlationId = 'test-correlation-123';
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} correlationId={correlationId} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner dismissed', {
        event: 'error_banner_dismiss',
        correlation_id: correlationId
      });
    });

    it('should log dismiss event without correlationId when not provided', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('ErrorBanner dismissed', {
        event: 'error_banner_dismiss',
        correlation_id: undefined
      });
    });
  });

  describe('Logging functionality', () => {
    it('should log error when banner is displayed', () => {
      const message = 'Test error message';
      const correlationId = 'test-correlation-456';
      
      render(<ErrorBanner message={message} correlationId={correlationId} />);
      
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message,
        correlation_id: correlationId
      });
    });

    it('should log error without correlationId when not provided', () => {
      const message = 'Test error message';
      
      render(<ErrorBanner message={message} />);
      
      expect(logger.error).toHaveBeenCalledWith('ErrorBanner displayed', {
        event: 'error_banner_display',
        message,
        correlation_id: undefined
      });
    });

    it('should log error again when message changes', () => {
      const { rerender } = render(<ErrorBanner message="First error" />);
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Second error" />);
      expect(logger.error).toHaveBeenCalledTimes(2);
    });

    it('should log error again when correlationId changes', () => {
      const { rerender } = render(<ErrorBanner message="Test error" correlationId="id-1" />);
      expect(logger.error).toHaveBeenCalledTimes(1);
      
      rerender(<ErrorBanner message="Test error" correlationId="id-2" />);
      expect(logger.error).toHaveBeenCalledTimes(2);
    });
  });

  describe('Focus management', () => {
    it('should focus the banner element on mount', async () => {
      render(<ErrorBanner message="Test error" />);
      const banner = screen.getByRole('alert');
      
      await waitFor(() => {
        expect(document.activeElement).toBe(banner);
      });
    });

    it('should focus the banner element when message changes', async () => {
      const { rerender } = render(<ErrorBanner message="First error" />);
      const banner = screen.getByRole('alert');
      
      await waitFor(() => {
        expect(document.activeElement).toBe(banner);
      });
      
      // Blur the element
      (banner as HTMLElement).blur();
      expect(document.activeElement).not.toBe(banner);
      
      // Rerender with new message
      rerender(<ErrorBanner message="Second error" />);
      
      await waitFor(() => {
        expect(document.activeElement).toBe(banner);
      });
    });
  });

  describe('Props interface compatibility', () => {
    it('should accept message prop', () => {
      expect(() => render(<ErrorBanner message="Test" />)).not.toThrow();
    });

    it('should accept optional onDismiss prop', () => {
      const onDismiss = vi.fn();
      expect(() => render(<ErrorBanner message="Test" onDismiss={onDismiss} />)).not.toThrow();
    });

    it('should accept optional correlationId prop', () => {
      expect(() => render(<ErrorBanner message="Test" correlationId="test-id" />)).not.toThrow();
    });

    it('should work with all props provided', () => {
      const onDismiss = vi.fn();
      expect(() => render(
        <ErrorBanner 
          message="Test" 
          onDismiss={onDismiss} 
          correlationId="test-id" 
        />
      )).not.toThrow();
    });

    it('should work with only required message prop', () => {
      expect(() => render(<ErrorBanner message="Test" />)).not.toThrow();
    });
  });

  describe('Styling and layout', () => {
    it('should apply correct container styles', () => {
      render(<ErrorBanner message="Test error" />);
      const banner = screen.getByRole('alert');
      
      expect(banner).toHaveStyle({
        padding: '1rem',
        marginBottom: '1rem',
        backgroundColor: '#fee',
        color: '#c33',
        borderRadius: '4px',
        border: '1px solid #fcc',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      });
    });

    it('should apply correct dismiss button styles', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      
      expect(dismissButton).toHaveStyle({
        marginLeft: '1rem',
        padding: '0.5rem 0.75rem',
        minWidth: '44px',
        minHeight: '44px',
        backgroundColor: 'transparent',
        border: '1px solid #c33',
        borderRadius: '4px',
        color: '#c33',
        cursor: 'pointer',
        fontSize: '0.875rem'
      });
    });
  });

  describe('Integration scenarios', () => {
    it('should handle rapid dismiss clicks gracefully', () => {
      const onDismiss = vi.fn();
      render(<ErrorBanner message="Test error" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: /dismiss error message/i });
      
      fireEvent.click(dismissButton);
      fireEvent.click(dismissButton);
      fireEvent.click(dismissButton);
      
      expect(onDismiss).toHaveBeenCalledTimes(3);
      expect(logger.info).toHaveBeenCalledTimes(3);
    });

    it('should maintain error display pattern for LoginPage compatibility', () => {
      const message = 'Invalid credentials';
      const onDismiss = vi.fn();
      
      render(<ErrorBanner message={message} onDismiss={onDismiss} />);
      
      expect(screen.getByText(message)).toBeInTheDocument();
      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /dismiss error message/i })).toBeInTheDocument();
    });

    it('should work correctly without onDismiss for non-dismissible errors', () => {
      const message = 'Critical system error';
      
      render(<ErrorBanner message={message} />);
      
      expect(screen.getByText(message)).toBeInTheDocument();
      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });
  });
});