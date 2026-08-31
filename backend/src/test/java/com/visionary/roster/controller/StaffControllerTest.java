package com.visionary.roster.controller;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StaffController.
 * 
 * <p>This test class ensures 100% coverage of the StaffController business logic,
 * including successful operations and exception handling scenarios.</p>
 */
@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    @InjectMocks
    private StaffController staffController;

    private CreateStaffRequest validCreateRequest;
    private StaffResponse mockStaffResponse;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateStaffRequest();
        // Set up valid request fields as needed
        
        mockStaffResponse = new StaffResponse();
        mockStaffResponse.setId(1L);
        mockStaffResponse.setFirstName("John");
        mockStaffResponse.setLastName("Doe");
        mockStaffResponse.setEmploymentStatus("active");
    }

    // ==================== Constructor Tests ====================

    @Test
    void testConstructor_WithValidStaffService_ShouldInitializeController() {
        // Arrange
        StaffService service = mock(StaffService.class);
        
        // Act
        StaffController controller = new StaffController(service);
        
        // Assert
        assertNotNull(controller);
    }

    // ==================== createStaff() Tests ====================

    @Test
    void testCreateStaff_WithValidRequest_ShouldReturnCreatedStatus() {
        // Arrange
        when(staffService.createStaff(any(CreateStaffRequest.class))).thenReturn(mockStaffResponse);
        
        // Act
        ResponseEntity<StaffResponse> response = staffController.createStaff(validCreateRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockStaffResponse.getId(), response.getBody().getId());
        assertEquals(mockStaffResponse.getFirstName(), response.getBody().getFirstName());
        assertEquals(mockStaffResponse.getLastName(), response.getBody().getLastName());
        verify(staffService, times(1)).createStaff(validCreateRequest);
    }

    @Test
    void testCreateStaff_WithValidRequest_ShouldDelegateToService() {
        // Arrange
        when(staffService.createStaff(any(CreateStaffRequest.class))).thenReturn(mockStaffResponse);
        
        // Act
        staffController.createStaff(validCreateRequest);
        
        // Assert
        verify(staffService, times(1)).createStaff(validCreateRequest);
    }

    @Test
    void testCreateStaff_WithValidRequest_ShouldReturnStaffResponseInBody() {
        // Arrange
        when(staffService.createStaff(any(CreateStaffRequest.class))).thenReturn(mockStaffResponse);
        
        // Act
        ResponseEntity<StaffResponse> response = staffController.createStaff(validCreateRequest);
        
        // Assert
        assertNotNull(response.getBody());
        assertEquals(mockStaffResponse, response.getBody());
    }

    @Test
    void testCreateStaff_WhenServiceThrowsForbiddenAccessException_ShouldPropagateException() {
        // Arrange
        when(staffService.createStaff(any(CreateStaffRequest.class)))
                .thenThrow(new ForbiddenAccessException("Manager access required"));
        
        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () -> {
            staffController.createStaff(validCreateRequest);
        });
        verify(staffService, times(1)).createStaff(validCreateRequest);
    }

    @Test
    void testCreateStaff_WhenServiceThrowsIllegalArgumentException_ShouldPropagateException() {
        // Arrange
        when(staffService.createStaff(any(CreateStaffRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid date format"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            staffController.createStaff(validCreateRequest);
        });
        verify(staffService, times(1)).createStaff(validCreateRequest);
    }

    @Test
    void testCreateStaff_WithNullRequest_ShouldStillCallService() {
        // Arrange
        when(staffService.createStaff(null)).thenReturn(mockStaffResponse);
        
        // Act
        ResponseEntity<StaffResponse> response = staffController.createStaff(null);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(staffService, times(1)).createStaff(null);
    }

    // ==================== listStaff() Tests ====================

    @Test
    void testListStaff_WithDefaultEmploymentStatus_ShouldReturnOkStatus() {
        // Arrange
        List<StaffResponse> mockStaffList = Arrays.asList(mockStaffResponse);
        when(staffService.listStaff(eq("active"))).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff("active");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(staffService, times(1)).listStaff("active");
    }

    @Test
    void testListStaff_WithActiveStatus_ShouldDelegateToService() {
        // Arrange
        List<StaffResponse> mockStaffList = Arrays.asList(mockStaffResponse);
        when(staffService.listStaff(eq("active"))).thenReturn(mockStaffList);
        
        // Act
        staffController.listStaff("active");
        
        // Assert
        verify(staffService, times(1)).listStaff("active");
    }

    @Test
    void testListStaff_WithInactiveStatus_ShouldReturnInactiveStaff() {
        // Arrange
        StaffResponse inactiveStaff = new StaffResponse();
        inactiveStaff.setId(2L);
        inactiveStaff.setEmploymentStatus("inactive");
        List<StaffResponse> mockStaffList = Arrays.asList(inactiveStaff);
        when(staffService.listStaff(eq("inactive"))).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff("inactive");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("inactive", response.getBody().get(0).getEmploymentStatus());
        verify(staffService, times(1)).listStaff("inactive");
    }

    @Test
    void testListStaff_WithCustomStatus_ShouldPassStatusToService() {
        // Arrange
        String customStatus = "on_leave";
        List<StaffResponse> mockStaffList = Collections.emptyList();
        when(staffService.listStaff(eq(customStatus))).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff(customStatus);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService, times(1)).listStaff(customStatus);
    }

    @Test
    void testListStaff_WhenServiceReturnsEmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(staffService.listStaff(eq("active"))).thenReturn(Collections.emptyList());
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff("active");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(staffService, times(1)).listStaff("active");
    }

    @Test
    void testListStaff_WhenServiceReturnsMultipleStaff_ShouldReturnAllStaff() {
        // Arrange
        StaffResponse staff1 = new StaffResponse();
        staff1.setId(1L);
        staff1.setFirstName("John");
        
        StaffResponse staff2 = new StaffResponse();
        staff2.setId(2L);
        staff2.setFirstName("Jane");
        
        StaffResponse staff3 = new StaffResponse();
        staff3.setId(3L);
        staff3.setFirstName("Bob");
        
        List<StaffResponse> mockStaffList = Arrays.asList(staff1, staff2, staff3);
        when(staffService.listStaff(eq("active"))).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff("active");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("John", response.getBody().get(0).getFirstName());
        assertEquals("Jane", response.getBody().get(1).getFirstName());
        assertEquals("Bob", response.getBody().get(2).getFirstName());
        verify(staffService, times(1)).listStaff("active");
    }

    @Test
    void testListStaff_WhenServiceThrowsForbiddenAccessException_ShouldPropagateException() {
        // Arrange
        when(staffService.listStaff(any(String.class)))
                .thenThrow(new ForbiddenAccessException("Insufficient permissions"));
        
        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () -> {
            staffController.listStaff("active");
        });
        verify(staffService, times(1)).listStaff("active");
    }

    @Test
    void testListStaff_WithNullEmploymentStatus_ShouldPassNullToService() {
        // Arrange
        List<StaffResponse> mockStaffList = Arrays.asList(mockStaffResponse);
        when(staffService.listStaff(null)).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff(null);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService, times(1)).listStaff(null);
    }

    @Test
    void testListStaff_WithEmptyStringStatus_ShouldPassEmptyStringToService() {
        // Arrange
        List<StaffResponse> mockStaffList = Collections.emptyList();
        when(staffService.listStaff(eq(""))).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff("");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService, times(1)).listStaff("");
    }

    @Test
    void testListStaff_ShouldReturnListInResponseBody() {
        // Arrange
        List<StaffResponse> mockStaffList = Arrays.asList(mockStaffResponse);
        when(staffService.listStaff(eq("active"))).thenReturn(mockStaffList);
        
        // Act
        ResponseEntity<List<StaffResponse>> response = staffController.listStaff("active");
        
        // Assert
        assertNotNull(response.getBody());
        assertEquals(mockStaffList, response.getBody());
    }

    // ==================== Annotation Coverage Tests ====================

    @Test
    void testControllerAnnotations_ShouldHaveRestControllerAnnotation() {
        // Assert
        assertTrue(StaffController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    void testControllerAnnotations_ShouldHaveRequestMappingAnnotation() {
        // Assert
        assertTrue(StaffController.class.isAnnotationPresent(RequestMapping.class));
        RequestMapping mapping = StaffController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/v1/staff", mapping.value()[0]);
    }

    @Test
    void testControllerAnnotations_ShouldHaveCrossOriginAnnotation() {
        // Assert
        assertTrue(StaffController.class.isAnnotationPresent(CrossOrigin.class));
    }
}