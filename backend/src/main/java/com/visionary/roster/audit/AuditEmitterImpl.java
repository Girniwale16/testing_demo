package com.visionary.roster.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Implementation of AuditEmitter interface for emitting audit events.
 * This service handles audit event creation, logging, and future integration
 * with external audit systems.
 */
@Service
public class AuditEmitterImpl implements AuditEmitter {

    private static final Logger logger = LoggerFactory.getLogger(AuditEmitterImpl.class);
    private final ObjectMapper objectMapper;

    public AuditEmitterImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void emitStaffUpdateEvent(Long staffId, Long userId, Map<String, Object> changes) {
        String correlationId = MDC.get("correlationId");
        
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventType("STAFF_UPDATE");
        auditEvent.setEntityType("STAFF");
        auditEvent.setEntityId(staffId);
        auditEvent.setUserId(userId);
        auditEvent.setTimestamp(LocalDateTime.now());
        auditEvent.setCorrelationId(correlationId);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("changes", changes);
        auditEvent.setMetadata(metadata);
        
        logAuditEvent(auditEvent);
        publishAuditEvent(auditEvent);
    }

    @Override
    public void emitStaffDeactivateEvent(Long staffId, Long userId, String reason) {
        String correlationId = MDC.get("correlationId");
        
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventType("STAFF_DEACTIVATE");
        auditEvent.setEntityType("STAFF");
        auditEvent.setEntityId(staffId);
        auditEvent.setUserId(userId);
        auditEvent.setTimestamp(LocalDateTime.now());
        auditEvent.setCorrelationId(correlationId);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", reason);
        auditEvent.setMetadata(metadata);
        
        logAuditEvent(auditEvent);
        publishAuditEvent(auditEvent);
    }

    @Override
    public void emitAuditEvent(String eventType, String entityType, Long entityId, Long userId, Map<String, Object> metadata) {
        String correlationId = MDC.get("correlationId");
        
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventType(eventType);
        auditEvent.setEntityType(entityType);
        auditEvent.setEntityId(entityId);
        auditEvent.setUserId(userId);
        auditEvent.setTimestamp(LocalDateTime.now());
        auditEvent.setCorrelationId(correlationId);
        auditEvent.setMetadata(metadata);
        
        logAuditEvent(auditEvent);
        publishAuditEvent(auditEvent);
    }

    /**
     * Logs the audit event to application logs in JSON format.
     *
     * @param event the audit event to log
     */
    private void logAuditEvent(AuditEvent event) {
        try {
            String jsonString = objectMapper.writeValueAsString(event);
            logger.info("AUDIT: {}", jsonString);
        } catch (Exception e) {
            logger.error("Failed to serialize audit event to JSON", e);
        }
    }

    /**
     * Publishes the audit event to an external audit system.
     * TODO: Implement integration with external audit system (Kafka, SQS, etc.)
     *
     * @param event the audit event to publish
     */
    private void publishAuditEvent(AuditEvent event) {
        // TODO: Future integration with external audit system (Kafka, SQS, etc.)
        // This method is a stub for future implementation
    }

    /**
     * Inner class representing an audit event.
     */
    public static class AuditEvent {
        private String eventType;
        private String entityType;
        private Long entityId;
        private Long userId;
        private LocalDateTime timestamp;
        private String correlationId;
        private Map<String, Object> metadata;

        public AuditEvent() {
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public Long getEntityId() {
            return entityId;
        }

        public void setEntityId(Long entityId) {
            this.entityId = entityId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}