package com.visionary.roster.security;

import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FacilityScopingService {

    private static final Logger logger = LoggerFactory.getLogger("com.visionary.roster.security.FacilityScopingService");

    private final UserAccountRepository userAccountRepository;

    public FacilityScopingService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Extracts the facility ID from the authenticated user's security context.
     * <p>
     * This method is the single source of truth for facility context extraction across the application.
     * It retrieves the authenticated user's principal from the SecurityContextHolder, loads the full
     * UserAccount entity, and returns the associated facility ID.
     * </p>
     * <p>
     * <strong>CRITICAL:</strong> This method is consumed by AuthService, UserAccountRepository queries,
     * and StaffRepository. Do not modify the return type or signature as breaking changes will cascade
     * to 5+ services and 8+ repositories.
     * </p>
     *
     * @return the facility ID of the currently authenticated user
     * @throws IllegalStateException if the user is not authenticated, user not found, or facility is not assigned
     */
    public Long getCurrentUserFacilityId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Long userId = (Long) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + userId));

        if (user.getFacility() == null) {
            throw new IllegalStateException("User has no facility assigned");
        }

        return user.getFacility().getFacilityId();
    }

    /**
     * Validates that the provided facility ID matches the current authenticated user's facility.
     * <p>
     * This method must be called before any cross-facility data access to enforce facility scoping
     * and prevent unauthorized access to resources belonging to other facilities.
     * </p>
     * <p>
     * <strong>CRITICAL:</strong> This method throws ForbiddenAccessException to maintain consistency
     * with existing error handling in GlobalExceptionHandler. Do not modify the exception type as it
     * will break authorization flows across the application.
     * </p>
     *
     * @param facilityId the facility ID to validate against the current user's facility
     * @throws ForbiddenAccessException if the provided facility ID does not match the current user's facility
     */
    public void validateFacilityAccess(Long facilityId) {
        Long currentUserFacilityId = getCurrentUserFacilityId();
        
        if (!currentUserFacilityId.equals(facilityId)) {
            throw new ForbiddenAccessException("Cross-facility access denied");
        }
    }

    public void validateFacilityAccess(Long requestedFacilityId, String resourceType) {
        String correlationId = MDC.get("correlationId");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Facility scoping check failed - not authenticated - correlationId: {}, requestedFacilityId: {}, resourceType: {}",
                    correlationId, requestedFacilityId, resourceType);
            throw new ForbiddenAccessException(
                    "Authentication required for facility scoping check",
                    null,
                    requestedFacilityId,
                    resourceType,
                    "Not authenticated"
            );
        }

        Long userId = (Long) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Facility scoping check failed - user not found - correlationId: {}, userId: {}, requestedFacilityId: {}, resourceType: {}",
                            correlationId, userId, requestedFacilityId, resourceType);
                    return new ForbiddenAccessException(
                            "User not found",
                            userId,
                            requestedFacilityId,
                            resourceType,
                            "User not found in system"
                    );
                });

        if (user.getFacility() == null) {
            logger.error("Facility scoping check failed - user has no facility assigned - correlationId: {}, userId: {}, requestedFacilityId: {}, resourceType: {}",
                    correlationId, userId, requestedFacilityId, resourceType);
            throw new ForbiddenAccessException(
                    "User has no facility assigned",
                    userId,
                    requestedFacilityId,
                    resourceType,
                    "User facility is null"
            );
        }

        Long userFacilityId = user.getFacility().getFacilityId();

        if (!userFacilityId.equals(requestedFacilityId)) {
            logger.warn("Facility boundary violation - correlationId: {}, userId: {}, userFacilityId: {}, requestedFacilityId: {}, resourceType: {}, result: DENIED",
                    correlationId, userId, userFacilityId, requestedFacilityId, resourceType);
            throw new ForbiddenAccessException(
                    "Access denied: facility boundary violation",
                    userId,
                    requestedFacilityId,
                    resourceType,
                    "User facility does not match requested resource facility"
            );
        }

        logger.info("Facility scoping check passed - correlationId: {}, userId: {}, userFacilityId: {}, requestedFacilityId: {}, resourceType: {}, result: ALLOWED",
                correlationId, userId, userFacilityId, requestedFacilityId, resourceType);
    }
}