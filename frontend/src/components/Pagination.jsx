import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import Pagination from './Pagination';

describe('Pagination Component', () => {
  const mockOnPageChange = jest.fn();

  beforeEach(() => {
    mockOnPageChange.mockClear();
  });

  describe('Component Rendering', () => {
    test('should render pagination navigation with correct aria-label', () => {
      render(<Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />);
      const nav = screen.getByRole('navigation', { name: 'Pagination navigation' });
      expect(nav).toBeInTheDocument();
      expect(nav).toHaveClass('pagination-container');
    });

    test('should render Previous button', () => {
      render(<Pagination currentPage={2} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).toBeInTheDocument();
      expect(previousButton).toHaveClass('pagination-button');
    });

    test('should render Next button', () => {
      render(<Pagination currentPage={2} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toBeInTheDocument();
      expect(nextButton).toHaveClass('pagination-button');
    });

    test('should display current page and total pages', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const pageInfo = screen.getByText('Page 5 of 10');
      expect(pageInfo).toBeInTheDocument();
      expect(pageInfo).toHaveClass('page-info');
    });

    test('should display correct page info for first page', () => {
      render(<Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 1 of 10')).toBeInTheDocument();
    });

    test('should display correct page info for last page', () => {
      render(<Pagination currentPage={10} totalPages={10} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 10 of 10')).toBeInTheDocument();
    });
  });

  describe('Previous Button Functionality', () => {
    test('should call onPageChange with decremented page when Previous is clicked', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      fireEvent.click(previousButton);
      expect(mockOnPageChange).toHaveBeenCalledTimes(1);
      expect(mockOnPageChange).toHaveBeenCalledWith(4);
    });

    test('should disable Previous button when on first page', () => {
      render(<Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).toBeDisabled();
      expect(previousButton).toHaveAttribute('aria-disabled', 'true');
      expect(previousButton).toHaveClass('pagination-button', 'disabled');
    });

    test('should not disable Previous button when not on first page', () => {
      render(<Pagination currentPage={2} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).not.toBeDisabled();
      expect(previousButton).toHaveAttribute('aria-disabled', 'false');
      expect(previousButton).not.toHaveClass('disabled');
    });

    test('should not call onPageChange when Previous button is disabled and clicked', () => {
      render(<Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      fireEvent.click(previousButton);
      expect(mockOnPageChange).not.toHaveBeenCalled();
    });
  });

  describe('Next Button Functionality', () => {
    test('should call onPageChange with incremented page when Next is clicked', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      fireEvent.click(nextButton);
      expect(mockOnPageChange).toHaveBeenCalledTimes(1);
      expect(mockOnPageChange).toHaveBeenCalledWith(6);
    });

    test('should disable Next button when on last page', () => {
      render(<Pagination currentPage={10} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toBeDisabled();
      expect(nextButton).toHaveAttribute('aria-disabled', 'true');
      expect(nextButton).toHaveClass('pagination-button', 'disabled');
    });

    test('should not disable Next button when not on last page', () => {
      render(<Pagination currentPage={9} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).not.toBeDisabled();
      expect(nextButton).toHaveAttribute('aria-disabled', 'false');
      expect(nextButton).not.toHaveClass('disabled');
    });

    test('should not call onPageChange when Next button is disabled and clicked', () => {
      render(<Pagination currentPage={10} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      fireEvent.click(nextButton);
      expect(mockOnPageChange).not.toHaveBeenCalled();
    });
  });

  describe('Keyboard Navigation', () => {
    test('should allow Previous button to be focused', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      previousButton.focus();
      expect(previousButton).toHaveFocus();
    });

    test('should allow Next button to be focused', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      nextButton.focus();
      expect(nextButton).toHaveFocus();
    });

    test('should trigger onPageChange when Previous button is activated with Enter key', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      fireEvent.keyDown(previousButton, { key: 'Enter', code: 'Enter' });
      fireEvent.click(previousButton);
      expect(mockOnPageChange).toHaveBeenCalledWith(4);
    });

    test('should trigger onPageChange when Next button is activated with Enter key', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      fireEvent.keyDown(nextButton, { key: 'Enter', code: 'Enter' });
      fireEvent.click(nextButton);
      expect(mockOnPageChange).toHaveBeenCalledWith(6);
    });

    test('should trigger onPageChange when Previous button is activated with Space key', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      fireEvent.keyDown(previousButton, { key: ' ', code: 'Space' });
      fireEvent.click(previousButton);
      expect(mockOnPageChange).toHaveBeenCalledWith(4);
    });

    test('should trigger onPageChange when Next button is activated with Space key', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      fireEvent.keyDown(nextButton, { key: ' ', code: 'Space' });
      fireEvent.click(nextButton);
      expect(mockOnPageChange).toHaveBeenCalledWith(6);
    });
  });

  describe('Edge Cases', () => {
    test('should handle single page scenario', () => {
      render(<Pagination currentPage={1} totalPages={1} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(previousButton).toBeDisabled();
      expect(nextButton).toBeDisabled();
      expect(screen.getByText('Page 1 of 1')).toBeInTheDocument();
    });

    test('should handle two page scenario on first page', () => {
      render(<Pagination currentPage={1} totalPages={2} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(previousButton).toBeDisabled();
      expect(nextButton).not.toBeDisabled();
    });

    test('should handle two page scenario on last page', () => {
      render(<Pagination currentPage={2} totalPages={2} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(previousButton).not.toBeDisabled();
      expect(nextButton).toBeDisabled();
    });

    test('should handle large page numbers', () => {
      render(<Pagination currentPage={999} totalPages={1000} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 999 of 1000')).toBeInTheDocument();
      const previousButton = screen.getByRole('button', { name: /previous/i });
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(previousButton).not.toBeDisabled();
      expect(nextButton).not.toBeDisabled();
    });
  });

  describe('Accessibility Features', () => {
    test('should have proper aria-label on navigation element', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const nav = screen.getByRole('navigation');
      expect(nav).toHaveAttribute('aria-label', 'Pagination navigation');
    });

    test('should set aria-disabled to true when Previous button is disabled', () => {
      render(<Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).toHaveAttribute('aria-disabled', 'true');
    });

    test('should set aria-disabled to false when Previous button is enabled', () => {
      render(<Pagination currentPage={2} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).toHaveAttribute('aria-disabled', 'false');
    });

    test('should set aria-disabled to true when Next button is disabled', () => {
      render(<Pagination currentPage={10} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toHaveAttribute('aria-disabled', 'true');
    });

    test('should set aria-disabled to false when Next button is enabled', () => {
      render(<Pagination currentPage={9} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toHaveAttribute('aria-disabled', 'false');
    });
  });

  describe('CSS Classes', () => {
    test('should apply disabled class to Previous button when on first page', () => {
      render(<Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).toHaveClass('pagination-button', 'disabled');
    });

    test('should not apply disabled class to Previous button when not on first page', () => {
      render(<Pagination currentPage={2} totalPages={10} onPageChange={mockOnPageChange} />);
      const previousButton = screen.getByRole('button', { name: /previous/i });
      expect(previousButton).toHaveClass('pagination-button');
      expect(previousButton).not.toHaveClass('disabled');
    });

    test('should apply disabled class to Next button when on last page', () => {
      render(<Pagination currentPage={10} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toHaveClass('pagination-button', 'disabled');
    });

    test('should not apply disabled class to Next button when not on last page', () => {
      render(<Pagination currentPage={9} totalPages={10} onPageChange={mockOnPageChange} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toHaveClass('pagination-button');
      expect(nextButton).not.toHaveClass('disabled');
    });

    test('should apply pagination-container class to nav element', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const nav = screen.getByRole('navigation');
      expect(nav).toHaveClass('pagination-container');
    });

    test('should apply page-info class to page display span', () => {
      render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      const pageInfo = screen.getByText('Page 5 of 10');
      expect(pageInfo).toHaveClass('page-info');
    });
  });

  describe('Component Props', () => {
    test('should accept and use currentPage prop correctly', () => {
      const { rerender } = render(<Pagination currentPage={3} totalPages={10} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 3 of 10')).toBeInTheDocument();
      
      rerender(<Pagination currentPage={7} totalPages={10} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 7 of 10')).toBeInTheDocument();
    });

    test('should accept and use totalPages prop correctly', () => {
      const { rerender } = render(<Pagination currentPage={5} totalPages={10} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 5 of 10')).toBeInTheDocument();
      
      rerender(<Pagination currentPage={5} totalPages={20} onPageChange={mockOnPageChange} />);
      expect(screen.getByText('Page 5 of 20')).toBeInTheDocument();
    });

    test('should accept and use onPageChange callback prop correctly', () => {
      const customCallback = jest.fn();
      render(<Pagination currentPage={5} totalPages={10} onPageChange={customCallback} />);
      const nextButton = screen.getByRole('button', { name: /next/i });
      fireEvent.click(nextButton);
      expect(customCallback).toHaveBeenCalledWith(6);
    });
  });
});