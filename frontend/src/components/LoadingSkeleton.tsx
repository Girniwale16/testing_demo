import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import LoadingSkeleton from './LoadingSkeleton';

describe('LoadingSkeleton Component', () => {
  describe('Component Rendering', () => {
    it('should render LoadingSkeleton component', () => {
      render(<LoadingSkeleton />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toBeInTheDocument();
    });

    it('should render with default export', () => {
      expect(LoadingSkeleton).toBeDefined();
    });
  });

  describe('Default Props', () => {
    it('should use default rows value of 5', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const rows = container.querySelectorAll('.skeleton-table-row');
      expect(rows).toHaveLength(5);
    });

    it('should use default columns value of 4', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const headerCells = container.querySelectorAll('.skeleton-table-header-cell');
      expect(headerCells).toHaveLength(4);
    });

    it('should use default variant of table', () => {
      const { container } = render(<LoadingSkeleton />);
      const tableVariant = container.querySelector('.skeleton-table');
      expect(tableVariant).toBeInTheDocument();
    });
  });

  describe('Table Variant', () => {
    it('should render table variant when variant is "table"', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const tableVariant = container.querySelector('.skeleton-table');
      expect(tableVariant).toBeInTheDocument();
    });

    it('should render correct number of rows in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" rows={3} />);
      const rows = container.querySelectorAll('.skeleton-table-row');
      expect(rows).toHaveLength(3);
    });

    it('should render correct number of columns in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" columns={6} />);
      const headerCells = container.querySelectorAll('.skeleton-table-header-cell');
      expect(headerCells).toHaveLength(6);
    });

    it('should render table header in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const header = container.querySelector('.skeleton-table-header');
      expect(header).toBeInTheDocument();
    });

    it('should render table body in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const body = container.querySelector('.skeleton-table-body');
      expect(body).toBeInTheDocument();
    });

    it('should render correct number of cells per row in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" rows={2} columns={3} />);
      const firstRow = container.querySelector('.skeleton-table-row');
      const cells = firstRow?.querySelectorAll('.skeleton-table-cell');
      expect(cells).toHaveLength(3);
    });

    it('should render table cells with unique keys', () => {
      const { container } = render(<LoadingSkeleton variant="table" rows={2} columns={2} />);
      const cells = container.querySelectorAll('.skeleton-table-cell');
      expect(cells.length).toBe(4);
    });
  });

  describe('Card Variant', () => {
    it('should render card variant when variant is "card"', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const cardVariant = container.querySelector('.skeleton-card-grid');
      expect(cardVariant).toBeInTheDocument();
    });

    it('should render correct number of cards in card variant', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={3} />);
      const cards = container.querySelectorAll('.skeleton-card');
      expect(cards).toHaveLength(3);
    });

    it('should render card image in each card', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={2} />);
      const cardImages = container.querySelectorAll('.skeleton-card-image');
      expect(cardImages).toHaveLength(2);
    });

    it('should render card content in each card', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={2} />);
      const cardContents = container.querySelectorAll('.skeleton-card-content');
      expect(cardContents).toHaveLength(2);
    });

    it('should render card title in each card', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={2} />);
      const cardTitles = container.querySelectorAll('.skeleton-card-title');
      expect(cardTitles).toHaveLength(2);
    });

    it('should render card text elements in each card', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={1} />);
      const cardTexts = container.querySelectorAll('.skeleton-card-text');
      expect(cardTexts.length).toBeGreaterThanOrEqual(2);
    });

    it('should render short card text element in each card', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={1} />);
      const shortText = container.querySelector('.skeleton-card-text.short');
      expect(shortText).toBeInTheDocument();
    });

    it('should render cards with unique keys', () => {
      const { container } = render(<LoadingSkeleton variant="card" rows={3} />);
      const cards = container.querySelectorAll('.skeleton-card');
      expect(cards).toHaveLength(3);
    });
  });

  describe('List Variant', () => {
    it('should render list variant when variant is "list"', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const listVariant = container.querySelector('.skeleton-list');
      expect(listVariant).toBeInTheDocument();
    });

    it('should render correct number of list items in list variant', () => {
      const { container } = render(<LoadingSkeleton variant="list" rows={4} />);
      const listItems = container.querySelectorAll('.skeleton-list-item');
      expect(listItems).toHaveLength(4);
    });

    it('should render list avatar in each list item', () => {
      const { container } = render(<LoadingSkeleton variant="list" rows={2} />);
      const avatars = container.querySelectorAll('.skeleton-list-avatar');
      expect(avatars).toHaveLength(2);
    });

    it('should render list content in each list item', () => {
      const { container } = render(<LoadingSkeleton variant="list" rows={2} />);
      const contents = container.querySelectorAll('.skeleton-list-content');
      expect(contents).toHaveLength(2);
    });

    it('should render list title in each list item', () => {
      const { container } = render(<LoadingSkeleton variant="list" rows={2} />);
      const titles = container.querySelectorAll('.skeleton-list-title');
      expect(titles).toHaveLength(2);
    });

    it('should render list subtitle in each list item', () => {
      const { container } = render(<LoadingSkeleton variant="list" rows={2} />);
      const subtitles = container.querySelectorAll('.skeleton-list-subtitle');
      expect(subtitles).toHaveLength(2);
    });

    it('should render list items with unique keys', () => {
      const { container } = render(<LoadingSkeleton variant="list" rows={3} />);
      const listItems = container.querySelectorAll('.skeleton-list-item');
      expect(listItems).toHaveLength(3);
    });
  });

  describe('Accessibility Attributes', () => {
    it('should have aria-busy attribute set to true for table variant', () => {
      render(<LoadingSkeleton variant="table" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toHaveAttribute('aria-busy', 'true');
    });

    it('should have aria-label attribute for table variant', () => {
      render(<LoadingSkeleton variant="table" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toHaveAttribute('aria-label', 'Loading content');
    });

    it('should have aria-busy attribute set to true for card variant', () => {
      render(<LoadingSkeleton variant="card" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toHaveAttribute('aria-busy', 'true');
    });

    it('should have aria-label attribute for card variant', () => {
      render(<LoadingSkeleton variant="card" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toHaveAttribute('aria-label', 'Loading content');
    });

    it('should have aria-busy attribute set to true for list variant', () => {
      render(<LoadingSkeleton variant="list" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toHaveAttribute('aria-busy', 'true');
    });

    it('should have aria-label attribute for list variant', () => {
      render(<LoadingSkeleton variant="list" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toHaveAttribute('aria-label', 'Loading content');
    });
  });

  describe('Data Test ID', () => {
    it('should have data-testid attribute for table variant', () => {
      render(<LoadingSkeleton variant="table" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toBeInTheDocument();
    });

    it('should have data-testid attribute for card variant', () => {
      render(<LoadingSkeleton variant="card" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toBeInTheDocument();
    });

    it('should have data-testid attribute for list variant', () => {
      render(<LoadingSkeleton variant="list" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toBeInTheDocument();
    });
  });

  describe('CSS Animations and Styles', () => {
    it('should render style tag with shimmer keyframes', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag).toBeInTheDocument();
      expect(styleTag?.textContent).toContain('@keyframes shimmer');
    });

    it('should have shimmer animation starting at -1000px', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('background-position: -1000px 0');
    });

    it('should have shimmer animation ending at 1000px', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('background-position: 1000px 0');
    });

    it('should have linear gradient background with correct colors', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%)');
    });

    it('should have animation duration of 2s', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('animation: shimmer 2s infinite linear');
    });

    it('should have infinite animation', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('infinite');
    });

    it('should have linear animation timing', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('linear');
    });
  });

  describe('Responsive Design', () => {
    it('should include media query for 768px breakpoint', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('@media (max-width: 768px)');
    });

    it('should include media query for 480px breakpoint', () => {
      const { container } = render(<LoadingSkeleton />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('@media (max-width: 480px)');
    });

    it('should adjust grid columns in mobile view for table', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('grid-template-columns: 1fr');
    });

    it('should adjust card grid in tablet view', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('minmax(200px, 1fr)');
    });

    it('should adjust card image height in mobile view', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('height: 120px');
    });

    it('should adjust list avatar size in tablet view', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const styleTag = container.querySelector('style');
      expect(styleTag?.textContent).toContain('width: 40px');
      expect(styleTag?.textContent).toContain('height: 40px');
    });
  });

  describe('Variant Switching', () => {
    it('should default to table variant for invalid variant', () => {
      const { container } = render(<LoadingSkeleton variant={'invalid' as any} />);
      const tableVariant = container.querySelector('.skeleton-table');
      expect(tableVariant).toBeInTheDocument();
    });

    it('should not render card variant when table is selected', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const cardVariant = container.querySelector('.skeleton-card-grid');
      expect(cardVariant).not.toBeInTheDocument();
    });

    it('should not render list variant when table is selected', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const listVariant = container.querySelector('.skeleton-list');
      expect(listVariant).not.toBeInTheDocument();
    });

    it('should not render table variant when card is selected', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const tableVariant = container.querySelector('.skeleton-table');
      expect(tableVariant).not.toBeInTheDocument();
    });

    it('should not render list variant when card is selected', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const listVariant = container.querySelector('.skeleton-list');
      expect(listVariant).not.toBeInTheDocument();
    });

    it('should not render table variant when list is selected', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const tableVariant = container.querySelector('.skeleton-table');
      expect(tableVariant).not.toBeInTheDocument();
    });

    it('should not render card variant when list is selected', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const cardVariant = container.querySelector('.skeleton-card-grid');
      expect(cardVariant).not.toBeInTheDocument();
    });
  });

  describe('Props Interface', () => {
    it('should accept rows prop as number', () => {
      const { container } = render(<LoadingSkeleton rows={10} variant="table" />);
      const rows = container.querySelectorAll('.skeleton-table-row');
      expect(rows).toHaveLength(10);
    });

    it('should accept columns prop as number', () => {
      const { container } = render(<LoadingSkeleton columns={8} variant="table" />);
      const headerCells = container.querySelectorAll('.skeleton-table-header-cell');
      expect(headerCells).toHaveLength(8);
    });

    it('should accept variant prop as table', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const tableVariant = container.querySelector('.skeleton-table');
      expect(tableVariant).toBeInTheDocument();
    });

    it('should accept variant prop as card', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const cardVariant = container.querySelector('.skeleton-card-grid');
      expect(cardVariant).toBeInTheDocument();
    });

    it('should accept variant prop as list', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const listVariant = container.querySelector('.skeleton-list');
      expect(listVariant).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle rows value of 0', () => {
      const { container } = render(<LoadingSkeleton rows={0} variant="table" />);
      const rows = container.querySelectorAll('.skeleton-table-row');
      expect(rows).toHaveLength(0);
    });

    it('should handle columns value of 0', () => {
      const { container } = render(<LoadingSkeleton columns={0} variant="table" />);
      const headerCells = container.querySelectorAll('.skeleton-table-header-cell');
      expect(headerCells).toHaveLength(0);
    });

    it('should handle large rows value', () => {
      const { container } = render(<LoadingSkeleton rows={100} variant="list" />);
      const listItems = container.querySelectorAll('.skeleton-list-item');
      expect(listItems).toHaveLength(100);
    });

    it('should handle large columns value', () => {
      const { container } = render(<LoadingSkeleton columns={20} variant="table" />);
      const headerCells = container.querySelectorAll('.skeleton-table-header-cell');
      expect(headerCells).toHaveLength(20);
    });

    it('should render with only rows prop provided', () => {
      const { container } = render(<LoadingSkeleton rows={3} />);
      const skeleton = container.querySelector('.skeleton-table');
      expect(skeleton).toBeInTheDocument();
    });

    it('should render with only columns prop provided', () => {
      const { container } = render(<LoadingSkeleton columns={6} />);
      const skeleton = container.querySelector('.skeleton-table');
      expect(skeleton).toBeInTheDocument();
    });

    it('should render with only variant prop provided', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const skeleton = container.querySelector('.skeleton-card-grid');
      expect(skeleton).toBeInTheDocument();
    });

    it('should render with no props provided', () => {
      const { container } = render(<LoadingSkeleton />);
      const skeleton = container.querySelector('.skeleton-table');
      expect(skeleton).toBeInTheDocument();
    });
  });

  describe('Component Structure', () => {
    it('should render React Fragment as root element', () => {
      const { container } = render(<LoadingSkeleton />);
      expect(container.firstChild).toBeTruthy();
    });

    it('should render style tag before variant content', () => {
      const { container } = render(<LoadingSkeleton />);
      const firstChild = container.firstChild?.firstChild;
      expect(firstChild?.nodeName).toBe('STYLE');
    });

    it('should render variant content after style tag', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const skeleton = screen.getByTestId('loading-skeleton');
      expect(skeleton).toBeInTheDocument();
    });
  });

  describe('CSS Class Names', () => {
    it('should apply skeleton-table class for table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const element = container.querySelector('.skeleton-table');
      expect(element).toBeInTheDocument();
    });

    it('should apply skeleton-card-grid class for card variant', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const element = container.querySelector('.skeleton-card-grid');
      expect(element).toBeInTheDocument();
    });

    it('should apply skeleton-list class for list variant', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const element = container.querySelector('.skeleton-list');
      expect(element).toBeInTheDocument();
    });

    it('should apply skeleton-table-header class in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const element = container.querySelector('.skeleton-table-header');
      expect(element).toBeInTheDocument();
    });

    it('should apply skeleton-table-body class in table variant', () => {
      const { container } = render(<LoadingSkeleton variant="table" />);
      const element = container.querySelector('.skeleton-table-body');
      expect(element).toBeInTheDocument();
    });

    it('should apply skeleton-card class in card variant', () => {
      const { container } = render(<LoadingSkeleton variant="card" />);
      const element = container.querySelector('.skeleton-card');
      expect(element).toBeInTheDocument();
    });

    it('should apply skeleton-list-item class in list variant', () => {
      const { container } = render(<LoadingSkeleton variant="list" />);
      const element = container.querySelector('.skeleton-list-item');
      expect(element).toBeInTheDocument();
    });
  });
});