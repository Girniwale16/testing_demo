import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DeactivationDialog from '../components/DeactivationDialog';

describe('DeactivationDialog', () => {
  const mockOnClose = jest.fn();
  const mockOnConfirm = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders dialog with correct accessibility attributes', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeInTheDocument();
    expect(dialog).toHaveAttribute('aria-modal', 'true');
    expect(dialog).toHaveAttribute('aria-labelledby');
    expect(dialog).toHaveAttribute('aria-describedby');
    
    const labelId = dialog.getAttribute('aria-labelledby');
    const descId = dialog.getAttribute('aria-describedby');
    
    expect(document.getElementById(labelId!)).toBeInTheDocument();
    expect(document.getElementById(descId!)).toBeInTheDocument();
  });

  test('displays staff name in confirmation message', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="John Doe"
      />
    );

    expect(screen.getByText(/deactivate John Doe/i)).toBeInTheDocument();
  });

  test('calls onClose when Cancel button is clicked', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    fireEvent.click(cancelButton);
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  test('calls onConfirm when Confirm button is clicked', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const confirmButton = screen.getByRole('button', { name: /confirm/i });
    fireEvent.click(confirmButton);
    
    expect(mockOnConfirm).toHaveBeenCalledTimes(1);
  });

  test('closes dialog when Escape key is pressed', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const dialog = screen.getByRole('dialog');
    fireEvent.keyDown(dialog, { key: 'Escape', code: 'Escape' });
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  test('confirms deactivation when Enter key is pressed on Confirm button', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const confirmButton = screen.getByRole('button', { name: /confirm/i });
    confirmButton.focus();
    fireEvent.keyDown(confirmButton, { key: 'Enter', code: 'Enter' });
    
    expect(mockOnConfirm).toHaveBeenCalledTimes(1);
  });

  test('traps focus within dialog when open', async () => {
    const user = userEvent.setup();
    
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const dialog = screen.getByRole('dialog');
    const focusableElements = dialog.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    
    const firstElement = focusableElements[0] as HTMLElement;
    const lastElement = focusableElements[focusableElements.length - 1] as HTMLElement;

    firstElement.focus();
    expect(document.activeElement).toBe(firstElement);

    await user.tab();
    expect(dialog.contains(document.activeElement)).toBe(true);

    lastElement.focus();
    await user.tab();
    expect(dialog.contains(document.activeElement)).toBe(true);
  });

  test('returns focus to trigger element on close', async () => {
    const user = userEvent.setup();
    
    const { rerender } = render(
      <>
        <button data-testid="trigger-button">Open Dialog</button>
        <DeactivationDialog
          isOpen={false}
          onClose={mockOnClose}
          onConfirm={mockOnConfirm}
          staffName="Test User"
        />
      </>
    );

    const triggerButton = screen.getByTestId('trigger-button');
    triggerButton.focus();
    expect(document.activeElement).toBe(triggerButton);

    rerender(
      <>
        <button data-testid="trigger-button">Open Dialog</button>
        <DeactivationDialog
          isOpen={true}
          onClose={mockOnClose}
          onConfirm={mockOnConfirm}
          staffName="Test User"
        />
      </>
    );

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    rerender(
      <>
        <button data-testid="trigger-button">Open Dialog</button>
        <DeactivationDialog
          isOpen={false}
          onClose={mockOnClose}
          onConfirm={mockOnConfirm}
          staffName="Test User"
        />
      </>
    );

    await waitFor(() => {
      expect(document.activeElement).toBe(triggerButton);
    });
  });

  test('disables Confirm button when isLoading is true', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
        isLoading={true}
      />
    );

    const confirmButton = screen.getByRole('button', { name: /confirm/i });
    expect(confirmButton).toBeDisabled();
  });

  test('displays aria-busy attribute when loading', () => {
    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
        isLoading={true}
      />
    );

    const dialog = screen.getByRole('dialog');
    expect(dialog).toHaveAttribute('aria-busy', 'true');
  });

  test('renders full-screen on mobile viewport', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    });

    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const dialog = screen.getByRole('dialog');
    const styles = window.getComputedStyle(dialog);
    
    expect(styles.width).toBe('100vw');
    expect(styles.height).toBe('100vh');
  });

  test('renders centered modal on desktop viewport', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1024,
    });

    render(
      <DeactivationDialog
        isOpen={true}
        onClose={mockOnClose}
        onConfirm={mockOnConfirm}
        staffName="Test User"
      />
    );

    const dialog = screen.getByRole('dialog');
    const styles = window.getComputedStyle(dialog);
    
    expect(styles.width).toBe('500px');
    expect(styles.maxWidth).toBe('90vw');
  });
});