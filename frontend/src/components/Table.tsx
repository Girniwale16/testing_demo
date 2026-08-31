import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import Table from './Table';

interface TestData {
  id: number;
  name: string;
  email: string;
}

describe('Table Component', () => {
  const mockData: TestData[] = [
    { id: 1, name: 'John Doe', email: 'john@example.com' },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com' },
    { id: 3, name: 'Bob Johnson', email: 'bob@example.com' },
  ];

  const mockColumns = [
    { key: 'id', header: 'ID', accessor: 'id' as const },
    { key: 'name', header: 'Name', accessor: 'name' as const },
    { key: 'email', header: 'Email', accessor: 'email' as const },
  ];

  const mockPagination = {
    currentPage: 1,
    totalPages: 3,
    pageSize: 10,
    totalItems: 30,
  };

  const mockOnPageChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('should render table container with data-testid', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByTestId('table-container')).toBeInTheDocument();
    });

    it('should render table with correct structure', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
      expect(screen.getByTestId('table-header')).toBeInTheDocument();
    });

    it('should render all column headers', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByText('ID')).toBeInTheDocument();
      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Email')).toBeInTheDocument();
    });

    it('should render all data rows', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      expect(rows).toHaveLength(3);
    });

    it('should render cell data correctly with string accessor', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('jane@example.com')).toBeInTheDocument();
    });

    it('should render cell data correctly with function accessor', () => {
      const columnsWithFunction = [
        {
          key: 'name',
          header: 'Name',
          accessor: (item: TestData) => <span>{item.name.toUpperCase()}</span>,
        },
      ];

      render(
        <Table
          data={mockData}
          columns={columnsWithFunction}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByText('JOHN DOE')).toBeInTheDocument();
      expect(screen.getByText('JANE SMITH')).toBeInTheDocument();
    });
  });

  describe('Loading State', () => {
    it('should display LoadingSkeleton when loading is true', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={true}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByTestId('table-container')).toBeInTheDocument();
      expect(screen.queryByRole('table')).not.toBeInTheDocument();
      const skeletonRows = document.querySelectorAll('.skeleton-row');
      expect(skeletonRows).toHaveLength(3);
    });

    it('should not display table when loading is true', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={true}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.queryByRole('table')).not.toBeInTheDocument();
      expect(screen.queryByTestId('table-header')).not.toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should display EmptyState when data is empty and not loading', () => {
      render(
        <Table
          data={[]}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByText('No data available')).toBeInTheDocument();
      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });

    it('should display custom empty state when provided', () => {
      const customEmptyState = <div>Custom empty message</div>;

      render(
        <Table
          data={[]}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
          emptyState={customEmptyState}
        />
      );

      expect(screen.getByText('Custom empty message')).toBeInTheDocument();
      expect(screen.queryByText('No data available')).not.toBeInTheDocument();
    });

    it('should not display empty state when loading is true', () => {
      render(
        <Table
          data={[]}
          columns={mockColumns}
          loading={true}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.queryByText('No data available')).not.toBeInTheDocument();
    });
  });

  describe('ARIA Accessibility', () => {
    it('should have aria-label on column headers', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const headers = screen.getAllByRole('columnheader');
      expect(headers[0]).toHaveAttribute('aria-label', 'ID');
      expect(headers[1]).toHaveAttribute('aria-label', 'Name');
      expect(headers[2]).toHaveAttribute('aria-label', 'Email');
    });

    it('should have role="columnheader" on table headers', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const headers = screen.getAllByRole('columnheader');
      expect(headers).toHaveLength(3);
    });

    it('should apply sortable class to sortable columns', () => {
      const sortableColumns = [
        { key: 'name', header: 'Name', accessor: 'name' as const, sortable: true },
        { key: 'email', header: 'Email', accessor: 'email' as const, sortable: false },
      ];

      render(
        <Table
          data={mockData}
          columns={sortableColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const nameHeader = screen.getByText('Name').closest('th');
      const emailHeader = screen.getByText('Email').closest('th');

      expect(nameHeader).toHaveClass('sortable');
      expect(emailHeader).not.toHaveClass('sortable');
    });

    it('should apply custom width to columns', () => {
      const columnsWithWidth = [
        { key: 'id', header: 'ID', accessor: 'id' as const, width: '100px' },
        { key: 'name', header: 'Name', accessor: 'name' as const, width: '200px' },
      ];

      render(
        <Table
          data={mockData}
          columns={columnsWithWidth}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const idHeader = screen.getByText('ID').closest('th');
      const nameHeader = screen.getByText('Name').closest('th');

      expect(idHeader).toHaveStyle({ width: '100px' });
      expect(nameHeader).toHaveStyle({ width: '200px' });
    });
  });

  describe('Keyboard Navigation', () => {
    it('should have tabIndex={0} on table rows', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      rows.forEach((row) => {
        expect(row).toHaveAttribute('tabIndex', '0');
      });
    });

    it('should handle Enter key press on table row', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      fireEvent.keyDown(rows[0], { key: 'Enter' });

      expect(consoleSpy).toHaveBeenCalledWith('Row 0 selected');

      consoleSpy.mockRestore();
    });

    it('should not trigger action on non-Enter key press', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      fireEvent.keyDown(rows[0], { key: 'Tab' });

      expect(consoleSpy).not.toHaveBeenCalled();

      consoleSpy.mockRestore();
    });

    it('should handle Enter key on different rows correctly', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      fireEvent.keyDown(rows[1], { key: 'Enter' });
      fireEvent.keyDown(rows[2], { key: 'Enter' });

      expect(consoleSpy).toHaveBeenCalledWith('Row 1 selected');
      expect(consoleSpy).toHaveBeenCalledWith('Row 2 selected');

      consoleSpy.mockRestore();
    });
  });

  describe('Pagination Controls', () => {
    it('should render pagination controls when totalPages > 1', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByTestId('pagination-controls')).toBeInTheDocument();
    });

    it('should not render pagination controls when totalPages = 1', () => {
      const singlePagePagination = { ...mockPagination, totalPages: 1 };

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={singlePagePagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.queryByTestId('pagination-controls')).not.toBeInTheDocument();
    });

    it('should render Previous and Next buttons', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByLabelText('Previous page')).toBeInTheDocument();
      expect(screen.getByLabelText('Next page')).toBeInTheDocument();
    });

    it('should render all page number buttons', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByLabelText('Page 1')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 2')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 3')).toBeInTheDocument();
    });

    it('should disable Previous button on first page', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const previousButton = screen.getByLabelText('Previous page');
      expect(previousButton).toBeDisabled();
    });

    it('should enable Previous button when not on first page', () => {
      const pagination = { ...mockPagination, currentPage: 2 };

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={pagination}
          onPageChange={mockOnPageChange}
        />
      );

      const previousButton = screen.getByLabelText('Previous page');
      expect(previousButton).not.toBeDisabled();
    });

    it('should disable Next button on last page', () => {
      const pagination = { ...mockPagination, currentPage: 3 };

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={pagination}
          onPageChange={mockOnPageChange}
        />
      );

      const nextButton = screen.getByLabelText('Next page');
      expect(nextButton).toBeDisabled();
    });

    it('should enable Next button when not on last page', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const nextButton = screen.getByLabelText('Next page');
      expect(nextButton).not.toBeDisabled();
    });

    it('should call onPageChange with correct page when Previous is clicked', () => {
      const pagination = { ...mockPagination, currentPage: 2 };

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={pagination}
          onPageChange={mockOnPageChange}
        />
      );

      const previousButton = screen.getByLabelText('Previous page');
      fireEvent.click(previousButton);

      expect(mockOnPageChange).toHaveBeenCalledWith(1);
    });

    it('should call onPageChange with correct page when Next is clicked', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const nextButton = screen.getByLabelText('Next page');
      fireEvent.click(nextButton);

      expect(mockOnPageChange).toHaveBeenCalledWith(2);
    });

    it('should call onPageChange with correct page when page number is clicked', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const page2Button = screen.getByLabelText('Page 2');
      fireEvent.click(page2Button);

      expect(mockOnPageChange).toHaveBeenCalledWith(2);
    });

    it('should apply active class to current page button', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const page1Button = screen.getByLabelText('Page 1');
      expect(page1Button).toHaveClass('active');
    });

    it('should not apply active class to non-current page buttons', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const page2Button = screen.getByLabelText('Page 2');
      const page3Button = screen.getByLabelText('Page 3');

      expect(page2Button).not.toHaveClass('active');
      expect(page3Button).not.toHaveClass('active');
    });

    it('should have aria-current="page" on current page button', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const page1Button = screen.getByLabelText('Page 1');
      expect(page1Button).toHaveAttribute('aria-current', 'page');
    });

    it('should not have aria-current on non-current page buttons', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const page2Button = screen.getByLabelText('Page 2');
      expect(page2Button).not.toHaveAttribute('aria-current');
    });

    it('should render correct number of page buttons based on totalPages', () => {
      const pagination = { ...mockPagination, totalPages: 5 };

      render(
        <Table
          data={mockData}
          columns={columnsWithFunction}
          loading={false}
          pagination={pagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByLabelText('Page 1')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 2')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 3')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 4')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 5')).toBeInTheDocument();
    });
  });

  describe('Data Test IDs', () => {
    it('should have data-testid on table-container', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByTestId('table-container')).toBeInTheDocument();
    });

    it('should have data-testid on table-header', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByTestId('table-header')).toBeInTheDocument();
    });

    it('should have data-testid on table-row for each row', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      expect(rows).toHaveLength(mockData.length);
    });

    it('should have data-testid on pagination-controls', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByTestId('pagination-controls')).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle single row of data', () => {
      const singleData = [mockData[0]];

      render(
        <Table
          data={singleData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const rows = screen.getAllByTestId('table-row');
      expect(rows).toHaveLength(1);
    });

    it('should handle single column', () => {
      const singleColumn = [mockColumns[0]];

      render(
        <Table
          data={mockData}
          columns={singleColumn}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const headers = screen.getAllByRole('columnheader');
      expect(headers).toHaveLength(1);
    });

    it('should handle complex data types in accessor function', () => {
      interface ComplexData {
        id: number;
        user: { firstName: string; lastName: string };
      }

      const complexData: ComplexData[] = [
        { id: 1, user: { firstName: 'John', lastName: 'Doe' } },
      ];

      const complexColumns = [
        {
          key: 'fullName',
          header: 'Full Name',
          accessor: (item: ComplexData) => `${item.user.firstName} ${item.user.lastName}`,
        },
      ];

      render(
        <Table
          data={complexData}
          columns={complexColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });

    it('should handle zero totalPages gracefully', () => {
      const zeroPagination = { ...mockPagination, totalPages: 0 };

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={zeroPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.queryByTestId('pagination-controls')).not.toBeInTheDocument();
    });

    it('should handle large number of pages', () => {
      const largePagination = { ...mockPagination, totalPages: 100 };

      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={largePagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByLabelText('Page 1')).toBeInTheDocument();
      expect(screen.getByLabelText('Page 100')).toBeInTheDocument();
    });

    it('should handle undefined emptyState prop', () => {
      render(
        <Table
          data={[]}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
          emptyState={undefined}
        />
      );

      expect(screen.getByText('No data available')).toBeInTheDocument();
    });

    it('should handle columns without sortable property', () => {
      const columnsWithoutSortable = [
        { key: 'id', header: 'ID', accessor: 'id' as const },
      ];

      render(
        <Table
          data={mockData}
          columns={columnsWithoutSortable}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const header = screen.getByText('ID').closest('th');
      expect(header).not.toHaveClass('sortable');
    });

    it('should handle columns without width property', () => {
      render(
        <Table
          data={mockData}
          columns={mockColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      const header = screen.getByText('ID').closest('th');
      expect(header).not.toHaveStyle({ width: expect.any(String) });
    });
  });

  describe('Component Export', () => {
    it('should export Table as default export', () => {
      expect(Table).toBeDefined();
      expect(typeof Table).toBe('function');
    });
  });

  describe('Generic Type Support', () => {
    it('should work with different data types', () => {
      interface Product {
        sku: string;
        price: number;
      }

      const products: Product[] = [
        { sku: 'ABC123', price: 99.99 },
        { sku: 'DEF456', price: 149.99 },
      ];

      const productColumns = [
        { key: 'sku', header: 'SKU', accessor: 'sku' as const },
        { key: 'price', header: 'Price', accessor: 'price' as const },
      ];

      render(
        <Table
          data={products}
          columns={productColumns}
          loading={false}
          pagination={mockPagination}
          onPageChange={mockOnPageChange}
        />
      );

      expect(screen.getByText('ABC123')).toBeInTheDocument();
      expect(screen.getByText('99.99')).toBeInTheDocument();
    });
  });
});