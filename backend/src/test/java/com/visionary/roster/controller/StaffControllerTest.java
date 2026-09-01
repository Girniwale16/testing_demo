package com.visionary.roster.controller;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StaffController staffController;

    private static final Long TEST_USER_ID = 100L;
    private static final Long TEST_FACILITY_ID = 200L;
    private static final Long TEST_STAFF_ID = 300L;
    private static final String TEST_CORRELATION_ID = "test-correlation-123";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(TEST_USER_ID);
    }

    @Test
    void createStaff_shouldReturnCreatedStatusWithLocationHeader() {
        // Arrange
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFacilityId(TEST_FACILITY_ID);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setEmail("john.doe@example.com");

        when(staffService.createStaff(request, TEST_FACILITY_ID, TEST_USER_ID)).thenReturn(response);

        // Act
        ResponseEntity<StaffResponse> result = staffController.createStaff(request, TEST_CORRELATION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(TEST_STAFF_ID, result.getBody().getId());
        assertNotNull(result.getHeaders().getLocation());
        assertTrue(result.getHeaders().getLocation().toString().contains("/api/staff/" + TEST_STAFF_ID));
        
        verify(staffService, times(1)).createStaff(request, TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void createStaff_shouldExtractUserIdFromSecurityContext() {
        // Arrange
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFacilityId(TEST_FACILITY_ID);

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.createStaff(any(), anyLong(), anyLong())).thenReturn(response);

        // Act
        staffController.createStaff(request, TEST_CORRELATION_ID);

        // Assert
        verify(authentication, times(1)).getPrincipal();
        verify(staffService, times(1)).createStaff(request, TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void createStaff_shouldExtractFacilityIdFromRequestBody() {
        // Arrange
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFacilityId(TEST_FACILITY_ID);

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.createStaff(any(), anyLong(), anyLong())).thenReturn(response);

        // Act
        staffController.createStaff(request, TEST_CORRELATION_ID);

        // Assert
        verify(staffService, times(1)).createStaff(request, TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void updateStaff_shouldReturnOkStatusWithUpdatedStaff() {
        // Arrange
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);
        response.setFirstName("Jane");
        response.setLastName("Smith");

        when(staffService.updateStaff(TEST_STAFF_ID, request, TEST_USER_ID)).thenReturn(response);

        // Act
        ResponseEntity<StaffResponse> result = staffController.updateStaff(TEST_STAFF_ID, request, TEST_CORRELATION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(TEST_STAFF_ID, result.getBody().getId());
        assertEquals("Jane", result.getBody().getFirstName());
        assertEquals("Smith", result.getBody().getLastName());
        
        verify(staffService, times(1)).updateStaff(TEST_STAFF_ID, request, TEST_USER_ID);
    }

    @Test
    void updateStaff_shouldExtractUserIdFromSecurityContext() {
        // Arrange
        UpdateStaffRequest request = new UpdateStaffRequest();
        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.updateStaff(anyLong(), any(), anyLong())).thenReturn(response);

        // Act
        staffController.updateStaff(TEST_STAFF_ID, request, TEST_CORRELATION_ID);

        // Assert
        verify(authentication, times(1)).getPrincipal();
        verify(staffService, times(1)).updateStaff(TEST_STAFF_ID, request, TEST_USER_ID);
    }

    @Test
    void getStaff_shouldReturnOkStatusWithStaffDetails() {
        // Arrange
        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);
        response.setFirstName("John");
        response.setLastName("Doe");

        when(staffService.getStaff(TEST_STAFF_ID, TEST_USER_ID)).thenReturn(response);

        // Act
        ResponseEntity<StaffResponse> result = staffController.getStaff(TEST_STAFF_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(TEST_STAFF_ID, result.getBody().getId());
        assertEquals("John", result.getBody().getFirstName());
        
        verify(staffService, times(1)).getStaff(TEST_STAFF_ID, TEST_USER_ID);
    }

    @Test
    void getStaff_shouldExtractUserIdFromSecurityContext() {
        // Arrange
        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.getStaff(anyLong(), anyLong())).thenReturn(response);

        // Act
        staffController.getStaff(TEST_STAFF_ID);

        // Assert
        verify(authentication, times(1)).getPrincipal();
        verify(staffService, times(1)).getStaff(TEST_STAFF_ID, TEST_USER_ID);
    }

    @Test
    void listStaff_shouldReturnOkStatusWithStaffList() {
        // Arrange
        StaffResponse staff1 = new StaffResponse();
        staff1.setId(1L);
        staff1.setFirstName("John");

        StaffResponse staff2 = new StaffResponse();
        staff2.setId(2L);
        staff2.setFirstName("Jane");

        List<StaffResponse> staffList = Arrays.asList(staff1, staff2);

        when(staffService.listStaff(TEST_FACILITY_ID, TEST_USER_ID)).thenReturn(staffList);

        // Act
        ResponseEntity<List<StaffResponse>> result = staffController.listStaff(TEST_FACILITY_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals("John", result.getBody().get(0).getFirstName());
        assertEquals("Jane", result.getBody().get(1).getFirstName());
        
        verify(staffService, times(1)).listStaff(TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void listStaff_shouldExtractUserIdFromSecurityContext() {
        // Arrange
        List<StaffResponse> staffList = Arrays.asList(new StaffResponse());

        when(staffService.listStaff(anyLong(), anyLong())).thenReturn(staffList);

        // Act
        staffController.listStaff(TEST_FACILITY_ID);

        // Assert
        verify(authentication, times(1)).getPrincipal();
        verify(staffService, times(1)).listStaff(TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void listStaff_shouldReturnEmptyListWhenNoStaffFound() {
        // Arrange
        List<StaffResponse> emptyList = Arrays.asList();

        when(staffService.listStaff(TEST_FACILITY_ID, TEST_USER_ID)).thenReturn(emptyList);

        // Act
        ResponseEntity<List<StaffResponse>> result = staffController.listStaff(TEST_FACILITY_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        
        verify(staffService, times(1)).listStaff(TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void deactivateStaff_shouldReturnOkStatus() {
        // Arrange
        doNothing().when(staffService).deactivateStaff(TEST_STAFF_ID, TEST_USER_ID);

        // Act
        ResponseEntity<Void> result = staffController.deactivateStaff(TEST_STAFF_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
        
        verify(staffService, times(1)).deactivateStaff(TEST_STAFF_ID, TEST_USER_ID);
    }

    @Test
    void deactivateStaff_shouldExtractUserIdFromSecurityContext() {
        // Arrange
        doNothing().when(staffService).deactivateStaff(anyLong(), anyLong());

        // Act
        staffController.deactivateStaff(TEST_STAFF_ID);

        // Assert
        verify(authentication, times(1)).getPrincipal();
        verify(staffService, times(1)).deactivateStaff(TEST_STAFF_ID, TEST_USER_ID);
    }

    @Test
    void listActiveStaff_shouldReturnOkStatusWithActiveStaffList() {
        // Arrange
        StaffResponse staff1 = new StaffResponse();
        staff1.setId(1L);
        staff1.setFirstName("Active1");
        staff1.setActive(true);

        StaffResponse staff2 = new StaffResponse();
        staff2.setId(2L);
        staff2.setFirstName("Active2");
        staff2.setActive(true);

        List<StaffResponse> activeStaffList = Arrays.asList(staff1, staff2);

        when(staffService.listActiveStaff(TEST_FACILITY_ID, TEST_USER_ID)).thenReturn(activeStaffList);

        // Act
        ResponseEntity<List<StaffResponse>> result = staffController.listActiveStaff(TEST_FACILITY_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertTrue(result.getBody().get(0).isActive());
        assertTrue(result.getBody().get(1).isActive());
        
        verify(staffService, times(1)).listActiveStaff(TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void listActiveStaff_shouldExtractUserIdFromSecurityContext() {
        // Arrange
        List<StaffResponse> activeStaffList = Arrays.asList(new StaffResponse());

        when(staffService.listActiveStaff(anyLong(), anyLong())).thenReturn(activeStaffList);

        // Act
        staffController.listActiveStaff(TEST_FACILITY_ID);

        // Assert
        verify(authentication, times(1)).getPrincipal();
        verify(staffService, times(1)).listActiveStaff(TEST_FACILITY_ID, TEST_USER_ID);
    }

    @Test
    void createStaff_shouldCallServiceWithCorrectParameters() {
        // Arrange
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFacilityId(TEST_FACILITY_ID);
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.createStaff(request, TEST_FACILITY_ID, TEST_USER_ID)).thenReturn(response);

        // Act
        staffController.createStaff(request, TEST_CORRELATION_ID);

        // Assert
        verify(staffService, times(1)).createStaff(
            argThat(req -> req.getFirstName().equals("Test") && 
                          req.getLastName().equals("User") && 
                          req.getEmail().equals("test@example.com")),
            eq(TEST_FACILITY_ID),
            eq(TEST_USER_ID)
        );
    }

    @Test
    void updateStaff_shouldCallServiceWithCorrectParameters() {
        // Arrange
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.updateStaff(TEST_STAFF_ID, request, TEST_USER_ID)).thenReturn(response);

        // Act
        staffController.updateStaff(TEST_STAFF_ID, request, TEST_CORRELATION_ID);

        // Assert
        verify(staffService, times(1)).updateStaff(
            eq(TEST_STAFF_ID),
            argThat(req -> req.getFirstName().equals("Updated") && 
                          req.getLastName().equals("Name")),
            eq(TEST_USER_ID)
        );
    }

    @Test
    void createStaff_shouldBuildCorrectLocationUri() {
        // Arrange
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFacilityId(TEST_FACILITY_ID);

        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);

        when(staffService.createStaff(any(), anyLong(), anyLong())).thenReturn(response);

        // Act
        ResponseEntity<StaffResponse> result = staffController.createStaff(request, TEST_CORRELATION_ID);

        // Assert
        URI location = result.getHeaders().getLocation();
        assertNotNull(location);
        assertTrue(location.toString().endsWith("/api/staff/" + TEST_STAFF_ID));
    }

    @Test
    void allEndpoints_shouldExtractUserIdFromSecurityContextCorrectly() {
        // Arrange
        CreateStaffRequest createRequest = new CreateStaffRequest();
        createRequest.setFacilityId(TEST_FACILITY_ID);
        UpdateStaffRequest updateRequest = new UpdateStaffRequest();
        StaffResponse response = new StaffResponse();
        response.setId(TEST_STAFF_ID);
        List<StaffResponse> staffList = Arrays.asList(response);

        when(staffService.createStaff(any(), anyLong(), anyLong())).thenReturn(response);
        when(staffService.updateStaff(anyLong(), any(), anyLong())).thenReturn(response);
        when(staffService.getStaff(anyLong(), anyLong())).thenReturn(response);
        when(staffService.listStaff(anyLong(), anyLong())).thenReturn(staffList);
        when(staffService.listActiveStaff(anyLong(), anyLong())).thenReturn(staffList);
        doNothing().when(staffService).deactivateStaff(anyLong(), anyLong());

        // Act
        staffController.createStaff(createRequest, TEST_CORRELATION_ID);
        staffController.updateStaff(TEST_STAFF_ID, updateRequest, TEST_CORRELATION_ID);
        staffController.getStaff(TEST_STAFF_ID);
        staffController.listStaff(TEST_FACILITY_ID);
        staffController.listActiveStaff(TEST_FACILITY_ID);
        staffController.deactivateStaff(TEST_STAFF_ID);

        // Assert - verify authentication.getPrincipal() was called 6 times (once per endpoint)
        verify(authentication, times(6)).getPrincipal();
    }
}