package com.visionary.roster.service;

import com.visionary.roster.dto.LoginResponse;
import com.visionary.roster.dto.SessionResponse;
import com.visionary.roster.exception.InactiveAccountException;
import com.visionary.roster.exception.InvalidCredentialsException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserAccount testUser;
    private Facility testFacility;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_PASSWORD_HASH = "$2a$10$hashedPassword";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_FACILITY_ID = 100L;
    private static final String TEST_FACILITY_NAME = "Test Facility";
    private static final String TEST_ROLE = "ADMIN";
    private static final String TEST_CORRELATION_ID = "test-correlation-123";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        MDC.clear();
        MDC.put("correlationId", TEST_CORRELATION_ID);

        testFacility = new Facility();
        testFacility.setFacilityId(TEST_FACILITY_ID);
        testFacility.setName(TEST_FACILITY_NAME);

        testUser = new UserAccount();
        testUser.setUserAccountId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setPasswordHash(TEST_PASSWORD_HASH);
        testUser.setRole(TEST_ROLE);
        testUser.setIsActive(true);
        testUser.setFacility(testFacility);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("login() - Successful login with valid credentials")
    void login_WithValidCredentials_ReturnsLoginResponse() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(response.getRole()).isEqualTo(TEST_ROLE);
        assertThat(response.getFacilityId()).isEqualTo(TEST_FACILITY_ID);
        assertThat(response.getFacilityName()).isEqualTo(TEST_FACILITY_NAME);
        assertThat(response.getMessage()).isEqualTo("Login successful");

        verify(userAccountRepository).findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_PASSWORD_HASH);
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    @DisplayName("login() - Updates lastLoginAt timestamp")
    void login_UpdatesLastLoginTimestamp() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        LocalDateTime beforeLogin = LocalDateTime.now();

        // Act
        authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);

        // Assert
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userCaptor.capture());
        UserAccount savedUser = userCaptor.getValue();
        
        assertThat(savedUser.getLastLoginAt()).isNotNull();
        assertThat(savedUser.getLastLoginAt()).isAfterOrEqualTo(beforeLogin);
    }

    @Test
    @DisplayName("login() - Establishes Spring Security authentication context")
    void login_EstablishesSecurityContext() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        // Act
        authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(TEST_USER_ID);
        assertThat(authentication.getAuthorities())
                .containsExactly(new SimpleGrantedAuthority("ROLE_" + TEST_ROLE));
    }

    @Test
    @DisplayName("login() - Populates MDC with userId and facilityId")
    void login_PopulatesMDCContext() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        // Act
        authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);

        // Assert
        assertThat(MDC.get("userId")).isEqualTo(String.valueOf(TEST_USER_ID));
        assertThat(MDC.get("facilityId")).isEqualTo(String.valueOf(TEST_FACILITY_ID));
    }

    @Test
    @DisplayName("login() - Throws InvalidCredentialsException when user not found")
    void login_WithNonExistentUser_ThrowsInvalidCredentialsException() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(userAccountRepository).findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    @DisplayName("login() - Throws InvalidCredentialsException when password does not match")
    void login_WithInvalidPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(userAccountRepository).findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_PASSWORD_HASH);
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    @DisplayName("login() - Throws InactiveAccountException when account is inactive")
    void login_WithInactiveAccount_ThrowsInactiveAccountException() {
        // Arrange
        testUser.setIsActive(false);
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessage("Account is inactive");

        verify(userAccountRepository).findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_PASSWORD_HASH);
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    @DisplayName("login() - Wraps unexpected exceptions in RuntimeException")
    void login_WithUnexpectedException_ThrowsRuntimeException() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("An unexpected error occurred during login")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(userAccountRepository).findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID);
    }

    // ==================== GET CURRENT SESSION TESTS ====================

    @Test
    @DisplayName("getCurrentSession() - Returns session for authenticated user")
    void getCurrentSession_WithAuthenticatedUser_ReturnsSessionResponse() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(TEST_USER_ID);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userAccountRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        SessionResponse response = authService.getCurrentSession();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(response.getRole()).isEqualTo(TEST_ROLE);
        assertThat(response.getFacilityId()).isEqualTo(TEST_FACILITY_ID);
        assertThat(response.getFacilityName()).isEqualTo(TEST_FACILITY_NAME);
        assertThat(response.getIsActive()).isTrue();

        verify(userAccountRepository).findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("getCurrentSession() - Throws InvalidCredentialsException when authentication is null")
    void getCurrentSession_WithNullAuthentication_ThrowsInvalidCredentialsException() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(null);

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentSession())
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Not authenticated");

        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCurrentSession() - Throws InvalidCredentialsException when not authenticated")
    void getCurrentSession_WithUnauthenticatedUser_ThrowsInvalidCredentialsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentSession())
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Not authenticated");

        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCurrentSession() - Throws InvalidCredentialsException for anonymous user")
    void getCurrentSession_WithAnonymousUser_ThrowsInvalidCredentialsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentSession())
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Not authenticated");

        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCurrentSession() - Throws InvalidCredentialsException when user not found in database")
    void getCurrentSession_WithNonExistentUser_ThrowsInvalidCredentialsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(TEST_USER_ID);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userAccountRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentSession())
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("User not found");

        verify(userAccountRepository).findById(TEST_USER_ID);
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("logout() - Clears security context")
    void logout_ClearsSecurityContext() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(TEST_USER_ID);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        authService.logout();

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("logout() - Removes userId and facilityId from MDC")
    void logout_RemovesMDCContext() {
        // Arrange
        MDC.put("userId", String.valueOf(TEST_USER_ID));
        MDC.put("facilityId", String.valueOf(TEST_FACILITY_ID));
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(TEST_USER_ID);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        authService.logout();

        // Assert
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("facilityId")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID); // Should remain
    }

    @Test
    @DisplayName("logout() - Handles logout when no authentication exists")
    void logout_WithNoAuthentication_CompletesSuccessfully() {
        // Arrange
        SecurityContextHolder.clearContext();
        MDC.put("userId", String.valueOf(TEST_USER_ID));
        MDC.put("facilityId", String.valueOf(TEST_FACILITY_ID));

        // Act
        authService.logout();

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("facilityId")).isNull();
    }

    @Test
    @DisplayName("logout() - Handles logout when authentication is not authenticated")
    void logout_WithUnauthenticatedUser_CompletesSuccessfully() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        MDC.put("userId", String.valueOf(TEST_USER_ID));
        MDC.put("facilityId", String.valueOf(TEST_FACILITY_ID));

        // Act
        authService.logout();

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("facilityId")).isNull();
    }

    // ==================== TRANSACTIONAL BOUNDARY TESTS ====================

    @Test
    @DisplayName("login() - Verifies @Transactional annotation is present")
    void login_HasTransactionalAnnotation() throws NoSuchMethodException {
        // Assert
        assertThat(AuthService.class.getMethod("login", String.class, String.class, Long.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("getCurrentSession() - Verifies @Transactional(readOnly=true) annotation is present")
    void getCurrentSession_HasReadOnlyTransactionalAnnotation() throws NoSuchMethodException {
        // Assert
        Transactional annotation = AuthService.class.getMethod("getCurrentSession")
                .getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }

    // ==================== PASSWORD SECURITY TESTS ====================

    @Test
    @DisplayName("login() - Uses PasswordEncoder.matches() for secure password verification")
    void login_UsesPasswordEncoderForSecureVerification() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        // Act
        authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);

        // Assert
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_PASSWORD_HASH);
    }

    @Test
    @DisplayName("login() - Never stores or returns plain-text password")
    void login_NeverExposesPlainTextPassword() {
        // Arrange
        when(userAccountRepository.findByUsernameAndFacilityId(TEST_USERNAME, TEST_FACILITY_ID))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_HASH)).thenReturn(true);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);

        // Assert
        assertThat(response.toString()).doesNotContain(TEST_PASSWORD);
        
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userCaptor.capture());
        UserAccount savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo(TEST_PASSWORD_HASH);
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(TEST_PASSWORD);
    }
}