package com.visionary.roster.controller;

import com.visionary.roster.dto.LoginRequest;
import com.visionary.roster.dto.LoginResponse;
import com.visionary.roster.dto.SessionResponse;
import com.visionary.roster.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication operations.
 * 
 * <p>This controller handles user authentication, session management, and logout operations.
 * It delegates business logic to {@link AuthService} and returns appropriate {@link ResponseEntity} types.
 * 
 * <h2>Endpoint Patterns:</h2>
 * <ul>
 *   <li><strong>POST /api/v1/auth/login</strong> - Authenticates user credentials and returns session token</li>
 *   <li><strong>POST /api/v1/auth/logout</strong> - Invalidates current user session</li>
 *   <li><strong>GET /api/v1/auth/session</strong> - Retrieves current session information</li>
 * </ul>
 * 
 * <h2>Request Validation:</h2>
 * <ul>
 *   <li>Uses {@code @Valid} annotation for automatic DTO validation</li>
 *   <li>Validation errors are handled by global exception handler</li>
 *   <li>Returns 400 Bad Request for validation failures</li>
 * </ul>
 * 
 * <h2>Error Handling:</h2>
 * <ul>
 *   <li>Service exceptions are propagated to global exception handler</li>
 *   <li>Error responses align with ErrorResponse DTO structure</li>
 *   <li>Security-sensitive information is redacted from logs</li>
 *   <li>All errors include correlationId for traceability</li>
 * </ul>
 * 
 * <h2>Response Structure:</h2>
 * <ul>
 *   <li>Success responses return 200 OK with appropriate DTO</li>
 *   <li>LoginResponse includes userId, username, facilityId, role, and sessionToken</li>
 *   <li>SessionResponse includes userId, username, facilityId, and role</li>
 *   <li>Logout returns simple success message map</li>
 * </ul>
 * 
 * <h2>Security Logging:</h2>
 * <ul>
 *   <li>All authentication attempts are logged with correlationId and IP address</li>
 *   <li>Sensitive data (passwords) are never logged</li>
 *   <li>Failed attempts are logged with redacted username</li>
 *   <li>MDC context is properly cleaned up in finally blocks</li>
 * </ul>
 * 
 * <h2>Frontend Integration:</h2>
 * <ul>
 *   <li>Endpoint paths and contracts are stable to maintain compatibility with authApi.ts</li>
 *   <li>Response DTOs match TypeScript interfaces in frontend</li>
 *   <li>HTTP status codes follow REST conventions expected by frontend</li>
 * </ul>
 * 
 * <h2>Pattern Reference for StaffController:</h2>
 * <ul>
 *   <li>Use constructor injection for service dependencies</li>
 *   <li>Apply {@code @Valid} for request body validation</li>
 *   <li>Delegate business logic to service layer</li>
 *   <li>Return {@code ResponseEntity<T>} with appropriate HTTP status</li>
 *   <li>Use MDC for correlation tracking and structured logging</li>
 *   <li>Extract client IP from X-Forwarded-For or X-Real-IP headers</li>
 *   <li>Clean up MDC context in finally blocks</li>
 *   <li>Let global exception handler manage error responses</li>
 * </ul>
 * 
 * @see AuthService
 * @see LoginRequest
 * @see LoginResponse
 * @see SessionResponse
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger securityLogger = LoggerFactory.getLogger("com.visionary.roster.security");
    
    private final AuthService authService;

    /**
     * Constructs AuthController with required dependencies.
     * 
     * @param authService the authentication service for business logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates user credentials and establishes a session.
     * 
     * <p><strong>Endpoint:</strong> POST /api/v1/auth/login
     * 
     * <p><strong>Request Body:</strong> {@link LoginRequest} containing username, password, and facilityId
     * 
     * <p><strong>Success Response:</strong> 200 OK with {@link LoginResponse} containing session token
     * 
     * <p><strong>Error Responses:</strong>
     * <ul>
     *   <li>400 Bad Request - Invalid request format or validation failure</li>
     *   <li>401 Unauthorized - Invalid credentials</li>
     *   <li>500 Internal Server Error - Unexpected server error</li>
     * </ul>
     * 
     * @param loginRequest the login credentials (validated)
     * @param request the HTTP servlet request for IP extraction
     * @return ResponseEntity containing LoginResponse with session token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String ipAddress = getClientIpAddress(request);
        
        MDC.put("ipAddress", ipAddress);
        MDC.put("action", "LOGIN_ATTEMPT");

        try {
            securityLogger.info("Login attempt - correlationId: {}, username: {}, facilityId: {}, ipAddress: {}",
                    correlationId, loginRequest.getUsername(), loginRequest.getFacilityId(), ipAddress);

            LoginResponse response = authService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword(),
                    loginRequest.getFacilityId()
            );

            MDC.put("action", "LOGIN_SUCCESS");
            securityLogger.info("Login successful - correlationId: {}, userId: {}, facilityId: {}, ipAddress: {}",
                    correlationId, response.getUserId(), response.getFacilityId(), ipAddress);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            MDC.put("action", "LOGIN_FAILURE");
            securityLogger.error("Login failed - correlationId: {}, username: [REDACTED], facilityId: {}, ipAddress: {}, exception: {}",
                    correlationId, loginRequest.getFacilityId(), ipAddress, ex.getMessage());
            throw ex;
        } finally {
            MDC.remove("ipAddress");
            MDC.remove("action");
        }
    }

    /**
     * Invalidates the current user session.
     * 
     * <p><strong>Endpoint:</strong> POST /api/v1/auth/logout
     * 
     * <p><strong>Success Response:</strong> 200 OK with success message
     * 
     * <p><strong>Error Responses:</strong>
     * <ul>
     *   <li>401 Unauthorized - No active session</li>
     *   <li>500 Internal Server Error - Unexpected server error</li>
     * </ul>
     * 
     * @param request the HTTP servlet request for IP extraction
     * @return ResponseEntity containing success message map
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String ipAddress = getClientIpAddress(request);
        
        MDC.put("ipAddress", ipAddress);
        MDC.put("action", "LOGOUT");

        try {
            securityLogger.info("Logout attempt - correlationId: {}, ipAddress: {}", correlationId, ipAddress);

            authService.logout();

            securityLogger.info("Logout successful - correlationId: {}, ipAddress: {}", correlationId, ipAddress);

            return ResponseEntity.ok(Map.of("message", "Logout successful"));

        } catch (Exception ex) {
            securityLogger.error("Logout failed - correlationId: {}, ipAddress: {}, exception: {}",
                    correlationId, ipAddress, ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.remove("ipAddress");
            MDC.remove("action");
        }
    }

    /**
     * Retrieves current session information for the authenticated user.
     * 
     * <p><strong>Endpoint:</strong> GET /api/v1/auth/session
     * 
     * <p><strong>Success Response:</strong> 200 OK with {@link SessionResponse} containing user and facility info
     * 
     * <p><strong>Error Responses:</strong>
     * <ul>
     *   <li>401 Unauthorized - No active session or invalid token</li>
     *   <li>500 Internal Server Error - Unexpected server error</li>
     * </ul>
     * 
     * @param request the HTTP servlet request for IP extraction
     * @return ResponseEntity containing SessionResponse with current session data
     */
    @GetMapping("/session")
    public ResponseEntity<SessionResponse> getSession(HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String ipAddress = getClientIpAddress(request);
        
        MDC.put("ipAddress", ipAddress);
        MDC.put("action", "SESSION_CHECK");

        try {
            securityLogger.info("Session check - correlationId: {}, ipAddress: {}", correlationId, ipAddress);

            SessionResponse response = authService.getCurrentSession();

            securityLogger.info("Session check successful - correlationId: {}, userId: {}, facilityId: {}, ipAddress: {}",
                    correlationId, response.getUserId(), response.getFacilityId(), ipAddress);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            securityLogger.error("Session check failed - correlationId: {}, ipAddress: {}, exception: {}",
                    correlationId, ipAddress, ex.getMessage());
            throw ex;
        } finally {
            MDC.remove("ipAddress");
            MDC.remove("action");
        }
    }

    /**
     * Extracts the client IP address from the HTTP request.
     * 
     * <p>Checks headers in the following order:
     * <ol>
     *   <li>X-Forwarded-For (takes first IP if multiple)</li>
     *   <li>X-Real-IP</li>
     *   <li>Remote address from request</li>
     * </ol>
     * 
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}