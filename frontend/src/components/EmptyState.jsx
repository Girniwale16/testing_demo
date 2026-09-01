import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { useNavigate } from 'react-router-dom';
import EmptyState from './EmptyState';

jest.mock('react-router-dom', () => ({
  useNavigate: jest.fn(),
}));

describe('EmptyState Component', () => {
  let mockNavigate;

  beforeEach(() => {
    mockNavigate = jest.fn();
    useNavigate.mockReturnValue(mockNavigate);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    test('should render the component with correct structure', () => {
      render(<EmptyState />);
      
      const container = screen.getByRole('region', { name: 'No staff members' });
      expect(container).toBeInTheDocument();
      expect(container).toHaveClass('empty-state-container');
    });

    test('should render heading with correct text', () => {
      render(<EmptyState />);
      
      const heading = screen.getByRole('heading', { name: 'No Staff Members Yet' });
      expect(heading).toBeInTheDocument();
      expect(heading).toHaveClass('empty-state-heading');
    });

    test('should render descriptive paragraph with correct text', () => {
      render(<EmptyState />);
      
      const description = screen.getByText('Get started by adding your first staff member to the roster.');
      expect(description).toBeInTheDocument();
      expect(description).toHaveClass('empty-state-description');
    });

    test('should render primary action button with correct text', () => {
      render(<EmptyState />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      expect(button).toBeInTheDocument();
      expect(button).toHaveClass('btn', 'btn-primary');
    });
  });

  describe('Accessibility', () => {
    test('should have correct role attribute on outer div', () => {
      render(<EmptyState />);
      
      const region = screen.getByRole('region');
      expect(region).toHaveAttribute('role', 'region');
    });

    test('should have correct aria-label for screen readers', () => {
      render(<EmptyState />);
      
      const region = screen.getByLabelText('No staff members');
      expect(region).toBeInTheDocument();
    });
  });

  describe('Navigation Behavior', () => {
    test('should call navigate with /staff/create when button clicked without onCreateClick prop', () => {
      render(<EmptyState />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      
      expect(mockNavigate).toHaveBeenCalledTimes(1);
      expect(mockNavigate).toHaveBeenCalledWith('/staff/create');
    });

    test('should not call navigate when onCreateClick prop is provided', () => {
      const mockOnCreateClick = jest.fn();
      render(<EmptyState onCreateClick={mockOnCreateClick} />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      
      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  describe('Custom Callback Behavior', () => {
    test('should call onCreateClick callback when provided', () => {
      const mockOnCreateClick = jest.fn();
      render(<EmptyState onCreateClick={mockOnCreateClick} />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      
      expect(mockOnCreateClick).toHaveBeenCalledTimes(1);
    });

    test('should call onCreateClick instead of navigate when both are available', () => {
      const mockOnCreateClick = jest.fn();
      render(<EmptyState onCreateClick={mockOnCreateClick} />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      
      expect(mockOnCreateClick).toHaveBeenCalledTimes(1);
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('should handle multiple button clicks with custom callback', () => {
      const mockOnCreateClick = jest.fn();
      render(<EmptyState onCreateClick={mockOnCreateClick} />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      fireEvent.click(button);
      fireEvent.click(button);
      
      expect(mockOnCreateClick).toHaveBeenCalledTimes(3);
    });
  });

  describe('Props Handling', () => {
    test('should render correctly when onCreateClick is undefined', () => {
      render(<EmptyState onCreateClick={undefined} />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      
      expect(mockNavigate).toHaveBeenCalledWith('/staff/create');
    });

    test('should render correctly when onCreateClick is null', () => {
      render(<EmptyState onCreateClick={null} />);
      
      const button = screen.getByRole('button', { name: 'Add Staff Member' });
      fireEvent.click(button);
      
      expect(mockNavigate).toHaveBeenCalledWith('/staff/create');
    });

    test('should render correctly with no props', () => {
      render(<EmptyState />);
      
      expect(screen.getByRole('region')).toBeInTheDocument();
      expect(screen.getByRole('button')).toBeInTheDocument();
    });
  });

  describe('CSS Classes', () => {
    test('should apply correct CSS class to container', () => {
      render(<EmptyState />);
      
      const container = screen.getByRole('region');
      expect(container).toHaveClass('empty-state-container');
    });

    test('should apply correct CSS class to heading', () => {
      render(<EmptyState />);
      
      const heading = screen.getByRole('heading');
      expect(heading).toHaveClass('empty-state-heading');
    });

    test('should apply correct CSS class to description', () => {
      render(<EmptyState />);
      
      const description = screen.getByText('Get started by adding your first staff member to the roster.');
      expect(description).toHaveClass('empty-state-description');
    });

    test('should apply correct CSS classes to button', () => {
      render(<EmptyState />);
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('btn');
      expect(button).toHaveClass('btn-primary');
    });
  });

  describe('Integration Tests', () => {
    test('should render complete component structure with all elements', () => {
      const { container } = render(<EmptyState />);
      
      expect(screen.getByRole('region')).toBeInTheDocument();
      expect(screen.getByRole('heading')).toBeInTheDocument();
      expect(screen.getByText(/Get started by adding/)).toBeInTheDocument();
      expect(screen.getByRole('button')).toBeInTheDocument();
      expect(container.querySelector('.empty-state-container')).toBeInTheDocument();
    });

    test('should maintain functionality after multiple renders', () => {
      const { rerender } = render(<EmptyState />);
      
      let button = screen.getByRole('button');
      fireEvent.click(button);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
      
      const mockOnCreateClick = jest.fn();
      rerender(<EmptyState onCreateClick={mockOnCreateClick} />);
      
      button = screen.getByRole('button');
      fireEvent.click(button);
      expect(mockOnCreateClick).toHaveBeenCalledTimes(1);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
    });
  });
});