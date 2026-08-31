package com.visionary.roster.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for StaffMember entity
 * Ensures 100% coverage of all business logic, constructors, getters, setters, equals, hashCode, and toString
 */
@DisplayName("StaffMember Entity Tests")
class StaffMemberTest {

    private StaffMember staffMember;
    private LocalDate testStartDate;
    private LocalDate testEndDate;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        staffMember = new StaffMember();
        testStartDate = LocalDate.of(2023, 1, 15);
        testEndDate = LocalDate.of(2024, 12, 31);
        testCreatedAt = LocalDateTime.of(2023, 1, 15, 10, 0, 0);
        testUpdatedAt = LocalDateTime.of(2024, 6, 20, 14, 30, 0);
    }

    @Test
    @DisplayName("Test no-argument constructor creates empty StaffMember")
    void testNoArgumentConstructor() {
        StaffMember newStaffMember = new StaffMember();
        assertNotNull(newStaffMember);
        assertNull(newStaffMember.getStaffMemberId());
        assertNull(newStaffMember.getName());
        assertNull(newStaffMember.getContact());
        assertNull(newStaffMember.getRole());
        assertNull(newStaffMember.getEmploymentStatus());
        assertNull(newStaffMember.getFacilityId());
        assertNull(newStaffMember.getStartDate());
        assertNull(newStaffMember.getEndDate());
        assertNull(newStaffMember.getCreatedAt());
        assertNull(newStaffMember.getUpdatedAt());
    }

    @Test
    @DisplayName("Test all-arguments constructor initializes all fields correctly")
    void testAllArgumentsConstructor() {
        StaffMember newStaffMember = new StaffMember(
                1L,
                "John Doe",
                "john.doe@example.com",
                "Nurse",
                "ACTIVE",
                100L,
                testStartDate,
                testEndDate,
                testCreatedAt,
                testUpdatedAt
        );

        assertEquals(1L, newStaffMember.getStaffMemberId());
        assertEquals("John Doe", newStaffMember.getName());
        assertEquals("john.doe@example.com", newStaffMember.getContact());
        assertEquals("Nurse", newStaffMember.getRole());
        assertEquals("ACTIVE", newStaffMember.getEmploymentStatus());
        assertEquals(100L, newStaffMember.getFacilityId());
        assertEquals(testStartDate, newStaffMember.getStartDate());
        assertEquals(testEndDate, newStaffMember.getEndDate());
        assertEquals(testCreatedAt, newStaffMember.getCreatedAt());
        assertEquals(testUpdatedAt, newStaffMember.getUpdatedAt());
    }

    @Test
    @DisplayName("Test staffMemberId getter and setter")
    void testStaffMemberIdGetterAndSetter() {
        assertNull(staffMember.getStaffMemberId());
        staffMember.setStaffMemberId(42L);
        assertEquals(42L, staffMember.getStaffMemberId());
    }

    @Test
    @DisplayName("Test name getter and setter")
    void testNameGetterAndSetter() {
        assertNull(staffMember.getName());
        staffMember.setName("Jane Smith");
        assertEquals("Jane Smith", staffMember.getName());
    }

    @Test
    @DisplayName("Test contact getter and setter")
    void testContactGetterAndSetter() {
        assertNull(staffMember.getContact());
        staffMember.setContact("jane.smith@hospital.com");
        assertEquals("jane.smith@hospital.com", staffMember.getContact());
    }

    @Test
    @DisplayName("Test role getter and setter")
    void testRoleGetterAndSetter() {
        assertNull(staffMember.getRole());
        staffMember.setRole("Doctor");
        assertEquals("Doctor", staffMember.getRole());
    }

    @Test
    @DisplayName("Test employmentStatus getter and setter with ACTIVE status")
    void testEmploymentStatusGetterAndSetterActive() {
        assertNull(staffMember.getEmploymentStatus());
        staffMember.setEmploymentStatus("ACTIVE");
        assertEquals("ACTIVE", staffMember.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test employmentStatus getter and setter with INACTIVE status")
    void testEmploymentStatusGetterAndSetterInactive() {
        staffMember.setEmploymentStatus("INACTIVE");
        assertEquals("INACTIVE", staffMember.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test employmentStatus getter and setter with TERMINATED status")
    void testEmploymentStatusGetterAndSetterTerminated() {
        staffMember.setEmploymentStatus("TERMINATED");
        assertEquals("TERMINATED", staffMember.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test facilityId getter and setter")
    void testFacilityIdGetterAndSetter() {
        assertNull(staffMember.getFacilityId());
        staffMember.setFacilityId(500L);
        assertEquals(500L, staffMember.getFacilityId());
    }

    @Test
    @DisplayName("Test startDate getter and setter")
    void testStartDateGetterAndSetter() {
        assertNull(staffMember.getStartDate());
        staffMember.setStartDate(testStartDate);
        assertEquals(testStartDate, staffMember.getStartDate());
    }

    @Test
    @DisplayName("Test endDate getter and setter")
    void testEndDateGetterAndSetter() {
        assertNull(staffMember.getEndDate());
        staffMember.setEndDate(testEndDate);
        assertEquals(testEndDate, staffMember.getEndDate());
    }

    @Test
    @DisplayName("Test createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        assertNull(staffMember.getCreatedAt());
        staffMember.setCreatedAt(testCreatedAt);
        assertEquals(testCreatedAt, staffMember.getCreatedAt());
    }

    @Test
    @DisplayName("Test updatedAt getter and setter")
    void testUpdatedAtGetterAndSetter() {
        assertNull(staffMember.getUpdatedAt());
        staffMember.setUpdatedAt(testUpdatedAt);
        assertEquals(testUpdatedAt, staffMember.getUpdatedAt());
    }

    @Test
    @DisplayName("Test equals method with same object returns true")
    void testEqualsWithSameObject() {
        staffMember.setStaffMemberId(1L);
        assertTrue(staffMember.equals(staffMember));
    }

    @Test
    @DisplayName("Test equals method with null returns false")
    void testEqualsWithNull() {
        staffMember.setStaffMemberId(1L);
        assertFalse(staffMember.equals(null));
    }

    @Test
    @DisplayName("Test equals method with different class returns false")
    void testEqualsWithDifferentClass() {
        staffMember.setStaffMemberId(1L);
        assertFalse(staffMember.equals("Not a StaffMember"));
    }

    @Test
    @DisplayName("Test equals method with same staffMemberId returns true")
    void testEqualsWithSameStaffMemberId() {
        staffMember.setStaffMemberId(1L);
        StaffMember other = new StaffMember();
        other.setStaffMemberId(1L);
        assertTrue(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test equals method with different staffMemberId returns false")
    void testEqualsWithDifferentStaffMemberId() {
        staffMember.setStaffMemberId(1L);
        StaffMember other = new StaffMember();
        other.setStaffMemberId(2L);
        assertFalse(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test equals method with both null staffMemberId returns true")
    void testEqualsWithBothNullStaffMemberId() {
        StaffMember other = new StaffMember();
        assertTrue(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test equals method with one null staffMemberId returns false")
    void testEqualsWithOneNullStaffMemberId() {
        staffMember.setStaffMemberId(1L);
        StaffMember other = new StaffMember();
        assertFalse(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test hashCode consistency with same staffMemberId")
    void testHashCodeConsistency() {
        staffMember.setStaffMemberId(1L);
        int hashCode1 = staffMember.hashCode();
        int hashCode2 = staffMember.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("Test hashCode equality for equal objects")
    void testHashCodeEqualityForEqualObjects() {
        staffMember.setStaffMemberId(1L);
        StaffMember other = new StaffMember();
        other.setStaffMemberId(1L);
        assertEquals(staffMember.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("Test hashCode difference for different staffMemberId")
    void testHashCodeDifferenceForDifferentStaffMemberId() {
        staffMember.setStaffMemberId(1L);
        StaffMember other = new StaffMember();
        other.setStaffMemberId(2L);
        assertNotEquals(staffMember.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("Test hashCode with null staffMemberId")
    void testHashCodeWithNullStaffMemberId() {
        int hashCode = staffMember.hashCode();
        assertNotNull(hashCode);
    }

    @Test
    @DisplayName("Test toString contains all field values")
    void testToStringContainsAllFields() {
        staffMember.setStaffMemberId(1L);
        staffMember.setName("Alice Johnson");
        staffMember.setContact("alice@hospital.com");
        staffMember.setRole("Surgeon");
        staffMember.setEmploymentStatus("ACTIVE");
        staffMember.setFacilityId(200L);
        staffMember.setStartDate(testStartDate);
        staffMember.setEndDate(testEndDate);
        staffMember.setCreatedAt(testCreatedAt);
        staffMember.setUpdatedAt(testUpdatedAt);

        String result = staffMember.toString();

        assertTrue(result.contains("staffMemberId=1"));
        assertTrue(result.contains("name='Alice Johnson'"));
        assertTrue(result.contains("contact='alice@hospital.com'"));
        assertTrue(result.contains("role='Surgeon'"));
        assertTrue(result.contains("employmentStatus='ACTIVE'"));
        assertTrue(result.contains("facilityId=200"));
        assertTrue(result.contains("startDate=" + testStartDate));
        assertTrue(result.contains("endDate=" + testEndDate));
        assertTrue(result.contains("createdAt=" + testCreatedAt));
        assertTrue(result.contains("updatedAt=" + testUpdatedAt));
    }

    @Test
    @DisplayName("Test toString with null values")
    void testToStringWithNullValues() {
        String result = staffMember.toString();
        assertTrue(result.contains("StaffMember{"));
        assertTrue(result.contains("staffMemberId=null"));
        assertTrue(result.contains("name='null'"));
        assertTrue(result.contains("contact='null'"));
        assertTrue(result.contains("role='null'"));
        assertTrue(result.contains("employmentStatus='null'"));
        assertTrue(result.contains("facilityId=null"));
        assertTrue(result.contains("startDate=null"));
        assertTrue(result.contains("endDate=null"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("updatedAt=null"));
    }

    @Test
    @DisplayName("Test soft-delete pattern: ACTIVE to INACTIVE transition")
    void testSoftDeletePatternActiveToInactive() {
        staffMember.setEmploymentStatus("ACTIVE");
        staffMember.setEndDate(null);

        staffMember.setEmploymentStatus("INACTIVE");
        staffMember.setEndDate(LocalDate.now());

        assertEquals("INACTIVE", staffMember.getEmploymentStatus());
        assertNotNull(staffMember.getEndDate());
    }

    @Test
    @DisplayName("Test soft-delete pattern: ACTIVE to TERMINATED transition")
    void testSoftDeletePatternActiveToTerminated() {
        staffMember.setEmploymentStatus("ACTIVE");
        staffMember.setEndDate(null);

        staffMember.setEmploymentStatus("TERMINATED");
        staffMember.setEndDate(LocalDate.now());

        assertEquals("TERMINATED", staffMember.getEmploymentStatus());
        assertNotNull(staffMember.getEndDate());
    }

    @Test
    @DisplayName("Test facilityId as foreign key reference")
    void testFacilityIdAsForeignKeyReference() {
        Long facilityId = 999L;
        staffMember.setFacilityId(facilityId);
        assertEquals(facilityId, staffMember.getFacilityId());
    }

    @Test
    @DisplayName("Test all fields can be set and retrieved independently")
    void testAllFieldsIndependentSetAndGet() {
        staffMember.setStaffMemberId(10L);
        staffMember.setName("Bob Williams");
        staffMember.setContact("bob@clinic.com");
        staffMember.setRole("Technician");
        staffMember.setEmploymentStatus("ACTIVE");
        staffMember.setFacilityId(300L);
        staffMember.setStartDate(testStartDate);
        staffMember.setEndDate(testEndDate);
        staffMember.setCreatedAt(testCreatedAt);
        staffMember.setUpdatedAt(testUpdatedAt);

        assertEquals(10L, staffMember.getStaffMemberId());
        assertEquals("Bob Williams", staffMember.getName());
        assertEquals("bob@clinic.com", staffMember.getContact());
        assertEquals("Technician", staffMember.getRole());
        assertEquals("ACTIVE", staffMember.getEmploymentStatus());
        assertEquals(300L, staffMember.getFacilityId());
        assertEquals(testStartDate, staffMember.getStartDate());
        assertEquals(testEndDate, staffMember.getEndDate());
        assertEquals(testCreatedAt, staffMember.getCreatedAt());
        assertEquals(testUpdatedAt, staffMember.getUpdatedAt());
    }

    @Test
    @DisplayName("Test entity with maximum length values for string fields")
    void testMaximumLengthStringFields() {
        String maxName = "A".repeat(255);
        String maxContact = "B".repeat(255);
        String maxRole = "C".repeat(100);
        String maxStatus = "D".repeat(20);

        staffMember.setName(maxName);
        staffMember.setContact(maxContact);
        staffMember.setRole(maxRole);
        staffMember.setEmploymentStatus(maxStatus);

        assertEquals(maxName, staffMember.getName());
        assertEquals(maxContact, staffMember.getContact());
        assertEquals(maxRole, staffMember.getRole());
        assertEquals(maxStatus, staffMember.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test entity with empty string values")
    void testEmptyStringValues() {
        staffMember.setName("");
        staffMember.setContact("");
        staffMember.setRole("");
        staffMember.setEmploymentStatus("");

        assertEquals("", staffMember.getName());
        assertEquals("", staffMember.getContact());
        assertEquals("", staffMember.getRole());
        assertEquals("", staffMember.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test startDate and endDate with same date")
    void testStartDateAndEndDateSameDate() {
        LocalDate sameDate = LocalDate.of(2024, 6, 15);
        staffMember.setStartDate(sameDate);
        staffMember.setEndDate(sameDate);

        assertEquals(sameDate, staffMember.getStartDate());
        assertEquals(sameDate, staffMember.getEndDate());
    }

    @Test
    @DisplayName("Test createdAt and updatedAt with same timestamp")
    void testCreatedAtAndUpdatedAtSameTimestamp() {
        LocalDateTime sameTimestamp = LocalDateTime.now();
        staffMember.setCreatedAt(sameTimestamp);
        staffMember.setUpdatedAt(sameTimestamp);

        assertEquals(sameTimestamp, staffMember.getCreatedAt());
        assertEquals(sameTimestamp, staffMember.getUpdatedAt());
    }
}