package com.visionary.roster;

import com.visionary.roster.dto.ErrorResponse;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.Staff;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.FacilityRepository;
import com.visionary.roster.repository.StaffRepository;
import com.visionary.roster.repository.UserAccountRepository;
import com.visionary.roster.security.FacilityScopingService;
import com.visionary.roster.security.RoleAuthorizationService;
import com.visionary.roster.service.StaffService;
import com.visionary.roster.audit.AuditEmitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        user.setUserAccountId(userId);
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
        user.setUserAccountId(userId);
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
        user.setUserAccountId(userId);
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
        doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();

        assertDoesNotThrow(() -> roleAuthorizationService.validateRole("MANAGER", "create_roster"));
    }

    @Test
    void validateRole_roleMismatch_throwsForbiddenAccessException() {
        Long userId = 1L;
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_STAFF");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();

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
        doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();

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
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();

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

@SpringBootTest
@AutoConfigureMockMvc
class StaffControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private StaffService staffService;

    @Autowired
    private RoleAuthorizationService roleAuthorizationService;

    @Autowired
    private FacilityScopingService facilityScopingService;

    @Autowired
    private AuditEmitter auditEmitter;

    private Facility facilityA;
    private Facility facilityB;
    private Staff testStaff;
    private UserAccount managerUser;
    private UserAccount nonManagerUser;
    private UserAccount managerUserFacilityB;

    @BeforeEach
    void setUp() {
        facilityA = new Facility();
        facilityA.setFacilityName("Facility A");
        facilityA = facilityRepository.save(facilityA);

        facilityB = new Facility();
        facilityB.setFacilityName("Facility B");
        facilityB = facilityRepository.save(facilityB);

        testStaff = new Staff();
        testStaff.setFirstName("John");
        testStaff.setLastName("Doe");
        testStaff.setEmail("john.doe@example.com");
        testStaff.setPhoneNumber("1234567890");
        testStaff.setRole("NURSE");
        testStaff.setFacility(facilityA);
        testStaff.setActive(true);
        testStaff = staffRepository.save(testStaff);

        managerUser = new UserAccount();
        managerUser.setUsername("manager");
        managerUser.setRole("MANAGER");
        managerUser.setFacility(facilityA);
        managerUser = userAccountRepository.save(managerUser);

        nonManagerUser = new UserAccount();
        nonManagerUser.setUsername("staff");
        nonManagerUser.setRole("STAFF");
        nonManagerUser.setFacility(facilityA);
        nonManagerUser = userAccountRepository.save(nonManagerUser);

        managerUserFacilityB = new UserAccount();
        managerUserFacilityB.setUsername("managerB");
        managerUserFacilityB.setRole("MANAGER");
        managerUserFacilityB.setFacility(facilityB);
        managerUserFacilityB = userAccountRepository.save(managerUserFacilityB);
    }

    @Test
    @Transactional
    void testApplicationContextLoadsWithAllBeans() {
        assertNotNull(staffService, "StaffService should be wired");
        assertNotNull(roleAuthorizationService, "RoleAuthorizationService should be wired");
        assertNotNull(facilityScopingService, "FacilityScopingService should be wired");
        assertNotNull(auditEmitter, "AuditEmitter should be wired");
        assertNotNull(staffRepository, "StaffRepository should be wired");
        assertNotNull(facilityRepository, "FacilityRepository should be wired");
        assertNotNull(userAccountRepository, "UserAccountRepository should be wired");
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_Success() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testUpdateStaff_Forbidden_NonManager() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(username = "managerB", roles = {"MANAGER"})
    void testUpdateStaff_Forbidden_CrossFacility() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_NotFound() throws Exception {
        Long nonExistentStaffId = 99999L;
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + nonExistentStaffId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_ValidationError() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"invalid-email\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDeactivateStaff_Success() throws Exception {
        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Staff deactivatedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertFalse(deactivatedStaff.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDeactivateStaff_Idempotent() throws Exception {
        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Staff deactivatedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertFalse(deactivatedStaff.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testDeactivateStaff_Forbidden_NonManager() throws Exception {
        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testListActiveStaff_ExcludesDeactivated() throws Exception {
        Staff activeStaff = new Staff();
        activeStaff.setFirstName("Active");
        activeStaff.setLastName("Staff");
        activeStaff.setEmail("active@example.com");
        activeStaff.setPhoneNumber("1111111111");
        activeStaff.setRole("NURSE");
        activeStaff.setFacility(facilityA);
        activeStaff.setActive(true);
        staffRepository.save(activeStaff);

        Staff deactivatedStaff = new Staff();
        deactivatedStaff.setFirstName("Deactivated");
        deactivatedStaff.setLastName("Staff");
        deactivatedStaff.setEmail("deactivated@example.com");
        deactivatedStaff.setPhoneNumber("2222222222");
        deactivatedStaff.setRole("NURSE");
        deactivatedStaff.setFacility(facilityA);
        deactivatedStaff.setActive(false);
        staffRepository.save(deactivatedStaff);

        mockMvc.perform(get("/api/staff")
                .param("facilityId", facilityA.getFacilityId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'active@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'deactivated@example.com')]").doesNotExist());
    }
}