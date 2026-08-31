package com.visionary.roster.security;

import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacilityScopingServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FacilityScopingService facilityScopingService;

    private UserAccount testUser;
    private Facility testFacility;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        MDC.clear();
        
        testFacility = new Facility();
        testFacility.setFacilityId(100L);
        
        testUser = new UserAccount();
        testUser.setUserId(1L);
        testUser.setFacility(testFacility);
    }

    // ==================== getCurrentUserFacilityId() Tests ====================

    @Test
    void getCurrentUserFacilityId_WhenAuthenticationIsNull_ShouldThrowIllegalStateException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> facilityScopingService.getCurrentUserFacilityId());

        assertEquals("User is not authenticated", exception.getMessage());
        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    void getCurrentUserFacilityId_WhenUserIsNotAuthenticated_ShouldThrowIllegalStateException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> facilityScopingService.getCurrentUserFacilityId());

        assertEquals("User is not authenticated", exception.getMessage());
        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    void getCurrentUserFacilityId_WhenUserNotFoundInRepository_ShouldThrowIllegalStateException() {
        Long userId = 1L;
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> facilityScopingService.getCurrentUserFacilityId());

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void getCurrentUserFacilityId_WhenUserHasNoFacilityAssigned_ShouldThrowIllegalStateException() {
        Long userId = 1L;
        UserAccount userWithoutFacility = new UserAccount();
        userWithoutFacility.setUserId(userId);
        userWithoutFacility.setFacility(null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(userWithoutFacility));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> facilityScopingService.getCurrentUserFacilityId());

        assertEquals("User has no facility assigned", exception.getMessage());
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void getCurrentUserFacilityId_WhenUserIsAuthenticatedAndHasFacility_ShouldReturnFacilityId() {
        Long userId = 1L;
        Long expectedFacilityId = 100L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Long actualFacilityId = facilityScopingService.getCurrentUserFacilityId();

        assertEquals(expectedFacilityId, actualFacilityId);
        verify(userAccountRepository).findById(userId);
    }

    // ==================== validateFacilityAccess(Long facilityId) Tests ====================

    @Test
    void validateFacilityAccess_WhenFacilityIdMatches_ShouldNotThrowException() {
        Long userId = 1L;
        Long facilityId = 100L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> facilityScopingService.validateFacilityAccess(facilityId));
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccess_WhenFacilityIdDoesNotMatch_ShouldThrowForbiddenAccessException() {
        Long userId = 1L;
        Long requestedFacilityId = 200L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> facilityScopingService.validateFacilityAccess(requestedFacilityId));

        assertEquals("Cross-facility access denied", exception.getMessage());
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccess_WhenUserNotAuthenticated_ShouldThrowIllegalStateException() {
        Long facilityId = 100L;
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> facilityScopingService.validateFacilityAccess(facilityId));

        assertEquals("User is not authenticated", exception.getMessage());
        verify(userAccountRepository, never()).findById(any());
    }

    // ==================== validateFacilityAccess(Long, String) Tests ====================

    @Test
    void validateFacilityAccessWithResourceType_WhenAuthenticationIsNull_ShouldThrowForbiddenAccessException() {
        Long requestedFacilityId = 100L;
        String resourceType = "Staff";
        MDC.put("correlationId", "test-correlation-id");

        when(securityContext.getAuthentication()).thenReturn(null);

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));

        assertEquals("Authentication required for facility scoping check", exception.getMessage());
        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    void validateFacilityAccessWithResourceType_WhenUserIsNotAuthenticated_ShouldThrowForbiddenAccessException() {
        Long requestedFacilityId = 100L;
        String resourceType = "Staff";
        MDC.put("correlationId", "test-correlation-id");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));

        assertEquals("Authentication required for facility scoping check", exception.getMessage());
        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    void validateFacilityAccessWithResourceType_WhenUserNotFound_ShouldThrowForbiddenAccessException() {
        Long userId = 1L;
        Long requestedFacilityId = 100L;
        String resourceType = "Staff";
        MDC.put("correlationId", "test-correlation-id");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.empty());

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));

        assertEquals("User not found", exception.getMessage());
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccessWithResourceType_WhenUserHasNoFacility_ShouldThrowForbiddenAccessException() {
        Long userId = 1L;
        Long requestedFacilityId = 100L;
        String resourceType = "Staff";
        MDC.put("correlationId", "test-correlation-id");

        UserAccount userWithoutFacility = new UserAccount();
        userWithoutFacility.setUserId(userId);
        userWithoutFacility.setFacility(null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(userWithoutFacility));

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));

        assertEquals("User has no facility assigned", exception.getMessage());
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccessWithResourceType_WhenFacilityIdDoesNotMatch_ShouldThrowForbiddenAccessException() {
        Long userId = 1L;
        Long requestedFacilityId = 200L;
        String resourceType = "Staff";
        MDC.put("correlationId", "test-correlation-id");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));

        assertEquals("Access denied: facility boundary violation", exception.getMessage());
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccessWithResourceType_WhenFacilityIdMatches_ShouldNotThrowException() {
        Long userId = 1L;
        Long requestedFacilityId = 100L;
        String resourceType = "Staff";
        MDC.put("correlationId", "test-correlation-id");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));
        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccessWithResourceType_WhenCorrelationIdIsNull_ShouldStillValidate() {
        Long userId = 1L;
        Long requestedFacilityId = 100L;
        String resourceType = "Staff";
        MDC.clear();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> facilityScopingService.validateFacilityAccess(requestedFacilityId, resourceType));
        verify(userAccountRepository).findById(userId);
    }

    // ==================== Edge Case Tests ====================

    @Test
    void validateFacilityAccess_WithNullFacilityId_ShouldThrowNullPointerException() {
        Long userId = 1L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThrows(NullPointerException.class,
                () -> facilityScopingService.validateFacilityAccess(null));
    }

    @Test
    void getCurrentUserFacilityId_ShouldReturnLongType() {
        Long userId = 1L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Long result = facilityScopingService.getCurrentUserFacilityId();

        assertNotNull(result);
        assertTrue(result instanceof Long);
    }

    @Test
    void validateFacilityAccess_ShouldCallGetCurrentUserFacilityIdInternally() {
        Long userId = 1L;
        Long facilityId = 100L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        facilityScopingService.validateFacilityAccess(facilityId);

        verify(userAccountRepository, times(1)).findById(userId);
    }
}