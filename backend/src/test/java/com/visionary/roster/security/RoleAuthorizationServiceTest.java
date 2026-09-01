package com.visionary.roster.security;

import com.visionary.roster.entity.UserAccount;
import com.visionary.roster.exception.ForbiddenAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoleAuthorizationService {

    private final AuditEventPublisher auditEventPublisher;

    public RoleAuthorizationService(AuditEventPublisher auditEventPublisher) {
        this.auditEventPublisher = auditEventPublisher;
    }

    public void requireManagerRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenAccessException(
                "Access denied: MANAGER role required",
                null,
                "requireManagerRole",
                "Not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserAccount) {
            UserAccount user = (UserAccount) principal;
            if (!"MANAGER".equals(user.getRole())) {
                throw new ForbiddenAccessException(
                    "Access denied: MANAGER role required",
                    user.getId(),
                    "requireRole",
                    "User does not have MANAGER role"
                );
            }
        } else if (principal instanceof Long) {
            Long userId = (Long) principal;
            boolean hasManagerRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_MANAGER".equals(auth.getAuthority()));
            
            if (!hasManagerRole) {
                throw new ForbiddenAccessException(
                    "Access denied: MANAGER role required",
                    userId,
                    "requireManagerRole",
                    "User does not have MANAGER role"
                );
            }
        }
    }

    public void requireManagerRole(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenAccessException(
                "Manager role required",
                userId,
                "requireManagerRole",
                "Not authenticated"
            );
        }

        String userRole = extractUserRole(authentication);
        
        if (!"MANAGER".equals(userRole)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userRole", userRole);
            metadata.put("requiredRole", "MANAGER");
            metadata.put("authorizationResult", "DENIED");
            
            auditEventPublisher.publishAuthorizationFailure(userId, "requireManagerRole", metadata);
            
            throw new ForbiddenAccessException(
                "Manager role required",
                userId,
                "requireManagerRole",
                "User does not have MANAGER role"
            );
        }
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserAccount) {
            UserAccount user = (UserAccount) principal;
            return role.equals(user.getRole());
        } else if (principal instanceof Long) {
            return authentication.getAuthorities().stream()
                .anyMatch(auth -> ("ROLE_" + role).equals(auth.getAuthority()));
        }
        
        return false;
    }

    public void validateRole(String requiredRole, String operation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenAccessException(
                "Authentication required for role authorization check",
                null,
                operation,
                "Not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();
        Long userId = null;
        String userRole = null;

        if (principal instanceof Long) {
            userId = (Long) principal;
            userRole = authentication.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))
                .findFirst()
                .orElse(null);
        }

        if (userRole == null) {
            throw new ForbiddenAccessException(
                "No role assigned to user",
                userId,
                operation,
                "User has no role assigned"
            );
        }

        if ("SUPERVISOR".equals(userRole)) {
            throw new ForbiddenAccessException(
                "Access denied: SUPERVISOR role capabilities not yet defined",
                userId,
                operation,
                "SUPERVISOR role capabilities not yet defined"
            );
        }

        if (!requiredRole.equals(userRole)) {
            throw new ForbiddenAccessException(
                "Access denied: insufficient role privileges",
                userId,
                operation,
                "User role '" + userRole + "' does not match required role '" + requiredRole + "'"
            );
        }
    }

    private String extractUserRole(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserAccount) {
            return ((UserAccount) principal).getRole();
        } else if (principal instanceof Long) {
            return authentication.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))
                .findFirst()
                .orElse("UNKNOWN");
        }
        
        return "UNKNOWN";
    }
}