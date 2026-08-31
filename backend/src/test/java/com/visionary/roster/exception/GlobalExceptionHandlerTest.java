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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for GlobalExceptionHandler.
 * Tests verify error response structure for 401, 403, and 5xx scenarios.
 * Ensures all exception handlers return ErrorResponse DTO with consistent structure.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MDC.clear();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    // ==================== 401 UNAUTHORIZED Tests ====================

    @Test
    void handleInvalidCredentialsException_shouldReturn401WithCorrectStructure() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentialsException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(401);
        assertThat(errorResponse.getError()).isEqualTo("Unauthorized");
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid username or password");
        assertThat(errorResponse.getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCorrelationId()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getRemediation()).contains("verify your credentials");
    }

    @Test
    void handleAuthenticationException_shouldReturn401WithLoginRedirectMessage() {
        // Arrange
        AuthenticationException exception = new BadCredentialsException("Authentication failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(401);
        assertThat(errorResponse.getError()).isEqualTo("Unauthorized");
        assertThat(errorResponse.getMessage()).contains("Authentication required");
        assertThat(errorResponse.getMessage()).contains("please log in to continue");
        assertThat(errorResponse.getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getRemediation()).contains("Please log in");
    }

    @Test
    void handle401Exceptions_shouldUseExistingCorrelationIdFromMDC() {
        // Arrange
        String existingCorrelationId = "test-correlation-123";
        MDC.put("correlationId", existingCorrelationId);
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentialsException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(existingCorrelationId);
    }

    // ==================== 403 FORBIDDEN Tests ====================

    @Test
    void handleInactiveAccountException_shouldReturn403WithClearMessage() {
        // Arrange
        InactiveAccountException exception = new InactiveAccountException("user123", "Account is disabled");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInactiveAccountException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Forbidden");
        assertThat(errorResponse.getMessage()).contains("not authorized");
        assertThat(errorResponse.getMessage()).contains("account is inactive");
        assertThat(errorResponse.getErrorCode()).isEqualTo("ACCOUNT_INACTIVE");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getRemediation()).contains("contact your system administrator");
    }

    @Test
    void handleForbiddenAccessException_shouldReturn403WithNotAuthorizedMessage() {
        // Arrange
        ForbiddenAccessException exception = new ForbiddenAccessException(
            "user123", "facility456", "Employee", "User does not have access to this facility"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenAccessException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Forbidden");
        assertThat(errorResponse.getMessage()).isEqualTo("You are not authorized to access this resource");
        assertThat(errorResponse.getErrorCode()).isEqualTo("FORBIDDEN_ACCESS");
        assertThat(errorResponse.getDetails()).contains("User does not have access");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getRemediation()).contains("contact your system administrator");
    }

    @Test
    void handleAccessDeniedException_shouldReturn403WithNotAuthorizedMessage() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Forbidden");
        assertThat(errorResponse.getMessage()).isEqualTo("You are not authorized to access this resource");
        assertThat(errorResponse.getErrorCode()).isEqualTo("ACCESS_DENIED");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getRemediation()).contains("do not have permission");
    }

    // ==================== 5xx INTERNAL SERVER ERROR Tests ====================

    @Test
    void handleGenericException_shouldReturn500WithActionableRetryMessage() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected database error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.getMessage()).contains("unexpected error occurred");
        assertThat(errorResponse.getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getRemediation()).contains("try again in a few moments");
        assertThat(errorResponse.getRemediation()).contains("contact support");
        assertThat(errorResponse.getRemediation()).contains("correlation ID");
    }

    @Test
    void handle500Exception_shouldIncludeCorrelationIdInRemediationMessage() {
        // Arrange
        Exception exception = new NullPointerException("Null pointer error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getRemediation()).contains(errorResponse.getCorrelationId());
    }

    // ==================== ErrorResponse Structure Consistency Tests ====================

    @Test
    void handleValidationException_shouldReturnConsistentErrorResponseStructure() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "must not be blank");
        List<FieldError> fieldErrors = Arrays.asList(fieldError);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(422);
        assertThat(errorResponse.getError()).isEqualTo("Unprocessable Entity");
        assertThat(errorResponse.getMessage()).isNotNull();
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCorrelationId()).isNotNull();
        assertThat(errorResponse.getErrorCode()).isNotNull();
    }

    @Test
    void handleResourceNotFoundException_shouldReturnConsistentErrorResponseStructure() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Employee", "123");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).contains("Employee with ID 123 not found");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCorrelationId()).isNotNull();
        assertThat(errorResponse.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void handleIllegalArgumentException_shouldReturnConsistentErrorResponseStructure() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter value");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Bad Request");
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid parameter value");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCorrelationId()).isNotNull();
        assertThat(errorResponse.getErrorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void handleEntityNotFoundException_shouldReturnConsistentErrorResponseStructure() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFoundException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).isEqualTo("Entity not found");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getCorrelationId()).isNotNull();
        assertThat(errorResponse.getErrorCode()).isEqualTo("NOT_FOUND");
    }

    // ==================== Backward Compatibility Tests ====================

    @Test
    void allExceptionHandlers_shouldReturnErrorResponseWithRequiredFields() {
        // Test that all handlers return ErrorResponse with timestamp, status, error, message, path
        
        // 401 - InvalidCredentialsException
        ResponseEntity<ErrorResponse> response401 = globalExceptionHandler.handleInvalidCredentialsException(
            new InvalidCredentialsException("test"), webRequest
        );
        verifyErrorResponseStructure(response401.getBody(), 401);

        // 403 - ForbiddenAccessException
        ResponseEntity<ErrorResponse> response403 = globalExceptionHandler.handleForbiddenAccessException(
            new ForbiddenAccessException("user", "facility", "resource", "reason"), webRequest
        );
        verifyErrorResponseStructure(response403.getBody(), 403);

        // 500 - Generic Exception
        ResponseEntity<ErrorResponse> response500 = globalExceptionHandler.handleGenericException(
            new RuntimeException("test"), webRequest
        );
        verifyErrorResponseStructure(response500.getBody(), 500);
    }

    private void verifyErrorResponseStructure(ErrorResponse errorResponse, int expectedStatus) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(errorResponse.getError()).isNotNull();
        assertThat(errorResponse.getMessage()).isNotNull();
        assertThat(errorResponse.getPath()).isNotNull();
        assertThat(errorResponse.getCorrelationId()).isNotNull();
    }

    // ==================== Correlation ID Tests ====================

    @Test
    void allExceptionHandlers_shouldGenerateCorrelationIdWhenNotInMDC() {
        // Arrange - ensure MDC is clear
        MDC.clear();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
            new RuntimeException("test"), webRequest
        );

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNotEmpty();
    }

    @Test
    void allExceptionHandlers_shouldReuseExistingCorrelationIdFromMDC() {
        // Arrange
        String existingCorrelationId = "existing-correlation-id-456";
        MDC.put("correlationId", existingCorrelationId);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
            new RuntimeException("test"), webRequest
        );

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(existingCorrelationId);
    }

    // ==================== Frontend Integration Tests ====================

    @Test
    void handle401Responses_shouldContainMessagesSuitableForLoginRedirect() {
        // Test InvalidCredentialsException
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleInvalidCredentialsException(
            new InvalidCredentialsException("test"), webRequest
        );
        assertThat(response1.getBody().getMessage()).containsAnyOf("Invalid username or password", "credentials");

        // Test AuthenticationException
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleAuthenticationException(
            new BadCredentialsException("test"), webRequest
        );
        assertThat(response2.getBody().getMessage()).containsAnyOf("Authentication required", "log in");
    }

    @Test
    void handle403Responses_shouldContainClearNotAuthorizedMessages() {
        // Test InactiveAccountException
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleInactiveAccountException(
            new InactiveAccountException("user", "test"), webRequest
        );
        assertThat(response1.getBody().getMessage()).contains("not authorized");

        // Test ForbiddenAccessException
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleForbiddenAccessException(
            new ForbiddenAccessException("user", "facility", "resource", "reason"), webRequest
        );
        assertThat(response2.getBody().getMessage()).contains("not authorized");

        // Test AccessDeniedException
        ResponseEntity<ErrorResponse> response3 = globalExceptionHandler.handleAccessDeniedException(
            new AccessDeniedException("test"), webRequest
        );
        assertThat(response3.getBody().getMessage()).contains("not authorized");
    }

    @Test
    void handle500Responses_shouldContainActionableRetryMessages() {
        // Arrange
        Exception exception = new RuntimeException("Database connection failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRemediation()).contains("try again");
        assertThat(response.getBody().getRemediation()).contains("contact support");
    }
}