package com.visionary.roster.security;

import com.visionary.roster.exception.ForbiddenAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RoleAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger("com.visionary.roster.security.RoleAuthorizationService");

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
}