package com.visionary.roster.security;

import com.visionary.roster.entity.UserAccount;
import com.visionary.roster.exception.ForbiddenAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAuthorizationServiceTest {

    @InjectMocks
    private RoleAuthorizationService roleAuthorizationService;

    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        MDC.put("correlationId", "test-correlation-id");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    // ==================== requireManagerRole() Tests ====================

    @Test
    void requireManagerRole_whenNotAuthenticated_throwsForbiddenAccessException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertNull(exception.getUserId());
        assertEquals("requireManagerRole", exception.getOperation());
        assertEquals("Not authenticated", exception.getReason());
    }

    @Test
    void requireManagerRole_whenAuthenticationNotAuthenticated_throwsForbiddenAccessException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertEquals("requireManagerRole", exception.getOperation());
    }

    @Test
    void requireManagerRole_whenPrincipalIsUserAccountWithManagerRole_succeeds() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole("MANAGER");

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act & Assert
        assertDoesNotThrow(() -> roleAuthorizationService.requireManagerRole());
    }

    @Test
    void requireManagerRole_whenPrincipalIsUserAccountWithoutManagerRole_throwsForbiddenAccessException() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole("EMPLOYEE");

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertEquals(1L, exception.getUserId());
        assertEquals("requireRole", exception.getOperation());
        assertEquals("User does not have MANAGER role", exception.getReason());
    }

    @Test
    void requireManagerRole_whenPrincipalIsUserAccountWithNullRole_throwsForbiddenAccessException() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole(null);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertEquals(1L, exception.getUserId());
    }

    @Test
    void requireManagerRole_whenPrincipalIsLongWithManagerRole_succeeds() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        assertDoesNotThrow(() -> roleAuthorizationService.requireManagerRole());
    }

    @Test
    void requireManagerRole_whenPrincipalIsLongWithoutManagerRole_throwsForbiddenAccessException() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertEquals(1L, exception.getUserId());
        assertEquals("requireManagerRole", exception.getOperation());
        assertEquals("User does not have MANAGER role", exception.getReason());
    }

    @Test
    void requireManagerRole_whenPrincipalIsLongWithNoRoleAuthority_throwsForbiddenAccessException() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("SOME_OTHER_AUTHORITY"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertEquals(1L, exception.getUserId());
    }

    @Test
    void requireManagerRole_whenPrincipalIsLongWithEmptyAuthorities_throwsForbiddenAccessException() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.emptyList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.requireManagerRole()
        );

        assertEquals("Access denied: MANAGER role required", exception.getMessage());
        assertEquals(1L, exception.getUserId());
    }

    // ==================== hasRole() Tests ====================

    @Test
    void hasRole_whenNotAuthenticated_returnsFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    @Test
    void hasRole_whenAuthenticationNotAuthenticated_returnsFalse() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    @Test
    void hasRole_whenPrincipalIsUserAccountWithMatchingRole_returnsTrue() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole("MANAGER");

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertTrue(result);
    }

    @Test
    void hasRole_whenPrincipalIsUserAccountWithDifferentRole_returnsFalse() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole("EMPLOYEE");

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    @Test
    void hasRole_whenPrincipalIsUserAccountWithNullRole_returnsFalse() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole(null);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    @Test
    void hasRole_whenPrincipalIsLongWithMatchingRole_returnsTrue() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertTrue(result);
    }

    @Test
    void hasRole_whenPrincipalIsLongWithDifferentRole_returnsFalse() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    @Test
    void hasRole_whenPrincipalIsLongWithNoRoleAuthority_returnsFalse() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("SOME_OTHER_AUTHORITY"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    @Test
    void hasRole_whenPrincipalIsLongWithEmptyAuthorities_returnsFalse() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.emptyList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act
        boolean result = roleAuthorizationService.hasRole("MANAGER");

        // Assert
        assertFalse(result);
    }

    // ==================== validateRole() Tests ====================

    @Test
    void validateRole_whenNotAuthenticated_throwsForbiddenAccessException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.validateRole("MANAGER", "testOperation")
        );

        assertEquals("Authentication required for role authorization check", exception.getMessage());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("Not authenticated", exception.getReason());
    }

    @Test
    void validateRole_whenAuthenticationNotAuthenticated_throwsForbiddenAccessException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.validateRole("MANAGER", "testOperation")
        );

        assertEquals("Authentication required for role authorization check", exception.getMessage());
    }

    @Test
    void validateRole_whenNoRoleFound_throwsForbiddenAccessException() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("SOME_OTHER_AUTHORITY"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.validateRole("MANAGER", "testOperation")
        );

        assertEquals("No role assigned to user", exception.getMessage());
        assertEquals(1L, exception.getUserId());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("User has no role assigned", exception.getReason());
    }

    @Test
    void validateRole_whenUserHasSupervisorRole_throwsForbiddenAccessException() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.validateRole("MANAGER", "testOperation")
        );

        assertEquals("Access denied: SUPERVISOR role capabilities not yet defined", exception.getMessage());
        assertEquals(1L, exception.getUserId());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("SUPERVISOR role capabilities not yet defined", exception.getReason());
    }

    @Test
    void validateRole_whenRoleMismatch_throwsForbiddenAccessException() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> roleAuthorizationService.validateRole("MANAGER", "testOperation")
        );

        assertEquals("Access denied: insufficient role privileges", exception.getMessage());
        assertEquals(1L, exception.getUserId());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("User role 'EMPLOYEE' does not match required role 'MANAGER'", exception.getReason());
    }

    @Test
    void validateRole_whenRoleMatches_succeeds() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        assertDoesNotThrow(() -> roleAuthorizationService.validateRole("MANAGER", "testOperation"));
    }

    // ==================== Exception Type Verification Tests ====================

    @Test
    void requireManagerRole_throwsForbiddenAccessExceptionType_notOtherExceptionTypes() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setRole("EMPLOYEE");

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act & Assert
        try {
            roleAuthorizationService.requireManagerRole();
            fail("Expected ForbiddenAccessException to be thrown");
        } catch (Exception e) {
            assertEquals(ForbiddenAccessException.class, e.getClass());
        }
    }

    @Test
    void validateRole_throwsForbiddenAccessExceptionType_notOtherExceptionTypes() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert
        try {
            roleAuthorizationService.validateRole("MANAGER", "testOperation");
            fail("Expected ForbiddenAccessException to be thrown");
        } catch (Exception e) {
            assertEquals(ForbiddenAccessException.class, e.getClass());
        }
    }

    // ==================== Role String Constant Tests ====================

    @Test
    void requireManagerRole_usesManagerRoleConstant() {
        // Arrange
        Long userId = 1L;
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act & Assert - verifies that "MANAGER" string is used
        assertDoesNotThrow(() -> roleAuthorizationService.requireManagerRole());
    }

    @Test
    void requireManagerRole_rejectsNonManagerRoles() {
        // Arrange - test various non-MANAGER roles
        String[] nonManagerRoles = {"EMPLOYEE", "ADMIN", "USER", "SUPERVISOR", "DIRECTOR"};

        for (String role : nonManagerRoles) {
            Long userId = 1L;
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Act & Assert
            assertThrows(
                    ForbiddenAccessException.class,
                    () -> roleAuthorizationService.requireManagerRole(),
                    "Should reject role: " + role
            );
        }
    }
}