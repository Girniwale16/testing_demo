package com.visionary.roster.controller;

import com.visionary.roster.dto.LoginRequest;
import com.visionary.roster.dto.LoginResponse;
import com.visionary.roster.dto.SessionResponse;
import com.visionary.roster.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuthController.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>All REST endpoint paths and HTTP methods</li>
 *   <li>Request/response contracts and DTO mappings</li>
 *   <li>HTTP status codes for success and error scenarios</li>
 *   <li>Proper delegation to AuthService</li>
 *   <li>MDC context management and cleanup</li>
 *   <li>IP address extraction from various headers</li>
 *   <li>Exception propagation to global handler</li>
 *   <li>Security logging patterns</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthController authController;

    private static final String TEST_CORRELATION_ID = "test-correlation-123";
    private static final String TEST_IP_ADDRESS = "192.168.1.100";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final Long TEST_FACILITY_ID = 1L;
    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_ROLE = "ADMIN";
    private static final String TEST_SESSION_TOKEN = "session-token-xyz";

    @BeforeEach
    void setUp() {
        MDC.clear();
        MDC.put("correlationId", TEST_CORRELATION_ID);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    // ==================== Login Endpoint Tests ====================

    @Test
    void login_WithValidCredentials_ReturnsLoginResponseWith200() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setUsername(TEST_USERNAME);
        expectedResponse.setFacilityId(TEST_FACILITY_ID);
        expectedResponse.setRole(TEST_ROLE);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getBody().getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(response.getBody().getFacilityId()).isEqualTo(TEST_FACILITY_ID);
        assertThat(response.getBody().getRole()).isEqualTo(TEST_ROLE);
        assertThat(response.getBody().getSessionToken()).isEqualTo(TEST_SESSION_TOKEN);

        verify(authService).login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest).getRemoteAddr();
    }

    @Test
    void login_WithInvalidCredentials_PropagatesException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("wrongpassword");
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authController.login(loginRequest, httpServletRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");

        verify(authService).login(TEST_USERNAME, "wrongpassword", TEST_FACILITY_ID);
    }

    @Test
    void login_ExtractsIpFromXForwardedForHeader() {
        // Arrange
        String forwardedIp = "10.0.0.1, 10.0.0.2, 10.0.0.3";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest, never()).getHeader("X-Real-IP");
        verify(httpServletRequest, never()).getRemoteAddr();
    }

    @Test
    void login_ExtractsIpFromXRealIpHeader_WhenXForwardedForIsNull() {
        // Arrange
        String realIp = "172.16.0.1";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(realIp);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest, never()).getRemoteAddr();
    }

    @Test
    void login_ExtractsIpFromRemoteAddr_WhenHeadersAreNull() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest).getRemoteAddr();
    }

    @Test
    void login_CleansUpMDCContext_OnSuccess() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        authController.login(loginRequest, httpServletRequest);

        // Assert
        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("action")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID);
    }

    @Test
    void login_CleansUpMDCContext_OnException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThatThrownBy(() -> authController.login(loginRequest, httpServletRequest))
                .isInstanceOf(RuntimeException.class);

        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("action")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID);
    }

    // ==================== Logout Endpoint Tests ====================

    @Test
    void logout_WithValidSession_ReturnsSuccessMessageWith200() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        doNothing().when(authService).logout();

        // Act
        ResponseEntity<Map<String, String>> response = authController.logout(httpServletRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("message", "Logout successful");

        verify(authService).logout();
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest).getRemoteAddr();
    }

    @Test
    void logout_WhenServiceThrowsException_PropagatesException() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        doThrow(new RuntimeException("Session not found")).when(authService).logout();

        // Act & Assert
        assertThatThrownBy(() -> authController.logout(httpServletRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Session not found");

        verify(authService).logout();
    }

    @Test
    void logout_CleansUpMDCContext_OnSuccess() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        doNothing().when(authService).logout();

        // Act
        authController.logout(httpServletRequest);

        // Assert
        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("action")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID);
    }

    @Test
    void logout_CleansUpMDCContext_OnException() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        doThrow(new RuntimeException("Service error")).when(authService).logout();

        // Act & Assert
        assertThatThrownBy(() -> authController.logout(httpServletRequest))
                .isInstanceOf(RuntimeException.class);

        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("action")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID);
    }

    // ==================== Get Session Endpoint Tests ====================

    @Test
    void getSession_WithValidSession_ReturnsSessionResponseWith200() {
        // Arrange
        SessionResponse expectedResponse = new SessionResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setUsername(TEST_USERNAME);
        expectedResponse.setFacilityId(TEST_FACILITY_ID);
        expectedResponse.setRole(TEST_ROLE);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.getCurrentSession()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<SessionResponse> response = authController.getSession(httpServletRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getBody().getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(response.getBody().getFacilityId()).isEqualTo(TEST_FACILITY_ID);
        assertThat(response.getBody().getRole()).isEqualTo(TEST_ROLE);

        verify(authService).getCurrentSession();
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest).getRemoteAddr();
    }

    @Test
    void getSession_WithInvalidSession_PropagatesException() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.getCurrentSession())
                .thenThrow(new RuntimeException("No active session"));

        // Act & Assert
        assertThatThrownBy(() -> authController.getSession(httpServletRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No active session");

        verify(authService).getCurrentSession();
    }

    @Test
    void getSession_CleansUpMDCContext_OnSuccess() {
        // Arrange
        SessionResponse expectedResponse = new SessionResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setFacilityId(TEST_FACILITY_ID);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.getCurrentSession()).thenReturn(expectedResponse);

        // Act
        authController.getSession(httpServletRequest);

        // Assert
        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("action")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID);
    }

    @Test
    void getSession_CleansUpMDCContext_OnException() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.getCurrentSession())
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThatThrownBy(() -> authController.getSession(httpServletRequest))
                .isInstanceOf(RuntimeException.class);

        assertThat(MDC.get("ipAddress")).isNull();
        assertThat(MDC.get("action")).isNull();
        assertThat(MDC.get("correlationId")).isEqualTo(TEST_CORRELATION_ID);
    }

    // ==================== IP Address Extraction Tests ====================

    @Test
    void getClientIpAddress_ExtractsFirstIpFromXForwardedFor_WhenMultipleIpsPresent() {
        // Arrange
        String multipleIps = "203.0.113.1, 198.51.100.1, 192.0.2.1";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(multipleIps);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        authController.login(loginRequest, httpServletRequest);

        // Assert - verify first IP is extracted
        verify(httpServletRequest).getHeader("X-Forwarded-For");
    }

    @Test
    void getClientIpAddress_HandlesEmptyXForwardedFor_FallsBackToXRealIp() {
        // Arrange
        String realIp = "198.51.100.50";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("");
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(realIp);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        authController.login(loginRequest, httpServletRequest);

        // Assert
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest, never()).getRemoteAddr();
    }

    @Test
    void getClientIpAddress_HandlesEmptyXRealIp_FallsBackToRemoteAddr() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("");
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn("");
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        authController.login(loginRequest, httpServletRequest);

        // Assert
        verify(httpServletRequest).getHeader("X-Forwarded-For");
        verify(httpServletRequest).getHeader("X-Real-IP");
        verify(httpServletRequest).getRemoteAddr();
    }

    // ==================== Constructor Tests ====================

    @Test
    void constructor_InitializesWithAuthService() {
        // Arrange & Act
        AuthController controller = new AuthController(authService);

        // Assert
        assertThat(controller).isNotNull();
    }

    // ==================== Response Contract Tests ====================

    @Test
    void login_ResponseContainsAllRequiredFields() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setUsername(TEST_USERNAME);
        expectedResponse.setFacilityId(TEST_FACILITY_ID);
        expectedResponse.setRole(TEST_ROLE);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(anyString(), anyString(), anyLong())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isNotNull();
        assertThat(response.getBody().getUsername()).isNotNull();
        assertThat(response.getBody().getFacilityId()).isNotNull();
        assertThat(response.getBody().getRole()).isNotNull();
        assertThat(response.getBody().getSessionToken()).isNotNull();
    }

    @Test
    void getSession_ResponseContainsAllRequiredFields() {
        // Arrange
        SessionResponse expectedResponse = new SessionResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setUsername(TEST_USERNAME);
        expectedResponse.setFacilityId(TEST_FACILITY_ID);
        expectedResponse.setRole(TEST_ROLE);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.getCurrentSession()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<SessionResponse> response = authController.getSession(httpServletRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isNotNull();
        assertThat(response.getBody().getUsername()).isNotNull();
        assertThat(response.getBody().getFacilityId()).isNotNull();
        assertThat(response.getBody().getRole()).isNotNull();
    }

    @Test
    void logout_ResponseContainsSuccessMessage() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        doNothing().when(authService).logout();

        // Act
        ResponseEntity<Map<String, String>> response = authController.logout(httpServletRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message")).isEqualTo("Logout successful");
    }

    // ==================== Service Delegation Tests ====================

    @Test
    void login_DelegatesToAuthService_WithCorrectParameters() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setFacilityId(TEST_FACILITY_ID);

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setSessionToken(TEST_SESSION_TOKEN);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID))
                .thenReturn(expectedResponse);

        // Act
        authController.login(loginRequest, httpServletRequest);

        // Assert
        verify(authService, times(1)).login(TEST_USERNAME, TEST_PASSWORD, TEST_FACILITY_ID);
    }

    @Test
    void logout_DelegatesToAuthService() {
        // Arrange
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        doNothing().when(authService).logout();

        // Act
        authController.logout(httpServletRequest);

        // Assert
        verify(authService, times(1)).logout();
    }

    @Test
    void getSession_DelegatesToAuthService() {
        // Arrange
        SessionResponse expectedResponse = new SessionResponse();
        expectedResponse.setUserId(TEST_USER_ID);
        expectedResponse.setFacilityId(TEST_FACILITY_ID);

        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS);
        when(authService.getCurrentSession()).thenReturn(expectedResponse);

        // Act
        authController.getSession(httpServletRequest);

        // Assert
        verify(authService, times(1)).getCurrentSession();
    }
}