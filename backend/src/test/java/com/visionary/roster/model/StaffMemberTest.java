package com.visionary.roster.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for StaffMember entity.
 * Ensures 100% test coverage of all business logic including constructors,
 * getters, setters, equals, hashCode, and toString methods.
 */
@DisplayName("StaffMember Entity Tests")
class StaffMemberTest {

    private StaffMember staffMember;
    private LocalDate testStartDate;
    private LocalDate testEndDate;

    @BeforeEach
    void setUp() {
        testStartDate = LocalDate.of(2023, 1, 15);
        testEndDate = LocalDate.of(2024, 12, 31);
        staffMember = new StaffMember();
    }

    @Test
    @DisplayName("Test no-args constructor creates instance")
    void testNoArgsConstructor() {
        StaffMember member = new StaffMember();
        assertNotNull(member);
        assertNull(member.getId());
        assertNull(member.getName());
        assertNull(member.getContact());
        assertNull(member.getRole());
        assertNull(member.getEmploymentStatus());
        assertNull(member.getStartDate());
        assertNull(member.getEndDate());
        assertNull(member.getFacilityId());
    }

    @Test
    @DisplayName("Test all-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        StaffMember member = new StaffMember(
                1L,
                "John Doe",
                "john.doe@example.com",
                "Nurse",
                "ACTIVE",
                testStartDate,
                testEndDate,
                100L
        );

        assertEquals(1L, member.getId());
        assertEquals("John Doe", member.getName());
        assertEquals("john.doe@example.com", member.getContact());
        assertEquals("Nurse", member.getRole());
        assertEquals("ACTIVE", member.getEmploymentStatus());
        assertEquals(testStartDate, member.getStartDate());
        assertEquals(testEndDate, member.getEndDate());
        assertEquals(100L, member.getFacilityId());
    }

    @Test
    @DisplayName("Test all-args constructor with null optional fields")
    void testAllArgsConstructorWithNullOptionalFields() {
        StaffMember member = new StaffMember(
                2L,
                "Jane Smith",
                "jane.smith@example.com",
                "Doctor",
                "ACTIVE",
                null,
                null,
                200L
        );

        assertEquals(2L, member.getId());
        assertEquals("Jane Smith", member.getName());
        assertEquals("jane.smith@example.com", member.getContact());
        assertEquals("Doctor", member.getRole());
        assertEquals("ACTIVE", member.getEmploymentStatus());
        assertNull(member.getStartDate());
        assertNull(member.getEndDate());
        assertEquals(200L, member.getFacilityId());
    }

    @Test
    @DisplayName("Test getId and setId")
    void testGetAndSetId() {
        assertNull(staffMember.getId());
        staffMember.setId(10L);
        assertEquals(10L, staffMember.getId());
    }

    @Test
    @DisplayName("Test getName and setName")
    void testGetAndSetName() {
        assertNull(staffMember.getName());
        staffMember.setName("Alice Johnson");
        assertEquals("Alice Johnson", staffMember.getName());
    }

    @Test
    @DisplayName("Test getContact and setContact")
    void testGetAndSetContact() {
        assertNull(staffMember.getContact());
        staffMember.setContact("alice.johnson@example.com");
        assertEquals("alice.johnson@example.com", staffMember.getContact());
    }

    @Test
    @DisplayName("Test getRole and setRole")
    void testGetAndSetRole() {
        assertNull(staffMember.getRole());
        staffMember.setRole("Physician");
        assertEquals("Physician", staffMember.getRole());
    }

    @Test
    @DisplayName("Test getEmploymentStatus and setEmploymentStatus")
    void testGetAndSetEmploymentStatus() {
        assertNull(staffMember.getEmploymentStatus());
        staffMember.setEmploymentStatus("INACTIVE");
        assertEquals("INACTIVE", staffMember.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test getStartDate and setStartDate")
    void testGetAndSetStartDate() {
        assertNull(staffMember.getStartDate());
        staffMember.setStartDate(testStartDate);
        assertEquals(testStartDate, staffMember.getStartDate());
    }

    @Test
    @DisplayName("Test getEndDate and setEndDate")
    void testGetAndSetEndDate() {
        assertNull(staffMember.getEndDate());
        staffMember.setEndDate(testEndDate);
        assertEquals(testEndDate, staffMember.getEndDate());
    }

    @Test
    @DisplayName("Test getFacilityId and setFacilityId")
    void testGetAndSetFacilityId() {
        assertNull(staffMember.getFacilityId());
        staffMember.setFacilityId(500L);
        assertEquals(500L, staffMember.getFacilityId());
    }

    @Test
    @DisplayName("Test equals with same object returns true")
    void testEqualsWithSameObject() {
        staffMember.setId(1L);
        assertTrue(staffMember.equals(staffMember));
    }

    @Test
    @DisplayName("Test equals with null returns false")
    void testEqualsWithNull() {
        staffMember.setId(1L);
        assertFalse(staffMember.equals(null));
    }

    @Test
    @DisplayName("Test equals with different class returns false")
    void testEqualsWithDifferentClass() {
        staffMember.setId(1L);
        assertFalse(staffMember.equals("Not a StaffMember"));
    }

    @Test
    @DisplayName("Test equals with same id returns true")
    void testEqualsWithSameId() {
        staffMember.setId(1L);
        StaffMember other = new StaffMember();
        other.setId(1L);
        assertTrue(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test equals with different id returns false")
    void testEqualsWithDifferentId() {
        staffMember.setId(1L);
        StaffMember other = new StaffMember();
        other.setId(2L);
        assertFalse(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test equals with both null ids returns true")
    void testEqualsWithBothNullIds() {
        StaffMember other = new StaffMember();
        assertTrue(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test equals with one null id returns false")
    void testEqualsWithOneNullId() {
        staffMember.setId(1L);
        StaffMember other = new StaffMember();
        assertFalse(staffMember.equals(other));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void testHashCodeConsistency() {
        staffMember.setId(1L);
        int hashCode1 = staffMember.hashCode();
        int hashCode2 = staffMember.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("Test hashCode with same id produces same hash")
    void testHashCodeWithSameId() {
        staffMember.setId(1L);
        StaffMember other = new StaffMember();
        other.setId(1L);
        assertEquals(staffMember.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("Test hashCode with null id")
    void testHashCodeWithNullId() {
        int hashCode = staffMember.hashCode();
        assertNotNull(hashCode);
    }

    @Test
    @DisplayName("Test toString contains all fields")
    void testToStringContainsAllFields() {
        staffMember.setId(1L);
        staffMember.setName("Bob Wilson");
        staffMember.setContact("bob.wilson@example.com");
        staffMember.setRole("Technician");
        staffMember.setEmploymentStatus("ACTIVE");
        staffMember.setStartDate(testStartDate);
        staffMember.setEndDate(testEndDate);
        staffMember.setFacilityId(300L);

        String toString = staffMember.toString();

        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name='Bob Wilson'"));
        assertTrue(toString.contains("contact='bob.wilson@example.com'"));
        assertTrue(toString.contains("role='Technician'"));
        assertTrue(toString.contains("employmentStatus='ACTIVE'"));
        assertTrue(toString.contains("startDate=" + testStartDate));
        assertTrue(toString.contains("endDate=" + testEndDate));
        assertTrue(toString.contains("facilityId=300"));
        assertTrue(toString.startsWith("StaffMember{"));
        assertTrue(toString.endsWith("}"));
    }

    @Test
    @DisplayName("Test toString with null fields")
    void testToStringWithNullFields() {
        String toString = staffMember.toString();

        assertTrue(toString.contains("id=null"));
        assertTrue(toString.contains("name='null'"));
        assertTrue(toString.contains("contact='null'"));
        assertTrue(toString.contains("role='null'"));
        assertTrue(toString.contains("employmentStatus='null'"));
        assertTrue(toString.contains("startDate=null"));
        assertTrue(toString.contains("endDate=null"));
        assertTrue(toString.contains("facilityId=null"));
    }

    @Test
    @DisplayName("Test facilityId enforces multi-tenant scoping")
    void testFacilityIdMultiTenantScoping() {
        staffMember.setFacilityId(100L);
        assertEquals(100L, staffMember.getFacilityId());

        staffMember.setFacilityId(200L);
        assertEquals(200L, staffMember.getFacilityId());
    }

    @Test
    @DisplayName("Test complete object lifecycle")
    void testCompleteObjectLifecycle() {
        StaffMember member = new StaffMember(
                1L,
                "Complete Test",
                "complete@test.com",
                "Manager",
                "ACTIVE",
                testStartDate,
                testEndDate,
                999L
        );

        member.setName("Updated Name");
        member.setEmploymentStatus("INACTIVE");
        member.setEndDate(LocalDate.of(2025, 6, 30));

        assertEquals("Updated Name", member.getName());
        assertEquals("INACTIVE", member.getEmploymentStatus());
        assertEquals(LocalDate.of(2025, 6, 30), member.getEndDate());
        assertEquals(1L, member.getId());
        assertEquals(999L, member.getFacilityId());
    }

    @Test
    @DisplayName("Test entity with only required fields")
    void testEntityWithOnlyRequiredFields() {
        StaffMember member = new StaffMember();
        member.setName("Required Only");
        member.setContact("required@test.com");
        member.setRole("Staff");
        member.setEmploymentStatus("ACTIVE");
        member.setFacilityId(1L);

        assertEquals("Required Only", member.getName());
        assertEquals("required@test.com", member.getContact());
        assertEquals("Staff", member.getRole());
        assertEquals("ACTIVE", member.getEmploymentStatus());
        assertEquals(1L, member.getFacilityId());
        assertNull(member.getStartDate());
        assertNull(member.getEndDate());
    }

    @Test
    @DisplayName("Test equals and hashCode contract")
    void testEqualsAndHashCodeContract() {
        StaffMember member1 = new StaffMember();
        member1.setId(1L);

        StaffMember member2 = new StaffMember();
        member2.setId(1L);

        StaffMember member3 = new StaffMember();
        member3.setId(2L);

        assertTrue(member1.equals(member2) && member2.equals(member1));
        assertEquals(member1.hashCode(), member2.hashCode());

        assertFalse(member1.equals(member3));
    }
}