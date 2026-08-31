package com.visionary.roster.dto;

import com.visionary.roster.entity.StaffMember;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateStaffRequest DTO Tests")
class CreateStaffRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create instance using no-args constructor")
    void testNoArgsConstructor() {
        CreateStaffRequest request = new CreateStaffRequest();
        assertNotNull(request);
    }

    @Test
    @DisplayName("Should create instance using all-args constructor with all fields")
    void testAllArgsConstructorWithAllFields() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        CreateStaffRequest request = new CreateStaffRequest(
            "John Doe",
            "john.doe@example.com",
            "Nurse",
            "Full-time",
            startDate,
            endDate
        );
        
        assertEquals("John Doe", request.getName());
        assertEquals("john.doe@example.com", request.getContact());
        assertEquals("Nurse", request.getRole());
        assertEquals("Full-time", request.getEmploymentStatus());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
    }

    @Test
    @DisplayName("Should create instance using all-args constructor with null dates")
    void testAllArgsConstructorWithNullDates() {
        CreateStaffRequest request = new CreateStaffRequest(
            "Jane Smith",
            "jane.smith@example.com",
            "Doctor",
            "Part-time",
            null,
            null
        );
        
        assertEquals("Jane Smith", request.getName());
        assertEquals("jane.smith@example.com", request.getContact());
        assertEquals("Doctor", request.getRole());
        assertEquals("Part-time", request.getEmploymentStatus());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
    }

    @Test
    @DisplayName("Should set and get name field")
    void testNameGetterAndSetter() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("Test Name");
        assertEquals("Test Name", request.getName());
    }

    @Test
    @DisplayName("Should set and get contact field")
    void testContactGetterAndSetter() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setContact("test@example.com");
        assertEquals("test@example.com", request.getContact());
    }

    @Test
    @DisplayName("Should set and get role field")
    void testRoleGetterAndSetter() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setRole("Administrator");
        assertEquals("Administrator", request.getRole());
    }

    @Test
    @DisplayName("Should set and get employmentStatus field")
    void testEmploymentStatusGetterAndSetter() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setEmploymentStatus("Contract");
        assertEquals("Contract", request.getEmploymentStatus());
    }

    @Test
    @DisplayName("Should set and get startDate field")
    void testStartDateGetterAndSetter() {
        CreateStaffRequest request = new CreateStaffRequest();
        LocalDate startDate = LocalDate.of(2024, 6, 15);
        request.setStartDate(startDate);
        assertEquals(startDate, request.getStartDate());
    }

    @Test
    @DisplayName("Should set and get endDate field")
    void testEndDateGetterAndSetter() {
        CreateStaffRequest request = new CreateStaffRequest();
        LocalDate endDate = LocalDate.of(2025, 6, 15);
        request.setEndDate(endDate);
        assertEquals(endDate, request.getEndDate());
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void testValidationFailsWhenNameIsNull() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName(null);
        request.setContact("contact@example.com");
        request.setRole("Nurse");
        request.setEmploymentStatus("Full-time");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name") 
                && v.getMessage().equals("Name is required")));
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testValidationFailsWhenNameIsBlank() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("   ");
        request.setContact("contact@example.com");
        request.setRole("Nurse");
        request.setEmploymentStatus("Full-time");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name") 
                && v.getMessage().equals("Name is required")));
    }

    @Test
    @DisplayName("Should fail validation when contact is null")
    void testValidationFailsWhenContactIsNull() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact(null);
        request.setRole("Nurse");
        request.setEmploymentStatus("Full-time");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("contact") 
                && v.getMessage().equals("Contact is required")));
    }

    @Test
    @DisplayName("Should fail validation when contact is blank")
    void testValidationFailsWhenContactIsBlank() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("");
        request.setRole("Nurse");
        request.setEmploymentStatus("Full-time");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("contact") 
                && v.getMessage().equals("Contact is required")));
    }

    @Test
    @DisplayName("Should fail validation when role is null")
    void testValidationFailsWhenRoleIsNull() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("contact@example.com");
        request.setRole(null);
        request.setEmploymentStatus("Full-time");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("role") 
                && v.getMessage().equals("Role is required")));
    }

    @Test
    @DisplayName("Should fail validation when role is blank")
    void testValidationFailsWhenRoleIsBlank() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("contact@example.com");
        request.setRole("  ");
        request.setEmploymentStatus("Full-time");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("role") 
                && v.getMessage().equals("Role is required")));
    }

    @Test
    @DisplayName("Should fail validation when employmentStatus is null")
    void testValidationFailsWhenEmploymentStatusIsNull() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("contact@example.com");
        request.setRole("Nurse");
        request.setEmploymentStatus(null);
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("employmentStatus") 
                && v.getMessage().equals("Employment status is required")));
    }

    @Test
    @DisplayName("Should fail validation when employmentStatus is blank")
    void testValidationFailsWhenEmploymentStatusIsBlank() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("contact@example.com");
        request.setRole("Nurse");
        request.setEmploymentStatus("");
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("employmentStatus") 
                && v.getMessage().equals("Employment status is required")));
    }

    @Test
    @DisplayName("Should pass validation when all required fields are provided and dates are null")
    void testValidationPassesWithRequiredFieldsAndNullDates() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("contact@example.com");
        request.setRole("Nurse");
        request.setEmploymentStatus("Full-time");
        request.setStartDate(null);
        request.setEndDate(null);
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation when all fields are provided")
    void testValidationPassesWithAllFields() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName("John Doe");
        request.setContact("contact@example.com");
        request.setRole("Nurse");
        request.setEmploymentStatus("Full-time");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should convert DTO to StaffMember entity with all fields")
    void testToEntityWithAllFields() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long facilityId = 100L;
        
        CreateStaffRequest request = new CreateStaffRequest(
            "John Doe",
            "john.doe@example.com",
            "Nurse",
            "Full-time",
            startDate,
            endDate
        );
        
        StaffMember staffMember = request.toEntity(facilityId);
        
        assertNotNull(staffMember);
        assertEquals(facilityId, staffMember.getFacilityId());
        assertEquals("John Doe", staffMember.getName());
        assertEquals("john.doe@example.com", staffMember.getContact());
        assertEquals("Nurse", staffMember.getRole());
        assertEquals("Full-time", staffMember.getEmploymentStatus());
        assertEquals(startDate, staffMember.getStartDate());
        assertEquals(endDate, staffMember.getEndDate());
    }

    @Test
    @DisplayName("Should convert DTO to StaffMember entity with null dates")
    void testToEntityWithNullDates() {
        Long facilityId = 200L;
        
        CreateStaffRequest request = new CreateStaffRequest(
            "Jane Smith",
            "jane.smith@example.com",
            "Doctor",
            "Part-time",
            null,
            null
        );
        
        StaffMember staffMember = request.toEntity(facilityId);
        
        assertNotNull(staffMember);
        assertEquals(facilityId, staffMember.getFacilityId());
        assertEquals("Jane Smith", staffMember.getName());
        assertEquals("jane.smith@example.com", staffMember.getContact());
        assertEquals("Doctor", staffMember.getRole());
        assertEquals("Part-time", staffMember.getEmploymentStatus());
        assertNull(staffMember.getStartDate());
        assertNull(staffMember.getEndDate());
    }

    @Test
    @DisplayName("Should convert DTO to StaffMember entity with only startDate")
    void testToEntityWithOnlyStartDate() {
        LocalDate startDate = LocalDate.of(2024, 3, 15);
        Long facilityId = 300L;
        
        CreateStaffRequest request = new CreateStaffRequest(
            "Bob Johnson",
            "bob.johnson@example.com",
            "Technician",
            "Contract",
            startDate,
            null
        );
        
        StaffMember staffMember = request.toEntity(facilityId);
        
        assertNotNull(staffMember);
        assertEquals(facilityId, staffMember.getFacilityId());
        assertEquals("Bob Johnson", staffMember.getName());
        assertEquals("bob.johnson@example.com", staffMember.getContact());
        assertEquals("Technician", staffMember.getRole());
        assertEquals("Contract", staffMember.getEmploymentStatus());
        assertEquals(startDate, staffMember.getStartDate());
        assertNull(staffMember.getEndDate());
    }

    @Test
    @DisplayName("Should convert DTO to StaffMember entity with different facilityId values")
    void testToEntityWithDifferentFacilityIds() {
        CreateStaffRequest request = new CreateStaffRequest(
            "Alice Brown",
            "alice.brown@example.com",
            "Manager",
            "Full-time",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        StaffMember staffMember1 = request.toEntity(1L);
        StaffMember staffMember2 = request.toEntity(999L);
        
        assertEquals(1L, staffMember1.getFacilityId());
        assertEquals(999L, staffMember2.getFacilityId());
        assertEquals(staffMember1.getName(), staffMember2.getName());
        assertEquals(staffMember1.getContact(), staffMember2.getContact());
    }

    @Test
    @DisplayName("Should handle multiple validation violations")
    void testMultipleValidationViolations() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setName(null);
        request.setContact("");
        request.setRole("  ");
        request.setEmploymentStatus(null);
        
        Set<ConstraintViolation<CreateStaffRequest>> violations = validator.validate(request);
        
        assertEquals(4, violations.size());
    }
}