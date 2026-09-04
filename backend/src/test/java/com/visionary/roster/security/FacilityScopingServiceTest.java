package com.visionary.roster.security;

import com.visionary.roster.exception.FacilityAccessDeniedException;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Service
public class FacilityScopingService {

    private static final Logger logger = LoggerFactory.getLogger(FacilityScopingService.class);
    private final UserAccountRepository userAccountRepository;

    public FacilityScopingService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

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

    public void validateFacilityAccess(Long facilityId) {
        Long userFacilityId = getCurrentUserFacilityId();
        
        if (!userFacilityId.equals(facilityId)) {
            throw new ForbiddenAccessException("Cross-facility access denied");
        }
    }

    public void validateFacilityAccess(Long requestedFacilityId, String resourceType) {
        String correlationId = MDC.get("correlationId");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Facility scoping check failed: User not authenticated. CorrelationId: {}, ResourceType: {}, RequestedFacilityId: {}",
                    correlationId, resourceType, requestedFacilityId);
            throw new ForbiddenAccessException("Authentication required for facility scoping check");
        }

        Long userId = (Long) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Facility scoping check failed: User not found. UserId: {}, CorrelationId: {}, ResourceType: {}, RequestedFacilityId: {}",
                            userId, correlationId, resourceType, requestedFacilityId);
                    return new ForbiddenAccessException("User not found");
                });

        if (user.getFacility() == null) {
            logger.warn("Facility scoping check failed: User has no facility assigned. UserId: {}, CorrelationId: {}, ResourceType: {}, RequestedFacilityId: {}",
                    userId, correlationId, resourceType, requestedFacilityId);
            throw new ForbiddenAccessException("User has no facility assigned");
        }

        Long userFacilityId = user.getFacility().getFacilityId();

        if (!userFacilityId.equals(requestedFacilityId)) {
            logger.warn("Facility scoping violation detected. UserId: {}, UserFacilityId: {}, RequestedFacilityId: {}, ResourceType: {}, CorrelationId: {}",
                    userId, userFacilityId, requestedFacilityId, resourceType, correlationId);
            throw new ForbiddenAccessException("Access denied: facility boundary violation");
        }

        logger.debug("Facility scoping check passed. UserId: {}, FacilityId: {}, ResourceType: {}, CorrelationId: {}",
                userId, userFacilityId, resourceType, correlationId);
    }

    public void validateFacilityAccess(Long userId, Long facilityId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + userId));

        if (user.getFacility() == null) {
            throw new IllegalStateException("User has no facility assigned");
        }

        Long userFacilityId = user.getFacility().getFacilityId();

        if (!userFacilityId.equals(facilityId)) {
            throw new FacilityAccessDeniedException("User does not have access to facility: " + facilityId);
        }
    }
}