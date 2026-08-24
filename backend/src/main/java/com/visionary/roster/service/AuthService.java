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

@Service
public class AuthService {

    private static final Logger securityLogger = LoggerFactory.getLogger("com.visionary.roster.security");
    
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(String username, String password, Long facilityId) {
        String correlationId = MDC.get("correlationId");

        try {
            UserAccount user = userAccountRepository.findByUsernameAndFacilityId(username, facilityId)
                    .orElseThrow(() -> {
                        securityLogger.warn("Login failed - invalid credentials - correlationId: {}, username: [REDACTED], facilityId: {}, reason: user not found",
                                correlationId, facilityId);
                        return new InvalidCredentialsException("Invalid username or password");
                    });

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                securityLogger.warn("Login failed - invalid credentials - correlationId: {}, username: [REDACTED], facilityId: {}, reason: password mismatch",
                        correlationId, facilityId);
                throw new InvalidCredentialsException("Invalid username or password");
            }

            if (!user.getIsActive()) {
                securityLogger.warn("Login failed - inactive account - correlationId: {}, userId: {}, facilityId: {}",
                        correlationId, user.getUserAccountId(), facilityId);
                throw new InactiveAccountException("Account is inactive", user.getUserAccountId());
            }

            user.setLastLoginAt(LocalDateTime.now());
            userAccountRepository.save(user);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUserAccountId(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            MDC.put("userId", String.valueOf(user.getUserAccountId()));
            MDC.put("facilityId", String.valueOf(user.getFacility().getFacilityId()));

            securityLogger.info("Login successful - correlationId: {}, userId: {}, facilityId: {}, role: {}",
                    correlationId, user.getUserAccountId(), user.getFacility().getFacilityId(), user.getRole());

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