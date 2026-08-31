package com.visionary.roster.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StaffUpdateRequest Validation Tests")
class StaffUpdateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid StaffUpdateRequest should pass all validations")
    void testValidStaffUpdateRequest() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);
        request.setEmploymentStatus("Full-time");

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("FirstName should not be blank")
    void testFirstNameNotBlank() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        ConstraintViolation<StaffUpdateRequest> violation = violations.iterator().next();
        assertEquals("First name is required", violation.getMessage());
        assertEquals("firstName", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("FirstName should not be null")
    void testFirstNameNotNull() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName(null);
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        ConstraintViolation<StaffUpdateRequest> violation = violations.iterator().next();
        assertEquals("First name is required", violation.getMessage());
    }

    @Test
    @DisplayName("FirstName should not exceed 100 characters")
    void testFirstNameMaxSize() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("a".repeat(101));
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("size"));
    }

    @Test
    @DisplayName("FirstName with exactly 100 characters should be valid")
    void testFirstNameExactly100Characters() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("a".repeat(100));
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LastName should not be blank")
    void testLastNameNotBlank() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        ConstraintViolation<StaffUpdateRequest> violation = violations.iterator().next();
        assertEquals("Last name is required", violation.getMessage());
        assertEquals("lastName", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("LastName should not be null")
    void testLastNameNotNull() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName(null);
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        ConstraintViolation<StaffUpdateRequest> violation = violations.iterator().next();
        assertEquals("Last name is required", violation.getMessage());
    }

    @Test
    @DisplayName("LastName should not exceed 100 characters")
    void testLastNameMaxSize() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("a".repeat(101));
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("size"));
    }

    @Test
    @DisplayName("LastName with exactly 100 characters should be valid")
    void testLastNameExactly100Characters() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("a".repeat(100));
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Email should not be blank")
    void testEmailNotBlank() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    @DisplayName("Email should not be null")
    void testEmailNotNull() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(null);
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Email should be valid format")
    void testEmailValidFormat() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Email must be valid", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Email should not exceed 255 characters")
    void testEmailMaxSize() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("a".repeat(246) + "@test.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("size"));
    }

    @Test
    @DisplayName("Email with exactly 255 characters should be valid")
    void testEmailExactly255Characters() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("a".repeat(244) + "@test.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Role should not be blank")
    void testRoleNotBlank() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("");
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        ConstraintViolation<StaffUpdateRequest> violation = violations.iterator().next();
        assertEquals("Role is required", violation.getMessage());
        assertEquals("role", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Role should not be null")
    void testRoleNotNull() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole(null);
        request.setFacilityId(1L);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Role is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("FacilityId should not be null")
    void testFacilityIdNotNull() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(null);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        ConstraintViolation<StaffUpdateRequest> violation = violations.iterator().next();
        assertEquals("Facility ID is required", violation.getMessage());
        assertEquals("facilityId", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("EmploymentStatus is optional and can be null")
    void testEmploymentStatusOptional() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);
        request.setEmploymentStatus(null);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "EmploymentStatus should be optional");
    }

    @Test
    @DisplayName("EmploymentStatus can be empty string")
    void testEmploymentStatusEmptyString() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setRole("Nurse");
        request.setFacilityId(1L);
        request.setEmploymentStatus("");

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "EmploymentStatus should accept empty string");
    }

    @Test
    @DisplayName("Multiple validation failures should be reported")
    void testMultipleValidationFailures() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName(null);
        request.setLastName(null);
        request.setEmail(null);
        request.setRole(null);
        request.setFacilityId(null);

        Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
        assertEquals(5, violations.size(), "Should have 5 validation failures");
    }

    @Test
    @DisplayName("Getter and Setter for FirstName should work correctly")
    void testFirstNameGetterSetter() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFirstName("John");
        assertEquals("John", request.getFirstName());
    }

    @Test
    @DisplayName("Getter and Setter for LastName should work correctly")
    void testLastNameGetterSetter() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setLastName("Doe");
        assertEquals("Doe", request.getLastName());
    }

    @Test
    @DisplayName("Getter and Setter for Email should work correctly")
    void testEmailGetterSetter() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setEmail("john.doe@example.com");
        assertEquals("john.doe@example.com", request.getEmail());
    }

    @Test
    @DisplayName("Getter and Setter for Role should work correctly")
    void testRoleGetterSetter() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setRole("Nurse");
        assertEquals("Nurse", request.getRole());
    }

    @Test
    @DisplayName("Getter and Setter for FacilityId should work correctly")
    void testFacilityIdGetterSetter() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFacilityId(123L);
        assertEquals(123L, request.getFacilityId());
    }

    @Test
    @DisplayName("Getter and Setter for EmploymentStatus should work correctly")
    void testEmploymentStatusGetterSetter() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setEmploymentStatus("Full-time");
        assertEquals("Full-time", request.getEmploymentStatus());
    }

    @Test
    @DisplayName("Valid email formats should pass validation")
    void testValidEmailFormats() {
        String[] validEmails = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com"
        };

        for (String email : validEmails) {
            StaffUpdateRequest request = new StaffUpdateRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setEmail(email);
            request.setRole("Nurse");
            request.setFacilityId(1L);

            Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
        }
    }

    @Test
    @DisplayName("Invalid email formats should fail validation")
    void testInvalidEmailFormats() {
        String[] invalidEmails = {
            "plaintext",
            "@example.com",
            "user@",
            "user name@example.com",
            "user@example"
        };

        for (String email : invalidEmails) {
            StaffUpdateRequest request = new StaffUpdateRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setEmail(email);
            request.setRole("Nurse");
            request.setFacilityId(1L);

            Set<ConstraintViolation<StaffUpdateRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty(), "Email " + email + " should be invalid");
        }
    }
}