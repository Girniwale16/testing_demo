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