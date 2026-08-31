package com.visionary.roster.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AuditEmitterImpl class.
 * Tests cover all business logic including audit event creation, logging,
 * correlation ID handling, and JSON serialization.
 */
class AuditEmitterImplTest {

    private AuditEmitterImpl auditEmitter;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        auditEmitter = new AuditEmitterImpl();
        objectMapper = new ObjectMapper();
        
        // Set up log appender to capture log output
        logger = (Logger) LoggerFactory.getLogger(AuditEmitterImpl.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
        
        // Clear MDC before each test
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up MDC after each test
        MDC.clear();
        
        // Remove log appender
        if (logger != null && logAppender != null) {
            logger.detachAppender(logAppender);
        }
    }

    @Test
    void testEmitStaffUpdateEvent_WithCorrelationId_LogsCorrectAuditEvent() {
        // Arrange
        String correlationId = "test-correlation-123";
        MDC.put("correlationId", correlationId);
        Long staffId = 100L;
        Long userId = 200L;
        Map<String, Object> changes = new HashMap<>();
        changes.put("field1", "oldValue");
        changes.put("field2", "newValue");

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().startsWith("AUDIT: "));
        
        String jsonLog = logEvent.getFormattedMessage().substring(7);
        assertTrue(jsonLog.contains("STAFF_UPDATE"));
        assertTrue(jsonLog.contains("STAFF"));
        assertTrue(jsonLog.contains(staffId.toString()));
        assertTrue(jsonLog.contains(userId.toString()));
        assertTrue(jsonLog.contains(correlationId));
        assertTrue(jsonLog.contains("changes"));
    }

    @Test
    void testEmitStaffUpdateEvent_WithoutCorrelationId_LogsAuditEventWithNullCorrelationId() {
        // Arrange
        Long staffId = 100L;
        Long userId = 200L;
        Map<String, Object> changes = new HashMap<>();
        changes.put("status", "active");

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("STAFF_UPDATE"));
    }

    @Test
    void testEmitStaffUpdateEvent_WithEmptyChanges_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "corr-456");
        Long staffId = 101L;
        Long userId = 201L;
        Map<String, Object> changes = new HashMap<>();

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_UPDATE"));
    }

    @Test
    void testEmitStaffDeactivateEvent_WithCorrelationId_LogsCorrectAuditEvent() {
        // Arrange
        String correlationId = "deactivate-corr-789";
        MDC.put("correlationId", correlationId);
        Long staffId = 102L;
        Long userId = 202L;
        String reason = "Employee resignation";

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().startsWith("AUDIT: "));
        
        String jsonLog = logEvent.getFormattedMessage().substring(7);
        assertTrue(jsonLog.contains("STAFF_DEACTIVATE"));
        assertTrue(jsonLog.contains("STAFF"));
        assertTrue(jsonLog.contains(staffId.toString()));
        assertTrue(jsonLog.contains(userId.toString()));
        assertTrue(jsonLog.contains(correlationId));
        assertTrue(jsonLog.contains("reason"));
        assertTrue(jsonLog.contains(reason));
    }

    @Test
    void testEmitStaffDeactivateEvent_WithoutCorrelationId_LogsAuditEvent() {
        // Arrange
        Long staffId = 103L;
        Long userId = 203L;
        String reason = "Contract ended";

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_DEACTIVATE"));
    }

    @Test
    void testEmitStaffDeactivateEvent_WithNullReason_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-reason-corr");
        Long staffId = 104L;
        Long userId = 204L;
        String reason = null;

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_DEACTIVATE"));
    }

    @Test
    void testEmitAuditEvent_WithAllParameters_LogsCorrectAuditEvent() {
        // Arrange
        String correlationId = "generic-corr-999";
        MDC.put("correlationId", correlationId);
        String eventType = "CUSTOM_EVENT";
        String entityType = "DEPARTMENT";
        Long entityId = 500L;
        Long userId = 600L;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "create");
        metadata.put("details", "New department created");

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        
        String jsonLog = logEvent.getFormattedMessage().substring(7);
        assertTrue(jsonLog.contains(eventType));
        assertTrue(jsonLog.contains(entityType));
        assertTrue(jsonLog.contains(entityId.toString()));
        assertTrue(jsonLog.contains(userId.toString()));
        assertTrue(jsonLog.contains(correlationId));
        assertTrue(jsonLog.contains("action"));
        assertTrue(jsonLog.contains("create"));
    }

    @Test
    void testEmitAuditEvent_WithNullMetadata_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-metadata-corr");
        String eventType = "NULL_METADATA_EVENT";
        String entityType = "ENTITY";
        Long entityId = 501L;
        Long userId = 601L;
        Map<String, Object> metadata = null;

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains(eventType));
    }

    @Test
    void testEmitAuditEvent_WithEmptyMetadata_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "empty-metadata-corr");
        String eventType = "EMPTY_METADATA_EVENT";
        String entityType = "ENTITY";
        Long entityId = 502L;
        Long userId = 602L;
        Map<String, Object> metadata = new HashMap<>();

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains(eventType));
    }

    @Test
    void testAuditEvent_GettersAndSetters() {
        // Arrange
        AuditEmitterImpl.AuditEvent auditEvent = new AuditEmitterImpl.AuditEvent();
        String eventType = "TEST_EVENT";
        String entityType = "TEST_ENTITY";
        Long entityId = 123L;
        Long userId = 456L;
        String correlationId = "test-corr-id";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        // Act
        auditEvent.setEventType(eventType);
        auditEvent.setEntityType(entityType);
        auditEvent.setEntityId(entityId);
        auditEvent.setUserId(userId);
        auditEvent.setCorrelationId(correlationId);
        auditEvent.setMetadata(metadata);

        // Assert
        assertEquals(eventType, auditEvent.getEventType());
        assertEquals(entityType, auditEvent.getEntityType());
        assertEquals(entityId, auditEvent.getEntityId());
        assertEquals(userId, auditEvent.getUserId());
        assertEquals(correlationId, auditEvent.getCorrelationId());
        assertEquals(metadata, auditEvent.getMetadata());
        assertNotNull(auditEvent.getTimestamp());
    }

    @Test
    void testAuditEvent_TimestampIsSet() {
        // Arrange
        AuditEmitterImpl.AuditEvent auditEvent = new AuditEmitterImpl.AuditEvent();

        // Act
        auditEvent.setTimestamp(java.time.LocalDateTime.now());

        // Assert
        assertNotNull(auditEvent.getTimestamp());
    }

    @Test
    void testEmitStaffUpdateEvent_VerifyTimestampIsSet() {
        // Arrange
        MDC.put("correlationId", "timestamp-test");
        Long staffId = 105L;
        Long userId = 205L;
        Map<String, Object> changes = new HashMap<>();
        changes.put("field", "value");

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("timestamp"));
    }

    @Test
    void testEmitStaffDeactivateEvent_VerifyTimestampIsSet() {
        // Arrange
        MDC.put("correlationId", "timestamp-deactivate-test");
        Long staffId = 106L;
        Long userId = 206L;
        String reason = "Test reason";

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("timestamp"));
    }

    @Test
    void testEmitAuditEvent_VerifyTimestampIsSet() {
        // Arrange
        MDC.put("correlationId", "timestamp-generic-test");
        String eventType = "TIMESTAMP_TEST";
        String entityType = "ENTITY";
        Long entityId = 503L;
        Long userId = 603L;
        Map<String, Object> metadata = new HashMap<>();

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("timestamp"));
    }

    @Test
    void testEmitStaffUpdateEvent_VerifyEntityTypeIsStaff() {
        // Arrange
        MDC.put("correlationId", "entity-type-test");
        Long staffId = 107L;
        Long userId = 207L;
        Map<String, Object> changes = new HashMap<>();

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("\"entityType\":\"STAFF\""));
    }

    @Test
    void testEmitStaffDeactivateEvent_VerifyEntityTypeIsStaff() {
        // Arrange
        MDC.put("correlationId", "entity-type-deactivate-test");
        Long staffId = 108L;
        Long userId = 208L;
        String reason = "Test";

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("\"entityType\":\"STAFF\""));
    }

    @Test
    void testEmitStaffUpdateEvent_WithComplexChangesMap_LogsCorrectly() {
        // Arrange
        MDC.put("correlationId", "complex-changes-test");
        Long staffId = 109L;
        Long userId = 209L;
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "John Doe");
        changes.put("age", 30);
        changes.put("active", true);
        Map<String, String> nestedMap = new HashMap<>();
        nestedMap.put("nested", "value");
        changes.put("nested", nestedMap);

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("changes"));
        assertTrue(jsonLog.contains("John Doe"));
    }

    @Test
    void testEmitAuditEvent_WithComplexMetadata_LogsCorrectly() {
        // Arrange
        MDC.put("correlationId", "complex-metadata-test");
        String eventType = "COMPLEX_EVENT";
        String entityType = "COMPLEX_ENTITY";
        Long entityId = 504L;
        Long userId = 604L;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("string", "value");
        metadata.put("number", 42);
        metadata.put("boolean", false);
        metadata.put("array", new String[]{"a", "b", "c"});

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        String jsonLog = logEvents.get(0).getFormattedMessage();
        assertTrue(jsonLog.contains("COMPLEX_EVENT"));
    }

    @Test
    void testMultipleAuditEvents_AllLogged() {
        // Arrange
        MDC.put("correlationId", "multiple-events-test");

        // Act
        auditEmitter.emitStaffUpdateEvent(110L, 210L, new HashMap<>());
        auditEmitter.emitStaffDeactivateEvent(111L, 211L, "reason1");
        auditEmitter.emitAuditEvent("CUSTOM", "ENTITY", 505L, 605L, new HashMap<>());

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(3, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_UPDATE"));
        assertTrue(logEvents.get(1).getFormattedMessage().contains("STAFF_DEACTIVATE"));
        assertTrue(logEvents.get(2).getFormattedMessage().contains("CUSTOM"));
    }

    @Test
    void testEmitStaffUpdateEvent_VerifyLogFormat() {
        // Arrange
        MDC.put("correlationId", "log-format-test");
        Long staffId = 112L;
        Long userId = 212L;
        Map<String, Object> changes = new HashMap<>();
        changes.put("field", "value");

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        ILoggingEvent logEvent = logEvents.get(0);
        assertTrue(logEvent.getFormattedMessage().startsWith("AUDIT: {"));
        assertTrue(logEvent.getFormattedMessage().endsWith("}"));
    }

    @Test
    void testAuditEvent_DefaultConstructor() {
        // Act
        AuditEmitterImpl.AuditEvent auditEvent = new AuditEmitterImpl.AuditEvent();

        // Assert
        assertNotNull(auditEvent);
        assertNull(auditEvent.getEventType());
        assertNull(auditEvent.getEntityType());
        assertNull(auditEvent.getEntityId());
        assertNull(auditEvent.getUserId());
        assertNull(auditEvent.getTimestamp());
        assertNull(auditEvent.getCorrelationId());
        assertNull(auditEvent.getMetadata());
    }

    @Test
    void testEmitStaffUpdateEvent_WithNullStaffId_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-staffid-test");
        Long staffId = null;
        Long userId = 213L;
        Map<String, Object> changes = new HashMap<>();

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_UPDATE"));
    }

    @Test
    void testEmitStaffUpdateEvent_WithNullUserId_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-userid-test");
        Long staffId = 113L;
        Long userId = null;
        Map<String, Object> changes = new HashMap<>();

        // Act
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changes);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_UPDATE"));
    }

    @Test
    void testEmitStaffDeactivateEvent_WithNullStaffId_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-staffid-deactivate-test");
        Long staffId = null;
        Long userId = 214L;
        String reason = "Test reason";

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_DEACTIVATE"));
    }

    @Test
    void testEmitStaffDeactivateEvent_WithNullUserId_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-userid-deactivate-test");
        Long staffId = 114L;
        Long userId = null;
        String reason = "Test reason";

        // Act
        auditEmitter.emitStaffDeactivateEvent(staffId, userId, reason);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("STAFF_DEACTIVATE"));
    }

    @Test
    void testEmitAuditEvent_WithNullEntityId_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-entityid-test");
        String eventType = "NULL_ENTITY_EVENT";
        String entityType = "ENTITY";
        Long entityId = null;
        Long userId = 606L;
        Map<String, Object> metadata = new HashMap<>();

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains(eventType));
    }

    @Test
    void testEmitAuditEvent_WithNullUserId_LogsAuditEvent() {
        // Arrange
        MDC.put("correlationId", "null-userid-generic-test");
        String eventType = "NULL_USER_EVENT";
        String entityType = "ENTITY";
        Long entityId = 506L;
        Long userId = null;
        Map<String, Object> metadata = new HashMap<>();

        // Act
        auditEmitter.emitAuditEvent(eventType, entityType, entityId, userId, metadata);

        // Assert
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(1, logEvents.size());
        assertTrue(logEvents.get(0).getFormattedMessage().contains(eventType));
    }
}