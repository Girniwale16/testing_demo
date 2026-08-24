package com.visionary.roster.exception;

public class InactiveAccountException extends RuntimeException {

    private final Long userId;

    public InactiveAccountException(String message, Long userId) {
        super(message);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}