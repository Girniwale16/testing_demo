package com.visionary.roster.dto;

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

/**
 * Comprehensive test suite for UpdateStaffRequest DTO.
 * Tests all validation annotations, constructors, getters, setters, equals, hashCode, and toString.
 */
class UpdateStaffRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests
    @Test
    @DisplayName("Test no-args constructor creates empty object")
    void testNoArgsConstructor() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        assertNotNull(request);
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getContact());
        assertNull(request.getRole());
        assertNull(request.getEmploymentStatus());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
    }

    @Test
    @DisplayName("Test all-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john.doe@example.com", request.getContact());
        assertEquals("Developer", request.getRole());
        assertEquals("Full-Time", request.getEmploymentStatus());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
    }

    // Validation Tests - @NotBlank firstName
    @Test
    @DisplayName("Test firstName validation - null value fails")
    void testFirstNameNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            null, "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("First name is required")));
    }

    @Test
    @DisplayName("Test firstName validation - empty string fails")
    void testFirstNameEmpty() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    @DisplayName("Test firstName validation - blank string fails")
    void testFirstNameBlank() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "   ", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    @DisplayName("Test firstName validation - valid value passes")
    void testFirstNameValid() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    // Validation Tests - @NotBlank lastName
    @Test
    @DisplayName("Test lastName validation - null value fails")
    void testLastNameNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", null, "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Last name is required")));
    }

    @Test
    @DisplayName("Test lastName validation - empty string fails")
    void testLastNameEmpty() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    @DisplayName("Test lastName validation - valid value passes")
    void testLastNameValid() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    // Validation Tests - @NotBlank and @Pattern contact
    @Test
    @DisplayName("Test contact validation - null value fails")
    void testContactNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", null, "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Contact is required")));
    }

    @Test
    @DisplayName("Test contact validation - empty string fails")
    void testContactEmpty() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
    }

    @Test
    @DisplayName("Test contact validation - valid email passes")
    void testContactValidEmail() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
    }

    @Test
    @DisplayName("Test contact validation - valid phone number passes")
    void testContactValidPhone() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "1234567890", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
    }

    @Test
    @DisplayName("Test contact validation - valid phone with plus sign passes")
    void testContactValidPhoneWithPlus() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "+1234567890", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
    }

    @Test
    @DisplayName("Test contact validation - invalid email format fails")
    void testContactInvalidEmail() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "invalid-email", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Contact must be a valid email or phone number")));
    }

    @Test
    @DisplayName("Test contact validation - invalid phone format fails")
    void testContactInvalidPhone() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "123", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contact")));
    }

    // Validation Tests - @NotBlank role
    @Test
    @DisplayName("Test role validation - null value fails")
    void testRoleNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", null, 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    @DisplayName("Test role validation - empty string fails")
    void testRoleEmpty() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("Test role validation - valid value passes")
    void testRoleValid() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    // Validation Tests - @NotBlank employmentStatus
    @Test
    @DisplayName("Test employmentStatus validation - null value fails")
    void testEmploymentStatusNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            null, LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("employmentStatus")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Employment status is required")));
    }

    @Test
    @DisplayName("Test employmentStatus validation - empty string fails")
    void testEmploymentStatusEmpty() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("employmentStatus")));
    }

    @Test
    @DisplayName("Test employmentStatus validation - valid value passes")
    void testEmploymentStatusValid() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("employmentStatus")));
    }

    // Validation Tests - @PastOrPresent startDate
    @Test
    @DisplayName("Test startDate validation - future date fails")
    void testStartDateFuture() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be in the past or present")));
    }

    @Test
    @DisplayName("Test startDate validation - present date passes")
    void testStartDatePresent() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now(), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDate") && 
            v.getMessage().equals("Start date must be in the past or present")
        ));
    }

    @Test
    @DisplayName("Test startDate validation - past date passes")
    void testStartDatePast() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now().minusDays(10), LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDate") && 
            v.getMessage().equals("Start date must be in the past or present")
        ));
    }

    @Test
    @DisplayName("Test startDate validation - null value passes")
    void testStartDateNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", null, LocalDate.now().plusDays(1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDate") && 
            v.getMessage().equals("Start date must be in the past or present")
        ));
    }

    // Validation Tests - @ValidDateRange custom annotation
    @Test
    @DisplayName("Test ValidDateRange validation - startDate after endDate fails")
    void testValidDateRangeStartAfterEnd() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2024, 12, 31), LocalDate.of(2024, 1, 1)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be before end date")));
    }

    @Test
    @DisplayName("Test ValidDateRange validation - startDate equals endDate fails")
    void testValidDateRangeStartEqualsEnd() {
        LocalDate sameDate = LocalDate.of(2024, 6, 15);
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", sameDate, sameDate
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be before end date")));
    }

    @Test
    @DisplayName("Test ValidDateRange validation - startDate before endDate passes")
    void testValidDateRangeStartBeforeEnd() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be before end date")));
    }

    @Test
    @DisplayName("Test ValidDateRange validation - null startDate passes")
    void testValidDateRangeNullStartDate() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", null, LocalDate.of(2024, 12, 31)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be before end date")));
    }

    @Test
    @DisplayName("Test ValidDateRange validation - null endDate passes")
    void testValidDateRangeNullEndDate() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2024, 1, 1), null
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be before end date")));
    }

    @Test
    @DisplayName("Test ValidDateRange validation - both dates null passes")
    void testValidDateRangeBothNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", null, null
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().equals("Start date must be before end date")));
    }

    @Test
    @DisplayName("Test ValidDateRange validation - null request passes")
    void testValidDateRangeNullRequest() {
        UpdateStaffRequest.DateRangeValidator validator = new UpdateStaffRequest.DateRangeValidator();
        assertTrue(validator.isValid(null, null));
    }

    // Getter and Setter Tests
    @Test
    @DisplayName("Test setFirstName and getFirstName")
    void testSetGetFirstName() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFirstName("Jane");
        assertEquals("Jane", request.getFirstName());
    }

    @Test
    @DisplayName("Test setLastName and getLastName")
    void testSetGetLastName() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setLastName("Smith");
        assertEquals("Smith", request.getLastName());
    }

    @Test
    @DisplayName("Test setContact and getContact")
    void testSetGetContact() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setContact("jane.smith@example.com");
        assertEquals("jane.smith@example.com", request.getContact());
    }

    @Test
    @DisplayName("Test setRole and getRole")
    void testSetGetRole() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setRole("Manager");
        assertEquals("Manager", request.getRole());
    }

    @Test
    @DisplayName("Test setEmploymentStatus and getEmploymentStatus")
    void testSetGetEmploymentStatus() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setEmploymentStatus("Part-Time");
        assertEquals("Part-Time", request.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test setStartDate and getStartDate")
    void testSetGetStartDate() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        LocalDate date = LocalDate.of(2023, 5, 15);
        request.setStartDate(date);
        assertEquals(date, request.getStartDate());
    }

    @Test
    @DisplayName("Test setEndDate and getEndDate")
    void testSetGetEndDate() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        LocalDate date = LocalDate.of(2024, 5, 15);
        request.setEndDate(date);
        assertEquals(date, request.getEndDate());
    }

    // Equals and HashCode Tests
    @Test
    @DisplayName("Test equals - same object returns true")
    void testEqualsSameObject() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)
        );
        
        assertEquals(request, request);
    }

    @Test
    @DisplayName("Test equals - equal objects return true")
    void testEqualsEqualObjects() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Test equals - different firstName returns false")
    void testEqualsDifferentFirstName() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "Jane", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - different lastName returns false")
    void testEqualsDifferentLastName() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Smith", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - different contact returns false")
    void testEqualsDifferentContact() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "jane.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - different role returns false")
    void testEqualsDifferentRole() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Manager", 
            "Full-Time", startDate, endDate
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - different employmentStatus returns false")
    void testEqualsDifferentEmploymentStatus() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Part-Time", startDate, endDate
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - different startDate returns false")
    void testEqualsDifferentStartDate() {
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 6, 1), LocalDate.of(2024, 1, 1)
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - different endDate returns false")
    void testEqualsDifferentEndDate() {
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 6, 1)
        );
        
        assertNotEquals(request1, request2);
    }

    @Test
    @DisplayName("Test equals - null object returns false")
    void testEqualsNull() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)
        );
        
        assertNotEquals(request, null);
    }

    @Test
    @DisplayName("Test equals - different class returns false")
    void testEqualsDifferentClass() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)
        );
        
        assertNotEquals(request, new Object());
    }

    @Test
    @DisplayName("Test hashCode - equal objects have same hashCode")
    void testHashCodeEqual() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Test hashCode - different objects have different hashCode")
    void testHashCodeDifferent() {
        UpdateStaffRequest request1 = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)
        );
        
        UpdateStaffRequest request2 = new UpdateStaffRequest(
            "Jane", "Smith", "jane.smith@example.com", "Manager", 
            "Part-Time", LocalDate.of(2023, 6, 1), LocalDate.of(2024, 6, 1)
        );
        
        assertNotEquals(request1.hashCode(), request2.hashCode());
    }

    // ToString Tests
    @Test
    @DisplayName("Test toString contains all field values")
    void testToString() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", startDate, endDate
        );
        
        String result = request.toString();
        
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
        assertTrue(result.contains("john.doe@example.com"));
        assertTrue(result.contains("Developer"));
        assertTrue(result.contains("Full-Time"));
        assertTrue(result.contains(startDate.toString()));
        assertTrue(result.contains(endDate.toString()));
        assertTrue(result.contains("UpdateStaffRequest"));
    }

    @Test
    @DisplayName("Test toString with null values")
    void testToStringWithNulls() {
        UpdateStaffRequest request = new UpdateStaffRequest();
        String result = request.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("UpdateStaffRequest"));
    }

    // Complete validation test
    @Test
    @DisplayName("Test complete valid request passes all validations")
    void testCompleteValidRequest() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "John", "Doe", "john.doe@example.com", "Developer", 
            "Full-Time", LocalDate.now().minusDays(30), LocalDate.now().plusDays(365)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test multiple validation failures")
    void testMultipleValidationFailures() {
        UpdateStaffRequest request = new UpdateStaffRequest(
            "", "", "invalid", "", 
            "", LocalDate.now().plusDays(10), LocalDate.now().minusDays(10)
        );
        
        Set<ConstraintViolation<UpdateStaffRequest>> violations = validator.validate(request);
        assertTrue(violations.size() >= 5);
    }
}