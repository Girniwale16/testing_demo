package com.visionary.roster.exception;

public class ForbiddenAccessException extends RuntimeException {

    private final Long userId;
    private final Long facilityId;
    private final String resource;
    private final String reason;

    public ForbiddenAccessException(String message, Long userId, Long facilityId, String resource, String reason) {
        super(message);
        this.userId = userId;
        this.facilityId = facilityId;
        this.resource = resource;
        this.reason = reason;
    }

    public ForbiddenAccessException(String message) {
        this(message, null, null, null, null);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public String getResource() {
        return resource;
    }

    public String getReason() {
        return reason;
    }
}