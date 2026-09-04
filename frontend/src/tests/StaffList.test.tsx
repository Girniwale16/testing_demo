import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import StaffList from '../components/StaffList';

const mockStaffData = [
  {
    id: 123,
    name: 'John Doe',
    email: 'john.doe@example.com',
    role: 'Nurse',
    facility: 'General Hospital',
    active: true,
  },
  {
    id: 124,
    name: 'Jane Smith',
    email: 'jane.smith@example.com',
    role: 'Doctor',
    facility: 'City Clinic',
    active: true,
  },
  {
    id: 125,
    name: 'Bob Johnson',
    email: 'bob.johnson@example.com',
    role: 'Technician',
    facility: 'Medical Center',
    active: false,
  },
];

describe('StaffList Component', () => {
  test('renders staff list table with correct accessibility attributes', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const table = screen.getByRole('table');
    expect(table).toBeInTheDocument();
    expect(table).toHaveAttribute('aria-label', 'Staff members list');
  });

  test('displays all staff members in table rows', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const rows = screen.getAllByRole('row');
    expect(rows).toHaveLength(4);
  });

  test('displays staff name, email, role, facility, and status in table cells', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
    expect(screen.getByText('Nurse')).toBeInTheDocument();
    expect(screen.getByText('General Hospital')).toBeInTheDocument();
    
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@example.com')).toBeInTheDocument();
    expect(screen.getByText('Doctor')).toBeInTheDocument();
    expect(screen.getByText('City Clinic')).toBeInTheDocument();
    
    expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    expect(screen.getByText('bob.johnson@example.com')).toBeInTheDocument();
    expect(screen.getByText('Technician')).toBeInTheDocument();
    expect(screen.getByText('Medical Center')).toBeInTheDocument();
  });

  test('renders Deactivate button only when userRole is MANAGER', () => {
    const mockOnDeactivate = jest.fn();
    const { rerender } = render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const deactivateButtons = screen.getAllByRole('button', { name: /Deactivate/i });
    expect(deactivateButtons.length).toBeGreaterThan(0);
    
    rerender(<StaffList staff={mockStaffData} userRole="STAFF" onDeactivate={mockOnDeactivate} />);
    
    const noButtons = screen.queryAllByRole('button', { name: /Deactivate/i });
    expect(noButtons).toHaveLength(0);
  });

  test('Deactivate button has correct aria-label with staff name', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
    expect(deactivateButton).toBeInTheDocument();
    expect(deactivateButton).toHaveAttribute('aria-label', 'Deactivate John Doe');
  });

  test('calls onDeactivate with correct staffId when Deactivate button is clicked', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
    fireEvent.click(deactivateButton);
    
    expect(mockOnDeactivate).toHaveBeenCalledWith(123);
    expect(mockOnDeactivate).toHaveBeenCalledTimes(1);
  });

  test('Deactivate button has minimum 44x44px touch target size', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
    const styles = window.getComputedStyle(deactivateButton);
    const width = parseInt(styles.width);
    const height = parseInt(styles.height);
    
    expect(width).toBeGreaterThanOrEqual(44);
    expect(height).toBeGreaterThanOrEqual(44);
  });

  test('Deactivate button has minimum 4.5:1 color contrast ratio', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
    const styles = window.getComputedStyle(deactivateButton);
    
    const getLuminance = (rgb: string) => {
      const [r, g, b] = rgb.match(/\d+/g)!.map(Number);
      const [rs, gs, bs] = [r, g, b].map(val => {
        const s = val / 255;
        return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
      });
      return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs;
    };
    
    const bgColor = styles.backgroundColor;
    const textColor = styles.color;
    
    const bgLuminance = getLuminance(bgColor);
    const textLuminance = getLuminance(textColor);
    
    const contrastRatio = (Math.max(bgLuminance, textLuminance) + 0.05) / (Math.min(bgLuminance, textLuminance) + 0.05);
    
    expect(contrastRatio).toBeGreaterThanOrEqual(4.5);
  });

  test('Deactivate button is keyboard accessible', () => {
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const deactivateButton = screen.getByRole('button', { name: 'Deactivate John Doe' });
    deactivateButton.focus();
    
    expect(deactivateButton).toHaveFocus();
    
    fireEvent.keyDown(deactivateButton, { key: 'Enter', code: 'Enter' });
    
    expect(mockOnDeactivate).toHaveBeenCalledWith(123);
  });

  test('table stacks cells vertically on mobile viewport (≤768px)', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    });
    
    window.dispatchEvent(new Event('resize'));
    
    const mockOnDeactivate = jest.fn();
    const { container } = render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const tableCells = container.querySelectorAll('td');
    if (tableCells.length > 0) {
      const styles = window.getComputedStyle(tableCells[0]);
      expect(styles.display).toMatch(/flex|block/);
    }
  });

  test('table displays full layout on desktop viewport (≥769px)', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1024,
    });
    
    window.dispatchEvent(new Event('resize'));
    
    const mockOnDeactivate = jest.fn();
    render(<StaffList staff={mockStaffData} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const table = screen.getByRole('table');
    const styles = window.getComputedStyle(table);
    
    expect(styles.display).toMatch(/table/);
  });

  test('displays Active status for active staff members', () => {
    const mockOnDeactivate = jest.fn();
    const activeStaff = [
      {
        id: 123,
        name: 'John Doe',
        email: 'john.doe@example.com',
        role: 'Nurse',
        facility: 'General Hospital',
        active: true,
      },
    ];
    
    render(<StaffList staff={activeStaff} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  test('displays Inactive status for inactive staff members', () => {
    const mockOnDeactivate = jest.fn();
    const inactiveStaff = [
      {
        id: 125,
        name: 'Bob Johnson',
        email: 'bob.johnson@example.com',
        role: 'Technician',
        facility: 'Medical Center',
        active: false,
      },
    ];
    
    render(<StaffList staff={inactiveStaff} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });

  test('table has horizontal scroll on mobile for overflow content', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    });
    
    window.dispatchEvent(new Event('resize'));
    
    const mockOnDeactivate = jest.fn();
    const wideContentStaff = [
      {
        id: 123,
        name: 'John Doe with a very long name that exceeds mobile width',
        email: 'john.doe.with.very.long.email@example.com',
        role: 'Senior Nurse Practitioner',
        facility: 'General Hospital Medical Center',
        active: true,
      },
    ];
    
    const { container } = render(<StaffList staff={wideContentStaff} userRole="MANAGER" onDeactivate={mockOnDeactivate} />);
    
    const tableWrapper = container.querySelector('[style*="overflow"]') || container.firstChild;
    if (tableWrapper) {
      const styles = window.getComputedStyle(tableWrapper as Element);
      expect(styles.overflowX).toMatch(/auto|scroll/);
    }
  });
});