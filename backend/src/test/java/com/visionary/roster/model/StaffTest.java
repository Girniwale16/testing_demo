package com.visionary.roster.model;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.UpdateStaffRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Staff entity.
 * Ensures 100% coverage of all business logic including the active field functionality.
 */
class StaffTest {

    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = new Staff();
    }

    @Test
    @DisplayName("Test active field exists and has default value true")
    void testActiveFieldDefaultValue() {
        Staff newStaff = new Staff();
        assertTrue(newStaff.isActive(), "Active field should default to true");
    }

    @Test
    @DisplayName("Test isActive() getter returns correct boolean value")
    void testIsActiveGetter() {
        staff.setActive(true);
        assertTrue(staff.isActive(), "isActive() should return true when active is true");
        
        staff.setActive(false);
        assertFalse(staff.isActive(), "isActive() should return false when active is false");
    }

    @Test
    @DisplayName("Test setActive() setter correctly sets boolean value")
    void testSetActiveSetter() {
        staff.setActive(false);
        assertFalse(staff.isActive(), "setActive(false) should set active to false");
        
        staff.setActive(true);
        assertTrue(staff.isActive(), "setActive(true) should set active to true");
    }

    @Test
    @DisplayName("Test active field is updatable")
    void testActiveFieldIsUpdatable() {
        staff.setActive(true);
        assertTrue(staff.isActive());
        
        staff.setActive(false);
        assertFalse(staff.isActive());
        
        staff.setActive(true);
        assertTrue(staff.isActive());
    }

    @Test
    @DisplayName("Test @PreUpdate callback updates updatedAt timestamp")
    void testPreUpdateCallbackUpdatesTimestamp() throws InterruptedException {
        LocalDateTime initialTime = LocalDateTime.now().minusMinutes(5);
        staff.setUpdatedAt(initialTime);
        
        Thread.sleep(10);
        staff.onUpdate();
        
        assertNotNull(staff.getUpdatedAt(), "updatedAt should not be null after onUpdate()");
        assertTrue(staff.getUpdatedAt().isAfter(initialTime), "updatedAt should be updated to current time");
    }

    @Test
    @DisplayName("Test @PreUpdate callback is triggered when active field changes")
    void testPreUpdateCallbackOnActiveFieldChange() throws InterruptedException {
        LocalDateTime initialTime = LocalDateTime.now().minusMinutes(5);
        staff.setUpdatedAt(initialTime);
        staff.setActive(true);
        
        Thread.sleep(10);
        staff.setActive(false);
        staff.onUpdate();
        
        assertTrue(staff.getUpdatedAt().isAfter(initialTime), "updatedAt should be updated when active field changes");
    }

    @Test
    @DisplayName("Test default constructor initializes active to true")
    void testDefaultConstructor() {
        Staff defaultStaff = new Staff();
        assertTrue(defaultStaff.isActive(), "Default constructor should initialize active to true");
    }

    @Test
    @DisplayName("Test toEntity factory method preserves active default value")
    void testToEntityFactoryMethodActiveField() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        
        Staff createdStaff = Staff.toEntity(request, 1L);
        
        assertTrue(createdStaff.isActive(), "toEntity should preserve default active value of true");
    }

    @Test
    @DisplayName("Test toEntity factory method with all fields")
    void testToEntityFactoryMethod() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");
        request.setContact("555-1234");
        request.setRole("Doctor");
        request.setEmploymentStatus("ACTIVE");
        request.setStartDate(LocalDate.of(2023, 1, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));
        
        Staff createdStaff = Staff.toEntity(request, 100L);
        
        assertEquals("Jane", createdStaff.getFirstName());
        assertEquals("Smith", createdStaff.getLastName());
        assertEquals("jane.smith@example.com", createdStaff.getEmail());
        assertEquals("555-1234", createdStaff.getContact());
        assertEquals("Doctor", createdStaff.getRole());
        assertEquals("ACTIVE", createdStaff.getEmploymentStatus());
        assertEquals(LocalDate.of(2023, 1, 1), createdStaff.getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), createdStaff.getEndDate());
        assertEquals(100L, createdStaff.getFacilityId());
        assertTrue(createdStaff.isActive());
    }

    @Test
    @DisplayName("Test toEntity with null employment status defaults to ACTIVE")
    void testToEntityWithNullEmploymentStatus() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setRole("Admin");
        request.setEmploymentStatus(null);
        
        Staff createdStaff = Staff.toEntity(request, 1L);
        
        assertEquals("ACTIVE", createdStaff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only non-null fields")
    void testUpdateFromRequest() {
        staff.setFirstName("Original");
        staff.setLastName("Name");
        staff.setContact("111-1111");
        staff.setRole("Nurse");
        staff.setEmploymentStatus("ACTIVE");
        
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFirstName("Updated");
        request.setRole("Doctor");
        
        staff.updateFromRequest(request);
        
        assertEquals("Updated", staff.getFirstName());
        assertEquals("Name", staff.getLastName());
        assertEquals("111-1111", staff.getContact());
        assertEquals("Doctor", staff.getRole());
        assertEquals("ACTIVE", staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test updateFromRequest with all fields")
    void testUpdateFromRequestAllFields() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");
        request.setContact("999-9999");
        request.setRole("Surgeon");
        request.setEmploymentStatus("INACTIVE");
        request.setStartDate(LocalDate.of(2023, 6, 1));
        request.setEndDate(LocalDate.of(2023, 12, 31));
        
        staff.updateFromRequest(request);
        
        assertEquals("NewFirst", staff.getFirstName());
        assertEquals("NewLast", staff.getLastName());
        assertEquals("999-9999", staff.getContact());
        assertEquals("Surgeon", staff.getRole());
        assertEquals("INACTIVE", staff.getEmploymentStatus());
        assertEquals(LocalDate.of(2023, 6, 1), staff.getStartDate());
        assertEquals(LocalDate.of(2023, 12, 31), staff.getEndDate());
    }

    @Test
    @DisplayName("Test updateFromRequest with all null fields")
    void testUpdateFromRequestAllNullFields() {
        staff.setFirstName("Original");
        staff.setLastName("Name");
        staff.setContact("111-1111");
        staff.setRole("Nurse");
        
        UpdateStaffRequest request = new UpdateStaffRequest();
        
        staff.updateFromRequest(request);
        
        assertEquals("Original", staff.getFirstName());
        assertEquals("Name", staff.getLastName());
        assertEquals("111-1111", staff.getContact());
        assertEquals("Nurse", staff.getRole());
    }

    @Test
    @DisplayName("Test deactivate method sets employment status and end date")
    void testDeactivate() {
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        staff.setEmploymentStatus("ACTIVE");
        
        staff.deactivate(endDate);
        
        assertEquals("INACTIVE", staff.getEmploymentStatus());
        assertEquals(endDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test all getters and setters")
    void testGettersAndSetters() {
        staff.setId(1L);
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setEmail("john@example.com");
        staff.setContact("555-0000");
        staff.setRole("Nurse");
        staff.setEmploymentStatus("ACTIVE");
        staff.setStartDate(LocalDate.of(2023, 1, 1));
        staff.setEndDate(LocalDate.of(2024, 1, 1));
        staff.setFacilityId(10L);
        staff.setActive(false);
        LocalDateTime now = LocalDateTime.now();
        staff.setCreatedAt(now);
        staff.setUpdatedAt(now);
        
        assertEquals(1L, staff.getId());
        assertEquals("John", staff.getFirstName());
        assertEquals("Doe", staff.getLastName());
        assertEquals("john@example.com", staff.getEmail());
        assertEquals("555-0000", staff.getContact());
        assertEquals("Nurse", staff.getRole());
        assertEquals("ACTIVE", staff.getEmploymentStatus());
        assertEquals(LocalDate.of(2023, 1, 1), staff.getStartDate());
        assertEquals(LocalDate.of(2024, 1, 1), staff.getEndDate());
        assertEquals(10L, staff.getFacilityId());
        assertFalse(staff.isActive());
        assertEquals(now, staff.getCreatedAt());
        assertEquals(now, staff.getUpdatedAt());
    }

    @Test
    @DisplayName("Test facility getter and setter")
    void testFacilityGetterSetter() {
        Facility facility = new Facility();
        staff.setFacility(facility);
        
        assertEquals(facility, staff.getFacility());
    }

    @Test
    @DisplayName("Test equals method with same object")
    void testEqualsSameObject() {
        assertTrue(staff.equals(staff));
    }

    @Test
    @DisplayName("Test equals method with null")
    void testEqualsNull() {
        assertFalse(staff.equals(null));
    }

    @Test
    @DisplayName("Test equals method with different class")
    void testEqualsDifferentClass() {
        assertFalse(staff.equals("Not a Staff object"));
    }

    @Test
    @DisplayName("Test equals method with same id")
    void testEqualsSameId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);
        
        Staff staff2 = new Staff();
        staff2.setId(1L);
        
        assertTrue(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test equals method with different id")
    void testEqualsDifferentId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);
        
        Staff staff2 = new Staff();
        staff2.setId(2L);
        
        assertFalse(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test equals method with null ids")
    void testEqualsNullIds() {
        Staff staff1 = new Staff();
        Staff staff2 = new Staff();
        
        assertTrue(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test hashCode method")
    void testHashCode() {
        staff.setId(1L);
        
        assertEquals(staff.hashCode(), staff.hashCode());
        
        Staff staff2 = new Staff();
        staff2.setId(1L);
        
        assertEquals(staff.hashCode(), staff2.hashCode());
    }

    @Test
    @DisplayName("Test toString method includes active field")
    void testToString() {
        staff.setId(1L);
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setEmail("john@example.com");
        staff.setRole("Nurse");
        staff.setEmploymentStatus("ACTIVE");
        staff.setActive(true);
        staff.setStartDate(LocalDate.of(2023, 1, 1));
        staff.setEndDate(LocalDate.of(2024, 1, 1));
        staff.setFacilityId(10L);
        
        String result = staff.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.contains("email='john@example.com'"));
        assertTrue(result.contains("role='Nurse'"));
        assertTrue(result.contains("employmentStatus='ACTIVE'"));
        assertTrue(result.contains("active=true"));
        assertTrue(result.contains("startDate=2023-01-01"));
        assertTrue(result.contains("endDate=2024-01-01"));
        assertTrue(result.contains("facilityId=10"));
    }

    @Test
    @DisplayName("Test toString method with active false")
    void testToStringWithActiveFalse() {
        staff.setActive(false);
        
        String result = staff.toString();
        
        assertTrue(result.contains("active=false"));
    }

    @Test
    @DisplayName("Test active field persistence through multiple operations")
    void testActiveFieldPersistence() {
        staff.setActive(true);
        staff.setFirstName("Test");
        staff.setLastName("User");
        assertTrue(staff.isActive());
        
        staff.setActive(false);
        staff.setEmail("test@example.com");
        assertFalse(staff.isActive());
        
        staff.deactivate(LocalDate.now());
        assertFalse(staff.isActive());
    }

    @Test
    @DisplayName("Test active field with updateFromRequest does not affect active")
    void testActiveFieldNotAffectedByUpdateFromRequest() {
        staff.setActive(true);
        
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFirstName("Updated");
        
        staff.updateFromRequest(request);
        
        assertTrue(staff.isActive(), "Active field should remain unchanged by updateFromRequest");
    }

    @Test
    @DisplayName("Test active field boolean type")
    void testActiveFieldBooleanType() {
        staff.setActive(true);
        Object activeValue = staff.isActive();
        
        assertTrue(activeValue instanceof Boolean, "Active field should be of boolean type");
    }
}