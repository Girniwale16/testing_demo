package com.visionary.roster.dto;

import com.visionary.roster.entity.StaffMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StaffResponse DTO Tests")
class StaffResponseTest {

    @Test
    @DisplayName("Test no-args constructor creates empty StaffResponse")
    void testNoArgsConstructor() {
        StaffResponse response = new StaffResponse();
        
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getContact());
        assertNull(response.getRole());
        assertNull(response.getEmploymentStatus());
        assertNull(response.getStartDate());
        assertNull(response.getEndDate());
        assertNull(response.getFacilityId());
    }

    @Test
    @DisplayName("Test all-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        Long id = 1L;
        String name = "John Doe";
        String contact = "john.doe@example.com";
        String role = "Nurse";
        String employmentStatus = "ACTIVE";
        LocalDate startDate = LocalDate.of(2023, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long facilityId = 100L;
        
        StaffResponse response = new StaffResponse(id, name, contact, role, employmentStatus, 
                                                   startDate, endDate, facilityId);
        
        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
        assertEquals(contact, response.getContact());
        assertEquals(role, response.getRole());
        assertEquals(employmentStatus, response.getEmploymentStatus());
        assertEquals(startDate, response.getStartDate());
        assertEquals(endDate, response.getEndDate());
        assertEquals(facilityId, response.getFacilityId());
    }

    @Test
    @DisplayName("Test all-args constructor with null dates")
    void testAllArgsConstructorWithNullDates() {
        Long id = 2L;
        String name = "Jane Smith";
        String contact = "jane.smith@example.com";
        String role = "Doctor";
        String employmentStatus = "ACTIVE";
        Long facilityId = 200L;
        
        StaffResponse response = new StaffResponse(id, name, contact, role, employmentStatus, 
                                                   null, null, facilityId);
        
        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
        assertEquals(contact, response.getContact());
        assertEquals(role, response.getRole());
        assertEquals(employmentStatus, response.getEmploymentStatus());
        assertNull(response.getStartDate());
        assertNull(response.getEndDate());
        assertEquals(facilityId, response.getFacilityId());
    }

    @Test
    @DisplayName("Test fromEntity with valid StaffMember entity")
    void testFromEntityWithValidEntity() {
        StaffMember entity = new StaffMember();
        entity.setId(1L);
        entity.setName("John Doe");
        entity.setContact("john.doe@example.com");
        entity.setRole("Nurse");
        entity.setEmploymentStatus("ACTIVE");
        entity.setStartDate(LocalDate.of(2023, 1, 15));
        entity.setEndDate(LocalDate.of(2024, 12, 31));
        entity.setFacilityId(100L);
        
        StaffResponse response = StaffResponse.fromEntity(entity);
        
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getName(), response.getName());
        assertEquals(entity.getContact(), response.getContact());
        assertEquals(entity.getRole(), response.getRole());
        assertEquals(entity.getEmploymentStatus(), response.getEmploymentStatus());
        assertEquals(entity.getStartDate(), response.getStartDate());
        assertEquals(entity.getEndDate(), response.getEndDate());
        assertEquals(entity.getFacilityId(), response.getFacilityId());
    }

    @Test
    @DisplayName("Test fromEntity with null entity returns null")
    void testFromEntityWithNullEntity() {
        StaffResponse response = StaffResponse.fromEntity(null);
        
        assertNull(response);
    }

    @Test
    @DisplayName("Test fromEntity handles null startDate gracefully")
    void testFromEntityWithNullStartDate() {
        StaffMember entity = new StaffMember();
        entity.setId(2L);
        entity.setName("Jane Smith");
        entity.setContact("jane.smith@example.com");
        entity.setRole("Doctor");
        entity.setEmploymentStatus("ACTIVE");
        entity.setStartDate(null);
        entity.setEndDate(LocalDate.of(2024, 12, 31));
        entity.setFacilityId(200L);
        
        StaffResponse response = StaffResponse.fromEntity(entity);
        
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getName(), response.getName());
        assertNull(response.getStartDate());
        assertEquals(entity.getEndDate(), response.getEndDate());
    }

    @Test
    @DisplayName("Test fromEntity handles null endDate gracefully")
    void testFromEntityWithNullEndDate() {
        StaffMember entity = new StaffMember();
        entity.setId(3L);
        entity.setName("Bob Johnson");
        entity.setContact("bob.johnson@example.com");
        entity.setRole("Technician");
        entity.setEmploymentStatus("ACTIVE");
        entity.setStartDate(LocalDate.of(2023, 6, 1));
        entity.setEndDate(null);
        entity.setFacilityId(300L);
        
        StaffResponse response = StaffResponse.fromEntity(entity);
        
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getName(), response.getName());
        assertEquals(entity.getStartDate(), response.getStartDate());
        assertNull(response.getEndDate());
    }

    @Test
    @DisplayName("Test fromEntity handles both null startDate and endDate gracefully")
    void testFromEntityWithBothDatesNull() {
        StaffMember entity = new StaffMember();
        entity.setId(4L);
        entity.setName("Alice Williams");
        entity.setContact("alice.williams@example.com");
        entity.setRole("Administrator");
        entity.setEmploymentStatus("PENDING");
        entity.setStartDate(null);
        entity.setEndDate(null);
        entity.setFacilityId(400L);
        
        StaffResponse response = StaffResponse.fromEntity(entity);
        
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getName(), response.getName());
        assertNull(response.getStartDate());
        assertNull(response.getEndDate());
    }

    @Test
    @DisplayName("Test setters and getters for id field")
    void testIdSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        Long id = 10L;
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    @DisplayName("Test setters and getters for name field")
    void testNameSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        String name = "Test Name";
        
        response.setName(name);
        
        assertEquals(name, response.getName());
    }

    @Test
    @DisplayName("Test setters and getters for contact field")
    void testContactSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        String contact = "test@example.com";
        
        response.setContact(contact);
        
        assertEquals(contact, response.getContact());
    }

    @Test
    @DisplayName("Test setters and getters for role field")
    void testRoleSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        String role = "Manager";
        
        response.setRole(role);
        
        assertEquals(role, response.getRole());
    }

    @Test
    @DisplayName("Test setters and getters for employmentStatus field")
    void testEmploymentStatusSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        String status = "INACTIVE";
        
        response.setEmploymentStatus(status);
        
        assertEquals(status, response.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test setters and getters for startDate field")
    void testStartDateSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        LocalDate startDate = LocalDate.of(2023, 3, 15);
        
        response.setStartDate(startDate);
        
        assertEquals(startDate, response.getStartDate());
    }

    @Test
    @DisplayName("Test setters and getters for endDate field")
    void testEndDateSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        LocalDate endDate = LocalDate.of(2024, 6, 30);
        
        response.setEndDate(endDate);
        
        assertEquals(endDate, response.getEndDate());
    }

    @Test
    @DisplayName("Test setters and getters for facilityId field")
    void testFacilityIdSetterAndGetter() {
        StaffResponse response = new StaffResponse();
        Long facilityId = 500L;
        
        response.setFacilityId(facilityId);
        
        assertEquals(facilityId, response.getFacilityId());
    }

    @Test
    @DisplayName("Test fromEntity with entity having all null fields except id")
    void testFromEntityWithMinimalData() {
        StaffMember entity = new StaffMember();
        entity.setId(5L);
        
        StaffResponse response = StaffResponse.fromEntity(entity);
        
        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertNull(response.getName());
        assertNull(response.getContact());
        assertNull(response.getRole());
        assertNull(response.getEmploymentStatus());
        assertNull(response.getStartDate());
        assertNull(response.getEndDate());
        assertNull(response.getFacilityId());
    }
}