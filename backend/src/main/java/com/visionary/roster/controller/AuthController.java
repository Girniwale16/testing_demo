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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger securityLogger = LoggerFactory.getLogger("com.visionary.roster.security");
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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