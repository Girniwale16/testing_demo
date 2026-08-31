import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import EmptyState from './EmptyState';

describe('EmptyState Component', () => {
  describe('Required Props Rendering', () => {
    it('should render with required title and message props', () => {
      render(<EmptyState title="No Data" message="There is no data to display" />);
      
      expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      expect(screen.getByTestId('empty-state-title')).toHaveTextContent('No Data');
      expect(screen.getByText('There is no data to display')).toBeInTheDocument();
    });

    it('should render title as h2 element', () => {
      render(<EmptyState title="Test Title" message="Test message" />);
      
      const title = screen.getByTestId('empty-state-title');
      expect(title.tagName).toBe('H2');
    });

    it('should render message as p element', () => {
      render(<EmptyState title="Test Title" message="Test message" />);
      
      const message = screen.getByText('Test message');
      expect(message.tagName).toBe('P');
    });
  });

  describe('Accessibility Features', () => {
    it('should have role="status" on container', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      const container = screen.getByTestId('empty-state');
      expect(container).toHaveAttribute('role', 'status');
    });

    it('should have aria-label on default icon', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      const icon = screen.getByLabelText('Empty box icon');
      expect(icon).toBeInTheDocument();
    });

    it('should have data-testid attributes for testing', () => {
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={() => {}}
        />
      );
      
      expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      expect(screen.getByTestId('empty-state-title')).toBeInTheDocument();
      expect(screen.getByTestId('empty-state-action')).toBeInTheDocument();
    });
  });

  describe('Icon Rendering', () => {
    it('should render default icon when no icon prop is provided', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      const defaultIcon = screen.getByLabelText('Empty box icon');
      expect(defaultIcon).toBeInTheDocument();
      expect(defaultIcon.tagName).toBe('svg');
    });

    it('should render custom icon when icon prop is provided', () => {
      const customIcon = <div data-testid="custom-icon">Custom Icon</div>;
      render(<EmptyState title="Test" message="Test message" icon={customIcon} />);
      
      expect(screen.getByTestId('custom-icon')).toBeInTheDocument();
      expect(screen.queryByLabelText('Empty box icon')).not.toBeInTheDocument();
    });

    it('should render custom ReactNode icon', () => {
      const customIcon = (
        <svg data-testid="custom-svg">
          <circle cx="50" cy="50" r="40" />
        </svg>
      );
      render(<EmptyState title="Test" message="Test message" icon={customIcon} />);
      
      expect(screen.getByTestId('custom-svg')).toBeInTheDocument();
    });
  });

  describe('Action Button Rendering', () => {
    it('should not render action button when actionLabel is not provided', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      expect(screen.queryByTestId('empty-state-action')).not.toBeInTheDocument();
    });

    it('should not render action button when onAction is not provided', () => {
      render(<EmptyState title="Test" message="Test message" actionLabel="Click me" />);
      
      expect(screen.queryByTestId('empty-state-action')).not.toBeInTheDocument();
    });

    it('should render action button when both actionLabel and onAction are provided', () => {
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={() => {}}
        />
      );
      
      const button = screen.getByTestId('empty-state-action');
      expect(button).toBeInTheDocument();
      expect(button).toHaveTextContent('Click me');
    });

    it('should call onAction when action button is clicked', () => {
      const mockOnAction = jest.fn();
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={mockOnAction}
        />
      );
      
      const button = screen.getByTestId('empty-state-action');
      fireEvent.click(button);
      
      expect(mockOnAction).toHaveBeenCalledTimes(1);
    });

    it('should call onAction multiple times on multiple clicks', () => {
      const mockOnAction = jest.fn();
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={mockOnAction}
        />
      );
      
      const button = screen.getByTestId('empty-state-action');
      fireEvent.click(button);
      fireEvent.click(button);
      fireEvent.click(button);
      
      expect(mockOnAction).toHaveBeenCalledTimes(3);
    });
  });

  describe('Button Hover States', () => {
    it('should change background color on mouse enter', () => {
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={() => {}}
        />
      );
      
      const button = screen.getByTestId('empty-state-action') as HTMLButtonElement;
      expect(button.style.backgroundColor).toBe('rgb(59, 130, 246)');
      
      fireEvent.mouseEnter(button);
      expect(button.style.backgroundColor).toBe('rgb(37, 99, 235)');
    });

    it('should revert background color on mouse leave', () => {
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={() => {}}
        />
      );
      
      const button = screen.getByTestId('empty-state-action') as HTMLButtonElement;
      
      fireEvent.mouseEnter(button);
      expect(button.style.backgroundColor).toBe('rgb(37, 99, 235)');
      
      fireEvent.mouseLeave(button);
      expect(button.style.backgroundColor).toBe('rgb(59, 130, 246)');
    });
  });

  describe('Styling and Layout', () => {
    it('should apply centered layout styles to container', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      const container = screen.getByTestId('empty-state');
      expect(container).toHaveStyle({
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
      });
    });

    it('should apply responsive font size to title', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      const title = screen.getByTestId('empty-state-title');
      expect(title).toHaveStyle({
        fontSize: 'clamp(1.25rem, 2vw, 1.5rem)',
      });
    });

    it('should apply responsive font size to message', () => {
      render(<EmptyState title="Test" message="Test message" />);
      
      const message = screen.getByText('Test message');
      expect(message).toHaveStyle({
        fontSize: 'clamp(0.875rem, 1.5vw, 1rem)',
      });
    });

    it('should apply conditional margin to message when action button is present', () => {
      const { rerender } = render(<EmptyState title="Test" message="Test message" />);
      
      let message = screen.getByText('Test message');
      expect(message).toHaveStyle({ marginBottom: '0' });
      
      rerender(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click"
          onAction={() => {}}
        />
      );
      
      message = screen.getByText('Test message');
      expect(message).toHaveStyle({ marginBottom: '1.5rem' });
    });

    it('should apply primary button styles to action button', () => {
      render(
        <EmptyState
          title="Test"
          message="Test message"
          actionLabel="Click me"
          onAction={() => {}}
        />
      );
      
      const button = screen.getByTestId('empty-state-action');
      expect(button).toHaveStyle({
        backgroundColor: 'rgb(59, 130, 246)',
        color: 'rgb(255, 255, 255)',
        padding: '0.75rem 1.5rem',
        border: 'none',
        borderRadius: '0.5rem',
        cursor: 'pointer',
      });
    });
  });

  describe('Component Integration', () => {
    it('should render complete component with all props', () => {
      const mockOnAction = jest.fn();
      const customIcon = <div data-testid="custom-icon">Icon</div>;
      
      render(
        <EmptyState
          title="No Items Found"
          message="Start by adding your first item"
          icon={customIcon}
          actionLabel="Add Item"
          onAction={mockOnAction}
        />
      );
      
      expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      expect(screen.getByTestId('empty-state-title')).toHaveTextContent('No Items Found');
      expect(screen.getByText('Start by adding your first item')).toBeInTheDocument();
      expect(screen.getByTestId('custom-icon')).toBeInTheDocument();
      expect(screen.getByTestId('empty-state-action')).toHaveTextContent('Add Item');
      
      fireEvent.click(screen.getByTestId('empty-state-action'));
      expect(mockOnAction).toHaveBeenCalled();
    });

    it('should render minimal component with only required props', () => {
      render(<EmptyState title="Empty" message="Nothing here" />);
      
      expect(screen.getByTestId('empty-state')).toBeInTheDocument();
      expect(screen.getByTestId('empty-state-title')).toHaveTextContent('Empty');
      expect(screen.getByText('Nothing here')).toBeInTheDocument();
      expect(screen.getByLabelText('Empty box icon')).toBeInTheDocument();
      expect(screen.queryByTestId('empty-state-action')).not.toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty string title', () => {
      render(<EmptyState title="" message="Test message" />);
      
      const title = screen.getByTestId('empty-state-title');
      expect(title).toHaveTextContent('');
    });

    it('should handle empty string message', () => {
      render(<EmptyState title="Test" message="" />);
      
      expect(screen.getByText('')).toBeInTheDocument();
    });

    it('should handle long title text', () => {
      const longTitle = 'This is a very long title that should still render properly and maintain responsive design';
      render(<EmptyState title={longTitle} message="Test message" />);
      
      expect(screen.getByTestId('empty-state-title')).toHaveTextContent(longTitle);
    });

    it('should handle long message text', () => {
      const longMessage = 'This is a very long message that provides detailed information to the user about the empty state and what actions they can take to resolve it';
      render(<EmptyState title="Test" message={longMessage} />);
      
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    it('should handle special characters in title and message', () => {
      render(<EmptyState title="Test & <Title>" message="Message with 'quotes' and \"double quotes\"" />);
      
      expect(screen.getByTestId('empty-state-title')).toHaveTextContent("Test & <Title>");
      expect(screen.getByText("Message with 'quotes' and \"double quotes\"")).toBeInTheDocument();
    });

    it('should not render button when only actionLabel is provided without onAction', () => {
      render(<EmptyState title="Test" message="Test message" actionLabel="Click me" />);
      
      expect(screen.queryByTestId('empty-state-action')).not.toBeInTheDocument();
    });

    it('should not render button when only onAction is provided without actionLabel', () => {
      render(<EmptyState title="Test" message="Test message" onAction={() => {}} />);
      
      expect(screen.queryByTestId('empty-state-action')).not.toBeInTheDocument();
    });
  });
});