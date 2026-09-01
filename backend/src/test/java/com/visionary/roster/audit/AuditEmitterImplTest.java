package com.visionary.roster.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implementation of audit event emitter for the roster system.
 * Publishes audit events as structured JSON logs.
 */
public class AuditEmitterImpl {

    private static final Logger logger = LoggerFactory.getLogger(AuditEmitterImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Emits an audit event for staff update operations.
     *
     * @param staffId the ID of the staff member being updated
     * @param userId the ID of the user performing the update
     * @param changes map of changes being made
     */
    public void emitStaffUpdateEvent(Long staffId, Long userId, Map<String, Object> changes) {
        emitAuditEvent("STAFF_UPDATE", "STAFF", staffId, userId, changes);
    }

    /**
     * Emits an audit event for staff deactivation operations.
     *
     * @param staffId the ID of the staff member being deactivated
     * @param userId the ID of the user performing the deactivation
     * @param reason the reason for deactivation
     */
    public void emitStaffDeactivateEvent(Long staffId, Long userId, String reason) {
        Map<String, Object> metadata = Map.of("reason", reason != null ? reason : "");
        emitAuditEvent("STAFF_DEACTIVATE", "STAFF", staffId, userId, metadata);
    }

    /**
     * Generic method to emit audit events.
     *
     * @param eventType the type of event
     * @param entityType the type of entity being audited
     * @param entityId the ID of the entity
     * @param userId the ID of the user performing the action
     * @param metadata additional metadata for the event
     */
    public void emitAuditEvent(String eventType, String entityType, Long entityId, Long userId, Map<String, Object> metadata) {
        try {
            AuditEvent event = new AuditEvent();
            event.setEventType(eventType);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setUserId(userId);
            event.setTimestamp(LocalDateTime.now());
            event.setCorrelationId(MDC.get("correlationId"));
            event.setMetadata(metadata);

            String jsonLog = objectMapper.writeValueAsString(event);
            logger.info("AUDIT: {}", jsonLog);
        } catch (Exception e) {
            logger.error("Failed to emit audit event", e);
        }
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