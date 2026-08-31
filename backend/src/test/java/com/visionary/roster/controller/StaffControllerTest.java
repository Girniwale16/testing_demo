package com.visionary.roster.controller;

import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
import com.visionary.roster.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StaffController.
 * Tests all REST endpoints for staff management operations.
 */
@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private StaffController staffController;

    private StaffResponse mockStaffResponse;
    private StaffUpdateRequest mockUpdateRequest;
    private static final Long STAFF_ID = 1L;
    private static final Long FACILITY_ID = 100L;
    private static final String REQUESTING_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        mockStaffResponse = new StaffResponse();
        mockStaffResponse.setId(STAFF_ID);
        mockStaffResponse.setName("John Doe");
        mockStaffResponse.setActive(true);

        mockUpdateRequest = new StaffUpdateRequest();
        mockUpdateRequest.setName("John Doe Updated");
        mockUpdateRequest.setEmail("john.doe@example.com");

        when(userDetails.getUsername()).thenReturn(REQUESTING_USER_ID);
    }

    // ==================== updateStaff Tests ====================

    @Test
    void updateStaff_Success_ReturnsOkWithStaffResponse() {
        // Arrange
        when(staffService.updateStaff(eq(STAFF_ID), eq(mockUpdateRequest), eq(REQUESTING_USER_ID)))
                .thenReturn(mockStaffResponse);

        // Act
        ResponseEntity<StaffResponse> response = staffController.updateStaff(STAFF_ID, mockUpdateRequest, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(STAFF_ID, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        verify(staffService, times(1)).updateStaff(eq(STAFF_ID), eq(mockUpdateRequest), eq(REQUESTING_USER_ID));
        verify(userDetails, times(1)).getUsername();
    }

    @Test
    void updateStaff_ExtractsCorrectUserIdFromUserDetails() {
        // Arrange
        when(staffService.updateStaff(anyLong(), any(StaffUpdateRequest.class), anyString()))
                .thenReturn(mockStaffResponse);

        // Act
        staffController.updateStaff(STAFF_ID, mockUpdateRequest, userDetails);

        // Assert
        verify(userDetails, times(1)).getUsername();
        verify(staffService, times(1)).updateStaff(eq(STAFF_ID), eq(mockUpdateRequest), eq(REQUESTING_USER_ID));
    }

    @Test
    void updateStaff_CallsServiceWithCorrectParameters() {
        // Arrange
        when(staffService.updateStaff(eq(STAFF_ID), eq(mockUpdateRequest), eq(REQUESTING_USER_ID)))
                .thenReturn(mockStaffResponse);

        // Act
        staffController.updateStaff(STAFF_ID, mockUpdateRequest, userDetails);

        // Assert
        verify(staffService, times(1)).updateStaff(
                eq(STAFF_ID),
                eq(mockUpdateRequest),
                eq(REQUESTING_USER_ID)
        );
    }

    @Test
    void updateStaff_ServiceThrowsException_ExceptionPropagates() {
        // Arrange
        when(staffService.updateStaff(anyLong(), any(StaffUpdateRequest.class), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffController.updateStaff(STAFF_ID, mockUpdateRequest, userDetails);
        });
        verify(staffService, times(1)).updateStaff(eq(STAFF_ID), eq(mockUpdateRequest), eq(REQUESTING_USER_ID));
    }

    // ==================== deactivateStaff Tests ====================

    @Test
    void deactivateStaff_Success_ReturnsOkWithNoContent() {
        // Arrange
        doNothing().when(staffService).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));

        // Act
        ResponseEntity<Void> response = staffController.deactivateStaff(STAFF_ID, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(staffService, times(1)).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));
        verify(userDetails, times(1)).getUsername();
    }

    @Test
    void deactivateStaff_ExtractsCorrectUserIdFromUserDetails() {
        // Arrange
        doNothing().when(staffService).deactivateStaff(anyLong(), anyString());

        // Act
        staffController.deactivateStaff(STAFF_ID, userDetails);

        // Assert
        verify(userDetails, times(1)).getUsername();
        verify(staffService, times(1)).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));
    }

    @Test
    void deactivateStaff_CallsServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(staffService).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));

        // Act
        staffController.deactivateStaff(STAFF_ID, userDetails);

        // Assert
        verify(staffService, times(1)).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));
    }

    @Test
    void deactivateStaff_IdempotentOperation_ReturnsOk() {
        // Arrange - Service handles idempotency, controller just returns OK
        doNothing().when(staffService).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));

        // Act
        ResponseEntity<Void> response = staffController.deactivateStaff(STAFF_ID, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService, times(1)).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));
    }

    @Test
    void deactivateStaff_ServiceThrowsException_ExceptionPropagates() {
        // Arrange
        doThrow(new RuntimeException("Deactivation error"))
                .when(staffService).deactivateStaff(anyLong(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffController.deactivateStaff(STAFF_ID, userDetails);
        });
        verify(staffService, times(1)).deactivateStaff(eq(STAFF_ID), eq(REQUESTING_USER_ID));
    }

    // ==================== getStaff Tests ====================

    @Test
    void getStaff_Success_ReturnsOkWithStaffResponse() {
        // Arrange
        when(staffService.getStaff(eq(STAFF_ID))).thenReturn(mockStaffResponse);

        // Act
        ResponseEntity<StaffResponse> response = staffController.getStaff(STAFF_ID, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(STAFF_ID, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        assertTrue(response.getBody().isActive());
        verify(staffService, times(1)).getStaff(eq(STAFF_ID));
    }

    @Test
    void getStaff_CallsServiceWithCorrectId() {
        // Arrange
        when(staffService.getStaff(eq(STAFF_ID))).thenReturn(mockStaffResponse);

        // Act
        staffController.getStaff(STAFF_ID, userDetails);

        // Assert
        verify(staffService, times(1)).getStaff(eq(STAFF_ID));
    }

    @Test
    void getStaff_ServiceThrowsException_ExceptionPropagates() {
        // Arrange
        when(staffService.getStaff(anyLong()))
                .thenThrow(new RuntimeException("Staff not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffController.getStaff(STAFF_ID, userDetails);
        });
        verify(staffService, times(1)).getStaff(eq(STAFF_ID));
    }

    @Test
    void getStaff_UserDetailsProvided_DoesNotAffectRetrieval() {
        // Arrange
        when(staffService.getStaff(eq(STAFF_ID))).thenReturn(mockStaffResponse);

        // Act
        ResponseEntity<StaffResponse> response = staffController.getStaff(STAFF_ID, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService, times(1)).getStaff(eq(STAFF_ID));
        verify(userDetails, never()).getUsername();
    }

    // ==================== listActiveStaff Tests ====================

    @Test
    void listActiveStaff_Success_ReturnsOkWithStaffList() {
        // Arrange
        StaffResponse staff1 = new StaffResponse();
        staff1.setId(1L);
        staff1.setName("Staff One");
        staff1.setActive(true);

        StaffResponse staff2 = new StaffResponse();
        staff2.setId(2L);
        staff2.setName("Staff Two");
        staff2.setActive(true);

        List<StaffResponse> staffList = Arrays.asList(staff1, staff2);
        when(staffService.listActiveStaff(eq(FACILITY_ID))).thenReturn(staffList);

        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listActiveStaff(FACILITY_ID, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Staff One", response.getBody().get(0).getName());
        assertEquals("Staff Two", response.getBody().get(1).getName());
        verify(staffService, times(1)).listActiveStaff(eq(FACILITY_ID));
    }

    @Test
    void listActiveStaff_EmptyList_ReturnsOkWithEmptyList() {
        // Arrange
        when(staffService.listActiveStaff(eq(FACILITY_ID))).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listActiveStaff(FACILITY_ID, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(staffService, times(1)).listActiveStaff(eq(FACILITY_ID));
    }

    @Test
    void listActiveStaff_CallsServiceWithCorrectFacilityId() {
        // Arrange
        when(staffService.listActiveStaff(eq(FACILITY_ID))).thenReturn(Arrays.asList());

        // Act
        staffController.listActiveStaff(FACILITY_ID, userDetails);

        // Assert
        verify(staffService, times(1)).listActiveStaff(eq(FACILITY_ID));
    }

    @Test
    void listActiveStaff_ServiceThrowsException_ExceptionPropagates() {
        // Arrange
        when(staffService.listActiveStaff(anyLong()))
                .thenThrow(new RuntimeException("Facility not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffController.listActiveStaff(FACILITY_ID, userDetails);
        });
        verify(staffService, times(1)).listActiveStaff(eq(FACILITY_ID));
    }

    @Test
    void listActiveStaff_UserDetailsProvided_DoesNotAffectListing() {
        // Arrange
        when(staffService.listActiveStaff(eq(FACILITY_ID))).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listActiveStaff(FACILITY_ID, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService, times(1)).listActiveStaff(eq(FACILITY_ID));
        verify(userDetails, never()).getUsername();
    }

    // ==================== Constructor Tests ====================

    @Test
    void constructor_InjectsStaffServiceCorrectly() {
        // Arrange & Act
        StaffController controller = new StaffController(staffService);

        // Assert
        assertNotNull(controller);
    }

    @Test
    void constructor_NullStaffService_AllowsInstantiation() {
        // Arrange & Act
        StaffController controller = new StaffController(null);

        // Assert
        assertNotNull(controller);
    }
}