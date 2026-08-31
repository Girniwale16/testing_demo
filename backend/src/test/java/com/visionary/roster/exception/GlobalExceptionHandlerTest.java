package com.visionary.roster.exception;

import com.visionary.roster.dto.ErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleValidationException_shouldReturn422WithFieldErrors() {
        // Arrange
        FieldError fieldError1 = new FieldError("user", "email", "Email is required");
        FieldError fieldError2 = new FieldError("user", "name", "Name must not be blank");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getStatusCode().value());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(422, errorResponse.getStatus());
        assertEquals("VALIDATION_ERROR", errorResponse.getErrorCode());
        assertEquals("Request validation failed", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getCorrelationId());
        
        Map<String, String> fieldErrorsMap = errorResponse.getFieldErrors();
        assertNotNull(fieldErrorsMap);
        assertEquals(2, fieldErrorsMap.size());
        assertEquals("Email is required", fieldErrorsMap.get("email"));
        assertEquals("Name must not be blank", fieldErrorsMap.get("name"));
        
        assertTrue(errorResponse.getDetails().contains("Email is required"));
        assertTrue(errorResponse.getDetails().contains("Name must not be blank"));
    }

    @Test
    void handleValidationException_shouldIncludeTimestamp() {
        // Arrange
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(!errorResponse.getTimestamp().isBefore(beforeCall));
        assertTrue(!errorResponse.getTimestamp().isAfter(afterCall));
    }

    @Test
    void handleValidationException_shouldIncludeStatusCode() {
        // Arrange
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(422, errorResponse.getStatus());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), errorResponse.getStatus());
    }

    @Test
    void handleForbiddenAccessException_shouldReturn403WithStandardizedErrorResponse() {
        // Arrange
        ForbiddenAccessException exception = new ForbiddenAccessException(
            "User does not have access to this facility",
            123L,
            456L,
            "Facility",
            "User not assigned to facility"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenAccessException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getStatusCode().value());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(403, errorResponse.getStatus());
        assertEquals("FORBIDDEN_ACCESS", errorResponse.getErrorCode());
        assertEquals("User does not have access to this facility", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getCorrelationId());
    }

    @Test
    void handleForbiddenAccessException_shouldIncludeTimestampAndStatus() {
        // Arrange
        ForbiddenAccessException exception = new ForbiddenAccessException(
            "Access denied",
            123L,
            456L,
            "Resource",
            "Insufficient permissions"
        );

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenAccessException(exception);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(!errorResponse.getTimestamp().isBefore(beforeCall));
        assertTrue(!errorResponse.getTimestamp().isAfter(afterCall));
        assertEquals(403, errorResponse.getStatus());
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400ForMalformedPayloads() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid date format");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getStatusCode().value());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("BAD_REQUEST", errorResponse.getErrorCode());
        assertEquals("Invalid date format", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getCorrelationId());
    }

    @Test
    void handleIllegalArgumentException_shouldIncludeTimestampAndStatus() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Malformed request");

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(!errorResponse.getTimestamp().isBefore(beforeCall));
        assertTrue(!errorResponse.getTimestamp().isAfter(afterCall));
        assertEquals(400, errorResponse.getStatus());
    }

    @Test
    void handleEntityNotFoundException_shouldReturn404WithStandardizedErrorResponse() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("User with ID 123 not found");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getStatusCode().value());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.getStatus());
        assertEquals("NOT_FOUND", errorResponse.getErrorCode());
        assertEquals("User with ID 123 not found", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getCorrelationId());
        assertEquals("The requested resource could not be found", errorResponse.getRemediation());
    }

    @Test
    void handleEntityNotFoundException_shouldIncludeTimestampAndStatus() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Resource not found");

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(!errorResponse.getTimestamp().isBefore(beforeCall));
        assertTrue(!errorResponse.getTimestamp().isAfter(afterCall));
        assertEquals(404, errorResponse.getStatus());
    }

    @Test
    void handleInvalidCredentialsException_shouldReturn401() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid username or password");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentialsException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(401, errorResponse.getStatus());
        assertEquals("INVALID_CREDENTIALS", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleInactiveAccountException_shouldReturn403() {
        // Arrange
        InactiveAccountException exception = new InactiveAccountException("Account is inactive", 123L);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInactiveAccountException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(403, errorResponse.getStatus());
        assertEquals("ACCOUNT_INACTIVE", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleAuthenticationException_shouldReturn401() {
        // Arrange
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Authentication failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(401, errorResponse.getStatus());
        assertEquals("AUTHENTICATION_FAILED", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(403, errorResponse.getStatus());
        assertEquals("ACCESS_DENIED", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleGenericException_shouldReturn500() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void allExceptionHandlers_shouldMaintainBackwardCompatibilityWithErrorResponseStructure() {
        // Test that all handlers return ErrorResponse with required fields
        
        // Test validation exception
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        
        ResponseEntity<ErrorResponse> validationResponse = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);
        assertErrorResponseStructure(validationResponse.getBody());
        
        // Test forbidden access exception
        ForbiddenAccessException forbiddenException = new ForbiddenAccessException("Access denied", 1L, 2L, "Resource", "Reason");
        ResponseEntity<ErrorResponse> forbiddenResponse = globalExceptionHandler.handleForbiddenAccessException(forbiddenException);
        assertErrorResponseStructure(forbiddenResponse.getBody());
        
        // Test illegal argument exception
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Bad request");
        ResponseEntity<ErrorResponse> illegalArgResponse = globalExceptionHandler.handleIllegalArgumentException(illegalArgException);
        assertErrorResponseStructure(illegalArgResponse.getBody());
        
        // Test entity not found exception
        EntityNotFoundException notFoundException = new EntityNotFoundException("Not found");
        ResponseEntity<ErrorResponse> notFoundResponse = globalExceptionHandler.handleEntityNotFoundException(notFoundException);
        assertErrorResponseStructure(notFoundResponse.getBody());
    }

    @Test
    void allExceptionHandlers_shouldFollowStandardizedErrorResponseEnvelope() {
        // Verify all handlers include: errorCode, message, status, timestamp, correlationId
        
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);
        ErrorResponse errorResponse = response.getBody();
        
        assertNotNull(errorResponse.getErrorCode());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getCorrelationId());
    }

    @Test
    void handleValidationException_shouldUseExistingCorrelationIdFromMDC() {
        // Arrange
        String existingCorrelationId = "test-correlation-id-123";
        MDC.put("correlationId", existingCorrelationId);
        
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(existingCorrelationId, errorResponse.getCorrelationId());
    }

    @Test
    void handleValidationException_shouldGenerateCorrelationIdWhenNotInMDC() {
        // Arrange
        MDC.clear();
        
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getCorrelationId());
        assertFalse(errorResponse.getCorrelationId().isEmpty());
    }

    @Test
    void handleValidationException_withEmptyFieldErrors_shouldReturnEmptyFieldErrorsMap() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getFieldErrors());
        assertTrue(errorResponse.getFieldErrors().isEmpty());
    }

    @Test
    void handleValidationException_withMultipleFieldErrors_shouldIterateThroughAllFieldErrors() {
        // Arrange
        FieldError fieldError1 = new FieldError("user", "email", "Email is required");
        FieldError fieldError2 = new FieldError("user", "password", "Password must be at least 8 characters");
        FieldError fieldError3 = new FieldError("user", "username", "Username is already taken");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2, fieldError3);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        
        Map<String, String> fieldErrorsMap = errorResponse.getFieldErrors();
        assertEquals(3, fieldErrorsMap.size());
        assertEquals("Email is required", fieldErrorsMap.get("email"));
        assertEquals("Password must be at least 8 characters", fieldErrorsMap.get("password"));
        assertEquals("Username is already taken", fieldErrorsMap.get("username"));
    }

    private void assertErrorResponseStructure(ErrorResponse errorResponse) {
        assertNotNull(errorResponse, "ErrorResponse should not be null");
        assertNotNull(errorResponse.getErrorCode(), "errorCode should not be null");
        assertNotNull(errorResponse.getMessage(), "message should not be null");
        assertNotNull(errorResponse.getStatus(), "status should not be null");
        assertNotNull(errorResponse.getTimestamp(), "timestamp should not be null");
        assertNotNull(errorResponse.getCorrelationId(), "correlationId should not be null");
    }
}