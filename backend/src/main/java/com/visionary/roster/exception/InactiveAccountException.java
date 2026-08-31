package com.visionary.roster.exception;

/**
 * Custom exception thrown when an attempt is made to authenticate or perform operations
 * with an inactive user account.
 * 
 * <p><b>Exception Structure and Message Format:</b></p>
 * <ul>
 *   <li>Extends RuntimeException for unchecked exception handling</li>
 *   <li>Contains a descriptive message explaining the inactive account state</li>
 *   <li>Stores the userId of the inactive account for audit and logging purposes</li>
 * </ul>
 * 
 * <p><b>GlobalExceptionHandler Integration:</b></p>
 * <p>This exception is handled by the GlobalExceptionHandler which maps it to HTTP 403 (Forbidden)
 * status code, indicating that the user account exists but is not active and therefore access is denied.</p>
 * 
 * <p><b>Usage Pattern for Staff Deactivation Logic:</b></p>
 * <pre>
 * // Throw this exception when:
 * // 1. A user attempts to login but their account status is INACTIVE
 * // 2. An API request is made by a user whose account has been deactivated
 * // 3. Staff deactivation workflow needs to prevent further access
 * 
 * Example usage in AuthService:
 * {@code
 * if (!user.isActive()) {
 *     throw new InactiveAccountException(
 *         "Account is inactive. Please contact administrator.", 
 *         user.getId()
 *     );
 * }
 * }
 * </pre>
 * 
 * <p><b>Integration Compatibility:</b></p>
 * <p>This exception maintains backward compatibility with:</p>
 * <ul>
 *   <li>AuthService: Used during authentication flow to validate account status</li>
 *   <li>GlobalExceptionHandler: Properly caught and converted to standardized error response</li>
 * </ul>
 * 
 * @see com.visionary.roster.service.AuthService
 * @see com.visionary.roster.exception.GlobalExceptionHandler
 * @author Visionary Roster Team
 * @since 1.0
 */
public class InactiveAccountException extends RuntimeException {

    /**
     * The unique identifier of the user whose account is inactive.
     * Used for audit logging and tracking deactivation events.
     */
    private final Long userId;

    /**
     * Constructs a new InactiveAccountException with the specified detail message and user ID.
     * 
     * @param message the detail message explaining why the account is inactive
     *                (e.g., "Account is inactive. Please contact administrator.")
     * @param userId  the unique identifier of the inactive user account
     */
    public InactiveAccountException(String message, Long userId) {
        super(message);
        this.userId = userId;
    }

    /**
     * Returns the user ID associated with the inactive account.
     * 
     * @return the unique identifier of the inactive user
     */
    public Long getUserId() {
        return userId;
    }
}