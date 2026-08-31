package com.visionary.roster.dto;

import com.visionary.roster.entity.Facility;
import com.visionary.roster.entity.Staff;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StaffResponse DTO.
 * Ensures 100% test coverage for all fields, getters, setters, and factory methods.
 */
class StaffResponseTest {

    @Test
    @DisplayName("Test default constructor creates empty StaffResponse")
    void testDefaultConstructor() {
        StaffResponse response = new StaffResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
        assertNull(response.getEmail());
        assertNull(response.getRole());
        assertNull(response.getEmploymentStatus());
        assertNull(response.getFacilityId());
        assertNull(response.getFacilityName());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
        assertNull(response.getEndDate());
    }

    @Test
    @DisplayName("Test getId and setId")
    void testIdGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        Long expectedId = 123L;
        response.setId(expectedId);
        assertEquals(expectedId, response.getId());
    }

    @Test
    @DisplayName("Test getFirstName and setFirstName")
    void testFirstNameGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        String expectedFirstName = "John";
        response.setFirstName(expectedFirstName);
        assertEquals(expectedFirstName, response.getFirstName());
    }

    @Test
    @DisplayName("Test getLastName and setLastName")
    void testLastNameGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        String expectedLastName = "Doe";
        response.setLastName(expectedLastName);
        assertEquals(expectedLastName, response.getLastName());
    }

    @Test
    @DisplayName("Test getEmail and setEmail")
    void testEmailGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        String expectedEmail = "john.doe@example.com";
        response.setEmail(expectedEmail);
        assertEquals(expectedEmail, response.getEmail());
    }

    @Test
    @DisplayName("Test getRole and setRole")
    void testRoleGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        String expectedRole = "NURSE";
        response.setRole(expectedRole);
        assertEquals(expectedRole, response.getRole());
    }

    @Test
    @DisplayName("Test getEmploymentStatus and setEmploymentStatus")
    void testEmploymentStatusGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        String expectedStatus = "ACTIVE";
        response.setEmploymentStatus(expectedStatus);
        assertEquals(expectedStatus, response.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test getFacilityId and setFacilityId")
    void testFacilityIdGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        Long expectedFacilityId = 456L;
        response.setFacilityId(expectedFacilityId);
        assertEquals(expectedFacilityId, response.getFacilityId());
    }

    @Test
    @DisplayName("Test getFacilityName and setFacilityName")
    void testFacilityNameGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        String expectedFacilityName = "Central Hospital";
        response.setFacilityName(expectedFacilityName);
        assertEquals(expectedFacilityName, response.getFacilityName());
    }

    @Test
    @DisplayName("Test getCreatedAt and setCreatedAt")
    void testCreatedAtGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        LocalDateTime expectedCreatedAt = LocalDateTime.of(2023, 1, 15, 10, 30);
        response.setCreatedAt(expectedCreatedAt);
        assertEquals(expectedCreatedAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("Test getUpdatedAt and setUpdatedAt")
    void testUpdatedAtGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2023, 6, 20, 14, 45);
        response.setUpdatedAt(expectedUpdatedAt);
        assertEquals(expectedUpdatedAt, response.getUpdatedAt());
    }

    @Test
    @DisplayName("Test getEndDate and setEndDate")
    void testEndDateGetterAndSetter() {
        StaffResponse response = new StaffResponse();
        LocalDate expectedEndDate = LocalDate.of(2023, 12, 31);
        response.setEndDate(expectedEndDate);
        assertEquals(expectedEndDate, response.getEndDate());
    }

    @Test
    @DisplayName("Test fromEntity returns null when staff is null")
    void testFromEntityWithNullStaff() {
        StaffResponse response = StaffResponse.fromEntity(null);
        assertNull(response);
    }

    @Test
    @DisplayName("Test fromEntity maps all staff fields correctly with facility")
    void testFromEntityWithCompleteStaffAndFacility() {
        // Create mock Facility
        Facility facility = new Facility();
        facility.setId(789L);
        facility.setName("General Hospital");

        // Create mock Staff
        Staff staff = new Staff();
        staff.setId(100L);
        staff.setFirstName("Jane");
        staff.setLastName("Smith");
        staff.setEmail("jane.smith@example.com");
        staff.setRole("DOCTOR");
        staff.setEmploymentStatus("ACTIVE");
        staff.setCreatedAt(LocalDateTime.of(2022, 3, 10, 8, 0));
        staff.setUpdatedAt(LocalDateTime.of(2023, 7, 15, 16, 30));
        staff.setEndDate(LocalDate.of(2024, 12, 31));
        staff.setFacility(facility);

        // Execute fromEntity
        StaffResponse response = StaffResponse.fromEntity(staff);

        // Verify all fields
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("jane.smith@example.com", response.getEmail());
        assertEquals("DOCTOR", response.getRole());
        assertEquals("ACTIVE", response.getEmploymentStatus());
        assertEquals(LocalDateTime.of(2022, 3, 10, 8, 0), response.getCreatedAt());
        assertEquals(LocalDateTime.of(2023, 7, 15, 16, 30), response.getUpdatedAt());
        assertEquals(LocalDate.of(2024, 12, 31), response.getEndDate());
        assertEquals(789L, response.getFacilityId());
        assertEquals("General Hospital", response.getFacilityName());
    }

    @Test
    @DisplayName("Test fromEntity with staff having null facility")
    void testFromEntityWithNullFacility() {
        // Create mock Staff without facility
        Staff staff = new Staff();
        staff.setId(200L);
        staff.setFirstName("Bob");
        staff.setLastName("Johnson");
        staff.setEmail("bob.johnson@example.com");
        staff.setRole("ADMIN");
        staff.setEmploymentStatus("INACTIVE");
        staff.setCreatedAt(LocalDateTime.of(2021, 5, 20, 9, 15));
        staff.setUpdatedAt(LocalDateTime.of(2023, 8, 25, 11, 45));
        staff.setEndDate(LocalDate.of(2023, 9, 1));
        staff.setFacility(null);

        // Execute fromEntity
        StaffResponse response = StaffResponse.fromEntity(staff);

        // Verify all fields
        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("Bob", response.getFirstName());
        assertEquals("Johnson", response.getLastName());
        assertEquals("bob.johnson@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals("INACTIVE", response.getEmploymentStatus());
        assertEquals(LocalDateTime.of(2021, 5, 20, 9, 15), response.getCreatedAt());
        assertEquals(LocalDateTime.of(2023, 8, 25, 11, 45), response.getUpdatedAt());
        assertEquals(LocalDate.of(2023, 9, 1), response.getEndDate());
        assertNull(response.getFacilityId());
        assertNull(response.getFacilityName());
    }

    @Test
    @DisplayName("Test fromEntity with staff having null endDate")
    void testFromEntityWithNullEndDate() {
        // Create mock Facility
        Facility facility = new Facility();
        facility.setId(999L);
        facility.setName("City Clinic");

        // Create mock Staff with null endDate
        Staff staff = new Staff();
        staff.setId(300L);
        staff.setFirstName("Alice");
        staff.setLastName("Williams");
        staff.setEmail("alice.williams@example.com");
        staff.setRole("NURSE");
        staff.setEmploymentStatus("ACTIVE");
        staff.setCreatedAt(LocalDateTime.of(2023, 1, 1, 7, 0));
        staff.setUpdatedAt(LocalDateTime.of(2023, 9, 1, 12, 0));
        staff.setEndDate(null);
        staff.setFacility(facility);

        // Execute fromEntity
        StaffResponse response = StaffResponse.fromEntity(staff);

        // Verify all fields
        assertNotNull(response);
        assertEquals(300L, response.getId());
        assertEquals("Alice", response.getFirstName());
        assertEquals("Williams", response.getLastName());
        assertEquals("alice.williams@example.com", response.getEmail());
        assertEquals("NURSE", response.getRole());
        assertEquals("ACTIVE", response.getEmploymentStatus());
        assertEquals(LocalDateTime.of(2023, 1, 1, 7, 0), response.getCreatedAt());
        assertEquals(LocalDateTime.of(2023, 9, 1, 12, 0), response.getUpdatedAt());
        assertNull(response.getEndDate());
        assertEquals(999L, response.getFacilityId());
        assertEquals("City Clinic", response.getFacilityName());
    }

    @Test
    @DisplayName("Test fromEntity with minimal staff data")
    void testFromEntityWithMinimalStaffData() {
        // Create mock Staff with only required fields
        Staff staff = new Staff();
        staff.setId(400L);
        staff.setFirstName("Charlie");
        staff.setLastName("Brown");
        staff.setEmail("charlie.brown@example.com");
        staff.setRole("TECHNICIAN");
        staff.setEmploymentStatus("ACTIVE");
        staff.setCreatedAt(null);
        staff.setUpdatedAt(null);
        staff.setEndDate(null);
        staff.setFacility(null);

        // Execute fromEntity
        StaffResponse response = StaffResponse.fromEntity(staff);

        // Verify all fields
        assertNotNull(response);
        assertEquals(400L, response.getId());
        assertEquals("Charlie", response.getFirstName());
        assertEquals("Brown", response.getLastName());
        assertEquals("charlie.brown@example.com", response.getEmail());
        assertEquals("TECHNICIAN", response.getRole());
        assertEquals("ACTIVE", response.getEmploymentStatus());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
        assertNull(response.getEndDate());
        assertNull(response.getFacilityId());
        assertNull(response.getFacilityName());
    }
}