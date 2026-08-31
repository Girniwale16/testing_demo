package com.visionary.roster.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Staff entity class.
 * Tests cover all fields, getters, setters, and business logic methods.
 */
@DisplayName("Staff Entity Tests")
class StaffTest {

    private Staff staff;
    private Facility facility;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        facility = new Facility();
        facility.setId(1L);
        facility.setName("Test Facility");
    }

    @Test
    @DisplayName("Test default constructor creates instance with ACTIVE status")
    void testDefaultConstructor() {
        Staff newStaff = new Staff();
        assertNotNull(newStaff);
        assertEquals("ACTIVE", newStaff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test getId and setId")
    void testIdGetterAndSetter() {
        Long expectedId = 100L;
        staff.setId(expectedId);
        assertEquals(expectedId, staff.getId());
    }

    @Test
    @DisplayName("Test getFirstName and setFirstName")
    void testFirstNameGetterAndSetter() {
        String expectedFirstName = "John";
        staff.setFirstName(expectedFirstName);
        assertEquals(expectedFirstName, staff.getFirstName());
    }

    @Test
    @DisplayName("Test setFirstName with null value")
    void testFirstNameWithNull() {
        staff.setFirstName(null);
        assertNull(staff.getFirstName());
    }

    @Test
    @DisplayName("Test setFirstName with maximum length")
    void testFirstNameWithMaxLength() {
        String longName = "A".repeat(100);
        staff.setFirstName(longName);
        assertEquals(100, staff.getFirstName().length());
    }

    @Test
    @DisplayName("Test getLastName and setLastName")
    void testLastNameGetterAndSetter() {
        String expectedLastName = "Doe";
        staff.setLastName(expectedLastName);
        assertEquals(expectedLastName, staff.getLastName());
    }

    @Test
    @DisplayName("Test setLastName with null value")
    void testLastNameWithNull() {
        staff.setLastName(null);
        assertNull(staff.getLastName());
    }

    @Test
    @DisplayName("Test setLastName with maximum length")
    void testLastNameWithMaxLength() {
        String longName = "B".repeat(100);
        staff.setLastName(longName);
        assertEquals(100, staff.getLastName().length());
    }

    @Test
    @DisplayName("Test getEmail and setEmail")
    void testEmailGetterAndSetter() {
        String expectedEmail = "john.doe@example.com";
        staff.setEmail(expectedEmail);
        assertEquals(expectedEmail, staff.getEmail());
    }

    @Test
    @DisplayName("Test setEmail with null value")
    void testEmailWithNull() {
        staff.setEmail(null);
        assertNull(staff.getEmail());
    }

    @Test
    @DisplayName("Test setEmail with maximum length")
    void testEmailWithMaxLength() {
        String longEmail = "a".repeat(245) + "@email.com";
        staff.setEmail(longEmail);
        assertEquals(255, staff.getEmail().length());
    }

    @Test
    @DisplayName("Test getRole and setRole")
    void testRoleGetterAndSetter() {
        String expectedRole = "Nurse";
        staff.setRole(expectedRole);
        assertEquals(expectedRole, staff.getRole());
    }

    @Test
    @DisplayName("Test setRole with null value")
    void testRoleWithNull() {
        staff.setRole(null);
        assertNull(staff.getRole());
    }

    @Test
    @DisplayName("Test setRole with maximum length")
    void testRoleWithMaxLength() {
        String longRole = "R".repeat(50);
        staff.setRole(longRole);
        assertEquals(50, staff.getRole().length());
    }

    @Test
    @DisplayName("Test getEmploymentStatus and setEmploymentStatus")
    void testEmploymentStatusGetterAndSetter() {
        String expectedStatus = "INACTIVE";
        staff.setEmploymentStatus(expectedStatus);
        assertEquals(expectedStatus, staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test default employment status is ACTIVE")
    void testDefaultEmploymentStatus() {
        Staff newStaff = new Staff();
        assertEquals("ACTIVE", newStaff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test setEmploymentStatus with various values")
    void testEmploymentStatusWithVariousValues() {
        staff.setEmploymentStatus("ACTIVE");
        assertEquals("ACTIVE", staff.getEmploymentStatus());
        
        staff.setEmploymentStatus("INACTIVE");
        assertEquals("INACTIVE", staff.getEmploymentStatus());
        
        staff.setEmploymentStatus("ON_LEAVE");
        assertEquals("ON_LEAVE", staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test getEndDate and setEndDate")
    void testEndDateGetterAndSetter() {
        LocalDate expectedEndDate = LocalDate.of(2024, 12, 31);
        staff.setEndDate(expectedEndDate);
        assertEquals(expectedEndDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test setEndDate with null value")
    void testEndDateWithNull() {
        staff.setEndDate(null);
        assertNull(staff.getEndDate());
    }

    @Test
    @DisplayName("Test setEndDate with past date")
    void testEndDateWithPastDate() {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        staff.setEndDate(pastDate);
        assertEquals(pastDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test setEndDate with future date")
    void testEndDateWithFutureDate() {
        LocalDate futureDate = LocalDate.of(2025, 12, 31);
        staff.setEndDate(futureDate);
        assertEquals(futureDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test getFacility and setFacility")
    void testFacilityGetterAndSetter() {
        staff.setFacility(facility);
        assertEquals(facility, staff.getFacility());
        assertEquals(1L, staff.getFacility().getId());
    }

    @Test
    @DisplayName("Test setFacility with null value")
    void testFacilityWithNull() {
        staff.setFacility(null);
        assertNull(staff.getFacility());
    }

    @Test
    @DisplayName("Test getCreatedAt and setCreatedAt")
    void testCreatedAtGetterAndSetter() {
        LocalDateTime expectedCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        staff.setCreatedAt(expectedCreatedAt);
        assertEquals(expectedCreatedAt, staff.getCreatedAt());
    }

    @Test
    @DisplayName("Test setCreatedAt with null value")
    void testCreatedAtWithNull() {
        staff.setCreatedAt(null);
        assertNull(staff.getCreatedAt());
    }

    @Test
    @DisplayName("Test getUpdatedAt and setUpdatedAt")
    void testUpdatedAtGetterAndSetter() {
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2024, 6, 1, 15, 30);
        staff.setUpdatedAt(expectedUpdatedAt);
        assertEquals(expectedUpdatedAt, staff.getUpdatedAt());
    }

    @Test
    @DisplayName("Test setUpdatedAt with null value")
    void testUpdatedAtWithNull() {
        staff.setUpdatedAt(null);
        assertNull(staff.getUpdatedAt());
    }

    @Test
    @DisplayName("Test isActive returns true when status is ACTIVE")
    void testIsActiveReturnsTrueForActiveStatus() {
        staff.setEmploymentStatus("ACTIVE");
        assertTrue(staff.isActive());
    }

    @Test
    @DisplayName("Test isActive returns false when status is INACTIVE")
    void testIsActiveReturnsFalseForInactiveStatus() {
        staff.setEmploymentStatus("INACTIVE");
        assertFalse(staff.isActive());
    }

    @Test
    @DisplayName("Test isActive returns false when status is null")
    void testIsActiveReturnsFalseForNullStatus() {
        staff.setEmploymentStatus(null);
        assertFalse(staff.isActive());
    }

    @Test
    @DisplayName("Test isActive returns false for other status values")
    void testIsActiveReturnsFalseForOtherStatuses() {
        staff.setEmploymentStatus("ON_LEAVE");
        assertFalse(staff.isActive());
        
        staff.setEmploymentStatus("SUSPENDED");
        assertFalse(staff.isActive());
        
        staff.setEmploymentStatus("TERMINATED");
        assertFalse(staff.isActive());
    }

    @Test
    @DisplayName("Test isActive is case-sensitive")
    void testIsActiveCaseSensitive() {
        staff.setEmploymentStatus("active");
        assertFalse(staff.isActive());
        
        staff.setEmploymentStatus("Active");
        assertFalse(staff.isActive());
        
        staff.setEmploymentStatus("ACTIVE");
        assertTrue(staff.isActive());
    }

    @Test
    @DisplayName("Test deactivate method sets status to INACTIVE")
    void testDeactivateSetsStatusToInactive() {
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        staff.setEmploymentStatus("ACTIVE");
        
        staff.deactivate(endDate);
        
        assertEquals("INACTIVE", staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test deactivate method sets end date")
    void testDeactivateSetsEndDate() {
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        staff.deactivate(endDate);
        
        assertEquals(endDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test deactivate method with null end date")
    void testDeactivateWithNullEndDate() {
        staff.setEmploymentStatus("ACTIVE");
        
        staff.deactivate(null);
        
        assertEquals("INACTIVE", staff.getEmploymentStatus());
        assertNull(staff.getEndDate());
    }

    @Test
    @DisplayName("Test deactivate method changes active staff to inactive")
    void testDeactivateChangesActiveToInactive() {
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        staff.setEmploymentStatus("ACTIVE");
        assertTrue(staff.isActive());
        
        staff.deactivate(endDate);
        
        assertFalse(staff.isActive());
        assertEquals("INACTIVE", staff.getEmploymentStatus());
        assertEquals(endDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test deactivate can be called multiple times")
    void testDeactivateMultipleTimes() {
        LocalDate firstEndDate = LocalDate.of(2024, 6, 30);
        LocalDate secondEndDate = LocalDate.of(2024, 12, 31);
        
        staff.deactivate(firstEndDate);
        assertEquals(firstEndDate, staff.getEndDate());
        
        staff.deactivate(secondEndDate);
        assertEquals(secondEndDate, staff.getEndDate());
        assertEquals("INACTIVE", staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test complete staff lifecycle")
    void testCompleteStaffLifecycle() {
        // Create new staff
        Staff newStaff = new Staff();
        newStaff.setId(1L);
        newStaff.setFirstName("Jane");
        newStaff.setLastName("Smith");
        newStaff.setEmail("jane.smith@example.com");
        newStaff.setRole("Doctor");
        newStaff.setFacility(facility);
        newStaff.setCreatedAt(LocalDateTime.now());
        newStaff.setUpdatedAt(LocalDateTime.now());
        
        // Verify initial state
        assertEquals("ACTIVE", newStaff.getEmploymentStatus());
        assertTrue(newStaff.isActive());
        assertNull(newStaff.getEndDate());
        
        // Deactivate staff
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        newStaff.deactivate(endDate);
        
        // Verify deactivated state
        assertEquals("INACTIVE", newStaff.getEmploymentStatus());
        assertFalse(newStaff.isActive());
        assertEquals(endDate, newStaff.getEndDate());
    }

    @Test
    @DisplayName("Test all fields can be set and retrieved")
    void testAllFieldsSetAndGet() {
        Long id = 1L;
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String role = "Nurse";
        String employmentStatus = "ACTIVE";
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 1, 15, 30);
        
        staff.setId(id);
        staff.setFirstName(firstName);
        staff.setLastName(lastName);
        staff.setEmail(email);
        staff.setRole(role);
        staff.setEmploymentStatus(employmentStatus);
        staff.setEndDate(endDate);
        staff.setFacility(facility);
        staff.setCreatedAt(createdAt);
        staff.setUpdatedAt(updatedAt);
        
        assertEquals(id, staff.getId());
        assertEquals(firstName, staff.getFirstName());
        assertEquals(lastName, staff.getLastName());
        assertEquals(email, staff.getEmail());
        assertEquals(role, staff.getRole());
        assertEquals(employmentStatus, staff.getEmploymentStatus());
        assertEquals(endDate, staff.getEndDate());
        assertEquals(facility, staff.getFacility());
        assertEquals(createdAt, staff.getCreatedAt());
        assertEquals(updatedAt, staff.getUpdatedAt());
    }

    @Test
    @DisplayName("Test entity annotations are present")
    void testEntityAnnotations() {
        assertTrue(Staff.class.isAnnotationPresent(Entity.class));
        assertTrue(Staff.class.isAnnotationPresent(Table.class));
        
        Table tableAnnotation = Staff.class.getAnnotation(Table.class);
        assertEquals("staff", tableAnnotation.name());
    }
}