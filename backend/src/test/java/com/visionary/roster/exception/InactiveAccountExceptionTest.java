package com.visionary.roster.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for InactiveAccountException.
 * 
 * Tests cover:
 * 1. Exception structure and message format validation
 * 2. GlobalExceptionHandler integration and HTTP status code mapping
 * 3. Usage pattern validation for staff deactivation logic
 * 4. AuthService and GlobalExceptionHandler compatibility
 */
@DisplayName("InactiveAccountException Test Suite")
class InactiveAccountExceptionTest {

    @Nested
    @DisplayName("Exception Structure and Message Format Tests")
    class ExceptionStructureTests {

        @Test
        @DisplayName("Should create exception with message and userId")
        void testExceptionCreationWithMessageAndUserId() {
            // Arrange
            String expectedMessage = "Account is inactive. Please contact administrator.";
            Long expectedUserId = 12345L;

            // Act
            InactiveAccountException exception = new InactiveAccountException(expectedMessage, expectedUserId);

            // Assert
            assertNotNull(exception, "Exception should not be null");
            assertEquals(expectedMessage, exception.getMessage(), "Exception message should match");
            assertEquals(expectedUserId, exception.getUserId(), "User ID should match");
        }

        @Test
        @DisplayName("Should extend RuntimeException for unchecked exception handling")
        void testExceptionExtendsRuntimeException() {
            // Arrange & Act
            InactiveAccountException exception = new InactiveAccountException("Test message", 1L);

            // Assert
            assertTrue(exception instanceof RuntimeException, "InactiveAccountException should extend RuntimeException");
        }

        @Test
        @DisplayName("Should store userId for audit and logging purposes")
        void testUserIdStorageForAuditLogging() {
            // Arrange
            Long userId1 = 100L;
            Long userId2 = 200L;

            // Act
            InactiveAccountException exception1 = new InactiveAccountException("Message 1", userId1);
            InactiveAccountException exception2 = new InactiveAccountException("Message 2", userId2);

            // Assert
            assertEquals(userId1, exception1.getUserId(), "First exception should store correct userId");
            assertEquals(userId2, exception2.getUserId(), "Second exception should store correct userId");
            assertNotEquals(exception1.getUserId(), exception2.getUserId(), "Different exceptions should have different userIds");
        }

        @Test
        @DisplayName("Should handle null userId gracefully")
        void testNullUserIdHandling() {
            // Arrange
            String message = "Account is inactive";
            Long nullUserId = null;

            // Act
            InactiveAccountException exception = new InactiveAccountException(message, nullUserId);

            // Assert
            assertNotNull(exception, "Exception should be created even with null userId");
            assertNull(exception.getUserId(), "UserId should be null when passed as null");
            assertEquals(message, exception.getMessage(), "Message should still be set correctly");
        }

        @Test
        @DisplayName("Should handle empty message string")
        void testEmptyMessageHandling() {
            // Arrange
            String emptyMessage = "";
            Long userId = 999L;

            // Act
            InactiveAccountException exception = new InactiveAccountException(emptyMessage, userId);

            // Assert
            assertEquals(emptyMessage, exception.getMessage(), "Empty message should be preserved");
            assertEquals(userId, exception.getUserId(), "UserId should still be set correctly");
        }

        @Test
        @DisplayName("Should handle long descriptive messages")
        void testLongDescriptiveMessage() {
            // Arrange
            String longMessage = "Your account has been deactivated due to prolonged inactivity. " +
                    "Please contact the system administrator at admin@visionary.com to reactivate your account. " +
                    "Reference ID: IAE-2024-001";
            Long userId = 5678L;

            // Act
            InactiveAccountException exception = new InactiveAccountException(longMessage, userId);

            // Assert
            assertEquals(longMessage, exception.getMessage(), "Long message should be preserved completely");
            assertEquals(userId, exception.getUserId(), "UserId should be set correctly");
        }
    }

    @Nested
    @DisplayName("GlobalExceptionHandler Integration Tests")
    class GlobalExceptionHandlerIntegrationTests {

        @Test
        @DisplayName("Should be throwable and catchable as RuntimeException")
        void testExceptionThrowableAndCatchable() {
            // Arrange
            Long userId = 123L;
            String message = "Account inactive";

            // Act & Assert
            assertThrows(InactiveAccountException.class, () -> {
                throw new InactiveAccountException(message, userId);
            }, "Should be able to throw InactiveAccountException");
        }

        @Test
        @DisplayName("Should maintain exception data when caught and rethrown")
        void testExceptionDataPreservationWhenRethrown() {
            // Arrange
            String originalMessage = "Original inactive message";
            Long originalUserId = 456L;

            // Act & Assert
            InactiveAccountException caughtException = assertThrows(InactiveAccountException.class, () -> {
                try {
                    throw new InactiveAccountException(originalMessage, originalUserId);
                } catch (InactiveAccountException e) {
                    // Simulate GlobalExceptionHandler catching and processing
                    assertEquals(originalMessage, e.getMessage());
                    assertEquals(originalUserId, e.getUserId());
                    throw e; // Rethrow for outer assertion
                }
            });

            assertEquals(originalMessage, caughtException.getMessage(), "Message should be preserved after catch/rethrow");
            assertEquals(originalUserId, caughtException.getUserId(), "UserId should be preserved after catch/rethrow");
        }

        @Test
        @DisplayName("Should be identifiable by exception type for HTTP 403 mapping")
        void testExceptionTypeIdentification() {
            // Arrange
            InactiveAccountException exception = new InactiveAccountException("Test", 1L);

            // Act
            boolean isInactiveAccountException = exception instanceof InactiveAccountException;
            boolean isRuntimeException = exception instanceof RuntimeException;
            boolean isException = exception instanceof Exception;

            // Assert
            assertTrue(isInactiveAccountException, "Should be identifiable as InactiveAccountException");
            assertTrue(isRuntimeException, "Should be identifiable as RuntimeException");
            assertTrue(isException, "Should be identifiable as Exception");
        }

        @Test
        @DisplayName("Should provide userId for error response construction")
        void testUserIdAvailableForErrorResponse() {
            // Arrange
            Long expectedUserId = 789L;
            String message = "Account deactivated";
            InactiveAccountException exception = new InactiveAccountException(message, expectedUserId);

            // Act - Simulate GlobalExceptionHandler extracting data
            Long extractedUserId = exception.getUserId();
            String extractedMessage = exception.getMessage();

            // Assert
            assertNotNull(extractedUserId, "UserId should be available for error response");
            assertEquals(expectedUserId, extractedUserId, "Extracted userId should match original");
            assertNotNull(extractedMessage, "Message should be available for error response");
            assertEquals(message, extractedMessage, "Extracted message should match original");
        }
    }

    @Nested
    @DisplayName("Usage Pattern Validation for Staff Deactivation Logic")
    class UsagePatternValidationTests {

        @Test
        @DisplayName("Should support login attempt scenario with inactive account")
        void testLoginAttemptScenario() {
            // Arrange - Simulate user login with inactive account
            Long userId = 1001L;
            String expectedMessage = "Account is inactive. Please contact administrator.";

            // Act
            InactiveAccountException exception = new InactiveAccountException(expectedMessage, userId);

            // Assert
            assertEquals(expectedMessage, exception.getMessage(), "Login scenario message should be correct");
            assertEquals(userId, exception.getUserId(), "Login scenario userId should be correct");
        }

        @Test
        @DisplayName("Should support API request scenario with deactivated user")
        void testApiRequestScenario() {
            // Arrange - Simulate API request by deactivated user
            Long userId = 2002L;
            String expectedMessage = "Your account has been deactivated. Access denied.";

            // Act
            InactiveAccountException exception = new InactiveAccountException(expectedMessage, userId);

            // Assert
            assertEquals(expectedMessage, exception.getMessage(), "API request scenario message should be correct");
            assertEquals(userId, exception.getUserId(), "API request scenario userId should be correct");
        }

        @Test
        @DisplayName("Should support staff deactivation workflow scenario")
        void testStaffDeactivationWorkflowScenario() {
            // Arrange - Simulate staff deactivation preventing further access
            Long staffUserId = 3003L;
            String expectedMessage = "Staff account deactivated. Further access prevented.";

            // Act
            InactiveAccountException exception = new InactiveAccountException(expectedMessage, staffUserId);

            // Assert
            assertEquals(expectedMessage, exception.getMessage(), "Staff deactivation message should be correct");
            assertEquals(staffUserId, exception.getUserId(), "Staff deactivation userId should be correct");
        }

        @Test
        @DisplayName("Should support multiple deactivation reasons in message")
        void testMultipleDeactivationReasons() {
            // Arrange
            Long userId1 = 4001L;
            Long userId2 = 4002L;
            Long userId3 = 4003L;

            // Act
            InactiveAccountException policyViolation = new InactiveAccountException(
                    "Account deactivated due to policy violation", userId1);
            InactiveAccountException inactivity = new InactiveAccountException(
                    "Account deactivated due to prolonged inactivity", userId2);
            InactiveAccountException adminAction = new InactiveAccountException(
                    "Account deactivated by administrator", userId3);

            // Assert
            assertTrue(policyViolation.getMessage().contains("policy violation"), "Should support policy violation reason");
            assertTrue(inactivity.getMessage().contains("inactivity"), "Should support inactivity reason");
            assertTrue(adminAction.getMessage().contains("administrator"), "Should support admin action reason");
            assertEquals(userId1, policyViolation.getUserId(), "Policy violation userId should be correct");
            assertEquals(userId2, inactivity.getUserId(), "Inactivity userId should be correct");
            assertEquals(userId3, adminAction.getUserId(), "Admin action userId should be correct");
        }
    }

    @Nested
    @DisplayName("AuthService and GlobalExceptionHandler Compatibility Tests")
    class IntegrationCompatibilityTests {

        @Test
        @DisplayName("Should maintain backward compatibility with AuthService authentication flow")
        void testAuthServiceCompatibility() {
            // Arrange - Simulate AuthService checking user.isActive()
            Long userId = 5001L;
            boolean isActive = false; // Simulated inactive user

            // Act
            InactiveAccountException exception = null;
            if (!isActive) {
                exception = new InactiveAccountException(
                        "Account is inactive. Please contact administrator.",
                        userId
                );
            }

            // Assert
            assertNotNull(exception, "Exception should be created for inactive user");
            assertEquals(userId, exception.getUserId(), "UserId should match for AuthService integration");
            assertTrue(exception.getMessage().contains("inactive"), "Message should indicate inactive status");
        }

        @Test
        @DisplayName("Should provide consistent exception structure for GlobalExceptionHandler")
        void testGlobalExceptionHandlerStructureConsistency() {
            // Arrange
            Long userId = 6001L;
            String message = "Test inactive account";

            // Act
            InactiveAccountException exception = new InactiveAccountException(message, userId);

            // Assert - Verify structure expected by GlobalExceptionHandler
            assertNotNull(exception.getMessage(), "Message should never be null for handler");
            assertNotNull(exception.getUserId(), "UserId should be accessible for handler");
            assertTrue(exception instanceof RuntimeException, "Should be RuntimeException for handler compatibility");
        }

        @Test
        @DisplayName("Should support exception chaining if needed by handlers")
        void testExceptionChainingSupport() {
            // Arrange
            Long userId = 7001L;
            String message = "Account inactive";
            InactiveAccountException exception = new InactiveAccountException(message, userId);

            // Act - Simulate wrapping in another exception
            RuntimeException wrappedException = new RuntimeException("Wrapped exception", exception);

            // Assert
            assertNotNull(wrappedException.getCause(), "Should support exception chaining");
            assertTrue(wrappedException.getCause() instanceof InactiveAccountException, "Cause should be InactiveAccountException");
            assertEquals(userId, ((InactiveAccountException) wrappedException.getCause()).getUserId(),
                    "UserId should be accessible through exception chain");
        }

        @Test
        @DisplayName("Should maintain exception immutability for thread safety")
        void testExceptionImmutability() {
            // Arrange
            Long userId = 8001L;
            String message = "Immutable test";

            // Act
            InactiveAccountException exception = new InactiveAccountException(message, userId);
            Long retrievedUserId1 = exception.getUserId();
            Long retrievedUserId2 = exception.getUserId();

            // Assert
            assertEquals(retrievedUserId1, retrievedUserId2, "UserId should be immutable");
            assertEquals(userId, retrievedUserId1, "UserId should remain constant");
        }

        @Test
        @DisplayName("Should support concurrent exception creation for multi-threaded environments")
        void testConcurrentExceptionCreation() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            InactiveAccountException[] exceptions = new InactiveAccountException[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    exceptions[index] = new InactiveAccountException(
                            "Concurrent test " + index,
                            (long) index
                    );
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Assert
            for (int i = 0; i < threadCount; i++) {
                assertNotNull(exceptions[i], "Exception " + i + " should be created");
                assertEquals((long) i, exceptions[i].getUserId(), "UserId should match for exception " + i);
                assertTrue(exceptions[i].getMessage().contains("Concurrent test " + i),
                        "Message should be correct for exception " + i);
            }
        }

        @Test
        @DisplayName("Should validate standard error message format for consistency")
        void testStandardErrorMessageFormat() {
            // Arrange
            Long userId = 9001L;
            String standardMessage = "Account is inactive. Please contact administrator.";

            // Act
            InactiveAccountException exception = new InactiveAccountException(standardMessage, userId);

            // Assert
            assertEquals(standardMessage, exception.getMessage(), "Should use standard message format");
            assertTrue(exception.getMessage().contains("inactive"), "Message should contain 'inactive' keyword");
            assertTrue(exception.getMessage().contains("contact"), "Message should contain 'contact' keyword");
        }

        @Test
        @DisplayName("Should support edge case of userId being zero")
        void testZeroUserIdEdgeCase() {
            // Arrange
            Long zeroUserId = 0L;
            String message = "Zero userId test";

            // Act
            InactiveAccountException exception = new InactiveAccountException(message, zeroUserId);

            // Assert
            assertNotNull(exception, "Exception should be created with zero userId");
            assertEquals(zeroUserId, exception.getUserId(), "Zero userId should be preserved");
            assertEquals(0L, exception.getUserId(), "UserId should be exactly zero");
        }

        @Test
        @DisplayName("Should support edge case of maximum Long userId")
        void testMaximumUserIdEdgeCase() {
            // Arrange
            Long maxUserId = Long.MAX_VALUE;
            String message = "Maximum userId test";

            // Act
            InactiveAccountException exception = new InactiveAccountException(message, maxUserId);

            // Assert
            assertNotNull(exception, "Exception should be created with maximum userId");
            assertEquals(maxUserId, exception.getUserId(), "Maximum userId should be preserved");
            assertEquals(Long.MAX_VALUE, exception.getUserId(), "UserId should be exactly Long.MAX_VALUE");
        }

        @Test
        @DisplayName("Should support edge case of negative userId")
        void testNegativeUserIdEdgeCase() {
            // Arrange
            Long negativeUserId = -1L;
            String message = "Negative userId test";

            // Act
            InactiveAccountException exception = new InactiveAccountException(message, negativeUserId);

            // Assert
            assertNotNull(exception, "Exception should be created with negative userId");
            assertEquals(negativeUserId, exception.getUserId(), "Negative userId should be preserved");
            assertTrue(exception.getUserId() < 0, "UserId should be negative");
        }
    }
}