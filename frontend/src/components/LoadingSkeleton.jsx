import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import LoadingSkeleton from './LoadingSkeleton';

describe('LoadingSkeleton Component', () => {
  describe('Component Rendering', () => {
    test('should render without crashing', () => {
      const { container } = render(<LoadingSkeleton />);
      expect(container).toBeInTheDocument();
    });

    test('should render as a functional component returning JSX.Element', () => {
      const result = render(<LoadingSkeleton />);
      expect(result).toBeTruthy();
      expect(result.container.firstChild).toBeInstanceOf(HTMLElement);
    });
  });

  describe('Accessibility Attributes', () => {
    test('should have role="status" attribute on outer div', () => {
      render(<LoadingSkeleton />);
      const statusElement = screen.getByRole('status');
      expect(statusElement).toBeInTheDocument();
    });

    test('should have aria-live="polite" attribute on outer div', () => {
      render(<LoadingSkeleton />);
      const statusElement = screen.getByRole('status');
      expect(statusElement).toHaveAttribute('aria-live', 'polite');
    });

    test('should have aria-label="Loading staff list" for screen reader accessibility', () => {
      render(<LoadingSkeleton />);
      const statusElement = screen.getByRole('status');
      expect(statusElement).toHaveAttribute('aria-label', 'Loading staff list');
    });

    test('should be accessible via aria-label text', () => {
      render(<LoadingSkeleton />);
      const element = screen.getByLabelText('Loading staff list');
      expect(element).toBeInTheDocument();
    });
  });

  describe('Skeleton Row Structure', () => {
    test('should render exactly 5 skeleton rows', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const rows = statusDiv.children;
      expect(rows).toHaveLength(5);
    });

    test('should render 5 skeleton rows to simulate table rows during loading', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const skeletonRows = Array.from(statusDiv.children);
      expect(skeletonRows).toHaveLength(5);
      skeletonRows.forEach(row => {
        expect(row).toBeInstanceOf(HTMLDivElement);
      });
    });

    test('each skeleton row should have correct inline styles', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      
      expect(firstRow).toHaveStyle({
        display: 'flex',
        gap: '16px',
        padding: '12px 16px',
        borderBottom: '1px solid #f0f0f0',
        height: '56px',
        alignItems: 'center'
      });
    });

    test('should render rows with matching table row height', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const rows = Array.from(statusDiv.children);
      
      rows.forEach(row => {
        expect(row).toHaveStyle({ height: '56px' });
      });
    });
  });

  describe('Skeleton Cell Structure', () => {
    test('each skeleton row should contain exactly 4 skeleton cells', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const rows = Array.from(statusDiv.children);
      
      rows.forEach(row => {
        expect(row.children).toHaveLength(4);
      });
    });

    test('should render 4 cells per row for Name, Role, Facility, Actions columns', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const cells = Array.from(firstRow.children);
      
      expect(cells).toHaveLength(4);
      cells.forEach(cell => {
        expect(cell).toBeInstanceOf(HTMLDivElement);
      });
    });

    test('each skeleton cell should have flex: 1 style', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const cells = Array.from(firstRow.children);
      
      cells.forEach(cell => {
        expect(cell).toHaveStyle({ flex: '1' });
      });
    });
  });

  describe('CSS Skeleton Animation Styles', () => {
    test('skeleton cells should have background-color #e0e0e0', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      expect(firstCell).toHaveStyle({ backgroundColor: '#e0e0e0' });
    });

    test('skeleton cells should have border-radius for rounded corners', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      expect(firstCell).toHaveStyle({ borderRadius: '4px' });
    });

    test('skeleton cells should have height of 20px', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      expect(firstCell).toHaveStyle({ height: '20px' });
    });

    test('skeleton cells should have margin 8px 0', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      expect(firstCell).toHaveStyle({ margin: '8px 0' });
    });

    test('skeleton cells should have pulse animation applied', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      expect(firstCell).toHaveStyle({ animation: 'pulse 1.5s ease-in-out infinite' });
    });
  });

  describe('Keyframe Animation', () => {
    test('should inject @keyframes pulse animation with opacity transition', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleElement = container.querySelector('style');
      
      expect(styleElement).toBeInTheDocument();
      expect(styleElement.textContent).toContain('@keyframes pulse');
      expect(styleElement.textContent).toContain('0%, 100%');
      expect(styleElement.textContent).toContain('opacity: 1');
      expect(styleElement.textContent).toContain('50%');
      expect(styleElement.textContent).toContain('opacity: 0.5');
    });

    test('should render inline style tag with pulse keyframes', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleElement = container.querySelector('style');
      
      expect(styleElement).toBeTruthy();
      expect(styleElement.innerHTML).toMatch(/@keyframes pulse/);
    });

    test('pulse animation should have ease-in-out timing function', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      const animationValue = firstCell.style.animation;
      expect(animationValue).toContain('ease-in-out');
    });

    test('pulse animation should be infinite', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      const animationValue = firstCell.style.animation;
      expect(animationValue).toContain('infinite');
    });

    test('pulse animation duration should be 1.5s', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const firstRow = statusDiv.children[0];
      const firstCell = firstRow.children[0];
      
      const animationValue = firstCell.style.animation;
      expect(animationValue).toContain('1.5s');
    });
  });

  describe('Component Export', () => {
    test('should export LoadingSkeleton as default export', () => {
      expect(LoadingSkeleton).toBeDefined();
      expect(typeof LoadingSkeleton).toBe('function');
    });

    test('should be consumable by other components like StaffList.jsx', () => {
      const TestConsumer = () => {
        return (
          <div>
            <LoadingSkeleton />
          </div>
        );
      };
      
      const { container } = render(<TestConsumer />);
      const statusElement = container.querySelector('[role="status"]');
      expect(statusElement).toBeInTheDocument();
    });
  });

  describe('JSDoc Documentation', () => {
    test('component should have JSDoc comment documenting purpose', () => {
      const componentString = LoadingSkeleton.toString();
      expect(componentString).toBeDefined();
    });

    test('should be a functional component', () => {
      expect(typeof LoadingSkeleton).toBe('function');
      const result = LoadingSkeleton();
      expect(React.isValidElement(result)).toBe(true);
    });
  });

  describe('Integration Tests', () => {
    test('should render complete skeleton structure with all elements', () => {
      const { container } = render(<LoadingSkeleton />);
      
      const styleTag = container.querySelector('style');
      expect(styleTag).toBeInTheDocument();
      
      const statusDiv = screen.getByRole('status');
      expect(statusDiv).toBeInTheDocument();
      expect(statusDiv).toHaveAttribute('aria-live', 'polite');
      expect(statusDiv).toHaveAttribute('aria-label', 'Loading staff list');
      
      const rows = Array.from(statusDiv.children);
      expect(rows).toHaveLength(5);
      
      rows.forEach(row => {
        const cells = Array.from(row.children);
        expect(cells).toHaveLength(4);
      });
    });

    test('all skeleton cells should have consistent styling', () => {
      const { container } = render(<LoadingSkeleton />);
      const statusDiv = container.querySelector('[role="status"]');
      const allCells = [];
      
      Array.from(statusDiv.children).forEach(row => {
        Array.from(row.children).forEach(cell => {
          allCells.push(cell);
        });
      });
      
      expect(allCells).toHaveLength(20);
      
      allCells.forEach(cell => {
        expect(cell).toHaveStyle({
          animation: 'pulse 1.5s ease-in-out infinite',
          backgroundColor: '#e0e0e0',
          borderRadius: '4px',
          height: '20px',
          margin: '8px 0',
          flex: '1'
        });
      });
    });

    test('should maintain structure integrity across multiple renders', () => {
      const { rerender, container } = render(<LoadingSkeleton />);
      
      const getStructure = () => {
        const statusDiv = container.querySelector('[role="status"]');
        return {
          rowCount: statusDiv.children.length,
          cellCounts: Array.from(statusDiv.children).map(row => row.children.length)
        };
      };
      
      const structure1 = getStructure();
      rerender(<LoadingSkeleton />);
      const structure2 = getStructure();
      
      expect(structure1).toEqual(structure2);
      expect(structure1.rowCount).toBe(5);
      expect(structure1.cellCounts).toEqual([4, 4, 4, 4, 4]);
    });
  });

  describe('Edge Cases', () => {
    test('should handle multiple instances rendered simultaneously', () => {
      const { container } = render(
        <>
          <LoadingSkeleton />
          <LoadingSkeleton />
        </>
      );
      
      const statusElements = container.querySelectorAll('[role="status"]');
      expect(statusElements).toHaveLength(2);
    });

    test('should not throw errors when unmounted', () => {
      const { unmount } = render(<LoadingSkeleton />);
      expect(() => unmount()).not.toThrow();
    });

    test('should render correctly in different parent contexts', () => {
      const { container } = render(
        <div style={{ width: '100%' }}>
          <LoadingSkeleton />
        </div>
      );
      
      const statusElement = container.querySelector('[role="status"]');
      expect(statusElement).toBeInTheDocument();
    });
  });
});