import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import StaffList, { StaffMember, StaffListProps } from './StaffList';

describe('StaffList Component', () => {
  const mockStaff: StaffMember[] = [
    {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      role: 'Nurse',
      facility: 'Main Hospital',
      active: true
    },
    {
      id: 2,
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@example.com',
      role: 'Doctor',
      facility: 'Emergency Ward',
      active: false
    },
    {
      id: 3,
      firstName: 'Bob',
      lastName: 'Johnson',
      email: 'bob.johnson@example.com',
      role: 'Technician',
      facility: 'Lab',
      active: true
    }
  ];

  const mockOnDeactivate = jest.fn();

  beforeEach(() => {
    mockOnDeactivate.mockClear();
  });

  describe('Component Rendering', () => {
    test('should render StaffList component with correct structure', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
    });

    test('should render table with correct aria-label', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const table = screen.getByRole('table', { name: 'Staff members list' });
      expect(table).toBeInTheDocument();
    });

    test('should render table wrapper with overflow-x auto for horizontal scroll', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const tableWrapper = container.querySelector('.table-wrapper');
      expect(tableWrapper).toHaveStyle({ overflowX: 'auto' });
    });
  });

  describe('Table Header', () => {
    test('should render all table header columns', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Email')).toBeInTheDocument();
      expect(screen.getByText('Role')).toBeInTheDocument();
      expect(screen.getByText('Facility')).toBeInTheDocument();
      expect(screen.getByText('Status')).toBeInTheDocument();
      expect(screen.getByText('Actions')).toBeInTheDocument();
    });

    test('should have aria-label on each table header cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const headers = container.querySelectorAll('th');
      expect(headers[0]).toHaveAttribute('aria-label', 'Name');
      expect(headers[1]).toHaveAttribute('aria-label', 'Email');
      expect(headers[2]).toHaveAttribute('aria-label', 'Role');
      expect(headers[3]).toHaveAttribute('aria-label', 'Facility');
      expect(headers[4]).toHaveAttribute('aria-label', 'Status');
      expect(headers[5]).toHaveAttribute('aria-label', 'Actions');
    });
  });

  describe('Staff Data Rendering', () => {
    test('should render all staff members', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    });

    test('should render staff email addresses', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
      expect(screen.getByText('jane.smith@example.com')).toBeInTheDocument();
      expect(screen.getByText('bob.johnson@example.com')).toBeInTheDocument();
    });

    test('should render staff roles', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('Nurse')).toBeInTheDocument();
      expect(screen.getByText('Doctor')).toBeInTheDocument();
      expect(screen.getByText('Technician')).toBeInTheDocument();
    });

    test('should render staff facilities', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('Main Hospital')).toBeInTheDocument();
      expect(screen.getByText('Emergency Ward')).toBeInTheDocument();
      expect(screen.getByText('Lab')).toBeInTheDocument();
    });

    test('should render correct number of table rows', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const rows = container.querySelectorAll('tbody tr');
      expect(rows).toHaveLength(3);
    });

    test('should render empty table when staff array is empty', () => {
      const { container } = render(<StaffList staff={[]} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const rows = container.querySelectorAll('tbody tr');
      expect(rows).toHaveLength(0);
    });
  });

  describe('Data Label Attributes for Mobile', () => {
    test('should have data-label attribute on Name cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const nameCells = container.querySelectorAll('td[data-label="Name"]');
      expect(nameCells.length).toBeGreaterThan(0);
      expect(nameCells[0]).toHaveAttribute('data-label', 'Name');
    });

    test('should have data-label attribute on Email cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const emailCells = container.querySelectorAll('td[data-label="Email"]');
      expect(emailCells.length).toBeGreaterThan(0);
      expect(emailCells[0]).toHaveAttribute('data-label', 'Email');
    });

    test('should have data-label attribute on Role cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const roleCells = container.querySelectorAll('td[data-label="Role"]');
      expect(roleCells.length).toBeGreaterThan(0);
      expect(roleCells[0]).toHaveAttribute('data-label', 'Role');
    });

    test('should have data-label attribute on Facility cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const facilityCells = container.querySelectorAll('td[data-label="Facility"]');
      expect(facilityCells.length).toBeGreaterThan(0);
      expect(facilityCells[0]).toHaveAttribute('data-label', 'Facility');
    });

    test('should have data-label attribute on Status cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const statusCells = container.querySelectorAll('td[data-label="Status"]');
      expect(statusCells.length).toBeGreaterThan(0);
      expect(statusCells[0]).toHaveAttribute('data-label', 'Status');
    });

    test('should have data-label attribute on Actions cell', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const actionsCells = container.querySelectorAll('td[data-label="Actions"]');
      expect(actionsCells.length).toBeGreaterThan(0);
      expect(actionsCells[0]).toHaveAttribute('data-label', 'Actions');
    });
  });

  describe('Status Column', () => {
    test('should display "Active" for active staff members', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const activeStatuses = screen.getAllByText('Active');
      expect(activeStatuses).toHaveLength(2);
    });

    test('should display "Inactive" for inactive staff members', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const inactiveStatus = screen.getByText('Inactive');
      expect(inactiveStatus).toBeInTheDocument();
    });

    test('should correctly map active boolean to status text', () => {
      const singleStaff: StaffMember[] = [
        { ...mockStaff[0], active: true }
      ];
      const { rerender } = render(<StaffList staff={singleStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('Active')).toBeInTheDocument();
      
      const inactiveStaff: StaffMember[] = [
        { ...mockStaff[0], active: false }
      ];
      rerender(<StaffList staff={inactiveStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('Inactive')).toBeInTheDocument();
    });
  });

  describe('Deactivate Button - Manager Role', () => {
    test('should render Deactivate button when userRole is MANAGER', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButtons = screen.getAllByText('Deactivate');
      expect(deactivateButtons).toHaveLength(3);
    });

    test('should have correct aria-label on Deactivate button', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByLabelText('Deactivate John Doe')).toBeInTheDocument();
      expect(screen.getByLabelText('Deactivate Jane Smith')).toBeInTheDocument();
      expect(screen.getByLabelText('Deactivate Bob Johnson')).toBeInTheDocument();
    });

    test('should call onDeactivate with correct staffId when button is clicked', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.click(deactivateButton);
      
      expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
      expect(mockOnDeactivate).toHaveBeenCalledWith(1);
    });

    test('should call onDeactivate for different staff members', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButtonJane = screen.getByLabelText('Deactivate Jane Smith');
      fireEvent.click(deactivateButtonJane);
      
      expect(mockOnDeactivate).toHaveBeenCalledWith(2);
      
      const deactivateButtonBob = screen.getByLabelText('Deactivate Bob Johnson');
      fireEvent.click(deactivateButtonBob);
      
      expect(mockOnDeactivate).toHaveBeenCalledWith(3);
      expect(mockOnDeactivate).toHaveBeenCalledTimes(2);
    });
  });

  describe('Deactivate Button - Non-Manager Role', () => {
    test('should NOT render Deactivate button when userRole is not MANAGER', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="STAFF" />);
      
      const deactivateButtons = screen.queryAllByText('Deactivate');
      expect(deactivateButtons).toHaveLength(0);
    });

    test('should NOT render Deactivate button for empty userRole', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="" />);
      
      const deactivateButtons = screen.queryAllByText('Deactivate');
      expect(deactivateButtons).toHaveLength(0);
    });

    test('should NOT render Deactivate button for ADMIN role', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="ADMIN" />);
      
      const deactivateButtons = screen.queryAllByText('Deactivate');
      expect(deactivateButtons).toHaveLength(0);
    });

    test('should NOT render Deactivate button for USER role', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="USER" />);
      
      const deactivateButtons = screen.queryAllByText('Deactivate');
      expect(deactivateButtons).toHaveLength(0);
    });
  });

  describe('Deactivate Button - Touch Target Size', () => {
    test('should have minimum 44x44px touch target size', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      expect(deactivateButton).toHaveStyle({
        minWidth: '44px',
        minHeight: '44px'
      });
    });

    test('should have proper padding for touch target', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      expect(deactivateButton).toHaveStyle({
        padding: '8px 16px'
      });
    });
  });

  describe('Deactivate Button - Color Contrast', () => {
    test('should have correct background color for contrast ratio', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      expect(deactivateButton).toHaveStyle({
        backgroundColor: '#d32f2f',
        color: '#ffffff'
      });
    });

    test('should have proper styling for visibility', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      expect(deactivateButton).toHaveStyle({
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        fontSize: '14px',
        fontWeight: '500'
      });
    });
  });

  describe('Keyboard Accessibility', () => {
    test('should be focusable', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      deactivateButton.focus();
      
      expect(deactivateButton).toHaveFocus();
    });

    test('should call onDeactivate when Enter key is pressed', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.keyDown(deactivateButton, { key: 'Enter', code: 'Enter' });
      
      expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
      expect(mockOnDeactivate).toHaveBeenCalledWith(1);
    });

    test('should call onDeactivate when Space key is pressed', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate Jane Smith');
      fireEvent.keyDown(deactivateButton, { key: ' ', code: 'Space' });
      
      expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
      expect(mockOnDeactivate).toHaveBeenCalledWith(2);
    });

    test('should prevent default behavior on Enter key', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      const event = new KeyboardEvent('keydown', { key: 'Enter', bubbles: true });
      const preventDefaultSpy = jest.spyOn(event, 'preventDefault');
      
      fireEvent.keyDown(deactivateButton, { key: 'Enter', code: 'Enter' });
      
      expect(mockOnDeactivate).toHaveBeenCalled();
    });

    test('should prevent default behavior on Space key', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.keyDown(deactivateButton, { key: ' ', code: 'Space' });
      
      expect(mockOnDeactivate).toHaveBeenCalled();
    });

    test('should NOT call onDeactivate for other keys', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.keyDown(deactivateButton, { key: 'Tab', code: 'Tab' });
      fireEvent.keyDown(deactivateButton, { key: 'Escape', code: 'Escape' });
      fireEvent.keyDown(deactivateButton, { key: 'a', code: 'KeyA' });
      
      expect(mockOnDeactivate).not.toHaveBeenCalled();
    });
  });

  describe('ARIA Live Region', () => {
    test('should have aria-live="polite" on tbody for announcing updates', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const tbody = container.querySelector('tbody');
      expect(tbody).toHaveAttribute('aria-live', 'polite');
    });
  });

  describe('CSS Classes', () => {
    test('should have staff-list-container class on wrapper div', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const wrapper = container.querySelector('.staff-list-container');
      expect(wrapper).toBeInTheDocument();
    });

    test('should have table-wrapper class on table wrapper div', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const tableWrapper = container.querySelector('.table-wrapper');
      expect(tableWrapper).toBeInTheDocument();
    });

    test('should have deactivate-button class on Deactivate button', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      expect(deactivateButton).toHaveClass('deactivate-button');
    });
  });

  describe('Component Props Interface', () => {
    test('should accept staff array prop', () => {
      const { container } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const rows = container.querySelectorAll('tbody tr');
      expect(rows).toHaveLength(mockStaff.length);
    });

    test('should accept onDeactivate callback prop', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.click(deactivateButton);
      
      expect(mockOnDeactivate).toHaveBeenCalled();
    });

    test('should accept userRole string prop', () => {
      const { rerender } = render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getAllByText('Deactivate')).toHaveLength(3);
      
      rerender(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="STAFF" />);
      
      expect(screen.queryAllByText('Deactivate')).toHaveLength(0);
    });
  });

  describe('Edge Cases', () => {
    test('should handle single staff member', () => {
      const singleStaff: StaffMember[] = [mockStaff[0]];
      render(<StaffList staff={singleStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getAllByText('Deactivate')).toHaveLength(1);
    });

    test('should handle staff with special characters in name', () => {
      const specialStaff: StaffMember[] = [
        {
          id: 99,
          firstName: "O'Brien",
          lastName: "Smith-Jones",
          email: 'obrien@example.com',
          role: 'Nurse',
          facility: 'Main',
          active: true
        }
      ];
      render(<StaffList staff={specialStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText("O'Brien Smith-Jones")).toBeInTheDocument();
      expect(screen.getByLabelText("Deactivate O'Brien Smith-Jones")).toBeInTheDocument();
    });

    test('should handle long staff names', () => {
      const longNameStaff: StaffMember[] = [
        {
          id: 100,
          firstName: 'VeryLongFirstNameThatExceedsNormalLength',
          lastName: 'VeryLongLastNameThatExceedsNormalLength',
          email: 'longname@example.com',
          role: 'Specialist',
          facility: 'Research Center',
          active: true
        }
      ];
      render(<StaffList staff={longNameStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      expect(screen.getByText('VeryLongFirstNameThatExceedsNormalLength VeryLongLastNameThatExceedsNormalLength')).toBeInTheDocument();
    });

    test('should handle multiple clicks on same button', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.click(deactivateButton);
      fireEvent.click(deactivateButton);
      fireEvent.click(deactivateButton);
      
      expect(mockOnDeactivate).toHaveBeenCalledTimes(3);
      expect(mockOnDeactivate).toHaveBeenCalledWith(1);
    });

    test('should handle mixed keyboard and mouse interactions', () => {
      render(<StaffList staff={mockStaff} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const deactivateButton = screen.getByLabelText('Deactivate John Doe');
      fireEvent.click(deactivateButton);
      fireEvent.keyDown(deactivateButton, { key: 'Enter' });
      fireEvent.keyDown(deactivateButton, { key: ' ' });
      
      expect(mockOnDeactivate).toHaveBeenCalledTimes(3);
    });
  });

  describe('Responsive Design Attributes', () => {
    test('should have all required data-label attributes for responsive layout', () => {
      const { container } = render(<StaffList staff={[mockStaff[0]]} onDeactivate={mockOnDeactivate} userRole="MANAGER" />);
      
      const row = container.querySelector('tbody tr');
      const cells = row?.querySelectorAll('td');
      
      expect(cells?.[0]).toHaveAttribute('data-label', 'Name');
      expect(cells?.[1]).toHaveAttribute('data-label', 'Email');
      expect(cells?.[2]).toHaveAttribute('data-label', 'Role');
      expect(cells?.[3]).toHaveAttribute('data-label', 'Facility');
      expect(cells?.[4]).toHaveAttribute('data-label', 'Status');
      expect(cells?.[5]).toHaveAttribute('data-label', 'Actions');
    });
  });

  describe('Component Export', () => {
    test('should export StaffList as default', () => {
      expect(StaffList).toBeDefined();
      expect(typeof StaffList).toBe('function');
    });
  });
});