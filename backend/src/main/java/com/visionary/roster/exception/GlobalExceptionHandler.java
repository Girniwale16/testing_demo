package com.visionary.roster.exception;

import com.visionary.roster.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Visionary Roster application.
 * Provides centralized exception handling across all @RequestMapping methods.
 * All handlers return ErrorResponse DTO with consistent structure for frontend consumption.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger("com.visionary.roster.security");

    /**
     * Handles validation exceptions for request body validation failures.
     * Returns 422 UNPROCESSABLE_ENTITY with field-level error details.
     *
     * @param ex the validation exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse containing validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        logger.warn("Validation error - correlationId: {}, path: {}, details: {}", correlationId, path, details);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .details(details)
                .remediation("Please check the request body and ensure all required fields are provided with valid values")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .path(path)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * Handles invalid credentials exception during authentication.
     * Returns 401 UNAUTHORIZED with message suitable for frontend login redirect.
     *
     * @param ex the invalid credentials exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for authentication failure
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Invalid credentials - correlationId: {}, path: {}, message: {}", correlationId, path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("INVALID_CREDENTIALS")
                .message("Invalid username or password")
                .remediation("Please verify your credentials and try again")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles inactive account exception when user account is disabled.
     * Returns 403 FORBIDDEN with clear message for frontend display.
     *
     * @param ex the inactive account exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for inactive account
     */
    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponse> handleInactiveAccountException(InactiveAccountException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Inactive account access attempt - correlationId: {}, path: {}, userId: {}, message: {}", 
                   correlationId, path, ex.getUserId(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("ACCOUNT_INACTIVE")
                .message("You are not authorized to access this resource - account is inactive")
                .remediation("Please contact your system administrator to activate your account")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles forbidden access exception for authorization failures.
     * Returns 403 FORBIDDEN with clear 'not authorized' message for frontend display.
     *
     * @param ex the forbidden access exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for forbidden access
     */
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccessException(ForbiddenAccessException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Forbidden access - correlationId: {}, path: {}, userId: {}, facilityId: {}, resource: {}, reason: {}",
                correlationId, path, ex.getUserId(), ex.getFacilityId(), ex.getResource(), ex.getReason());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("FORBIDDEN_ACCESS")
                .message("You are not authorized to access this resource")
                .details(ex.getMessage())
                .remediation("Please contact your system administrator if you believe you should have access to this resource")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles resource not found exception.
     * Returns 404 NOT_FOUND with resource details.
     *
     * @param ex the resource not found exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for not found resource
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        String resourceType = ex.getResourceType() != null ? ex.getResourceType() : "Resource";
        String resourceId = ex.getResourceId() != null ? ex.getResourceId() : "unknown";
        String message = String.format("%s with ID %s not found", resourceType, resourceId);

        logger.warn("Resource not found - correlationId: {}, path: {}, resourceType: {}, resourceId: {}", 
                   correlationId, path, resourceType, resourceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("RESOURCE_NOT_FOUND")
                .message(message)
                .details(ex.getMessage())
                .remediation("Please verify the resource identifier and try again")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions for invalid request parameters.
     * Returns 400 BAD_REQUEST with error details.
     *
     * @param ex the illegal argument exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for bad request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Illegal argument - correlationId: {}, path: {}, message: {}", correlationId, path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("BAD_REQUEST")
                .message(ex.getMessage())
                .remediation("Please check your request parameters and try again")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles JPA entity not found exceptions.
     * Returns 404 NOT_FOUND with error details.
     *
     * @param ex the entity not found exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for not found entity
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Entity not found - correlationId: {}, path: {}, message: {}", correlationId, path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("NOT_FOUND")
                .message(ex.getMessage())
                .remediation("The requested resource could not be found")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles Spring Security authentication exceptions.
     * Returns 401 UNAUTHORIZED with message suitable for frontend login redirect.
     *
     * @param ex the authentication exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for authentication failure
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Authentication failed - correlationId: {}, path: {}, message: {}", correlationId, path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("AUTHENTICATION_FAILED")
                .message("Authentication required - please log in to continue")
                .remediation("Please log in to access this resource")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles Spring Security access denied exceptions.
     * Returns 403 FORBIDDEN with clear 'not authorized' message for frontend display.
     *
     * @param ex the access denied exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for access denial
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.warn("Access denied - correlationId: {}, path: {}, message: {}", correlationId, path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("ACCESS_DENIED")
                .message("You are not authorized to access this resource")
                .remediation("You do not have permission to access this resource. Please contact your administrator if you believe this is an error")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles all uncaught exceptions as internal server errors.
     * Returns 500 INTERNAL_SERVER_ERROR with actionable message for frontend retry banner.
     *
     * @param ex the generic exception
     * @param request the web request
     * @return ResponseEntity with ErrorResponse for internal server error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String correlationId = getOrGenerateCorrelationId();
        String path = request.getDescription(false).replace("uri=", "");

        logger.error("Unexpected error - correlationId: {}, path: {}, exception: {}, message: {}", 
                    correlationId, path, ex.getClass().getName(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred while processing your request")
                .remediation("Please try again in a few moments. If the problem persists, contact support with correlation ID: " + correlationId)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Retrieves or generates a correlation ID for request tracing.
     * Uses MDC (Mapped Diagnostic Context) for thread-safe correlation ID management.
     *
     * @return the correlation ID
     */
    private String getOrGenerateCorrelationId() {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }
        return correlationId;
    }
}