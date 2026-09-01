package com.visionary.roster.security;

import com.visionary.roster.model.UserAccount;
import com.visionary.roster.exception.ForbiddenAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for role-based authorization checks.
 * Provides methods to enforce RBAC (Role-Based Access Control) across the application.
 */
@Service
public class RoleAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger("com.visionary.roster.security.RoleAuthorizationService");

    /**
     * Validates that the current authenticated user has the required role.
     * 
     * @param requiredRole the role required to perform the operation
     * @param operation the operation being performed (for logging purposes)
     * @throws ForbiddenAccessException if user is not authenticated or does not have the required role
     */
    public void validateRole(String requiredRole, String operation) {
        String correlationId = MDC.get("correlationId");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Role authorization check failed - not authenticated - correlationId: {}, requiredRole: {}, operation: {}",
                    correlationId, requiredRole, operation);
            throw new ForbiddenAccessException(
                    "Authentication required for role authorization check",
                    null,
                    null,
                    operation,
                    "Not authenticated"
            );
        }

        Long userId = (Long) authentication.getPrincipal();

        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse(null);

        if (userRole == null) {
            logger.error("Role authorization check failed - no role found - correlationId: {}, userId: {}, requiredRole: {}, operation: {}",
                    correlationId, userId, requiredRole, operation);
            throw new ForbiddenAccessException(
                    "No role assigned to user",
                    userId,
                    null,
                    operation,
                    "User has no role assigned"
            );
        }

        if ("SUPERVISOR".equals(userRole)) {
            logger.warn("Role authorization check failed - SUPERVISOR role capabilities not defined - correlationId: {}, userId: {}, userRole: {}, requiredRole: {}, operation: {}, result: DENIED",
                    correlationId, userId, userRole, requiredRole, operation);
            throw new ForbiddenAccessException(
                    "Access denied: SUPERVISOR role capabilities not yet defined",
                    userId,
                    null,
                    operation,
                    "SUPERVISOR role capabilities not yet defined"
            );
        }

        if (!userRole.equals(requiredRole)) {
            logger.warn("Role authorization check failed - role mismatch - correlationId: {}, userId: {}, userRole: {}, requiredRole: {}, operation: {}, result: DENIED",
                    correlationId, userId, userRole, requiredRole, operation);
            throw new ForbiddenAccessException(
                    "Access denied: insufficient role privileges",
                    userId,
                    null,
                    operation,
                    "User role '" + userRole + "' does not match required role '" + requiredRole + "'"
            );
        }

        logger.info("Role authorization check passed - correlationId: {}, userId: {}, userRole: {}, requiredRole: {}, operation: {}, result: ALLOWED",
                correlationId, userId, userRole, requiredRole, operation);
    }

    /**
     * Enforces that the current authenticated user has the MANAGER role.
     * This method must be called at the beginning of all manager-only service methods.
     * 
     * @throws ForbiddenAccessException if user does not have MANAGER role
     */
    public void requireManagerRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenAccessException(
                    "Access denied: MANAGER role required",
                    null,
                    null,
                    "requireManagerRole",
                    "Not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();
        UserAccount user = null;
        
        if (principal instanceof UserAccount) {
            user = (UserAccount) principal;
        } else if (principal instanceof Long) {
            // Handle case where principal is userId
            Long userId = (Long) principal;
            String userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.substring(5))
                    .findFirst()
                    .orElse(null);
            
            if (userRole == null || !userRole.equals("MANAGER")) {
                throw new ForbiddenAccessException(
                        "Access denied: MANAGER role required",
                        userId,
                        null,
                        "requireManagerRole",
                        "User does not have MANAGER role"
                );
            }
            return;
        }

        if (user != null) {
            requireRole("MANAGER", user);
        }
    }

    /**
     * Checks if the current authenticated user has the specified role.
     * 
     * @param role the role to check
     * @return true if the user has the specified role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserAccount) {
            UserAccount user = (UserAccount) principal;
            return user.getRole() != null && user.getRole().equals(role);
        } else if (principal instanceof Long) {
            // Handle case where principal is userId
            String userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.substring(5))
                    .findFirst()
                    .orElse(null);
            
            return userRole != null && userRole.equals(role);
        }
        
        return false;
    }

    /**
     * Enforces that the given user has the required role.
     * 
     * @param requiredRole the role required
     * @param user the user to check
     * @throws ForbiddenAccessException if user does not have the required role
     */
    private void requireRole(String requiredRole, UserAccount user) {
        if (user == null || user.getRole() == null || !user.getRole().equals(requiredRole)) {
            Long userId = user != null ? user.getUserAccountId() : null;
            throw new ForbiddenAccessException(
                    "Access denied: " + requiredRole + " role required",
                    userId,
                    null,
                    "requireRole",
                    "User does not have " + requiredRole + " role"
            );
        }
    }
}