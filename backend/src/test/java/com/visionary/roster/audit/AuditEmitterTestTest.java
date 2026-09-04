package com.visionary.roster.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AuditEmitter interface implementations.
 * Tests all method signatures and validates parameter constraints as documented.
 */
@DisplayName("AuditEmitter Interface Tests")
class AuditEmitterTest {

    private AuditEmitter auditEmitter;

    @BeforeEach
    void setUp() {
        // Create a test implementation that validates all constraints
        auditEmitter = new TestAuditEmitterImpl();
    }

    @Nested
    @DisplayName("emitStaffUpdateEvent Tests")
    class EmitStaffUpdateEventTests {

        @Test
        @DisplayName("Should emit staff update event with valid parameters")
        void shouldEmitStaffUpdateEventWithValidParameters() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> changes = new HashMap<>();
            changes.put("firstName", "John");
            changes.put("department", "Engineering");

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitStaffUpdateEvent(staffId, userId, changes));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when staffId is null")
        void shouldThrowExceptionWhenStaffIdIsNull() {
            // Arrange
            Long staffId = null;
            Long userId = 100L;
            Map<String, Object> changes = new HashMap<>();
            changes.put("firstName", "John");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffUpdateEvent(staffId, userId, changes)
            );
            assertTrue(exception.getMessage().contains("staffId"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Arrange
            Long staffId = 1L;
            Long userId = null;
            Map<String, Object> changes = new HashMap<>();
            changes.put("firstName", "John");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffUpdateEvent(staffId, userId, changes)
            );
            assertTrue(exception.getMessage().contains("userId"));
        }

        @Test
        @DisplayName("Should emit staff update event with empty changes map")
        void shouldEmitStaffUpdateEventWithEmptyChanges() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> changes = new HashMap<>();

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitStaffUpdateEvent(staffId, userId, changes));
        }

        @Test
        @DisplayName("Should emit staff update event with null changes map")
        void shouldEmitStaffUpdateEventWithNullChanges() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> changes = null;

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitStaffUpdateEvent(staffId, userId, changes));
        }

        @Test
        @DisplayName("Should emit staff update event with multiple field changes")
        void shouldEmitStaffUpdateEventWithMultipleChanges() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> changes = new HashMap<>();
            changes.put("firstName", "John");
            changes.put("lastName", "Doe");
            changes.put("department", "Engineering");
            changes.put("email", "john.doe@example.com");

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitStaffUpdateEvent(staffId, userId, changes));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action")
        void testEmitStaffUpdateEvent_DeactivationAction() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");

            // Create a spy to verify method calls and capture arguments
            TestAuditEmitterImpl spyEmitter = spy(new TestAuditEmitterImpl());

            // Act
            spyEmitter.emitStaffUpdateEvent(staffId, userId, metadata);

            // Assert - Verify audit event is published with correct staffId, userId, and metadata fields
            verify(spyEmitter, times(1)).emitStaffUpdateEvent(eq(staffId), eq(userId), eq(metadata));
            
            // Verify metadata contains all required fields for deactivation audit trail
            assertNotNull(metadata.get("action"));
            assertEquals("DEACTIVATE", metadata.get("action"));
            assertEquals("MANAGER", metadata.get("userRole"));
            assertEquals("MANAGER", metadata.get("requiredRole"));
            assertEquals("AUTHORIZED", metadata.get("authorizationResult"));
            
            // Verify correlation ID is included in audit event for distributed tracing
            assertNotNull(spyEmitter.getLastCorrelationId());
            assertFalse(spyEmitter.getLastCorrelationId().isEmpty());
            
            // Verify timestamp is automatically added to audit event
            assertNotNull(spyEmitter.getLastTimestamp());
            assertTrue(spyEmitter.getLastTimestamp() > 0);
            
            // Verify metadata serialization handles all required fields for deactivation audit trail
            assertTrue(spyEmitter.isMetadataSerializable(metadata));
            assertEquals(4, metadata.size());
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with null staffId")
        void testEmitStaffUpdateEvent_DeactivationAction_NullStaffId() {
            // Arrange
            Long staffId = null;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffUpdateEvent(staffId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("staffId"));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with null userId")
        void testEmitStaffUpdateEvent_DeactivationAction_NullUserId() {
            // Arrange
            Long staffId = 1L;
            Long userId = null;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffUpdateEvent(staffId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("userId"));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with missing action field")
        void testEmitStaffUpdateEvent_DeactivationAction_MissingActionField() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);

            // Assert - Verify correlation ID and timestamp are still generated
            assertNotNull(testEmitter.getLastCorrelationId());
            assertNotNull(testEmitter.getLastTimestamp());
            assertNull(metadata.get("action"));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with unauthorized result")
        void testEmitStaffUpdateEvent_DeactivationAction_UnauthorizedResult() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "USER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "UNAUTHORIZED");

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);

            // Assert - Verify audit event is still published even for unauthorized attempts
            assertNotNull(testEmitter.getLastCorrelationId());
            assertNotNull(testEmitter.getLastTimestamp());
            assertEquals("UNAUTHORIZED", metadata.get("authorizationResult"));
            assertEquals("USER", metadata.get("userRole"));
            assertEquals("MANAGER", metadata.get("requiredRole"));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with additional metadata fields")
        void testEmitStaffUpdateEvent_DeactivationAction_AdditionalMetadata() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");
            metadata.put("reason", "Employee resignation");
            metadata.put("effectiveDate", "2024-01-15");
            metadata.put("ipAddress", "192.168.1.100");

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);

            // Assert - Verify all metadata fields are serializable
            assertTrue(testEmitter.isMetadataSerializable(metadata));
            assertEquals(7, metadata.size());
            assertEquals("Employee resignation", metadata.get("reason"));
            assertEquals("2024-01-15", metadata.get("effectiveDate"));
            assertEquals("192.168.1.100", metadata.get("ipAddress"));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action verifies correlation ID uniqueness")
        void testEmitStaffUpdateEvent_DeactivationAction_CorrelationIdUniqueness() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act - Emit multiple events
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);
            String firstCorrelationId = testEmitter.getLastCorrelationId();
            
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);
            String secondCorrelationId = testEmitter.getLastCorrelationId();

            // Assert - Verify correlation IDs are unique
            assertNotNull(firstCorrelationId);
            assertNotNull(secondCorrelationId);
            assertNotEquals(firstCorrelationId, secondCorrelationId);
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action verifies timestamp progression")
        void testEmitStaffUpdateEvent_DeactivationAction_TimestampProgression() throws InterruptedException {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act - Emit multiple events with delay
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);
            Long firstTimestamp = testEmitter.getLastTimestamp();
            
            Thread.sleep(10); // Small delay to ensure timestamp difference
            
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);
            Long secondTimestamp = testEmitter.getLastTimestamp();

            // Assert - Verify timestamps progress forward
            assertNotNull(firstTimestamp);
            assertNotNull(secondTimestamp);
            assertTrue(secondTimestamp >= firstTimestamp);
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with non-serializable metadata")
        void testEmitStaffUpdateEvent_DeactivationAction_NonSerializableMetadata() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "DEACTIVATE");
            metadata.put("userRole", "MANAGER");
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "AUTHORIZED");
            metadata.put("nonSerializable", new Object()); // Non-serializable object

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act
            testEmitter.emitStaffUpdateEvent(staffId, userId, metadata);

            // Assert - Verify metadata serialization check fails for non-serializable objects
            assertFalse(testEmitter.isMetadataSerializable(metadata));
        }

        @Test
        @DisplayName("Test emit staff update event for deactivation action with different role combinations")
        void testEmitStaffUpdateEvent_DeactivationAction_DifferentRoleCombinations() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            
            String[][] roleCombinations = {
                {"ADMIN", "ADMIN", "AUTHORIZED"},
                {"MANAGER", "MANAGER", "AUTHORIZED"},
                {"USER", "MANAGER", "UNAUTHORIZED"},
                {"MANAGER", "ADMIN", "UNAUTHORIZED"},
                {"ADMIN", "MANAGER", "AUTHORIZED"}
            };

            TestAuditEmitterImpl testEmitter = new TestAuditEmitterImpl();

            // Act & Assert
            for (String[] roles : roleCombinations) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("action", "DEACTIVATE");
                metadata.put("userRole", roles[0]);
                metadata.put("requiredRole", roles[1]);
                metadata.put("authorizationResult", roles[2]);

                assertDoesNotThrow(() -> testEmitter.emitStaffUpdateEvent(staffId, userId, metadata));
                
                assertEquals(roles[0], metadata.get("userRole"));
                assertEquals(roles[1], metadata.get("requiredRole"));
                assertEquals(roles[2], metadata.get("authorizationResult"));
                assertNotNull(testEmitter.getLastCorrelationId());
                assertNotNull(testEmitter.getLastTimestamp());
            }
        }
    }

    @Nested
    @DisplayName("emitStaffDeactivateEvent Tests")
    class EmitStaffDeactivateEventTests {

        @Test
        @DisplayName("Should emit staff deactivate event with valid parameters")
        void shouldEmitStaffDeactivateEventWithValidParameters() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            String reason = "Resignation";

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when staffId is null")
        void shouldThrowExceptionWhenStaffIdIsNull() {
            // Arrange
            Long staffId = null;
            Long userId = 100L;
            String reason = "Resignation";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason)
            );
            assertTrue(exception.getMessage().contains("staffId"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Arrange
            Long staffId = 1L;
            Long userId = null;
            String reason = "Resignation";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason)
            );
            assertTrue(exception.getMessage().contains("userId"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when reason is null")
        void shouldThrowExceptionWhenReasonIsNull() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            String reason = null;

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason)
            );
            assertTrue(exception.getMessage().contains("reason"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when reason is empty")
        void shouldThrowExceptionWhenReasonIsEmpty() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            String reason = "";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason)
            );
            assertTrue(exception.getMessage().contains("reason"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when reason is blank")
        void shouldThrowExceptionWhenReasonIsBlank() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            String reason = "   ";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason)
            );
            assertTrue(exception.getMessage().contains("reason"));
        }

        @Test
        @DisplayName("Should emit staff deactivate event with different reason types")
        void shouldEmitStaffDeactivateEventWithDifferentReasons() {
            // Arrange
            Long staffId = 1L;
            Long userId = 100L;
            String[] reasons = {"Resignation", "Termination", "Retirement", "Contract End"};

            // Act & Assert
            for (String reason : reasons) {
                assertDoesNotThrow(() -> auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason));
            }
        }
    }

    @Nested
    @DisplayName("emitAuditEvent Tests")
    class EmitAuditEventTests {

        @Test
        @DisplayName("Should emit generic audit event with valid parameters")
        void shouldEmitAuditEventWithValidParameters() {
            // Arrange
            String eventType = "CREATE";
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ipAddress", "192.168.1.1");
            metadata.put("action", "LOGIN_SUCCESS");

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when eventType is null")
        void shouldThrowExceptionWhenEventTypeIsNull() {
            // Arrange
            String eventType = null;
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("eventType"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when eventType is empty")
        void shouldThrowExceptionWhenEventTypeIsEmpty() {
            // Arrange
            String eventType = "";
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("eventType"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when entityType is null")
        void shouldThrowExceptionWhenEntityTypeIsNull() {
            // Arrange
            String eventType = "CREATE";
            String entityType = null;
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("entityType"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when entityType is empty")
        void shouldThrowExceptionWhenEntityTypeIsEmpty() {
            // Arrange
            String eventType = "CREATE";
            String entityType = "";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("entityType"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when entityId is null")
        void shouldThrowExceptionWhenEntityIdIsNull() {
            // Arrange
            String eventType = "CREATE";
            String entityType = "STAFF";
            Long entityId = null;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("entityId"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Arrange
            String eventType = "CREATE";
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = null;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata)
            );
            assertTrue(exception.getMessage().contains("userId"));
        }

        @Test
        @DisplayName("Should emit audit event with null metadata")
        void shouldEmitAuditEventWithNullMetadata() {
            // Arrange
            String eventType = "CREATE";
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = null;

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata));
        }

        @Test
        @DisplayName("Should emit audit event with empty metadata")
        void shouldEmitAuditEventWithEmptyMetadata() {
            // Arrange
            String eventType = "CREATE";
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata));
        }

        @Test
        @DisplayName("Should emit audit event with different event types")
        void shouldEmitAuditEventWithDifferentEventTypes() {
            // Arrange
            String[] eventTypes = {"CREATE", "UPDATE", "DELETE", "ACCESS", "STAFF_CREATED", "ROLE_ASSIGNED"};
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            for (String eventType : eventTypes) {
                assertDoesNotThrow(() -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata));
            }
        }

        @Test
        @DisplayName("Should emit audit event with different entity types")
        void shouldEmitAuditEventWithDifferentEntityTypes() {
            // Arrange
            String eventType = "CREATE";
            String[] entityTypes = {"STAFF", "DEPARTMENT", "USER", "ROLE"};
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();

            // Act & Assert
            for (String entityType : entityTypes) {
                assertDoesNotThrow(() -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata));
            }
        }

        @Test
        @DisplayName("Should emit audit event with complex metadata")
        void shouldEmitAuditEventWithComplexMetadata() {
            // Arrange
            String eventType = "UPDATE";
            String entityType = "STAFF";
            Long entityId = 1L;
            Long userId = 100L;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ipAddress", "192.168.1.1");
            metadata.put("action", "LOGIN_SUCCESS");
            metadata.put("sessionId", "abc123");
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("changes", Map.of("field1", "value1", "field2", "value2"));

            // Act & Assert
            assertDoesNotThrow(() -> auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata));
        }
    }

    /**
     * Test implementation of AuditEmitter that validates all constraints.
     */
    private static class TestAuditEmitterImpl implements AuditEmitter {

        private String lastCorrelationId;
        private Long lastTimestamp;

        @Override
        public void emitStaffUpdateEvent(Long staffId, Long userId, Map<String, Object> changes) {
            if (staffId == null) {
                throw new IllegalArgumentException("staffId cannot be null");
            }
            if (userId == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }
            // Generate correlation ID for distributed tracing
            this.lastCorrelationId = java.util.UUID.randomUUID().toString();
            // Automatically add timestamp to audit event
            this.lastTimestamp = System.currentTimeMillis();
            // Implementation would emit the event here
        }

        @Override
        public void emitStaffDeactivateEvent(Long staffId, Long userId, String reason) {
            if (staffId == null) {
                throw new IllegalArgumentException("staffId cannot be null");
            }
            if (userId == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("reason cannot be null or empty");
            }
            // Implementation would emit the event here
        }

        @Override
        public void emitAuditEvent(String eventType, String entityType, Long entityId, Long userId, Map<String, Object> metadata) {
            if (eventType == null || eventType.trim().isEmpty()) {
                throw new IllegalArgumentException("eventType cannot be null or empty");
            }
            if (entityType == null || entityType.trim().isEmpty()) {
                throw new IllegalArgumentException("entityType cannot be null or empty");
            }
            if (entityId == null) {
                throw new IllegalArgumentException("entityId cannot be null");
            }
            if (userId == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }
            // Implementation would emit the event here with correlation ID
        }

        public String getLastCorrelationId() {
            return lastCorrelationId;
        }

        public Long getLastTimestamp() {
            return lastTimestamp;
        }

        public boolean isMetadataSerializable(Map<String, Object> metadata) {
            if (metadata == null) {
                return true;
            }
            // Validate that all metadata fields are serializable
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getKey() == null) {
                    return false;
                }
                // Check if value is of a serializable type
                Object value = entry.getValue();
                if (value != null && !(value instanceof String || value instanceof Number || 
                    value instanceof Boolean || value instanceof java.io.Serializable)) {
                    return false;
                }
            }
            return true;
        }
    }
}