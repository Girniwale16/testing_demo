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

    private static final Logger logger = LoggerFactory.getLogger("com.visionary.roster.security");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String correlationId = getOrGenerateCorrelationId();
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        logger.warn("Validation error - correlationId: {}, details: {}", correlationId, details);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .details(details)
                .remediation("Please check the request body and ensure all required fields are provided with valid values")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Invalid credentials - correlationId: {}, message: {}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("INVALID_CREDENTIALS")
                .message("Invalid username or password")
                .remediation("Please verify your credentials and try again")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponse> handleInactiveAccountException(InactiveAccountException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Inactive account access attempt - correlationId: {}, userId: {}, message: {}", 
                   correlationId, ex.getUserId(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("ACCOUNT_INACTIVE")
                .message("Account is inactive")
                .remediation("Please contact your system administrator to activate your account")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccessException(ForbiddenAccessException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Forbidden access - correlationId: {}, userId: {}, facilityId: {}, resource: {}, reason: {}",
                correlationId, ex.getUserId(), ex.getFacilityId(), ex.getResource(), ex.getReason());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("FORBIDDEN_ACCESS")
                .message(ex.getMessage())
                .remediation("Please contact your system administrator if you believe you should have access to this resource")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Illegal argument - correlationId: {}, message: {}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("BAD_REQUEST")
                .message(ex.getMessage())
                .remediation("Please check your request parameters and try again")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Entity not found - correlationId: {}, message: {}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("NOT_FOUND")
                .message(ex.getMessage())
                .remediation("The requested resource could not be found")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Authentication failed - correlationId: {}, message: {}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("AUTHENTICATION_FAILED")
                .message("Authentication required")
                .remediation("Please log in to access this resource")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.warn("Access denied - correlationId: {}, message: {}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("ACCESS_DENIED")
                .message("Access denied")
                .remediation("You do not have permission to access this resource")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String correlationId = getOrGenerateCorrelationId();

        logger.error("Unexpected error - correlationId: {}, exception: {}, message: {}", 
                    correlationId, ex.getClass().getName(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .correlationId(correlationId)
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .remediation("Please try again later or contact support with the correlation ID")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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