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
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    private static final String TEST_CORRELATION_ID = "test-correlation-id-12345";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testHandleFacilityAccessDeniedException_WithCorrelationIdFromHeader() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("User does not have access to facility");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(TEST_CORRELATION_ID);
        assertThat(response.getBody().getErrorCode()).isEqualTo("FACILITY_ACCESS_DENIED");
        assertThat(response.getBody().getMessage()).isEqualTo("No facility access");
        assertThat(response.getBody().getRemediation()).isEqualTo("Please contact your system administrator if you believe you should have access to this facility");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().getTimestamp()).isNotNull();

        verify(webRequest, times(1)).getHeader(CORRELATION_ID_HEADER);
    }

    @Test
    void testHandleFacilityAccessDeniedException_WithCorrelationIdFromMDC() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("Facility access denied");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(null);
        MDC.put("correlationId", TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo(TEST_CORRELATION_ID);
        assertThat(response.getBody().getErrorCode()).isEqualTo("FACILITY_ACCESS_DENIED");
        assertThat(response.getBody().getMessage()).isEqualTo("No facility access");
        assertThat(response.getBody().getStatus()).isEqualTo(403);

        verify(webRequest, times(1)).getHeader(CORRELATION_ID_HEADER);
    }

    @Test
    void testHandleFacilityAccessDeniedException_WithGeneratedCorrelationId() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("No facility access");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(null);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNotEmpty();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FACILITY_ACCESS_DENIED");
        assertThat(response.getBody().getMessage()).isEqualTo("No facility access");
        assertThat(response.getBody().getRemediation()).contains("system administrator");
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getTimestamp()).isNotNull();

        verify(webRequest, times(1)).getHeader(CORRELATION_ID_HEADER);
    }

    @Test
    void testHandleFacilityAccessDeniedException_ErrorResponseStructureConsistency() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("Access denied to facility");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert - Verify ErrorResponse DTO structure consistency
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getCorrelationId()).isNotNull();
        assertThat(errorResponse.getErrorCode()).isNotNull();
        assertThat(errorResponse.getMessage()).isNotNull();
        assertThat(errorResponse.getRemediation()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isNotNull();
    }

    @Test
    void testHandleFacilityAccessDeniedException_HttpStatusCode() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("Facility access denied");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert - Verify HTTP 403 FORBIDDEN status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void testHandleValidationException_BackwardCompatibility() {
        // Arrange
        FieldError fieldError1 = new FieldError("user", "email", "must not be blank");
        FieldError fieldError2 = new FieldError("user", "name", "size must be between 2 and 50");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(methodArgumentNotValidException, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getBody().getFieldErrors()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
    }

    @Test
    void testHandleInvalidCredentialsException_BackwardCompatibility() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentialsException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid username or password");
    }

    @Test
    void testHandleInactiveAccountException_BackwardCompatibility() {
        // Arrange
        InactiveAccountException exception = new InactiveAccountException(123L, "Account is inactive");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInactiveAccountException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCOUNT_INACTIVE");
        assertThat(response.getBody().getMessage()).isEqualTo("Account is inactive");
    }

    @Test
    void testHandleForbiddenAccessException_BackwardCompatibility() {
        // Arrange
        ForbiddenAccessException exception = new ForbiddenAccessException(123L, 456L, "Resource", "No access");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenAccessException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FORBIDDEN_ACCESS");
    }

    @Test
    void testHandleResourceNotFoundException_BackwardCompatibility() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User", 123L);
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void testHandleIllegalArgumentException_BackwardCompatibility() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void testHandleEntityNotFoundException_BackwardCompatibility() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFoundException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void testHandleAuthenticationException_BackwardCompatibility() {
        // Arrange
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Authentication failed");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    void testHandleAccessDeniedException_BackwardCompatibility() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void testHandleGenericException_BackwardCompatibility() {
        // Arrange
        Exception exception = new Exception("Unexpected error");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert - Verify existing handler still works
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void testFacilityAccessDeniedException_MessageContent() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("Custom facility message");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert - Verify message is always "No facility access" regardless of exception message
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No facility access");
    }

    @Test
    void testFacilityAccessDeniedException_RemediationMessage() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("Access denied");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert - Verify remediation message
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRemediation()).isEqualTo("Please contact your system administrator if you believe you should have access to this facility");
    }

    @Test
    void testFacilityAccessDeniedException_ErrorCodeValue() {
        // Arrange
        FacilityAccessDeniedException exception = new FacilityAccessDeniedException("Denied");
        when(webRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFacilityAccessDeniedException(exception, webRequest);

        // Assert - Verify error code is exactly "FACILITY_ACCESS_DENIED"
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FACILITY_ACCESS_DENIED");
    }
}