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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        logger.warn("Validation exception occurred. CorrelationId: {}, Errors: {}", correlationId, details);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .details(details)
                .correlationId(correlationId)
                .fieldErrors(fieldErrors)
                .remediation("Please check the request body and ensure all required fields are valid")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Invalid credentials exception. CorrelationId: {}", correlationId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("INVALID_CREDENTIALS")
                .message("Invalid username or password")
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("Please verify your username and password and try again")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponse> handleInactiveAccountException(InactiveAccountException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Inactive account exception. CorrelationId: {}, UserId: {}", correlationId, ex.getUserId());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode("ACCOUNT_INACTIVE")
                .message(ex.getMessage())
                .details("User account with ID " + ex.getUserId() + " is inactive")
                .correlationId(correlationId)
                .remediation("Please contact your administrator to activate your account")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccessException(ForbiddenAccessException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Forbidden access exception. CorrelationId: {}, UserId: {}, FacilityId: {}", 
                correlationId, ex.getUserId(), ex.getFacilityId());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode("FORBIDDEN_ACCESS")
                .message(ex.getMessage())
                .details("User " + ex.getUserId() + " does not have access to facility " + ex.getFacilityId())
                .correlationId(correlationId)
                .remediation("Please contact your administrator to request access to this facility")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        String resourceType = ex.getResourceType() != null ? ex.getResourceType() : "Resource";
        String resourceId = ex.getResourceId() != null ? ex.getResourceId() : "unknown";
        String message = resourceType + " with ID " + resourceId + " not found";

        logger.warn("Resource not found exception. CorrelationId: {}, ResourceType: {}, ResourceId: {}", 
                correlationId, resourceType, resourceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("RESOURCE_NOT_FOUND")
                .message(message)
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("Please verify the resource ID and try again")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Illegal argument exception. CorrelationId: {}", correlationId, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("BAD_REQUEST")
                .message(ex.getMessage())
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("Please check your request parameters and try again")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Entity not found exception. CorrelationId: {}", correlationId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("NOT_FOUND")
                .message(ex.getMessage())
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("Please verify the entity ID and try again")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Authentication exception. CorrelationId: {}", correlationId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("AUTHENTICATION_FAILED")
                .message("Authentication required")
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("Please provide valid authentication credentials")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.warn("Access denied exception. CorrelationId: {}", correlationId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode("ACCESS_DENIED")
                .message(ex.getMessage())
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("You do not have permission to access this resource")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        logger.error("Unexpected exception occurred. CorrelationId: {}", correlationId, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details(ex.getMessage())
                .correlationId(correlationId)
                .remediation("Please contact support if the problem persists")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getOrGenerateCorrelationId() {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }
        return correlationId;
    }
}