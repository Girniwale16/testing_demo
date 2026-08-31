package com.visionary.roster.service;

import com.visionary.roster.dto.LoginResponse;
import com.visionary.roster.dto.SessionResponse;
import com.visionary.roster.exception.InactiveAccountException;
import com.visionary.roster.exception.InvalidCredentialsException;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Authentication Service
 * 
 * Handles authentication business logic including login validation, session management,
 * and secure password verification. This service properly throws InvalidCredentialsException
 * and InactiveAccountException which are mapped by GlobalExceptionHandler to 401/403 responses.
 * 
 * Service Layer Patterns (to be replicated in StaffService):
 * - Constructor-based dependency injection
 * - Transactional boundaries with @Transactional annotations
 * - Comprehensive security logging with correlation IDs
 * - Proper exception handling with custom exceptions
 * - MDC context management for distributed tracing
 * - Password encoding using Spring Security's PasswordEncoder
 * 
 * Security Best Practices:
 * - Passwords are verified using BCrypt via PasswordEncoder (never stored or logged in plain text)
 * - Failed login attempts are logged without exposing sensitive data
 * - Account status validation prevents inactive users from authenticating
 * - Authentication state is managed via Spring Security Context
 */
@Service
public class AuthService {

    private static final Logger securityLogger = LoggerFactory.getLogger("com.visionary.roster.security");
    
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user with username, password, and facility context.
     * 
     * @param username the username
     * @param password the plain-text password (verified securely using BCrypt)
     * @param facilityId the facility context for multi-tenant authentication
     * @return LoginResponse containing user details and session information
     * @throws InvalidCredentialsException if username/password is invalid (mapped to 401 by GlobalExceptionHandler)
     * @throws InactiveAccountException if account is inactive (mapped to 403 by GlobalExceptionHandler)
     */
    @Transactional
    public LoginResponse login(String username, String password, Long facilityId) {
        String correlationId = MDC.get("correlationId");

        try {
            // Step 1: Retrieve user by username and facility
            UserAccount user = userAccountRepository.findByUsernameAndFacilityId(username, facilityId)
                    .orElseThrow(() -> {
                        securityLogger.warn("Login failed - invalid credentials - correlationId: {}, username: [REDACTED], facilityId: {}, reason: user not found",
                                correlationId, facilityId);
                        return new InvalidCredentialsException("Invalid username or password");
                    });

            // Step 2: Verify password using secure BCrypt matching (best practice)
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                securityLogger.warn("Login failed - invalid credentials - correlationId: {}, username: [REDACTED], facilityId: {}, reason: password mismatch",
                        correlationId, facilityId);
                throw new InvalidCredentialsException("Invalid username or password");
            }

            // Step 3: Validate account is active
            if (!user.getIsActive()) {
                securityLogger.warn("Login failed - inactive account - correlationId: {}, userId: {}, facilityId: {}",
                        correlationId, user.getUserAccountId(), facilityId);
                throw new InactiveAccountException("Account is inactive", user.getUserAccountId());
            }

            // Step 4: Update last login timestamp
            user.setLastLoginAt(LocalDateTime.now());
            userAccountRepository.save(user);

            // Step 5: Establish Spring Security authentication context
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUserAccountId(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Step 6: Populate MDC for distributed tracing
            MDC.put("userId", String.valueOf(user.getUserAccountId()));
            MDC.put("facilityId", String.valueOf(user.getFacility().getFacilityId()));

            securityLogger.info("Login successful - correlationId: {}, userId: {}, facilityId: {}, role: {}",
                    correlationId, user.getUserAccountId(), user.getFacility().getFacilityId(), user.getRole());

            // Step 7: Return login response
            return LoginResponse.builder()
                    .userId(user.getUserAccountId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .facilityId(user.getFacility().getFacilityId())
                    .facilityName(user.getFacility().getName())
                    .message("Login successful")
                    .build();

        } catch (InvalidCredentialsException | InactiveAccountException ex) {
            securityLogger.error("Authentication exception - correlationId: {}, exception: {}", correlationId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            securityLogger.error("Unexpected error during login - correlationId: {}, exception: {}", correlationId, ex.getMessage(), ex);
            throw new RuntimeException("An unexpected error occurred during login", ex);
        }
    }

    /**
     * Retrieves the current authenticated user's session information.
     * 
     * @return SessionResponse containing current user session details
     * @throws InvalidCredentialsException if user is not authenticated (mapped to 401 by GlobalExceptionHandler)
     */
    @Transactional(readOnly = true)
    public SessionResponse getCurrentSession() {
        String correlationId = MDC.get("correlationId");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            securityLogger.warn("Session check failed - not authenticated - correlationId: {}", correlationId);
            throw new InvalidCredentialsException("Not authenticated");
        }

        Long userId = (Long) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    securityLogger.warn("Session check failed - user not found - correlationId: {}, userId: {}", correlationId, userId);
                    return new InvalidCredentialsException("User not found");
                });

        securityLogger.info("Session check successful - correlationId: {}, userId: {}, facilityId: {}",
                correlationId, user.getUserAccountId(), user.getFacility().getFacilityId());

        return SessionResponse.builder()
                .userId(user.getUserAccountId())
                .username(user.getUsername())
                .role(user.getRole())
                .facilityId(user.getFacility().getFacilityId())
                .facilityName(user.getFacility().getName())
                .isActive(user.getIsActive())
                .build();
    }

    /**
     * Logs out the current user by clearing the security context and MDC.
     * Method signature maintained for AuthController compatibility.
     */
    public void logout() {
        String correlationId = MDC.get("correlationId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Long userId = (Long) authentication.getPrincipal();
            securityLogger.info("Logout successful - correlationId: {}, userId: {}", correlationId, userId);
        }

        SecurityContextHolder.clearContext();
        MDC.remove("userId");
        MDC.remove("facilityId");
    }
}