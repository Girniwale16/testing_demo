package com.visionary.roster.exception;

import com.visionary.roster.dto.ErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testHandleValidationException_Returns422WithFieldErrors() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);

        FieldError fieldError1 = new FieldError("user", "name", "must not be blank");
        FieldError fieldError2 = new FieldError("user", "contact", "must be a valid contact");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        
        Map<String, String> errors = response.getBody().getFieldErrors();
        assertThat(errors).isNotNull();
        assertThat(errors).containsKey("name");
        assertThat(errors).containsKey("contact");
        assertThat(errors.get("name")).isEqualTo("must not be blank");
        assertThat(errors.get("contact")).isEqualTo("must be a valid contact");
    }

    @Test
    void testHandleValidationException_LogsCorrelationId() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);

        FieldError fieldError = new FieldError("user", "name", "must not be blank");
        List<FieldError> fieldErrors = Arrays.asList(fieldError);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void handleValidationException_shouldReturn422WithFieldErrors() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);

        FieldError fieldError1 = new FieldError("user", "username", "must not be blank");
        FieldError fieldError2 = new FieldError("user", "email", "must be a valid email");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
        assertThat(response.getBody().getFieldErrors()).containsEntry("username", "must not be blank");
        assertThat(response.getBody().getFieldErrors()).containsEntry("email", "must be a valid email");
        assertThat(response.getBody().getDetails()).contains("username: must not be blank");
        assertThat(response.getBody().getDetails()).contains("email: must be a valid email");
    }

    @Test
    void handleValidationException_shouldGenerateCorrelationIdWhenNotPresent() {
        // Arrange
        FieldError fieldError = new FieldError("user", "username", "must not be blank");
        List<FieldError> fieldErrors = Arrays.asList(fieldError);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNotEmpty();
    }

    @Test
    void handleInvalidCredentialsException_shouldReturn401() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentialsException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid username or password");
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    void handleInactiveAccountException_shouldReturn403() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        InactiveAccountException exception = new InactiveAccountException(123L, "Account is inactive");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInactiveAccountException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCOUNT_INACTIVE");
        assertThat(response.getBody().getMessage()).isEqualTo("Account is inactive");
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void handleForbiddenAccessException_shouldReturn403WithDetails() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        ForbiddenAccessException exception = new ForbiddenAccessException(
            123L, 456L, "Staff", "User does not have access to this facility"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenAccessException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("FORBIDDEN_ACCESS");
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void handleResourceNotFoundException_shouldReturn404WithResourceDetails() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        ResourceNotFoundException exception = new ResourceNotFoundException("Staff", "123");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Staff with ID 123 not found");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleResourceNotFoundException_shouldHandleNullResourceType() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        ResourceNotFoundException exception = new ResourceNotFoundException(null, "123");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource with ID 123 not found");
    }

    @Test
    void handleResourceNotFoundException_shouldHandleNullResourceId() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        ResourceNotFoundException exception = new ResourceNotFoundException("Staff", null);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Staff with ID unknown not found");
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument provided");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void handleEntityNotFoundException_shouldReturn404() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Entity not found");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleAuthenticationException_shouldReturn401() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Authentication failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication required");
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void handleGenericException_shouldReturn500() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    void allExceptionHandlers_shouldIncludeCorrelationIdInResponse() {
        // Test that all handlers include correlation ID
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);

        // Test multiple exception handlers
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException("test")
        );
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleGenericException(
            new Exception("test")
        );
        ResponseEntity<ErrorResponse> response3 = globalExceptionHandler.handleAccessDeniedException(
            new AccessDeniedException("test")
        );

        assertThat(response1.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response2.getBody().getCorrelationId()).isEqualTo(correlationId);
        assertThat(response3.getBody().getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void allExceptionHandlers_shouldGenerateCorrelationIdWhenMissing() {
        // Ensure MDC is clear
        MDC.clear();

        // Test multiple exception handlers
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException("test")
        );
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleGenericException(
            new Exception("test")
        );

        assertThat(response1.getBody().getCorrelationId()).isNotNull();
        assertThat(response1.getBody().getCorrelationId()).isNotEmpty();
        assertThat(response2.getBody().getCorrelationId()).isNotNull();
        assertThat(response2.getBody().getCorrelationId()).isNotEmpty();
    }

    @Test
    void allExceptionHandlers_shouldIncludeTimestamp() {
        // Test that all handlers include timestamp
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException("test")
        );
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleGenericException(
            new Exception("test")
        );

        assertThat(response1.getBody().getTimestamp()).isNotNull();
        assertThat(response2.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void allExceptionHandlers_shouldIncludeRemediation() {
        // Test that all handlers include remediation
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException("test")
        );
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleInvalidCredentialsException(
            new InvalidCredentialsException("test")
        );
        ResponseEntity<ErrorResponse> response3 = globalExceptionHandler.handleResourceNotFoundException(
            new ResourceNotFoundException("Staff", "123")
        );

        assertThat(response1.getBody().getRemediation()).isNotNull();
        assertThat(response1.getBody().getRemediation()).isNotEmpty();
        assertThat(response2.getBody().getRemediation()).isNotNull();
        assertThat(response2.getBody().getRemediation()).isNotEmpty();
        assertThat(response3.getBody().getRemediation()).isNotNull();
        assertThat(response3.getBody().getRemediation()).isNotEmpty();
    }

    @Test
    void handleValidationException_shouldHandleEmptyFieldErrors() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).isEmpty();
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void handleResourceNotFoundException_shouldIncludeDetailsFromException() {
        // Arrange
        String correlationId = "test-correlation-id";
        MDC.put("correlationId", correlationId);
        ResourceNotFoundException exception = new ResourceNotFoundException("Staff", "123", "Additional details");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isEqualTo(exception.getMessage());
    }

    @Test
    void getOrGenerateCorrelationId_shouldReturnExistingCorrelationId() {
        // Arrange
        String existingCorrelationId = "existing-correlation-id";
        MDC.put("correlationId", existingCorrelationId);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException("test")
        );

        // Assert
        assertThat(response.getBody().getCorrelationId()).isEqualTo(existingCorrelationId);
    }

    @Test
    void getOrGenerateCorrelationId_shouldGenerateNewCorrelationIdWhenEmpty() {
        // Arrange
        MDC.put("correlationId", "");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException("test")
        );

        // Assert
        assertThat(response.getBody().getCorrelationId()).isNotEmpty();
        assertThat(response.getBody().getCorrelationId()).isNotEqualTo("");
    }
}