package com.visionary.roster;

import com.visionary.roster.controller.AuthController;
import com.visionary.roster.dto.LoginRequest;
import com.visionary.roster.dto.LoginResponse;
import com.visionary.roster.dto.SessionResponse;
import com.visionary.roster.exception.InactiveAccountException;
import com.visionary.roster.exception.InvalidCredentialsException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.UserAccountRepository;
import com.visionary.roster.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private Facility testFacility;
    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        testFacility = new Facility();
        testFacility.setFacilityId(1L);
        testFacility.setName("Test Facility");
        testFacility.setTimezone("America/New_York");
        testFacility.setRegionCode("US-EAST");
        testFacility.setIsActive(true);
        testFacility.setCreatedAt(LocalDateTime.now());
        testFacility.setCreatedBy("system");

        testUser = new UserAccount();
        testUser.setUserAccountId(1L);
        testUser.setFacility(testFacility);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setRole("MANAGER");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setCreatedBy("system");
    }

    @Test
    void postLogin_validCredentials_returns200() throws Exception {
        when(userAccountRepository.findByUsernameAndFacilityId("testuser", 1L))
                .thenReturn(Optional.of(testUser));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password123\",\"facilityId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.facilityId").value(1))
                .andExpect(jsonPath("$.facilityName").value("Test Facility"))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(userAccountRepository, times(1)).findByUsernameAndFacilityId("testuser", 1L);
        verify(userAccountRepository, times(1)).save(any(UserAccount.class));
    }

    @Test
    void postLogin_invalidCredentials_returns401() throws Exception {
        when(userAccountRepository.findByUsernameAndFacilityId(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wronguser\",\"password\":\"wrongpass\",\"facilityId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(userAccountRepository, times(1)).findByUsernameAndFacilityId("wronguser", 1L);
    }

    @Test
    void postLogin_inactiveAccount_returns403() throws Exception {
        testUser.setIsActive(false);
        when(userAccountRepository.findByUsernameAndFacilityId("testuser", 1L))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password123\",\"facilityId\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_INACTIVE"))
                .andExpect(jsonPath("$.message").value("Account is inactive"));

        verify(userAccountRepository, times(1)).findByUsernameAndFacilityId("testuser", 1L);
    }

    @Test
    void postLogin_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"password123\",\"facilityId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = "1")
    void postLogout_authenticated_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void postLogout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1")
    void getSession_authenticated_returns200() throws Exception {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.facilityId").value(1))
                .andExpect(jsonPath("$.facilityName").value("Test Facility"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userAccountRepository, times(1)).findById(1L);
    }

    @Test
    void getSession_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authService_login_validCredentials_updatesLastLoginAt() {
        when(userAccountRepository.findByUsernameAndFacilityId("testuser", 1L))
                .thenReturn(Optional.of(testUser));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(testUser);

        LoginResponse response = authService.login("testuser", "password123", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("MANAGER", response.getRole());
        verify(userAccountRepository, times(1)).save(any(UserAccount.class));
    }

    @Test
    void authService_login_invalidPassword_throwsInvalidCredentialsException() {
        when(userAccountRepository.findByUsernameAndFacilityId("testuser", 1L))
                .thenReturn(Optional.of(testUser));

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login("testuser", "wrongpassword", 1L);
        });
    }

    @Test
    void authService_login_inactiveAccount_throwsInactiveAccountException() {
        testUser.setIsActive(false);
        when(userAccountRepository.findByUsernameAndFacilityId("testuser", 1L))
                .thenReturn(Optional.of(testUser));

        assertThrows(InactiveAccountException.class, () -> {
            authService.login("testuser", "password123", 1L);
        });
    }

    @Test
    void authService_login_userNotFound_throwsInvalidCredentialsException() {
        when(userAccountRepository.findByUsernameAndFacilityId("nonexistent", 1L))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login("nonexistent", "password123", 1L);
        });
    }
}