package com.visionary.roster.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.Staff;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StaffResponse DTO.
 * Ensures 100% coverage of the active field functionality and fromEntity mapping.
 */
class StaffResponseTest {

    @Test
    @DisplayName("Test isActive() returns true when staff member is active")
    void testIsActive_WhenActive_ReturnsTrue() {
        StaffResponse response = new StaffResponse();
        response.setActive(true);
        
        assertTrue(response.isActive(), "isActive() should return true for active staff member");
    }

    @Test
    @DisplayName("Test isActive() returns false when staff member is deactivated")
    void testIsActive_WhenDeactivated_ReturnsFalse() {
        StaffResponse response = new StaffResponse();
        response.setActive(false);
        
        assertFalse(response.isActive(), "isActive() should return false for deactivated staff member");
    }

    @Test
    @DisplayName("Test isActive() returns false by default")
    void testIsActive_DefaultValue_ReturnsFalse() {
        StaffResponse response = new StaffResponse();
        
        assertFalse(response.isActive(), "isActive() should return false by default");
    }

    @Test
    @DisplayName("Test setActive() correctly sets active status to true")
    void testSetActive_WithTrue_SetsActiveStatusTrue() {
        StaffResponse response = new StaffResponse();
        response.setActive(true);
        
        assertTrue(response.isActive(), "setActive(true) should set active status to true");
    }

    @Test
    @DisplayName("Test setActive() correctly sets active status to false")
    void testSetActive_WithFalse_SetsActiveStatusFalse() {
        StaffResponse response = new StaffResponse();
        response.setActive(false);
        
        assertFalse(response.isActive(), "setActive(false) should set active status to false");
    }

    @Test
    @DisplayName("Test fromEntity() correctly maps active field when staff is active")
    void testFromEntity_WithActiveStaff_MapsActiveFieldCorrectly() {
        Staff staff = createMockStaff();
        staff.setActive(true);
        
        StaffResponse response = StaffResponse.fromEntity(staff);
        
        assertNotNull(response, "fromEntity() should not return null");
        assertTrue(response.isActive(), "fromEntity() should map active=true correctly");
    }

    @Test
    @DisplayName("Test fromEntity() correctly maps active field when staff is deactivated")
    void testFromEntity_WithDeactivatedStaff_MapsActiveFieldCorrectly() {
        Staff staff = createMockStaff();
        staff.setActive(false);
        
        StaffResponse response = StaffResponse.fromEntity(staff);
        
        assertNotNull(response, "fromEntity() should not return null");
        assertFalse(response.isActive(), "fromEntity() should map active=false correctly");
    }

    @Test
    @DisplayName("Test fromEntity() returns null when staff entity is null")
    void testFromEntity_WithNullStaff_ReturnsNull() {
        StaffResponse response = StaffResponse.fromEntity(null);
        
        assertNull(response, "fromEntity() should return null when staff entity is null");
    }

    @Test
    @DisplayName("Test fromEntity() maps all fields including active field")
    void testFromEntity_MapsAllFieldsIncludingActive() {
        Staff staff = createMockStaff();
        staff.setId(1L);
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setEmail("john.doe@example.com");
        staff.setRole("Nurse");
        staff.setEmploymentStatus("Full-Time");
        staff.setActive(true);
        LocalDateTime now = LocalDateTime.now();
        staff.setCreatedAt(now);
        staff.setUpdatedAt(now);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        staff.setEndDate(endDate);
        
        Facility facility = new Facility();
        facility.setFacilityId(100L);
        facility.setName("Test Facility");
        staff.setFacility(facility);
        
        StaffResponse response = StaffResponse.fromEntity(staff);
        
        assertNotNull(response, "fromEntity() should not return null");
        assertEquals(1L, response.getId(), "ID should be mapped correctly");
        assertEquals("John", response.getFirstName(), "First name should be mapped correctly");
        assertEquals("Doe", response.getLastName(), "Last name should be mapped correctly");
        assertEquals("john.doe@example.com", response.getEmail(), "Email should be mapped correctly");
        assertEquals("Nurse", response.getRole(), "Role should be mapped correctly");
        assertEquals("Full-Time", response.getEmploymentStatus(), "Employment status should be mapped correctly");
        assertTrue(response.isActive(), "Active field should be mapped correctly");
        assertEquals(now, response.getCreatedAt(), "CreatedAt should be mapped correctly");
        assertEquals(now, response.getUpdatedAt(), "UpdatedAt should be mapped correctly");
        assertEquals(endDate, response.getEndDate(), "EndDate should be mapped correctly");
        assertEquals(100L, response.getFacilityId(), "Facility ID should be mapped correctly");
        assertEquals("Test Facility", response.getFacilityName(), "Facility name should be mapped correctly");
    }

    @Test
    @DisplayName("Test fromEntity() handles null facility gracefully")
    void testFromEntity_WithNullFacility_HandlesGracefully() {
        Staff staff = createMockStaff();
        staff.setActive(true);
        staff.setFacility(null);
        
        StaffResponse response = StaffResponse.fromEntity(staff);
        
        assertNotNull(response, "fromEntity() should not return null");
        assertTrue(response.isActive(), "Active field should be mapped correctly");
        assertNull(response.getFacilityId(), "Facility ID should be null when facility is null");
        assertNull(response.getFacilityName(), "Facility name should be null when facility is null");
    }

    @Test
    @DisplayName("Test JSON serialization includes active field")
    void testJsonSerialization_IncludesActiveField() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        StaffResponse response = new StaffResponse();
        response.setId(1L);
        response.setFirstName("Jane");
        response.setLastName("Smith");
        response.setEmail("jane.smith@example.com");
        response.setRole("Doctor");
        response.setEmploymentStatus("Part-Time");
        response.setActive(true);
        response.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        response.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));
        response.setEndDate(LocalDate.of(2025, 12, 31));
        response.setFacilityId(200L);
        response.setFacilityName("Main Hospital");
        
        String json = objectMapper.writeValueAsString(response);
        
        assertNotNull(json, "JSON serialization should not return null");
        assertTrue(json.contains("\"active\":true"), "JSON should contain active field with value true");
        assertTrue(json.contains("\"id\":1"), "JSON should contain id field");
        assertTrue(json.contains("\"firstName\":\"Jane\""), "JSON should contain firstName field");
        assertTrue(json.contains("\"email\":\"jane.smith@example.com\""), "JSON should contain email field");
    }

    @Test
    @DisplayName("Test JSON serialization with active=false")
    void testJsonSerialization_WithActiveFieldFalse() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        StaffResponse response = new StaffResponse();
        response.setId(2L);
        response.setFirstName("Bob");
        response.setLastName("Johnson");
        response.setEmail("bob.johnson@example.com");
        response.setActive(false);
        
        String json = objectMapper.writeValueAsString(response);
        
        assertNotNull(json, "JSON serialization should not return null");
        assertTrue(json.contains("\"active\":false"), "JSON should contain active field with value false");
    }

    @Test
    @DisplayName("Test JSON deserialization includes active field")
    void testJsonDeserialization_IncludesActiveField() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        String json = "{\"id\":3,\"firstName\":\"Alice\",\"lastName\":\"Williams\",\"email\":\"alice.williams@example.com\",\"role\":\"Technician\",\"employmentStatus\":\"Contract\",\"active\":true,\"facilityId\":300,\"facilityName\":\"City Clinic\",\"createdAt\":\"2024-01-01T10:00:00\",\"updatedAt\":\"2024-01-02T10:00:00\",\"endDate\":\"2025-12-31\"}";
        
        StaffResponse response = objectMapper.readValue(json, StaffResponse.class);
        
        assertNotNull(response, "JSON deserialization should not return null");
        assertTrue(response.isActive(), "Deserialized active field should be true");
        assertEquals(3L, response.getId(), "Deserialized ID should match");
        assertEquals("Alice", response.getFirstName(), "Deserialized firstName should match");
        assertEquals("alice.williams@example.com", response.getEmail(), "Deserialized email should match");
    }

    @Test
    @DisplayName("Test all getters and setters for complete coverage")
    void testAllGettersAndSetters() {
        StaffResponse response = new StaffResponse();
        
        response.setId(10L);
        assertEquals(10L, response.getId());
        
        response.setFirstName("Test");
        assertEquals("Test", response.getFirstName());
        
        response.setLastName("User");
        assertEquals("User", response.getLastName());
        
        response.setEmail("test@example.com");
        assertEquals("test@example.com", response.getEmail());
        
        response.setRole("Admin");
        assertEquals("Admin", response.getRole());
        
        response.setEmploymentStatus("Permanent");
        assertEquals("Permanent", response.getEmploymentStatus());
        
        response.setFacilityId(500L);
        assertEquals(500L, response.getFacilityId());
        
        response.setFacilityName("Test Facility");
        assertEquals("Test Facility", response.getFacilityName());
        
        LocalDateTime created = LocalDateTime.now();
        response.setCreatedAt(created);
        assertEquals(created, response.getCreatedAt());
        
        LocalDateTime updated = LocalDateTime.now();
        response.setUpdatedAt(updated);
        assertEquals(updated, response.getUpdatedAt());
        
        LocalDate end = LocalDate.now();
        response.setEndDate(end);
        assertEquals(end, response.getEndDate());
        
        response.setActive(true);
        assertTrue(response.isActive());
    }

    /**
     * Helper method to create a mock Staff entity for testing.
     */
    private Staff createMockStaff() {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setFirstName("Mock");
        staff.setLastName("Staff");
        staff.setEmail("mock@example.com");
        staff.setRole("Test Role");
        staff.setEmploymentStatus("Test Status");
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());
        return staff;
    }
}