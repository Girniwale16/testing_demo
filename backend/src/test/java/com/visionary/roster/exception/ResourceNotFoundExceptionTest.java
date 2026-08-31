package com.visionary.roster.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceNotFoundException class.
 * Ensures 100% test coverage for all constructors, methods, and annotations.
 */
class ResourceNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String expectedMessage = "Resource not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(expectedMessage);

        // Assert
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getResourceType());
        assertNull(exception.getResourceId());
    }

    @Test
    void testConstructorWithResourceTypeAndId() {
        // Arrange
        String resourceType = "Employee";
        Long resourceId = 123L;
        String expectedMessage = "Employee with ID 123 not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }

    @Test
    void testConstructorWithResourceTypeAndIdFormatsMessageCorrectly() {
        // Arrange
        String resourceType = "Department";
        Long resourceId = 456L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertTrue(exception.getMessage().contains("Department"));
        assertTrue(exception.getMessage().contains("456"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testGetResourceType() {
        // Arrange
        String resourceType = "Project";
        Long resourceId = 789L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertEquals(resourceType, exception.getResourceType());
    }

    @Test
    void testGetResourceId() {
        // Arrange
        String resourceType = "Task";
        Long resourceId = 999L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertEquals(resourceId, exception.getResourceId());
    }

    @Test
    void testExceptionExtendsRuntimeException() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException("Test message");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testResponseStatusAnnotation() {
        // Arrange & Act
        ResponseStatus annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void testConstructorWithNullMessage() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException((String) null);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getResourceType());
        assertNull(exception.getResourceId());
    }

    @Test
    void testConstructorWithNullResourceType() {
        // Arrange
        String resourceType = null;
        Long resourceId = 100L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertEquals("null with ID 100 not found", exception.getMessage());
        assertNull(exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }

    @Test
    void testConstructorWithNullResourceId() {
        // Arrange
        String resourceType = "User";
        Long resourceId = null;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertEquals("User with ID null not found", exception.getMessage());
        assertEquals(resourceType, exception.getResourceType());
        assertNull(exception.getResourceId());
    }

    @Test
    void testConstructorWithEmptyResourceType() {
        // Arrange
        String resourceType = "";
        Long resourceId = 200L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Assert
        assertEquals(" with ID 200 not found", exception.getMessage());
        assertEquals("", exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }

    @Test
    void testExceptionCanBeThrown() {
        // Arrange
        String message = "Test exception";

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException(message);
        });
    }

    @Test
    void testExceptionCanBeCaught() {
        // Arrange
        String resourceType = "Order";
        Long resourceId = 300L;

        // Act
        try {
            throw new ResourceNotFoundException(resourceType, resourceId);
        } catch (ResourceNotFoundException e) {
            // Assert
            assertEquals("Order with ID 300 not found", e.getMessage());
            assertEquals(resourceType, e.getResourceType());
            assertEquals(resourceId, e.getResourceId());
        }
    }
}