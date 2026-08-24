package com.visionary.roster;

import com.visionary.roster.dto.ErrorResponse;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import com.visionary.roster.security.FacilityScopingService;
import com.visionary.roster.security.RoleAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FacilityScopingService facilityScopingService;

    @InjectMocks
    private RoleAuthorizationService roleAuthorizationService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void validateFacilityAccess_validAccess_passes() {
        Long userId = 1L;
        Long facilityId = 100L;
        UserAccount user = new UserAccount();
        user.setUserId(userId);
        Facility facility = new Facility();
        facility.setFacilityId(facilityId);
        user.setFacility(facility);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> facilityScopingService.validateFacilityAccess(facilityId, "staff"));

        verify(userAccountRepository).findById(userId);
    }

    @Test
    void validateFacilityAccess_facilityMismatch_throwsForbiddenAccessException() {
        Long userId = 1L;
        Long userFacilityId = 100L;
        Long requestedFacilityId = 200L;
        UserAccount user = new UserAccount();
        user.setUserId(userId);
        Facility facility = new Facility();
        facility.setFacilityId(userFacilityId);
        user.setFacility(facility);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, "staff")
        );

        assertEquals("Access denied: facility boundary violation", exception.getMessage());
        assertEquals(userId, exception.getUserId());
        assertEquals(requestedFacilityId, exception.getFacilityId());
        assertEquals("staff", exception.getResource());
    }

    @Test
    void validateFacilityAccess_nullFacility_throwsForbiddenAccessException() {
        Long userId = 1L;
        Long requestedFacilityId = 100L;
        UserAccount user = new UserAccount();
        user.setUserId(userId);
        user.setFacility(null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, "staff")
        );

        assertEquals("User has no facility assigned", exception.getMessage());
        assertEquals(userId, exception.getUserId());
        assertEquals(requestedFacilityId, exception.getFacilityId());
    }

    @Test
    void validateFacilityAccess_notAuthenticated_throwsForbiddenAccessException() {
        Long requestedFacilityId = 100L;

        when(securityContext.getAuthentication()).thenReturn(null);

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, "staff")
        );

        assertEquals("Authentication required for facility scoping check", exception.getMessage());
        assertNull(exception.getUserId());
        assertEquals(requestedFacilityId, exception.getFacilityId());
    }

    @Test
    void validateFacilityAccess_userNotFound_throwsForbiddenAccessException() {
        Long userId = 1L;
        Long requestedFacilityId = 100L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.empty());

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> facilityScopingService.validateFacilityAccess(requestedFacilityId, "staff")
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals(userId, exception.getUserId());
        assertEquals(requestedFacilityId, exception.getFacilityId());
    }

    @Test
    void validateRole_validRole_passes() {
        Long userId = 1L;
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_MANAGER");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenReturn(Collections.singletonList(authority));

        assertDoesNotThrow(() -> roleAuthorizationService.validateRole("MANAGER", "create_roster"));
    }

    @Test
    void validateRole_roleMismatch_throwsForbiddenAccessException() {
        Long userId = 1L;
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_STAFF");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenReturn(Collections.singletonList(authority));

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> roleAuthorizationService.validateRole("MANAGER", "create_roster")
        );

        assertEquals("Access denied: insufficient role privileges", exception.getMessage());
        assertEquals(userId, exception.getUserId());
        assertEquals("create_roster", exception.getResource());
    }

    @Test
    void validateRole_supervisorRole_throwsForbiddenAccessException() {
        Long userId = 1L;
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SUPERVISOR");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenReturn(Collections.singletonList(authority));

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> roleAuthorizationService.validateRole("MANAGER", "create_roster")
        );

        assertEquals("Access denied: SUPERVISOR role capabilities not yet defined", exception.getMessage());
        assertEquals(userId, exception.getUserId());
    }

    @Test
    void validateRole_noRole_throwsForbiddenAccessException() {
        Long userId = 1L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> roleAuthorizationService.validateRole("MANAGER", "create_roster")
        );

        assertEquals("No role assigned to user", exception.getMessage());
        assertEquals(userId, exception.getUserId());
    }

    @Test
    void validateRole_notAuthenticated_throwsForbiddenAccessException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        ForbiddenAccessException exception = assertThrows(
            ForbiddenAccessException.class,
            () -> roleAuthorizationService.validateRole("MANAGER", "create_roster")
        );

        assertEquals("Authentication required for role authorization check", exception.getMessage());
        assertNull(exception.getUserId());
    }

    @Test
    void forbiddenAccessException_fieldsPopulated_correctValues() {
        Long userId = 1L;
        Long facilityId = 100L;
        String resource = "staff";
        String reason = "facility mismatch";

        ForbiddenAccessException exception = new ForbiddenAccessException(
            "Access denied",
            userId,
            facilityId,
            resource,
            reason
        );

        assertEquals("Access denied", exception.getMessage());
        assertEquals(userId, exception.getUserId());
        assertEquals(facilityId, exception.getFacilityId());
        assertEquals(resource, exception.getResource());
        assertEquals(reason, exception.getReason());
    }
}