package com.visionary.roster.audit;

import java.util.Map;

/**
 * Interface for emitting audit events in the roster management system.
 * <p>
 * Implementations of this interface are responsible for publishing audit events
 * to the appropriate audit logging or event streaming infrastructure.
 * All audit events should include correlation IDs from CorrelationIdFilter
 * to enable request tracing across distributed systems.
 * </p>
 *
 * @author Roster Management System
 * @version 1.0
 */
public interface AuditEmitter {

    /**
     * Emits an audit event when staff information is updated.
     * <p>
     * This method should be called whenever staff data is modified to maintain
     * a complete audit trail of all changes.
     * </p>
     *
     * @param staffId the unique identifier of the staff member being updated
     * @param userId the unique identifier of the user performing the update
     * @param changes a map containing the changed fields and their new values.
     *                Key: field name (e.g., "firstName", "department")
     *                Value: new value of the field
     *                Example: {"firstName": "John", "department": "Engineering"}
     * @throws IllegalArgumentException if staffId or userId is null
     */
    void emitStaffUpdateEvent(Long staffId, Long userId, Map<String, Object> changes);

    /**
     * Emits an audit event when a staff member is deactivated.
     * <p>
     * This method captures the deactivation action along with the reason
     * to maintain compliance and accountability.
     * </p>
     *
     * @param staffId the unique identifier of the staff member being deactivated
     * @param userId the unique identifier of the user performing the deactivation
     * @param reason the reason for deactivation (e.g., "Resignation", "Termination", "Retirement")
     * @throws IllegalArgumentException if staffId or userId is null, or if reason is null or empty
     */
    void emitStaffDeactivateEvent(Long staffId, Long userId, String reason);

    /**
     * Emits a generic audit event for any auditable action in the system.
     * <p>
     * This method provides flexibility for emitting custom audit events
     * that don't fit the predefined event types. The implementation should
     * automatically include the correlation ID from CorrelationIdFilter
     * to enable end-to-end request tracing.
     * </p>
     * <p>
     * Event Structure:
     * The emitted event should contain at minimum:
     * <ul>
     *   <li>eventType: The type of event (e.g., "STAFF_CREATED", "ROLE_ASSIGNED")</li>
     *   <li>entityType: The type of entity affected (e.g., "STAFF", "DEPARTMENT", "ROLE")</li>
     *   <li>entityId: The unique identifier of the affected entity</li>
     *   <li>userId: The user who triggered the event</li>
     *   <li>timestamp: When the event occurred</li>
     *   <li>correlationId: Request correlation ID from CorrelationIdFilter for tracing</li>
     *   <li>metadata: Additional context-specific information</li>
     * </ul>
     * </p>
     *
     * @param eventType the type of audit event (e.g., "CREATE", "UPDATE", "DELETE", "ACCESS")
     * @param entityType the type of entity involved (e.g., "STAFF", "DEPARTMENT", "USER")
     * @param entityId the unique identifier of the entity involved in the event
     * @param userId the unique identifier of the user who triggered the event
     * @param metadata additional contextual information about the event.
     *                 This may include field changes, IP addresses, session information,
     *                 or any other relevant data specific to the event type.
     *                 Example: {"ipAddress": "192.168.1.1", "action": "LOGIN_SUCCESS"}
     * @throws IllegalArgumentException if any required parameter is null or if eventType/entityType is empty
     */
    void emitAuditEvent(String eventType, String entityType, Long entityId, Long userId, Map<String, Object> metadata);
}