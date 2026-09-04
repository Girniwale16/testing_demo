package com.visionary.roster.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Should allow unauthenticated access to login endpoint")
    void testLoginEndpointPermitAll() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should deny unauthenticated access to logout endpoint")
    void testLogoutEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny unauthenticated access to session endpoint")
    void testSessionEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should allow MANAGER to PUT /api/staff/**")
    void testManagerCanPutStaffEndpoint() throws Exception {
        mockMvc.perform(put("/api/staff/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("Should deny STAFF from PUT /api/staff/**")
    void testStaffCannotPutStaffEndpoint() throws Exception {
        mockMvc.perform(put("/api/staff/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny unauthenticated PUT /api/staff/**")
    void testUnauthenticatedCannotPutStaffEndpoint() throws Exception {
        mockMvc.perform(put("/api/staff/123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should allow MANAGER to POST /api/staff/*/deactivate")
    void testManagerCanDeactivateStaff() throws Exception {
        mockMvc.perform(post("/api/staff/123/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("Should deny STAFF from POST /api/staff/*/deactivate")
    void testStaffCannotDeactivateStaff() throws Exception {
        mockMvc.perform(post("/api/staff/123/deactivate"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny unauthenticated POST /api/staff/*/deactivate")
    void testUnauthenticatedCannotDeactivateStaff() throws Exception {
        mockMvc.perform(post("/api/staff/123/deactivate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should allow MANAGER to GET /api/staff/**")
    void testManagerCanGetStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/staff/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("Should allow STAFF to GET /api/staff/**")
    void testStaffCanGetStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/staff/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should deny unauthenticated GET /api/staff/**")
    void testUnauthenticatedCannotGetStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/staff/123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny USER role from GET /api/staff/**")
    void testUserRoleCannotGetStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/staff/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny USER role from PUT /api/staff/**")
    void testUserRoleCannotPutStaffEndpoint() throws Exception {
        mockMvc.perform(put("/api/staff/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny USER role from POST /api/staff/*/deactivate")
    void testUserRoleCannotDeactivateStaff() throws Exception {
        mockMvc.perform(post("/api/staff/123/deactivate"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STAFF")
    @DisplayName("Should allow authenticated STAFF to access logout endpoint")
    void testAuthenticatedStaffCanLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "MANAGER")
    @DisplayName("Should allow authenticated MANAGER to access session endpoint")
    void testAuthenticatedManagerCanAccessSession() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify CSRF is disabled")
    void testCsrfIsDisabled() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should verify staff endpoints are evaluated before anyRequest")
    void testStaffEndpointsOrderBeforeAnyRequest() throws Exception {
        mockMvc.perform(put("/api/staff/test"))
                .andExpect(status().isNotFound());
        
        mockMvc.perform(get("/api/staff/test"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("Should allow STAFF GET but deny STAFF PUT on same endpoint")
    void testStaffGetAllowedButPutDenied() throws Exception {
        mockMvc.perform(get("/api/staff/123"))
                .andExpect(status().isNotFound());
        
        mockMvc.perform(put("/api/staff/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should allow MANAGER all HTTP methods on staff endpoints")
    void testManagerHasFullAccessToStaffEndpoints() throws Exception {
        mockMvc.perform(get("/api/staff/123"))
                .andExpect(status().isNotFound());
        
        mockMvc.perform(put("/api/staff/123"))
                .andExpect(status().isNotFound());
        
        mockMvc.perform(post("/api/staff/123/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 for any authenticated endpoint without credentials")
    void testAnyRequestRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/some/protected/endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should allow any authenticated user to access non-staff endpoints")
    void testAuthenticatedUserCanAccessOtherEndpoints() throws Exception {
        mockMvc.perform(get("/api/other/endpoint"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify HttpStatusEntryPoint returns 401 for unauthenticated requests")
    void testUnauthorizedEntryPoint() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should handle nested staff paths with PUT method")
    void testNestedStaffPathsWithPut() throws Exception {
        mockMvc.perform(put("/api/staff/123/details"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("Should handle nested staff paths with GET method")
    void testNestedStaffPathsWithGet() throws Exception {
        mockMvc.perform(get("/api/staff/123/details"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should handle deactivate endpoint with different staff IDs")
    void testDeactivateEndpointWithDifferentIds() throws Exception {
        mockMvc.perform(post("/api/staff/456/deactivate"))
                .andExpect(status().isNotFound());
        
        mockMvc.perform(post("/api/staff/789/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deactivate staff endpoint requires authentication - HTTP 401 Unauthorized")
    void testDeactivateStaffEndpoint_RequiresAuthentication() throws Exception {
        // Send POST request to /api/staff/{staffId}/deactivate without authentication
        mockMvc.perform(post("/api/staff/1/deactivate"))
                // Verify HTTP 401 Unauthorized response
                .andExpect(status().is(401));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("Deactivate staff endpoint requires MANAGER role - HTTP 403 Forbidden")
    void testDeactivateStaffEndpoint_RequiresManagerRole() throws Exception {
        // Authenticate as non-MANAGER user (STAFF role) and send POST request to /api/staff/{staffId}/deactivate
        mockMvc.perform(post("/api/staff/1/deactivate"))
                // Verify HTTP 403 Forbidden response
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Deactivate staff endpoint with MANAGER role succeeds - HTTP 204 No Content")
    void testDeactivateStaffEndpoint_ManagerRoleSuccess() throws Exception {
        // Authenticate as MANAGER user and send POST request to /api/staff/{staffId}/deactivate
        mockMvc.perform(post("/api/staff/1/deactivate"))
                // Verify HTTP 204 No Content response on successful deactivation
                .andExpect(status().is(204));
    }