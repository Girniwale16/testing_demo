import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import Toast from './Toast';
import logger from '../utils/logger';

jest.mock('../utils/logger', () => ({
  info: jest.fn(),
}));

describe('Toast Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('Component Rendering', () => {
    it('should render toast with message', () => {
      render(<Toast message="Test message" type="success" />);
      expect(screen.getByText('Test message')).toBeInTheDocument();
    });

    it('should render dismiss button with correct aria-label', () => {
      render(<Toast message="Test message" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      expect(dismissButton).toBeInTheDocument();
    });

    it('should return null when visible is false', () => {
      const { container } = render(<Toast message="Test message" type="success" duration={100} />);
      
      act(() => {
        jest.advanceTimersByTime(100);
      });

      expect(container.firstChild).toBeNull();
    });
  });

  describe('Toast Types and Accessibility', () => {
    it('should render success toast with correct role and aria-live', () => {
      render(<Toast message="Success" type="success" />);
      const toast = screen.getByRole('status');
      expect(toast).toHaveAttribute('aria-live', 'polite');
      expect(toast).toHaveAttribute('aria-atomic', 'true');
      expect(toast).toHaveClass('toast-success');
    });

    it('should render error toast with correct role and aria-live', () => {
      render(<Toast message="Error" type="error" />);
      const toast = screen.getByRole('alert');
      expect(toast).toHaveAttribute('aria-live', 'assertive');
      expect(toast).toHaveAttribute('aria-atomic', 'true');
      expect(toast).toHaveClass('toast-error');
    });

    it('should render info toast with correct role and aria-live', () => {
      render(<Toast message="Info" type="info" />);
      const toast = screen.getByRole('status');
      expect(toast).toHaveAttribute('aria-live', 'polite');
      expect(toast).toHaveAttribute('aria-atomic', 'true');
      expect(toast).toHaveClass('toast-info');
    });

    it('should render warning toast with correct role and aria-live', () => {
      render(<Toast message="Warning" type="warning" />);
      const toast = screen.getByRole('alert');
      expect(toast).toHaveAttribute('aria-live', 'assertive');
      expect(toast).toHaveAttribute('aria-atomic', 'true');
      expect(toast).toHaveClass('toast-warning');
    });
  });

  describe('Toast Styling', () => {
    it('should apply correct background color for success type', () => {
      render(<Toast message="Success" type="success" />);
      const toast = screen.getByRole('status');
      expect(toast).toHaveStyle({ backgroundColor: '#10b981' });
    });

    it('should apply correct background color for error type', () => {
      render(<Toast message="Error" type="error" />);
      const toast = screen.getByRole('alert');
      expect(toast).toHaveStyle({ backgroundColor: '#ef4444' });
    });

    it('should apply correct background color for info type', () => {
      render(<Toast message="Info" type="info" />);
      const toast = screen.getByRole('status');
      expect(toast).toHaveStyle({ backgroundColor: '#3b82f6' });
    });

    it('should apply correct background color for warning type', () => {
      render(<Toast message="Warning" type="warning" />);
      const toast = screen.getByRole('alert');
      expect(toast).toHaveStyle({ backgroundColor: '#f59e0b' });
    });

    it('should apply fixed positioning styles', () => {
      render(<Toast message="Test" type="success" />);
      const toast = screen.getByRole('status');
      expect(toast).toHaveStyle({
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: 2000,
      });
    });

    it('should apply correct padding and border radius', () => {
      render(<Toast message="Test" type="success" />);
      const toast = screen.getByRole('status');
      expect(toast).toHaveStyle({
        padding: '16px 24px',
        borderRadius: '8px',
      });
    });
  });

  describe('Auto-dismiss Functionality', () => {
    it('should auto-dismiss after default duration of 3000ms', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      
      expect(container.firstChild).not.toBeNull();
      
      act(() => {
        jest.advanceTimersByTime(3000);
      });

      expect(container.firstChild).toBeNull();
    });

    it('should auto-dismiss after custom duration', () => {
      const { container } = render(<Toast message="Test" type="success" duration={5000} />);
      
      expect(container.firstChild).not.toBeNull();
      
      act(() => {
        jest.advanceTimersByTime(4999);
      });
      expect(container.firstChild).not.toBeNull();
      
      act(() => {
        jest.advanceTimersByTime(1);
      });
      expect(container.firstChild).toBeNull();
    });

    it('should call onDismiss callback after auto-dismiss', () => {
      const onDismiss = jest.fn();
      render(<Toast message="Test" type="success" duration={1000} onDismiss={onDismiss} />);
      
      expect(onDismiss).not.toHaveBeenCalled();
      
      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should cleanup timer on unmount', () => {
      const onDismiss = jest.fn();
      const { unmount } = render(<Toast message="Test" type="success" duration={5000} onDismiss={onDismiss} />);
      
      unmount();
      
      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(onDismiss).not.toHaveBeenCalled();
    });
  });

  describe('Manual Dismiss Functionality', () => {
    it('should dismiss toast when dismiss button is clicked', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      fireEvent.click(dismissButton);
      
      expect(container.firstChild).toBeNull();
    });

    it('should call onDismiss callback when manually dismissed', () => {
      const onDismiss = jest.fn();
      render(<Toast message="Test" type="success" onDismiss={onDismiss} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      fireEvent.click(dismissButton);
      
      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should log dismissal with correlationId when provided', () => {
      const correlationId = 'test-correlation-id-123';
      render(<Toast message="Test message" type="success" correlationId={correlationId} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('Toast dismissed', {
        correlationId,
        type: 'success',
        message: 'Test message',
      });
    });

    it('should not log dismissal when correlationId is not provided', () => {
      render(<Toast message="Test message" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      fireEvent.click(dismissButton);
      
      expect(logger.info).not.toHaveBeenCalled();
    });

    it('should log with correct type for error toast', () => {
      const correlationId = 'error-id';
      render(<Toast message="Error message" type="error" correlationId={correlationId} />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('Toast dismissed', {
        correlationId,
        type: 'error',
        message: 'Error message',
      });
    });
  });

  describe('Keyboard Accessibility', () => {
    it('should dismiss toast when Enter key is pressed on dismiss button', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      dismissButton.focus();
      fireEvent.keyDown(dismissButton, { key: 'Enter', code: 'Enter' });
      fireEvent.click(dismissButton);
      
      expect(container.firstChild).toBeNull();
    });

    it('should dismiss toast when Space key is pressed on dismiss button', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      dismissButton.focus();
      fireEvent.keyDown(dismissButton, { key: ' ', code: 'Space' });
      fireEvent.click(dismissButton);
      
      expect(container.firstChild).toBeNull();
    });

    it('should be focusable', () => {
      render(<Toast message="Test" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      dismissButton.focus();
      
      expect(dismissButton).toHaveFocus();
    });
  });

  describe('Edge Cases', () => {
    it('should handle onDismiss being undefined', () => {
      const { container } = render(<Toast message="Test" type="success" duration={100} />);
      
      expect(() => {
        act(() => {
          jest.advanceTimersByTime(100);
        });
      }).not.toThrow();
      
      expect(container.firstChild).toBeNull();
    });

    it('should handle manual dismiss without onDismiss callback', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      expect(() => {
        fireEvent.click(dismissButton);
      }).not.toThrow();
      
      expect(container.firstChild).toBeNull();
    });

    it('should handle duration of 0', () => {
      const { container } = render(<Toast message="Test" type="success" duration={0} />);
      
      act(() => {
        jest.advanceTimersByTime(0);
      });

      expect(container.firstChild).toBeNull();
    });

    it('should handle very long duration', () => {
      const { container } = render(<Toast message="Test" type="success" duration={999999} />);
      
      act(() => {
        jest.advanceTimersByTime(999998);
      });
      expect(container.firstChild).not.toBeNull();
      
      act(() => {
        jest.advanceTimersByTime(1);
      });
      expect(container.firstChild).toBeNull();
    });

    it('should handle empty message', () => {
      render(<Toast message="" type="success" />);
      expect(screen.getByText('')).toBeInTheDocument();
    });

    it('should handle long message text', () => {
      const longMessage = 'A'.repeat(500);
      render(<Toast message={longMessage} type="success" />);
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });
  });

  describe('Component State Management', () => {
    it('should initialize with visible state as true', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      expect(container.firstChild).not.toBeNull();
    });

    it('should update visible state to false after duration', () => {
      const { container } = render(<Toast message="Test" type="success" duration={1000} />);
      
      expect(container.firstChild).not.toBeNull();
      
      act(() => {
        jest.advanceTimersByTime(1000);
      });
      
      expect(container.firstChild).toBeNull();
    });

    it('should update visible state to false on manual dismiss', () => {
      const { container } = render(<Toast message="Test" type="success" />);
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      expect(container.firstChild).not.toBeNull();
      
      fireEvent.click(dismissButton);
      
      expect(container.firstChild).toBeNull();
    });
  });

  describe('Integration Tests', () => {
    it('should handle both auto-dismiss and manual dismiss correctly', () => {
      const onDismiss = jest.fn();
      const { container } = render(
        <Toast message="Test" type="success" duration={5000} onDismiss={onDismiss} />
      );
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      
      fireEvent.click(dismissButton);
      
      expect(container.firstChild).toBeNull();
      expect(onDismiss).toHaveBeenCalledTimes(1);
      
      act(() => {
        jest.advanceTimersByTime(5000);
      });
      
      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should handle all props together', () => {
      const onDismiss = jest.fn();
      const correlationId = 'full-test-id';
      render(
        <Toast
          message="Full test message"
          type="warning"
          duration={2000}
          onDismiss={onDismiss}
          correlationId={correlationId}
        />
      );
      
      const toast = screen.getByRole('alert');
      expect(toast).toHaveAttribute('aria-live', 'assertive');
      expect(toast).toHaveClass('toast-warning');
      expect(screen.getByText('Full test message')).toBeInTheDocument();
      
      const dismissButton = screen.getByRole('button', { name: 'Dismiss notification' });
      fireEvent.click(dismissButton);
      
      expect(logger.info).toHaveBeenCalledWith('Toast dismissed', {
        correlationId,
        type: 'warning',
        message: 'Full test message',
      });
      expect(onDismiss).toHaveBeenCalledTimes(1);
    });
  });
});