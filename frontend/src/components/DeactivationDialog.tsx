import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import DeactivationDialog from './DeactivationDialog';

describe('DeactivationDialog', () => {
  const mockOnClose = jest.fn();
  const mockOnConfirm = jest.fn();
  const defaultProps = {
    isOpen: true,
    onClose: mockOnClose,
    onConfirm: mockOnConfirm,
    staffName: 'John Doe',
    isLoading: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    document.body.style.overflow = '';
  });

  afterEach(() => {
    document.body.style.overflow = '';
  });

  describe('Component Rendering', () => {
    it('should render dialog when isOpen is true', () => {
      render(<DeactivationDialog {...defaultProps} />);
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('should not render dialog when isOpen is false', () => {
      render(<DeactivationDialog {...defaultProps} isOpen={false} />);
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('should render dialog title with correct id', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const title = screen.getByText('Confirm Deactivation');
      expect(title).toBeInTheDocument();
      expect(title).toHaveAttribute('id', 'deactivation-dialog-title');
    });

    it('should render dialog description with correct id and staff name', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const description = screen.getByText(/Are you sure you want to deactivate John Doe/);
      expect(description).toBeInTheDocument();
      expect(description).toHaveAttribute('id', 'deactivation-dialog-description');
    });

    it('should render Cancel button with correct aria-label', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      expect(cancelButton).toBeInTheDocument();
      expect(cancelButton).toHaveTextContent('Cancel');
    });

    it('should render Confirm button with correct aria-label', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      expect(confirmButton).toBeInTheDocument();
      expect(confirmButton).toHaveTextContent('Confirm');
    });
  });

  describe('ARIA Attributes', () => {
    it('should have role="dialog" attribute', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('role', 'dialog');
    });

    it('should have aria-modal="true" attribute', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-modal', 'true');
    });

    it('should have aria-labelledby pointing to title element', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-labelledby', 'deactivation-dialog-title');
    });

    it('should have aria-describedby pointing to description element', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-describedby', 'deactivation-dialog-description');
    });

    it('should have aria-busy="false" when not loading', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={false} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-busy', 'false');
    });

    it('should have aria-busy="true" when loading', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={true} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-busy', 'true');
    });
  });

  describe('Button Interactions', () => {
    it('should call onClose when Cancel button is clicked', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      fireEvent.click(cancelButton);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should call onConfirm when Confirm button is clicked', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      fireEvent.click(confirmButton);
      expect(mockOnConfirm).toHaveBeenCalledTimes(1);
    });

    it('should disable Cancel button when isLoading is true', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={true} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      expect(cancelButton).toBeDisabled();
    });

    it('should disable Confirm button when isLoading is true', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={true} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      expect(confirmButton).toBeDisabled();
    });

    it('should display "Deactivating..." text on Confirm button when loading', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={true} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      expect(confirmButton).toHaveTextContent('Deactivating...');
    });

    it('should display "Confirm" text on Confirm button when not loading', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={false} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      expect(confirmButton).toHaveTextContent('Confirm');
    });
  });

  describe('Keyboard Event Handling', () => {
    it('should call onClose when Escape key is pressed', () => {
      render(<DeactivationDialog {...defaultProps} />);
      fireEvent.keyDown(document, { key: 'Escape' });
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should not call onClose when Escape key is pressed and dialog is closed', () => {
      render(<DeactivationDialog {...defaultProps} isOpen={false} />);
      fireEvent.keyDown(document, { key: 'Escape' });
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it('should call onConfirm when Enter key is pressed on confirm button', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      confirmButton.focus();
      fireEvent.keyDown(document, { key: 'Enter' });
      expect(mockOnConfirm).toHaveBeenCalledTimes(1);
    });

    it('should not call onConfirm when Enter key is pressed on non-confirm button', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      cancelButton.focus();
      fireEvent.keyDown(document, { key: 'Enter' });
      expect(mockOnConfirm).not.toHaveBeenCalled();
    });
  });

  describe('Focus Management', () => {
    it('should store reference to trigger element when dialog opens', () => {
      const button = document.createElement('button');
      document.body.appendChild(button);
      button.focus();

      const { rerender } = render(<DeactivationDialog {...defaultProps} isOpen={false} />);
      rerender(<DeactivationDialog {...defaultProps} isOpen={true} />);

      expect(document.activeElement).not.toBe(button);
      document.body.removeChild(button);
    });

    it('should return focus to trigger element when dialog closes', async () => {
      const button = document.createElement('button');
      document.body.appendChild(button);
      button.focus();

      const { rerender } = render(<DeactivationDialog {...defaultProps} isOpen={false} />);
      rerender(<DeactivationDialog {...defaultProps} isOpen={true} />);
      rerender(<DeactivationDialog {...defaultProps} isOpen={false} />);

      await waitFor(() => {
        expect(document.activeElement).toBe(button);
      });

      document.body.removeChild(button);
    });

    it('should focus first focusable element when dialog opens', async () => {
      render(<DeactivationDialog {...defaultProps} />);
      
      await waitFor(() => {
        const cancelButton = screen.getByLabelText('Cancel deactivation');
        expect(document.activeElement).toBe(cancelButton);
      });
    });
  });

  describe('Focus Trap', () => {
    it('should trap focus within dialog when Tab is pressed on last element', async () => {
      render(<DeactivationDialog {...defaultProps} />);
      
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      confirmButton.focus();

      fireEvent.keyDown(confirmButton.closest('.dialog-container')!, { key: 'Tab' });

      await waitFor(() => {
        const cancelButton = screen.getByLabelText('Cancel deactivation');
        expect(document.activeElement).toBe(cancelButton);
      });
    });

    it('should trap focus within dialog when Shift+Tab is pressed on first element', async () => {
      render(<DeactivationDialog {...defaultProps} />);
      
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      cancelButton.focus();

      fireEvent.keyDown(cancelButton.closest('.dialog-container')!, { key: 'Tab', shiftKey: true });

      await waitFor(() => {
        const confirmButton = screen.getByLabelText('Confirm deactivation');
        expect(document.activeElement).toBe(confirmButton);
      });
    });
  });

  describe('Body Scroll Prevention', () => {
    it('should set body overflow to hidden when dialog opens', () => {
      render(<DeactivationDialog {...defaultProps} isOpen={true} />);
      expect(document.body.style.overflow).toBe('hidden');
    });

    it('should restore body overflow when dialog closes', () => {
      const { rerender } = render(<DeactivationDialog {...defaultProps} isOpen={true} />);
      expect(document.body.style.overflow).toBe('hidden');
      
      rerender(<DeactivationDialog {...defaultProps} isOpen={false} />);
      expect(document.body.style.overflow).toBe('');
    });

    it('should restore body overflow on unmount', () => {
      const { unmount } = render(<DeactivationDialog {...defaultProps} isOpen={true} />);
      expect(document.body.style.overflow).toBe('hidden');
      
      unmount();
      expect(document.body.style.overflow).toBe('');
    });
  });

  describe('Backdrop Click Handling', () => {
    it('should call onClose when backdrop is clicked', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const backdrop = screen.getByRole('dialog').parentElement;
      fireEvent.click(backdrop!);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should not call onClose when dialog content is clicked', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      fireEvent.click(dialog);
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe('Touch Target Size', () => {
    it('should have minimum 44x44px touch target for Cancel button', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      const styles = window.getComputedStyle(cancelButton);
      expect(styles.minWidth).toBe('44px');
      expect(styles.minHeight).toBe('44px');
    });

    it('should have minimum 44x44px touch target for Confirm button', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      const styles = window.getComputedStyle(confirmButton);
      expect(styles.minWidth).toBe('44px');
      expect(styles.minHeight).toBe('44px');
    });
  });

  describe('Responsive Styling', () => {
    it('should render with full-screen styling on mobile', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      const styles = window.getComputedStyle(dialog);
      expect(styles.width).toBe('100vw');
      expect(styles.height).toBe('100vh');
    });

    it('should have dialog-container class for responsive styling', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveClass('dialog-container');
    });
  });

  describe('Loading State', () => {
    it('should show loading state on buttons when isLoading is true', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={true} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      
      expect(cancelButton).toBeDisabled();
      expect(confirmButton).toBeDisabled();
      expect(confirmButton).toHaveTextContent('Deactivating...');
    });

    it('should apply reduced opacity to buttons when loading', () => {
      render(<DeactivationDialog {...defaultProps} isLoading={true} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      
      const cancelStyles = window.getComputedStyle(cancelButton);
      const confirmStyles = window.getComputedStyle(confirmButton);
      
      expect(cancelStyles.opacity).toBe('0.6');
      expect(confirmStyles.opacity).toBe('0.6');
    });
  });

  describe('Staff Name Display', () => {
    it('should display correct staff name in description', () => {
      render(<DeactivationDialog {...defaultProps} staffName="Jane Smith" />);
      expect(screen.getByText(/Jane Smith/)).toBeInTheDocument();
    });

    it('should update staff name when prop changes', () => {
      const { rerender } = render(<DeactivationDialog {...defaultProps} staffName="John Doe" />);
      expect(screen.getByText(/John Doe/)).toBeInTheDocument();
      
      rerender(<DeactivationDialog {...defaultProps} staffName="Jane Smith" />);
      expect(screen.getByText(/Jane Smith/)).toBeInTheDocument();
    });
  });

  describe('Confirm Button Data Attribute', () => {
    it('should have data-confirm-button="true" attribute on Confirm button', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const confirmButton = screen.getByLabelText('Confirm deactivation');
      expect(confirmButton).toHaveAttribute('data-confirm-button', 'true');
    });

    it('should not have data-confirm-button attribute on Cancel button', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const cancelButton = screen.getByLabelText('Cancel deactivation');
      expect(cancelButton).not.toHaveAttribute('data-confirm-button');
    });
  });

  describe('Dialog Backdrop Styling', () => {
    it('should have dialog-backdrop class', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const backdrop = screen.getByRole('dialog').parentElement;
      expect(backdrop).toHaveClass('dialog-backdrop');
    });

    it('should have fixed positioning with full viewport coverage', () => {
      render(<DeactivationDialog {...defaultProps} />);
      const backdrop = screen.getByRole('dialog').parentElement;
      const styles = window.getComputedStyle(backdrop!);
      
      expect(styles.position).toBe('fixed');
      expect(styles.top).toBe('0px');
      expect(styles.left).toBe('0px');
      expect(styles.right).toBe('0px');
      expect(styles.bottom).toBe('0px');
    });
  });

  describe('Edge Cases', () => {
    it('should handle rapid open/close transitions', () => {
      const { rerender } = render(<DeactivationDialog {...defaultProps} isOpen={false} />);
      
      rerender(<DeactivationDialog {...defaultProps} isOpen={true} />);
      rerender(<DeactivationDialog {...defaultProps} isOpen={false} />);
      rerender(<DeactivationDialog {...defaultProps} isOpen={true} />);
      
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('should handle empty staff name', () => {
      render(<DeactivationDialog {...defaultProps} staffName="" />);
      expect(screen.getByText(/Are you sure you want to deactivate/)).toBeInTheDocument();
    });

    it('should handle special characters in staff name', () => {
      render(<DeactivationDialog {...defaultProps} staffName="O'Brien & Sons <Test>" />);
      expect(screen.getByText(/O'Brien & Sons <Test>/)).toBeInTheDocument();
    });

    it('should not call onConfirm when Enter is pressed without confirm button focused', () => {
      render(<DeactivationDialog {...defaultProps} />);
      fireEvent.keyDown(document, { key: 'Enter' });
      expect(mockOnConfirm).not.toHaveBeenCalled();
    });
  });
});