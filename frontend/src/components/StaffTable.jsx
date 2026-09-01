import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import StaffTable from './StaffTable';

describe('StaffTable Component', () => {
  const mockStaff = [
    {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      role: 'Nurse',
      facilityName: 'General Hospital'
    },
    {
      id: 2,
      firstName: 'Jane',
      lastName: 'Smith',
      role: 'Doctor',
      facilityName: 'City Clinic'
    },
    {
      id: 3,
      firstName: 'Bob',
      lastName: 'Johnson',
      role: 'Administrator',
      facilityName: 'Medical Center'
    }
  ];

  const mockOnEdit = jest.fn();
  const mockOnDeactivate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Component Rendering', () => {
    test('should render StaffTable component with wrapper div', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const wrapper = container.querySelector('.table-responsive');
      expect(wrapper).toBeInTheDocument();
      expect(wrapper).toHaveStyle({ overflowX: 'auto' });
    });

    test('should render table with correct aria-label', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const table = screen.getByRole('table', { name: 'Staff members list' });
      expect(table).toBeInTheDocument();
    });

    test('should render empty table when staff array is empty', () => {
      render(<StaffTable staff={[]} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
      const tbody = table.querySelector('tbody');
      expect(tbody.children.length).toBe(0);
    });
  });

  describe('Table Header', () => {
    test('should render all header columns with correct text', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByRole('columnheader', { name: 'Name' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'Role' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'Facility' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'Actions' })).toBeInTheDocument();
    });

    test('should have scope="col" attribute on all header cells for WCAG compliance', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const headers = container.querySelectorAll('th');
      headers.forEach(header => {
        expect(header).toHaveAttribute('scope', 'col');
      });
    });
  });

  describe('Table Body - Staff Data Rendering', () => {
    test('should render correct number of staff rows', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const rows = container.querySelectorAll('tbody tr');
      expect(rows.length).toBe(mockStaff.length);
    });

    test('should render staff member name correctly', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    });

    test('should render staff member role correctly', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByText('Nurse')).toBeInTheDocument();
      expect(screen.getByText('Doctor')).toBeInTheDocument();
      expect(screen.getByText('Administrator')).toBeInTheDocument();
    });

    test('should render staff member facility correctly', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByText('General Hospital')).toBeInTheDocument();
      expect(screen.getByText('City Clinic')).toBeInTheDocument();
      expect(screen.getByText('Medical Center')).toBeInTheDocument();
    });

    test('should have data-label attribute on Name cells', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const nameCells = container.querySelectorAll('td[data-label="Name"]');
      expect(nameCells.length).toBe(mockStaff.length);
    });

    test('should have data-label attribute on Role cells', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const roleCells = container.querySelectorAll('td[data-label="Role"]');
      expect(roleCells.length).toBe(mockStaff.length);
    });

    test('should have data-label attribute on Facility cells', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const facilityCells = container.querySelectorAll('td[data-label="Facility"]');
      expect(facilityCells.length).toBe(mockStaff.length);
    });

    test('should have data-label attribute on Actions cells', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const actionCells = container.querySelectorAll('td[data-label="Actions"]');
      expect(actionCells.length).toBe(mockStaff.length);
    });
  });

  describe('Keyboard Navigation - Accessibility', () => {
    test('should have tabIndex={0} on all staff rows for keyboard navigation', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const rows = container.querySelectorAll('tbody tr');
      rows.forEach(row => {
        expect(row).toHaveAttribute('tabIndex', '0');
      });
    });

    test('should allow keyboard focus on table rows', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const firstRow = container.querySelector('tbody tr');
      firstRow.focus();
      expect(firstRow).toHaveFocus();
    });
  });

  describe('Edit Button Functionality', () => {
    test('should render Edit button for each staff member', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const editButtons = screen.getAllByRole('button', { name: /Edit/i });
      expect(editButtons.length).toBe(mockStaff.length);
    });

    test('should have correct aria-label on Edit button', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByRole('button', { name: 'Edit John Doe' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Edit Jane Smith' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Edit Bob Johnson' })).toBeInTheDocument();
    });

    test('should call onEdit with correct staff id when Edit button is clicked', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const editButton = screen.getByRole('button', { name: 'Edit John Doe' });
      fireEvent.click(editButton);
      expect(mockOnEdit).toHaveBeenCalledTimes(1);
      expect(mockOnEdit).toHaveBeenCalledWith(1);
    });

    test('should call onEdit with correct id for multiple staff members', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      
      fireEvent.click(screen.getByRole('button', { name: 'Edit John Doe' }));
      expect(mockOnEdit).toHaveBeenCalledWith(1);
      
      fireEvent.click(screen.getByRole('button', { name: 'Edit Jane Smith' }));
      expect(mockOnEdit).toHaveBeenCalledWith(2);
      
      fireEvent.click(screen.getByRole('button', { name: 'Edit Bob Johnson' }));
      expect(mockOnEdit).toHaveBeenCalledWith(3);
      
      expect(mockOnEdit).toHaveBeenCalledTimes(3);
    });

    test('should be keyboard accessible - Edit button', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const editButton = screen.getByRole('button', { name: 'Edit John Doe' });
      editButton.focus();
      expect(editButton).toHaveFocus();
    });
  });

  describe('Deactivate Button Functionality', () => {
    test('should render Deactivate button for each staff member', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const deactivateButtons = screen.getAllByRole('button', { name: /Deactivate/i });
      expect(deactivateButtons.length).toBe(mockStaff.length);
    });

    test('should have correct aria-label on Deactivate button', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByRole('button', { name: 'Deactivate John Doe' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Deactivate Jane Smith' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Deactivate Bob Johnson' })).toBeInTheDocument();
    });

    test('should call onDeactivate with correct staff id when Deactivate button is clicked', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
      fireEvent.click(deactivateButton);
      expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
      expect(mockOnDeactivate).toHaveBeenCalledWith(1);
    });

    test('should call onDeactivate with correct id for multiple staff members', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      
      fireEvent.click(screen.getByRole('button', { name: 'Deactivate John Doe' }));
      expect(mockOnDeactivate).toHaveBeenCalledWith(1);
      
      fireEvent.click(screen.getByRole('button', { name: 'Deactivate Jane Smith' }));
      expect(mockOnDeactivate).toHaveBeenCalledWith(2);
      
      fireEvent.click(screen.getByRole('button', { name: 'Deactivate Bob Johnson' }));
      expect(mockOnDeactivate).toHaveBeenCalledWith(3);
      
      expect(mockOnDeactivate).toHaveBeenCalledTimes(3);
    });

    test('should be keyboard accessible - Deactivate button', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
      deactivateButton.focus();
      expect(deactivateButton).toHaveFocus();
    });
  });

  describe('Button Interaction Independence', () => {
    test('should not call onDeactivate when Edit button is clicked', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const editButton = screen.getByRole('button', { name: 'Edit John Doe' });
      fireEvent.click(editButton);
      expect(mockOnEdit).toHaveBeenCalledTimes(1);
      expect(mockOnDeactivate).not.toHaveBeenCalled();
    });

    test('should not call onEdit when Deactivate button is clicked', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
      fireEvent.click(deactivateButton);
      expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
      expect(mockOnEdit).not.toHaveBeenCalled();
    });
  });

  describe('Props Validation', () => {
    test('should handle staff prop correctly', () => {
      const singleStaff = [mockStaff[0]];
      render(<StaffTable staff={singleStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });

    test('should handle onEdit callback prop', () => {
      const customOnEdit = jest.fn();
      render(<StaffTable staff={mockStaff} onEdit={customOnEdit} onDeactivate={mockOnDeactivate} />);
      fireEvent.click(screen.getByRole('button', { name: 'Edit John Doe' }));
      expect(customOnEdit).toHaveBeenCalledWith(1);
    });

    test('should handle onDeactivate callback prop', () => {
      const customOnDeactivate = jest.fn();
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={customOnDeactivate} />);
      fireEvent.click(screen.getByRole('button', { name: 'Deactivate John Doe' }));
      expect(customOnDeactivate).toHaveBeenCalledWith(1);
    });
  });

  describe('Responsive Design - CSS Media Query', () => {
    test('should render style tag with mobile media query', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const styleTag = container.querySelector('style');
      expect(styleTag).toBeInTheDocument();
    });

    test('should include media query for max-width 768px in styles', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const styleTag = container.querySelector('style');
      expect(styleTag.textContent).toContain('@media (max-width: 768px)');
    });

    test('should include thead display none rule in mobile styles', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const styleTag = container.querySelector('style');
      expect(styleTag.textContent).toContain('thead');
      expect(styleTag.textContent).toContain('display: none');
    });

    test('should include td display flex rule in mobile styles', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const styleTag = container.querySelector('style');
      expect(styleTag.textContent).toContain('td');
      expect(styleTag.textContent).toContain('display: flex');
    });

    test('should include td::before pseudo-element with data-label content', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const styleTag = container.querySelector('style');
      expect(styleTag.textContent).toContain('td::before');
      expect(styleTag.textContent).toContain('content: attr(data-label)');
      expect(styleTag.textContent).toContain('font-weight: bold');
    });
  });

  describe('WCAG 2.1 AA Compliance', () => {
    test('should have semantic table structure', () => {
      const { container } = render(
        <StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      expect(container.querySelector('table')).toBeInTheDocument();
      expect(container.querySelector('thead')).toBeInTheDocument();
      expect(container.querySelector('tbody')).toBeInTheDocument();
    });

    test('should have aria-label on table for screen readers', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const table = screen.getByRole('table');
      expect(table).toHaveAttribute('aria-label', 'Staff members list');
    });

    test('should have aria-label on all action buttons', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const allButtons = screen.getAllByRole('button');
      allButtons.forEach(button => {
        expect(button).toHaveAttribute('aria-label');
      });
    });

    test('should have unique aria-labels for each staff member actions', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const ariaLabels = screen.getAllByRole('button').map(btn => btn.getAttribute('aria-label'));
      const uniqueLabels = new Set(ariaLabels);
      expect(uniqueLabels.size).toBe(ariaLabels.length);
    });
  });

  describe('Edge Cases', () => {
    test('should handle staff with special characters in names', () => {
      const specialStaff = [{
        id: 99,
        firstName: "O'Brien",
        lastName: "Smith-Jones",
        role: 'Nurse',
        facilityName: 'General Hospital'
      }];
      render(<StaffTable staff={specialStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByText("O'Brien Smith-Jones")).toBeInTheDocument();
    });

    test('should handle staff with long names', () => {
      const longNameStaff = [{
        id: 100,
        firstName: 'Bartholomew',
        lastName: 'Montgomery-Williamson',
        role: 'Senior Consultant Physician',
        facilityName: 'International Medical Research Center'
      }];
      render(<StaffTable staff={longNameStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      expect(screen.getByText('Bartholomew Montgomery-Williamson')).toBeInTheDocument();
    });

    test('should handle multiple clicks on same button', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      const editButton = screen.getByRole('button', { name: 'Edit John Doe' });
      
      fireEvent.click(editButton);
      fireEvent.click(editButton);
      fireEvent.click(editButton);
      
      expect(mockOnEdit).toHaveBeenCalledTimes(3);
      expect(mockOnEdit).toHaveBeenCalledWith(1);
    });

    test('should render correctly with single staff member', () => {
      const singleStaff = [mockStaff[0]];
      const { container } = render(
        <StaffTable staff={singleStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />
      );
      const rows = container.querySelectorAll('tbody tr');
      expect(rows.length).toBe(1);
    });

    test('should handle staff with numeric id types', () => {
      const numericIdStaff = [{
        id: 12345,
        firstName: 'Test',
        lastName: 'User',
        role: 'Tester',
        facilityName: 'Test Facility'
      }];
      render(<StaffTable staff={numericIdStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      fireEvent.click(screen.getByRole('button', { name: 'Edit Test User' }));
      expect(mockOnEdit).toHaveBeenCalledWith(12345);
    });
  });

  describe('Component Export', () => {
    test('should export StaffTable as default export', () => {
      expect(StaffTable).toBeDefined();
      expect(typeof StaffTable).toBe('function');
    });
  });

  describe('Integration - Full User Flow', () => {
    test('should support complete user interaction flow', () => {
      render(<StaffTable staff={mockStaff} onEdit={mockOnEdit} onDeactivate={mockOnDeactivate} />);
      
      // Verify all staff are rendered
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
      
      // Edit first staff
      fireEvent.click(screen.getByRole('button', { name: 'Edit John Doe' }));
      expect(mockOnEdit).toHaveBeenCalledWith(1);
      
      // Deactivate second staff
      fireEvent.click(screen.getByRole('button', { name: 'Deactivate Jane Smith' }));
      expect(mockOnDeactivate).toHaveBeenCalledWith(2);
      
      // Edit third staff
      fireEvent.click(screen.getByRole('button', { name: 'Edit Bob Johnson' }));
      expect(mockOnEdit).toHaveBeenCalledWith(3);
      
      expect(mockOnEdit).toHaveBeenCalledTimes(2);
      expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
    });
  });
});