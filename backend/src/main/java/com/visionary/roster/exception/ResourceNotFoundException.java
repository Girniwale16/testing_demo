package com.visionary.roster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception class for resource not found scenarios.
 * This unchecked exception is thrown when a requested resource cannot be found in the system.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private String resourceType;
    private Long resourceId;

    /**
     * Constructs a new ResourceNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with resource type and ID.
     * Formats the message as '{resourceType} with ID {resourceId} not found'.
     *
     * @param resourceType the type of resource that was not found
     * @param resourceId the ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(resourceType + " with ID " + resourceId + " not found");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Gets the resource ID.
     *
     * @return the resource ID
     */
    public Long getResourceId() {
        return resourceId;
    }
}