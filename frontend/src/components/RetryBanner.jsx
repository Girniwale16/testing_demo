import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import RetryBanner from './RetryBanner';

describe('RetryBanner Component', () => {
  const mockOnRetry = jest.fn();
  const defaultMessage = 'Failed to load staff data. Please try again.';

  beforeEach(() => {
    mockOnRetry.mockClear();
  });

  describe('Component Rendering', () => {
    test('should render the RetryBanner component', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    test('should render with correct message prop', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      expect(screen.getByText(defaultMessage)).toBeInTheDocument();
    });

    test('should render with custom message', () => {
      const customMessage = 'Network error occurred';
      render(<RetryBanner onRetry={mockOnRetry} message={customMessage} />);
      expect(screen.getByText(customMessage)).toBeInTheDocument();
    });

    test('should render error icon', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const icon = screen.getByRole('img', { name: 'Error icon' });
      expect(icon).toBeInTheDocument();
      expect(icon).toHaveTextContent('⚠️');
    });

    test('should render Retry button', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      expect(button).toBeInTheDocument();
    });
  });

  describe('Accessibility Attributes', () => {
    test('should have role="alert" attribute', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('role', 'alert');
    });

    test('should have aria-live="assertive" attribute', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('aria-live', 'assertive');
    });

    test('should have aria-label="Server error - retry available"', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveAttribute('aria-label', 'Server error - retry available');
    });

    test('should have className="retry-banner"', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveClass('retry-banner');
    });

    test('should have aria-label on error icon', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const icon = screen.getByRole('img', { name: 'Error icon' });
      expect(icon).toHaveAttribute('aria-label', 'Error icon');
    });
  });

  describe('Button Interaction', () => {
    test('should call onRetry callback when Retry button is clicked', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      fireEvent.click(button);
      expect(mockOnRetry).toHaveBeenCalledTimes(1);
    });

    test('should call onRetry callback multiple times on multiple clicks', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      fireEvent.click(button);
      fireEvent.click(button);
      fireEvent.click(button);
      expect(mockOnRetry).toHaveBeenCalledTimes(3);
    });

    test('should not call onRetry if button is not clicked', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      expect(mockOnRetry).not.toHaveBeenCalled();
    });
  });

  describe('Button Hover Interactions', () => {
    test('should change button background color on mouse over', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      
      expect(button).toHaveStyle({ backgroundColor: '#ffffff' });
      
      fireEvent.mouseOver(button);
      expect(button).toHaveStyle({ backgroundColor: '#ffebee' });
      expect(button).toHaveStyle({ transform: 'scale(1.05)' });
    });

    test('should reset button background color on mouse out', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      
      fireEvent.mouseOver(button);
      expect(button).toHaveStyle({ backgroundColor: '#ffebee' });
      
      fireEvent.mouseOut(button);
      expect(button).toHaveStyle({ backgroundColor: '#ffffff' });
      expect(button).toHaveStyle({ transform: 'scale(1)' });
    });
  });

  describe('Button Focus Interactions', () => {
    test('should add outline on button focus', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      
      fireEvent.focus(button);
      expect(button).toHaveStyle({ outline: '2px solid #ffffff' });
      expect(button).toHaveStyle({ outlineOffset: '2px' });
    });

    test('should remove outline on button blur', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      
      fireEvent.focus(button);
      expect(button).toHaveStyle({ outline: '2px solid #ffffff' });
      
      fireEvent.blur(button);
      expect(button).toHaveStyle({ outline: 'none' });
    });
  });

  describe('Styling', () => {
    test('should have correct banner background color', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ backgroundColor: '#f44336' });
    });

    test('should have correct banner text color', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ color: '#ffffff' });
    });

    test('should have correct banner padding', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ padding: '16px 24px' });
    });

    test('should have correct banner border', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ border: '2px solid #d32f2f' });
    });

    test('should have correct banner border radius', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ borderRadius: '4px' });
    });

    test('should have flexbox display', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ display: 'flex' });
      expect(banner).toHaveStyle({ alignItems: 'center' });
      expect(banner).toHaveStyle({ justifyContent: 'space-between' });
    });

    test('should have correct margin bottom', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ marginBottom: '20px' });
    });

    test('should have box shadow', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = screen.getByRole('alert');
      expect(banner).toHaveStyle({ boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)' });
    });

    test('should have correct button initial styles', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const button = screen.getByRole('button', { name: 'Retry' });
      expect(button).toHaveStyle({
        backgroundColor: '#ffffff',
        color: '#f44336',
        border: '2px solid #ffffff',
        borderRadius: '4px',
        padding: '8px 16px',
        fontSize: '14px',
        fontWeight: '600',
        cursor: 'pointer'
      });
    });
  });

  describe('Props Validation', () => {
    test('should handle empty message string', () => {
      render(<RetryBanner onRetry={mockOnRetry} message="" />);
      const banner = screen.getByRole('alert');
      expect(banner).toBeInTheDocument();
    });

    test('should handle very long message', () => {
      const longMessage = 'A'.repeat(500);
      render(<RetryBanner onRetry={mockOnRetry} message={longMessage} />);
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    test('should handle special characters in message', () => {
      const specialMessage = 'Error: <script>alert("test")</script> & special chars!';
      render(<RetryBanner onRetry={mockOnRetry} message={specialMessage} />);
      expect(screen.getByText(specialMessage)).toBeInTheDocument();
    });
  });

  describe('Component Structure', () => {
    test('should have correct DOM structure with outer div and inner content div', () => {
      const { container } = render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const banner = container.querySelector('.retry-banner');
      expect(banner).toBeInTheDocument();
      expect(banner.children).toHaveLength(2);
    });

    test('should render message and icon in same container', () => {
      render(<RetryBanner onRetry={mockOnRetry} message={defaultMessage} />);
      const icon = screen.getByRole('img', { name: 'Error icon' });
      const message = screen.getByText(defaultMessage);
      expect(icon.parentElement).toBe(message.parentElement);
    });
  });

  describe('Export', () => {
    test('should export RetryBanner as default export', () => {
      expect(RetryBanner).toBeDefined();
      expect(typeof RetryBanner).toBe('function');
    });
  });
});